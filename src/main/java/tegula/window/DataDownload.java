/*
 * DataDownload.java Copyright (C) 2026 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tegula.window;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.util.ProgramProperties;
import jloda.fx.window.NotificationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * downloads a database of tilings from the data release that accompanies the program.
 * <p>
 * The databases are hundreds of megabytes and do not change from one release of the program to the next, so
 * they are not shipped with it and are not held in the source repository: they live as assets of a single
 * GitHub release, and are fetched on demand. Where a database was put is remembered, both so that the program
 * can offer it again and so that it knows not to ask on every start.
 * Daniel Huson, 9.2026
 */
public class DataDownload {
	/**
	 * the release that holds the databases. Assets of a release are flat, so the names here are the names the
	 * files must be uploaded under
	 */
	private static final String RELEASE_URL = "https://github.com/husonlab/tegula/releases/download/data-v1/";

	/**
	 * where the last downloaded database was put, and the directory it was put in. The first is also how the
	 * program knows whether it has ever been given any data
	 */
	public static final String DOWNLOADED_FILE = "DownloadedDataFile";
	public static final String DOWNLOAD_DIRECTORY = "DownloadDataDirectory";

	/**
	 * one database, as offered to the user
	 *
	 * @param fileName    the name of the release asset, and of the file written
	 * @param description what it contains, shown in the menu and in the dialog
	 * @param megaBytes   roughly how large it is, so that the user can judge before starting
	 */
	public record DataSet(String fileName, String description, int megaBytes, int expandedMegaBytes) {
		public String url() {
			return RELEASE_URL + fileName;
		}

		/**
		 * the name the database has once expanded, which is what the user opens. SQLite needs a real file,
		 * so a database cannot be read compressed and is expanded as it arrives
		 */
		public String expandedFileName() {
			return fileName.endsWith(".gz") ? fileName.substring(0, fileName.length() - 3) : fileName;
		}

		@Override
		public String toString() {
			return "%s (%d MB download, %,d MB on disk) - %s".formatted(expandedFileName(), megaBytes, expandedMegaBytes, description);
		}
	}

	// the sizes are those of the assets of the data-v1 release, and of the files they expand to
	public static final DataSet ALL_TO_18 =
			new DataSet("tilings-1-18.tdb.gz", "all tilings of Dress complexity at most 18", 200, 1480);
	public static final DataSet EUCLIDEAN_TO_24 =
			new DataSet("euclidean-1-24.tdb.gz", "all euclidean tilings of Dress complexity at most 24", 74, 600);
	public static final DataSet SPHERICAL_TO_24 =
			new DataSet("spherical-1-24.tdb.gz", "all spherical tilings of Dress complexity at most 24", 79, 755);

	/**
	 * every database on offer, the first being the one a program offers when it offers only one
	 */
	public static List<DataSet> all() {
		return List.of(ALL_TO_18, EUCLIDEAN_TO_24, SPHERICAL_TO_24);
	}

	/**
	 * has the user ever been given a database? Used to decide whether to offer one at startup
	 */
	public static boolean haveData() {
		final String path = ProgramProperties.get(DOWNLOADED_FILE, "");
		return !path.isBlank() && Files.exists(Path.of(path));
	}

	/**
	 * on the first start, ask whether to fetch a database, because the program can do nothing without one.
	 * Asks once: answering no records that, so that the question does not come back every time
	 */
	public static void offerOnFirstStart(Stage owner, DataSet dataSet) {
		if (haveData() || ProgramProperties.get(DOWNLOADED_FILE, "").equals("declined"))
			return;

		final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.initOwner(owner);
		alert.setTitle("Download tilings");
		alert.setHeaderText("This program needs a database of tilings");
		alert.setContentText("""
				This program browses databases of enumerated tilings, and none has been downloaded yet.

				Download %s now? The download is about %d MB and expands to about %,d MB. It is needed only
				once: the data does not change from one release to the next.

				You can also do this later, with File -> Download.""".formatted(dataSet.expandedFileName(), dataSet.megaBytes(), dataSet.expandedMegaBytes()));
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		alert.showAndWait().ifPresent(answer -> {
			if (answer == ButtonType.YES)
				show(owner, dataSet);
			else // remember, so as not to ask again on every start
				ProgramProperties.put(DOWNLOADED_FILE, "declined");
		});
	}

	/**
	 * asks where to put the database and then fetches it, showing how far it has got and letting it be stopped
	 */
	public static void show(Stage owner, DataSet dataSet) {
		final DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Where to put " + dataSet.expandedFileName());
		final String previous = ProgramProperties.get(DOWNLOAD_DIRECTORY, System.getProperty("user.home"));
		final java.io.File previousDirectory = new java.io.File(previous);
		if (previousDirectory.isDirectory())
			chooser.setInitialDirectory(previousDirectory);

		final java.io.File directory = chooser.showDialog(owner);
		if (directory == null)
			return;
		ProgramProperties.put(DOWNLOAD_DIRECTORY, directory.getPath());

		final Path target = directory.toPath().resolve(dataSet.expandedFileName());
		if (Files.exists(target)) {
			final Alert exists = new Alert(Alert.AlertType.CONFIRMATION,
					target + " already exists. Download it again?", ButtonType.YES, ButtonType.NO);
			exists.initOwner(owner);
			if (exists.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
				ProgramProperties.put(DOWNLOADED_FILE, target.toString()); // it is there, so we do have data
				return;
			}
		}
		runDownload(owner, dataSet, target);
	}

	/**
	 * the dialog that shows the download running, and the task behind it
	 */
	private static void runDownload(Stage owner, DataSet dataSet, Path target) {
		final Stage stage = new Stage();
		stage.initOwner(owner);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Downloading " + dataSet.expandedFileName());

		final Label what = new Label(dataSet.description());
		final Label where = new Label("To: " + target);
		final Label status = new Label("Contacting the server...");
		final ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		final Button cancelButton = new Button("Cancel");

		final HBox buttons = new HBox(cancelButton);
		buttons.setAlignment(Pos.CENTER_RIGHT);
		final VBox vBox = new VBox(what, where, progressBar, status, buttons);
		vBox.setSpacing(8);
		vBox.setPadding(new Insets(12));
		VBox.setVgrow(progressBar, Priority.NEVER);
		stage.setScene(new Scene(vBox, 520, 180));

		final Task<Path> task = downloadTask(dataSet, target);
		progressBar.progressProperty().bind(task.progressProperty());
		status.textProperty().bind(task.messageProperty());
		cancelButton.setOnAction(e -> task.cancel());
		stage.setOnCloseRequest(e -> task.cancel());

		task.setOnSucceeded(e -> {
			ProgramProperties.put(DOWNLOADED_FILE, target.toString());
			stage.close();
			NotificationManager.showInformation("Downloaded " + target.getFileName()
											   + "\nOpen it with File -> Open");
		});
		task.setOnCancelled(e -> {
			deleteQuietly(target); // a half a database is of no use to anybody
			stage.close();
			NotificationManager.showWarning("Download cancelled");
		});
		task.setOnFailed(e -> {
			deleteQuietly(target);
			stage.close();
			final Throwable ex = task.getException();
			NotificationManager.showError("Download failed: " + (ex == null ? "unknown reason" : ex.getMessage()));
		});

		final Thread thread = new Thread(task, "download-" + dataSet.expandedFileName());
		thread.setDaemon(true);
		thread.start();
		stage.show();
	}

	/**
	 * fetches the file, reporting how far it has got. Written to a part file and moved into place at the end,
	 * so that an interrupted download cannot be mistaken for a database
	 */
	static Task<Path> downloadTask(DataSet dataSet, Path target) {
		return downloadTask(dataSet.url(), target);
	}

	/**
	 * fetches a gzipped file from any URL and writes it out expanded. Separate from the data sets so that
	 * it can be run against a local server in a test
	 */
	static Task<Path> downloadTask(String url, Path target) {
		return new Task<>() {
			@Override
			protected Path call() throws Exception {
				final Path part = target.resolveSibling(target.getFileName() + ".part");
				final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
				final HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();

				updateMessage("Contacting the server...");
				final HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
				if (response.statusCode() != 200)
					throw new IOException("Server returned " + response.statusCode() + " for " + url);

				final long total = response.headers().firstValueAsLong("content-length").orElse(-1);
				final byte[] buffer = new byte[1 << 16];
				final long[] compressedRead = {0};

				// expand as it arrives, rather than writing the archive and expanding it afterwards: that
				// halves the disk traffic and never leaves a copy of the archive behind. Progress is counted
				// on the bytes coming off the network, which is what content-length measures, so the counting
				// stream sits below the decompressor rather than above it
				try (InputStream counting = new java.io.FilterInputStream(response.body()) {
					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						final int count = super.read(b, off, len);
						if (count > 0) compressedRead[0] += count;
						return count;
					}
				};
					 InputStream ins = new java.util.zip.GZIPInputStream(counting, 1 << 16);
					 OutputStream outs = Files.newOutputStream(part)) {
					int count;
					while ((count = ins.read(buffer)) > 0) {
						if (isCancelled()) {
							deleteQuietly(part);
							return null;
						}
						outs.write(buffer, 0, count);
						if (total > 0) {
							updateProgress(compressedRead[0], total);
							updateMessage("%,d of %,d MB downloaded and expanded".formatted(compressedRead[0] >> 20, total >> 20));
						} else
							updateMessage("%,d MB downloaded".formatted(compressedRead[0] >> 20));
					}
				} catch (Exception ex) {
					deleteQuietly(part);
					throw ex;
				}
				Files.move(part, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				return target;
			}
		};
	}

	private static void deleteQuietly(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException ignored) {
		}
	}

	/**
	 * runs the first-start offer once the window is up, so that the dialog has something to sit on
	 */
	public static void offerLater(Stage owner, DataSet dataSet) {
		Platform.runLater(() -> offerOnFirstStart(owner, dataSet));
	}
}
