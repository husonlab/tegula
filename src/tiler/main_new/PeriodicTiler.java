/*
 *  Copyright (C) 2018 University of Tuebingen
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

package tiler.main_new;

import com.briksoftware.javafx.platform.osx.OSXIntegration;
import javafx.application.Application;
import javafx.stage.Stage;
import jloda.fx.SplashScreen;
import jloda.util.*;

import java.io.File;
import java.time.Duration;

/**
 * starts the main tiler program
 * todo: this will replace Main.java in the future
 * Daniel Huson, 11.2018
 */
public class PeriodicTiler extends Application {
    /**
     * launch the program
     *
     * @param argv
     * @throws CanceledException
     * @throws UsageException
     */
    public static void main(String[] argv) throws CanceledException, UsageException {
        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();

        ProgramProperties.setProgramName(Version.NAME);
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);

        final ArgsOptions options = new ArgsOptions(argv, PeriodicTiler.class, "PeriodicTiler - 2D tiler");
        options.setAuthors("Daniel H. Huson, Klaus Westphal, Ruediger Zeller with contributions from Julius Vetter and Cornelius Wiehl");
        options.setLicense("This is an early (ALPHA) version of Periodic, made available for testing purposes. Source code will be released wih first official version");
        options.setVersion(ProgramProperties.getProgramVersion());
        final String defaultPropertiesFile;
        if (ProgramProperties.isMacOS())
            defaultPropertiesFile = System.getProperty("user.home") + "/Library/Preferences/PeriodicTiler.def";
        else
            defaultPropertiesFile = System.getProperty("user.home") + File.separator + ".PeriodicTiler.def";
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
            System.err.println(jloda.util.Version.getVersion(PeriodicTiler.class, ProgramProperties.getProgramName()));
            System.err.println("Java version: " + System.getProperty("java.version"));
        }

        Application.launch(argv);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(ProgramProperties.getProgramName());

        new MainView(primaryStage);
        primaryStage.sizeToScene();

        // setup about and preferences menu for apple:
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
    public void stop() throws Exception {
        super.stop();
        ProgramProperties.store();
    }
}
