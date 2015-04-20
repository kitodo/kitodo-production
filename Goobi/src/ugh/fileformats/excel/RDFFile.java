package ugh.fileformats.excel;

/*******************************************************************************
 * ugh.fileformats.excel / RDFFile.java
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.FileSet;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.dl.Reference;
import ugh.dl.RomanNumeral;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/*******************************************************************************
 * <p>
 * The RDFFile class allows to load and save a DigitalDocument in the GDZ
 * RDF/XML format. When reading and writing the files, this class makes some
 * assumptions:
 * 
 * <ul>
 * <li>The physical structure just consists only of the BoundBook with pages (as
 * children); there are no further hierarchical levels in the physical
 * structures.</li>
 * <li>References from logical structures to pages are stored in references of
 * the type "logical_physical"</li>
 * <li>The page numbers are stored in Metadata objects of the type
 * physPageNumber aner logicalPageNumber; Both MetadataTypes are created in this
 * class, if they are NOT available</li>
 * <li>Each page may just have one physPageNumber and one logicalPageNumber
 * metadata element</li>
 * </ul>
 * </p>
 * 
 * @author Markus Enders
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-18
 * @since 2004-05-21
 * 
 *        TODOLOG
 * 
 *        TODO read and write metadataGroups
 *        
 *        TODO read and write normdata
 * 
 *        TODO Use final strings for fixed XML strings and error messages!
 * 
 *        TODO Move this out of the excel package!
 * 
 *        TODO Get rid of deprecated Classes!! (Hm, we have here a deprecated
 *        class itself, so we maybe should leave it at is is!)
 * 
 *        CHANGELOG
 *        
 *        18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *        05.05.2010 --- Funk --- Just trying to solve some person and metadata
 *        reading bugs (see recent SLUB mails and DPD-408).
 * 
 *        15.03.2010 --- Funk --- Added displayName to writing persons method.
 * 
 *        10.03.2010 --- Funk --- Added FormatSourcePrint to <ImageSet> tag,
 *        will be created as metadata of BoundBook now. --- More persons are
 *        taken with displayname now.
 * 
 *        25.02.2010 --- Funk --- Added some more logging.
 * 
 *        15.02.2010 --- Funk --- Logging version information now.
 * 
 *        14.02.2010 --- Funk --- Commented the whitespace things, trim() added
 *        to method getMDValueOfNode(). --- Using HIDDEN_METADATA_CHAR now. ---
 *        Refactored some error message strings. -- Slightly refactored the
 *        displayName retrieval, text content of the metadata nodes now are
 *        retrieved by the method getMDValueOfNode() to get the trim()
 *        everywhere.
 * 
 *        03.02.2010 --- Funk --- Commmented out the whitespace diminishing in
 *        getMDValueOfNode() due to .
 * 
 *        26.01.2010 --- Funk --- Handling text in person tags without FirstName
 *        and LastName tags is put into displayName now.
 * 
 *        22.01.2010 --- Funk --- Handling text in person tags without FirstName
 *        and LastName tags as LastName. --- Some findbugs improvements.
 * 
 *        18.01.2010 --- Funk --- Adapted class to changed
 *        DocStruct.getAllMetadataByType().
 * 
 *        21.12.2009 --- Funk --- Added some "? extends " to metadata things.
 * 
 *        03.12.2009 --- Funk --- Slightly improved the person extraction from
 *        the RDF file.
 * 
 *        19.11.2009 --- Funk --- Improved class for Sonar.
 * 
 *        30.10.2009 --- Funk --- Improved XML date and RDFFile version comment.
 * 
 *        27.10.2009 --- Funk --- Changed the conditionals that caused DPD-359
 *        and DPD-361 from ">= -1" to "== -1". --- Removed debug output.
 * 
 *        09.10.2009 --- Funk --- Removed some of the deprecated anotations,
 *        only WRITE should be deprecated!
 * 
 *        05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *        30.09.2009 --- Funk --- Merged Wulf's thingsg into here.
 * 
 *        29.04.2008 --- Funk --- All whitespaces of text nodes now are replaced
 *        with a single space in getMDValueOfNode().
 * 
 ******************************************************************************/

@Deprecated
public class RDFFile implements ugh.dl.Fileformat {

	/***************************************************************************
	 * VERSION STRING
	 **************************************************************************/

	private static final String							VERSION						= "1.2-20100505";

	/***************************************************************************
	 * STATIC FINALS
	 **************************************************************************/

	private static final String							RDF_PREFS_NODE_NAME_STRING	= "RDF";
	private static final Logger							LOGGER						= Logger
																							.getLogger(ugh.dl.DigitalDocument.class);
	public static final short							ELEMENT_NODE				= 1;

	// UGH document.
	private ugh.dl.DigitalDocument						mydoc;
	// A list of all pages.
//	private List<DocStruct>								allPages;
	// Imageset.
	private ugh.dl.FileSet								myImageset;
	// General preferences.
	private final ugh.dl.Prefs								myPreferences;

//	private final boolean								exportable					= true;
//	private final boolean								importable					= true;
//	private final boolean								updateable					= false;

	// Hashtables are used for matching the internal Name of metadata and
	// docstructs to the name used in the rdf-xml file.
	// The contents is read from the preferences in readPrefs method.
	private final Hashtable<String, MatchingMetadataObject>	rdfNamesMD;
	private final Hashtable<String, MatchingMetadataObject>	rdfNamesDS;

	private static final String							HIDDEN_METADATA_CHAR		= "_";

	/***************************************************************************
	 * CONSTRUCTORS
	 **************************************************************************/

	/***************************************************************************
	 * @param inPrefs
	 * @throws PreferencesException
	 **************************************************************************/
	public RDFFile(ugh.dl.Prefs inPrefs) throws PreferencesException {

		this.myPreferences = inPrefs;
		this.rdfNamesMD = new Hashtable<String, MatchingMetadataObject>();
		this.rdfNamesDS = new Hashtable<String, MatchingMetadataObject>();

		// Read preferences.
		Node rdfNode = inPrefs.getPreferenceNode("RDF");
		if (rdfNode == null) {
			String message = "Can't read preferences for RDF fileformat! Node '"
					+ RDF_PREFS_NODE_NAME_STRING
					+ "' in preferences file not found!";
			PreferencesException pe = new PreferencesException(message);
			LOGGER.error(message, pe);
			throw pe;
		}

		this.readPrefs(rdfNode);
	}

	/***************************************************************************
	 * WHAT THE OBJECT DOES
	 **************************************************************************/

	/*
	 * Loads file and builds a DOM tree.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#read(java.lang.String)
	 */
	@Override
	public boolean read(String filename) throws ReadException {

		Document document;
		NodeList childlist;
		NodeList upperChildlist;
		Node upperchild;
		// Single node of the childlist.
		Node currentNode;
		// New document builder instance.
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Do not validate xml file.
		factory.setValidating(false);
		// Namespace does not matter.
		factory.setNamespaceAware(false);

		this.mydoc = new DigitalDocument();

		// Read RDF file and parse it.
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new File(filename));
		} catch (SAXParseException e) {
			String message = "Parse error at line " + e.getLineNumber()
					+ ", URI: " + e.getSystemId();
			LOGGER.error(message, e);
			throw new ReadException(message, e);
		} catch (SAXException e) {
			String message = "Can not create DOM tree!";
			LOGGER.error(message, e);
			throw new ReadException(message, e);
		} catch (ParserConfigurationException e) {
			String message = "XML parser not configured correctly!";
			LOGGER.error(message, e);
			throw new ReadException(message, e);
		} catch (IOException e) {
			String message = "Can not create DOM tree!";
			LOGGER.error(message, e);
			throw new ReadException(message, e);
		}

		// The file was parsed; DOM was created. Parse the DOM now.
		upperChildlist = document.getElementsByTagName("RDF:RDF");
		if (upperChildlist == null) {
			String message = "Wrong file type! No <RDF:RDF> element found!";
			ReadException re = new ReadException(message);
			LOGGER.error(message, re);
			throw re;
		}

		// Get first preferences element.
		upperchild = upperChildlist.item(0);
		if (upperchild == null) {
			return false;
		}

		// Try to find ImageSet.
		childlist = upperchild.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			currentNode = childlist.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			if (currentNode.getNodeName().equals("AGORA:ImageSet")) {
				try {
					if (!parseImageSet(currentNode)) {
						// Error occurred while reading imageset.
						String message = "Wrong file type! No <AGORA:ImageSet> element found!";
						ugh.exceptions.ReadException re = new ugh.exceptions.ReadException(
								message);
						LOGGER.error(message, re);
						throw re;
					}
					// Get out of loop.
					continue;
				} catch (TypeNotAllowedForParentException e) {
					String message = "DocStruct type is not allowed for parent DocStruct";
					LOGGER.error(message, e);
					throw new ReadException(message, e);
				} catch (MetadataTypeNotAllowedException e) {
					String message = "Metadata type is not allowed for current DocStruct";
					LOGGER.error(message, e);
					throw new ReadException(message, e);
				}
			}
		}

		LOGGER.debug("Loading logical structure entities");

		// Try to find DocStructs.
		childlist = upperchild.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			currentNode = childlist.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			if (currentNode.getNodeName().equals("AGORA:DocStrct")) {
				try {
					if (!parseAllDocStructs(currentNode)) {
						String message = "Can not read <AGORA:DocStruct>";
						LOGGER.error(message);
						throw new ReadException(message);
					}
					// Get out of loop.
					continue;
				} catch (TypeNotAllowedForParentException e) {
					String message = "DocStruct type is not allowed for parent DocStruct";
					LOGGER.error(message, e);
					throw new ReadException(message, e);
				} catch (TypeNotAllowedAsChildException e) {
					String message = "DocStruct type is not allowed as a child";
					LOGGER.error(message, e);
					throw new ReadException(message, e);
				} catch (MetadataTypeNotAllowedException e) {
					String message = "Metadata type is not allowed for current DocStruct";
					LOGGER.error(message, e);
					throw new ReadException(message, e);
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public static String getVersion() {
		return VERSION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#update(java.lang.String)
	 */
	@Override
	@Deprecated
	public boolean update(String filename) {
		return false;
	}

	/*
	 * Returns the digital document in which this file is loaded.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#getDigitalDocument()
	 */
	@Override
	public DigitalDocument getDigitalDocument() {
		return this.mydoc;
	}

	/***************************************************************************
	 * PRIVATE AND PROTECTED METHODS
	 **************************************************************************/

	/***************************************************************************
	 * <p>
	 * Reads all document structures; the first parameter is already the first
	 * document structure.
	 * </p>
	 * 
	 * @param inNode
	 * @return
	 * @throws TypeNotAllowedForParentException
	 * @throws MetadataTypeNotAllowedException
	 * @throws TypeNotAllowedAsChildException
	 **************************************************************************/
	private boolean parseAllDocStructs(Node inNode)
			throws TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

		// Read top structure entitiy.
		if (readDocStruct(inNode, null)) {
			LOGGER.debug("Parsing all docstructs complete");
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#write(java.lang.String)
	 */
	@Override
	@Deprecated
	public boolean write(String filename) throws WriteException {

		FileOutputStream xmlFile;

		// Get output stream.
		try {
			xmlFile = new FileOutputStream(filename);
		} catch (Exception e) {
			LOGGER.error("Can't write file '" + filename
					+ "'! System message: " + e.getMessage());
			return false;
		}

		try {
			// Find the implementation.
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document domdoc = builder.newDocument();

			// Create the document.
			String message = " This RDF file was created on "
					+ new java.util.Date()
					+ " using the UGH Metadata Library: "
					+ this.getClass().getCanonicalName() + " (version "
					+ VERSION + ") ";
			Comment comment = domdoc.createComment(message);
			domdoc.appendChild(comment);

			Element rdf = domdoc.createElement("RDF:RDF");
			rdf.setAttribute("xmlns:RDF", "http://www.w3c.org/RDF");
			rdf.setAttribute("xmlns:AGORA", "GDZ:DMSDB-Semantics");
			domdoc.appendChild(rdf);

			if (this.mydoc == null) {
				LOGGER.error("No DigitalDocument");
				xmlFile.close();
				return false;
			}

			if (this.mydoc.getLogicalDocStruct() == null) {
				LOGGER.error("No logical DocStruct");
				xmlFile.close();
				return false;
			}

			// Build all logical structures (fill the DOM tree).
			if (!writeDocStruct(rdf, this.mydoc.getLogicalDocStruct())) {
				// Error occurred while writing the RDF/XML file.
				LOGGER.error("Error occurred while writing logical docstruct");
				xmlFile.close();
				return false;
			}

			// Build all physical structures.
			if (!writePhysical(rdf)) {
				// Error occurred while writing the RDF/XML file.
				LOGGER.error("Error occurred while writing physical docstruct");
				xmlFile.close();
				return false;
			}

			// Serialize the document.
			OutputFormat format = new OutputFormat(domdoc);
			// Format must not be used; if indenting is set to true (default is
			// false), also the content of XML-tags (text values) will be
			// modified to look nicer - this means linebreaks and tabs are added
			// :-(
			// format.setLineWidth(10000);
			// format.setIndenting(true);
			// format.setIndent(4);

			// write it into file
			XMLSerializer serializer = new XMLSerializer(xmlFile, format);
			serializer.asDOMSerializer();
			serializer.serialize(domdoc);
			xmlFile.close();
		} catch (FactoryConfigurationError e) {
			String message = "Could not locate a JAXP factory class";
			LOGGER.error(message, e);
			return false;
		} catch (ParserConfigurationException e) {
			String message = "Could not locate a JAXP DocumentBuilder class";
			LOGGER.error(message, e);
			return false;
		} catch (DOMException e) {
			String message = "Error writing DOM tree";
			LOGGER.error(message, e);
			return false;
		} catch (IOException e) {
			String message = "Could not write file due to an IOException";
			LOGGER.error(message, e);
			return false;
		} finally {
			try {
				xmlFile.close();
			} catch (IOException e) {
				String message = "RDF file '" + filename
						+ "' could not be closed";
				LOGGER.error(message, e);
				throw new WriteException(message, e);
			}
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 * @throws TypeNotAllowedForParentException
	 * @throws ReadException
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private boolean parseImageSet(Node inNode)
			throws TypeNotAllowedForParentException, ReadException,
			MetadataTypeNotAllowedException {

		NodeList childlist;
		NodeList childlist2;
		NodeList childlist3;
		Node currentNode;
		Node currentNode2;
		Node currentNode3;
		// Top physical document structure for the book.
		DocStruct topdocstruct;
		DocStructType topdocstructtype;
		// Temporary Metadata object.
		Metadata tempMD;

		// Create a new ImageSet object, which contains all images, get Metadata
		// for Imageset.
		this.myImageset = new ugh.dl.FileSet();
		childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			currentNode = childlist.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			if (currentNode.getNodeName().equals("AGORA:MediumSource")) {
				String mediumsource = getMDValueOfNode(currentNode);

				// Check, if MetadataType for mediumsource is already available,
				// if not, add it.
				MetadataType myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("mediumsource") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences, create new metadata.
					myMDType = new MetadataType();
					myMDType.setName("mediumsource");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("mediumsource");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(mediumsource);
				this.myImageset.addMetadata(myMD);
			}

			// Check, if MetadataType for shelfmarksource is already available,
			// if not, add it.
			MetadataType myMDType = null;
			if (currentNode.getNodeName().equals("AGORA:ShelfmarkSource")) {
				String shelfmarksource = getMDValueOfNode(currentNode);

				if (this.myPreferences.getMetadataTypeByName("shelfmarksource") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences, create new metadata.
					myMDType = new MetadataType();
					myMDType.setName("shelfmarksource");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("shelfmarksource");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(shelfmarksource);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:ImageDescr")) {
				String imagedescr = getMDValueOfNode(currentNode);

				myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("imagedescr") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences, create new metadata.
					myMDType = new MetadataType();
					myMDType.setName("imagedescr");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("imagedescr");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(imagedescr);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:CommentSource")) {
				String commentsource = getMDValueOfNode(currentNode);

				// Create new metadata type for commentsource.
				myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("commentsource") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences, create new metadata.
					myMDType = new MetadataType();
					myMDType.setName("commentsource");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("commentsource");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(commentsource);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals(
					"AGORA:DateDigitizationImageSet")) {
				String datedigit = getMDValueOfNode(currentNode);

				// Create new metadata type for datedigit.
				myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("datedigit") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences, create new metadata.
					myMDType = new MetadataType();
					myMDType.setName("datedigit");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("datedigit");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(datedigit);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:PathImagefiles")) {
				String pathimagefiles = getMDValueOfNode(currentNode);

				// Create new metadata type for pathimagefiles.
				myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("pathimagefiles") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences.
					myMDType = new MetadataType();
					myMDType.setName("pathimagefiles");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("pathimagefiles");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(pathimagefiles);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:FormatSourcePrint")) {
				String formatsourceprint = getMDValueOfNode(currentNode);

				// Create new metadata type for pathimagefiles.
				myMDType = null;
				if (this.myPreferences
						.getMetadataTypeByName("FormatSourcePrint") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences.
					myMDType = new MetadataType();
					myMDType.setName("FormatSourcePrint");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("FormatSourcePrint");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(formatsourceprint);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:OriginatorImageSet")) {
				String originImageset = getMDValueOfNode(currentNode);

				// Create new metadata type for originImageSet.
				myMDType = null;
				if (this.myPreferences.getMetadataTypeByName("originImageSet") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences.
					myMDType = new MetadataType();
					myMDType.setName("originImageSet");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("originImageSet");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(originImageset);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals("AGORA:CopyrightImageSet")) {
				String copyrightimageset = getMDValueOfNode(currentNode);

				// Create new metadata type for mediumsource.
				myMDType = null;
				if (this.myPreferences
						.getMetadataTypeByName("copyrightimageset") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences.
					myMDType = new MetadataType();
					myMDType.setName("copyrightimageset");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("copyrightimageset");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(copyrightimageset);
				this.myImageset.addMetadata(myMD);
			}
			if (currentNode.getNodeName().equals(
					"AGORA:ShelfmarkArchiveImageSet")) {
				String shelfmarkarchiveimageset = getMDValueOfNode(currentNode);

				// Create new metadata type for shelfmarkarchiveimageset.
				myMDType = null;
				if (this.myPreferences
						.getMetadataTypeByName("shelfmarkarchiveimageset") == null) {
					// Not avialable, so add it to the metadatatypes in the
					// preferences.
					myMDType = new MetadataType();
					myMDType.setName("shelfmarkarchiveimageset");
					myMDType.setNum("1o");
					this.myPreferences.addMetadataType(myMDType);
				} else {
					myMDType = this.myPreferences
							.getMetadataTypeByName("shelfmarkarchiveimageset");
				}

				// Get new Metadata object.
				Metadata myMD = new Metadata(myMDType);
				myMD.setValue(shelfmarkarchiveimageset);
				this.myImageset.addMetadata(myMD);
			}
		}

		// Create top physical document structure containing the same
		// information as the imageset.
		topdocstructtype = this.myPreferences
				.getDocStrctTypeByName("BoundBook");
		topdocstruct = this.mydoc.createDocStruct(topdocstructtype);

		// Add all metadata from imageset to Bound book.
		List<Metadata> allImageSetMD = this.myImageset.getAllMetadata();
		for (int i = 0; i < allImageSetMD.size(); i++) {
			tempMD = allImageSetMD.get(i);
			try {
				if (!(topdocstruct.addMetadata(tempMD))) {
					LOGGER.debug("Can't add metadata for imageset");
				}
			} catch (MetadataTypeNotAllowedException mtnae) {
				LOGGER.debug("Can't add metadata for imageset");
			}
		}

		this.mydoc.setPhysicalDocStruct(topdocstruct);

		// Get pagination sequences and pages = create Physical document
		// structure.
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			currentNode = childlist.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			if (currentNode.getNodeName().equals("AGORA:SequencesPagination")) {

				// Get subelement to detect, if it is an uncouted or counted
				// sequence.
				childlist2 = currentNode.getChildNodes();
				for (int x = 0; x < childlist2.getLength(); x++) {
					// Get single node.
					currentNode2 = childlist2.item(x);
					if (currentNode2.getNodeType() != ELEMENT_NODE) {
						// It's not an element node; so next iteration.
						continue;
					}

					if (currentNode2.getNodeName().equals("RDF:Seq")) {
						// Get nodes of RDF:Li elements.
						childlist3 = currentNode2.getChildNodes();
						for (int y = 0; y < childlist3.getLength(); y++) {
							currentNode3 = childlist3.item(y);
							if (currentNode3.getNodeType() != ELEMENT_NODE) {
								continue;
							}

							if (((currentNode3.getNodeName()).equals("RDF:Li"))
									|| ((currentNode3.getNodeName())
											.equals("RDF:LI"))) {

								// That's it; found start of new sequence.
								// Reads information about sequence and creates
								// appropriate physical document strcuture
								// entities.
								readPagSequence(currentNode3);
							}
						}
					}
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Creates a DOM-tree for a single document structure entitiy and its
	 * children (containing all MEtadata) parameter: the document instance (from
	 * DOM) the DocStruct instance, which should be written to XML.
	 * </p>
	 * 
	 * @param parentElement
	 * @param inDocStruct
	 * @return
	 **************************************************************************/
	private boolean writeDocStruct(Element parentElement, DocStruct inDocStruct) {

		Element docStructElement;
		String typename;
		Document domdoc = parentElement.getOwnerDocument();
		List<Metadata> allmetadata;
		List<DocStruct> allchildren;
		List<Metadata> alreadyWritten = new LinkedList<Metadata>();

		// Create DocStruct element.
		docStructElement = domdoc.createElement("AGORA:DocStrct");
		if (inDocStruct == null) {
			LOGGER.debug("inDocStruct is null");
			return false;
		}
		typename = getRDFName(inDocStruct.getType());
		if (typename == null) {
			LOGGER.debug("RDF name for document structure '"
					+ inDocStruct.getType().getName() + "' unknown");
			return false;
		}
		docStructElement.setAttribute("AGORA:Type", typename);

		// Add all rdf:seq persons to this element.
		if (!writeRDFLIPerson(docStructElement, inDocStruct)) {
			LOGGER.debug("Error while writing RDF:Li for persons");
			return false;
		}

		// Add all rdf:seq metadata to this element.
		if (!writeRDFLIMetadata(docStructElement, inDocStruct)) {
			LOGGER.debug("Error while writing RDF:Li for metadata");
			return false;
		}

		// Add single metadata to this element.
		allmetadata = inDocStruct.getAllMetadata();
		if (allmetadata != null) {
			for (Metadata mymetadata : allmetadata) {
				MetadataType mymetadatatype = mymetadata.getType();

				// Check, if metadata is an RDF-sequence or RDF-bag, get
				// MetadataMatchingObject and check, if this metadata is member
				// of an rdfList. If rdfList is NOT null, than it is a member of
				// an rdf List.
				MatchingMetadataObject mmo = getMMOByName(mymetadatatype
						.getName());
				if (mmo == null) {
					// Not available; next one in loop.
					LOGGER.debug("Can't find RDF element for metadata '"
							+ mymetadatatype.getName() + "'");
					continue;
				}
				// Get name of RDFList.
				String rdfListName = mmo.getRDFList();
				if (rdfListName == null) {
					writeMetadata(docStructElement, mymetadata);
					// Add metadata to the list of already written metadata.
					alreadyWritten.add(mymetadata);
				}
			}
		}

		// Write pagenumbers / references to start and endpage only if the
		// current DocStruct is NOT an anchor.
		if (inDocStruct.getType().getAnchorClass() != null && inDocStruct.getParent() == null) {
			LOGGER.debug("Is anchor, do not write RefImageSetRange "
					+ inDocStruct.getType().getAnchorClass());
		} else {
			writeRefImageSetRange(inDocStruct, docStructElement);
		}

		// Add children to this element.
		allchildren = inDocStruct.getAllChildren();
		if (allchildren != null) {
			for (int i = 0; i < allchildren.size(); i++) {
				writeDocStruct(docStructElement, allchildren.get(i));
			}
		}

		// Append this docstruct to parent element.
		parentElement.appendChild(docStructElement);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Writes all physical document strcutres entities; calculates the
	 * pagination sequences and writes them.
	 * </p>
	 * 
	 * @param parentElement
	 * @return
	 * @throws WriteException
	 **************************************************************************/
	private boolean writePhysical(Element parentElement) throws WriteException {

		Document domdoc = parentElement.getOwnerDocument();
		List<PaginationSequence> allPaginationSequences = new LinkedList<PaginationSequence>();
		List<DocStruct> allPhysicalPages;
		List<Metadata> allMetadata;
		DocStruct page;
		DocStruct topdoc = this.mydoc.getPhysicalDocStruct();
		Metadata singleMetadata;
		String currentPhyicalPage = "";
		String currentLogicalPage = "";
		boolean currentCounted = false;
		boolean currentArabic = true;
		String lastPhysicalPage = "0";
		String lastLogicalPage = "";
		boolean lastCounted = false;
		boolean lastArabic = true;
		String expectedLogicalPage = "";
		// Counts number of page in sequence; only used for uncounted page.
		int uncountedNumber = 1;
		boolean newSequence = false;
		// <AGORA:ImageSet> element.
		Element imageSetElement;
		// Create DOM-elements for ImageSet.
		imageSetElement = domdoc.createElement("AGORA:ImageSet");
		parentElement.appendChild(imageSetElement);

		// Write additional information.
		Element tempElement = domdoc.createElement("AGORA:ImageSetDocSource");

		// Get content of CatalogIDDigital.
		MetadataType idType = this.myPreferences.getMetadataTypeByName(
				"AGORA:CatalogIDDigital", "rdf");
		if (idType != null) {
			List<? extends Metadata> allIDs = this.mydoc.getLogicalDocStruct()
					.getAllMetadataByType(idType);

			for (int x = 0; x < allIDs.size(); x++) {
				Metadata identifier = allIDs.get(x);
				String idvalue = identifier.getValue();
				if (idvalue != null) {
					idvalue = idvalue.substring(3, idvalue.length());
					tempElement.setAttribute("RDF:HREF", "#" + idvalue);
					imageSetElement.appendChild(tempElement);
					break;
				}
			}
		}

		// Write Metadata for topdoc.
		if (topdoc == null) {
			return true;
		}
		allMetadata = topdoc.getAllMetadata();
		if (allMetadata != null) {
			for (int i = 0; i < allMetadata.size(); i++) {
				singleMetadata = allMetadata.get(i);
				writeMetadata(imageSetElement, singleMetadata);
			}
		}

		// Count TOP document structures.
		//
		// Detect pagination sequences; find page numbers with different
		// pagination and split pages into pagination sequences.
		allPhysicalPages = topdoc.getAllChildren();
		// No pages, no pagination sequences.
		if (allPhysicalPages == null) {
			return true;
		}
		PaginationSequence currentPaginationSequence = new PaginationSequence(
				this.myPreferences);
		currentPaginationSequence.physicalstart = 1;

		for (int i = 0; i < allPhysicalPages.size(); i++) {
			page = allPhysicalPages.get(i);
			allMetadata = page.getAllMetadata();
			// No metadata available.
			if (allMetadata == null) {
				continue;
			}
			for (int x = 0; x < allMetadata.size(); x++) {
				singleMetadata = allMetadata.get(x);
				if (singleMetadata.getType().getName().equals("physPageNumber")) {
					currentPhyicalPage = singleMetadata.getValue();
				}
				if (singleMetadata.getType().getName().equals(
						"logicalPageNumber")) {
					currentLogicalPage = singleMetadata.getValue();
				}
			}

			// Check, if page is a counted or uncounted page.
			if (currentLogicalPage == null
					|| currentLogicalPage.equals("uncounted")) {
				currentCounted = false;
			} else {
				currentCounted = true;
			}

			// Check, if it's roman or arabic.
			if (currentCounted) {
				// Check only, if it's a counted page.
				currentArabic = false;
				try {
					RomanNumeral roman = new RomanNumeral();
					roman.setValue(currentLogicalPage);
				} catch (NumberFormatException nfe) {
					currentArabic = true;
				}
			}

			// Check, if we have a new sequence here:
			//
			// Check, if current_logpage is like the expected logpage; if not, a
			// new pagination sequence is generated.
			if (i == 0) {
				// It's the first page, so create new sequence.
				newSequence = true;
			} else {
				// Check, if (counted) page number changes from roman or arabic
				// (or vice versa).
				if ((currentArabic && !lastArabic)
						|| (!currentArabic && lastArabic)) {
					newSequence = true;
				}
				if (!newSequence && (currentCounted && !lastCounted)
						|| (!currentCounted && lastCounted)) {
					// Change from counted to uncounted sequence (or vice
					// versa).
					newSequence = true;
				}

				// Check, if (counted) page numbers are increasing (roman and
				// arabic).
				if (!newSequence && currentLogicalPage != null
						&& lastLogicalPage.compareTo("") != 0 && lastCounted
						&& currentCounted) {
					// Only if it's a counted page and we had a former logpage
					// (which was also counted).
					if (lastArabic) {
						expectedLogicalPage = String.valueOf(Integer
								.parseInt(lastLogicalPage) + 1);
					} else {
						RomanNumeral roman;
						try {
							roman = new RomanNumeral(lastLogicalPage);
						} catch (Exception e) {
							String message = "Problem converting value for roman numeral (lastlogpage) - (1)";
							LOGGER.error(message, e);
							throw new WriteException(message, e);
						}
						int introman = (roman.intValue()) + 1;
						roman = new RomanNumeral(introman);
						expectedLogicalPage = roman.toString();
					}
					if (currentLogicalPage.compareTo(expectedLogicalPage) != 0) {
						newSequence = true;
					}
				}
			}
			if (newSequence) {
				// It's a new sequence.
				//
				// Add information for former sequence (still the current
				// sequence).
				if (lastLogicalPage.compareTo("") != 0) {
					// Do it only, if a a last page is available (it means: this
					// must not be the first page).
					if (lastLogicalPage.equals("uncounted")) {
						// Last page was uncounted.
						currentPaginationSequence.logcountedend = 0;
						currentPaginationSequence.lognotcountedend = uncountedNumber;
						currentPaginationSequence.physicalend = Integer
								.parseInt(lastPhysicalPage);
					} else {
						// Counted page.
						if (lastArabic) {
							// An arabic number.
							currentPaginationSequence.logcountedend = Integer
									.parseInt(lastLogicalPage);
						} else {
							// A roman number.
							RomanNumeral roman = null;
							try {
								roman = new RomanNumeral(lastLogicalPage);
							} catch (Exception e) {
								String message = "Problem converting value for roman numeral (lastlogpage) - (1)";
								LOGGER.error(message, e);
								throw new WriteException(message, e);
							}

							int introman = roman.intValue();
							currentPaginationSequence.logcountedend = introman;
						}
						currentPaginationSequence.physicalend = Integer
								.parseInt(lastPhysicalPage);
					}
					// Add this sequence.
					allPaginationSequences.add(currentPaginationSequence);
				}

				// Create a new sequence.
				currentPaginationSequence = new PaginationSequence(
						this.myPreferences);
				uncountedNumber = 1;
				if (currentArabic) {
					currentPaginationSequence.pageformatnumber = "1";
				} else {
					currentPaginationSequence.pageformatnumber = "R";
				}
				if (currentLogicalPage == null
						|| currentLogicalPage.equals("uncounted")) {
					// Uncounted page.
					currentPaginationSequence.logcountedstart = 0;
					currentPaginationSequence.lognotcountedstart = 1;
					currentPaginationSequence.physicalstart = Integer
							.parseInt(currentPhyicalPage);
				} else {
					// Counted page.
					if (currentArabic) {
						// An arabic number.
						currentPaginationSequence.logcountedstart = Integer
								.parseInt(currentLogicalPage);
					} else {
						// A roman number.
						RomanNumeral roman = null;
						try {
							roman = new RomanNumeral(currentLogicalPage);
						} catch (Exception e) {
							String message = "Problem converting value for roman numeral (currentlogpage) - (3)";
							LOGGER.error(message, e);
							throw new WriteException(message, e);
						}
						int introman = roman.intValue();
						currentPaginationSequence.logcountedstart = introman;
					}
					currentPaginationSequence.physicalstart = Integer
							.parseInt(currentPhyicalPage);
				}
				newSequence = false;
			} else {
				// Count number in sequence.
				uncountedNumber++;
			}
			// Store values for next iteration.
			lastLogicalPage = currentLogicalPage;
			lastPhysicalPage = currentPhyicalPage;
			lastArabic = currentArabic;
			lastCounted = currentCounted;
		}

		// Add endpages to last sequence.
		if ((lastLogicalPage == null || lastLogicalPage.equals(""))
				|| (lastLogicalPage != null && lastLogicalPage
						.equals("uncounted"))) {
			// Last page was uncounted.,
			currentPaginationSequence.logcountedend = 0;
			currentPaginationSequence.lognotcountedend = uncountedNumber;
			currentPaginationSequence.physicalend = Integer
					.parseInt(lastPhysicalPage);
		} else {
			// Counted page.
			if (lastArabic) {
				// An arabic number.
				currentPaginationSequence.logcountedend = Integer
						.parseInt(lastLogicalPage);
			} else {
				// A roman number.
				RomanNumeral roman = null;
				try {
					roman = new RomanNumeral(lastLogicalPage);
				} catch (Exception e) {
					String message = "Problem converting value for roman numeral (currentlogpage) - (3)";
					LOGGER.error(message, e);
					throw new WriteException(message, e);
				}
				int introman = roman.intValue();
				currentPaginationSequence.logcountedend = introman;
			}
			currentPaginationSequence.physicalend = Integer
					.parseInt(lastPhysicalPage);
		}
		allPaginationSequences.add(currentPaginationSequence);

		// Now we have all pagination sequences; write a DOM tree for them.
		Element seqElement = domdoc.createElement("RDF:Seq");
		Element pagElement = domdoc.createElement("AGORA:SequencesPagination");
		Element liElement;
		Element pagNo;
		Element pagPhysStart;
		Element pagPhysEnd;
		Element pagLogCountStart;
		Element pagLogCountEnd;
		Element pagLogUncountStart;
		Element pagLogUncountEnd;
		Element pagFormat;
		Element formatSection;
		Element pageDelimiter;
		Node value;

		pagElement.appendChild(seqElement);
		imageSetElement.appendChild(pagElement);
		imageSetElement.setAttribute("ID", "Imageset001");

		for (int i = 0; i < allPaginationSequences.size(); i++) {
			currentPaginationSequence = allPaginationSequences.get(i);
			liElement = domdoc.createElement("RDF:Li");
			seqElement.appendChild(liElement);

			pagNo = domdoc.createElement("AGORA:SeqPaginNo");
			value = domdoc.createTextNode(String.valueOf(i + 1));
			pagNo.appendChild(value);

			pagPhysStart = domdoc.createElement("AGORA:PagePhysStart");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.physicalstart));
			pagPhysStart.appendChild(value);

			pagPhysEnd = domdoc.createElement("AGORA:PagePhysEnd");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.physicalend));
			pagPhysEnd.appendChild(value);

			pagLogCountStart = domdoc.createElement("AGORA:PageAccountedStart");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.logcountedstart));
			pagLogCountStart.appendChild(value);

			pagLogCountEnd = domdoc.createElement("AGORA:PageAccountedEnd");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.logcountedend));
			pagLogCountEnd.appendChild(value);

			pagLogUncountStart = domdoc
					.createElement("AGORA:PageNotAccountedStart");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.lognotcountedstart));

			pagLogUncountStart.appendChild(value);

			pagLogUncountEnd = domdoc
					.createElement("AGORA:PageNotAccountedEnd");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.lognotcountedend));

			pagLogUncountEnd.appendChild(value);

			pagFormat = domdoc.createElement("AGORA:FormatPageNumber");
			value = domdoc.createTextNode(String
					.valueOf(currentPaginationSequence.pageformatnumber));
			pagFormat.appendChild(value);

			formatSection = domdoc.createElement("AGORA:FormatSectionNumber");
			pageDelimiter = domdoc.createElement("AGORA:PaginDelimiter");
			liElement.appendChild(pagNo);
			liElement.appendChild(pagPhysStart);
			liElement.appendChild(pagPhysEnd);
			liElement.appendChild(pagLogCountStart);
			liElement.appendChild(pagLogCountEnd);
			liElement.appendChild(pagLogUncountStart);
			liElement.appendChild(pagLogUncountEnd);
			liElement.appendChild(pagFormat);
			liElement.appendChild(formatSection);
			liElement.appendChild(pageDelimiter);
		}
		return true;
	}

	/***************************************************************************
	 * <p>
	 * Writes a single metadata instance to XML.
	 * </p>
	 * 
	 * @param parentElement
	 * @param inMetadata
	 * @return
	 **************************************************************************/
	private boolean writeMetadata(Element parentElement, Metadata inMetadata) {

		String elementName;
		Element mdElement;
		Document domdoc = parentElement.getOwnerDocument();
		Node value;

		MetadataType inMDT = inMetadata.getType();
		if (inMDT != null) {
			if (inMDT.getName().substring(0, 1).equals(HIDDEN_METADATA_CHAR)) {
				// Get ot of method; it's an internal metadata type, we don't
				// write it, but it's okay.
				return true;
			}
		} else {
			return false;
		}

		MetadataType metadataType = inMetadata.getType();
		if (metadataType == null) {
			LOGGER.debug("Invalid metadata type");
			return false;
		}

		MatchingMetadataObject mmo = getMMOByName(metadataType.getName());
		// Metadata belongs to an RDFList; don't write it; this is handled by an
		// extra method WriteRDFLIMetadata.
		if (mmo != null && mmo.getRDFList() != null) {
			// Continue with next metadata.
			return true;
		}

		// Only one simple element.
		//	
		// Get name of XML-element.
		elementName = getRDFName(metadataType);
		if (elementName == null) {
			LOGGER.debug("Unknown XML-element name for metadata '"
					+ metadataType.getName() + "'");
			return false;
		}

		// Check if this metadata is an RDF:Li or not.
		mdElement = domdoc.createElement(elementName);
		if (inMetadata.getValue() != null) {
			// It's not an RDF_Li.
			value = domdoc.createTextNode(inMetadata.getValue());
			mdElement.appendChild(value);
			// Add metadata element to DOM tree.
			parentElement.appendChild(mdElement);
		}

		// If the following line is uncommented, we will write (empty) elements
		// for every single metadata.

		// Add metadata element to DOM tree.
		parentElement.appendChild(mdElement);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Writes all Metadata being in an RDF-List /RDF:Li.
	 * </p>
	 * 
	 * @param parentElement
	 *            XML-Element in DOM-Tree under which the RDF:Li shall be
	 *            wirtten
	 * @param ds
	 *            DocStruct Object whose metadata will be written
	 * @return true, if write performance was successful
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private boolean writeRDFLIMetadata(Element parentElement, DocStruct ds) {

		// List of all metadata RDF:List.
		HashMap<String, List<Metadata>> allRDFLIs = new HashMap<String, List<Metadata>>();
		// Get XML-document.
		Document domdoc = parentElement.getOwnerDocument();

		List<Metadata> ll = ds.getAllMetadata();
		if (ll == null) {
			return true;
		}

		// Get all metadata and order them.
		Iterator<Metadata> it = ll.iterator();

		while (it.hasNext()) {
			Metadata md = it.next();
			MetadataType mdt = md.getType();

			// Check, if metadata type of this name belongs to a RDF:Li group.
			MatchingMetadataObject mmo = getMMOByName(mdt.getName());
			if (mmo == null) {
				LOGGER.warn("No RDF-element found for metadata '"
						+ mdt.getName() + "'");
				continue;
			}
			if (mmo.getRDFList() != null) {
				// Get name of list.
				String rdflistname = mmo.getRDFList();
				if (allRDFLIs.containsKey(rdflistname)) {
					// Key already available, list exists just add this
					// metadata.
					List<Metadata> mdlist = allRDFLIs.get(rdflistname);
					mdlist.add(md);
				} else {
					// Key is not available; so there is no list with this name,
					// create one.
					List<Metadata> mdlist = new LinkedList<Metadata>();
					// Add metadata to this list.
					mdlist.add(md);
					allRDFLIs.put(rdflistname, mdlist);
				}
			}
		}

		// Finally we have read all metadata objects.
		//
		// Now get all lists and write them metadata which are not included in a
		// list, will not be written.
		Set<String> keys = allRDFLIs.keySet();
		Iterator<String> it2 = keys.iterator();
		// Iterator over all RDFLists.
		while (it2.hasNext()) {
			// Get name of list.
			String listname = it2.next();
			// Get a linked list with all metadata for this list.
			List<?> mds = allRDFLIs.get(listname);

			if (mds == null || mds.size() == 0) {
				// List is empty - however this happens.
				continue;
			}

			Element listElement = domdoc.createElement(listname);
			Element seqElement = null;

			// Write XML elements around. Decide when it is RDF:Seq or RDF:Bag.
			//
			// Iterate over metadata to find the ListType from a metadata.
			Iterator<Metadata> it4 = (Iterator<Metadata>) mds.iterator();
			while (it4.hasNext()) {
				Metadata md = it4.next();
				// Get internal metadata name.
				String mdtname = md.getType().getName();
				MatchingMetadataObject mmo = getMMOByName(mdtname);
				String listtype = mmo.getRdfListType();
				if ((listtype != null) && (listtype.equalsIgnoreCase("bag"))) {
					seqElement = domdoc.createElement("RDF:Bag");
				} else {
					seqElement = domdoc.createElement("RDF:Seq");
				}
				// Get out of loop.
				break;
			}

			// Add list elements and than add metadata to the <RDF:Seq> or
			// <RDF:Bag> element.
			listElement.appendChild(seqElement);
			parentElement.appendChild(listElement);

			Iterator<Metadata> it3 = (Iterator<Metadata>) mds.iterator();
			// Iterate over the lists metadata.
			while (it3.hasNext()) {
				Metadata md = it3.next();
				Element liElement = domdoc.createElement("RDF:Li");
				seqElement.appendChild(liElement);

				// Get element name for this metadata type, get internal
				// metadata name.
				String mdtname = md.getType().getName();
				MatchingMetadataObject mmo = getMMOByName(mdtname);
				if (mmo == null) {
					LOGGER.error("No RDF name available for metadata '"
							+ mdtname + "'");
					// Get out of loop.
					continue;
				}
				// Get name for the RDF-Element.
				String rdfname = mmo.getRDFName();
				Element mdElement = domdoc.createElement(rdfname);
				liElement.appendChild(mdElement);

				if (md.getValue() != null) {
					Node value = domdoc.createTextNode(md.getValue());
					mdElement.appendChild(value);
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Writes all Metadata being in an RDF-List /RDF:Li
	 * </p>
	 * 
	 * @param parentElement
	 *            XML-Element in DOM-Tree under which the RDF:Li shall be
	 *            wirtten
	 * @param ds
	 *            DocStruct Object whose metadata will be written
	 * @return true, if RDF:Li element and underlying name elements for the
	 *         person had been written successfully.
	 **************************************************************************/
	private boolean writeRDFLIPerson(Element parentElement, DocStruct ds) {

		// List of all metadata RDF:List.
		HashMap<String, List<Person>> allRDFLIs = new HashMap<String, List<Person>>();
		// List contains the top elements for the appropriate metadata.
		HashMap<String, String> allRDF = new HashMap<String, String>();
		// Get XML-document.
		Document domdoc = parentElement.getOwnerDocument();

		// Get list of all persons.
		List<Person> ll = ds.getAllPersons();
		if (ll == null) {
			return true;
		}

		// Get all metadata and order them.
		Iterator<Person> it = ll.iterator();

		while (it.hasNext()) {
			Person ps = it.next();
			MetadataType pst = ps.getType();

			// Check, if metadata type of this name belongs to a RDF:Li group.
			if (pst == null) {
				// No Metadatatype was found. Check, if type is set; if not; set
				// a new type based on the role.
				String role = ps.getRole();
				if (role == null) {
					LOGGER
							.error("Neither type nor role set; can't write person");
					continue;
				}
				pst = this.myPreferences.getMetadataTypeByName(ps.getRole());
				if (pst == null) {
					LOGGER
							.error("Role cannot be found in MetadataType; can't write person");
					continue;
				}
				ps.setType(pst);
			}
			String pstname = pst.getName();
			MatchingMetadataObject mmo = getMMOByName(pstname);
			if (mmo == null) {
				LOGGER.error("No mapping found found for person type '"
						+ pstname + "'");
				continue;
			}
			if (mmo.getRDFList() != null) {
				// Get name of list.
				String rdflistname = mmo.getRDFList();
				if (allRDFLIs.containsKey(rdflistname)) {
					// Key already available, list exists just add this
					// metadata.
					List<Person> mdlist = allRDFLIs.get(rdflistname);
					mdlist.add(ps);
				} else {
					// Key is not available; so there is no list with this name,
					// create one.
					List<Person> mdlist = new LinkedList<Person>();
					// Add metadata to this list.
					mdlist.add(ps);
					allRDFLIs.put(rdflistname, mdlist);
					allRDF.put(rdflistname, rdflistname);
				}
			}
		}

		// Finally we have read all person objects now get all lists and write
		// them. Persons which are not included in a list, will not be written.

		// Get a collection of all keys.
		// Collection coll=allRDFLIs.values();
		// Get all keys.
		Set<String> keys = allRDFLIs.keySet();

		Iterator<String> it2 = keys.iterator();
		// Iterator over all RDFLists.
		while (it2.hasNext()) {
			// Get name of list.
			String listname = it2.next();
			// Get a linked list with all metadata for this key.
			List<?> mds = allRDFLIs.get(listname);
			if (mds == null || mds.size() == 0) {
				// List is empty - however this happens.
				continue;
			}
			// Write XML elements around.
			Element listElement = domdoc.createElement(listname);
			Element seqElement = domdoc.createElement("RDF:Seq");
			listElement.appendChild(seqElement);
			parentElement.appendChild(listElement);

			Iterator<Metadata> it3 = (Iterator<Metadata>) mds.iterator();
			// Iterate over the lists metadata.
			while (it3.hasNext()) {
				Person ps = (Person) it3.next();
				Element liElement = domdoc.createElement("RDF:Li");
				seqElement.appendChild(liElement);

				// Get element name for this metadata type, get internal
				// metadata name.
				String mdtname = ps.getType().getName();
				MatchingMetadataObject mmo = getMMOByName(mdtname);
				if (mmo == null) {
					LOGGER.error("No RDF name available for metadata '"
							+ mdtname + "'");
					// Get out of loop.
					continue;
				}
				// Gget name for the RDF-Element.
				String rdfname = mmo.getRDFName();
				Element mdElement = domdoc.createElement(rdfname);
				liElement.appendChild(mdElement);

				// Check, if metadata has external identifier; if so, write
				// value in ID field.
			

				Element lastnameElement = domdoc
						.createElement("AGORA:CreatorLastName");
				Element firstnameElement = domdoc
						.createElement("AGORA:CreatorFirstName");
				Element displaynameElement = domdoc
						.createElement("AGORA:DisplayName");
				mdElement.appendChild(lastnameElement);
				mdElement.appendChild(firstnameElement);
				mdElement.appendChild(displaynameElement);

				if (ps.getFirstname() != null) {
					Node value = domdoc.createTextNode(ps.getFirstname());
					firstnameElement.appendChild(value);
				}
				if (ps.getLastname() != null) {
					Node value = domdoc.createTextNode(ps.getLastname());
					lastnameElement.appendChild(value);
				}
				if (ps.getDisplayname() != null) {
					Node value = domdoc.createTextNode(ps.getDisplayname());
					displaynameElement.appendChild(value);
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * @param inDoc
	 * @param parentElement
	 * @return true, if imageSetRange element was written successfully.
	 **************************************************************************/
	private boolean writeRefImageSetRange(DocStruct inDoc, Element parentElement) {

		// Minimal page number.
		int min = 9999;
		// Maximum page number.
		int max = 0;
		DocStruct singlepage;
		Reference singleref;
		// Metadata type for a page.
		MetadataType mdtype;
		Metadata singlemd;
		String pagenumber;
		int pagenumberint;
		Document document;
		List<?> allRefs;
		boolean refsAreRefs = true;

		mdtype = this.myPreferences.getMetadataTypeByName("physPageNumber");
		allRefs = inDoc.getAllReferences("to");

		// Check, if it's a BoundBook or a page.
		if (allRefs == null || allRefs.size() == 0) {
			return false;
		}
		// Get first reference.
		singleref = (Reference) allRefs.get(0);
		// Get first DocStruct.
		DocStruct firstdoc = singleref.getTarget();

		if (firstdoc.getType().getName().equals("BoundBook")) {
			// It's not a page - we have.
			allRefs = firstdoc.getAllChildren();
			// BoundBook has no children.
			if (allRefs == null || allRefs.size() == 0) {
				return false;
			}
			refsAreRefs = false;
		} else {
			allRefs = inDoc.getAllReferences("to");
		}

		for (int i = 0; i < allRefs.size(); i++) {
			if (refsAreRefs) {
				// allRefs contains references.
				singleref = (Reference) allRefs.get(i);
				singlepage = singleref.getTarget();
			} else {
				// allRefs contains DocStruct objects.
				singlepage = (DocStruct) allRefs.get(i);
			}
			// it's a page.
			List<? extends Metadata> allMDs = singlepage
					.getAllMetadataByType(mdtype);
			if (allMDs.isEmpty()) {
				// Get next reference.
				continue;
			}
			// Usually this should be the case; there must be one metadata of
			// the type physPageNumber.
			if (allMDs.size() > 0) {
				// There can only be one metadata;
				singlemd = allMDs.get(0);
				pagenumber = singlemd.getValue();
				// Convert to intger; pagenumber is always an integer.
				pagenumberint = Integer.parseInt(pagenumber);
				if (pagenumberint < min) {
					min = pagenumberint;
				}
				if (pagenumberint > max) {
					max = pagenumberint;
				}
			}
		}

		// Now max and min pagenumbers are available; write xml elements.
		document = parentElement.getOwnerDocument();
		Element refrangeElement = document
				.createElement("AGORA:RefImageSetRange");
		parentElement.appendChild(refrangeElement);
		Element refimagesetElement = document
				.createElement("AGORA:RefImageSet");
		refimagesetElement.setAttribute("RDF:HREF", "#Imageset001");
		refrangeElement.appendChild(refimagesetElement);
		Element refStart = document.createElement("AGORA:ImageSetPageStart");
		refrangeElement.appendChild(refStart);
		Element refEnd = document.createElement("AGORA:ImageSetPageEnd");
		refrangeElement.appendChild(refEnd);

		Node valuestart = document.createTextNode(Integer.toString(min));
		Node valueend = document.createTextNode(Integer.toString(max));
		refStart.appendChild(valuestart);
		refEnd.appendChild(valueend);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Reads a logical document structure with all metadata and tries to read
	 * all children.
	 * </p>
	 * 
	 * @param inNode
	 * @param inStruct
	 * @return
	 * @throws TypeNotAllowedForParentException
	 * @throws MetadataTypeNotAllowedException
	 * @throws TypeNotAllowedAsChildException
	 **************************************************************************/
	private boolean readDocStruct(Node inNode, DocStruct inStruct)
			throws TypeNotAllowedForParentException,
			MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {

		NodeList childNodes;
		Node currentNode;
		DocStructType docStructType;
		MetadataType metadataType;
		NamedNodeMap attributes;
		String attributeValue;
		DocStruct docStruct = null;
		List<MetadataType> subTypeList;

		// Get NodeName and check, if it's a known DocumentStructure type.
		short nodeType = inNode.getNodeType();
		if (nodeType != ELEMENT_NODE) {
			return false;
		}

		// Get type of structure from Attribute.
		attributes = inNode.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node currentAttribute = attributes.item(i);
			if (currentAttribute.getNodeName().equals("AGORA:Type")) {
				attributeValue = currentAttribute.getNodeValue();

				LOGGER.debug("Found AGORA DocStruct '" + attributeValue + "'");

				docStructType = getDSTypeByName(attributeValue);
				if (docStructType == null) {
					return false;
				}
				// Document structure type is known, create DocStruct.
				docStruct = this.mydoc.createDocStruct(docStructType);
				if (inStruct != null) {
					try {
						// Set this element as child of inDocStruct.
						inStruct.addChild(docStruct);
					} catch (TypeNotAllowedAsChildException tnaace) {
						// Can't add child; type is not allowed for inStruct to
						// be added.
						String message = "Can't add child to '"
								+ inStruct.getType().getName() + "'";
						LOGGER.error(message);
						throw new TypeNotAllowedAsChildException(inStruct
								.getType());
					}
				} else {
					this.mydoc.setLogicalDocStruct(docStruct);
				}

				// Get out of loop.
				break;
			}
		}

		// Get all child nodes.
		childNodes = inNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			// Get single node.
			currentNode = childNodes.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			// It's an element node;
			if (currentNode.getNodeName().equals("AGORA:DocStrct")) {
				// Child document structure was found.
				readDocStruct(currentNode, docStruct);
				continue;
			}

			// Reference to start/endpages.
			//	    
			// Child document structure was found.
			if (currentNode.getNodeName().equals("AGORA:RefImageSetRange")) {
				readRefImageSetRange(currentNode, docStruct);
				continue;
			}
			String nodename = currentNode.getNodeName();

			// Check, if the node is an rdf-sequence.
			if (isRDFList(nodename)) {
				// if((mymetadatatype.getName().equals("allDigCollections"))||
				// (mymetadatatype.getName().equals("AllPlacesOfPublication"))||
				// (mymetadatatype.getName().equals("ListallCreators"))){
				// it's an rdf group (sequence or bag)

				// Get all MetadataMatchingObjects for this list.
				List<MatchingMetadataObject> members = getRDFListMembers(nodename);
				if (members == null || members.size() == 0) {
					LOGGER.error("RDFList has no member types!");
				}
				// This list contain the RDF-XML names.
				subTypeList = new LinkedList<MetadataType>();

				// Get RDF-XML names from members and their MetadataType.
				//
				// MetadataTypes for this list are stored in subTypeList.
				Iterator<MatchingMetadataObject> it = members.iterator();
				while (it.hasNext()) {
					MatchingMetadataObject mmo = it.next();
					String rdfname = mmo.getRDFName();
					// Get metadata type.
					metadataType = getMDTypeByName(rdfname);
					if (metadataType == null) {
						LOGGER.error("Unknown Metadatatype '" + nodename
								+ "' for RDFList '" + nodename + "'!");

						// Read next xml-node.
						continue;
					}

					subTypeList.add(metadataType);
				}

				// Read the nodes of the list; only nodes which are in the
				// subTypeList are recognized.
				if (subTypeList == null || subTypeList.size() == 0) {
					continue;
				}
				// Search for it; all Metadata elements are returned in a list.
				List<Metadata> allMDs = readRDFSeq(currentNode, subTypeList);
				for (int a = 0; a < allMDs.size(); a++) {
					// Check if it's a person or normal metadata.
					Object singleObj = allMDs.get(a);
					if (singleObj.getClass().getName()
							.equals("ugh.dl.Metadata")) {
						// It's metadata, so add it to Metadata.
						Metadata singleMD = (Metadata) singleObj;
						try {
							// Add Metadata to the document structure.
							docStruct.addMetadata(singleMD);
							LOGGER.debug("Added metadata '"
									+ singleMD.getType().getName()
									+ "' with value '" + singleMD.getValue()
									+ "' to DocStruct '"
									+ docStruct.getType().getName() + "'");
						} catch (MetadataTypeNotAllowedException e) {
							String message = "Metadata '"
									+ singleMD.getType().getName()
									+ "' can not be added to DocStruct '"
									+ docStruct.getType().getName() + "'";
							LOGGER.error(message, e);
							throw new MetadataTypeNotAllowedException(singleMD
									.getType(), docStruct.getType());
						}
					}

					if (singleObj.getClass().getName().equals("ugh.dl.Person")) {
						// It's a person, so add it to PersonList.
						Person singlePer = (Person) singleObj;
						// Gets global type.
						try {
							docStruct.addPerson(singlePer);
						} catch (MetadataTypeNotAllowedException e) {
							String message = "Person '"
									+ singlePer.getType().getName()
									+ "' can not be added to DocStruct '"
									+ docStruct.getType().getName() + "'";
							LOGGER.error(message, e);
							throw new MetadataTypeNotAllowedException(message,
									e);
						}
					}
				}
			} else {
				// It is not an RDF-List but maybe a single metadata or person.
				// 
				// Get internal metadata name from MMO.
				metadataType = getMDTypeByName(nodename);

				// Check, if metadata type is known.
				String metadataValue = getMDValueOfNode(currentNode);
				if (metadataType == null) {
					LOGGER.warn("Unknown metadata or person type '" + nodename
							+ "'");
					// Read next xml-node.
					continue;
				}

				// Read value and add it to document structure (as person or
				// metadata), but only if we have content.
				if (metadataValue != null) {

					// System.out.println("METADATA VALUE: " + metadataValue);
					// System.out.println("METADATA TYPE: "
					// + metadataType.getName());
					// System.out.println("METADATA TYPE IS PERSON: "
					// + metadataType.getIsPerson());
					//
					// if (metadataType.getIsPerson()) {
					// Person newPerson = new Person(metadataType);
					// newPerson.setDisplayname(metadataValue);
					// newPerson.setValue(metadataValue);
					//
					// System.out.println("PERSON: "
					// + newPerson.getType().getName());
					// System.out.println("PERSON: "
					// + newPerson.getDisplayname());
					//
					// try {
					// docStruct.addPerson(newPerson);
					// LOGGER.debug("Added person '"
					// + newPerson.getType().getName()
					// + "' with displayName '"
					// + newPerson.getDisplayname()
					// + "' to DocStruct '"
					// + docStruct.getType().getName() + "'");
					// } catch (MetadataTypeNotAllowedException e) {
					// String message = "Person '"
					// + newPerson.getType().getName()
					// + "' can not be added to DocStruct '"
					// + docStruct.getType().getName() + "'";
					// LOGGER.error(message, e);
					// throw new MetadataTypeNotAllowedException(message,
					// e);
					// }
					// } else {
					Metadata newMetadata = new Metadata(metadataType);
					newMetadata.setValue(metadataValue);
					try {
						docStruct.addMetadata(newMetadata);
						LOGGER.debug("Added metadata '"
								+ newMetadata.getType().getName()
								+ "' with value '" + newMetadata.getValue()
								+ "' to DocStruct '"
								+ docStruct.getType().getName() + "'");
					} catch (MetadataTypeNotAllowedException e) {
						String message = "Metadata '"
								+ newMetadata.getType().getName()
								+ "' can not be added to DocStruct '"
								+ docStruct.getType().getName() + "'";
						LOGGER.error(message, e);
						throw new MetadataTypeNotAllowedException(message, e);
					}
					// }

					// We do not want to change the metadata value, we just want
					// to remove leading and following whitespaces! We do that
					// already in method getMDValueOfNode(), so take it out
					// here!
					//
					// TODO Make the whitespace handling configurable!!
					//
					// Parse metadata and delete some characters as tabstops and
					// newlines etc. The occurrence of several spaces are
					// replaced by a single space character.
					// metadataValue = metadataValue
					// .replaceAll("[\n\t\b\f\r]", "");
					// metadataValue = metadataValue.replaceAll("[ ]{2,}", " ")
				}
				// If the value of the metadata is NULL or empty, do log a
				// warning.
				else {
					LOGGER.warn("Metadata or person of type '"
							+ metadataType.getName()
							+ "' has no value! It was not added to DocStruct '"
							+ docStruct.getType().getName() + "'!");
				}
			}
		}

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Reads the start and end pages.
	 * </p>
	 * 
	 * @param inNode
	 *            Node in DOM tree
	 * @param mydocstruct
	 *            logical structure entity...
	 * @return
	 **************************************************************************/
	private boolean readRefImageSetRange(Node inNode, DocStruct mydocstruct) {

		NodeList childnodes;
		Node currentNode;
		int physstartpage = -1;
		int physendpage = -1;

		childnodes = inNode.getChildNodes();
		for (int i = 0; i < childnodes.getLength(); i++) {
			// Get single node.
			currentNode = childnodes.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			if (currentNode.getNodeName().equals("AGORA:ImageSetPageStart")) {
				try {
					physstartpage = Integer
							.parseInt(getMDValueOfNode(currentNode));
				} catch (NumberFormatException nfe) {
					// Not an integer value.
					return false;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:ImageSetPageEnd")) {
				try {
					physendpage = Integer
							.parseInt(getMDValueOfNode(currentNode));
				} catch (NumberFormatException nfe) {
					// Not an integer value.
					return false;
				}
			}
		}

		if (physstartpage < 0 || physendpage < 0) {
			// Start or endpage wasn't available.
			return false;
		}
		if (physendpage < physstartpage) {
			return false;
		}

		// Build references; one for each page to the logical docstruct.
		//
		// Get all pages.
		List<DocStruct> allChildren = this.mydoc.getPhysicalDocStruct()
				.getAllChildrenByTypeAndMetadataType("page", "physPageNumber");
		MetadataType myMDType = this.myPreferences
				.getMetadataTypeByName("physPageNumber");
		for (int i = physstartpage; i < physendpage + 1; i++) {
			for (int x = 0; x < allChildren.size(); x++) {
				String checkvalue = Integer.toString(i);
				// Currentchild is the page.
				DocStruct currentphyschild = allChildren.get(x);
				List<? extends Metadata> allMyMD = currentphyschild
						.getAllMetadataByType(myMDType);
				// Problem; there seem to be two "page"-metadata being
				// available.
				if (allMyMD.size() > 1) {
					return false;
				}
				Metadata myMD = allMyMD.get(0);
				String myMDvalue = myMD.getValue();
				if (checkvalue.equals(myMDvalue)) {
					// This is one; so create a new reference from logical to
					// physical document structure.
					if (!(mydocstruct.getType().isTopmost())) {
						mydocstruct.addReferenceTo(currentphyschild,
								"logical_physical");
					} else {
						// Set a single reference to the boundbook (physical
						// struct).
						List<Reference> refs = mydocstruct
								.getAllReferences("to");
						if (refs.size() == 0) {
							// No references set, so set one to the bound book.
							DocStruct topphys = this.mydoc
									.getPhysicalDocStruct();
							mydocstruct.addReferenceTo(topphys,
									"logical_physical");
						}
					}

					// Jump to next page.
					break;
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ugh.dl.Fileformat#setDigitalDocument(ugh.dl.DigitalDocument)
	 */
	@Override
	public boolean setDigitalDocument(DigitalDocument inDoc) {
		this.mydoc = inDoc;
		return true;
	}

	/***************************************************************************
	 * <p>
	 * Reads pagination sequence form file; adds appropriate document
	 * structures; one for each page.
	 * </p>
	 * 
	 * @param inNode
	 * @return
	 * @throws ReadException
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private boolean readPagSequence(Node inNode) throws ReadException,
			MetadataTypeNotAllowedException {

		NodeList childnodes;
		Node currentNode;
		LinkedList<DocStruct> allpages;
		DocStruct boundbook;
		int logcountedstart = 0;
		int logcountedend = 0;
		int lognotcountedstart = 0;
		int lognotcountedend = 0;
		int physicalstart = 0;
		int physicalend = 0;
		String pageformatnumber = null;
		// Object to present a pagination sequence.
		PaginationSequence pagesequence;
		// Single page (physical DocStruct entitiy).
		DocStruct currentPage;
		childnodes = inNode.getChildNodes();

		for (int i = 0; i < childnodes.getLength(); i++) {
			// Get single node.
			currentNode = childnodes.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}

			// Get logical and pyhsical page numbers of sequence.
			if (currentNode.getNodeName().equals("AGORA:PagePhysStart")) {
				try {
					physicalstart = Integer
							.parseInt(getMDValueOfNode(currentNode));
				} catch (NumberFormatException nfe) {
					// Not an integer value.
					return false;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:PagePhysEnd")) {
				try {
					physicalend = Integer
							.parseInt(getMDValueOfNode(currentNode));
				} catch (NumberFormatException nfe) {
					// Not an integer value.
					return false;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:PageAccountedStart")) {
				try {
					logcountedstart = Integer
							.parseInt(getMDValueOfNode(currentNode));
					if (logcountedstart == -1) {
						LOGGER
								.debug("Case of PageAccountedStart == -1 found and corrected (set to 0)");
						logcountedstart = 0;
					}
				} catch (NumberFormatException nfe) {
					// Not an integer.
					logcountedstart = 0;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:PageAccountedEnd")) {
				try {
					logcountedend = Integer
							.parseInt(getMDValueOfNode(currentNode));
					if (logcountedend == -1) {
						LOGGER
								.debug("Case of PageAccountedEnd == -1 found and corrected (set to 0)");
						logcountedend = 0;
					}
				} catch (NumberFormatException nfe) {
					// Not an integer.
					logcountedend = 0;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:PageNotAccountedStart")) {
				try {
					lognotcountedstart = Integer
							.parseInt(getMDValueOfNode(currentNode));
					if (lognotcountedstart == -1) {
						LOGGER
								.debug("Case of PageNotAccountedStart == -1 found and corrected (set to 0)");
						lognotcountedstart = 0;
					}
				} catch (NumberFormatException nfe) {
					// Not an integer.
					lognotcountedstart = 0;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:PageNotAccountedEnd")) {
				try {
					lognotcountedend = Integer
							.parseInt(getMDValueOfNode(currentNode));
					if (lognotcountedend >= -1) {
						LOGGER
								.debug("Case of PageNotAccountedEnd == -1 found and corrected (set to 0)");
						lognotcountedend = 0;
					}
				} catch (NumberFormatException nfe) {
					// Not an integer.
					lognotcountedend = 0;
				}
			}
			if (currentNode.getNodeName().equals("AGORA:FormatPageNumber")) {
				pageformatnumber = (getMDValueOfNode(currentNode));
			}
		}

		// Check, if AGORA:PagePhysStart und AGORA:PagePhysEnd were read.
		if (physicalstart == 0 && physicalend == 0) {
			// No phyiscalstart and no physicalend had been read!
			return false;
		}

		// All informtaion about pagination sequence is now read; create
		// pagseq-instance.
		pagesequence = new PaginationSequence(this.myPreferences);
		pagesequence.physicalstart = physicalstart;
		pagesequence.physicalend = physicalend;
		if (logcountedstart != 0 && logcountedend != 0) {
			pagesequence.logcountedstart = logcountedstart;
			pagesequence.logcountedend = logcountedend;
		}
		if (lognotcountedstart != 0 && lognotcountedend != 0) {
			pagesequence.lognotcountedstart = lognotcountedstart;
			pagesequence.lognotcountedend = lognotcountedend;
		}
		if (pageformatnumber != null) {
			pagesequence.pageformatnumber = pageformatnumber;
		}

		// Convert pagseq-object in several document structure objects.
		allpages = pagesequence.ConvertToPhysicalStructure(this.mydoc);

		// Add LinkedList to all physical Document structures.
		if (this.mydoc == null) {
			return false;
		}

		// Get top physical document structure.
		boundbook = this.mydoc.getPhysicalDocStruct();
		if (boundbook == null) {
			ugh.dl.DocStructType imagesettype = this.myPreferences
					.getDocStrctTypeByName("imageset");
			try {
				boundbook = this.mydoc.createDocStruct(imagesettype);
			} catch (TypeNotAllowedForParentException e) {
				String message = "Can't create BoundBook!";
				LOGGER.error(message, e);
				throw new ReadException(message, e);
			}
		}
		if (allpages == null) {
			// No pages available.
			return false;
		}

		// Handle imageset.
		//
		// Get MetadataTypes for path of Imageset and Imagesfiles.
		MetadataType metadataTypeForPath = this.myPreferences
				.getMetadataTypeByName("pathimagefiles");
		// Metadataobject for path.
		List<? extends Metadata> allpaths = boundbook
				.getAllMetadataByType(metadataTypeForPath);
		Metadata path;
		if (allpaths.isEmpty()) {
			// Calculate path for imageset.
			path = new Metadata(metadataTypeForPath);
			// Calculate value for image path.
			path.setValue("./defaultpath/");
		} else {
			path = allpaths.get(0);
		}
		if (path == null) {
			// No path to images available.
			LOGGER.debug("Can't find path to images");
			return false;
		}
		// Contains the path as string.
		String pathasstring = path.getValue();
		// Replace all backslashes with slashes (UNIX like path).
		for (int y = 0; y < pathasstring.length(); y++) {
			if (pathasstring.substring(y, y + 1).equals("\\")) {
				// Replace backslash.
				String front = pathasstring.substring(0, y);
				String back = pathasstring.substring(y + 1, pathasstring
						.length());
				pathasstring = front + "/" + back;
			}
		}

		// Get a FileSet to store all images as ContentFiles.
		if (this.myImageset == null) {
			// Create ImageSet object.
			this.myImageset = new FileSet();
			this.mydoc.setFileSet(this.myImageset);
		}

		// Allpages just contains pages from this pagination sequence.
		for (int i = 0; i < allpages.size(); i++) {
			// Get single node.
			currentPage = allpages.get(i);
			try {
				boundbook.addChild(currentPage);
			} catch (TypeNotAllowedAsChildException tnaace) {
				LOGGER.error("Can't add pages to BoundBook");
				return false;
			}

			// Create new Image object and add it to myImageSet.
			ugh.dl.ContentFile newimage = new ugh.dl.ContentFile();
			String filename = "";

			// Get physical page number.
			ugh.dl.MetadataType metadataType2 = this.myPreferences
					.getMetadataTypeByName("physPageNumber");
			List<? extends Metadata> physpagelist = currentPage
					.getAllMetadataByType(metadataType2);
			Iterator<? extends Metadata> it = physpagelist.iterator();
			int physpage = 0;
			while (it.hasNext()) {
				Metadata md = it.next();
				try {
					physpage = Integer.parseInt(md.getValue());
				} catch (Exception e) {
					LOGGER
							.error("Physical page number seems to be a non integer value!");
					return false;
				}
			}

			if (physpage < 100000) {
				filename = "000" + (physpage) + ".tif";
			}
			if (physpage < 10000) {
				filename = "0000" + (physpage) + ".tif";
			}
			if (physpage < 1000) {
				filename = "00000" + (physpage) + ".tif";
			}
			if (physpage < 100) {
				filename = "000000" + (physpage) + ".tif";
			}
			if (physpage < 10) {
				filename = "0000000" + (physpage) + ".tif";
			}
			newimage.setLocation("file://" + pathasstring + "/" + filename);
			newimage.setMimetype("image/tiff");
			// Add the file to the imageset.
			this.myImageset.addFile(newimage);
			// Add contentFile to page.
			currentPage.addContentFile(newimage);
		}

		this.mydoc.setPhysicalDocStruct(boundbook);
		this.mydoc.setFileSet(this.myImageset);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Read an RDF:Seq or RDF:Bag with all RDF:Li elements returns the values of
	 * the given XML-Elements being child of RDF:Li elements parameters:
	 * LinkedList containing the Names (as Strings) of XML-Elements if an
	 * RDF:Seq RDF:Bag or RDF:Li cannot be found, NULL is returned.
	 * </p>
	 * 
	 * @param inNode
	 * @param inXMLElements
	 * @return
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	private List<Metadata> readRDFSeq(Node inNode,
			List<MetadataType> inXMLElements)
			throws MetadataTypeNotAllowedException {

		// Nodes containing RDF:Seq.
		NodeList childnodes;
		// Nodes containing RDF:Li.
		NodeList liNodes;
		// Nodes containing the metadata.
		NodeList metadataNodes;
		Node currentNode = null;
		Node currentNode2 = null;
		Node currentNode3 = null;
		Node currentNode4 = null;
		String nodeName;
		String nodeName2;
		String nodeName3;
		String nodeName4;
		List<Metadata> resultList = new LinkedList<Metadata>();
		// Stores value of metadata.
		String value = "";

		// Search for RDF:Seq.
		childnodes = inNode.getChildNodes();
		for (int i = 0; i < childnodes.getLength(); i++) {
			// Get single node.
			currentNode = childnodes.item(i);
			if (currentNode.getNodeType() != ELEMENT_NODE) {
				// It's not an element node; so next iteration.
				continue;
			}
			nodeName = currentNode.getNodeName();
			if (nodeName.equals("RDF:Seq") || nodeName.equals("RDF:Bag")) {
				// Search for RDF:Li.
				liNodes = currentNode.getChildNodes();
				for (int x = 0; x < liNodes.getLength(); x++) {
					currentNode2 = liNodes.item(x);
					if (currentNode.getNodeType() != ELEMENT_NODE) {
						// It's not an element node; so next iteration.
						continue;
					}
					nodeName2 = currentNode2.getNodeName();
					if (nodeName2.equalsIgnoreCase("RDF:Li")) {
						// Find metadata nodes.
						metadataNodes = currentNode2.getChildNodes();
						for (int y = 0; y < metadataNodes.getLength(); y++) {
							currentNode3 = metadataNodes.item(y);
							if (currentNode3.getNodeType() != ELEMENT_NODE) {
								continue;
							}
							nodeName3 = currentNode3.getNodeName();
							// Get metadatatype.
							MetadataType mdType = getMDTypeByName(nodeName3);
							if (mdType == null) {
								// Metadata type not known.
								continue;
							}
							for (MetadataType searchMDType : inXMLElements) {
								// We found the correct metadata element; create
								// new Metadata object, add value and add it to
								// the result list.
								if (searchMDType != null
										&& mdType.equals(searchMDType)) {
									// It's a person.
									if (mdType.getIsPerson()) {
										// Create Person Object.
										Person resultPerson = new Person(mdType);

										// At this point we might ask for
										// (myMDType.getIsPerson() == true) ==>
										// it's a person, it's an creator; so
										// the child node is an Element Node.

										// Get the two elements and create
										// value.
										/***************************************
										 * if ((myMDType.getName().equals(
										 * "Author"))||
										 * (myMDType.getName().equals
										 * ("Editor"))||
										 * (myMDType.getName().equals
										 * ("Photographer"))||
										 * (myMDType.getName(
										 * ).equals("Illustrator"))){
										 **************************************/

										

										// Get values of first- and lastname.
										NodeList namenodes = currentNode3
												.getChildNodes();
										String lastname = null;
										String firstname = null;
										String displayname = null;
										for (int a = 0; a < namenodes
												.getLength(); a++) {
											currentNode4 = namenodes.item(a);

											// If a text node is directly
											// following the elementnode, there
											// is some first or last name tag
											// missing! If a value is existing,
											// take it as displayname!
											if (currentNode4.getNodeType() == Node.TEXT_NODE
													&& currentNode4 != null
													&& getMDValueOfNode(currentNode4) != null) {
												displayname = getMDValueOfNode(currentNode4);
												resultPerson
														.setDisplayname(displayname);
												LOGGER
														.warn("LastName and FirstName tags are missing within the person node '"
																+ nodeName3
																+ "', taking entry '"
																+ displayname
																+ "' as displayName!");
											}
											// We do not handle other types than
											// text or element node types here.
											else if (currentNode4.getNodeType() != Node.ELEMENT_NODE) {
												continue;
											}
											// Here an element is following, and
											// we can retrieve the persons data.
											nodeName4 = currentNode4
													.getNodeName();
											// Get lastname.
											if (nodeName4
													.equals("AGORA:CreatorLastName")) {
												lastname = getMDValueOfNode(currentNode4);
												resultPerson
														.setLastname(lastname);
											}
											// Get firstname.
											if (nodeName4
													.equals("AGORA:CreatorFirstName")) {
												firstname = getMDValueOfNode(currentNode4);
												resultPerson
														.setFirstname(firstname);
											}
											// Set value, if firstname and
											// lastname is not NULL.
											if (firstname != null
													&& lastname != null) {
												value = lastname + ", "
														+ firstname;
												resultPerson.setValue(value);
											}
										}

										// Set person's role.
										resultPerson.setRole(mdType.getName());

										// Add person, if either firstname,
										// lastname or displayName is not null.
										if (resultPerson.getFirstname() != null
												|| resultPerson.getLastname() != null
												|| resultPerson
														.getDisplayname() != null) {
											resultList.add(resultPerson);
											LOGGER
													.trace("Added person '"
															+ resultPerson
																	.getRole()
															+ "' with firstname '"
															+ (firstname == null ? "NULL"
																	: firstname)
															+ "', lastname '"
															+ (lastname == null ? "NULL"
																	: lastname)
															+ "', and displayname '"
															+ (displayname == null ? "NULL"
																	: displayname)
															+ "'");
										}
										// Add person, if node has any value at
										// all.
										else if (currentNode4 != null
												&& currentNode4.getNodeValue() != null
												&& !currentNode4.equals("")) {
											resultPerson
													.setDisplayname(currentNode4
															.getNodeValue());
											resultList.add(resultPerson);
											LOGGER.info("Added person '"
													+ resultPerson.getRole()
													+ "' with displayname '"
													+ resultPerson
															.getDisplayname()
													+ "'");
										}
									}
									// It's a normal metadata object.
									else {
										value = getMDValueOfNode(currentNode3);
										// Create Metadata object.
										Metadata resultMD = new Metadata(
												searchMDType);
										resultMD.setValue(value);
										resultList.add(resultMD);
									}

									break;
								}
							}
						}
					}
				}
			}
		}

		return resultList;
	}

	/***************************************************************************
	 * <p>
	 * Gets the metadata value out of an element; the method will find the FIRST
	 * textnode and returns its content as a string.
	 * </p>
	 * 
	 * @param inNode
	 * @return
	 **************************************************************************/
	private String getMDValueOfNode(Node inNode) {

		// Contents of textnode.
		NodeList textnodes = inNode.getChildNodes();
		if (textnodes != null && textnodes.getLength() > 0) {
			if (textnodes.item(0).getNodeType() == Node.TEXT_NODE) {
				// TODO We could handle removing or not removing whitespaces
				// within the XML tags here! Make it configurable!
				return textnodes.item(0).getNodeValue().trim();
			}
		}

		// Name element is empty.
		return null;
	}

	/***************************************************************************
	 * <p>
	 * Returns the RDF name, which is the name of the XML element.
	 * </p>
	 * 
	 * @param inType
	 * @return
	 **************************************************************************/
	private String getRDFName(DocStructType inType) {

		// Get internal name first.
		String dsName = inType.getName();

		// Search all rdfNames (from rdfNamesDS hashtable for a
		// MatchingMetadataObject, with an internalname of mdName.
		Collection<MatchingMetadataObject> col = this.rdfNamesDS.values();
		for (MatchingMetadataObject mmo : col) {
			if (mmo.getInternalName().equals(dsName)) {
				return mmo.getRDFName();
			}
		}

		return null;
	}

	/***************************************************************************
	 * <p>
	 * Description get the mmo objecdt by the internal name.
	 * </p>
	 * 
	 * @param theMetadataName
	 *            internal name of the metadata type
	 * @return the MetadataMatchingObject which contains the mapping for the
	 *         Metadatatype with the given internal name
	 **************************************************************************/
	private MatchingMetadataObject getMMOByName(String theMetadataName) {

		Collection<MatchingMetadataObject> col = this.rdfNamesMD.values();
		for (MatchingMetadataObject mmo : col) {
			if (mmo.getInternalName().equals(theMetadataName)) {
				return mmo;
			}
		}

		return null;
	}

	/***************************************************************************
	 * @param inType
	 * @return
	 **************************************************************************/
	private String getRDFName(MetadataType inType) {

		// Get internamName of MetadataType.
		String mdName = inType.getName();
		if (mdName == null) {
			return null;
		}

		// Search all rdfNames (from rdfNamesMD hashtable for a
		// MatchingMetadataObject, with an internalname of mdName.
		Collection<MatchingMetadataObject> col = this.rdfNamesMD.values();
		for (MatchingMetadataObject mmo : col) {
			if (mmo.getInternalName().equals(mdName)) {
				return mmo.getRDFName();
			}
		}

		return null;
	}

	/***************************************************************************
	 * @param mdName
	 *            Name of metadata in rdf-namespace
	 * @return the internal MetadataType
	 **************************************************************************/
	private MetadataType getMDTypeByName(String mdName) {

		MetadataType result = null;
		String mdtName = null;
		MatchingMetadataObject mmo = null;
		// Get internal Name.
		mmo = this.rdfNamesMD.get(mdName);
		if (mmo == null) {
			// mmo is null, which means, no type with this name is available.
			return null;
		}

		// Get internal Name.
		mdtName = mmo.getInternalName();
		// Get MetadataType from internalName.
		result = this.myPreferences.getMetadataTypeByName(mdtName);

		return result;
	}

	/***************************************************************************
	 * @param dsName
	 * @return
	 **************************************************************************/
	private DocStructType getDSTypeByName(String dsName) {

		DocStructType result = null;
		String mdtName = null;
		MatchingMetadataObject mmo = null;
		// Get internal name.
		mmo = this.rdfNamesDS.get(dsName);
		if (mmo == null) {
			// mmo is null, which means, no type with this name is available.
			LOGGER.warn("Can't find internal Docstruct type with RDF name '"
					+ dsName + "'");
			return null;
		}

		// Get internal name.
		mdtName = mmo.getInternalName();
		// Get MetadataType from internal name.
		result = this.myPreferences.getDocStrctTypeByName(mdtName);

		return result;
	}

	/***************************************************************************
	 * <p>
	 * All methods to read RDF file specific preferences are read here.
	 * </p>
	 * 
	 * @param inNode
	 * @return true, if preference file can be read. false, if preference file
	 *         has wrong structure, element names are wrong etc.
	 **************************************************************************/
	public boolean readPrefs(Node inNode) throws PreferencesException {

		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("Metadata")
					&& !readMetadataPrefs(currentNode)) {
				// Read information about a single metadata matching.
				String message = "Can't read for node '"
						+ currentNode.getNodeName() + "."
						+ currentNode.getNodeValue() + "'";
				LOGGER.error(message);
				throw new PreferencesException(message);
			}
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("DocStruct")
					&& !readDocStructPrefs(currentNode)) {
				// Read information about a single docstruct matching.
				String message = "Error occurred while reading DocStructs";
				LOGGER.error(message);
				return false;
			}
		}

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Methods ro read all the preferences (matching information).
	 * </p>
	 * 
	 * @param inNode
	 * @return
	 **************************************************************************/
	private boolean readMetadataPrefs(Node inNode) {

		String internalName = null;
		String rdfName = null;
		String rdfList = null;
		String rdfListType = null;
		MatchingMetadataObject mmo = null;

		// Read information from XML.
		//
		// Check for rdfList attribute.
		NamedNodeMap nnm = inNode.getAttributes();
		if (nnm != null) {
			Node rdfListNode = nnm.getNamedItem("rdfList");
			if (rdfListNode != null) {
				rdfList = rdfListNode.getNodeValue();
			}
			Node rdfListTypeNode = nnm.getNamedItem("rdfListType");
			if (rdfListTypeNode != null) {
				rdfListType = rdfListTypeNode.getNodeValue();
			}
		}

		// Get all child elements.
		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("Name")) {
				internalName = getTextNodeValue(currentNode);
			}
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("RDFName")) {
				rdfName = getTextNodeValue(currentNode);
				mmo = new MatchingMetadataObject();
				mmo.setRDFName(rdfName);
			}
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("filterRule")) {
				if (rdfName != null) {
					// rdfName must be declared already.
					return false;
				}
				// Get NamedNodeMap to access atrributes.
				NamedNodeMap nnm2 = currentNode.getAttributes();
				if (nnm2 != null) {
					Node nameNode = nnm.getNamedItem("name");
					Node parameterNode = nnm.getNamedItem("parameter");
					Node conditionNode = nnm.getNamedItem("condition");
					Node typeNode = nnm.getNamedItem("type");
					String ruleName = null;
					String ruleParameter = null;
					String ruleCondition = null;
					String ruleType = null;
					if (nameNode != null) {
						ruleName = nameNode.getNodeValue();
					} else {
						// Invalid rule; rule must always have a name.
						return false;
					}
					if (parameterNode != null) {
						ruleParameter = parameterNode.getNodeValue();
					}
					if (conditionNode != null) {
						ruleCondition = conditionNode.getNodeValue();
					}
					if (typeNode != null) {
						ruleType = typeNode.getNodeValue();
					}
					mmo.addFilterRule(ruleName, ruleParameter, ruleCondition,
							ruleType);
				}
			}
		}

		// Check, if internal Name is really available.
		if (this.myPreferences.getMetadataTypeByName(internalName) == null) {
			// No metadatatype with internalName is available.
			LOGGER
					.error("Error while reading format preferences for RDF: No MetadataType with internal name '"
							+ internalName + "' known");
			return false;
		}
		if (rdfList != null && rdfListType == null) {
			LOGGER.error("RDF list defined, but no list type for metadata '"
					+ internalName + "'");
			rdfListType = "seq";
		}
		if (rdfListType != null && !rdfListType.equalsIgnoreCase("seq")
				&& !rdfListType.equalsIgnoreCase("bag")) {
			LOGGER.error("RDF list type for metadata '" + internalName
					+ "' is of unknown type (only 'seq' or 'bag' allowed)");
		}
		if (rdfName != null) {
			// Add it to Hashtable excelNames.
			mmo.setInternalName(internalName);
			if (rdfList != null) {
				mmo.setRDFList(rdfList);
			}
			if (rdfListType != null) {
				mmo.setRdfListType(rdfListType);
			}
			this.rdfNamesMD.put(rdfName, mmo);
		} else {
			LOGGER
					.error("Error while reading format preferences for RDF: RDF name must not be null!");
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 **************************************************************************/
	private boolean readDocStructPrefs(Node inNode) {

		String internalName = null;
		String rdfName = null;

		// Read information from XML.
		NodeList childlist = inNode.getChildNodes();
		for (int i = 0; i < childlist.getLength(); i++) {
			// Get single node.
			Node currentNode = childlist.item(i);
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("Name")) {
				internalName = getTextNodeValue(currentNode);
			}
			if (currentNode.getNodeType() == ELEMENT_NODE
					&& currentNode.getNodeName().equals("RDFName")) {
				rdfName = getTextNodeValue(currentNode);
			}
		}

		// Check, if internal Name is really available.
		if (this.myPreferences.getDocStrctTypeByName(internalName) == null) {
			// No metadatatype with internalName is available.
			LOGGER
					.error("Error while reading format preferences for RDF: No DocStructType with internal name '"
							+ internalName + "' known");
			return false;
		}
		if (rdfName != null) {
			// Add it to Hashtable.
			MatchingMetadataObject mmo = new MatchingMetadataObject();
			mmo.setRDFName(rdfName);
			mmo.setInternalName(internalName);
			this.rdfNamesDS.put(rdfName, mmo);
		} else {
			LOGGER
					.error("Error while reading format preferences for RDF: RDF name must not be null!");
		}

		return true;
	}

	/***************************************************************************
	 * @param inNode
	 * @return
	 **************************************************************************/
	private String getTextNodeValue(Node inNode) {

		String result = null;

		NodeList textnodes = inNode.getChildNodes();
		if (textnodes != null) {
			Node textnode = textnodes.item(0);
			if (textnode.getNodeType() != Node.TEXT_NODE) {
				// No text node available; maybe it's another element etc..
				// anyhow: an error.
				return null;
			}

			result = textnode.getNodeValue();
		}

		return result;
	}

	/***************************************************************************
	 * <p>
	 * Checks, wether there is an RDFList with this name or not.
	 * </p>
	 * 
	 * @param nodename
	 *            name of the node, which can be the list
	 * @return true, if this should be in a RDF-List
	 **************************************************************************/
	private boolean isRDFList(String nodename) {

		// Read all mmo objects from rdfNamesMD Hashtable to check, if one mmo
		// contains such a list.
		Collection<MatchingMetadataObject> col = this.rdfNamesMD.values();

		for (MatchingMetadataObject mmo : col) {
			if (mmo.getRDFList() != null && mmo.getRDFList().equals(nodename)) {
				return true;
			}
		}

		return false;
	}

	/***************************************************************************
	 * <p>
	 * Retrieves all MetadataMatchingObjects belonging to a single rdfList.
	 * </p>
	 * 
	 * @param listname
	 * @return LinkedList containing mmos.
	 **************************************************************************/
	private List<MatchingMetadataObject> getRDFListMembers(String listname) {

		List<MatchingMetadataObject> listmembers = new LinkedList<MatchingMetadataObject>();

		// Read all mmo objects from rdfNamesMD Hashtable to check, if one mmo
		// contains such a list.
		Collection<MatchingMetadataObject> col = this.rdfNamesMD.values();

		for (MatchingMetadataObject mmo : col) {
			if (mmo.getRDFList() != null && mmo.getRDFList().equals(listname)) {
				listmembers.add(mmo);
			}
		}

		return listmembers;
	}

	/***************************************************************************
	 * <p>
	 * Define inner class to store all matching information for a single object,
	 * e.g. rdfname and rdflist name for an internal metadata type.
	 * </p>
	 **************************************************************************/
	class MatchingMetadataObject {

		private String					rdfName			= null;
		private String					rdfList			= null;
		private String					rdfListType		= null;
		private String					internalName	= null;
		private final List<String[]>	allFilterRules	= new LinkedList<String[]>();

		/***************************************************************************
		 * @return Returns the rdfListType.
		 **************************************************************************/
		public String getRdfListType() {
			return this.rdfListType;
		}

		/***************************************************************************
		 * @param rdfListType
		 *            The rdfListType to set.
		 **************************************************************************/
		public void setRdfListType(String rdfListType) {
			this.rdfListType = rdfListType;
		}

		/***************************************************************************
		 * Constructor.
		 **************************************************************************/
		public MatchingMetadataObject() {
			//
		}

		/***************************************************************************
		 * @param in
		 **************************************************************************/
		public void setRDFName(String in) {
			this.rdfName = in;
		}

		/***************************************************************************
		 * @param in
		 **************************************************************************/
		public void setRDFList(String in) {
			this.rdfList = in;
		}

		/***************************************************************************
		 * @param in
		 **************************************************************************/
		public void setInternalName(String in) {
			this.internalName = in;
		}

		/***************************************************************************
		 * @return
		 **************************************************************************/
		public String getRDFName() {
			return this.rdfName;
		}

		/***************************************************************************
		 * @return
		 **************************************************************************/
		public String getRDFList() {
			return this.rdfList;
		}

		/***************************************************************************
		 * @return
		 **************************************************************************/
		public String getInternalName() {
			return this.internalName;
		}

		/***************************************************************************
		 * @param name
		 * @param parameter
		 * @param condition
		 * @param type
		 **************************************************************************/
		public void addFilterRule(String name, String parameter,
				String condition, String type) {
			String[] rule = { name, parameter, condition, type };
			this.allFilterRules.add(rule);
		}

		/***************************************************************************
		 * @return
		 **************************************************************************/
		public List<String[]> getFilterRules() {
			return this.allFilterRules;
		}

	}

}
