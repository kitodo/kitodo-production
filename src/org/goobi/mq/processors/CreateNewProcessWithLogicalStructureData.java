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
import de.sub.goobi.importer.ImportOpac;
import org.apache.log4j.Logger;
import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ugh.dl.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class CreateNewProcessWithLogicalStructureData extends ActiveMQProcessor {
	private static final Logger logger = Logger.getLogger(CreateNewProcessWithLogicalStructureData.class);

	XPath xpath = null;

	/**
	 *
	 */
	public CreateNewProcessWithLogicalStructureData() {
		super(ConfigMain.getParameter("activeMQ.createNewProcessWithLogicalStructureData.queue", null));

		XPathFactory xFactory = XPathFactory.newInstance();
		xpath = xFactory.newXPath();
	}

	@Override
	protected void process(MapMessageObjectReader args) throws Exception {
		logger.info("CreateNewProcessWithLogicalStructureData got new ticket.");

		String processIdentifier = args.getMandatoryString("id");
		String templateName = args.getMandatoryString("template");
		String docType = args.getMandatoryString("docType");
		String xmlData = args.getMandatoryString("xml");
		Set<String> collections = args.getMandatorySetOfString("collections");
		Map<String, String> userFields = args.getMapOfStringToString("userMessageFields");

		createNewProcess(processIdentifier, templateName, docType, collections, xmlData, userFields);
	}

	/**
	 * Creates a new process with submitted information.
	 *
	 * @param processIdentifier used identifier for creating
	 * @param templateName name of template to be used
	 * @param docType name of document type to be used
	 * @param collections set of collections to be used
	 * @param xmlData holds data for global metadata and additional logical structure elements
	 * @param userFields holds field information
	 *
	 * @throws Exception
	 */
	private void createNewProcess(String processIdentifier, String templateName, String docType, Set<String> collections, String xmlData, Map<String, String> userFields) throws Exception {
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

			Document xmlDocument = createXmlDocument(xmlData);

			if (userFields == null) {
				userFields = new HashMap<String, String>();
			}

			userFields.putAll(getGlobalMetadata(xmlDocument));

			if (!userFields.isEmpty()) {
				setUserFields(newPFK, userFields);
			} else {
				throw new IllegalArgumentException("Userfields should not be empty!");
			}

			newPFK.CalcProzesstitel(); // use given meta data from userFields

			addAdditionalLogicalStructureData(newPFK, xmlDocument);

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
	 * Utility method to create a new process form object.
	 *
	 * @param templateName name of template to be used to create new process form
	 * @return returns created and basic intialized process form object.
	 */
	private ProzesskopieForm createNewProcessFromTemplate(String templateName) {
		ProzesskopieForm result = new ProzesskopieForm();

		Prozess selectedTemplate = getProcessTemplateByName(templateName);

		result.setProzessVorlage(selectedTemplate);
		result.Prepare();

		return result;
	}

	/**
	 * Utility method to look up for a process template to be used for creating a process form.
	 *
	 * @param templateName name of template which should be used
	 * @return returns a process template
	 */
	private Prozess getProcessTemplateByName(String templateName) {
		Criteria criteria;
		Session hibernateSession;
		List interimResult;
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
		result = (Prozess) interimResult.get(0);

		return result;
	}

	/**
	 * Utility method to validate given collections.
	 *
	 * @param pKF process form with a valid collections
	 * @param collections collections to proof if they are valid for this process form
	 * @return returns true on valid otherwise false
	 */
	private boolean validateCollectionsForProcess(ProzesskopieForm pKF, Set<String> collections) {
		HashSet<String> possibleCollections = new HashSet<String>(pKF.getPossibleDigitalCollections());
		return possibleCollections.containsAll(collections);
	}

	/**
	 * Utility method to validate document type name.
	 *
	 * @param pKF process form to proof against
	 * @param docType name of document type tp proof
	 * @return returns true if name are valid
	 */
	private boolean validDocTypeForProcess(ProzesskopieForm pKF, String docType) {
		Boolean fieldIsUsed = pKF.getStandardFields().get("doctype");

		if (fieldIsUsed == null || fieldIsUsed.equals(Boolean.FALSE))
			throw new IllegalArgumentException("Bad argument \"docType\": Selected template doesn’t provide the standard field \"doctype\".");

		boolean valueIsValid;
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
	private void setUserFields(ProzesskopieForm pKF, Map<String, String> userFields) throws RuntimeException {

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
	private void setAdditionalField(ProzesskopieForm pKF, String key, String value) throws RuntimeException {

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
	 * Utility method for adding logical structure elements.
	 *
	 * @param pKF process form which holds a digital document which is used for adding logical structure elements-
	 * @param xmlDocument holds logical structure information
	 * @throws Exception thrown if digital document or insert point inside digitial document could not be determinated
	 */
	private	void addAdditionalLogicalStructureData(ProzesskopieForm pKF, Document xmlDocument) throws Exception {

		DigitalDocument digitalDocument;
		DocStruct insertPoint;
		Prefs prefs;

		digitalDocument = getDigitalDocument(pKF);

		insertPoint = getInsertPointOfLogicalDocument(digitalDocument);

		prefs = getProjectPreferences(pKF);

		NodeList elements = extractListInformation(xmlDocument, "/bundle/folders/folder/elements/*");
		for (int i = 0; i < elements.getLength(); i++){
			Node currentNode = elements.item(i);
			String nodeName = currentNode.getNodeName();
			if (nodeName.equals("letter")) {
				logger.debug("Found letter - continue processing...");
				DocStruct letterElement = processLetterElement(prefs, digitalDocument, currentNode);
				insertPoint.addChild(letterElement);
			}   else {
				logger.error("Unknown element named: '" + nodeName + "' Skipping...");
			}
		}
	}

	/**
	 * Utility method to get digital document.
	 *
	 * @param pKF process form to use
	 * @return returns digital document of process form
	 * @throws Exception thrown if internal file format is null or no digital document is available
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
	 * Utility method to get useful entry point for later created structure elements.
	 *
	 * @param digitalDocument used digital document
	 * @return returns first logical structure element of given digital document
	 * @throws Exception thrown if logical document structure is invalid or there are no child element
	 */
	private DocStruct getInsertPointOfLogicalDocument(DigitalDocument digitalDocument) throws Exception {

		DocStruct logicalDocStruct;

		logicalDocStruct = digitalDocument.getLogicalDocStruct();
		if (logicalDocStruct == null) {
			throw new Exception("Could not retrieve logical document structure.");
		}

		List<DocStruct> children;
		DocStruct firstChild;
		children = logicalDocStruct.getAllChildren();
		if (children == null || children.isEmpty()) {
			throw new Exception("Expecting children, but none found.");
		}
		if (children.size() > 1) {
			logger.warn("More than one child element found. Using first child!");
		}

		firstChild = children.get(0);
		if (firstChild == null) {
			throw new Exception("Expecting at leat one children of logical document structure.");
		}

		return firstChild;
	}

	/**
	 * Utility method to get preferences of current process.
	 *
	 * @param pKF process form which holds current process
	 * @return preferences of current process
	 * @throws Exception thrown if Regelsatz or depending preferences could not be found.
	 */
	private Prefs getProjectPreferences(ProzesskopieForm pKF) throws Exception {
		Regelsatz regelsatz;
		Prefs prefs;

		regelsatz = pKF.getProzessKopie().getRegelsatz();
		if (regelsatz == null) {
			throw new Exception("Could not retrieve Regelsatz for current process.");
		}

		prefs = regelsatz.getPreferences();
		if (prefs == null) {
			throw new Exception("Could not retrieve preferences for current process.");
		}

		return prefs;
	}

	/**
	 * Create new structure element of given name.
	 *
	 * @param digitalDocument digital document as source for creating
	 * @param prefs preferences to use
	 * @param docStructName name of new document structure element
	 * @return returns created structure element
	 * @throws Exception thrown if structure element could not be found in preferences or element is not allowed
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
	 * Create a metadata element and add its value.
	 *
	 * @param prefs holds preferences for to be generated metadata element
	 * @param metadataTypeName name of metadata element to be created
	 * @param metadataValue value of new metadata element
	 * @return returns created metadata element
	 * @throws Exception thrown if metadata name could not be found
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

	/**
	 * Create a usable dom document with given xml string data.
	 *
	 * @param xmlData holds xml
	 * @return returns a dom document
	 * @throws Exception thrown if parsing of xml data failed
	 */
	private Document createXmlDocument(String xmlData) throws Exception {

		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		Document doc;
		InputSource source;

		factory = DocumentBuilderFactory.newInstance();
		source = new InputSource(new StringReader(xmlData));

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(source);
		} catch (ParserConfigurationException e) {
			throw new Exception("Parser configuration error while converting xml data string into dom object. Reason: " + e.getMessage());
		} catch (SAXException e) {
			throw new Exception("SAX error while converting xml data string into dom object. Reason: " + e.getMessage());
		} catch (IOException e) {
			throw new Exception("IO error while converting xml data string into dom object. Reason: " + e.getMessage());
		}

		return doc;
	}

	/**
	 * Add single metadata fields for structure element letter.
	 *
	 * @param prefs holds preferences for created metadata elements
	 * @param digitalDocument global digital document used for creating structure element letter
	 * @param currentNode holds values for metada fields
	 * @return returns a created and well populated structure element
	 * @throws Exception
	 */
	private DocStruct processLetterElement(Prefs prefs, DigitalDocument digitalDocument, Node currentNode) throws Exception {
		DocStruct letterElement = createDocStructElement(digitalDocument, prefs, "Letter");
		addMetadataToStructure(prefs, letterElement, "CatalogIDDigital", extractTextInformation(currentNode, "./id"));
		addMetadataToStructure(prefs, letterElement, "slub_comment", extractTextInformation(currentNode, "./folio"));
		addMetadataToStructure(prefs, letterElement, "shelfmarksource", extractTextInformation(currentNode, "./signature"));
		addMetadataToStructure(prefs, letterElement, "shelfmarksource", extractTextInformation(currentNode, "./further-signature"));

		addPersonDataToStructure(prefs, letterElement, "Author", extractListInformation(currentNode, "./creator"));
		addPersonDataToStructure(prefs, letterElement, "slub_Recipient", extractListInformation(currentNode, "./addressee"));

		String place = extractTextInformation(currentNode, "./origin");
		String date = extractTextInformation(currentNode, "./date");
		String formatedDate = formatDateString(date);
		String generatedTitle;
		String title = extractTextInformation(currentNode, "./title");
		String firstAuthor = convertPersonName(extractTextInformation(currentNode, "./creator[1]/name"));
		String firstRecipient = convertPersonName(extractTextInformation(currentNode, "./addressee[1]/name"));

		generatedTitle = title + " von " + firstAuthor + " an " + firstRecipient + ", " + place + ", " + formatedDate;

		addMetadataToStructure(prefs, letterElement, "TitleDocMain", generatedTitle);
		addMetadataToStructure(prefs, letterElement, "SizeSourcePrint", extractTextInformation(currentNode, "./extent"));
		addMetadataToStructure(prefs, letterElement, "slub_dateCreated", formatedDate);
		addMetadataToStructure(prefs, letterElement, "PlaceOfPublication", place);
		addMetadataToStructure(prefs, letterElement, "DocLanguage", extractTextInformation(currentNode, "./language"));
		addMetadataToStructure(prefs, letterElement, "FormatSourcePrint", extractTextInformation(currentNode, "./dimensions"));

		return letterElement;
	}

	/**
	 * Retrieve global used metadata informations from xml dom document.
	 *
	 * @param doc dom document used for retrieving data
	 * @return returns a key value map
	 */
	private Map<String, String> getGlobalMetadata(Document doc) {
		Map<String, String> result = new HashMap<String, String>();

		try {

			// required fields
			result.put("Titel Konvolut", extractTextInformation(doc, "/bundle/title"));
			result.put("Identifier Konvolut", extractTextInformation(doc, "/bundle/id"));
			result.put("Identifier Mappe", extractTextInformation(doc, "/bundle/folders/folder/id"));
			result.put("Mappennummer", extractTextInformation(doc, "/bundle/folders/folder/folder"));

			String folderTitle = extractTextInformation(doc, "/bundle/folders/folder/title");

			// optional fields
			result.put("ATS", createAtsTls(folderTitle, ""));
			result.put("Titel (Mappe)", folderTitle);
			result.put("Signatur", extractTextInformation(doc, "/bundle/folders/folder/signature"));

			result.put("Besitzende Institution (Vorlage)", extractTextInformation(doc, "/bundle/owner/name"));
			result.put("Besitzende Institution (Digitalisat)", extractTextInformation(doc, "/bundle/owner/name"));

		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
		}

		return result;
	}

	/**
	 * Returns a text value for a given xpath expression on a specific object.
	 *
	 * @param item object to be searched
	 * @param xpathExpression xpath expression used for searching
	 * @return returns founded text value
	 * @throws XPathExpressionException thrown if xpath expression is invalid
	 */
	private String extractTextInformation(Object item, String xpathExpression) throws XPathExpressionException {
		String result;

		result = (String) extractInformation(item, xpathExpression, XPathConstants.STRING);

		return result;
	}

	/**
	 * Returns a list of objects for a given xpath expression.
	 *
	 * @param item object to be searched
	 * @param xpathExpression xpath pression used for searching
	 * @return list of nodes which contains searched data
	 * @throws XPathExpressionException thrown if xpath expression is invalid
	 */
	private NodeList extractListInformation(Object item, String xpathExpression) throws XPathExpressionException {
		NodeList nodes;

		nodes = (NodeList) extractInformation(item, xpathExpression, XPathConstants.NODESET);

		return nodes;
	}

	/**
	 * Returns a object of a xpath expression on given search object.
	 *
	 * @param item object to be searched
	 * @param xpathExpression xpath expression used for searching
	 * @param returnValue expected return value (string, list, ...)
	 * @return returns a unspecific object - casting to result definition neccessary
	 * @throws XPathExpressionException thrown if xpath expression is invalid
	 */
	private Object extractInformation(Object item, String xpathExpression, QName returnValue) throws XPathExpressionException {
		XPathExpression expr;
		Object result;

		expr = xpath.compile(xpathExpression);
		result = expr.evaluate(item, returnValue);

		return result;
	}

	/**
	 * Utility method for creating ats / tls.
	 *
	 * @param title name of title to be used
	 * @param authors name of authors to be used
	 * @return returns generated ats / tls
	 */
	private String createAtsTls(String title, String authors) {
		ImportOpac importOpac = new ImportOpac();
		String result;
		result = importOpac.createAtstsl(title, authors);
		logger.debug("generated ats tls: '" + result + "'");
		return result;
	}

	/**
	 * Add a metadata element to structure.
	 *
	 * @param prefs for retrieving metadata element
	 * @param docStructElement structure element to add new metadata element
	 * @param metadataName name of metadata element
	 * @param metadataValue value of metadata element
	 * @throws Exception thrown if meta data element could not be created or added to structure element
	 */
	private void addMetadataToStructure(Prefs prefs, DocStruct docStructElement, String metadataName, String metadataValue) throws Exception {
		Metadata metadataElement;

		metadataElement = createMetadataElement(prefs, metadataName, metadataValue);
		metadataElement.setDocStruct(docStructElement); // adding reference to structure element

		// putting data together
		docStructElement.addMetadata(metadataElement); // adding metadata object to structure element
	}

	/**
	 * Adds person data to a structure element.
	 *
	 * @param prefs for retrieving metadata element
	 * @param docStructElement structure element to add new person data
	 * @param personRole role of a person f.e. author, recipient, ...
	 * @param personList a list of persons to add
	 * @throws Exception if person role is not a valid element
	 */
	private void addPersonDataToStructure(Prefs prefs, DocStruct docStructElement, String personRole, NodeList personList) throws Exception {
		Person person;
		MetadataType mdt;

		mdt = prefs.getMetadataTypeByName(personRole);
		if (mdt == null) {
			throw new Exception("Could not retrieve metadata type for '" + personRole + "'.");
		}

		logger.debug("Create " + personList.getLength() + " new persons for role " + personRole + ".");

		for (int i = 0; i < personList.getLength(); i++) {
			Node currentNode = personList.item(i);
			person = new Person(mdt);
	        String firstName = "";
			String lastName;
	        String personName = extractTextInformation(currentNode, "./name");

			HashMap<String, String> splittedPersonName = splitPersonName(personName);
			if (splittedPersonName.size() == 2) {
				firstName = splittedPersonName.get("firstName");
				lastName = splittedPersonName.get("lastName");
			} else {
				lastName = splittedPersonName.get("lastName");
			}

			logger.debug("first name: " + firstName + " lastname: " + lastName + " gnd: " + extractTextInformation(currentNode, "./gnd-id"));
			person.setFirstname(firstName.trim());
			person.setLastname(lastName.trim());
			person.setIdentifier(extractTextInformation(currentNode, "./gnd-id"));
			person.setIdentifierType("GND");
			person.setRole(personRole);

			// putting data together
			docStructElement.addPerson(person);
		}
	}

	/**
	 * Format a date string from yyyyMMdd into dd.MM.yyyy
	 *
	 * @param inputDate date string in yyyyMMdd format
	 * @return result date string in dd.MM.yyyy
	 */
	private String formatDateString(String inputDate) {
		String inputFormat = "yyyyMMdd";
		String resultFormat = "dd.MM.yyyy";
		return formatDateString(inputDate, inputFormat, resultFormat);
	}

	/**
	 * Format a given date string into an other date string format
	 *
	 * @param inputDate given date string to convert
	 * @param inputFormat format of input date string
	 * @param resultFormat result format to convert date string
	 * @return returns formated date string in new format
	 */
	private String formatDateString(String inputDate, String inputFormat, String resultFormat) {
		Date date;
		SimpleDateFormat parser;
		SimpleDateFormat formatter;
		String resultDate;

		parser = new SimpleDateFormat(inputFormat);
		formatter = new SimpleDateFormat(resultFormat);

		try {
			date = parser.parse(inputDate);
			resultDate = formatter.format(date);
		} catch (ParseException e) {
			logger.warn("Input date '" + inputDate + "' is not valid for format '" + inputFormat + "'.");
			resultDate = inputDate;
		}

		return resultDate;
	}

	/**
	 * Split a given person name <last name><comma><first name> into first and last name entry.
	 * Exception to this is, if person name contains no comma as seperator or person name contains < and >
	 *
	 * @param personName person name to split
	 * @return returns as hash map with at least lastName key and optional firstName key
	 */
	private HashMap<String, String> splitPersonName(String personName) {
		HashMap<String, String> result = new HashMap<String, String>();
		String[] splitted = personName.split(",", 2);

		if (personName.contains("<") && personName.contains(">")) {
			result.put("lastName", personName.trim());
		} else if (splitted.length == 2){
			result.put("firstName", splitted[1].trim());
			result.put("lastName", splitted[0].trim());
		} else if (splitted.length == 1) {
			result.put("lastName", personName.trim());
		}

		return result;
	}

	/**
	 * Convert a given person name in format <last name><comma><first name> into <first name> <last name>
	 *
	 * @param personName person name to convert
	 * @return returns converted person name (could be the same as input)
	 */
	private String convertPersonName(String personName) {
		HashMap<String, String> splittedName;
		String result;

		splittedName = splitPersonName(personName);
		if (splittedName.size() == 2) {
			result = splittedName.get("firstName") + " " + splittedName.get("lastName");
		} else {
			result = splittedName.get("lastName");
		}

		return result;
	}

}
