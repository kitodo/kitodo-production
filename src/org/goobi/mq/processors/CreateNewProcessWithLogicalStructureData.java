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
		String xmlData = args.getMandatoryString("xml");
		Set<String> collections = args.getMandatorySetOfString("collections");

		createNewProcess(processIdentifier, templateName, docType, collections, xmlData);
	}

	/**
	 *
	 * @param processIdentifier
	 * @param templateName
	 * @param docType
	 * @param collections
	 * @param xmlData
	 * @throws Exception
	 */
	protected void createNewProcess(String processIdentifier, String templateName, String docType, Set<String> collections, String xmlData) throws Exception {
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

			Map<String, String> userFields = getGlobalMetadata(xmlDocument);

			if ((userFields != null) && (!userFields.isEmpty())) {
				setUserFields(newPFK, userFields);
			} else {
				throw new IllegalArgumentException("Userfields should not be null!");
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
	 * @param xmlDocument
	 * @throws Exception
	 */
	protected void addAdditionalLogicalStructureData(ProzesskopieForm pKF, Document xmlDocument) throws Exception {

		DigitalDocument digitalDocument;
		DocStruct insertPoint;
		Prefs prefs;

		digitalDocument = getDigitalDocument(pKF);

		insertPoint = getInsertPointOfLogicalDocument(digitalDocument);

		prefs = getProjectPreferences(pKF);

		NodeList elements = extractListInformation(xmlDocument, "/convolute/folders/folder/elements/*");
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
	 * @return
	 * @throws Exception
	 */
	private DocStruct getInsertPointOfLogicalDocument(DigitalDocument digitalDocument) throws Exception {

		DocStruct logicalDocStruct;
		DocStruct insertPoint;

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

		insertPoint = firstChild;

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

	/**
	 *
	 * @param xmlData
	 * @return
	 */
	private Document createXmlDocument(String xmlData) {

		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		Document doc = null;
		InputSource source;

		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		source = new InputSource(new StringReader(xmlData));

		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(source);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		return doc;
	}

	/**
	 *
	 * @param prefs
	 * @param digitalDocument
	 * @param currentNode
	 * @return
	 * @throws Exception
	 */
	private DocStruct processLetterElement(Prefs prefs, DigitalDocument digitalDocument, Node currentNode) throws Exception {
		DocStruct letterElement = createDocStructElement(digitalDocument, prefs, "Letter");
		addMetadataToStructure(prefs, letterElement, "CatalogIDDigital", extractTextInformation(currentNode, "./id"));
		addMetadataToStructure(prefs, letterElement, "slub_comment", "Blatt: " + extractTextInformation(currentNode, "./folio"));
		addMetadataToStructure(prefs, letterElement, "shelfmarksource", extractTextInformation(currentNode, "./signature"));
		addMetadataToStructure(prefs, letterElement, "shelfmarksource", extractTextInformation(currentNode, "./further-signature"));

		addPersonDataToStructure(prefs, letterElement, "Author", extractListInformation(currentNode, "./creator"));
		addPersonDataToStructure(prefs, letterElement, "slub_Recipient", extractListInformation(currentNode, "./addressee"));

		String place = extractTextInformation(currentNode, "./origin");
		String date = extractTextInformation(currentNode, "./date");
		String generatedTitle;
		String title = extractTextInformation(currentNode, "./title");
		String firstAuthor = extractTextInformation(currentNode, "./creator[1]/name");
		String firstRecipient = extractTextInformation(currentNode, "./addressee[1]/name");

		generatedTitle = title + " von " + firstAuthor + " an " + firstRecipient + ", " + place + ", " + date;

		addMetadataToStructure(prefs, letterElement, "TitleDocMain", generatedTitle);
		addMetadataToStructure(prefs, letterElement, "slub_comment", "Umfang: " + extractTextInformation(currentNode, "./extent"));
		addMetadataToStructure(prefs, letterElement, "slub_comment", "Entstehungszeit: " + date);
		addMetadataToStructure(prefs, letterElement, "slub_Place", "Entstehungsort: " + place);
		addMetadataToStructure(prefs, letterElement, "DocLanguage", extractTextInformation(currentNode, "./language"));
		addMetadataToStructure(prefs, letterElement, "slub_comment", "Formatangabe: " + extractTextInformation(currentNode, "./dimensions"));
		addMetadataToStructure(prefs, letterElement, "slub_ownerOrig", extractTextInformation(currentNode, "/convolute/owner/name"));
		addMetadataToStructure(prefs, letterElement, "slub_ownerDigi", extractTextInformation(currentNode, "/convolute/owner/name"));

		return letterElement;
	}

	/**
	 *
	 * @param doc
	 * @return
	 */
	protected Map<String, String> getGlobalMetadata(Document doc) {
		Map<String, String> result = new HashMap<String, String>();

		try {
			// required fields
			result.put("Schrifttyp", "keine OCR");
			result.put("Titel Konvolut", extractTextInformation(doc, "/convolute/title"));
			result.put("Identifier Konvolut", extractTextInformation(doc, "/convolute/id"));
			result.put("Identifier Mappe", extractTextInformation(doc, "/convolute/folders/folder/id"));
			result.put("Mappennummer", extractTextInformation(doc, "/convolute/folders/folder/folder"));

			String portfolioTitle = extractTextInformation(doc, "/convolute/folders/folder/title");

			// optional fields
			result.put("Artist", "SLUB");
			result.put("ATS", createAtsTls(portfolioTitle, ""));
			result.put("Titel Konvolut (Sortierung)", "");
			result.put("Autoren", "");
			result.put("Titel (Mappe)", portfolioTitle);
			result.put("Titel (Mappe) (Sortierung)", "");
			result.put("Autoren (Mappe)", "");
			result.put("Signatur", extractTextInformation(doc, "/convolute/folders/folder/signature"));
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
		}

		return result;
	}

	/**
	 *
	 * @param item
	 * @param xpathExpression
	 * @return
	 * @throws XPathExpressionException
	 */
	private String extractTextInformation(Object item, String xpathExpression) throws XPathExpressionException {
		String result;

		result = (String) extractInformation(item, xpathExpression, XPathConstants.STRING);

		return result;
	}

	/**
	 *
	 * @param item
	 * @param xpathExpression
	 * @return
	 * @throws XPathExpressionException
	 */
	private NodeList extractListInformation(Object item, String xpathExpression) throws XPathExpressionException {
		NodeList nodes;

		nodes = (NodeList) extractInformation(item, xpathExpression, XPathConstants.NODESET);

		return nodes;
	}

	/**
	 *
	 * @param item
	 * @param xpathExpression
	 * @param returnValue
	 * @return
	 * @throws XPathExpressionException
	 */
	private Object extractInformation(Object item, String xpathExpression, QName returnValue) throws XPathExpressionException {
		XPathExpression expr;
		Object result;

		expr = xpath.compile(xpathExpression);
		result = expr.evaluate(item, returnValue);

		return result;
	}

	/**
	 *
	 * @param title
	 * @param authors
	 * @return
	 */
	private String createAtsTls(String title, String authors) {
		ImportOpac importOpac = new ImportOpac();
		String result;
		result = importOpac.createAtstsl(title, authors);
		logger.debug("generated ats tls: '" + result + "'");
		return result;
	}

	/**
	 *
	 * @param prefs
	 * @param docStructElement
	 * @param metadataName
	 * @param metadataValue
	 * @throws Exception
	 */
	private void addMetadataToStructure(Prefs prefs, DocStruct docStructElement, String metadataName, String metadataValue) throws Exception {
		Metadata metadataElement;

		metadataElement = createMetadataElement(prefs, metadataName, metadataValue);
		metadataElement.setDocStruct(docStructElement); // adding reference to document structure

		// putting data together
		docStructElement.addMetadata(metadataElement); // adding metadata object to document structure
	}

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
			String lastName = "";
	        String personName = extractTextInformation(currentNode, "./name");

			if (personName.contains("<") || personName.contains(">")) {
				lastName = personName;
			} else {
				String nameParts[] = personName.split(",", 2);
				if (nameParts.length == 2) {
					firstName = nameParts[1];
					lastName = nameParts[0];
				} else {
					lastName = personName;
				}
			}
			logger.debug("first name: " + firstName + " lastname: " + lastName + " gnd: " + extractTextInformation(currentNode, "./gnd-id"));
			person.setFirstname(firstName.trim());
			person.setLastname(lastName.trim());
			person.setIdentifier(extractTextInformation(currentNode, "./gnd-id"));
			person.setIdentifierType("GND");
			person.setRole(personRole);

			docStructElement.addPerson(person);
		}
	}

}
