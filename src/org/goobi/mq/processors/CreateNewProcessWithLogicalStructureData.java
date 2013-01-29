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

package org.goobi.mq.processors;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.config.ConfigOpacDoctype;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;
import org.apache.log4j.Logger;
import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import ugh.dl.*;

import java.util.*;

/**
 *
 */
public class CreateNewProcessWithLogicalStructureData extends ActiveMQProcessor {
	private static final Logger logger = Logger.getLogger(CreateNewProcessWithLogicalStructureData.class);

	/**
	 *
	 */
	public CreateNewProcessWithLogicalStructureData() {
		super(ConfigMain.getParameter("activeMQ.createNewProcessWithLogicalStructureData.queue", null));
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	@Override
	protected void process(MapMessageObjectReader args) throws Exception {
		logger.info("CreateNewProcessWithLogicalStructureData got new ticket.");

		String processIdentifier = args.getMandatoryString("id");
		String templateName = args.getMandatoryString("template");
		String docType = args.getMandatoryString("docType");
		Set<String> collections = args.getMandatorySetOfString("collections");
		Map<String, String> userFields = args.getMapOfStringToString("userFields");

		createNewProcess(processIdentifier, templateName, docType, collections, userFields);
	}

	/**
	 *
	 * @param processIdentifier
	 * @param templateName
	 * @param docType
	 * @param collections
	 * @param userFields
	 * @throws Exception
	 */
	protected void createNewProcess(String processIdentifier, String templateName, String docType, Set<String> collections, Map<String, String> userFields) throws Exception {
		try {
			ProzesskopieForm newPFK = createNewProcessFromTemplate(templateName);

			if (validateCollectionsForProcess(newPFK, collections)) {
				ArrayList<String> transformedCollections = new ArrayList<String>(collections);
				newPFK.setDigitalCollections(transformedCollections);
			} else {
				throw new IllegalArgumentException("Given collections for process are not valid!");
			}

			if (docType != null && validDocTypeForProcess(newPFK, docType)) {
				newPFK.setDocType(docType);
			} else {
				throw new IllegalArgumentException("Needed document type missed or invalid!");
			}

			if (userFields != null) {
				setUserFields(newPFK, userFields);
			} else {
				throw new IllegalArgumentException("Userfields should not be null!");
			}

			newPFK.CalcProzesstitel(); // use given meta data from userFields

			addAdditionalLogicalStructureData(newPFK); // TODO: structure data

			String state = newPFK.NeuenProzessAnlegen(); // create new process and store process metadata to file space storage
			if (!state.equals("ProzessverwaltungKopie3")) {
				throw new Exception("NeuenProzessAnlegen() returned an unexpected return value: \"" + state + "\"!");
			}

			logger.info("Succesfull created new process with identifier " + processIdentifier + " and Goobi ID " + newPFK.getProzessKopie().getId());
		} catch (Exception e) {
			logger.error("Failed to create new process with identifier " + processIdentifier, e);
			throw e;
		}
	}

	/**
	 *
	 * @param templateName
	 * @return
	 */
	protected ProzesskopieForm createNewProcessFromTemplate(String templateName) {
		ProzesskopieForm result = new ProzesskopieForm();

		Prozess selectedTemplate = getProcessTemplateByName(templateName);

		result.setProzessVorlage(selectedTemplate);
		result.Prepare();

		return result;
	}

	/**
	 *
	 * @param templateName
	 * @return
	 */
	protected Prozess getProcessTemplateByName(String templateName) {
		Criteria criteria;
		Session hibernateSession;
		List<Prozess> interimResult;
		Integer resultSize;
		Prozess result;

		// get global hibernate session
		hibernateSession = Helper.getHibernateSession();

		criteria = hibernateSession.createCriteria(Prozess.class)
				.add(Restrictions.eq("istTemplate", true))
				.add(Restrictions.eq("titel", templateName));

		interimResult = criteria.list();
		resultSize = interimResult.size();

		if (resultSize == 0) {
			throw new IllegalArgumentException("Bad argument: No template \"" + templateName + "\" available.");
		} else if (resultSize > 1) {
			logger.warn("Got more than one result for template \"" + templateName + "\", taken first result!");
		}
		result = interimResult.get(0);

		return result;
	}

	/**
	 *
	 * @param pKF
	 * @param collections
	 * @return
	 */
	protected boolean validateCollectionsForProcess(ProzesskopieForm pKF, Set<String> collections) {
		HashSet<String> possibleCollections = new HashSet<String>(pKF.getPossibleDigitalCollections());
		return possibleCollections.containsAll(collections);
	}

	/**
	 *
	 * @param pKF
	 * @param docType
	 * @return
	 */
	protected boolean validDocTypeForProcess(ProzesskopieForm pKF, String docType) {
		Boolean fieldIsUsed = pKF.getStandardFields().get("doctype");

		if (fieldIsUsed == null || fieldIsUsed.equals(Boolean.FALSE))
			throw new IllegalArgumentException("Bad argument \"docType\": Selected template doesn’t provide the standard field \"doctype\".");

		boolean valueIsValid = false;
		Iterator<ConfigOpacDoctype> configOpacDoctypeIterator = pKF.getAllDoctypes().iterator();
		do {
			ConfigOpacDoctype option = configOpacDoctypeIterator.next();
			valueIsValid = docType.equals(option.getTitle());
		} while (!valueIsValid && configOpacDoctypeIterator.hasNext());

		if (valueIsValid)
			return true;

		throw new IllegalArgumentException("Bad argument for docType: Selected template doesn’t provide a docType \"" + docType + "\".");
	}

	/**
	 * The method setUserFields() allows to set any AdditionalField to a user
	 * specific value.
	 *
	 * @param pKF       a ProzesskopieForm object whose AdditionalField objects are
	 *                   subject to the change
	 * @param userFields the data to pass to the pKF
	 * @throws RuntimeException in case that no field with a matching title was found in the
	 *                          ProzesskopieForm object
	 */
	protected void setUserFields(ProzesskopieForm pKF, Map<String, String> userFields) throws RuntimeException {

		for (String key : userFields.keySet()) {
			setAdditionalField(pKF, key, userFields.get(key));
		}

	}

	/**
	 * The method setAdditionalField() sets the value of an AdditionalField held
	 * by a ProzesskopieForm object.
	 *
	 * @param pKF a ProzesskopieForm object
	 * @param key       the title of the AdditionalField whose value shall be modified
	 * @param value     the new value for the AdditionalField
	 * @throws RuntimeException in case that no field with a matching title was found in the
	 *                          ProzesskopieForm object
	 */
	protected void setAdditionalField(ProzesskopieForm pKF, String key, String value) throws RuntimeException {

		boolean unknownField = true;
		for (AdditionalField field : pKF.getAdditionalFields()) {
			if (key.equals(field.getTitel())) {
				field.setWert(value);
				unknownField = false;
			}
		}

		if (unknownField)
			throw new RuntimeException("Couldn’t set \"" + key + "\" to \"" + value + "\": No such field in record.");
	}

	/**
	 *
	 * @param pKF
	 * @throws Exception
	 */
	protected void addAdditionalLogicalStructureData(ProzesskopieForm pKF) throws Exception {

		DigitalDocument digitalDocument;
		DocStruct insertPoint;
		DocStruct docStructElement;
		Metadata metadataElement;
		Prefs prefs;

		digitalDocument = getDigitalDocument(pKF);

		insertPoint = getInsertPointOfLogicalDocument(digitalDocument, true);

		prefs = getProjectPreferences(pKF);

		docStructElement = createDocStructElement(digitalDocument, prefs, "Letter");

		metadataElement = createMetadataElement(prefs, "TitleDocMain", "Hello World of TitleDocMain");
		metadataElement.setDocStruct(docStructElement); // adding reference to document structure

		// putting data togehter
		docStructElement.addMetadata(metadataElement); // adding metadata object to document structure
		insertPoint.addChild(docStructElement); // adding document structure to logical document structure
	}

	/**
	 *
	 * @param pKF
	 * @return
	 * @throws Exception
	 */
	private DigitalDocument getDigitalDocument(ProzesskopieForm pKF) throws Exception {

		Fileformat ff;
		DigitalDocument digitalDocument;

		ff = pKF.createAndReturnNewFileformatObject();
		if (ff == null) {
			throw new Exception("Got empty fileformat object.");
		}

		digitalDocument = ff.getDigitalDocument();
		if (digitalDocument == null) {
			throw new Exception("Could not retrieve digital document.");
		}

		return digitalDocument;
	}

	/**
	 *
	 * @param digitalDocument
	 * @param multiVolume
	 * @return
	 * @throws Exception
	 */
	private DocStruct getInsertPointOfLogicalDocument(DigitalDocument digitalDocument, boolean multiVolume) throws Exception {

		DocStruct logicalDocStruct;
		DocStruct insertPoint;

		logicalDocStruct = digitalDocument.getLogicalDocStruct();
		if (logicalDocStruct == null) {
			throw new Exception("Could not retrieve logical document structure.");
		}

		if (multiVolume) {

			List<DocStruct> children;
			DocStruct firstChild;
			children = logicalDocStruct.getAllChildren();
			if (children == null || children.isEmpty()) {
				throw new Exception("Expecting children, but none found.");
			}
			firstChild = children.get(0);
			if (firstChild == null) {
				throw new Exception("Expecting at leat one children of logical document structure.");
			}

			insertPoint = firstChild; // used for multi volume
		} else {
			insertPoint = logicalDocStruct; // used for monograph
		}

		return insertPoint;
	}

	/**
	 *
	 * @param pKF
	 * @return
	 * @throws Exception
	 */
	private Prefs getProjectPreferences(ProzesskopieForm pKF) throws Exception {
		Regelsatz regelsatz;
		Prefs prefs;

		regelsatz = pKF.getProzessKopie().getRegelsatz();
		if (regelsatz == null) {
			throw new Exception("Could not retrieve Regelsatz for current pKF.");
		}

		prefs = regelsatz.getPreferences();
		if (prefs == null) {
			throw new Exception("Could not retrieve preferences for current pKF.");
		}

		return prefs;
	}

	/**
	 *
	 * @param digitalDocument
	 * @param prefs
	 * @param docStructName
	 * @return
	 * @throws Exception
	 */
	private DocStruct createDocStructElement(DigitalDocument digitalDocument, Prefs prefs, String docStructName) throws Exception {
		DocStructType dst;

		dst = prefs.getDocStrctTypeByName(docStructName);
		if (dst == null) {
			throw new Exception("Could not retrieve document structure for '" + docStructName + "'.");
		}

		return digitalDocument.createDocStruct(dst);
	}

	/**
	 *
	 * @param prefs
	 * @param metadataTypeName
	 * @param metadataValue
	 * @return
	 * @throws Exception
	 */
	private Metadata createMetadataElement(Prefs prefs, String metadataTypeName, String metadataValue) throws Exception {
		MetadataType mdt;
		Metadata md;

		mdt = prefs.getMetadataTypeByName(metadataTypeName);
		if (mdt == null) {
			throw new Exception("Could not retrieve metadata type for '" + metadataTypeName + "'.");
		}

		md = new Metadata(mdt);
		md.setValue(metadataValue);

		return md;
	}

}
