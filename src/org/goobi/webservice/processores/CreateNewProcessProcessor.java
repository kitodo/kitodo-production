/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.webservice.processores;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jms.MapMessage;

import org.apache.log4j.Logger;
import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;
import org.goobi.webservice.WebServiceResult;
import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.ReportLevel;

/**
 * CreateNewProcessProcessor is an Apache Active MQ consumer which registers to
 * a queue configured by "activeMQ.createNewProcess.queue" on application
 * startup.
 * 
 * It expects MapMessages on that queue containing:
 * 
 * <dl>
 * 		<dt>String id</dt>
 * 			<dd>ID to be used as digital PPN</dd>
 * 		<dt>String template</dt>
 * 			<dd>name of the process template to use</dd>
 * 		<dt>String opac</dt>
 * 			<dd>Cataloge to use for lookup</dd>
 * 		<dt>String field</dt>
 * 			<dd>Field to look into, usually 12 (PPN)</dd>
 * 		<dt>String value</dt>
 * 			<dd>Value to look for, id of physical medium</dd>
 * 		<dt>Set&lt;String&gt; collections</dt>
 * 			<dd>Collections to be selected</dd>
 * </dl>
 * 
 * It will then create a new process based on the given template.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class CreateNewProcessProcessor extends ActiveMQProcessor {
	private static final Logger logger = Logger
			.getLogger(CreateNewProcessProcessor.class);

	/**
	 * This is the “magic numbers” section − the values can be overridden in
	 * GoobiConfig.properties
	 */
	final String DIGITAL_ID_FIELD_NAME = ConfigMain.getParameter(
			"activeMQ.createNewProcess.digitalIdFieldName",
			"PPN digital a-Satz");
	final long WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR = ConfigMain
			.getLongParameter(
					"activeMQ.createNewProcess.waitBetweenOpacRequestsOnError",
					131072);
	final long WAIT_AT_MOST_ON_OPAC_ERROR = ConfigMain.getLongParameter(
			"activeMQ.createNewProcess.waitAtMostOnOpacError", 2097152);

	public CreateNewProcessProcessor() {
		super(ConfigMain.getParameter("activeMQ.createNewProcess.queue", null));
	}

	@Override
	protected void process(MapMessage ticket) throws Exception {
		MapMessageObjectReader ticketReader = new MapMessageObjectReader(ticket);

		Set<String> collections = ticketReader
				.getMandatorySetOfString("collections");
		String field = ticketReader.getMandatoryString("field");
		String id = ticketReader.getMandatoryString("id");
		String opac = ticketReader.getMandatoryString("opac");
		String template = ticketReader.getMandatoryString("template");
		String value = ticketReader.getMandatoryString("value");

		createNewProcessMain(template, opac, field, value, id, collections);
	}

	/**
	 * This is the main routine used to create new processes.
	 * 
	 * @param template
	 *            titel of the process template the new process shall be derived
	 *            from
	 * @param opac
	 *            name of the connection to a library catalogue to load the
	 *            bibliographic data from
	 * @param field
	 *            number of the catalogue search field
	 * @param value
	 *            search string
	 * @param id
	 *            identifier to be used for the digitisation
	 * @param collections
	 *            collections to add the digitisation to
	 * @throws Exception
	 *             in various cases, such as bad parameters or errors in the
	 *             underlying layers
	 */
	private void createNewProcessMain(String template, String opac,
			String field, String value, String id, Set<String> collections)
			throws Exception {

		try {
			ProzesskopieForm newProcess = newProcessFromTemplate(template);
			newProcess.setDigitalCollections(validCollectionsForProcess(
					collections, newProcess));
			getBibliorgaphicData(newProcess, id, opac, field, value);
			setAdditionalField(newProcess, DIGITAL_ID_FIELD_NAME, id);
			newProcess.CalcProzesstitel();
			newProcess.NeuenProzessAnlegen();
			logger.info("Created new process: " + id);
		} catch (Exception exited) {
			logger.error("Failed to create new process: " + id, exited);
			throw exited;
		}
	}

	/**
	 * The function newProcessFromTemplate() derives a ProzesskopieForm object
	 * from a given template.
	 * 
	 * @param templateTitle
	 *            titel value of the template to look for
	 * @return a ProzesskopieForm object, prepared from a given template
	 * @throws IllegalArgumentException
	 *             if no suitable template is found
	 */
	private ProzesskopieForm newProcessFromTemplate(String templateTitle)
			throws IllegalArgumentException {
		ProzesskopieForm result = new ProzesskopieForm();

		List<Prozess> allTemplates = allTemplatesFromDatabase();
		Prozess selectedTemplate = selectTemplateByTitle(allTemplates,
				templateTitle);
		result.setProzessVorlage(selectedTemplate);
		result.Prepare();
		return result;
	}

	/**
	 * This method reads all Prozess objects from the hibernate.
	 * 
	 * @return a List<Prozess> holding all templates
	 */
	private List<Prozess> allTemplatesFromDatabase() {
		Session hibernateSession = Helper.getHibernateSession();
		Criteria request = hibernateSession.createCriteria(Prozess.class);

		@SuppressWarnings("unchecked")
		List<Prozess> result = (List<Prozess>) request.list();

		return result;
	}

	/**
	 * The function selectTemplateByTitle() iterates over a List of Prozess and
	 * returns the first element whose titel equals the given templateTitle.
	 * 
	 * @param allTemplates
	 *            a List<Prozess> which shall be examined
	 * @param templateTitle
	 *            the title of the template to be picked up
	 * @return the template, if found
	 * @throws IllegalArgumentException
	 *             is thrown, if there is no template matching the given
	 *             templateTitle
	 */
	private Prozess selectTemplateByTitle(List<Prozess> allTemplates,
			String templateTitle) throws IllegalArgumentException {

		Prozess result = null;
		for (Prozess aTemplate : allTemplates) {
			if (aTemplate.getTitel() == templateTitle) {
				result = aTemplate;
				break;
			}
		}
		if (result == null)
			throw new IllegalArgumentException("Bad argument: No template \""
					+ templateTitle + "\" available.");
		return result;
	}

	/**
	 * The function validCollectionsForProcess() tests whether a given set of
	 * collections can be assigned to new process. If so, the set of collections
	 * is returned as a list ready for assignment.
	 * 
	 * @param collections
	 *            a set of collection names to be tested
	 * @param process
	 *            a ProzesskopieForm object whose prozessVorlage has been set
	 * @return an ArrayList which can be used to set the digitalCollections of a
	 *         ProzesskopieForm
	 * @throws IllegalArgumentException
	 *             in case that the given collection isn’t a valid subset of the
	 *             digitalCollections possible here
	 */
	private List<String> validCollectionsForProcess(Set<String> collections,
			ProzesskopieForm process) throws IllegalArgumentException {

		HashSet<String> possibleCollections = new HashSet<String>(
				process.getPossibleDigitalCollections());
		if (!possibleCollections.containsAll(collections))
			throw new IllegalArgumentException(
					"Bad argument: One or more elements of \"collections\" is not available for template \""
							+ process.getProzessVorlage().getTitel() + "\".");
		return new ArrayList<String>(collections);
	}

	/**
	 * Sets the bibliographic data for a new process from a library catalogue.
	 * This is equal to manually choosing a catalogue and a search field,
	 * entering the search string and clicking “Apply”.
	 * 
	 * Some efforts had to be made to capture errors potentially occurring here,
	 * as the application logic is implemented straight ahead in the form class
	 * and errors can be figured out only by examining the FacesContext for
	 * FacesMessages that have SEVERITY_ERROR.
	 * 
	 * There is a loop in case that there is an “Error on reading opac”, since
	 * the OPAC may be temporarily unavailable. In this case, the method will
	 * submit a WebServiceResult of ReportLevel.WARN prior to falling asleep for
	 * WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR milliseconds It will then retry the
	 * request. After WAIT_AT_MOST_ON_OPAC_ERROR milliseconds, a
	 * RuntimeException will be thrown.
	 * 
	 * @param inputForm
	 *            the ProzesskopieForm to be set
	 * @param id
	 *            the ticket’s id
	 * @param opac
	 *            the value for “Search in Opac”
	 * @param field
	 *            the number of the search field, e.g. “12” for PPN.
	 * @param value
	 *            the search string
	 * @throws RuntimeException
	 *             is thrown on unmanageable errors
	 */
	private void getBibliorgaphicData(ProzesskopieForm inputForm, String id,
			String opac, String field, String value) throws RuntimeException {
		boolean success = true;
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = null;
		long millisPassed = 0;

		inputForm.setOpacKatalog(opac);
		inputForm.setOpacSuchfeld(field);
		inputForm.setOpacSuchbegriff(value);

		do {
			inputForm.OpacAuswerten();

			@SuppressWarnings("unchecked")
			Iterator<FacesMessage> messages = context.getMessages(null);
			while (messages.hasNext()) {
				message = messages.next();
				if (FacesMessage.SEVERITY_ERROR == message.getSeverity()) {
					if (!message.getSummary().startsWith(
							"Error on reading opac"))
						throw new RuntimeException(message.getSummary());
					else {
						new WebServiceResult(queueName, id, ReportLevel.WARN,
								message.getSummary()).send();
						messages.remove();
						try {
							wait(WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR);
						} catch (InterruptedException irrelevant) {
						}
						millisPassed += WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR;
						success = false;
			}	}	}

		} while (!success && millisPassed < WAIT_AT_MOST_ON_OPAC_ERROR);

		if (!success)
			throw new RuntimeException(message.getSummary());
	}

	/**
	 * The method setAdditionalField() sets the value of an AdditionalField held
	 * by a ProzesskopieForm object.
	 * 
	 * @param inputForm
	 *            a ProzesskopieForm object
	 * @param key
	 *            the title of the AdditionalField whose value shall be modified
	 * @param value
	 *            the new value for the AdditionalField
	 * @throws RuntimeException
	 *             in case that no field with a matching title was found in the
	 *             ProzesskopieForm object
	 */
	private void setAdditionalField(ProzesskopieForm inputForm, String key,
			String value) throws RuntimeException {

		List<AdditionalField> addFieldsList = inputForm.getAdditionalFields();
		AdditionalField myField = null;
		for (Iterator<AdditionalField> i = addFieldsList.iterator(); i
				.hasNext(); myField = i.next()) {
			if (myField.getTitel().equals(key)) {
				myField.setWert(value);
				return;
			}
		}
		throw new RuntimeException("Couldn’t set “" + key + "” to “" + value
				+ "”: No such field in record.");
	}

}
