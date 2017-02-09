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

package de.sub.goobi.modul;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import de.sub.goobi.forms.ModuleServerForm;

public class ModulListener implements ServletContextListener {
   private static final Logger logger = Logger.getLogger(ModulListener.class);

   @Override
   public void contextInitialized(ServletContextEvent event) {
      logger.debug("Starte Modularisierung-Server", null);
      new ModuleServerForm().startAllModules();
      logger.debug("Gestartet: Modularisierung-Server", null);
   }

   @Override
   public void contextDestroyed(ServletContextEvent event) {
      logger.debug("Stoppe Modularisierung-Server", null);
      new ModuleServerForm().stopAllModules();
      logger.debug("Gestoppt: Modularisierung-Server", null);
   }
}
