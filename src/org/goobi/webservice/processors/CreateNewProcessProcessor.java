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

package org.goobi.webservice.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import de.sub.goobi.config.ConfigOpacDoctype;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;

/**
 * CreateNewProcessProcessor is an Apache Active MQ consumer which registers to
 * a queue configured by "activeMQ.createNewProcess.queue" on application
 * startup. It was designed to create new processes from outside Goobi. There
 * are two ways providing to create new processes. If the MapMessage on that
 * queue contains of all the fields listed, the bibliographic data is retrieved
 * using a catalogue configured within Goobi. If “opac” is missing, it will try
 * to create a process just upon the data passed in the “userFields” − “field”
 * and “value” will be ignored in that case, and the “docType” can be set
 * manually.
 * 
 * Field summary:
 * 
 * <dl>
 * 		<dt>String template</dt>
 * 			<dd>name of the process template to use. A list of all available
 *              templates can be obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listTemplates.jsp</kbd>.</dd>
 * 		<dt>String opac</dt>
 * 			<dd>Cataloge to use for lookup. A list of all available catalogues
 *              can be obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listCatalogues.jsp</kbd>.</dd>
 * 		<dt>String field</dt>
 * 			<dd>Field to look into, usually 12 (PPN). A list of all available
 *              search fields can be obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listSearchFields.jsp</kbd>.</dd>
 * 		<dt>String value</dt>
 * 			<dd>Value to look for, id of physical medium</dd>
 * 		<dt>String docType</dt>
 * 			<dd>DocType value to use if no catalogue request is performed. A
 *              list of possible values can be obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listAllDoctypes.jsp</kbd></dd>
 * 		<dt>Set&lt;String&gt; collections</dt>
 * 			<dd>Collections to be selected. A list of all available collections
 *              can be obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listCollections.jsp</kbd>.</dd>
 * 		<dt>Map&lt;String, String&gt; userFields collections</dt>
 * 			<dd>Fields to be populated manually. A list of fields can be
 *              obtained in JSON format calling
 *              <kbd><em>{ContextRoot}</em>/ws/listFieldConfig.jsp</kbd>
 *          </dd>
 * </dl>
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class CreateNewProcessProcessor extends ActiveMQProcessor {
	private static final Logger logger = Logger.getLogger(CreateNewProcessProcessor.class);

	public CreateNewProcessProcessor() {
		super(ConfigMain.getParameter("activeMQ.createNewProcess.queue", null));
	}

	@Override
	protected void process(MapMessageObjectReader args) throws Exception {

		Set<String> collections = args.getMandatorySetOfString("collections");
		String id = args.getMandatoryString("id");
		String template = args.getMandatoryString("template");
		Map<String, String> userFields = args.getMapOfStringToString("userFields");
		if (args.hasField("opac")) {
			String opac = args.getMandatoryString("opac");
			String field = args.getMandatoryString("field");
			String value = args.getMandatoryString("value");
			createNewProcessMain(template, opac, field, value, id, null, collections, userFields);
		} else {
			String docType = args.getString("docType");
			createNewProcessMain(template, null, null, null, id, docType, collections, userFields);
		}

	}

	/**
	 * This is the main routine used to create new processes.
	 * 
	 * @param template
	 *            titel of the process template the new process shall be derived
	 *            from
	 * @param opac
	 *            name of the connection to a library catalogue to load the
	 *            bibliographic data from (may be null)
	 * @param field
	 *            number of the catalogue search field (ignored if “opac” is
	 *            null)
	 * @param value
	 *            search string (ignored if “opac” is null)
	 * @param id
	 *            identifier to be used for the digitisation
	 * @param docType
	 *            docType to set (may be null)
	 * @param collections
	 *            collections to add the digitisation to
	 * @param userFields
	 *            Values for additional fields can be set here (may be null)
	 * @throws Exception
	 *             in various cases, such as bad parameters or errors in the
	 *             underlying layers
	 */
	protected void createNewProcessMain(String template, String opac, String field, String value, String id, String docType,
			Set<String> collections, Map<String, String> userFields) throws Exception {

		try {
			ProzesskopieForm newProcess = newProcessFromTemplate(template);
			newProcess.setDigitalCollections(validCollectionsForProcess(collections, newProcess));
			if (opac != null)
				getBibliorgaphicData(newProcess, opac, field, value);
			if (docType != null && docTypeIsPossible(newProcess, docType))
				newProcess.setDocType(docType);
			if (userFields != null)
				setUserFields(newProcess, userFields);
			newProcess.CalcProzesstitel();
			String state = newProcess.NeuenProzessAnlegen();
			if (!state.equals("ProzessverwaltungKopie3"))
				throw new RuntimeException();
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
	protected ProzesskopieForm newProcessFromTemplate(String templateTitle) throws IllegalArgumentException {
		ProzesskopieForm result = new ProzesskopieForm();

		List<Prozess> allTemplates = allTemplatesFromDatabase();
		Prozess selectedTemplate = selectTemplateByTitle(allTemplates, templateTitle);
		result.setProzessVorlage(selectedTemplate);
		result.Prepare();
		return result;
	}

	/**
	 * This method reads all Prozess objects from the hibernate.
	 * 
	 * @return a List<Prozess> holding all templates
	 */
	protected List<Prozess> allTemplatesFromDatabase() {
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
	protected Prozess selectTemplateByTitle(List<Prozess> allTemplates, String templateTitle) throws IllegalArgumentException {

		Prozess result = null;
		for (Prozess aTemplate : allTemplates) {
			if (aTemplate.getTitel().equals(templateTitle)) {
				result = aTemplate;
				break;
			}
		}
		if (result == null)
			throw new IllegalArgumentException("Bad argument: No template \"" + templateTitle + "\" available.");
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
	protected List<String> validCollectionsForProcess(Set<String> collections, ProzesskopieForm process) throws IllegalArgumentException {

		HashSet<String> possibleCollections = new HashSet<String>(process.getPossibleDigitalCollections());
		if (!possibleCollections.containsAll(collections))
			throw new IllegalArgumentException("Bad argument: One or more elements of \"collections\" is not available for template \""
					+ process.getProzessVorlage().getTitel() + "\".");
		return new ArrayList<String>(collections);
	}

	/**
	 * The function docTypeIsPossible() tests whether a given docType String can
	 * be applied to a given process template. If so, it will return “true”,
	 * otherwise, it will throw an informative IllegalArgumentException.
	 * 
	 * @param dialog
	 *            the ProzesskopieForm object to test against
	 * @param docType
	 *            the desired docType ID string
	 * @return true on success
	 * @throws IllegalArgumentException
	 *             if a docType is not applicable to the template or the docType
	 *             isn’t valid
	 */
	protected boolean docTypeIsPossible(ProzesskopieForm dialog, String docType) throws IllegalArgumentException {
		Boolean fieldIsUsed = dialog.getStandardFields().get("doctype");
		if (fieldIsUsed == null || fieldIsUsed.equals(Boolean.FALSE))
			throw new IllegalArgumentException("Bad argument “docType”: Selected template doesn’t provide the standard field “doctype”.");

		boolean valueIsValid = false;
		ConfigOpacDoctype option = null;
		for (Iterator<ConfigOpacDoctype> configOpacDoctypeIterator = dialog.getAllDoctypes().iterator(); !valueIsValid
				&& configOpacDoctypeIterator.hasNext(); option = configOpacDoctypeIterator.next())
			valueIsValid = docType.equals(option.getTitle());
		if (!valueIsValid)
			throw new IllegalArgumentException("Bad argument “docType”: Selected template doesn’t provide a docType “{0}”.".replace("{0}",
					docType));

		return true;
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
	protected void setUserFields(ProzesskopieForm form, Map<String, String> userFields) throws RuntimeException {

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
	protected void getBibliorgaphicData(ProzesskopieForm inputForm, String opac, String field, String value) throws RuntimeException {

		inputForm.setOpacKatalog(opac);
		inputForm.setOpacSuchfeld(field);
		inputForm.setOpacSuchbegriff(value);

		int before = countPopulatedAdditionalFields(inputForm);
		inputForm.OpacAuswerten();
		int afterwards = countPopulatedAdditionalFields(inputForm);

		if (!(afterwards > before))
			throw new RuntimeException("Searching the OPAC didn’t yield any results.");
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
	protected int countPopulatedAdditionalFields(ProzesskopieForm form) {
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
	protected void setAdditionalField(ProzesskopieForm inputForm, String key, String value) throws RuntimeException {

		for (AdditionalField field : inputForm.getAdditionalFields()) {
			if (key.equals(field.getTitel())) {
				field.setWert(value);
				return;
			}
		}

		throw new RuntimeException("Couldn’t set “" + key + "” to “" + value + "”: No such field in record.");
	}

}
