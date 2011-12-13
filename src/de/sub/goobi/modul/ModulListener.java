package de.sub.goobi.modul;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import de.sub.goobi.forms.ModuleServerForm;

public class ModulListener implements ServletContextListener {
   private static final Logger myLogger = Logger.getLogger(ModulListener.class);
   

   

   public void contextInitialized(ServletContextEvent event) {
      myLogger.debug("Starte Modularisierung-Server", null);
//      modules = new ExtendedGoobiModuleManager(8000);
//      modules
//            .add(new ModuleDesc("Hotburn Module", "http://localhost:8095", null, "unser Hotburn Testmodule"));
      new ModuleServerForm().startAllModules();
      myLogger.debug("Gestartet: Modularisierung-Server", null);
   }

   

   public void contextDestroyed(ServletContextEvent event) {
      myLogger.debug("Stoppe Modularisierung-Server", null);
//      modules.shutdown();
      new ModuleServerForm().stopAllModules();
      myLogger.debug("Gestoppt: Modularisierung-Server", null);
   }
}