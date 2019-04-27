/*
 * Tegula.java Copyright (C) 2019. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tegula.main;

import com.briksoftware.javafx.platform.osx.OSXIntegration;
import javafx.application.Application;
import javafx.stage.Stage;
import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.UsageException;

import java.io.File;
import java.time.Duration;

/**
 * starts the main tiler program
 * Daniel Huson, 4.2019
 */
public class Tegula extends Application {
    private static String[] inputFilesAtStartup;

    @Override
    public void init() throws Exception {
        ProgramPropertiesFX.setUseGUI(true);
    }

    /**
     * main
     *
     * @param args
     */
    public static void main(String[] args) throws CanceledException, UsageException {
        parseArguments(args);
        launch(args);
    }

    private static void parseArguments(String[] args) throws CanceledException, UsageException {
        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();

        ProgramPropertiesFX.setProgramName(Version.NAME);
        ProgramPropertiesFX.setProgramVersion(Version.SHORT_DESCRIPTION);

        final ArgsOptions options = new ArgsOptions(args, Tegula.class, Version.NAME + " - Interactive periodic tilings");
        options.setAuthors("Daniel H. Huson, Klaus Westphal and Ruediger Zeller, with contributions from Julius Vetter and Cornelius Wiehl");
        options.setLicense("This is an early (ALPHA) version of TILER, made available for testing purposes. Source code will be released on publication.");
        options.setVersion(ProgramPropertiesFX.getProgramVersion());

        options.comment("Input:");
        inputFilesAtStartup = options.getOption("-i", "input", "Input file(s)", new String[0]);


        final String defaultPropertiesFile;
        if (ProgramPropertiesFX.isMacOS())
            defaultPropertiesFile = System.getProperty("user.home") + "/Library/Preferences/Tegula.def";
        else
            defaultPropertiesFile = System.getProperty("user.home") + File.separator + ".Tegula.def";
        final String propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", defaultPropertiesFile);
        final boolean showVersion = options.getOption("-V", "version", "Show version string", false);
        final boolean silentMode = options.getOption("-S", "silentMode", "Silent mode", false);
        options.done();

        ProgramPropertiesFX.load(propertiesFile);

        if (silentMode) {
            Basic.stopCollectingStdErr();
            Basic.hideSystemErr();
            Basic.hideSystemOut();
        }

        if (showVersion) {
            System.err.println(ProgramPropertiesFX.getProgramVersion());
            System.err.println(jloda.util.Version.getVersion(Tegula.class, ProgramPropertiesFX.getProgramName()));
            System.err.println("Java version: " + System.getProperty("java.version"));
        }
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(ProgramPropertiesFX.getProgramName());

        final Window mainWindow = new Window();
        MainWindowManager.getInstance().addMainWindow(mainWindow);

        // todo: setup file opener
        //RecentFilesManager.getInstance().setFileOpener(OpenFileAction.fileOpener());

        final WindowGeometry windowGeometry = new WindowGeometry(ProgramPropertiesFX.get("WindowGeometry", "50 50 800 800"));


        mainWindow.show(primaryStage, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
        for (String fileName : inputFilesAtStartup) {
            //OpenFileAction.fileOpener().accept(fileName);
        }

        // setup about and preferences menu for apple:
        SplashScreen.setVersionString(Version.SHORT_DESCRIPTION);
        OSXIntegration.init();
        OSXIntegration.populateAppleMenu(() -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(1)), () -> System.err.println("Preferences"));

        // open files by double-click under Mac OS: // untested
        OSXIntegration.setOpenFilesHandler(files -> {
            for (File file : files) {
                System.err.println("Open file " + file + ": not implemented");
            }
        });
    }

    @Override
    public void stop() {
        ProgramPropertiesFX.store();
        System.exit(0);

    }
}
