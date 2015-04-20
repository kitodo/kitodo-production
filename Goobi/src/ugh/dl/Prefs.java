package ugh.dl;

/*******************************************************************************
 * ugh.dl / Prefs.java
 * 
 * Copyright 2010 Center for Retrospective Digitization, Göttingen (GDZ)
 * 
 * http://gdz.sub.uni-goettingen.de
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ugh.exceptions.PreferencesException;

/*******************************************************************************
 * <b>Title:</b> Preferences
 * 
 * <b>Description:</b> Reads global preferences (ruleset files) and provides methods to access information and retrieve information about
 * <code>MetadataType</code> and <code>DocStructType</code> objects.
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-18
 * @since 2004-05-21
 * 
 *        TODOLOG
 * 
 *        TODO Remove the "p004" error codes? Where do they come from anyway??
 * 
 *        CHANGELOG
 *        
 *        18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *        13.02.2010 --- Funk --- Refactored some conditionals and loops.
 * 
 *        14.12.2009 --- Funk --- Added the getAllAnchorDocStructTypes() method.
 * 
 *        17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *        30.10.2009 --- Funk --- Added generated serialVersionUID.
 * 
 *        24.10.2008 --- Funk --- Commented out the setting of: "current_DocStrctType.setMyPrefs(this);". Do we need that? I think not!
 * 
 *        29.09.2008 --- Funk --- Added log4j logging, removed the debug level methods.
 * 
 *        29.04.2008 --- Funk --- Added public setDebug() method.
 * 
 ******************************************************************************/

public class Prefs implements Serializable {

    private final static String VERSION = "1.1-20091117";

    private static final long serialVersionUID = 6162006030440683152L;
    private static final Logger LOGGER = Logger.getLogger(ugh.dl.DigitalDocument.class);
    private static final String HIDDEN_METADATA_CHAR = "_";

    private List<DocStructType> allDocStrctTypes;
    private List<MetadataType> allMetadataTypes;
    private final List<MetadataGroupType> allMetadataGroupTypes;
    private final Hashtable<String, Node> allFormats;

    public static final short ELEMENT_NODE = 1;

    /***************************************************************************
     * <p>
     * Constructor.
     * </p>
     **************************************************************************/
    public Prefs() {
        this.allDocStrctTypes = new LinkedList<DocStructType>();
        this.allMetadataTypes = new LinkedList<MetadataType>();
        this.allMetadataGroupTypes = new LinkedList<MetadataGroupType>();
        this.allFormats = new Hashtable<String, Node>();
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getVersion() {
        return VERSION;
    }

    /***************************************************************************
     * <p>
     * Loads all known DocStruct types from the prefs XML file.
     * </p>
     * 
     * @param filename
     * @return
     * @throws PreferencesException
     **************************************************************************/
    public boolean loadPrefs(String filename) throws PreferencesException {

        Document document;
        NodeList childlist;
        NodeList upperChildlist;
        Node upperchild;
        // Single node of the childlist.
        Node currentNode;
        // New document builder instance
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Do not validate xml file.
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(false);

        DocStructType parsedDocStrctType;
        MetadataType parsedMetadataType;

        MetadataGroupType parsedMetadataGroup;

        // Read file and parse it.
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filename));
        } catch (SAXParseException e) {
            // Error generated by the parser.
            String message = "Parse error at line " + e.getLineNumber() + ", uri " + e.getSystemId() + "!";
            LOGGER.error(message);
            throw new PreferencesException(message, e);
        } catch (SAXException e) {
            LOGGER.error(e);
            throw new PreferencesException(e);
        } catch (ParserConfigurationException e) {
            LOGGER.error(e);
            throw new PreferencesException(e);
        } catch (IOException e) {
            String message = "Unable to load preferences file '" + filename + "'!";
            LOGGER.error(message);
            throw new PreferencesException(message, e);
        }

        // File was parsed; DOM was created.
        //
        // Parse the DOM.
        upperChildlist = document.getElementsByTagName("Preferences");
        if (upperChildlist == null) {
            // No preference element -> wrong XML file.
            String message = "Preference file does not begin with <Preferences> element!";
            LOGGER.error(message);
            throw new PreferencesException(message);
        }

        // Get first preferences element.
        upperchild = upperChildlist.item(0);
        if (upperchild == null) {
            String message = "No upper child in preference file";
            LOGGER.error(message);
            throw new PreferencesException(message);
        }

        childlist = upperchild.getChildNodes();
        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            currentNode = childlist.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("DocStrctType")) {
                    NamedNodeMap nnm = currentNode.getAttributes();
                    Node topmost = nnm.getNamedItem("topStruct");
                    Node fileset = nnm.getNamedItem("fileset");
                    String topmostvalue = null;
                    String filesetvalue = null;
                    parsedDocStrctType = null;
                    if (topmost != null) {
                        topmostvalue = topmost.getNodeValue();
                    }
                    if (fileset != null) {
                        filesetvalue = fileset.getNodeValue();
                    }
                    parsedDocStrctType = parseDocStrctType(currentNode);
                    if (parsedDocStrctType != null) {
                        if (topmostvalue != null && (topmostvalue.equals("yes") || topmostvalue.equals("true"))) {
                            parsedDocStrctType.setTopmost(true);
                        }
                        if (filesetvalue != null && (filesetvalue.equals("no") || filesetvalue.equals("false"))) {
                            parsedDocStrctType.setHasFileSet(false);
                        }

                        this.allDocStrctTypes.add(parsedDocStrctType);
                    }
                }

                if (currentNode.getNodeName().equals("MetadataType")) {
                    parsedMetadataType = parseMetadataType(currentNode);
                    if (parsedMetadataType != null) {
                        this.allMetadataTypes.add(parsedMetadataType);
                    }
                }

                if (currentNode.getNodeName().equals("Group")) {
                    parsedMetadataGroup = parseMetadataGroup(currentNode);
                    if (parsedMetadataGroup != null) {
                        this.allMetadataGroupTypes.add(parsedMetadataGroup);
                    }
                }

                if (currentNode.getNodeName().equals("Formats")) {
                    // Get all formats.
                    NodeList formatlist = currentNode.getChildNodes();
                    for (int x = 0; x < formatlist.getLength(); x++) {
                        Node currentnode = formatlist.item(x);
                        if (currentnode.getNodeType() == ELEMENT_NODE) {
                            this.allFormats.put(currentnode.getNodeName(), currentnode);
                        }
                    }
                }
            }
        }

        // Add internal metadata types; all internal metadata types are
        // beginning with HIDDEN_METADATA_CHAR.
        MetadataType mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "pagephysstart");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "overlapping");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "pagephysend");
        this.allMetadataTypes.add(mdt);

        mdt = new MetadataType();
        mdt.setName(HIDDEN_METADATA_CHAR + "PaginationNo");
        this.allMetadataTypes.add(mdt);

        return true;
    }

    /***************************************************************************
     * <p>
     * Parses just the part of the XML-file which contains information about a single DocStrctType (everything inside the DocStrctType element).
     * </p>
     * 
     * @param theDocStrctTypeNode
     * @return DocStructType instance
     * @throws PreferencesException
     **************************************************************************/
    public DocStructType parseDocStrctType(Node theDocStrctTypeNode) {

        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attribNode;
        // Attribute containing information, if metadata type should be
        // displayed, even if it has no content.
        Node defaultNode;
        // Attribute containing information, if metadata type should be
        // displayed, if it has content.
        Node invisibleNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        String mdtypeName;
        String mdtypeNum;
        String allowedChild;
        DocStructType currentDocStrctType = new DocStructType();

        // Get all children.
        allchildren = theDocStrctTypeNode.getChildNodes();
        HashMap<String, String> allLanguages = new HashMap<String, String>();

        // Get attributes for docstructtype first.
        //
        // Get all attributes.
        NamedNodeMap attrnodes = theDocStrctTypeNode.getAttributes();
        if (attrnodes != null) {
            // Check if it's an anchor.
            Node typenode = attrnodes.item(0);
			if (typenode != null && typenode.getNodeName().equals("anchor")) {
				currentDocStrctType.setAnchorClass(typenode.getNodeValue());
            }
        }

        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("Name")) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for DocStrctType unknown (error code p004a)");
                            // No text node available; maybe is's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currentDocStrctType.setName(textnode.getNodeValue());
                    } else {
                        // Error; Name-element is empty element.
                        LOGGER.error("Error reading config for DocStrctType unknown (error code p004)");
                        return null;
                    }
                }
                if (currentNode.getNodeName().equals("language")) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("name");
                    if (attribNode == null) {
                        LOGGER.error("No name definition for language (" + currentDocStrctType.getName() + "); " + currentDocStrctType.getName()
                                + "; Error Code: p005");
                        return null;
                    }
                    languageName = attribNode.getNodeValue();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for DocStrctType " + currentDocStrctType.getName() + "; Error Code: p006");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        languageValue = textnode.getNodeValue();
                    }

                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                }
                // Read all types which are allowed for this documentstructure
                // type.
                if (currentNode.getNodeName().equals("metadata")) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("num");
                    defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");
                    invisibleNode = attributeNodelist.getNamedItem("Invisible");

                    if (attribNode == null) {
                        mdtypeNum = "1";
                        LOGGER.warn("Num attribute not set for <metadata> element!");
                    } else {
                        // Get max. number: 1,+,*
                        mdtypeNum = attribNode.getNodeValue();
                    }
                    // Get value for DefaultDisplay attribute.
                    String defaultValue = null;
                    if (defaultNode != null) {
                        defaultValue = defaultNode.getNodeValue();
                    }
                    mdtypeName = "";

                    String invisibleValue = null;
                    if (invisibleNode != null) {
                        invisibleValue = invisibleNode.getNodeValue();
                    }

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue();
                    }

                    if (mdtypeName == null || mdtypeNum == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    // MetadataType newMDType=new MetadataType();
                    // newMDType.setName(mdtype_name);
                    MetadataType newMdType = getMetadataTypeByName(mdtypeName);
                    if (newMdType == null) {
                        LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    // Set max. number.
                    newMdType.setNum(mdtypeNum);
                    MetadataType result = null;

                    // Handle Invisible attribute.
                    boolean invisible = false;
                    if (invisibleValue != null && (invisibleValue.equalsIgnoreCase("true") || invisibleValue.equalsIgnoreCase("yes"))) {
                        invisible = true;
                    }

                    // Handle DefaultDisplay attribute.
                    if (defaultValue != null) {
                        if (defaultValue.equalsIgnoreCase("true") || defaultValue.equalsIgnoreCase("yes")) {
                            result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum, true, invisible);
                        } else {
                            result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum, false, invisible);
                        }
                    } else {
                        result = currentDocStrctType.addMetadataType(newMdType, mdtypeNum);
                    }

                    if (result == null) {
                        // Error occurred; so exit this method; no new
                        // DocStrctType.
                        LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Can't add metadatatype '"
                                + newMdType.getName() + "'");
                        return null;
                    }
                }
                
                if (currentNode.getNodeName().equals("group")) {
                    attributeNodelist = currentNode.getAttributes();
                    attribNode = attributeNodelist.getNamedItem("num");
                    defaultNode = attributeNodelist.getNamedItem("DefaultDisplay");
                    invisibleNode = attributeNodelist.getNamedItem("Invisible");

                    if (attribNode == null) {
                        mdtypeNum = "1";
                        LOGGER.warn("Num attribute not set for <group> element!");
                    } else {
                        // Get max. number: 1,+,*
                        mdtypeNum = attribNode.getNodeValue();
                    }
                    // Get value for DefaultDisplay attribute.
                    String defaultValue = null;
                    if (defaultNode != null) {
                        defaultValue = defaultNode.getNodeValue();
                    }
                    mdtypeName = "";

                    String invisibleValue = null;
                    if (invisibleNode != null) {
                        invisibleValue = invisibleNode.getNodeValue();
                    }

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue();
                    }

                    if (mdtypeName == null || mdtypeNum == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    // MetadataType newMDType=new MetadataType();
                    // newMDType.setName(mdtype_name);
                    MetadataGroupType newMdGroup = getMetadataGroupTypeByName(mdtypeName);
                    if (newMdGroup == null) {
                        LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    // Set max. number.
                    newMdGroup.setNum(mdtypeNum);
                    MetadataGroupType result = null;

                    // Handle Invisible attribute.
                    boolean invisible = false;
                    if (invisibleValue != null && (invisibleValue.equalsIgnoreCase("true") || invisibleValue.equalsIgnoreCase("yes"))) {
                        invisible = true;
                    }

                    // Handle DefaultDisplay attribute.
                    if (defaultValue != null) {
                        if (defaultValue.equalsIgnoreCase("true") || defaultValue.equalsIgnoreCase("yes")) {
                            result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum, true, invisible);
                        } else {
                            result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum, false, invisible);
                        }
                    } else {
                        result = currentDocStrctType.addMetadataGroup(newMdGroup, mdtypeNum);
                    }

                    if (result == null) {
                        // Error occurred; so exit this method; no new
                        // DocStrctType.
                        LOGGER.error("Error reading config for DocStrctType '" + currentDocStrctType.getName() + "'! Can't add metadatatype '"
                                + newMdGroup.getName() + "'");
                        return null;
                    }
                }

                // Read type of DocStruct, which is allowed as children.
                if (currentNode.getNodeName().equals("allowedchildtype")) {
                    allowedChild = "";
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Syntax Error reading config for DocStrctType '" + currentDocStrctType.getName()
                                    + "'! Expected a text node under <allowedchildtype> element containing the DocStructType's name");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        allowedChild = textnode.getNodeValue();
                    }

                    // Check, if an appropriate DocStruct Type is defined.
                    boolean bResult = currentDocStrctType.addDocStructTypeAsChild(allowedChild);
                    if (!bResult) {
                        // Error occurred; so exit this method; no new
                        // DocStrctType.
                        LOGGER.error("Error reading config for DocStructType '" + currentDocStrctType.getName()
                                + "'! Can't addDocStructType as child '" + allowedChild + "'");
                        return null;
                    }
                }
            }

            // Add allLanguages and all Metadata to DocStrctType.
            currentDocStrctType.setAllLanguages(allLanguages);
        }

        return currentDocStrctType;
    }

    /***************************************************************************
     * @param theMetadataTypeNode
     * @return
     **************************************************************************/
    public MetadataType parseMetadataType(Node theMetadataTypeNode) {

        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attributeNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        HashMap<String, String> allLanguages = new HashMap<String, String>();

        MetadataType currenMdType = new MetadataType();

        NamedNodeMap nnm = theMetadataTypeNode.getAttributes();
        // Get type attribute.
        Node node = nnm.getNamedItem("type");
        if (node != null) {
            String nodevalue = node.getNodeValue();
            if (nodevalue != null && nodevalue.equals("person")) {
                currenMdType.setIsPerson(true);
            }
            if (nodevalue != null && nodevalue.equals("identifier")) {
                currenMdType.setIdentifier(true);
            }
        }

        allchildren = theMetadataTypeNode.getChildNodes(); // get allchildren
        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("Name")) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            LOGGER.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p002b! Expected a text node under <Name> attribute at '" + theMetadataTypeNode.getNodeName()
                                    + "'. <Name> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for MetadataType unknown; Error Code: p002! Expected a text node under <Name> element at '"
                                    + theMetadataTypeNode.getNodeName() + "'");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currenMdType.setName(textnode.getNodeValue());
                    }
                }
                if (currentNode.getNodeName().equals("language")) {
                    attributeNodelist = currentNode.getAttributes();
                    attributeNode = attributeNodelist.getNamedItem("name");
                    languageName = attributeNode.getNodeValue();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            LOGGER.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001b! Expected a text node under <language> attribute at '" + theMetadataTypeNode.getNodeName()
                                    + "'. <language> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Syntax Error reading config for MetadataType " + currenMdType.getName()
                                    + "; Error Code: p001! Wrong node type under <language> attribute - a text node was expected at "
                                    + theMetadataTypeNode.getNodeName());
                            return null;
                        }
                        languageValue = textnode.getNodeValue();
                    }
                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                }
            }
        }

        // Add allLanguages and all Metadata to DocStrctType.
        currenMdType.setAllLanguages(allLanguages);

        return currenMdType;
    }

    /***************************************************************************
     * @param theMetadataTypeNode
     * @return
     **************************************************************************/
    public MetadataGroupType parseMetadataGroup(Node theMetadataGroupNode) {

        NodeList allchildren;
        // NamedNodeMap containing all attributes.
        NamedNodeMap attributeNodelist;
        // Node containing a single Attribute.
        Node attributeNode;
        // Single node from allchildren nodeList.
        Node currentNode;

        String languageName;
        String languageValue;
        HashMap<String, String> allLanguages = new HashMap<String, String>();

        MetadataGroupType currenGroup = new MetadataGroupType();

        String mdtypeName;
        allchildren = theMetadataGroupNode.getChildNodes(); // get allchildren
        for (int i = 0; i < allchildren.getLength(); i++) {
            currentNode = allchildren.item(i);

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (currentNode.getNodeName().equals("Name")) {
                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            LOGGER.error("Syntax Error reading config for MetadataGroup! Expected a text node under <Name> attribute at '"
                                    + theMetadataGroupNode.getNodeName() + "'. <Name> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for MetadataType unknown; Error Code: p002! Expected a text node under <Name> element at '"
                                    + theMetadataGroupNode.getNodeName() + "'");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        currenGroup.setName(textnode.getNodeValue());
                    }
                }

                if (currentNode.getNodeName().equals("metadata")) {

                    mdtypeName = "";

                    // Get value; value is always a text node.
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Error reading config for DocStrctType '" + currenGroup.getName() + "'! Node is not of type text");
                            // No text node available; maybe it's another
                            // element etc. anyhow: an error.
                            return null;
                        }
                        mdtypeName = textnode.getNodeValue();
                    }

                    if (mdtypeName == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    // MetadataType newMDType=new MetadataType();
                    // newMDType.setName(mdtype_name);
                    MetadataType newMdType = getMetadataTypeByName(mdtypeName);
                    if (newMdType == null) {
                        LOGGER.error("Error reading config for DocStrctType '" + currenGroup.getName() + "'! MetadataType '" + mdtypeName
                                + "' is unknown");
                        return null;
                    }
                    currenGroup.addMetadataType(newMdType);
                }

                if (currentNode.getNodeName().equals("language")) {
                    attributeNodelist = currentNode.getAttributes();
                    attributeNode = attributeNodelist.getNamedItem("name");
                    languageName = attributeNode.getNodeValue();
                    // Get value; value is always a text node.
                    languageValue = "";
                    NodeList textnodes = currentNode.getChildNodes();
                    if (textnodes != null) {
                        Node textnode = textnodes.item(0);
                        if (textnode == null) {
                            LOGGER.error("Syntax Error reading config for MetadataType " + currenGroup.getName()
                                    + "; Error Code: p001b! Expected a text node under <language> attribute at '"
                                    + theMetadataGroupNode.getNodeName() + "'. <language> must not be empty!");
                            return null;
                        } else if (textnode.getNodeType() != Node.TEXT_NODE) {
                            LOGGER.error("Syntax Error reading config for MetadataType " + currenGroup.getName()
                                    + "; Error Code: p001! Wrong node type under <language> attribute - a text node was expected at "
                                    + theMetadataGroupNode.getNodeName());
                            return null;
                        }
                        languageValue = textnode.getNodeValue();
                    }
                    if (languageName == null || languageValue == null) {
                        // Language name or the value (term) wasn't set.
                        continue;
                    }
                    allLanguages.put(languageName, languageValue);
                }
            }
        }

        // Add allLanguages and all Metadata to DocStrctType.
        currenGroup.setAllLanguages(allLanguages);

        return currenGroup;
    }

    /***************************************************************************
     * <p>
     * Checks, if MetadataType is allowed for given DocStrctType returns the DocStructType, otherwise null.
     * </p>
     * 
     * @param theName
     * @return
     **************************************************************************/
    public DocStructType getDocStrctTypeByName(String theName) {

        for (DocStructType currentDocStructType : this.allDocStrctTypes) {
            if (currentDocStructType.getName().equals(theName)) {
                return currentDocStructType;
            }
        }

        return null;
    }

    /**************************************************************************
     * <p>
     * Gets all anchor DocStrctTypes defined in the Prefs.
     * </p>
     * 
     * @return A List of all anchor DocStructTypes defined in the Prefs if some are existing, an empty list otherwise.
     **************************************************************************/
    public List<DocStructType> getAllAnchorDocStructTypes() {

        List<DocStructType> result = new LinkedList<DocStructType>();

        // Get all DocStructTypes.
        List<DocStructType> allTypes = this.getAllDocStructTypes();
        if (allTypes != null) {
            // Iterate...
            for (DocStructType dst : allTypes) {
                // ...and add to result list if anchor DocStruct.
				if (dst.getAnchorClass() != null) {
                    result.add(dst);
                }
            }
        }

        return result;
    }

    /***************************************************************************
     * @param name
     * @param inLanguage
     * @return
     **************************************************************************/
    public DocStructType getDocStrctTypeByName(String name, String inLanguage) {

        DocStructType currentDocStrctType;
        HashMap<String, String> allLanguages;
        String checklanguage;
        String checklanguagevalue = "";

        // Get dstype first.
        Iterator<DocStructType> it = this.allDocStrctTypes.iterator();
        while (it.hasNext()) {
            currentDocStrctType = it.next();
            // Get all languages.
            allLanguages = currentDocStrctType.getAllLanguages();

            Iterator<Map.Entry<String, String>> itlang = allLanguages.entrySet().iterator();
            // Find language "inLanguage".
            while (itlang.hasNext()) {
                Map.Entry<String, String> entry = itlang.next();
                checklanguage = entry.getKey();
                checklanguagevalue = entry.getValue();
                if (checklanguage.equals(inLanguage)) {
                    break;
                }
            }

            if (!checklanguagevalue.equals("") && checklanguagevalue.equals(name)) {
                // Found DocStrctType.
                return currentDocStrctType;
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Provides access for FileFormat implementations to read their preferences. The preferences of FileFormats are included in the global preference
     * file (in section formats). This method just retrieves the Node element from the DOM tree, which contains the whole configuration.
     * </p>
     * <p>
     * It is up to the FileFormat implementation to parse this configuration.
     * </p>
     * 
     * @param in name of fileformat (Excel, RDF, METS....), which is the name of the node.
     * @return a DOM Node objects, which contains the whole configuration for this requested FileFormat.
     **************************************************************************/
    public Node getPreferenceNode(String in) {

        if (!this.allFormats.containsKey(in)) {
            // Format not available.
            return null;
        }
        Node result = this.allFormats.get(in);

        return result;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public List<MetadataType> getAllMetadataTypes() {
        return this.allMetadataTypes;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public List<DocStructType> getAllDocStructTypes() {
        return this.allDocStrctTypes;
    }

    /***************************************************************************
     * @deprecated
     * @param inList
     * @return
     **************************************************************************/
    @Deprecated
    public boolean SetAllMetadataTypes(List<MetadataType> inList) {
        return setAllMetadataTypes(inList);
    }

    /***************************************************************************
     * @param inList
     * @return
     **************************************************************************/
    public boolean setAllMetadataTypes(List<MetadataType> inList) {
        this.allMetadataTypes = inList;

        return true;
    }

    /***************************************************************************
     * @deprecetad
     * @param inList
     * @return
     **************************************************************************/
    @Deprecated
    public boolean SetAllDocStructTypes(List<DocStructType> inList) {
        return setAllDocStructTypes(inList);
    }

    /***************************************************************************
     * @param inList
     * @return
     **************************************************************************/
    public boolean setAllDocStructTypes(List<DocStructType> inList) {
        this.allDocStrctTypes = inList;

        return true;
    }

    /***************************************************************************
     * @deprecated
     * @param inType
     * @return
     **************************************************************************/
    @Deprecated
    public boolean AddMetadataType(MetadataType inType) {
        return addMetadataType(inType);
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addMetadataType(MetadataType inType) {

        MetadataType tempType;

        if (inType == null) {
            return false;
        }
        if (getMetadataTypeByName(inType.getName()) == null) {
            // still not available, so add ist
            this.allMetadataTypes.add(inType);
            return true;
        }

        tempType = getMetadataTypeByName(inType.getName());
        // Remove old.
        this.allMetadataTypes.remove(tempType);
        // Add new.
        this.allMetadataTypes.add(inType);

        return true;
    }

    /***************************************************************************
     * <p>
     * Returns all metadataType instances which represents a person. These metadata types have a "type"-attribute with the value "person".
     * </p>
     * 
     * @return List containing MetadataType attributes
     **************************************************************************/
    public List<MetadataType> getAllPersonTypes() {

        MetadataType currentMdType;
        List<MetadataType> allPersons = new LinkedList<MetadataType>();

        Iterator<MetadataType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            currentMdType = it.next();
            if (currentMdType.getIsPerson()) {
                allPersons.add(currentMdType);
            }
        }

        return allPersons;
    }

    /***************************************************************************
     * <p>
     * Needs string as parameter and returns MetadataType object with this name.
     * </p>
     * 
     * @param name
     * @return
     **************************************************************************/
    public MetadataType getMetadataTypeByName(String name) {

        MetadataType currentMdType;
        String checkname;

        // Get dstype first.
        Iterator<MetadataType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            currentMdType = it.next();
            checkname = currentMdType.getName();

            if (checkname.equals(name)) {
                // Found MetadataType.
                return currentMdType;
            }
        }

        return null;
    }
    
    
    /***************************************************************************
     * <p>
     * Needs string as parameter and returns MetadataGroup object with this name.
     * </p>
     * 
     * @param name
     * @return
     **************************************************************************/
    public MetadataGroupType getMetadataGroupTypeByName(String name) {

        MetadataGroupType currentMdGroup;
        String checkname;

        // Get dstype first.
        Iterator<MetadataGroupType> it = this.allMetadataGroupTypes.iterator();
        while (it.hasNext()) {
            currentMdGroup = it.next();
            checkname = currentMdGroup.getName();

            if (checkname.equals(name)) {
                // Found MetadataType.
                return currentMdGroup;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param name
     * @param inLanguage
     * @return
     **************************************************************************/
    public MetadataType getMetadataTypeByName(String name, String inLanguage) {

        MetadataType currentMdType;
        HashMap<String, String> allLanguages;
        String checklanguage;
        String checklanguagevalue = "";

        // Get dstype first.
        Iterator<MetadataType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            currentMdType = it.next();

            // Get all languages.
            allLanguages = currentMdType.getAllLanguages();
            if (allLanguages == null) {
                if (!(currentMdType.getName().substring(0, 1).equals(HIDDEN_METADATA_CHAR))) {
                    LOGGER.debug("MetadataType without language definition:" + currentMdType.getName());
                }

                // No languages available for this MetadataType.
                continue;
            }

            Iterator<Map.Entry<String, String>> itlang = allLanguages.entrySet().iterator();
            // Find language "inLanguage".
            while (itlang.hasNext()) {
                Map.Entry<String, String> entry = itlang.next();
                checklanguage = entry.getKey();
                checklanguagevalue = entry.getValue();
                if (checklanguage.equals(inLanguage)) {
                    break;
                }
            }

            if (!checklanguagevalue.equals("") && checklanguagevalue.equals(name)) {
                // Found MetadataType.
                return currentMdType;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addMetadataGroup(MetadataGroupType inGroup) {

        MetadataGroupType tempType;

        if (inGroup == null) {
            return false;
        }
        if (getMetadataGroupTypeByName(inGroup.getName()) == null) {
            // still not available, so add ist
            this.allMetadataGroupTypes.add(inGroup);
            return true;
        }

        tempType = getMetadataGroupTypeByName(inGroup.getName());
        // Remove old.
        this.allMetadataGroupTypes.remove(tempType);
        // Add new.
        this.allMetadataGroupTypes.add(inGroup);

        return true;
    }
}
