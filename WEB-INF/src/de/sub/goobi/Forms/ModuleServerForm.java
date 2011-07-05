package de.sub.goobi.Forms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.xmlrpc.XmlRpcException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Modul.ExtendedDataImpl;
import de.sub.goobi.Modul.ExtendedProzessImpl;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.unigoettingen.goobi.module.api.exception.GoobiException;
import de.unigoettingen.goobi.module.api.exception.GoobiModuleException;
import de.unigoettingen.goobi.module.api.message.Message;
import de.unigoettingen.goobi.module.api.message.MessageContainer;
import de.unigoettingen.goobi.module.api.module_manager.GoobiModuleManager;
import de.unigoettingen.goobi.module.api.module_manager.ModuleDesc;
import de.unigoettingen.goobi.module.api.types.GoobiModuleParameter;
import de.unigoettingen.goobi.module.api.util.UniqueID;

public class ModuleServerForm {
   private Boolean running = false;
   private static GoobiModuleManager modulmanager = null;
   private static HashMap<String, String> myRunningShortSessions = new HashMap<String, String>();
   private ModuleDesc myModule;
   Helper help = new Helper();
   static ProzessDAO pdao = new ProzessDAO();
   Timer messageTimer;

   /**
    * initialize all modules
    * ================================================================
    */
   public void startAllModules() {
      if (modulmanager == null)
         readAllModulesFromConfiguraion();

      /* --------------------------------
       * alle Module initialisieren
       * --------------------------------*/
      for (ModuleDesc md : modulmanager) {
         try {
            md.getModuleClient().initialize();
         } catch (GoobiModuleException e) {
            help.setFehlerMeldung("GoobiModuleException im Modul " + md.getName() + " mit der URL "
                  + md.getUrl() + ": ", e.getMessage());
            e.printStackTrace();
         } catch (XmlRpcException e) {
            help.setFehlerMeldung("XmlRpcException im Modul " + md.getName() + " mit der URL " + md.getUrl()
                  + ": ", e.getMessage() + "\n" + help.getStacktraceAsString(e));
            //				e.printStackTrace();
         }
      }
      running = true;
   }

   /**
    * Read module configurations von xml-file  
    * ================================================================
    */
   public void readAllModulesFromConfiguraion() {
      if (modulmanager == null) {
         int port = ConfigMain.getIntParameter("goobiModuleServerPort");
         modulmanager = new GoobiModuleManager(port, new ExtendedProzessImpl(), new ExtendedDataImpl());

         /* --------------------------------
          * Nachrichtensystem initialisieren
          * --------------------------------*/
         int delay = 5000;
         int period = 1000;
         messageTimer = new Timer();
         messageTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
               ModuleServerForm.check_new_messages(modulmanager);
            }
         }, delay, period);

         /* --------------------------------
          * Alle Modulbeschreibungen aus der Konfigurationsdatei modules.xml einlesen
          * --------------------------------*/
         for (ModuleDesc md : getModulesFromConfigurationFile())
            modulmanager.add(md);
      }
      running = true;
   }

   /**
    * shutdown Modulmanager 
    * ================================================================
    */
   public void stopAllModules() {
      if (modulmanager != null)
         modulmanager.shutdown();
      modulmanager = null;
      running = false;
   }

   /**
    * initialize Module
   * ================================================================
   */
   public void startModule() {
      if (myModule == null)
         return;
      try {
         myModule.getModuleClient().initialize();
      } catch (GoobiModuleException e) {
         help.setFehlerMeldung("GoobiModuleException: ", e.getMessage() + "\n" + help.getStacktraceAsString(e));
         e.printStackTrace();
      } catch (XmlRpcException e) {
         help.setFehlerMeldung("XmlRpcException: ", e.getMessage() + "\n" + help.getStacktraceAsString(e));
         e.printStackTrace();
      }
   }

   /**
    * shut Module down
   * ================================================================
   */
   public void stopModule() {
      if (myModule == null)
         return;
      try {
         myModule.getModuleClient().shutdown();
      } catch (GoobiModuleException e) {
         help.setFehlerMeldung("GoobiModuleException: ", e.getMessage() + "\n" + help.getStacktraceAsString(e));
         e.printStackTrace();
      } catch (XmlRpcException e) {
         //         help.setFehlerMeldung("XmlRpcException: " + e.code + " ", e.getMessage());
         //         e.printStackTrace();
      }
   }

   //   /**
   //    * den Modulemanager durchlaufen und ein Module mit dem übergebenem Namen zurückgeben 
   //    * ================================================================
   //    */
   //   public ModuleDesc getModuleByName(String inName) {
   //      for (ModuleDesc md : modulmanager)
   //         if (md.getName().equals(inName))
   //            return md;
   //      return null;
   //   }

   /**
    * Module-Konfigurationen aus externer xml-Datei modules.xml einlesen 
    * ================================================================
    */
   private List<ModuleDesc> getModulesFromConfigurationFile() {
      List<ModuleDesc> rueckgabe = new ArrayList<ModuleDesc>();
      String filename = help.getGoobiConfigDirectory() + "modules.xml";
      if (!(new File(filename).exists())) {
         help.setFehlerMeldung("File not found: ", filename);
         return rueckgabe;
      }
      try {
         SAXBuilder builder = new SAXBuilder();
         Document doc = builder.build(new File(filename));
         Element root = doc.getRootElement();
         /* alle Module durchlaufen */
         for (Iterator iter = root.getChildren().iterator(); iter.hasNext();) {
            Element myModule = (Element) iter.next();
            //            System.out.println(myModule.getAttributeValue("url"));
            rueckgabe.add(new ModuleDesc(myModule.getAttributeValue("name"), myModule
                  .getAttributeValue("url"), null, myModule.getAttributeValue("description")));
         }
      } catch (JDOMException e1) {
         help.setFehlerMeldung("Fehler beim Einlesen, JDOMException: ", e1.getMessage() + "\n" + help.getStacktraceAsString(e1));
         e1.printStackTrace();
      } catch (IOException e1) {
         help.setFehlerMeldung("Fehler beim Einlesen, IOException: ", e1.getMessage() + "\n" + help.getStacktraceAsString(e1));
         e1.printStackTrace();
      }
      return rueckgabe;
   }

   /**
    * Eine Shortsession für einen Schritt starten    
   * ================================================================
   */
   public String startShortSession(Schritt inSchritt) throws GoobiException, XmlRpcException {
	  myModule = null;
	  if (inSchritt.getTypModulName() == null || inSchritt.getTypModulName().length() == 0) {
         help.setFehlerMeldung("Der Arbeitsschritt verfügt über kein Modul");
         return "";
      }

      /* ---------------------
       * zusätzliche Parameter neben dem Modulnamen
       * -------------------*/
      HashMap<String, Object> typeParameters = new HashMap<String, Object>();
      String schrittModuleName = inSchritt.getTypModulName();
      StrTokenizer tokenizer = new StrTokenizer(inSchritt.getTypModulName());
      int counter = 0;
      while (tokenizer.hasNext()) {
         String tok = (String) tokenizer.next();
         if (counter == 0)
            schrittModuleName = tok;
         else {
            if (tok.contains(":")) {
               String key = tok.split(":")[0];
               String value = tok.split(":")[1];
               typeParameters.put(key, value);
            }
         }
         counter++;
      }

      /* --------------------------------
       * Modulserver läuft noch nicht
      * --------------------------------*/
      if (modulmanager == null)
         throw new GoobiException(0, "Der Modulserver läuft nicht");

      /* --------------------------------
       * ohne gewähltes Modul gleich wieder raus
       * --------------------------------*/
      for (ModuleDesc md : modulmanager)
         if (md.getName().equals(schrittModuleName))
            myModule = md;
      if (myModule == null) {
         help.setFehlerMeldung("Das Modul wurde nicht gefunden");
         return "";
      }

      /* --------------------------------
       * Verbindung zum Modul aufbauen und url zurückgeben
       * --------------------------------*/
      String processId = String.valueOf(inSchritt.getProzess().getId().intValue());
      String tempID = UniqueID.generate_session();
      myRunningShortSessions.put(tempID, processId);

      GoobiModuleParameter gmp1 = new GoobiModuleParameter(processId, tempID,
            myModule.getModuleClient().longsessionID, typeParameters);
      HttpSession insession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
            .getSession(false);

      // String applicationUrl = ConfigMain.getParameter("ApplicationWebsiteUrl");
      String applicationUrl = new HelperForm().getServletPathWithHostAsUrl();
      gmp1.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/AktuelleSchritteBearbeiten.jsf?jsessionId="
            + insession.getId());
      myModule.getGmps().add(gmp1); // add session in den Manager
      return myModule.getModuleClient().start(gmp1);
   }

   /**
    * Test for shortsession of selected module
   * ================================================================
   */
   public void startShortSessionTest() throws GoobiException, XmlRpcException {
      /* --------------------------------
       * ohne gewähltes Modul gleich wieder raus
       * --------------------------------*/
      if (myModule == null)
         return;

      String processId = "3346";
      String tempID = UniqueID.generate_session();
      myRunningShortSessions.put(tempID, processId);
      GoobiModuleParameter gmp = new GoobiModuleParameter(processId, tempID,
            myModule.getModuleClient().longsessionID, null);
      //		gmp.put("return_url", "http://localhost:8081/Goobi/newpages/aktiveModule.jsf?sessionId="
      //				+ tempID);
      String applicationUrl = ConfigMain.getParameter("ApplicationWebsiteUrl");
      //      String applicationUrl = new HelperForm().getServletPathWithHostAsUrl();
      gmp.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/aktiveModule.jsf?sessionId=" + tempID);
      gmp.put("type", "PRODUCE");

      myModule.getGmps().add(gmp); // add shortsession in den Manager
      help.setMeldung(myModule.getModuleClient().start(gmp));
      help.setMeldung(gmp.toString());

      /* und Shortsession wieder stoppen */
      //modules.get(0).getModuleClient().stop(gmp1);
      // shutdown
      // try{
      // modules.get(0).moduleClient.shutdown();
      // }catch (XmlRpcException e){ }
   }

   /*#####################################################
    #####################################################
    ##																															 
    ##											          Messaging system
    ##                                                   															    
    #####################################################
    ####################################################*/

   /**
    * Diese Methode überprüft, ob wir was neues in unserem Container haben.
    * ================================================================
    */
   private static void check_new_messages(GoobiModuleManager modules) {
      Message message = null;
      while (MessageContainer.size() > 0) {
         message = MessageContainer.pop();
         handle_message(message, modules);
      }
   }

   /**
    * Mit dieser Methode werden die Messages abgearbeitet
    * @param message Messages
    * @param modules GoobiModuleManager
    * ================================================================
    */
   private static void handle_message(Message message, GoobiModuleManager modules) {
      if ((message.body.error.faultCode == 0) && (message.body.error.faultString.equals("END"))) {
         String in_session = message.from;
         /*
          * erstmal wird geschaut welcher module und welche session ist
          * der Absender.
          */
         int i = 0;
         if (modules.size() == i) {
         } else {
            i++;
            ArrayList<GoobiModuleParameter> gmps = modules.get(i - 1).getGmps();
            for (GoobiModuleParameter gmp : gmps) {
               if (gmp.sessionId.equals(in_session)) {
                  /*
                   * Jetzt wird herausgefunden um was für eine Message es
                   * geht. dabei ist: Module : modules.get(i) Session wird
                   * durch gmp beschrieben
                   */
                  if (message.body.type.equals("trigger")) {
                     // behandlung aller trigger Messages
                     /*
                      *     Behandlung von "trigger - END"
                      */
                     if ((message.body.error.faultCode == 0)
                           && (message.body.error.faultString.equals("END"))) {
                        try {
                           modules.get(i - 1).getModuleClient().stop(gmp);
                        } catch (GoobiModuleException e) {
                           e.printStackTrace();
                        } catch (XmlRpcException e) {
                           e.printStackTrace();
                        }
                     }
                  } else if (message.body.type.equals("log")) {
                     // behandlung aller log Messages
                  }
               }
            }
         }
      }
   }

   /*#####################################################
    #####################################################
    ##																															 
    ##										Getter und Setter									
    ##                                                   															    
    #####################################################
    ####################################################*/

   public GoobiModuleManager getModulmanager() {
      return modulmanager;
   }

   public ModuleDesc getMyModule() {
      return myModule;
   }

   public void setMyModule(ModuleDesc myModule) {
      this.myModule = myModule;
   }

   public static String getProcessIDFromShortSession(String sessionId) {
      return myRunningShortSessions.get(sessionId);
   }

   public Boolean getRunning() {
      return running;
   }

   /**
       * get Prozess von shortSessionID
      * ================================================================
      */
   public static Prozess getProcessFromShortSession(String sessionId) throws GoobiException {
      String prozessidStr = getProcessIDFromShortSession(sessionId);
      try {
         Prozess tempProz = pdao.get(Integer.parseInt(prozessidStr));
         Helper.getHibernateSession().flush();
         Helper.getHibernateSession().clear();
         if (tempProz != null && tempProz.getId() != null)
            Helper.getHibernateSession().refresh(tempProz);

         return tempProz;
      } catch (NumberFormatException e) {
         throw new GoobiException(5, "******** wrapped NumberFormatException ********: " + e.getMessage() + "\n" + new Helper().getStacktraceAsString(e));
      } catch (DAOException e) {
         throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + new Helper().getStacktraceAsString(e));
      }
   }

}
