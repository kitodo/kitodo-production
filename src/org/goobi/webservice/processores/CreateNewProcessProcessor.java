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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;
import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;

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
	protected void process(MapMessageObjectReader args) throws Exception {

		Set<String> collections = args.getMandatorySetOfString("collections");
		String field = args.getMandatoryString("field");
		String id = args.getMandatoryString("id");
		String opac = args.getMandatoryString("opac");
		String template = args.getMandatoryString("template");
		Map<String, String> userFields = args
				.getMapOfStringToString("userFields");
		String value = args.getMandatoryString("value");

		createNewProcessMain(template, opac, field, value, id, collections,
				userFields);
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
	 * @param userFields
	 *            Values for additional fields can be set here (may be null)
	 * @throws Exception
	 *             in various cases, such as bad parameters or errors in the
	 *             underlying layers
	 */
	private void createNewProcessMain(String template, String opac,
			String field, String value, String id, Set<String> collections,
			Map<String, String> userFields) throws Exception {

		try {
			ProzesskopieForm newProcess = newProcessFromTemplate(template);
			newProcess.setDigitalCollections(validCollectionsForProcess(
					collections, newProcess));
			getBibliorgaphicData(newProcess, opac, field, value);
			setAdditionalField(newProcess, DIGITAL_ID_FIELD_NAME, id);
			if (userFields != null)
				setUserFields(newProcess, userFields);
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
			if (aTemplate.getTitel().equals(templateTitle)) {
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
	 * The method setUserFields() allows to set any AdditionalField to a user
	 * specific value.
	 * 
	 * @param form
	 *            a ProzesskopieForm object whose AdditionalField objects are
	 *            subject to the change
	 * @param userFields
	 *            the data to pass to the form
	 * @throws RuntimeException
	 *             in case that no field with a matching title was found in the
	 *             ProzesskopieForm object
	 */
	private void setUserFields(ProzesskopieForm form,
			Map<String, String> userFields) throws RuntimeException {

		for (String key : userFields.keySet()) {
			setAdditionalField(form, key, userFields.get(key));
		}

	}

	/**
	 * Sets the bibliographic data for a new process from a library catalogue.
	 * This is equal to manually choosing a catalogue and a search field,
	 * entering the search string and clicking “Apply”.
	 * 
	 * Since the underlying OpacAuswerten() method doesn’t raise exceptions, we
	 * count the populated “additional details” fields before and after running
	 * the request and assume the method to have failed if not even one more
	 * field was populated by the method call.
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
	 *             is thrown if the search didn’t bring any results
	 */
	private void getBibliorgaphicData(ProzesskopieForm inputForm, String opac,
			String field, String value) throws RuntimeException {

		inputForm.setOpacKatalog(opac);
		inputForm.setOpacSuchfeld(field);
		inputForm.setOpacSuchbegriff(value);

		int before = countPopulatedAdditionalFields(inputForm);
		inputForm.OpacAuswerten();
		int afterwards = countPopulatedAdditionalFields(inputForm);

		if (!(afterwards > before))
			throw new RuntimeException(
					"Searching the OPAC didn’t yield any results.");
	}

	/**
	 * The function countPopulatedAdditionalFields() returns the number of
	 * AdditionalFields in the given ProzesskopieForm that have meaningful
	 * content.
	 * 
	 * @param form
	 *            a ProzesskopieForm object to examine
	 * @return the number of AdditionalFields populated
	 */
	private int countPopulatedAdditionalFields(ProzesskopieForm form) {
		int result = 0;

		for (AdditionalField field : form.getAdditionalFields()) {
			String value = field.getWert();
			if (value != null && value.length() > 0)
				result++;
		}

		return result;
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

		for(AdditionalField field : inputForm.getAdditionalFields()) {
			if (key.equals(field.getTitel())){
				field.setWert(value);
				return;
			}
		}
		
		throw new RuntimeException("Couldn’t set “" + key + "” to “" + value
				+ "”: No such field in record.");
	}

}
