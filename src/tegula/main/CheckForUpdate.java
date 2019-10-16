/*
 * CheckForUpdates.java Copyright (C) 2019. Daniel H. Huson
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

import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;
import com.install4j.api.update.UpdateDescriptorEntry;
import jloda.fx.window.NotificationManager;
import jloda.swing.util.InfoMessage;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import javax.swing.*;

/**
 * check for update and install, if present
 * Daniel Huson, 5.2018
 */
public class CheckForUpdate {
    /**
     * check for update and install, if present
     */
    public static void apply(String projectName) {
        ApplicationDisplayMode applicationDisplayMode = ProgramProperties.isUseGUI() ? ApplicationDisplayMode.GUI : ApplicationDisplayMode.CONSOLE;
        UpdateDescriptor updateDescriptor;
        try {
            updateDescriptor = UpdateChecker.getUpdateDescriptor("http://software-ab.informatik.uni-tuebingen.de/download/tegula/updates.xml", applicationDisplayMode);
        } catch (Exception e) {
            Basic.caught(e);
            // NotificationManager.showInformation("Installed version is up-to-date");
            new InfoMessage("Installed version is up-to-date");
            return;
        }
        if (updateDescriptor.getEntries().length > 0) {
            if (!ProgramProperties.isUseGUI()) {
                UpdateDescriptorEntry entry = updateDescriptor.getEntries()[0];
                NotificationManager.showInformation("New version available: " + entry.getNewVersion() + "\nPlease download from: http://software-ab.informatik.uni-tuebingen.de/download/tegula");
                return;
            }
        } else {
            NotificationManager.showInformation("Installed version is up-to-date");
            return;
        }


        final Runnable runnable = () -> {
            System.err.println("Launching update dialog");
            ApplicationLauncher.launchApplicationInProcess("1691242391", null, new ApplicationLauncher.Callback() {
                public void exited(int exitValue) {
                    System.err.println("Exit value: " + exitValue);
                }

                public void prepareShutdown() {
                    ProgramProperties.store();
                }
            }, ApplicationLauncher.WindowMode.FRAME, null);
        };
        SwingUtilities.invokeLater(runnable);
        //Executors.newSingleThreadExecutor().submit(runnable);
    }
}
