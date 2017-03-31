/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.kitodo.modul;

import de.sub.kitodo.forms.ModuleServerForm;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ModulListener implements ServletContextListener {
    private static final Logger myLogger = Logger.getLogger(ModulListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        myLogger.debug("Starte Modularisierung-Server", null);
        new ModuleServerForm().startAllModules();
        myLogger.debug("Gestartet: Modularisierung-Server", null);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        myLogger.debug("Stoppe Modularisierung-Server", null);
        new ModuleServerForm().stopAllModules();
        myLogger.debug("Gestoppt: Modularisierung-Server", null);
    }
}
