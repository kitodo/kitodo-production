package ugh.fileformats.mets;

/*******************************************************************************
 * ugh.fileformats.mets / MetsModsImportExport.java
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

import gov.loc.mods.v3.ModsDocument;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ugh.dl.AmdSec;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Md;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.ImportException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * @author Stefan Funk
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-26
 * @since 2009-05-09
 * 
 * 
 *        CHANGELOG
 *        
 *        26.06.2014 --- Ronge --- Get anchor classes & get only real successors
 *        
 *        23.06.2014 --- Ronge --- Create ORDERLABEL attribute on export & add getter for meta data
 *        
 *        18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *        05.05.2010 --- Funk --- Added displayName check at displayName creation time. --- Added some DPD-407 debugging outputs (commented out).
 * 
 *        10.03.2010 --- Funk --- Added ValueRegExps to AnchorIdentifier. --- References to anchor is written only once now, and not for every
 *        identifier mapping in the prefs.
 * 
 *        03.03.2010 --- Funk --- ORDERLABEL uncounted things corrected. No additional tags are written anymore.
 * 
 *        24.02.2010 --- Funk --- ORDERLABEL only is filled with data, if a metadata value is existing. "uncounted" pages are not written as
 *        ORDERLABEL anymore. --- Empty nodes (nodes without value) in RegExp conditions are NOT created anymore.
 * 
 *        15.02.2010 --- Funk --- Logging version information now. --- Added the RegExp things for the AMD section to the documentation.
 * 
 * 
 *        12.02.2010 --- Funk --- Added RegExp support for amdsec setters.
 * 
 *        22.01.2010 --- Funk --- findbugs improvements.
 * 
 *        18.01.2010 --- Funk --- Adapted class to changed DocStruct.getAllMetadataByType().
 * 
 *        22.12.2009 --- Funk --- Grouping of MODS elements added.
 * 
 *        08.12.2009 --- Funk --- Moved the trim() calls some lines down to avoid NPEs.
 * 
 *        04.12.2009 --- Funk --- Added trim() to all METS prefs values' getTextNodeValue() calls.
 * 
 *        21.11.2009 --- Funk --- Added ValueCondition and ValueRegExp to MODS mapping.
 * 
 *        06.11.2009 --- Funk --- Beautified code for Sonar ranking improvement!
 * 
 *        26.10.2009 --- Funk --- Removed the constructor without Prefs object. We really need that Prefs thing!
 * 
 *        09.10.2009 --- Funk --- Changed the authority for the PND ID to "gbv".
 * 
 *        06.10.2009 --- Funk --- Corrected some not-conform-to-rules variable names.
 * 
 *        05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *        24.09.2009 --- Funk --- Refactored all the Exception things.
 * 
 *        21.09.2009 --- Funk --- Moved the class readPrefs from MetsMods, and put it into MetsModeImportExport. It is only needed here.
 * 
 *        11.09.2009 --- Funk --- Using more String finals now.
 * 
 *        04.09.2009 --- Funk --- Catch all the NULLs in the METS setters. If a value is "", do not create a METS tag, or throw an exception if
 *        necesarry.
 * 
 *        20.08.2009 --- Funk --- Removed the xPathAnchorReference attribute from this class. We must use the one from MetsMods!
 * 
 *        28.07.2009 --- Funk --- Added PURLs as CONTENTIDS to logical structmap div.
 * 
 *        24.07.2009 --- Funk --- Added version string.
 * 
 *        22.07.2009 --- Funk --- Added HTML tags to JavaDOC.
 * 
 *        26.06.2009 --- Funk --- ADMSEC is written only here now.
 * 
 *        22.06.2009 --- Funk --- <physicalDescription><digitalOrigin> added if a _digitalOrigin metadata is existing in the prefs and a
 *        CatalogIDDigital is set.
 * 
 *        19.06.2009 -- Funk --- writePhysDMD integriert, nun wird auch MODS in der PhysDmd geschrieben.
 * 
 *        18.06.2009 --- Funk --- Generalised the WriteLogDMD() method, using WriteMODS() now.
 * 
 *        09.06.2009 --- Funk --- Added authority and ID attributes to the person name MODS part (yet still hard coded).
 * 
 *        30.04.2009 --- Funk --- Added setPurlFromCatalogIDDigital() to put in Purls from PPNs.
 * 
 *        22.04.2009 --- Funk --- Corrected some logging things. --- Added methods writePhysDivs and writeLogDivs, here the METS DocStructTypes must
 *        be mapped from the prefs.
 * 
 *        31.03.2009 --- Funk --- Added the method checkForAnchorReference, because we need another one here.
 * 
 *        27.03.2009 --- Funk --- Class created.
 * 
 ******************************************************************************/

public class MetsModsImportExport extends ugh.fileformats.mets.MetsMods {

    /***************************************************************************
     * VERSION STRING
     **************************************************************************/

    private static final String VERSION = "1.9-20100505";

    /***************************************************************************
     * STATIC FINALS
     **************************************************************************/

	/**
	 * For each meta data element of this type that is associated with a
	 * DocStruct element of the logical structure tree of a digital document, a
	 * METS pointer element will be created during export.
	 */
	public static final String CREATE_MPTR_ELEMENT_TYPE = "MetsPointerURL";

	/**
	 * If there is a meta data element of this type associated with a DocStruct
	 * element of the logical structure tree of a digital document, a LABEL
	 * attribute will be attached to the logical div element during export which
	 * will have assigned the value assigned to the last meta data element of
	 * this type associated with the DocStruct element.
	 */
	public static final String CREATE_LABEL_ATTRIBUTE_TYPE = MetsMods.METS_PREFS_LABEL_METADATA_STRING;

	/**
	 * If there is a meta data element of this type associated with a DocStruct
	 * element of the logical structure tree of a digital document, an
	 * ORDERLABEL attribute will be attached to the logical div element during
	 * export which will have assigned the value assigned to the last meta data
	 * element of this type associated with the DocStruct element.
	 */
	public static final String CREATE_ORDERLABEL_ATTRIBUTE_TYPE = MetsMods.METS_PREFS_ORDERLABEL_METADATA_STRING;

    protected static final String METS_PREFS_XPATH_STRING = "XPath";
    protected static final String METS_PREFS_WRITEXPATH_STRING = "WriteXPath";
    protected static final String METS_PREFS_FIRSTNAMEXPATH_STRING = "FirstnameXPath";
    protected static final String METS_PREFS_LASTNAMEXPATH_STRING = "LastnameXPath";
    protected static final String METS_PREFS_AFFILIATIONXPATH_STRING = "AffiliationXPath";
    protected static final String METS_PREFS_DISPLAYNAMEXPATH_STRING = "DisplayNameXPath";
    protected static final String METS_PREFS_PERSONTYPEXPATH_STRING = "PersonTypeXPath";
    protected static final String METS_PREFS_AUTHORITYFILEIDXPATH_STRING = "AuthorityFileIDXPath";
    protected static final String METS_PREFS_IDENTIFIERXPATH_STRING = "IdentifierXPath";
    protected static final String METS_PREFS_IDENTIFIERTYPEXPATH_STRING = "IdentifierTypeXPath";
    protected static final String METS_PREFS_READMODSNAME_STRING = "ReadModsName";
    protected static final String METS_PREFS_WRITEMODSNAME_STRING = "WriteModsName";
    protected static final String METS_PREFS_MODSAUTHORITY_STRING = "MODSAuthority";
    protected static final String METS_PREFS_MODSENCODING_STRING = "MODSEncoding";
    protected static final String METS_PREFS_MODSID_STRING = "MODSID";
    protected static final String METS_PREFS_MODSLANG_STRING = "MODSLang";
    protected static final String METS_PREFS_MODSSCRIPT_STRING = "MODSScript";
    protected static final String METS_PREFS_MODSTRANSLITERATION_STRING = "MODSTransliteration";
    protected static final String METS_PREFS_MODSTYPE_STRING = "MODSType";
    protected static final String METS_PREFS_MODSXMLLANG_STRING = "MODSXMLLang";
    protected static final String METS_PREFS_VALUECONDITION_STRING = "ValueCondition";
    protected static final String METS_PREFS_VALUEREGEXP_STRING = "ValueRegExp";
    protected static final String METS_PREFS_DATABASE_SOURCE = "DatabaseXpath";
    protected static final String METS_PREFS_DATABASE_IDENTIFIER = "IdentifierXpath";

    protected static final String METS_RIGHTS_OWNER_STRING = "rightsOwner";
    protected static final String METS_RIGHTS_OWNER_LOGO_STRING = "rightsOwnerLogo";
    protected static final String METS_RIGHTS_OWNER_SITE_STRING = "rightsOwnerSiteUrl";
    protected static final String METS_RIGHTS_OWNER_CONTACT_STRING = "rightsOwnerContact";
    protected static final String METS_DIGIPROV_REFERENCE_STRING = "digiprovReference";
    protected static final String METS_DIGIPROV_PRESENTATION_STRING = "digiprovPresentation";

    /***************************************************************************
     * INSTANCE VARIABLES
     **************************************************************************/

    // Set METS Rights, Digiprov, PURLs, and CONTENTIDS.
    private String rightsOwner = "";
    private String rightsOwnerLogo = "";
    private String rightsOwnerSiteURL = "";
    private String rightsOwnerContact = "";
    private String digiprovReference = "";
    private String digiprovPresentation = "";
    private String digiprovReferenceAnchor = "";
    private String digiprovPresentationAnchor = "";
    private String purlUrl = "";
    private String contentIDs = "";

    /***************************************************************************
     * CONSTRUCTORS
     **************************************************************************/

    /***************************************************************************
     * @param inPrefs
     * @throws PreferencesException
     **************************************************************************/
    public MetsModsImportExport(Prefs inPrefs) throws PreferencesException {
        super(inPrefs);

        LOGGER.info(this.getClass().getName() + " " + getVersion());
    }

    /***************************************************************************
     * WHAT THE OBJECT DOES
     **************************************************************************/

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#Update(java.lang.String)
     */
    @Override
    public boolean update(String filename) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#SetDigitalDocument(ugh.dl.DigitalDocument)
     */
    @Override
    public boolean setDigitalDocument(DigitalDocument inDoc) {
        this.digdoc = inDoc;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.dl.Fileformat#read(java.lang.String)
     */
    @Override
    public boolean read(String filename) throws ReadException {
        // The reading of the METS is already existing in the method
        // parseMODSForLogicalDOM for import of external METS/MODS files
        // configured by the METS section in the prefs.

        return super.read(filename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#WriteMODS(ugh.dl.DocStruct, org.w3c.dom.Node, org.w3c.dom.Document)
     */
    @Override
    protected void writeLogModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc) throws PreferencesException, DOMException,
            WriteException {

        // Prepare lists of all metadata and all persons, that will monitor if
        // some metadata are NOT mapped to MODS.
        List notMappedMetadataAndPersons = new LinkedList();
        if (inStruct.getAllMetadata() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadata());
        }
        // Add persons to list.
        if (inStruct.getAllPersons() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllPersons());
        }

        // Add groups to list
        if (inStruct.getAllMetadataGroups() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadataGroups());
        }

        // Check if we already have written the anchor reference.
        boolean referenceWritten = false;

        // Iterate over all metadata (and person) objects, ordered by the
        // appearance of the metadata in the METS section of the prefs - the
        // MatchingMetadataObjects.
        for (MatchingMetadataObject mmo : this.modsNamesMD) {
            if (!mmo.getInternalName().equalsIgnoreCase(METS_URN_NAME)) {

                //
                // Check if we need a reference to an anchor.
                //

                // Get parent first.
                DocStruct parentStruct = inStruct.getParent();

                // Create a reference only, if parentStruct exists, the
                // MMO's internal name is mentioned in the prefs, and we have not
                // yet written the reference.
                if (parentStruct != null && mmo.getInternalName().equalsIgnoreCase(this.anchorIdentifierMetadataType) && !referenceWritten) {
                    DocStructType parentDst = parentStruct.getType();

                    // Check, if the parent is an anchor.
					if (parentDst.getAnchorClass() != null && this.xPathAnchorReference != null) {

                        // Get identifier(s) of parent.
                        MetadataType identifierType = this.myPreferences.getMetadataTypeByName(this.anchorIdentifierMetadataType);
                        if (identifierType == null) {
                            String message =
                                    "No Metadata of type '" + this.anchorIdentifierMetadataType + "' found to create the anchor in MODS record";
                            LOGGER.error(message);
                            throw new PreferencesException(message);
                        }

                        // Go throught all the identifier metadata of the
                        // parent struct and look for the XPath anchor
                        // reference.
                        for (Metadata md : parentStruct.getAllMetadataByType(identifierType)) {
                            // Create the node according to the prefs' METS/MODS
                            // section's XQuery.
                            Node createdNode = createNode(this.xPathAnchorReference, dommodsnode, domDoc);

                            if (createdNode != null) {
                                // Get the value of the node.
                                String metadataValue = md.getValue();
                                // If existing, process the valueRegExp.
                                if (this.valueRegExpAnchorReference != null && !this.valueRegExpAnchorReference.equals("")) {
                                    Perl5Util perlUtil = new Perl5Util();
                                    String oldMetadataValue = metadataValue;
                                    metadataValue = new String(perlUtil.substitute(this.valueRegExpAnchorReference, metadataValue));
                                    LOGGER.info("Regular expression '" + this.valueRegExpAnchorReference + "' changed value of Anchor Identifier '"
                                            + md.getType().getName() + "' from '" + oldMetadataValue + "' to '" + metadataValue + "'");
                                }
                                // Node was created successfully, now add
                                // value to it.
                                // metadataValue = metadataValue.replace("< ", "&lt; ").replace("> ", "&gt; ").replace("\"", "&quot;");
                                Node valueNode = domDoc.createTextNode(metadataValue);
                                createdNode.appendChild(valueNode);

                                // The node was successfully written, remove the
                                // metadata object from the notMappedMetadata
                                // list.
                                notMappedMetadataAndPersons.remove(md);

                                referenceWritten = true;
                            }
                        }
                    }
                }

                //
                // Handle Metadata.
                //

                if (inStruct.getAllMetadata() != null) {
                    // Only if the metadata type does exist in the current
                    // DocStruct...
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.
                    if (inStruct.hasMetadataType(mdt)) {
                        for (Metadata m : inStruct.getAllMetadataByType(mdt)) {
                            // Only if the metadata has a value, add it to the MODS!
                            if (m.getValue() != null && !m.getValue().equals("")) {

                                // Create the node according to the prefs' METS/MODS
                                // section's WriteXPath. The Query contains the path
                                // which is used for creating new elements.
                                if (mmo.getWriteXPath() != null) {
                                    writeSingleModsMetadata(mmo.getWriteXPath(), mmo, m, dommodsnode, domDoc);

                                    // The node was successfully written! Remove the
                                    // metadata object from the checklist.
                                    notMappedMetadataAndPersons.remove(m);
                                }
                            }
                        }
                    }
                }

                // handle groups

                if (inStruct.getAllMetadataGroups() != null) {
                    // Only if the metadata type does exist in the current DocStruct...
                    MetadataGroupType mdt = this.myPreferences.getMetadataGroupTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.

                    if (inStruct.hasMetadataGroupType(mdt)) {
                        for (MetadataGroup group : inStruct.getAllMetadataGroupsByType(mdt)) {
                            boolean isEmpty = true;
                            for (Metadata md : group.getMetadataList()) {
                                if (md.getValue() != null && md.getValue().length() > 0) {
                                    isEmpty = false;
                                    break;
                                }
                            }
                            // only write groups with values

                            if (!isEmpty) {
                                writeSingleModsGroup(mmo, group, dommodsnode, domDoc);

                                // The node was successfully written! Remove the
                                // metadata object from the checklist.
                                notMappedMetadataAndPersons.remove(group);

                            }

                        }
                    }
                }

                //
                // Handle Persons.
                //

                if (inStruct.getAllPersons() != null) {
                    // Only if the person type does exist in the current
                    // DocStruct...
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());

                    // ... go throught all the available metadata of that type.
                    if (inStruct.hasMetadataType(mdt) && inStruct.getAllPersonsByType(mdt) != null) {

                        for (Person p : inStruct.getAllPersonsByType(mdt)) {
                            // Only if the person has a firstname or a lastname, add
                            // it to the MODS!
                            if (((p.getFirstname() != null && !p.getFirstname().equals("")) || (p.getLastname() != null && !p.getLastname()
                                    .equals("")))) {

                                // Create the node according to the prefs' METS/MODS
                                // section's WriteXPath. The Query contains the path
                                // which is used for creating new elements.
                                if (mmo.getWriteXPath() != null) {
                                    writeSingleModsPerson(mmo.getWriteXPath(), mmo, p, dommodsnode, domDoc);

                                    // The node was successfully written! Remove the
                                    // person object from the checklist.
                                    notMappedMetadataAndPersons.remove(p);
                                }
                            }
                        }
                    }
                }
            }

            // Check for not mapped metadata and persons.
            if (!notMappedMetadataAndPersons.isEmpty()) {
                LOGGER.warn(getMappingWarning(inStruct.getType(), notMappedMetadataAndPersons));
            }
        }
        dirtyReplaceGroupingTagNameHack(dommodsnode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writePhysModsSection(ugh.dl.DocStruct, org.w3c.dom.Node, org.w3c.dom.Document, org.w3c.dom.Element)
     */
    @Override
    protected void writePhysModsSection(DocStruct inStruct, Node dommodsnode, Document domDoc, Element divElement) throws PreferencesException {

        // Prepare lists of all metadata and all persons, that will monitor if
        // some metadata are NOT mapped to MODS.
        List notMappedMetadataAndPersons = inStruct.getAllMetadata();
        if (notMappedMetadataAndPersons == null) {
            notMappedMetadataAndPersons = new LinkedList();
        }
        // Add persons to list.
        if (inStruct.getAllPersons() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllPersons());
        }

        // Add groups to list
        if (inStruct.getAllMetadataGroups() != null) {
            notMappedMetadataAndPersons.addAll(inStruct.getAllMetadataGroups());
        }

        // Iterate over all metadata, ordered by the appearance of the
        // metadata in the METS section of the prefs.
        for (MatchingMetadataObject mmo : this.modsNamesMD) {

            // Get current metdata object list.
            List<Metadata> currentMdList = new LinkedList<Metadata>();
            if (inStruct.getAllMetadata() != null) {
                for (Metadata m : inStruct.getAllMetadata()) {
                    // Only if the metadata has a value AND its type is
                    // equal to the given metadata object, take it as
                    // current metadata.
                    if (m.getValue() != null
                            && !m.getValue().equals("")
                            && (m.getType().getName().equalsIgnoreCase(mmo.getInternalName())
                                    || m.getType().getName().equals(METADATA_PHYSICAL_PAGE_NUMBER)
                                    || m.getType().getName().equals(METADATA_LOGICAL_PAGE_NUMBER) || m.getType().getName().equals(METS_URN_NAME))) {
                        currentMdList.add(m);
                    }
                }
            }

            // Handle Metadata.
            for (Metadata currentMd : currentMdList) {
                // Query contains the path which is used for creating new
                // elements. If writeXQuery is not existing, try to get
                // readXQuery.
                String xquery = mmo.getWriteXPath();
                if (xquery == null) {
                    xquery = mmo.getReadXQuery();
                }

                // Write physical page number into div.
                if (currentMd.getType().getName().equals(METADATA_PHYSICAL_PAGE_NUMBER)) {
                    divElement.setAttribute(METS_ORDER_STRING, currentMd.getValue());

                    notMappedMetadataAndPersons.remove(currentMd);
                } else if (currentMd.getType().getName().equals(METS_URN_NAME)) {
                    divElement.setAttribute("CONTENTIDS", currentMd.getValue());
                    notMappedMetadataAndPersons.remove(currentMd);
                }

                // Write logical page number into div, if current metadata value
                // is not METADATA_PAGE_UNCOUNTED_VALUE.
                else if (currentMd.getType().getName().equals(METADATA_LOGICAL_PAGE_NUMBER)) {
                    if (!currentMd.getValue().equals(METADATA_PAGE_UNCOUNTED_VALUE)) {
                        divElement.setAttribute(METS_ORDERLABEL_STRING, currentMd.getValue());
                    } else {
                        divElement.setAttribute(METS_ORDERLABEL_STRING, " - ");
                    }

                    notMappedMetadataAndPersons.remove(currentMd);
                } else {
                    // Create the node according to the prefs' METS/MODS
                    // section's XQuery.
                    if (xquery != null) {
                        // Write other metadata into MODS the section.
                        writeSingleModsMetadata(xquery, mmo, currentMd, dommodsnode, domDoc);

                        // The node was successfully written! Remove the
                        // metadata object from the notMappedMetadata list.
                        notMappedMetadataAndPersons.remove(currentMd);
                    }
                }
            }
            // handle groups

            if (inStruct.getAllMetadataGroups() != null) {
                // Only if the metadata type does exist in the current DocStruct...
                MetadataGroupType mdt = this.myPreferences.getMetadataGroupTypeByName(mmo.getInternalName());

                // ... go throught all the available metadata of that type.

                if (inStruct.hasMetadataGroupType(mdt)) {
                    for (MetadataGroup group : inStruct.getAllMetadataGroupsByType(mdt)) {
                        boolean isEmpty = true;
                        for (Metadata md : group.getMetadataList()) {
                            if (md.getValue() != null && md.getValue().length() > 0) {
                                isEmpty = false;
                                break;
                            }
                        }
                        // only write groups with values

                        if (!isEmpty) {
                            writeSingleModsGroup(mmo, group, dommodsnode, domDoc);

                            // The node was successfully written! Remove the
                            // metadata object from the checklist.
                            notMappedMetadataAndPersons.remove(group);

                        }

                    }
                }
            }
            // Get the current person object list.
            List<Person> currentPerList = new LinkedList<Person>();
            if (inStruct.getAllPersons() != null) {
                for (Person p : inStruct.getAllPersons()) {
                    // Only if the person has a first or last name AND its
                    // type is equal to the given metadata object, take it
                    // as current person.
                    if (((p.getFirstname() != null && !p.getFirstname().equals("")) || (p.getLastname() != null && !p.getLastname().equals("")))
                            && p.getType().getName().equalsIgnoreCase(mmo.getInternalName())) {
                        currentPerList.add(p);
                    }
                }
            }

            // Handle Persons.
            for (Person currentPer : currentPerList) {
                // Query contains the path which is used for
                // creating new elements.
                String xquery = mmo.getWriteXPath();
                if (xquery == null) {
                    xquery = mmo.getReadXQuery();
                }

                // Create the node according to the prefs' METS/MODS
                // section's XQuery.
                if (xquery != null) {
                    writeSingleModsPerson(xquery, mmo, currentPer, dommodsnode, domDoc);

                    // The node was successfully written, remove the
                    // person object from the notMappedPersons list.
                    notMappedMetadataAndPersons.remove(currentPer);
                }
            }
        }

        // Check for not mapped metadata and persons.
        if (!notMappedMetadataAndPersons.isEmpty()) {
            LOGGER.warn(getMappingWarning(inStruct.getType(), notMappedMetadataAndPersons));
        }
        dirtyReplaceGroupingTagNameHack(dommodsnode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#parseMODS(org.w3c.dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected void parseMODS(Node inMods, DocStruct inStruct) throws ReadException, ClassNotFoundException, InstantiationException,
            IllegalAccessException {

        // Document in DOM tree which represents the MODS.
        Document modsdocument = null;

        DOMImplementationRegistry registry = null;
        registry = DOMImplementationRegistry.newInstance();

        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

        // Test, if the needed DOMImplementation (DOM 3!) is available, else
        // throw Exception. We are using Xerxes here!
        if (impl == null) {
            String message =
                    "There is NO implementation of DOM3 in your ClassPath! We are using Xerxes here, I have no idea why that's not available!";
            LOGGER.error(message);
            throw new UnsupportedOperationException(message);
        }
        LSSerializer writer = impl.createLSSerializer();

        // Get string for MODS.
        String modsstr = writer.writeToString(inMods);

        // Parse MODS section; create a DOM tree just for the MODS from the
        // string new document builder instance.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Do not validate xml file (for we want to store unfinished files, too)
        factory.setValidating(false);
        // Namespace does not matter.
        factory.setNamespaceAware(true);

        Reader r = new StringReader(modsstr);

        // Read file and parse it.
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(r);

            modsdocument = builder.parse(is);
        } catch (SAXParseException e) {
            // Error generated by the parser.
            String message = "Parse error on line: " + e.getLineNumber() + ", uri: " + e.getSystemId();
            LOGGER.error(message, e);
            throw new ReadException(message, e);
        } catch (SAXException e) {
            // Error generated during parsing.
            String message = "Exception while parsing METS file; can't create DOM tree!";
            LOGGER.error(message, e);
            throw new ReadException(message, e);
        } catch (ParserConfigurationException e) {
            // Parser with specified options can't be built.
            String message = "XML parser not configured correctly!";
            LOGGER.error(message, e);
            throw new ReadException(message, e);
        } catch (IOException e) {
            String message = "Exception while parsing METS file; can't create DOM tree!";
            LOGGER.error(message, e);
            throw new ReadException(message, e);
        }

        LOGGER.trace("\n" + LINE + "\nMODS\n" + LINE + "\n" + modsstr + "\n" + LINE);

        // Result of XQuery.
        Object xqueryresult = null;

        // Create XQuery.
        XPathFactory xpathfactory = XPathFactory.newInstance();

        // New namespace context.
        PersonalNamespaceContext pnc = new PersonalNamespaceContext();
        pnc.setNamespaceHash(this.namespaces);
        XPath xpath = xpathfactory.newXPath();
        xpath.setNamespaceContext(pnc);

        // Get the first element; this is where we start with out XPATH.
        Node startingNode = null;
        NodeList nl = modsdocument.getChildNodes();
        if (nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == ELEMENT_NODE) {
                    startingNode = n;
                }
            }
        }

        //
        // Iterate over all MatchingMetadataObjects and try to find the required
        // MODS information for external METS import.
        //
        for (MatchingMetadataObject mmo : this.modsNamesMD) {

            // No internal name available: next one.
            if (mmo.getInternalName() == null) {
                continue;
            }

            String queryExpression = mmo.getReadXQuery();

            // No query expression in: next one.
            if (queryExpression == null) {
                continue;
            }

            // Delete the leading "." if there is one available.
            if (queryExpression.startsWith(".")) {
                queryExpression = queryExpression.substring(1, queryExpression.length());
            }

            // Carry out the XPATH query.
            try {
                XPathExpression expr = xpath.compile(queryExpression);
                xqueryresult = expr.evaluate(startingNode, XPathConstants.NODESET);
                LOGGER.debug("Query expression: " + queryExpression);
            } catch (XPathExpressionException e) {
                String message =
                        "Error while parsing MODS metadata: " + mmo.getInternalName() + "! Please check XPath '" + mmo.getReadXQuery() + "'!";
                LOGGER.error(message, e);
                throw new ReadException(message, e);
            }

            NodeList nodes = (NodeList) xqueryresult;

            // Get Nodes.
            //
            // Iterate over all nodes and get the text node which contains the
            // value of the element.
            Node node = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                // Get DOM Node.
                node = nodes.item(i);

                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int x = 0; x < nodelist.getLength(); x++) {
                    Node subnode = nodelist.item(x);
                    String value = null;
                    if (subnode.getNodeType() == TEXT_NODE) {
                        value = subnode.getNodeValue();
                    }

                    // Create Metadata.
                    MetadataType mdt = this.myPreferences.getMetadataTypeByName(mmo.getInternalName());
                    if (mdt == null) {
                        // No valid metadata type found.
                        String message =
                                "Can't find internal Metadata with name '" + mmo.getInternalName() + "' for MODS element '" + mmo.getReadModsName()
                                        + "'";
                        LOGGER.error(message);
                        throw new ImportException(message);
                    }

                    //
                    // Add Metadata to DocStruct.
                    //

                    // Handle Persons.
                    if (mdt.getIsPerson()) {
                        Person ps = null;
                        try {
                            ps = new Person(mdt);
                        } catch (MetadataTypeNotAllowedException e) {
                            // mdt is NOT null, we ensure this above!
                            e.printStackTrace();
                        }

                        ps.setRole(mdt.getName());

                        // It is supposed that the <name> element is selected,
                        // the following queries are just subqueries to the name
                        // element.

                        String[] firstnamevalue = null;
                        String[] lastnamevalue = null;
                        String[] affiliationvalue = null;
                        String[] authorityfileidvalue = null;
                        String[] authorityuri = null;
                        String[] authorityvalue = null;
                        String[] identifiervalue = null;
                        String[] identifiertypevalue = null;
                        String[] displaynamevalue = null;
                        String[] persontypevalue = null;

                        if (mmo.getFirstnameXQuery() != null) {
                            firstnamevalue = getValueForUnambigiousXQuery(node, mmo.getFirstnameXQuery());
                        }
                        if (mmo.getLastnameXQuery() != null) {
                            lastnamevalue = getValueForUnambigiousXQuery(node, mmo.getLastnameXQuery());
                        }
                        if (mmo.getAffiliationXQuery() != null) {
                            affiliationvalue = getValueForUnambigiousXQuery(node, mmo.getAffiliationXQuery());
                        }
                        if (mmo.getAuthorityIDXquery() != null) {
                            authorityfileidvalue = getValueForUnambigiousXQuery(node, mmo.getAuthorityIDXquery());
                        }
                        if (mmo.getAuthorityURIXquery() != null) {
                            authorityuri = getValueForUnambigiousXQuery(node, mmo.getAuthorityURIXquery());
                        }
                        if (mmo.getAuthorityValueXquery() != null) {
                            authorityvalue = getValueForUnambigiousXQuery(node, mmo.getAuthorityValueXquery());
                        }
                        if (mmo.getIdentifierXQuery() != null) {
                            identifiervalue = getValueForUnambigiousXQuery(node, mmo.getIdentifierXQuery());
                        }
                        if (mmo.getIdentifierTypeXQuery() != null) {
                            identifiertypevalue = getValueForUnambigiousXQuery(node, mmo.getIdentifierTypeXQuery());
                        }
                        if (mmo.getDisplayNameXQuery() != null) {
                            displaynamevalue = getValueForUnambigiousXQuery(node, mmo.getDisplayNameXQuery());
                        }
                        if (mmo.getPersontypeXQuery() != null) {
                            persontypevalue = getValueForUnambigiousXQuery(node, mmo.getDisplayNameXQuery());
                        }
               
                        

                        if (lastnamevalue != null) {
                            ps.setLastname(lastnamevalue[0]);
                        }
                        if (firstnamevalue != null) {
                            ps.setFirstname(firstnamevalue[0]);
                        }
                        if (affiliationvalue != null) {
                            ps.setAffiliation(affiliationvalue[0]);
                        }
                        if (authorityfileidvalue != null && authorityuri != null && authorityvalue != null) {
                            ps.setAutorityFile(authorityfileidvalue[0], authorityuri[0], authorityvalue[0]);
                        }
                        if (displaynamevalue != null) {
                            ps.setDisplayname(displaynamevalue[0]);
                        }
                        if (persontypevalue != null) {
                            ps.setPersontype(persontypevalue[0]);
                        }

                        try {
                            inStruct.addPerson(ps);
                        } catch (DocStructHasNoTypeException e) {
                            String message = "DocumentStructure for which metadata should be added has no type!";
                            LOGGER.error(message, e);
                            throw new ImportException(message, e);
                        } catch (MetadataTypeNotAllowedException e) {
                            String message =
                                    "Person '" + mdt.getName() + "' (" + ps.getDisplayname() + ") is not allowed as a child for '"
                                            + inStruct.getType().getName() + "' during MODS import!";
                            LOGGER.error(message, e);
                            // throw new ImportException(message, e);
                        }

                        // Get out of for loop; we don't need to iterate over
                        // all nodes.
                        break;
                    }

                    if (value == null) {
                        // Value not found, as the subnode is not a TEXT node
                        // continue iterating over subnodes.
                        continue;
                    }

                    // Handle metadata.
                    Metadata md = null;
                    try {
                        md = new Metadata(mdt);
                    } catch (MetadataTypeNotAllowedException e) {
                        // mdt is NOT null, we ensure this above!
                        e.printStackTrace();
                    }
                    if (node.getAttributes().getNamedItem("authority") != null && node.getAttributes().getNamedItem("authorityURI") != null && node.getAttributes().getNamedItem("valueURI") != null) {
                        String authority =  node.getAttributes().getNamedItem("authority").getNodeValue();
                        String authorityURI = node.getAttributes().getNamedItem("authorityURI").getNodeValue();
                        String valueURI = node.getAttributes().getNamedItem("valueURI").getNodeValue();
                        md.setAutorityFile(authority, authorityURI, valueURI);
                     }
                    md.setValue(value);

                    // TODO read groups

                    // Add the metadata.
                    try {
                        inStruct.addMetadata(md);

                        LOGGER.debug("Added metadata '" + mdt.getName() + "' to DocStruct '" + inStruct.getType().getName() + "' with value '"
                                + value + "'");
                    } catch (DocStructHasNoTypeException e) {
                        String message = "DocumentStructure for which metadata should be added, has no type!";
                        LOGGER.error(message, e);
                        throw new ImportException(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        String message =
                                "Metadata '" + mdt.getName() + "' (" + value + ") is not allowed as child for '" + inStruct.getType().getName()
                                        + "' during MODS import!";
                        LOGGER.error(message, e);
                        throw new ImportException(message, e);
                    }

                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#checkForAnchorReference(java.lang.String, java.lang.String)
     */
    @Override
	protected DocStruct checkForAnchorReference(String inMods, String filename, String topAnchorClassName)
			throws ReadException {

        ModsDocument modsDocument;
        DocStruct anchorDocStruct = null;

        // Check for MODS validity.
        try {
            modsDocument = ModsDocument.Factory.parse(inMods);
        } catch (XmlException e) {
            String message = "MODS section doesn't seem to contain valid MODS";
            LOGGER.error(message, e);
            throw new ImportException("Doesn't seem to contain valid MODS", e);
        }

        // Do query. Query syntax is like in the following example:
        // String queryExpression = "declare namespace
        // xq='http://xmlbeans.apache.org/samples/xquery/employees';" +
        // "$this/xq:employees/xq:employee/xq:phone[contains(., '(206)')]";
        String path = this.namespaceDeclarations.get(this.modsNamespacePrefix) + " $this/" + this.xPathAnchorReference;

        XmlOptions xo = new XmlOptions();
        xo.setUseDefaultNamespace();
        XmlObject[] objects = modsDocument.selectPath(path, xo);

        // Iterate over all objects; objects can be available more than once.
        for (int i = 0; i < objects.length; i++) {
            // Get DOM Node.
            Node node = objects[i].getDomNode();

            // Get child nodes to find the text node.
            NodeList nodelist = node.getChildNodes();

            for (int x = 0; x < nodelist.getLength(); x++) {
                Node subnode = nodelist.item(x);
                if (subnode.getNodeType() == TEXT_NODE) {
                    String identifierOfAnchor = subnode.getNodeValue();
                    // Found the reference to the anchor.
                    LOGGER.debug("Anchor's identifier: " + identifierOfAnchor);

                    // Try to read anchor from separate file.
					String anchorfilename = buildAnchorFilename(filename, topAnchorClassName);
                    if (!new File(anchorfilename).exists()) {
                        // File does not exists: no anchor available.
                        return null;
                    }
                    MetsMods anchorMets = null;

                    try {
                        anchorMets = new MetsMods(this.myPreferences);
                    } catch (PreferencesException e) {
                        String message = "Can't read Preferences for METS while reading the Anchor file";
                        LOGGER.error(message, e);
                        throw new ReadException(message, e);
                    }

                    try {
                        anchorMets.read(anchorfilename);
                    } catch (ReadException e) {
                        String message = "Can't read Anchor file, which must be in METS format as well";
                        LOGGER.error(message, e);
                        throw new ReadException(message, e);
                    }

                    // Get Digital Document and first logical DocStruct (which
                    // should be the only one).
                    DigitalDocument anchordd = anchorMets.getDigitalDocument();
                    DocStruct anchorStruct = anchordd.getLogicalDocStruct();
                    List<Metadata> allMetadata = anchorStruct.getAllMetadata();

                    // Iterate over all metadata and find an identifier with the
                    // value of identifierOfAnchor.
                    if (allMetadata != null) {
                        for (Metadata md : allMetadata) {
                            if (md.getValue() != null && md.getValue().equals(identifierOfAnchor)) {
                                if (md.getType().isIdentifier()) {
                                    // That's the anchor!
                                    anchorDocStruct = anchorStruct;
                                } else {
                                    // Log an error, maybe only the metadata is
                                    // not set as identifier.
                                    LOGGER.warn("Identifier '" + md.getType().getName()
                                            + "' found, but its type is NOT set to 'identifier' in the prefs!");
                                }
                            }
                        }
                    }
                    if (anchorDocStruct == null) {
                        LOGGER.error("CheckForAnchorReference: Referenced identifier for anchor '" + identifierOfAnchor
                                + "' not found in anchor DocStruct '" + anchorfilename + "'");
                        return null;
                    }
                }
            }
        }

        // Copy the anchor DocStruct, so it can be added as a parent: copy all
        // metadata, but not it's children.
        DocStruct newanchor = anchorDocStruct.copy(true, false);

        return newanchor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#getAnchorIdentifierFromMODSDOM(org.w3c. dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected String getAnchorIdentifierFromMODSDOM(Node inMods, DocStruct inStruct) {

        String anchoridentifier = null;
        // Result from XPath expression.
        NodeList resultlist = null;

        // Create an XPath Query to get the anchor identifier. Check, if
        // currentPath is already available.
        XPathFactory factory = XPathFactory.newInstance();

        // New namespace context.
        PersonalNamespaceContext pnc = new PersonalNamespaceContext();
        pnc.setNamespaceHash(this.namespaces);
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(pnc);

        try {
            XPathExpression expr = xpath.compile(this.xPathAnchorReference);

            // Carry out the query.
            Object list = expr.evaluate(inMods, XPathConstants.NODESET);
            resultlist = (NodeList) list;

            // Iterate over results.
            if (resultlist.getLength() > 1) {
                LOGGER.error("XPath expression for reference to the anchor is ambigious!");
                return null;
            }
            for (int i = 0; i < resultlist.getLength(); i++) {
                Node node = resultlist.item(i);
                // Get child Nodes to find the TextNode.
                NodeList nodelist = node.getChildNodes();
                for (int x = 0; x < nodelist.getLength(); x++) {
                    Node subnode = nodelist.item(x);
                    if (subnode.getNodeType() == TEXT_NODE) {
                        anchoridentifier = subnode.getNodeValue();
                        break;
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Something is wrong with the XPATH: " + e.getMessage());
            e.printStackTrace();
        }

        return anchoridentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsModsGdz#writePhysDivs(org.w3c.dom.Node, ugh.dl.DocStruct)
     */
    @Override
    protected Element writePhysDivs(Node parentNode, DocStruct inStruct) throws PreferencesException {

        // Write div element.
        Document domDoc = parentNode.getOwnerDocument();
        Element div = domDoc.createElementNS(this.namespaces.get("mets").getUri(), METS_DIV_STRING);

        String idphys = PHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divphysidMax);
        this.divphysidMax++;

        inStruct.setIdentifier(idphys);
        div.setAttribute(METS_ID_STRING, idphys);

        // Write METS type given in preferences, if existing.
        String type = getMetsType(inStruct.getType());
        if (type == null) {
            // If no METS type was configured, use internal type.
            type = inStruct.getType().getName();
        }
        div.setAttribute(METS_DIVTYPE_STRING, type);

        // Add physical CONTENTIDS attribute, if existing.
        if (!this.contentIDs.equals("")) {
            div.setAttribute(METS_CONTENTIDS_STRING, this.contentIDs);
        }

        // Add div element as child to parentNode.
        parentNode.appendChild(div);

        // Write metdata.
        if (this.metsNode == null) {
            LOGGER.error("METS node is null... can't write anything");
            return null;
        }

        int dmdid = writePhysDmd(this.metsNode, div, inStruct);

        // If dmdid is != -1 then the appropriate metadata section has been
        // written, if dmdid == -1, the inStruct has no metadata.
        String dmdidString = "";
        if (dmdid != -1) {
            dmdidString = DMDPHYS_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(dmdid);
            div.setAttribute("DMDID", dmdidString);
        }

        // Write links to ContentFiles (FPTRs)
        writeFptrs(inStruct, domDoc, div);

        // Get all children and write their divs recursive.
        List<DocStruct> allChildren = inStruct.getAllChildren();
        if (allChildren != null) {
            for (DocStruct child : allChildren) {
                if (writePhysDivs(div, child) == null) {
                    // Error occurred while writing div for child.
                    return null;
                }
            }
        }

        return div;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writeLogDivs(org.w3c.dom.Node, ugh.dl.DocStruct, boolean)
     */
    @Override
	protected Element writeLogDivs(Node parentNode, DocStruct inStruct, String anchorClass) throws WriteException,
			PreferencesException {

        // Write div element.
        Document domDoc = parentNode.getOwnerDocument();
        Element div = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_DIV_STRING);

        // Add div element as child to parentNode.
        parentNode.appendChild(div);
        if (this.firstDivNode == null) {
            this.firstDivNode = div;
        }

        String idlog = LOG_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(this.divlogidMax);
        div.setAttribute(METS_ID_STRING, idlog);
        this.divlogidMax++;

        // Write METS type given in preferences, if existing.
        String type = getMetsType(inStruct.getType());
        if (type == null) {
            // If no METS type was configured, use internal type.
            type = inStruct.getType().getName();
        }
        div.setAttribute(METS_DIVTYPE_STRING, type);

        // Set logical CONTENTIDS attribute if existing, and if current
        // docstruct is topstruct.
        //
        if (inStruct.getParent() == null && !this.purlUrl.equals("")) {
            div.setAttribute(METS_CONTENTIDS_STRING, this.purlUrl);
        }

        String label = "";
		String orderlabel = "";

        if (inStruct.getAllMetadata() != null) {
            for (Metadata md : inStruct.getAllMetadata()) {
                if (md.getType().getName().equals(METS_PREFS_LABEL_METADATA_STRING)) {
                    label = md.getValue();
				} else if (md.getType().getName().equals(METS_PREFS_ORDERLABEL_METADATA_STRING)) {
					orderlabel = md.getValue();
                } else if (md.getType().getName().equals(METS_URN_NAME)) {
                    div.setAttribute(METS_CONTENTIDS_STRING, md.getValue());
                }
            }
        }
        if (label != null && !label.equals("")) {
            div.setAttribute(METS_LABEL_STRING, label);
        }
		if (orderlabel != null && !orderlabel.equals("")) {
			div.setAttribute(METS_ORDERLABEL_STRING, orderlabel);
		}

        // Set identifier for this docStruct.
        inStruct.setIdentifier(idlog);

        // Write metadata.
        if (this.metsNode == null) {
            LOGGER.error("METS node is null... can't write anything");
            return null;
        }

        // Set the DMDIDs.
		int dmdid = writeLogDmd(this.metsNode, inStruct, anchorClass);
        if (dmdid >= 0) {
            // Just set DMDID attribute, if there is a metadata set.
            String dmdidString = DMDLOG_PREFIX + new DecimalFormat(DECIMAL_FORMAT).format(dmdid);
            div.setAttribute(METS_DMDID_STRING, dmdidString);
        }

        //Set the AMDIDs if necessary
        if (inStruct != null && inStruct.getAmdSec() != null) {
            String amdid = inStruct.getAmdSec().getId();
            if (amdid != null && !amdid.isEmpty()) {
                div.setAttribute(METS_ADMID_STRING, amdid);
            }
        } else {
            // Set the ADMID, depends if the current element is an anchor or not.
			if ((anchorClass != null && anchorClass.equals(inStruct.getType().getAnchorClass()))
					|| (inStruct.getParent() != null && inStruct.getParent().getType().getAnchorClass() != null && !inStruct
							.getParent().getType().getAnchorClass().equals(anchorClass))
					|| (anchorClass == null && inStruct.getType().getAnchorClass() == null && inStruct.getParent() == null)) {
                div.setAttribute(METS_ADMID_STRING, AMD_PREFIX);
            }
        }

        // Create MPTR element.
        Element mptr = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MPTR_STRING);
        mptr.setAttribute(METS_LOCTYPE_STRING, "URL");

        String ordernumber = "";
        // current element is anchor file
		if (inStruct.getType().getAnchorClass() != null) {
            if (inStruct.getAllChildren() != null && inStruct.getAllChildren().size() > 0) {
                DocStruct child = inStruct.getAllChildren().get(0);
                if (child.getAllMetadata() != null) {
                    for (Metadata md : child.getAllMetadata()) {
                        if (md.getType().getName().equals(RULESET_ORDER_NAME)) {
                            ordernumber = md.getValue();
                        }
                    }
                }
            }
        }
        // current element is first child of an anchor file
		else if (inStruct.getParent() != null && inStruct.getParent().getType().getAnchorClass() != null
				&& !inStruct.getParent().getType().getAnchorClass().equals(inStruct.getType().getAnchorClass())) {
            if (inStruct.getAllMetadata() != null) {
                for (Metadata md : inStruct.getAllMetadata()) {
                    if (md.getType().getName().equals(RULESET_ORDER_NAME)) {
                        ordernumber = md.getValue();
                    }
                }
            }
        }

		if (anchorClass == null && inStruct.getType().getAnchorClass() == null && inStruct.getParent() != null && inStruct.getParent().getType().getAnchorClass() != null) {
            if (ordernumber != null && ordernumber.length() > 0) {
                div.setAttribute(METS_ORDER_STRING, ordernumber);
            }
        }

		// Write an "upwards" MPTR pointing to a higher anchor file if the
		// metadata of the current docStruct is not kept in the file currently
		// under construction, and either the current docStruct has no parent
		// and the anchor class of the file to create is different from the
		// anchor class of the current docStruct, or if the parent of the
		// current docStruct belongs to a different anchor class and the anchor
		// class of the file to create appears after the anchor class of the
		// parent of the current docStruct in the list of anchor classes.
        if (inStruct.mustWriteUpwardsMptrIn(anchorClass)){
            if (this.mptrUrl.equals("")) {
                LOGGER.warn("No METS pointer URL (mptr) to the parent/anchor DocStruct is defined! Referencing will NOT work!");
            }
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, getUpwardsMptrFor(inStruct));
            // Write mptr element.
            div.appendChild(mptr);
        }

		// Write a "downwards" MPTR pointing to the only or a higher anchor
        // file if if the parent docStruct is of the the anchor class of the
        // file thas is currently written, but this docStruct isn’t.
		List<Metadata> metsPointerURLs = inStruct.getMetadataByType(CREATE_MPTR_ELEMENT_TYPE);
		if (metsPointerURLs.size() == 0 && inStruct.mustWriteDownwardsMptrIn(anchorClass)) {
            if (this.mptrUrlAnchor.equals("")) {
                LOGGER.warn("No METS pointer URL (mptr) to the child DocStructs is defined! Referencing will NOT work!");
            }
            createDomAttributeNS(mptr, this.xlinkNamespacePrefix, METS_HREF_STRING, getDownwardsMptrFor(inStruct, anchorClass));
            if (ordernumber != null && ordernumber.length() > 0) {
                div.setAttribute(METS_ORDER_STRING, ordernumber);
            }
            // Write mptr element.
            div.appendChild(mptr);
        }

		// Create METS pointer element if requested through meta data element
		// METADATA_TYPE_CREATE_MPTR
		for (Metadata url : metsPointerURLs) {
			Element metsPointer = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MPTR_STRING);
			metsPointer.setAttribute(METS_LOCTYPE_STRING, "URL");
			createDomAttributeNS(metsPointer, this.xlinkNamespacePrefix, METS_HREF_STRING, url.getValue());
			div.appendChild(metsPointer);
		}

        // Get all children and write their divs.
        List<DocStruct> allChildren = inStruct.getAllChildren();
        if (allChildren != null) {
            for (DocStruct child : allChildren) {
				if (anchorClass == null && child.isMetsPointerStruct()) {
					continue;
				}
				if (writeLogDivs(div, child, anchorClass) == null) {
                    // Error occurred while writing div for child.
                    return null;
                }
            }
        }

        return div;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#writeAmdSec(org.w3c.dom.Document, boolean)
     */
    @Override
    protected void writeAmdSec(Document domDoc, boolean isAnchorFile) {

        boolean rightsMDExists = false;
        boolean digiprovMDExists = false;

        // Creates the METS' AMDSEC, uses only *ONE* AMDID for ZVDD/DFG-Viewer.
        Element amdSec = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_AMDSEC_STRING);
        AmdSec amd = this.digdoc.getAmdSec();
        if (amd != null) {
            amdSec.setAttribute(METS_ID_STRING, amd.getId());
        } else {
            amdSec.setAttribute(METS_ID_STRING, AMD_PREFIX);
        }

        // create techMD
        List<Md> techMdList = this.digdoc.getTechMds();
        if (techMdList != null && techMdList.size() > 0) {
            for (Md md : techMdList) {
                this.techidMax++;
                Node theNode = domDoc.importNode(md.getContent(), true);
                Node child = theNode.getFirstChild();
                Element techMd = createDomElementNS(domDoc, this.metsNamespacePrefix, md.getType());
                if (md.getType().contentEquals(METS_RIGHTSMD_STRING)) {
                    Node mdWrap = md.getContent();
                    if (mdWrap != null) {
                        Node mdType = mdWrap.getAttributes().getNamedItem("OTHERMDTYPE");
                        if (mdType != null && mdType.getNodeValue().contentEquals("DVRIGHTS")) {
                            rightsMDExists = true;
                        }
                    }
                } else if (md.getType().contentEquals("digiprovMD")) {
                    Node mdWrap = md.getContent();
                    if (mdWrap != null) {
                        Node mdType = mdWrap.getAttributes().getNamedItem("OTHERMDTYPE");
                        if (mdType != null && mdType.getNodeValue().contentEquals("DVLINKS")) {
                            digiprovMDExists = true;
                        }
                    }
                }
                techMd.setAttribute(METS_ID_STRING, md.getId());
                Element techNode = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MDWRAP_STRING);
                for (int i = 0; i < theNode.getAttributes().getLength(); i++) {
                    Node attribute = theNode.getAttributes().item(i);
                    techNode.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
                    //					System.out.println("mdWrap attribute " + attribute.getNodeName() + ": " + attribute.getNodeValue());
                }
                //				techNode.setAttribute(METS_MDTYPE_STRING, "PREMIS:OBJECT");
                //				String idlog = TECHMD_PREFIX + "_" + new DecimalFormat(DECIMAL_FORMAT).format(this.techidMax);
                //				techMd.setAttribute(METS_ID_STRING, idlog);
                techNode.appendChild(child);
                techMd.appendChild(techNode);
                amdSec.appendChild(techMd);
            }
        }

        if (!rightsMDExists) {
            // Create rightsMD.
            //
            Element rightsMd = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_RIGHTSMD_STRING);
            rightsMd.setAttribute(METS_ID_STRING, "RIGHTS");
            amdSec.appendChild(rightsMd);

            // Create mdWrap.
            Element mdWrap = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_MDWRAP_STRING);
            mdWrap.setAttribute(METS_MIMETYPE_STRING, "text/xml");
            mdWrap.setAttribute(METS_MDTYPE_STRING, "OTHER");
            mdWrap.setAttribute(METS_OTHERMDTYPE_STRING, "DVRIGHTS");
            rightsMd.appendChild(mdWrap);

            // Create xmlData.
            Element xmlData = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_XMLDATA_STRING);
            mdWrap.appendChild(xmlData);

            // Create dv tags.
            Element dv = createDomElementNS(domDoc, this.dvNamespacePrefix, "rights");
            xmlData.appendChild(dv);
            Element dvOwner = createDomElementNS(domDoc, this.dvNamespacePrefix, "owner");
            Element dvOwnerLogo = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerLogo");
            Element dvOwnerSiteURL = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerSiteURL");
            Element dvOwnerContact = createDomElementNS(domDoc, this.dvNamespacePrefix, "ownerContact");
            dvOwner.setTextContent(this.rightsOwner);
            dvOwnerLogo.setTextContent(this.rightsOwnerLogo);
            dvOwnerSiteURL.setTextContent(this.rightsOwnerSiteURL);
            dvOwnerContact.setTextContent(this.rightsOwnerContact);
            dv.appendChild(dvOwner);
            dv.appendChild(dvOwnerLogo);
            dv.appendChild(dvOwnerSiteURL);
            dv.appendChild(dvOwnerContact);
        }

        if (!digiprovMDExists) {
            // Create digiprovMD.
            //
            Element digiprovMd = createDomElementNS(domDoc, this.metsNamespacePrefix, "digiprovMD");
            digiprovMd.setAttribute(METS_ID_STRING, "DIGIPROV");
            amdSec.appendChild(digiprovMd);

            // Create mdWrap.
            Element mdWrapDigiprov = createDomElementNS(domDoc, this.metsNamespacePrefix, "mdWrap");
            mdWrapDigiprov.setAttribute(METS_MIMETYPE_STRING, "text/xml");
            mdWrapDigiprov.setAttribute(METS_MDTYPE_STRING, "OTHER");
            mdWrapDigiprov.setAttribute(METS_OTHERMDTYPE_STRING, "DVLINKS");
            digiprovMd.appendChild(mdWrapDigiprov);

            // Create xmlData.
            Element xmlDataDigiprov = createDomElementNS(domDoc, this.metsNamespacePrefix, METS_XMLDATA_STRING);
            mdWrapDigiprov.appendChild(xmlDataDigiprov);

            // Create dv tags.
            Element dvDigiprov = createDomElementNS(domDoc, this.dvNamespacePrefix, "links");
            xmlDataDigiprov.appendChild(dvDigiprov);
            Element dvReference = createDomElementNS(domDoc, this.dvNamespacePrefix, "reference");
            Element dvPresentation = createDomElementNS(domDoc, this.dvNamespacePrefix, "presentation");
            // Set values according to anchor flag.
            if (isAnchorFile) {
                dvReference.setTextContent(this.digiprovReferenceAnchor);
                dvPresentation.setTextContent(this.digiprovPresentationAnchor);
            } else {
                dvReference.setTextContent(this.digiprovReference);
                dvPresentation.setTextContent(this.digiprovPresentation);
            }

            dvDigiprov.appendChild(dvReference);
            dvDigiprov.appendChild(dvPresentation);
        }

        // Append to our metsNode, before the fileSec (or before the structMap
        // if anchor file).
        //
        String element;
        if (isAnchorFile) {
            element = this.metsNamespacePrefix + ":" + METS_STRUCTMAP_STRING;
        } else {
            element = this.metsNamespacePrefix + ":" + METS_FILESEC_STRING;
        }
        NodeList dmdList = this.metsNode.getElementsByTagName(element);
        Node refChild = dmdList.item(0);
        if (refChild != null) {
            this.metsNode.insertBefore(amdSec, refChild);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ugh.fileformats.mets.MetsMods#checkMissingSettings()
     */
    @Override
    protected List<String> checkMissingSettings() {
        List<String> result = new LinkedList<String>();

        if (this.rightsOwner.equals("")) {
            result.add(METS_RIGHTS_OWNER_STRING);
        }
        if (this.rightsOwnerLogo.equals("")) {
            result.add(METS_RIGHTS_OWNER_LOGO_STRING);
        }
        if (this.rightsOwnerSiteURL.equals("")) {
            result.add(METS_RIGHTS_OWNER_SITE_STRING);
        }
        if (this.rightsOwnerContact.equals("")) {
            result.add(METS_RIGHTS_OWNER_CONTACT_STRING);
        }
        if (this.digiprovReference.equals("")) {
            result.add(METS_DIGIPROV_REFERENCE_STRING);
        }
        if (this.digiprovPresentation.equals("") || this.digiprovPresentationAnchor.equals("")) {
            result.add(METS_DIGIPROV_PRESENTATION_STRING);
        }
        if (this.mptrUrl.equals("")) {
            result.add(METS_MPTR_URL_STRING);
        }
        if (this.mptrUrlAnchor.equals("")) {
            result.add(METS_MPTR_URL_ANCHOR_STRING);
        }

        return result;
    }

    /***************************************************************************
     * PRIVATE (AND PROTECTED) METHODS
     **************************************************************************/

    /***************************************************************************
     * <p>
     * Creates a single Goobi internal metadata element.
     * </p>
     * 
     * @param theXQuery
     * @param theMetadata
     * @param theStartingNode
     * @param theDocument
     * @throws PreferencesException
     **************************************************************************/
    protected void writeSingleModsMetadata(String theXQuery, MatchingMetadataObject theMMO, Metadata theMetadata, Node theStartingNode,
            Document theDocument) throws PreferencesException {

        // Get metadata to set.
        String newMetadataValue = theMetadata.getValue();

        // newMetadataValue = newMetadataValue.replace("< ", "&lt; ").replace("> ", "&gt; ").replace("\"", "&quot;");

        // Check conditions from the prefs. If they exist and do NOT
        // match, continue with the next mmo.
        Perl5Util perlUtil = new Perl5Util();

        try {
            if (theMMO != null && theMMO.getValueCondition() != null && !theMMO.getValueCondition().equals("")
                    && !perlUtil.match(theMMO.getValueCondition(), theMetadata.getValue())) {

                LOGGER.info("Condition '" + theMMO.getValueCondition() + "' for Metadata '" + theMMO.getInternalName() + " ("
                        + theMetadata.getValue() + ")" + "' does not match, no node was created...");
                return;
            }
        } catch (MalformedPerl5PatternException e) {
            String message =
                    "The regular expression '" + theMMO.getValueCondition() + "' delivered with Metadata '" + theMMO.getInternalName() + "' in the "
                            + METS_PREFS_NODE_NAME_STRING + " section of the preferences file is not valid!";
            LOGGER.error(message, e);
            throw new PreferencesException(message, e);
        }

        // Check and process regular expression from the prefs.

        try {
            if (theMMO != null && theMMO.getValueRegExp() != null && !theMMO.getValueRegExp().equals("")) {

                newMetadataValue = new String(perlUtil.substitute(theMMO.getValueRegExp(), theMetadata.getValue()));
                LOGGER.info("Regular expression '" + theMMO.getValueRegExp() + "' changed value of Metadata '" + theMMO.getInternalName()
                        + "' from '" + theMetadata.getValue() + "' to '" + newMetadataValue + "'");
            }
        } catch (MalformedPerl5PatternException e) {
            String message =
                    "The regular expression '" + theMMO.getValueRegExp() + "' delivered with Metadata '" + theMMO.getInternalName() + "' in the "
                            + METS_PREFS_NODE_NAME_STRING + " section of the preferences file is not valid!";
            LOGGER.error(message, e);
            throw new PreferencesException(message, e);
        }

        // Only create node, if a value is existing.
        if (!newMetadataValue.equals("")) {
            Node createdNode = createNode(theXQuery, theStartingNode, theDocument);

            if (createdNode == null) {
                String message =
                        "DOM Node could not be created for metadata '" + theMetadata.getType().getName() + "'! XQuery was '" + theXQuery + "'";
                LOGGER.error(message);
                throw new PreferencesException(message);
            }

            // Add value to node.
            Node valueNode = theDocument.createTextNode(newMetadataValue);

            
            if (theMetadata.getAuthorityID() != null && theMetadata.getAuthorityURI() != null && theMetadata.getAuthorityValue() != null) {
                ((Element) createdNode).setAttribute("authority", theMetadata.getAuthorityID());
                ((Element) createdNode).setAttribute("authorityURI", theMetadata.getAuthorityURI());
                ((Element) createdNode).setAttribute("valueURI", theMetadata.getAuthorityValue());
            }
            
            createdNode.appendChild(valueNode);
            LOGGER.trace("Value '" + newMetadataValue + "' (" + theMetadata.getType().getName() + ") added to node >>" + createdNode.getNodeName()
                    + "<<");
        }
    }

    protected void writeSingleModsGroup(MatchingMetadataObject mmo, MetadataGroup theGroup, Node theStartingNode, Document theDocument)
            throws PreferencesException {

        Node createdNode = createNode(mmo.getWriteXPath(), theStartingNode, theDocument);
        Map<String, Map<String, String>> xpathMap = mmo.getMetadataGroupXQueries();

        for (String metadataName : xpathMap.keySet()) {
            for (Metadata md : theGroup.getMetadataList()) {
                if (md.getType().getName().equals(metadataName) && md.getValue() != null && !md.getValue().isEmpty()) {
                    Map<String, String> xqueryMap = xpathMap.get(metadataName);
                    String xquery = xqueryMap.get(metadataName);
                    writeSingleModsMetadata(xquery, md, createdNode, theDocument);
                    break;
                }
            }
            for (Person p : theGroup.getPersonList()) {
                if (p.getType().getName().equals(metadataName)) {
                    Map<String, String> xqueryMap = xpathMap.get(metadataName);
                    writeSingleGroupPerson(p, xqueryMap, createdNode, theDocument);
                    break;
                }
            }
        }
    }

    private void writeSingleGroupPerson(Person thePerson, Map<String, String> xpathMap, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        if ((thePerson.getLastname() != null && !thePerson.getLastname().equals(""))
                || (thePerson.getFirstname() != null && !thePerson.getFirstname().equals(""))) {
            if (thePerson.getLastname() != null && !thePerson.getLastname().equals("") && thePerson.getFirstname() != null
                    && !thePerson.getFirstname().equals("")) {
                thePerson.setDisplayname(thePerson.getLastname() + ", " + thePerson.getFirstname());
            } else if (thePerson.getFirstname() == null || thePerson.getFirstname().equals("")) {
                thePerson.setDisplayname(thePerson.getLastname());
            } else {
                thePerson.setDisplayname(thePerson.getFirstname());
            }
        }

        String xquery = xpathMap.get(METS_PREFS_WRITEXPATH_STRING);
        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc);

        for (String key : xpathMap.keySet()) {
            xquery = xpathMap.get(key);
            

            if (key.equalsIgnoreCase(METS_PREFS_FIRSTNAMEXPATH_STRING) && thePerson.getFirstname() != null) {
                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s firstname '" + thePerson.getFirstname() + "'");
                } else {
                    Node firstnameNode = createNode(xquery, createdNode, theDomDoc);
                    Node firstnamevalueNode = theDomDoc.createTextNode(thePerson.getFirstname());
                    firstnameNode.appendChild(firstnamevalueNode);
                    createdNode.appendChild(firstnameNode);
                }

            } else if (key.equalsIgnoreCase(METS_PREFS_LASTNAMEXPATH_STRING) && thePerson.getLastname() != null) {
                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s lastname '" + thePerson.getLastname() + "'");
                } else {
                    Node lastnameNode = createNode(xquery, createdNode, theDomDoc);
                    Node lastnamevalueNode = theDomDoc.createTextNode(thePerson.getLastname());
                    lastnameNode.appendChild(lastnamevalueNode);
                    createdNode.appendChild(lastnameNode);
                }
            } else if (key.equalsIgnoreCase(METS_PREFS_AFFILIATIONXPATH_STRING) && thePerson.getAffiliation() != null) {
                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s affiliation '" + thePerson.getAffiliation() + "'");
                } else {
                    Node affiliationNode = createNode(xquery, createdNode, theDomDoc);
                    Node affiliationvalueNode = theDomDoc.createTextNode(thePerson.getAffiliation());
                    affiliationNode.appendChild(affiliationvalueNode);
                    createdNode.appendChild(affiliationNode);
                }

            } else if (key.equalsIgnoreCase(METS_PREFS_DISPLAYNAMEXPATH_STRING) && thePerson.getDisplayname() != null) {
                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s displayName '" + thePerson.getDisplayname() + "'");
                } else {
                    Node displaynameNode = createNode(xquery, createdNode, theDomDoc);
                    Node displaynamevalueNode = theDomDoc.createTextNode(thePerson.getDisplayname());
                    displaynameNode.appendChild(displaynamevalueNode);
                    createdNode.appendChild(displaynameNode);
                }

            } else if (key.equalsIgnoreCase(METS_PREFS_PERSONTYPEXPATH_STRING) && thePerson.getPersontype() != null) {
                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s personType '" + thePerson.getPersontype() + "'");
                } else {
                    Node persontypeNode = createNode(xquery, createdNode, theDomDoc);
                    Node persontypevalueNode = theDomDoc.createTextNode(thePerson.getPersontype());
                    persontypeNode.appendChild(persontypevalueNode);
                    createdNode.appendChild(persontypeNode);
                }

            } else if (key.equalsIgnoreCase(METS_PREFS_AUTHORITYFILEIDXPATH_STRING) && thePerson.getAuthorityID() != null) {

                if (xquery == null) {
                    LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s authorityFileID '" + thePerson.getAuthorityID()
                            + "'");
                } else {
                    Node authorityfileidNode = createNode(xquery, createdNode, theDomDoc);
                    Node authorityfileidvalueNode = theDomDoc.createTextNode(thePerson.getAuthorityID());
                    authorityfileidNode.appendChild(authorityfileidvalueNode);
                    createdNode.appendChild(authorityfileidNode);
                }

            }
        }

    }

    /***************************************************************************
     * <p>
     * Creates a single Goobi MODS person element.
     * </p>
     * 
     * @param xquery
     * @param theMMO
     * @param thePerson
     * @param theDomModsNode
     * @param theDomDoc
     * @throws PreferencesException
     **************************************************************************/
    private void writeSingleModsPerson(String xquery, MatchingMetadataObject theMMO, Person thePerson, Node theDomModsNode, Document theDomDoc)
            throws PreferencesException {

        Node createdNode = createNode(xquery, theDomModsNode, theDomDoc);

        if (createdNode == null) {
            String message = "DOM Node could not be created for person '" + thePerson + "'! XQuery was '" + xquery + "'";
            LOGGER.error(message);
            throw new PreferencesException(message);
        }

        // if (thePerson.getLastname() != null) {
        // thePerson.setLastname(thePerson.getLastname().replace("< ", "&lt; ").replace("> ", "&gt; ").replace("\"", "&quot;"));
        // }
        // if (thePerson.getFirstname() != null) {
        // thePerson.setFirstname(thePerson.getFirstname().replace("< ", "&lt; ").replace("> ", "&gt; ").replace("\"", "&quot;"));
        // }

        // Set the displayname of the current person, if NOT already set! Use
        // "lastname, name" as we were told in the MODS profile.
        if ((thePerson.getLastname() != null && !thePerson.getLastname().equals(""))
                || (thePerson.getFirstname() != null && !thePerson.getFirstname().equals(""))) {
            if (thePerson.getLastname() != null && !thePerson.getLastname().equals("") && thePerson.getFirstname() != null
                    && !thePerson.getFirstname().equals("")) {
                thePerson.setDisplayname(thePerson.getLastname() + ", " + thePerson.getFirstname());
            } else if (thePerson.getFirstname() == null || thePerson.getFirstname().equals("")) {
                thePerson.setDisplayname(thePerson.getLastname());
            } else {
                thePerson.setDisplayname(thePerson.getFirstname());
            }
        }

        // Create the subnodes.
        if (thePerson.getLastname() != null) {
            xquery = theMMO.getLastnameXQuery();
            if (xquery == null) {
                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s lastname '" + thePerson.getLastname() + "'");
            } else {
                Node lastnameNode = createNode(xquery, createdNode, theDomDoc);
                Node lastnamevalueNode = theDomDoc.createTextNode(thePerson.getLastname());
                lastnameNode.appendChild(lastnamevalueNode);
                createdNode.appendChild(lastnameNode);
            }
        }
        if (thePerson.getFirstname() != null) {
            xquery = theMMO.getFirstnameXQuery();
            if (xquery == null) {
                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s firstname '" + thePerson.getFirstname() + "'");
            } else {
                Node firstnameNode = createNode(xquery, createdNode, theDomDoc);
                Node firstnamevalueNode = theDomDoc.createTextNode(thePerson.getFirstname());
                firstnameNode.appendChild(firstnamevalueNode);
                createdNode.appendChild(firstnameNode);
            }
        }
        if (thePerson.getAffiliation() != null) {
            xquery = theMMO.getAffiliationXQuery();
            if (xquery == null) {
                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s affiliation '" + thePerson.getAffiliation() + "'");
            } else {
                Node affiliationNode = createNode(xquery, createdNode, theDomDoc);
                Node affiliationvalueNode = theDomDoc.createTextNode(thePerson.getAffiliation());
                affiliationNode.appendChild(affiliationvalueNode);
                createdNode.appendChild(affiliationNode);
            }
        }
        
        if (thePerson.getAuthorityID() != null && thePerson.getAuthorityURI() != null && thePerson.getAuthorityValue() != null) {
            ((Element) createdNode).setAttribute("authority", thePerson.getAuthorityID());
            ((Element) createdNode).setAttribute("authorityURI", thePerson.getAuthorityURI());
            ((Element) createdNode).setAttribute("valueURI", thePerson.getAuthorityValue());
            
//            xquery = theMMO.getAuthorityIDXquery();
//            if (xquery == null) {
//                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s authorityFileID '" + thePerson.getAuthorityID() + "'");
//            } else {
//                Node authorityfileidNode = createNode(xquery, createdNode, theDomDoc);
//                Node authorityfileidvalueNode = theDomDoc.createTextNode(thePerson.getAuthorityID());
//                authorityfileidNode.appendChild(authorityfileidvalueNode);
//                createdNode.appendChild(authorityfileidNode);
//            }
        }
        if (thePerson.getDisplayname() != null) {
            xquery = theMMO.getDisplayNameXQuery();
            if (xquery == null) {
                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s displayName '" + thePerson.getDisplayname() + "'");
            } else {
                Node displaynameNode = createNode(xquery, createdNode, theDomDoc);
                Node displaynamevalueNode = theDomDoc.createTextNode(thePerson.getDisplayname());
                displaynameNode.appendChild(displaynamevalueNode);
                createdNode.appendChild(displaynameNode);
            }
        }
        if (thePerson.getPersontype() != null) {
            xquery = theMMO.getPersontypeXQuery();
            if (xquery == null) {
                LOGGER.warn("No XQuery given for " + thePerson.getType().getName() + "'s personType '" + thePerson.getPersontype() + "'");
            } else {
                Node persontypeNode = createNode(xquery, createdNode, theDomDoc);
                Node persontypevalueNode = theDomDoc.createTextNode(thePerson.getPersontype());
                persontypeNode.appendChild(persontypevalueNode);
                createdNode.appendChild(persontypeNode);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Finds the METS type element for a given DocStructType; iterates over the modNamesDS list.
     * </p>
     * 
     * @param metstype
     * @return METS type string for the given internal DocStructType.
     **************************************************************************/
    private String getMetsType(DocStructType docStructType) {

        for (MatchingDocStructObject mds : this.modsNamesDS) {
            if (mds.getInternaltype() != null && mds.getInternaltype().getName().equals(docStructType.getName())) {
                return mds.getMetstype();
            }
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Gives a warning string including all missing MODS mappings.
     * </p>
     * 
     * @param theStruct
     * @param theList
     * @return
     **************************************************************************/
    private String getMappingWarning(DocStructType theStruct, List theList) {

        String result = "";

        if (theStruct != null && !theList.isEmpty()) {
            StringBuffer listEntries = new StringBuffer();

            for (Object o : theList) {
                if (o instanceof Metadata || o instanceof Person) {
                    Metadata m = (Metadata) o;
                    if (m.getValue() != null && !m.getValue().equals("")) {
                        listEntries.append("[" + m.getType().getName() + ":'" + m.getValue() + "'] ");
                    } else {
                        listEntries.append("[" + m.getType().getName() + "] ");
                    }
                }
            }

            result =
                    "The following metadata types for DocStruct '" + theStruct.getName() + "' are NOT YET mapped to the MODS: "
                            + listEntries.toString().trim();
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Parses a single &lt;Metadata> element in the METS section of the preference file. For the element an appropriate MatchingMetadataObject is
     * created and added to the list of all MatchingMetadataObjects - modsNamesMD.
     * </p>
     * 
     * @param inNode the DOM node of the opening tag of the &lt;metadata> element
     * @throws PreferencesException
     **************************************************************************/
    @Override
    protected void readMetadataPrefs(Node inNode) throws PreferencesException {

        String internalName = null;
        String personName = null;
        String modsName = null;
        NodeList childlist = inNode.getChildNodes();
        MatchingMetadataObject mmo = new MatchingMetadataObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);

            if (currentNode.getNodeName() == null) {
                continue;
            }

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                // Get internal name.
                if (currentNode.getNodeName().equals(METS_PREFS_INTERNALNAME_STRING)) {
                    internalName = getTextNodeValue(currentNode);

                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        LOGGER.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setInternalName(internalName.trim());
                }

                // Get valueCondition.
                if (currentNode.getNodeName().equals(METS_PREFS_VALUECONDITION_STRING)) {
                    internalName = getTextNodeValue(currentNode);
                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_VALUECONDITION_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        LOGGER.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setValueCondition(internalName.trim());
                }

                // Get valueRegExp.
                if (currentNode.getNodeName().equals(METS_PREFS_VALUEREGEXP_STRING)) {
                    internalName = getTextNodeValue(currentNode);
                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_VALUEREGEXP_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        LOGGER.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setValueRegExp(internalName.trim());
                }

                // Get MODS XPATH settings.
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_XPATH_STRING)) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName == null) {
                        LOGGER.warn("<" + METS_PREFS_XPATH_STRING + "> is existing for metadata '" + internalName + "', but has no value!");
                    }
                    mmo.setReadXQuery(xpathName.trim());
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEXPATH_STRING)) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName == null) {
                        PreferencesException pe = new PreferencesException("<" + METS_PREFS_WRITEXPATH_STRING + "> is existing, but has no value!");
                        throw pe;
                    }
                    mmo.setWriteXQuery(xpathName.trim());
                }
                // Get MODS Person settings.
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_FIRSTNAMEXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setFirstnameXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_LASTNAMEXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setLastnameXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_AFFILIATIONXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setAffiliationXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_DISPLAYNAMEXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setDisplayNameXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_PERSONTYPEXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setPersontypeXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_AUTHORITYFILEIDXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setAuthorityIDXquery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_IDENTIFIERXPATH_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setIdentifierXQuery(personName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_IDENTIFIERTYPEXPATH_STRING)) {
                    personName = getTextNodeValue(currentNode);
                    if (personName != null) {
                        mmo.setIdentifierTypeXQuery(personName.trim());
                    }
                }

                // Get other MODS settings (used for reading only?).
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_READMODSNAME_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setReadModsName(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEMODSNAME_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setReadModsName(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSTYPE_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSType(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSENCODING_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSEncoding(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSAUTHORITY_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSAuthority(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSLANG_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSLang(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSXMLLANG_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSXMLLang(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSID_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSID(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSSCRIPT_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSScript(modsName.trim());
                    }
                }
                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_MODSTRANSLITERATION_STRING)) {
                    modsName = getTextNodeValue(currentNode);
                    if (modsName != null) {
                        mmo.setMODSTransliteration(modsName.trim());
                    }
                }
            }
        }

        // The internal name is needed for every MMO!
        if (mmo.getInternalName() != null) {
            this.modsNamesMD.add(mmo);
        }
    }

    @Override
	protected void readMetadataGroupPrefs(Node inNode) throws PreferencesException {
        String internalName = null;

        NodeList childlist = inNode.getChildNodes();
        MatchingMetadataObject mmo = new MatchingMetadataObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);

            if (currentNode.getNodeName() == null) {
                continue;
            }

            if (currentNode.getNodeType() == ELEMENT_NODE) {
                // Get internal name.
                if (currentNode.getNodeName().equals(METS_PREFS_INTERNALNAME_STRING)) {
                    internalName = getTextNodeValue(currentNode);

                    if (internalName == null) {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_METADATA_STRING + " mapping, but has no value!";
                        LOGGER.error(message);
                        throw new PreferencesException(message);
                    }
                    mmo.setInternalName(internalName.trim());
                }

                // Get MODS XPATH settings.

                if (currentNode.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEXPATH_STRING)) {
                    String xpathName = getTextNodeValue(currentNode);
                    if (xpathName == null) {
                        PreferencesException pe = new PreferencesException("<" + METS_PREFS_WRITEXPATH_STRING + "> is existing, but has no value!");
                        throw pe;
                    }
                    mmo.setWriteXQuery(xpathName.trim());
                }

                if (currentNode.getNodeName().equalsIgnoreCase("Metadata")) {

                    NodeList metadataChildlist = currentNode.getChildNodes();

                    String elementName = "";
                    String xpath = "";
                    for (int k = 0; k < metadataChildlist.getLength(); k++) {
                        // Get single node.

                        Node metadataSubElement = metadataChildlist.item(k);

                        if (metadataSubElement.getNodeType() == ELEMENT_NODE) {
                            // Get internal name.
                            if (metadataSubElement.getNodeName().equals(METS_PREFS_INTERNALNAME_STRING)) {
                                elementName = getTextNodeValue(metadataSubElement);
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEXPATH_STRING)) {
                                xpath = getTextNodeValue(metadataSubElement);

                            }
                        }

                    }
                    if (!elementName.isEmpty() && !xpath.isEmpty()) {
                        Map<String, String> map = new LinkedHashMap<String, String>();
                        map.put(elementName, xpath);
                        mmo.addToMap(elementName, map);
                    }
                } else if (currentNode.getNodeName().equalsIgnoreCase("Person")) {

                    NodeList metadataChildlist = currentNode.getChildNodes();

                    String elementName = "";
                    Map<String, String> map = new HashMap<String, String>();
                    for (int k = 0; k < metadataChildlist.getLength(); k++) {
                        // Get single node.

                        Node metadataSubElement = metadataChildlist.item(k);
                        if (metadataSubElement.getNodeType() == ELEMENT_NODE) {
                            // Get internal name.
                            if (metadataSubElement.getNodeName().equals(METS_PREFS_INTERNALNAME_STRING)) {
                                elementName = getTextNodeValue(metadataSubElement);
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_WRITEXPATH_STRING, value.trim());
                                }
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_FIRSTNAMEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_FIRSTNAMEXPATH_STRING, value.trim());
                                }
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_LASTNAMEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_LASTNAMEXPATH_STRING, value.trim());
                                }
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_AFFILIATIONXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_AFFILIATIONXPATH_STRING, value.trim());
                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_DISPLAYNAMEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_DISPLAYNAMEXPATH_STRING, value.trim());
                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_PERSONTYPEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_PERSONTYPEXPATH_STRING, value.trim());
                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_AUTHORITYFILEIDXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_AUTHORITYFILEIDXPATH_STRING, value.trim());
                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_IDENTIFIERXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_IDENTIFIERXPATH_STRING, value.trim());
                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_IDENTIFIERTYPEXPATH_STRING)) {
                                String value = getTextNodeValue(metadataSubElement);
                                if (value != null) {
                                    map.put(METS_PREFS_IDENTIFIERTYPEXPATH_STRING, value.trim());
                                }

                                // Get other MODS settings (used for reading only?).
                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_READMODSNAME_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setReadModsName(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_WRITEMODSNAME_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setReadModsName(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSTYPE_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSType(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSENCODING_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSEncoding(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSAUTHORITY_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSAuthority(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSLANG_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSLang(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSXMLLANG_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSXMLLang(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSID_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSID(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSSCRIPT_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSScript(modsName.trim());
                                //                                }

                            } else if (metadataSubElement.getNodeName().equalsIgnoreCase(METS_PREFS_MODSTRANSLITERATION_STRING)) {
                                //                                modsName = getTextNodeValue(metadataSubElement);
                                //                                if (modsName != null) {
                                //                                    mmo.setMODSTransliteration(modsName.trim());
                                //                                }
                            }
                        }
                    }
                    mmo.addToMap(elementName, map);
                }
            }
        }

        // The internal name is needed for every MMO!
        if (mmo.getInternalName() != null) {
            this.modsNamesMD.add(mmo);
        }
    }

    /***************************************************************************
     * <p>
     * Reads the DocStruct settings from the preferences fie.
     * </p>
     * 
     * @param inNode
     **************************************************************************/
    @Override
    protected void readDocStructPrefs(Node inNode) throws PreferencesException {

        NodeList childlist = inNode.getChildNodes();
        MatchingDocStructObject mds = new MatchingDocStructObject();

        for (int i = 0; i < childlist.getLength(); i++) {
            // Get single node.
            Node currentNode = childlist.item(i);
            String nodename = currentNode.getNodeName();
            if (nodename == null) {
                continue;
            }
            if (currentNode.getNodeType() == ELEMENT_NODE) {
                if (nodename.equalsIgnoreCase(METS_PREFS_INTERNALNAME_STRING)) {
                    String internalName = getTextNodeValue(currentNode);
                    if (internalName != null) {
                        DocStructType internalType = this.myPreferences.getDocStrctTypeByName(internalName.trim());
                        mds.setInternaltype(internalType);
                    } else {
                        String message =
                                "<" + METS_PREFS_INTERNALNAME_STRING + "> is existing in " + PREFS_DOCSTRUCT_STRING + " mapping, but has no value!";
                        LOGGER.error(message);
                        throw new PreferencesException(message);
                    }
                }

                if (nodename.equalsIgnoreCase(METS_PREFS_METSTYPE_STRING)) {
                    String metstypename = getTextNodeValue(currentNode);
                    if (metstypename != null) {
                        mds.setMetstype(metstypename.trim());
                    }
                }
            }
        }

        // The internal type is needed for every MDS!
        if (mds.getInternaltype() != null) {
            this.modsNamesDS.add(mds);
        }
    }

    /**************************************************************************
     * <p>
     * PLEASE DO NOT TELL ANYONE! This is just a small grouping hack, until the MODS creation is implemented maybe using MODS XML Beans!
     * </p>
     * 
     * TODO This is a really dirty hack, I will fix it tomorrow! (hihi)
     * 
     * @param theModsNode
     * 
     **************************************************************************/

    @Deprecated
    private void dirtyReplaceGroupingTagNameHack(Node theNode) {

        // Replace things.
        if (this.replaceGroupTags.containsKey(theNode.getLocalName())) {
            // Get replacement name.
            String replacementName = this.replaceGroupTags.get(theNode.getLocalName());
            // Create replacement node.
            Node replacementNode = createDomElementNS(theNode.getOwnerDocument(), theNode.getPrefix(), replacementName);
            // Copy all children from the old node to the new node.
            if (theNode.hasChildNodes()) {
                for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
                    replacementNode.appendChild(theNode.getChildNodes().item(i).cloneNode(true));
                }
            }
            // Copy all attributes of the old node to the new one.
            if (theNode.hasAttributes()) {
                for (int i = 0; i < theNode.getAttributes().getLength(); i++) {
                    replacementNode.appendChild(theNode.getAttributes().item(i).cloneNode(true));
                }
            }
            // Finally replace the node.
            theNode.getParentNode().replaceChild(replacementNode, theNode);

            LOGGER.trace("Tag '" + theNode.getLocalName() + "' replaced with '" + replacementName + "'! DO NOT TELL ANYONE!");
        }

        // Get all child nodes and iterate, if some do exist.
        if (theNode.hasChildNodes()) {
            for (int i = 0; i < theNode.getChildNodes().getLength(); i++) {
                dirtyReplaceGroupingTagNameHack(theNode.getChildNodes().item(i));
            }
        }
    }

    /**************************************************************************
     * <p>
     * Substitutes an existing $REGEXP().
     * </p>
     * 
     * 
     * @param theString
     * @return
     **************************************************************************/
    private String checkForRegExp(String theString) {

        // Look, if things shall be substituted.
        Perl5Util perlUtil = new Perl5Util();
        if (perlUtil.match("/\\$REGEXP(.*)/", theString)) {
            // Get the index of the "(" and the index of the ")".
            int bracketStartIndex = perlUtil.beginOffset(0);
            int bracketEndIndex = perlUtil.endOffset(1);

            // Get the RegExp out of the string.
            String regExp = theString.substring(bracketStartIndex + 8, bracketEndIndex - 1);

            // Remove the RegExp from the string.
            theString = theString.substring(0, bracketStartIndex);

            // Substitute things, if any $REGEXP() is existing.
            theString = perlUtil.substitute(regExp, theString);
        }

        return theString;
    }

    /***************************************************************************
     * GETTERS AND SETTERS
     **************************************************************************/

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwner() {
        return this.rightsOwner;
    }

    /***************************************************************************
     * @param rightsOwner
     **************************************************************************/
    public void setRightsOwner(String rightsOwner) {

        if (rightsOwner == null) {
            this.rightsOwner = "";
        } else {
            this.rightsOwner = checkForRegExp(rightsOwner);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerLogo() {
        return this.rightsOwnerLogo;
    }

    /***************************************************************************
     * @param rightsOwnerLogo
     **************************************************************************/
    public void setRightsOwnerLogo(String rightsOwnerLogo) {

        if (rightsOwnerLogo == null) {
            this.rightsOwnerLogo = "";
        } else {
            this.rightsOwnerLogo = checkForRegExp(rightsOwnerLogo);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerSiteURL() {
        return this.rightsOwnerSiteURL;
    }

    /***************************************************************************
     * @param rightsOwnerSiteURL
     **************************************************************************/
    public void setRightsOwnerSiteURL(String rightsOwnerSiteURL) {

        if (rightsOwnerSiteURL == null) {
            this.rightsOwnerSiteURL = "";
        } else {
            this.rightsOwnerSiteURL = checkForRegExp(rightsOwnerSiteURL);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getRightsOwnerContact() {
        return this.rightsOwnerContact;
    }

    /***************************************************************************
     * @param rightsOwnerContact
     **************************************************************************/
    public void setRightsOwnerContact(String rightsOwnerContact) {

        if (rightsOwnerContact == null) {
            this.rightsOwnerContact = "";
        } else {
            this.rightsOwnerContact = checkForRegExp(rightsOwnerContact);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovReference() {
        return this.digiprovReference;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv reference and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovReference
     **************************************************************************/
    public void setDigiprovReference(String digiprovReference) {

        if (digiprovReference == null) {
            this.digiprovReference = "";
        } else {
            this.digiprovReference = checkForRegExp(digiprovReference);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovPresentation() {
        return this.digiprovPresentation;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv presentation and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovPresentation
     **************************************************************************/
    public void setDigiprovPresentation(String digiprovPresentation) {

        if (digiprovPresentation == null) {
            this.digiprovPresentation = "";
        } else {
            this.digiprovPresentation = checkForRegExp(digiprovPresentation);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovReferenceAnchor() {
        return this.digiprovReferenceAnchor;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv anchor reference and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovReference
     **************************************************************************/
    public void setDigiprovReferenceAnchor(String digiprovReferenceAnchor) {

        if (digiprovReferenceAnchor == null) {
            this.digiprovReferenceAnchor = "";
        } else {
            this.digiprovReferenceAnchor = checkForRegExp(digiprovReferenceAnchor);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getDigiprovPresentationAnchor() {
        return this.digiprovPresentationAnchor;
    }

    /***************************************************************************
     * <p>
     * Set the DigiProv anchor presentation and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param digiprovPresentation
     **************************************************************************/
    public void setDigiprovPresentationAnchor(String digiprovPresentationAnchor) {

        if (digiprovPresentationAnchor == null) {
            this.digiprovPresentationAnchor = "";
        } else {
            this.digiprovPresentationAnchor = checkForRegExp(digiprovPresentationAnchor);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getPurlUrl() {
        return this.purlUrl;
    }

    /***************************************************************************
     * <p>
     * Set the PURL URL and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param purlUrl
     **************************************************************************/
    public void setPurlUrl(String purlUrl) {

        if (purlUrl == null) {
            this.purlUrl = "";
        } else {
            this.purlUrl = checkForRegExp(purlUrl);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getContentIDs() {
        return this.contentIDs;
    }

    /***************************************************************************
     * <p>
     * Set the content IDs and substitute, if some RegExps are contained in $REGEXP().
     * </p>
     * 
     * @param contentIDs
     **************************************************************************/
    public void setContentIDs(String contentIDs) {

        if (contentIDs == null) {
            this.contentIDs = "";
        } else {
            this.contentIDs = checkForRegExp(contentIDs);
        }
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public static String getVersion() {
        return VERSION;
    }

}
