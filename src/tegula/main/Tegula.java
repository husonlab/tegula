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
import jloda.fx.util.ColorSchemeManager;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.ResourceManagerFX;
import jloda.fx.window.MainWindowManager;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgramProperties;
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
        ProgramProperties.setUseGUI(true);

        ColorSchemeManager.BuiltInColorTables=new String[]{"Alhambra;6;0X4d66cc;0Xb3e6e6;0Xcc9933;0X669966;0X666666;0X994d00;" +
                "Caspian8;8;0Xf64d1b;0X8633bc;0X41a744;0X747474;0X2746bc;0Xff9301;0Xc03150;0X2198bc;" +
                "Fews8;8;0X5da6dc;0Xfba53a;0X60be68;0Xf27db0;0Xb39230;0Xb376b2;0Xdfd040;0Xf15954;" +
                "Pairs12;12;0X267ab2;0Xa8cfe3;0X399f34;0Xb4df8e;0Xe11f27;0Xfa9b9b;0Xfe7f23;0Xfcbf75;0X6a4199;0Xcab3d6;0Xb05a2f;0Xffff9f;" +
                "Pale12;12;0Xdbdada;0Xf27e75;0Xba7bbd;0Xceedc5;0Xfbf074;0Xf8cbe5;0Xf9b666;0Xfdffb6;0X86b0d2;0X95d6c8;0Xb3e46c;0Xbfb8da;" +
                "Rainbow13;13;0Xed1582;0Xf73e43;0Xee8236;0Xe5ae3d;0Xe5da45;0Xa1e443;0X22da27;0X21d18e;0X21c8c7;0X1ba2fc;0X2346fb;0X811fd9;0X9f1cc5;" +
                "Retro29;29;0Xf4d564;0X97141d;0Xe9af6b;0X82ae92;0X356c7c;0X5c8c83;0X3a2b27;0Xe28b90;0X242666;0Xc2a690;0Xb80614;0X35644f;0Xe3a380;0Xb9a253;0X72a283;0X73605b;0X94a0ad;0Xf7a09d;0Xe5c09e;0X4a4037;0Xcec07c;0X6c80bb;0X7fa0a4;0Xb9805b;0Xd5c03f;0Xdd802e;0X8b807f;0Xc42030;0Xc2603d;" +
                "Sea9;9;0Xffffdb;0Xedfbb4;0Xc9ecb6;0X88cfbc;0X56b7c4;0X3c90bf;0X345aa7;0X2f2b93;0X121858;"};
    }

    /**
     * main
     *
     * @param args
     */
    public static void main(String[] args) throws CanceledException, UsageException {
        ResourceManagerFX.addResourceRoot(Tegula.class, "tegula.resources");
        ProgramProperties.setProgramName(Version.NAME);
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        ProgramProperties.setProgramLicence("This is an early (ALPHA) version of Tegula, made available for testing purposes. Source code will be released on publication.");
        SplashScreen.setVersionString(Version.SHORT_DESCRIPTION);

        parseArguments(args);
        launch(args);
    }

    protected static void parseArguments(String[] args) throws CanceledException, UsageException {
        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();

        final ArgsOptions options = new ArgsOptions(args, Tegula.class, Version.NAME + " - Interactive periodic tilings");
        options.setAuthors("Daniel H. Huson, Klaus Westphal and Ruediger Zeller, with contributions from Julius Vetter and Cornelius Wiehl");
        options.setLicense(ProgramProperties.getProgramLicence());
        options.setVersion(ProgramProperties.getProgramVersion());

        options.comment("Input:");
        inputFilesAtStartup = options.getOption("-i", "input", "Input file(s)", new String[0]);


        final String defaultPropertiesFile;
        if (ProgramProperties.isMacOS())
            defaultPropertiesFile = System.getProperty("user.home") + "/Library/Preferences/Tegula.def";
        else
            defaultPropertiesFile = System.getProperty("user.home") + File.separator + ".Tegula.def";
        final String propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", defaultPropertiesFile);
        final boolean showVersion = options.getOption("-V", "version", "Show version string", false);
        final boolean silentMode = options.getOption("-S", "silentMode", "Silent mode", false);
        options.done();

        ProgramProperties.load(propertiesFile);

        if (silentMode) {
            Basic.stopCollectingStdErr();
            Basic.hideSystemErr();
            Basic.hideSystemOut();
        }

        if (showVersion) {
            System.err.println(ProgramProperties.getProgramVersion());
            System.err.println(jloda.util.Version.getVersion(Tegula.class, ProgramProperties.getProgramName()));
            System.err.println("Java version: " + System.getProperty("java.version"));
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setTitle(ProgramProperties.getProgramName());
            NotificationManager.setShowNotifications(true);


            final Window mainWindow = new Window();
            MainWindowManager.getInstance().addMainWindow(mainWindow);

            // todo: setup file opener
            //RecentFilesManager.getInstance().setFileOpener(FileOpenManager.fileOpener());

            final WindowGeometry windowGeometry = new WindowGeometry(ProgramProperties.get("WindowGeometry", "50 50 800 800"));


            mainWindow.show(primaryStage, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());
            for (String fileName : inputFilesAtStartup) {
                //FileOpenManager.fileOpener().accept(fileName);
            }

            // setup about and preferences menu for apple:
            if (false) {
                OSXIntegration.init();
                OSXIntegration.populateAppleMenu(() -> SplashScreen.getInstance().showSplash(Duration.ofMinutes(1)), () -> System.err.println("Preferences"));

                // open files by do uble-click under Mac OS: // untested
                OSXIntegration.setOpenFilesHandler(files -> {
                    for (File file : files) {
                        System.err.println("Open file " + file + ": not implemented");
                    }
                });
            }

        } catch (Exception ex) {
            Basic.caught(ex);
            throw ex;
        }
    }

    @Override
    public void stop() {
        ProgramProperties.store();
        System.exit(0);

    }
}
