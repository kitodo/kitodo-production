/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.forms;

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
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.modul.ExtendedDataImpl;
import de.sub.goobi.modul.ExtendedProzessImpl;
import de.sub.goobi.persistence.ProzessDAO;
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
	private static final Logger logger = Logger.getLogger(ModuleServerForm.class);

	/**
	 * initialize all modules ================================================================
	 */
	public void startAllModules() {
		if (modulmanager == null)
			readAllModulesFromConfiguraion();

		/*
		 * -------------------------------- alle Module initialisieren --------------------------------
		 */
		for (ModuleDesc md : modulmanager) {
			try {
				md.getModuleClient().initialize();
			} catch (GoobiModuleException e) {
				Helper.setFehlerMeldung("GoobiModuleException im Modul " + md.getName() + " mit der URL " + md.getUrl() + ": ", e.getMessage());
				logger.error(e);
			} catch (XmlRpcException e) {
				Helper.setFehlerMeldung("XmlRpcException im Modul " + md.getName() + " mit der URL " + md.getUrl() + ": ", e.getMessage() + "\n"
						+ Helper.getStacktraceAsString(e));
			}
		}
		running = true;
	}

	/**
	 * Read module configurations von xml-file ================================================================
	 */
	public void readAllModulesFromConfiguraion() {
		if (modulmanager == null) {
			int port = ConfigMain.getIntParameter("goobiModuleServerPort");
			modulmanager = new GoobiModuleManager(port, new ExtendedProzessImpl(), new ExtendedDataImpl());

			/*
			 * -------------------------------- Nachrichtensystem initialisieren --------------------------------
			 */
			int delay = 5000;
			int period = 1000;
			messageTimer = new Timer();
			messageTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					ModuleServerForm.check_new_messages(modulmanager);
				}
			}, delay, period);

			/*
			 * -------------------------------- Alle Modulbeschreibungen aus der Konfigurationsdatei modules.xml einlesen
			 * --------------------------------
			 */
			for (ModuleDesc md : getModulesFromConfigurationFile())
				modulmanager.add(md);
		}
		running = true;
	}

	/**
	 * shutdown Modulmanager ================================================================
	 */
	public void stopAllModules() {
		if (modulmanager != null)
			modulmanager.shutdown();
		modulmanager = null;
		running = false;
	}

	/**
	 * initialize Module ================================================================
	 */
	public void startModule() {
		if (myModule == null)
			return;
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
	 * shut Module down ================================================================
	 */
	public void stopModule() {
		if (myModule == null)
			return;
		try {
			myModule.getModuleClient().shutdown();
		} catch (GoobiModuleException e) {
			Helper.setFehlerMeldung("GoobiModuleException: ", e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
			logger.error(e);
		} catch (XmlRpcException e) {
		}
	}

	/**
	 * Module-Konfigurationen aus externer xml-Datei modules.xml einlesen ================================================================
	 */
	@SuppressWarnings("unchecked")
	private List<ModuleDesc> getModulesFromConfigurationFile() {
		List<ModuleDesc> rueckgabe = new ArrayList<ModuleDesc>();
		String filename = help.getGoobiConfigDirectory() + "modules.xml";
		if (!(new File(filename).exists())) {
			Helper.setFehlerMeldung("File not found: ", filename);
			return rueckgabe;
		}
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(filename));
			Element root = doc.getRootElement();
			/* alle Module durchlaufen */
			for (Iterator<Element> iter = root.getChildren().iterator(); iter.hasNext();) {
				Element myModule = iter.next();
				rueckgabe.add(new ModuleDesc(myModule.getAttributeValue("name"), myModule.getAttributeValue("url"), null, myModule
						.getAttributeValue("description")));
			}
		} catch (JDOMException e1) {
			Helper.setFehlerMeldung("Error on reading, JDOMException: ", e1.getMessage() + "\n" + Helper.getStacktraceAsString(e1));
			logger.error(e1);
		} catch (IOException e1) {
			Helper.setFehlerMeldung("Error on reading, IOException: ", e1.getMessage() + "\n" + Helper.getStacktraceAsString(e1));
			logger.error(e1);
		}
		return rueckgabe;
	}

	/**
	 * Eine Shortsession für einen Schritt starten ================================================================
	 */
	public String startShortSession(Schritt inSchritt) throws GoobiException, XmlRpcException {
		myModule = null;
		if (inSchritt.getTypModulName() == null || inSchritt.getTypModulName().length() == 0) {
			Helper.setFehlerMeldung("this step has no mudule");
			return "";
		}

		/*
		 * --------------------- zusätzliche Parameter neben dem Modulnamen -------------------
		 */
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

		/*
		 * -------------------------------- Modulserver läuft noch nicht --------------------------------
		 */
		if (modulmanager == null)
			throw new GoobiException(0, "Der Modulserver läuft nicht");

		/*
		 * -------------------------------- ohne gewähltes Modul gleich wieder raus --------------------------------
		 */
		for (ModuleDesc md : modulmanager)
			if (md.getName().equals(schrittModuleName))
				myModule = md;
		if (myModule == null) {
			Helper.setFehlerMeldung("Module not found");
			return "";
		}

		/*
		 * -------------------------------- Verbindung zum Modul aufbauen und url zurückgeben --------------------------------
		 */
		String processId = String.valueOf(inSchritt.getProzess().getId().intValue());
		String tempID = UniqueID.generate_session();
		myRunningShortSessions.put(tempID, processId);

		GoobiModuleParameter gmp1 = new GoobiModuleParameter(processId, tempID, myModule.getModuleClient().longsessionID, typeParameters);
		HttpSession insession = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

		String applicationUrl = new HelperForm().getServletPathWithHostAsUrl();
		gmp1.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/AktuelleSchritteBearbeiten.jsf?jsessionId=" + insession.getId());
		myModule.getGmps().add(gmp1); // add session in den Manager
		return myModule.getModuleClient().start(gmp1);
	}

	/**
	 * Test for shortsession of selected module ================================================================
	 */
	public void startShortSessionTest() throws GoobiException, XmlRpcException {
		/*
		 * -------------------------------- ohne gewähltes Modul gleich wieder raus --------------------------------
		 */
		if (myModule == null)
			return;

		String processId = "3346";
		String tempID = UniqueID.generate_session();
		myRunningShortSessions.put(tempID, processId);
		GoobiModuleParameter gmp = new GoobiModuleParameter(processId, tempID, myModule.getModuleClient().longsessionID, null);

		String applicationUrl = ConfigMain.getParameter("ApplicationWebsiteUrl");

		gmp.put("return_url", applicationUrl + HelperForm.MAIN_JSF_PATH + "/aktiveModule.jsf?sessionId=" + tempID);
		gmp.put("type", "PRODUCE");

		myModule.getGmps().add(gmp); // add shortsession in den Manager
		Helper.setMeldung(myModule.getModuleClient().start(gmp));
		Helper.setMeldung(gmp.toString());

	}

	/*
	 * ##################################################### ##################################################### ## ## Messaging system ##
	 * ##################################################### ####################################################
	 */

	/**
	 * Diese Methode überprüft, ob wir was neues in unserem Container haben. ================================================================
	 */
	private static void check_new_messages(GoobiModuleManager modules) {
		Message message = null;
		while (MessageContainer.size() > 0) {
			message = MessageContainer.pop();
			handle_message(message, modules);
		}
	}

	/**
	 * Mit dieser Methode werden die messages abgearbeitet
	 * 
	 * @param message
	 *            messages
	 * @param modules
	 *            GoobiModuleManager ================================================================
	 */
	private static void handle_message(Message message, GoobiModuleManager modules) {
		if ((message.body.error.faultCode == 0) && (message.body.error.faultString.equals("END"))) {
			String in_session = message.from;
			/*
			 * erstmal wird geschaut welcher module und welche session ist der Absender.
			 */
			int i = 0;
			if (modules.size() == i) {
			} else {
				i++;
				ArrayList<GoobiModuleParameter> gmps = modules.get(i - 1).getGmps();
				for (GoobiModuleParameter gmp : gmps) {
					if (gmp.sessionId.equals(in_session)) {
						/*
						 * Jetzt wird herausgefunden um was für eine Message es geht. dabei ist: Module : modules.get(i) Session wird durch gmp
						 * beschrieben
						 */
						if (message.body.type.equals("trigger")) {
							// behandlung aller trigger messages
							/*
							 * Behandlung von "trigger - END"
							 */
							if ((message.body.error.faultCode == 0) && (message.body.error.faultString.equals("END"))) {
								try {
									modules.get(i - 1).getModuleClient().stop(gmp);
								} catch (GoobiModuleException e) {
									logger.error(e);
								} catch (XmlRpcException e) {
									logger.error(e);
								}
							}
						} else if (message.body.type.equals("log")) {
							// behandlung aller log messages
						}
					}
				}
			}
		}
	}

	/*
	 * ##################################################### ##################################################### ## ## Getter und Setter ##
	 * ##################################################### ####################################################
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
	 * get Prozess von shortSessionID ================================================================
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
			new Helper();
			throw new GoobiException(5, "******** wrapped NumberFormatException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
		} catch (DAOException e) {
			new Helper();
			throw new GoobiException(1400, "******** wrapped DAOException ********: " + e.getMessage() + "\n" + Helper.getStacktraceAsString(e));
		}
	}

}
