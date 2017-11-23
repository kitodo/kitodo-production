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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.modul.ExtendedDataImpl;
import de.sub.goobi.modul.ExtendedProzessImpl;
import de.unigoettingen.goobi.module.api.exception.GoobiException;
import de.unigoettingen.goobi.module.api.exception.GoobiModuleException;
import de.unigoettingen.goobi.module.api.message.Message;
import de.unigoettingen.goobi.module.api.message.MessageContainer;
import de.unigoettingen.goobi.module.api.module_manager.GoobiModuleManager;
import de.unigoettingen.goobi.module.api.module_manager.ModuleDesc;
import de.unigoettingen.goobi.module.api.types.GoobiModuleParameter;
import de.unigoettingen.goobi.module.api.util.UniqueID;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

@Named("ModuleServerForm")
@SessionScoped
public class ModuleServerForm implements Serializable {
    private static final long serialVersionUID = -3296039795297271346L;
    private Boolean running = false;
    private static volatile GoobiModuleManager modulmanager = null;
    private static HashMap<String, String> myRunningShortSessions = new HashMap<>();
    private ModuleDesc myModule;
    Helper help = new Helper();
    Timer messageTimer;
    private static final Logger logger = LogManager.getLogger(ModuleServerForm.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * initialize all modules.
     */
    public void startAllModules() {
        if (modulmanager == null) {
            readAllModulesFromConfiguration();
        }

        /*
         * alle Module initialisieren
         */
        for (ModuleDesc md : modulmanager) {
            try {
                md.getModuleClient().initialize();
            } catch (GoobiModuleException e) {
                Helper.setFehlerMeldung(
                        "GoobiModuleException im Modul " + md.getName() + " mit der URL " + md.getUrl() + ": ",
                        e.getMessage());
                logger.error(e);
            } catch (XmlRpcException e) {
                Helper.setFehlerMeldung(
                        "XmlRpcException im Modul " + md.getName() + " mit der URL " + md.getUrl() + ": ",
                        e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
            }
        }
        running = true;
    }

    /**
     * Start module server.
     */
    public void readAllModulesFromConfiguration() {
        if (modulmanager == null) {
            synchronized (ModuleServerForm.class) {
                if (modulmanager == null) {
                    int port = ConfigCore.getIntParameter("kitodoModuleServerPort");
                    final GoobiModuleManager manager = new GoobiModuleManager(port, new ExtendedProzessImpl(),
                            new ExtendedDataImpl());

                    // Alle Modulbeschreibungen aus der Konfigurationsdatei
                    // modules.xml einlesen
                    manager.addAll(getModulesFromConfigurationFile());
                    modulmanager = manager;
                }

                // Nachrichtensystem initialisieren
                int delay = 5000;
                int period = 1000;
                messageTimer = new Timer();
                messageTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        ModuleServerForm.check_new_messages(modulmanager);
                    }
                }, delay, period);

                running = true;
            }
        }
    }

    /**
     * shutdown Modulmanager.
     */
    public void stopAllModules() {
        if (modulmanager != null) {
            modulmanager.shutdown();
        }
        modulmanager = null;
        running = false;
    }

    /**
     * initialize Module.
     */
    public void startModule() {
        if (myModule == null) {
            return;
        }
        try {
            myModule.getModuleClient().initialize();
        } catch (GoobiModuleException e) {
            Helper.setFehlerMeldung("GoobiModuleException: ", e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
            logger.error(e);
        } catch (XmlRpcException e) {
            Helper.setFehlerMeldung("XmlRpcException: ", e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
            logger.error(e);
        }
    }

    /**
     * shut Module down.
     */
    public void stopModule() {
        if (myModule == null) {
            return;
        }
        try {
            myModule.getModuleClient().shutdown();
        } catch (GoobiModuleException e) {
            Helper.setFehlerMeldung("GoobiModuleException: ", e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
            logger.error(e);
        } catch (XmlRpcException e) {
            logger.error(e);
        }
    }

    /**
     * Module-Konfigurationen aus externer xml-Datei modules.xml einlesen.
     */
    @SuppressWarnings("unchecked")
    private List<ModuleDesc> getModulesFromConfigurationFile() {
        List<ModuleDesc> rueckgabe = new ArrayList<>();
        String fileName = ConfigCore.getKitodoConfigDirectory() + "modules.xml";
        if (!(new File(fileName).exists())) {
            Helper.setFehlerMeldung("File not found: ", fileName);
            return rueckgabe;
        }
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(fileName));
            Element root = doc.getRootElement();
            List<Element> children = root.getChildren();
            /* alle Module durchlaufen */
            for (Element child : children) {
                rueckgabe.add(new ModuleDesc(child.getAttributeValue("name"), child.getAttributeValue("url"),
                        null, child.getAttributeValue("description")));
            }
        } catch (JDOMException e1) {
            Helper.setFehlerMeldung("Error on reading, JDOMException: ",
                    e1.getMessage() + "\n" + Helper.getStacktraceAsString(e1));
            logger.error(e1);
        } catch (IOException e1) {
            Helper.setFehlerMeldung("Error on reading, IOException: ",
                    e1.getMessage() + "\n" + Helper.getStacktraceAsString(e1));
            logger.error(e1);
        }
        return rueckgabe;
    }

    /**
     * Eine Shortsession für einen Schritt starten.
     */
    public String startShortSession(Task inSchritt) throws GoobiException, XmlRpcException {
        myModule = null;

        /*
         * Modulserver läuft noch nicht
         */
        if (modulmanager == null) {
            throw new GoobiException(0, "Der Modulserver läuft nicht");
        }

        if (myModule == null) {
            Helper.setFehlerMeldung("Module not found");
            return null;
        }

        /*
         * Verbindung zum Modul aufbauen und url zurückgeben
         */
        String processId = String.valueOf(inSchritt.getProcess().getId().intValue());
        String tempID = UniqueID.generate_session();
        myRunningShortSessions.put(tempID, processId);

        GoobiModuleParameter gmp1 = new GoobiModuleParameter(processId, tempID,
                myModule.getModuleClient().longsessionID, null);
        HttpSession insession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

        String applicationUrl = new HelperForm().getServletPathWithHostAsUrl();
        gmp1.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/AktuelleSchritteBearbeiten.jsf?jsessionId="
                + insession.getId());
        myModule.getGmps().add(gmp1); // add session in den Manager
        return myModule.getModuleClient().start(gmp1);
    }

    /**
     * Test for shortsession of selected module.
     */
    public void startShortSessionTest() throws GoobiException, XmlRpcException {
        /*
         * ohne gewähltes Modul gleich wieder raus
         */
        if (myModule == null) {
            return;
        }

        String processId = "3346";
        String tempID = UniqueID.generate_session();
        myRunningShortSessions.put(tempID, processId);
        GoobiModuleParameter gmp = new GoobiModuleParameter(processId, tempID, myModule.getModuleClient().longsessionID,
                null);

        String applicationUrl = ConfigCore.getParameter("ApplicationWebsiteUrl");

        gmp.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/aktiveModule.jsf?sessionId=" + tempID);
        gmp.put("type", "PRODUCE");

        myModule.getGmps().add(gmp); // add shortsession in den Manager
        Helper.setMeldung(myModule.getModuleClient().start(gmp));
        Helper.setMeldung(gmp.toString());

    }

    /*
     * Messaging system
     */

    /**
     * Diese Methode überprüft, ob wir was neues in unserem Container haben.
     */
    private static void check_new_messages(GoobiModuleManager modules) {
        Message message = null;
        while (MessageContainer.size() > 0) {
            message = MessageContainer.pop();
            handleMessage(message, modules);
        }
    }

    /**
     * Mit dieser Methode werden die messages abgearbeitet.
     *
     * @param message
     *            messages
     * @param modules
     *            GoobiModuleManager
     */
    private static void handleMessage(Message message, GoobiModuleManager modules) {
        if ((message.body.error.faultCode == 0) && (message.body.error.faultString.equals("END"))) {
            String in_session = message.from;
            /*
             * erstmal wird geschaut welcher module und welche session ist der
             * Absender.
             */
            int i = 0;
            if (modules.size() == i) {
                //TODO: fix it
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
                            // Behandlung aller trigger messages
                            /*
                             * Behandlung von "trigger - END"
                             */
                            try {
                                modules.get(i - 1).getModuleClient().stop(gmp);
                            } catch (XmlRpcException e) {
                                logger.error(e);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * Getter und Setter
     */

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
     * get Prozess von shortSessionID.
     */
    public static Process getProcessFromShortSession(String sessionId) throws GoobiException {
        String prozessidStr = getProcessIDFromShortSession(sessionId);
        try {
            Process tempProz = serviceManager.getProcessService().getById(Integer.parseInt(prozessidStr));
            Helper.getHibernateSession().flush();
            Helper.getHibernateSession().clear();
            if (tempProz != null && tempProz.getId() != null) {
                Helper.getHibernateSession().refresh(tempProz);
            }

            return tempProz;
        } catch (NumberFormatException e) {
            throw new GoobiException(5, "******** wrapped NumberFormatException ********: " + e.getMessage() + "\n"
                    + Helper.getStacktraceAsString(e));
        } catch (DAOException e) {
            throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n"
                    + Helper.getStacktraceAsString(e));
        }
    }

}
