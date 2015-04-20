package ugh.dl;

/*******************************************************************************
 * ugh.dl / DocStruct.java
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
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 ******************************************************************************/

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument.ListPairCheck;
import ugh.exceptions.ContentFileNotLinkedException;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsModsImportExport;

/*******************************************************************************
 * <p>
 * A DocStruct object represents a structure entity in work. Every document consists of a structure, which can be separated into several structure
 * entities, which build hierarchical structure. Usually a <code>DigitalDocument</code> contains two structures; a logical and a physical one. Each
 * structure consists of a top DocStruct element that is embedded in some kind of structure. This structure is represented by parent and children of
 * <code>DocStruct</code> objects.
 * </p>
 * 
 * <p>
 * This class contains methods to:
 * <ul>
 * <li>Retrieve information about the structure (add, move and remove children),
 * <li>set the parent (the top element has no parent),
 * <li>set and retrieve metadata, which describe a structure entity,
 * <li>handle content files, which are linked to a structure entity.
 * </ul>
 * </p>
 * 
 * <p>
 * Every structure entity is of a special kind. The kind of entity is stored in a <code>DocStructType</code> element. Depending on the type of
 * structure entities certain metadata and children a permitted or forbidden.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-26
 * @see DigitalDocument
 * 
 *      TODOLOG
 *      
 *      TODO Remove databaseid and unreachable code
 * 
 *      TODO Remove all the boolean results that always are TRUE!!
 * 
 *      TODO Maybe use the equals() method for comparing the things from the ruleset and the things from the DigitalDocument?? This may only be
 *      interesting for XStream serialisation!!
 * 
 *      TODO Shall the metadata given by getMetadata() and similar methods already be sorted? Do we need public sorting methods here? Do we need
 *      different getMetadata methods like getMetadataAlphabetically and getMetadataInRulesetOrder?
 * 
 *      CHANGELOG
 *      
 *      20.02.2015 --- Ronge --- Export label metadata of first child to anchor
 * 
 *      26.06.2014 --- Ronge --- Get anchor classes & get only real successors --- Pass DigitalDocument as parameter --- Fix NullPointerException
 *      
 *      25.06.2014 --- Ronge --- Get reading of logical structure to work --- Recursive implementation of getChild() --- Get all childs' MODS
 *      sections --- Override toString() for DocStruct
 *      
 *      23.06.2014 --- Ronge --- Fixed an NPE --- Make read & write functions work with multiple anchor files --- Create ORDERLABEL attribute on
 *      export & add getter for meta data
 *      
 *      20.06.2014 --- Ronge --- Add some methods for easier use
 *      
 *      18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *      05.05.2010 --- Funk --- Minor changes.
 * 
 *      22.01.2010 --- Funk --- Minor changes due to findbugs.
 * 
 *      18.01.2010 --- Funk --- Adapted class to changed DocStruct.getAllMetadataByType(). Re-refactored method name.
 * 
 *      21.12.2009 --- Funk --- Re-added some missing (?) line. --- Added some logging.
 * 
 *      14.12.2009 --- Funk --- Removed an NPE.
 * 
 *      09.12.2009 --- Funk --- Some changes.
 * 
 *      08.12.2009 --- Funk --- Minor changes.
 * 
 *      30.11.2009 --- Funk --- Fixed NPE in getAllContentFiles().
 * 
 *      21.11.2009 --- Funk --- Fixed some NPEs.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      13.10.2009 --- Funk --- Slightly improved addMetadata()'s error handling --- Corrected the DocStruct update from the prefs.
 * 
 *      10.11.2009 --- Funk --- Changed getAllVisibleMetadata(), ignoring now empty metadata fields.
 * 
 *      05.11.2009 --- Funk --- Added getAllVisibleMetadata().
 * 
 *      28.10.2009 --- Funk --- Added HIDDEN_METADATA_CHAR.
 * 
 *      21.10.2009 --- Funk --- Removed some unmappable character for encoding ASCII.
 * 
 *      20.10.2009 --- Funk --- Refactored some list constructs and conditionals --- Changed getDisplayMetadataTypes() that no "internal" metadata
 *      types (starting with "_") are returned.
 * 
 *      05.10.2009 --- Funk --- Adapted metadata and person constructors.
 * 
 *      30.09.2009 --- Funk --- Moved the recursively sorting methods to DigitalDocument. --- Added "private" to all method attributes.
 * 
 *      29.09.2009 --- Funk --- Added the sortMetadataAbcdefg() methods.
 * 
 *      17.09.2009 --- Funk --- Fixed some NullPointerException occurrences in sortMetadata().
 * 
 *      11.09.2009 --- Wulf Riebensahm --- Equals method overloaded.
 * 
 *      08.06.2009 --- Funk --- Added the method sortMetadataRecursively, to be able to sort the metadata according to the prefs occurrence.
 * 
 *      03.06.2009 --- Funk --- Added null check in countMDofthisType
 * 
 *      22.05.2009 --- Funk --- Fixed Bug DPD-216: NullPointerExfeption.
 * 
 *      30.04.2009 --- Funk --- Removed a bug. Look for the fix, if you want :-)
 * 
 *      29.09.2008 --- Funk --- Logging added.
 * 
 *      28.07.2008 --- Funk --- Check if persons are existing (somewhere).
 * 
 *      07.07.2008 --- Funk --- Persons are checked with the getAllMetadataByType() method.
 * 
 *      25.06.2008 - Funk - Already used persons are considered now in the method getAddableMetadataTypes.
 * 
 ******************************************************************************/

public class DocStruct implements Serializable {

    private static final long serialVersionUID = -4531356062293054921L;

    private static final Logger LOGGER = Logger.getLogger(ugh.dl.DigitalDocument.class);
    private static final String HIDDEN_METADATA_CHAR = "_";
    
	private static final List<String> IDENTIFIER_METADATA_FIELDS_FOR_TOSTRING = Arrays.asList(
		new String[] { "TitleDocMain", "CatalogIDDigital", "TitleDocMainShort", "MetsPointerURL" }
	);

	private static final Set<String> FOREIGN_CHILD_METADATA_TYPES_TO_COPY = new HashSet<String>(
			Arrays.asList(new String[] { MetsModsImportExport.CREATE_MPTR_ELEMENT_TYPE,
					MetsModsImportExport.CREATE_LABEL_ATTRIBUTE_TYPE,
					MetsModsImportExport.CREATE_ORDERLABEL_ATTRIBUTE_TYPE }));
	
    // List containing all Metadata instances.
    private List<Metadata> allMetadata;
    // List containing Metadata instances which has been removed; this instances
    // must be deleted from database etc.
    private List<Metadata> removedMetadata;

    private List<MetadataGroup> allMetadataGroups;

    private List<MetadataGroup> removedMetadataGroups;

    // List containing all DocStrct-instances being children of this instance.
    private List<DocStruct> children;
    // List containing all references to Contentfile objects.
    private List<ContentFileReference> contentFileReferences = new LinkedList<ContentFileReference>();
    // List of all persons; list containing all Person objects.
    private List<Person> persons;

    private DocStruct parent;
    // All references to other DocStrct instances (containing References
    // objects).
    private final List<Reference> docStructRefsTo = new LinkedList<Reference>();
    // All references from another DocStruct to this one.
    private final List<Reference> docStructRefsFrom = new LinkedList<Reference>();
    // Type of this instance.
    private DocStructType type;
    // Local identifier of this docstruct.
    private String identifier = null;
    // Digital document, to which this DocStruct belongs.
    private DigitalDocument digdoc;
    // ID in database table (4 byte long).
    private final long databaseid = 0;
    private Object origObject = null;
    private boolean logical = false;
    private boolean physical = false;
    // String containing an identifier or a URL to the anchor.
    private String referenceToAnchor;
    //the amdSec referenced by this docStruct, if any
    private AmdSec amdSec;
    //the list of techMd sections referenced by this docStruct, if any
    private List<Md> techMdList;

    /***************************************************************************
     * <p>
     * This is needed so we can exclude the possibility to run eternal loops with non hierarchial references, will be filled with super()toString
     * signature of the compared DocStruct.
     * </p>
     **************************************************************************/
    private HashMap<String, Object> signaturesForEqualsMethodRefsFrom;
    private HashMap<String, Object> signaturesForEqualsMethodRefsTo;

    /***************************************************************************
     * <p>
     * Constructor just used to be compatible with JavaBeans.
     * </p>
     * 
     * @deprecated
     **************************************************************************/
    @Deprecated
    public DocStruct() {
        super();
    }

    /***************************************************************************
     * <p>
     * Constructs a new DocStruct object of a given type. The type can be changed later using the <code>setType</code> method.
     * </p>
     * 
     * @param inType type of this DocStruct instance
     * @throws TypeNotAllowedForParentException is thrown, if this docstruct is not allowed for a parent
     **************************************************************************/
    protected DocStruct(DocStructType inType) throws TypeNotAllowedForParentException {

        // We have to check, if this type is allowed here, this depends on the
        // parent DocStruct.
       setType(inType);

        //        // This conditional can never be reached, because the result ALWAYS
        //        // is true! See setType()! Check again and take it out!
        //        if (!result) {
        //            TypeNotAllowedForParentException tnae = new TypeNotAllowedForParentException();
        //            LOGGER.error("The type '" + inType.getName() + "' is not allowed as a child of '" + this.getType().getName() + "'");
        //            throw tnae;
        //        }
    }

    /***************************************************************************
     * @param dd
     **************************************************************************/
    protected void setDigitalDocument(DigitalDocument dd) {
        this.digdoc = dd;
    }

    /***************************************************************************
     * <p>
     * Sets the type of this DocStruct instance. When changing the type, the allowed metadata elements and children are NOT checked. Therefore it is
     * possible to create documents, that are not valid against the current preferences file.
     * </p>
     * 
     * @param inType DocStructType to be set
     * @return always true
     **************************************************************************/
    public boolean setType(DocStructType inType) {

        // Usually we had to check, if the new type is allowed. Search for
        // parent and see if the parent allows this type.
        this.type = inType;

        return true;
    }

    /***************************************************************************
     * <p>
     * Get the type of an instance.
     * </p>
     * 
     * @return DocStructType of this DocStruct
     **************************************************************************/
    public DocStructType getType() {
        return this.type;
    }

    /***************************************************************************
     * <p>
     * Returns all Children of an instance.
     * </p>
     * 
     * @return List containing DocStruct instances; if this instance has no children, null is returned.
     **************************************************************************/
    public List<DocStruct> getAllChildren() {

        if (this.children == null || this.children.isEmpty()) {
            return null;
        }

        return this.children;
    }

	/**
	 * Returns all real successors, i.e. all child nodes that are of a different
	 * or no anchor class at all, of an instance as a flat list. Doesn’t return
	 * unreal successors; that are those who are nothing but METS pointers.
	 * 
	 * @return List containing DocStruct instances; if this instance has no
	 *         children, an empty list is returned.
	 */
	public List<DocStruct> getAllRealSuccessors() {
		LinkedList<DocStruct> result = new LinkedList<DocStruct>();
		if (children != null) {
			for (DocStruct child : children) {
				if (type.getAnchorClass().equals(child.getType().getAnchorClass())) {
					result.addAll(child.getAllRealSuccessors());
				} else if (!child.hasMetadata(MetsModsImportExport.CREATE_MPTR_ELEMENT_TYPE)) {
					result.add(child);
				}
			}
		}
		return result;
	}

    /***************************************************************************
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public String getreferenceToAnchor() {
        return getReferenceToAnchor();
    }

    /**************************************************************************
     * <p>
     * Retrieves the identifier/URN/URL of the anchor. The anchor is another DocStruct which is stored in another DigitalDocument; e.g. a "Journal"
     * can be the anchor for PeriodicalVolumes. Both DocStructs are stored in different DigitalDocuments (different mets files) and are linked
     * together by an identifier. The identifier of the anchor should be stored here. On the side of the anchor, this identifier is stored as a normal
     * metadata field...
     * </p>
     * 
     * @return
     **************************************************************************/
    public String getReferenceToAnchor() {
        return this.referenceToAnchor;
    }

    /***************************************************************************
     * @param in
     * @deprecated
     **************************************************************************/
    @Deprecated
    public void setreferenceToAnchor(String in) {
        setReferenceToAnchor(in);
    }

    /**************************************************************************
     * <p>
     * Sets the identifier of the anchor.
     * </p>
     * 
     * @param in
     **************************************************************************/
    public void setReferenceToAnchor(String in) {
        this.referenceToAnchor = in;
    }

    /***************************************************************************
     * <p>
     * Gets all Children for a DocStruct instance, which are of a special type, and which have a special type of metadata. E.g. you can get all
     * Articles wihch have an author. It is possible to use "*" as a parameter value for MetadataType and DocStructType. In this case, the "*" is a
     * wildcard.
     * </p>
     * 
     * 
     * @param theDocTypeName internal name of the structure type (as String)
     * @param theMDTypeName internal name of metadata type (as String)
     * @return List containing DocStruct instances; ; if this instance has no children, null is returned
     **************************************************************************/
    public List<DocStruct> getAllChildrenByTypeAndMetadataType(String theDocTypeName, String theMDTypeName) {

        List<DocStruct> resultList = new LinkedList<DocStruct>();
        boolean docTypeTestPassed = false;
        boolean mdTypeTestPassed = false;
        List<Metadata> allMD;

        if (this.children == null || this.children.isEmpty()) {
            return null;
        }

        for (DocStruct child : this.children) {
            docTypeTestPassed = false;

            // Check doctype.
            if (theDocTypeName.equals("*")) {
                // Wildcard; we do not have to check the doctype.
                docTypeTestPassed = true;
            } else {
                DocStructType singleType = child.getType();
                String singlename = singleType.getName();
                if (singlename != null && singlename.equals(theDocTypeName)) {
                    docTypeTestPassed = true;
                } else {
                    // Wrong type.
                    continue;
                }
            }

            // Get all Metadatatypes.
            allMD = child.getAllMetadata();
            // Child has no metadata.
            if (allMD == null) {
                // MetadataType doesn't matter anyhow, so we can add this one,
                // too.
                if (theMDTypeName.equals("*")) {
                    mdTypeTestPassed = true;
                } else {
                    mdTypeTestPassed = false;
                }
            } else {
                for (Metadata md : allMD) {
                    mdTypeTestPassed = false;
                    if (theMDTypeName.equals("*")) {
                        mdTypeTestPassed = true;
                        break;
                    } else {
                        MetadataType mdtype = md.getType();
                        String mdtypename = mdtype.getName();

                        if (mdtypename != null && mdtypename.equals(theMDTypeName)) {
                            mdTypeTestPassed = true;
                            break;
                        }
                    }
                }
            }
            if (mdTypeTestPassed && docTypeTestPassed) {
                // Doctype and metadatatype test passed, add it.
                resultList.add(child);
            }
        }

        if (resultList.isEmpty()) {
            return null;
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Sets the local identifier; currently there is no automatic check, if the identifier is used for another docstruct or metadata element.
     * </p>
     * 
     * @param in
     * @return always true
     **************************************************************************/
    public boolean setIdentifier(String in) {
        this.identifier = in;

        return true;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getIdentifier() {
        return this.identifier;
    }

    /***************************************************************************
     * <p>
     * Extracts a list with all Metadata objects which are identifiers (their MetadataType has the identifier flag set).
     * </p>
     * 
     * @return a List containing Metadata instances; if none were found null is returned.
     **************************************************************************/
    public List<Metadata> getAllIdentifierMetadata() {

        List<Metadata> result = new LinkedList<Metadata>();

        if (this.allMetadata == null) {
            return null;
        }

        for (Metadata md : this.allMetadata) {
            if (md.getType().isIdentifier) {
                result.add(md);
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Copies a DocStruct element with all the associated Metadata and Person objects.
     * </p>
     * 
     * @param cpmetadata copies Metadata if set to true
	 * @param recursive
	 *            if true, copies all children as well; if null, copies all
	 *            children which are of the same anchor class; if false, doesn’t
	 *            copy any children
     * @return a new DocStruct instance
     * @throws TypeNotAllowedForParentException
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
	public DocStruct copy(boolean cpmetadata, Boolean recursive) {

        DocStruct newStruct = null;
        try {
            newStruct = new DocStruct(this.getType());
        } catch (TypeNotAllowedForParentException e) {
            // This should never happen as we are creating the same
            // DocStructType.
            String message = "This " + e.getClass().getName() + " should not have been occurred!";
            LOGGER.error(message, e);
        }

        // Copy the link to the parent.
        newStruct.setParent(this.getParent());
        newStruct.origObject = this.origObject;
        if (this.logical) {
            newStruct.logical = this.logical;
        }

        // Copy metadata and persons.
        if (cpmetadata) {
            if (this.getAllMetadata() != null) {
                for (Metadata md : this.getAllMetadata()) {
                    try {
                        Metadata mdnew = new Metadata(md.getType());
                        mdnew.setValue(md.getValue());
                        if (md.getValueQualifier() != null && md.getValueQualifierType() != null) {
                            mdnew.setValueQualifier(md.getValueQualifier(), md.getValueQualifierType());
                        }
                        if (md.getAuthorityID() != null && md.getAuthorityValue() != null && md.getAuthorityURI() != null) {
                            mdnew.setAutorityFile(md.getAuthorityID(), md.getAuthorityURI(), md.getAuthorityValue());
                        }
                        newStruct.addMetadata(mdnew);
                    } catch (DocStructHasNoTypeException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    }
                }
            }

            if (this.getAllMetadataGroups() != null) {
                for (MetadataGroup md : this.getAllMetadataGroups()) {
                    try {
                        MetadataGroup mdnew = new MetadataGroup(md.getType());
                        mdnew.setDocStruct(newStruct);
                        List<Metadata> newmdlist = new LinkedList<Metadata>();
                        List<Person> newPersonList = new LinkedList<Person>();
                        for (Metadata meta : md.getMetadataList()) {
                            Metadata newMeta = new Metadata(meta.getType());
                            newMeta.setValue(meta.getValue());
                            if (meta.getValueQualifier() != null && meta.getValueQualifierType() != null) {
                                newMeta.setValueQualifier(meta.getValueQualifier(), meta.getValueQualifierType());
                            }
                            if (meta.getAuthorityID() != null && meta.getAuthorityValue() != null && meta.getAuthorityURI() != null) {
                                newMeta.setAutorityFile(meta.getAuthorityID(), meta.getAuthorityURI(), meta.getAuthorityValue());
                            }
                            newmdlist.add(newMeta);
                        }

                        for (Person ps : md.getPersonList()) {
                            Person newps = new Person(ps.getType());
                            if (ps.getLastname() != null) {
                                newps.setLastname(ps.getLastname());
                            }
                            if (ps.getFirstname() != null) {
                                newps.setFirstname(ps.getFirstname());
                            }
                            if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null && ps.getAuthorityValue() != null) {
                                newps.setAutorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
                            }
                            if (ps.getInstitution() != null) {
                                newps.setInstitution(ps.getInstitution());
                            }
                            if (ps.getAffiliation() != null) {
                                newps.setAffiliation(ps.getAffiliation());
                            }
                            if (ps.getRole() != null) {
                                newps.setRole(ps.getRole());
                            }
                            newPersonList.add(newps);
                        }
                        mdnew.setMetadataList(newmdlist);
                        mdnew.setPersonList(newPersonList);
                        newStruct.addMetadataGroup(mdnew);

                        mdnew.setMetadataList(newmdlist);
                        newStruct.addMetadataGroup(mdnew);
                    } catch (DocStructHasNoTypeException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        // This should never happen, as we are adding the same
                        // MetadataType.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    }

                }
            }

            // Copy the persons.
            if (this.getAllPersons() != null) {
                for (Person ps : this.getAllPersons()) {
                    try {
                        Person newps = new Person(ps.getType());
                        if (ps.getLastname() != null) {
                            newps.setLastname(ps.getLastname());
                        }
                        if (ps.getFirstname() != null) {
                            newps.setFirstname(ps.getFirstname());
                        }
                
                        if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null && ps.getAuthorityValue() != null) {
                            newps.setAutorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
                        }

                        if (ps.getInstitution() != null) {
                            newps.setInstitution(ps.getInstitution());
                        }
                        if (ps.getAffiliation() != null) {
                            newps.setAffiliation(ps.getAffiliation());
                        }
                        if (ps.getRole() != null) {
                            newps.setRole(ps.getRole());
                        }
                        newStruct.addPerson(newps);
                    } catch (IncompletePersonObjectException e) {
                        // This should never happen as we are adding the same
                        // person type.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    } catch (MetadataTypeNotAllowedException e) {
                        // This should never happen as we are adding the same
                        // person type.
                        String message = "This " + e.getClass().getName() + " should not have been occurred!";
                        LOGGER.error(message, e);
                    }
                }
            }
            }        	

		// Iterate over all children, if recursive set to true.
		if ((recursive == null || recursive == true) && this.getAllChildren() != null) {
			for (DocStruct child : this.getAllChildren()) {
				if (recursive == null
						&& (type == null || type.getAnchorClass() == null || child.getType() == null || !type
								.getAnchorClass().equals(child.getType().getAnchorClass()))) {
					continue;
				}
				DocStruct copiedChild = child.copy(cpmetadata, recursive);
				try {
					newStruct.addChild(copiedChild);
				} catch (TypeNotAllowedAsChildException e) {
					String message = "This " + e.getClass().getName() + " should not have been occurred!";
					LOGGER.error(message, e);
				}
			}
		}

        return newStruct;
    }

	/**
	 * The function copyTruncated() returns a partial copy the structural tree
	 * with all structural elements down to one level below the given anchor
	 * class and meta data attached only to elements of the given anchor class.
	 * 
	 * @param anchorClass
	 *            anchor class below which the copy shall be truncated
	 * @return a paritial copy of the structure tree
	 */
	public DocStruct copyTruncated(String anchorClass) {
		return copyTruncated(anchorClass, parent);
	}

	/**
	 * The function copyTruncated() returns a partial copy the structural tree
	 * with all structural elements down to one level below the given anchor
	 * class and meta data attached only to elements of the given anchor class.
	 * 
	 * @param anchorClass
	 *            anchor class below which the copy shall be truncated
	 * @param parent
	 *            parent class of the copy to create
	 * @return a paritial copy of the structure tree
	 */
	private DocStruct copyTruncated(String anchorClass, DocStruct parent) {

		try {
			DocStruct newStruct = new DocStruct(type);
			newStruct.parent = parent;
			newStruct.logical = this.logical;

			if (anchorClass == null ? type.getAnchorClass() == null : anchorClass.equals(type.getAnchorClass())) {
				if (allMetadata != null) {
					for (Metadata md : allMetadata) {
						if (MetsModsImportExport.CREATE_MPTR_ELEMENT_TYPE.equals(md.getType().getName())) {
							continue;
						}
						Metadata mdnew = new Metadata(md.getType());
						mdnew.setValue(md.getValue());
						if (md.getValueQualifier() != null && md.getValueQualifierType() != null) {
							mdnew.setValueQualifier(md.getValueQualifier(), md.getValueQualifierType());
						}
						if (md.getAuthorityID() != null && md.getAuthorityValue() != null
								&& md.getAuthorityURI() != null) {
							mdnew.setAutorityFile(md.getAuthorityID(), md.getAuthorityURI(), md.getAuthorityValue());
						}
						newStruct.addMetadata(mdnew);
					}
				}

				if (allMetadataGroups != null) {
					for (MetadataGroup md : this.getAllMetadataGroups()) {
						MetadataGroup mdnew = new MetadataGroup(md.getType());
						mdnew.setDocStruct(newStruct);
						List<Metadata> newmdlist = new LinkedList<Metadata>();
						List<Person> newPersonList = new LinkedList<Person>();
						for (Metadata meta : md.getMetadataList()) {
							Metadata newMeta = new Metadata(meta.getType());
							newMeta.setValue(meta.getValue());
							if (meta.getValueQualifier() != null && meta.getValueQualifierType() != null) {
								newMeta.setValueQualifier(meta.getValueQualifier(), meta.getValueQualifierType());
							}
							if (meta.getAuthorityID() != null && meta.getAuthorityValue() != null
									&& meta.getAuthorityURI() != null) {
								newMeta.setAutorityFile(meta.getAuthorityID(), meta.getAuthorityURI(),
										meta.getAuthorityValue());
							}
							newmdlist.add(newMeta);
						}

						for (Person ps : md.getPersonList()) {
							Person newps = new Person(ps.getType());
							if (ps.getLastname() != null) {
								newps.setLastname(ps.getLastname());
							}
							if (ps.getFirstname() != null) {
								newps.setFirstname(ps.getFirstname());
							}
							if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null
									&& ps.getAuthorityValue() != null) {
								newps.setAutorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
							}
							if (ps.getInstitution() != null) {
								newps.setInstitution(ps.getInstitution());
							}
							if (ps.getAffiliation() != null) {
								newps.setAffiliation(ps.getAffiliation());
							}
							if (ps.getRole() != null) {
								newps.setRole(ps.getRole());
							}
							newPersonList.add(newps);
						}
						mdnew.setMetadataList(newmdlist);
						mdnew.setPersonList(newPersonList);
						newStruct.addMetadataGroup(mdnew);

						mdnew.setMetadataList(newmdlist);
						newStruct.addMetadataGroup(mdnew);

					}
				}

				// Copy the persons.
				if (this.getAllPersons() != null) {
					for (Person ps : this.getAllPersons()) {

						Person newps = new Person(ps.getType());
						if (ps.getLastname() != null) {
							newps.setLastname(ps.getLastname());
						}
						if (ps.getFirstname() != null) {
							newps.setFirstname(ps.getFirstname());
						}

						if (ps.getAuthorityID() != null && ps.getAuthorityURI() != null
								&& ps.getAuthorityValue() != null) {
							newps.setAutorityFile(ps.getAuthorityID(), ps.getAuthorityURI(), ps.getAuthorityValue());
						}

						if (ps.getInstitution() != null) {
							newps.setInstitution(ps.getInstitution());
						}
						if (ps.getAffiliation() != null) {
							newps.setAffiliation(ps.getAffiliation());
						}
						if (ps.getRole() != null) {
							newps.setRole(ps.getRole());
						}
						newStruct.addPerson(newps);

					}
				}
			} else if (allMetadata != null
					&& parent != null
					&& parent.getType().getAnchorClass() != null
					&& parent.getType().getAnchorClass().equals(anchorClass)
					&& (anchorClass == null ? type.getAnchorClass() != null : !anchorClass
							.equals(type.getAnchorClass()))) {
				for (Metadata md : allMetadata) {
					if (!FOREIGN_CHILD_METADATA_TYPES_TO_COPY.contains(md.getType().getName())) {
						continue;
					}
					Metadata mdnew = new Metadata(md.getType());
					mdnew.setValue(md.getValue());
					newStruct.addMetadata(mdnew);
				}
			} else if (allMetadata != null
					&& children != null
					&& (anchorClass == null ? type.getAnchorClass() != null : !anchorClass
							.equals(type.getAnchorClass())))
				for (DocStruct child : children)
					if (anchorClass == null ? child.getAnchorClass() == null : anchorClass.equals(child
							.getAnchorClass())) {
						for (Metadata md : allMetadata) {
							if (!FOREIGN_CHILD_METADATA_TYPES_TO_COPY.contains(md.getType().getName())) {
								continue;
							}
							Metadata mdnew = new Metadata(md.getType());
							mdnew.setValue(md.getValue());
							newStruct.addMetadata(mdnew);
						}
						break;
					}

			if (children != null
					&& (anchorClass.equals(type.getAnchorClass()) || parent == null || !parent.getType()
							.getAnchorClass().equals(anchorClass))) {
				for (DocStruct child : this.getAllChildren()) {
					if ((anchorClass == null ? type.getAnchorClass() == null : anchorClass.equals(type.getAnchorClass()))
							|| !child.isMetsPointerStruct() ) {
						DocStruct copiedChild = child.copyTruncated(anchorClass, this);
						newStruct.addChild(copiedChild);
					}
				}
			}

			return newStruct;
		} catch (UGHException thisShouldNeverHappen) {
			throw new RuntimeException(thisShouldNeverHappen.getMessage(), thisShouldNeverHappen);
		}
	}

	/**
	 * Returns whether this is a DocStruct that contains METS pointers himself
	 * or whose children are, without exception, METS pointers in the same
	 * sense.
	 * 
	 * @return whether this contains only METS pointers
	 */
	public boolean isMetsPointerStruct() {
		if (getMetadataByType(MetsModsImportExport.CREATE_MPTR_ELEMENT_TYPE).size() > 0) {
			return true;
		}
		if (children == null) {
			return false;
		}
		for (DocStruct child : children) {
			if (!child.isMetsPointerStruct()) {
				return false;
			}
		}
		return true;
	}

	/***************************************************************************
     * <p>
     * Returns all References; parameter must be "to" or "from"; otherwise all references are returned a List is returned, containing "References"
     * instances.
     * </p>
     * 
     * @param in can be "to" or "from"
     * @return List containing Reference objects
     **************************************************************************/
    public List<Reference> getAllReferences(String in) {

        if (in == null) {
            return null;
        }
        if (in.equals("to")) {
            return this.docStructRefsTo;
        }
        if (in.equals("from")) {
            return this.docStructRefsFrom;
        }

        return null;
    }

    /***************************************************************************
     * <p>
     * Retrieves all References from this DocStruct to another - in other words: All References, in which this DocStruct is the Source.
     * </p>
     * 
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllToReferences() {
        return this.docStructRefsTo;
    }

    /***************************************************************************
     * <p>
     * Returns all References (just to-References from this DocStruct to another) of a specific type.
     * </p>
     * 
     * @param theType Type of the reference; e.g. "logical_physical" for references from logical structures to physical ones
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllToReferences(String theType) {

        List<Reference> refs = new LinkedList<Reference>();

        if (this.docStructRefsTo != null) {
            for (Reference ref : this.docStructRefsTo) {
                if (ref.getType().equals(theType)) {
                    refs.add(ref);
                }
            }
        }

        if (refs == null || refs.isEmpty()) {
            return null;
        }

        return refs;
    }

    /***************************************************************************
     * <p>
     * Retrieves all References from this DocStruct from another - in other words: All References, in which this DocStruct is the target.
     * </p>
     * 
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllFromReferences() {
        return this.docStructRefsFrom;
    }

    /***************************************************************************
     * <p>
     * Returns all References (just from-References from this DocStruct to another) of a specific type.
     * </p>
     * 
     * @param theType Type of the reference; e.g. "logical_physical" for references from logical structures to physical ones
     * @return List containing <code>References</code> objects
     **************************************************************************/
    public List<Reference> getAllFromReferences(String theType) {

        List<Reference> refs = new LinkedList<Reference>();

        if (this.docStructRefsFrom != null) {
            for (Reference ref : this.docStructRefsFrom) {
                if (ref.getType().equals(theType)) {
                    refs.add(ref);
                }
            }
        }

        if (refs == null || refs.isEmpty()) {
            return null;
        }

        return refs;
    }

    /***************************************************************************
     * <p>
     * Sets the parent; usually not necessary as the parent is set automatically, if a DocStruct instance is added as a child.
     * </p>
     * 
     * @param inParent
     * @return true, if parent was set successfully
     **************************************************************************/
    public boolean setParent(DocStruct inParent) {

        if (inParent != null) {
            // Remove this DocStruct instance fromt he child's list.
            inParent.removeChild(this);
        }

        // Usually we had to check if this parent allows this instance being a
        // child because of its DocStructType.

        // Add child to this parent.
        this.parent = inParent;

        return true;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public DocStruct getParent() {
        return this.parent;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataGroups for this DocStruct instance.
     * </p>
     * 
     * @return List containing MetadataGroup instances; if no MetadataGroup is available, null is returned.
     **************************************************************************/
    public List<MetadataGroup> getAllMetadataGroups() {

        if (this.allMetadataGroups == null || this.allMetadataGroups.isEmpty()) {
            return null;
        }

        return this.allMetadataGroups;
    }

    /***************************************************************************
     * <p>
     * Allows to set all MetadataGroup. The MetadataGroup objects are contained in a List. This method sets all MetadataGroup; they are NOT added.
     * MetadataGroup which is already available will be overwritten.
     * </p>
     * 
     * @param inList List containing MetadataGroup objects.
     * @return always true
     **************************************************************************/
    public boolean setAllMetadataGroups(List<MetadataGroup> inList) {
        this.allMetadataGroups = inList;

        return true;
    }

    /***************************************************************************
     * <p>
     * Gets all Metadata for this DocStruct instance.
     * </p>
     * 
     * @return List containing Metadata instances; if no metadata is available, null is returned.
     **************************************************************************/
    public List<Metadata> getAllMetadata() {

        if (this.allMetadata == null || this.allMetadata.isEmpty()) {
            return null;
        }

        return this.allMetadata;
    }

    /***************************************************************************
     * <p>
     * Allows to set all Metadata. The Metadata objects are contained in a List. This method sets all Metadata; they are NOT added. Metadata which is
     * already available will be overwritten.
     * </p>
     * 
     * @param inList List containing Metadata objects.
     * @return always true
     **************************************************************************/
    public boolean setAllMetadata(List<Metadata> inList) {
        this.allMetadata = inList;

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all ContentFile objects, which belong to this instance.
     * </p>
     * 
     * @return List containing ContentFile objects; if no content files are available null is returned.
     **************************************************************************/
    public List<ContentFile> getAllContentFiles() {

        List<ContentFile> contentFiles = new LinkedList<ContentFile>();

        if (this.contentFileReferences == null || this.contentFileReferences.isEmpty()) {
            return null;
        }

        for (ContentFileReference contentFileReference : this.contentFileReferences) {
            // Add it, if it is not null AND it doesn't already belong to the
            // list.
            if (contentFileReference != null && !contentFiles.contains(contentFileReference.getCf())) {
                contentFiles.add(contentFileReference.getCf());
            }
        }

        return contentFiles;
    }

    /***************************************************************************
     * <p>
     * This method checks, if an instance of the DocStruct has a Metadata- or Person object of the given type.
     * </p>
     * 
     * @param inMDT
     * @return true, if available; otherwise false
     **************************************************************************/
    public boolean hasMetadataGroupType(MetadataGroupType inMDT) {

        // Check metadata.
        List<MetadataGroup> allMDs = this.getAllMetadataGroups();
        if (allMDs != null) {
            for (MetadataGroup md : allMDs) {
                MetadataGroupType mdt = md.getType();
                if (inMDT != null && inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * This method checks, if an instance of the DocStruct has a Metadata- or Person object of the given type.
     * </p>
     * 
     * @param inMDT
     * @return true, if available; otherwise false
     **************************************************************************/
    public boolean hasMetadataType(MetadataType inMDT) {

        // Check metadata.
        List<Metadata> allMDs = this.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                MetadataType mdt = md.getType();
                if (inMDT != null && inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }

        // Check persons.
        List<Person> allPersons = this.getAllPersons();
        if (allPersons != null) {
            for (Person per : allPersons) {
                MetadataType mdt = per.getType();
                if (inMDT != null && inMDT.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves all References to ContentFiles.
     * </p>
     * 
     * @return List containing ContentFileReference objects
     * @see ContentFileReference
     **************************************************************************/
    public List<ContentFileReference> getAllContentFileReferences() {
        return this.contentFileReferences;
    }

    /***************************************************************************
     * <p>
     * Adds a new ContentFileReference to this DocStruct and adds the file to the FileSet.
     * </p>
     * 
     * 
     * @param theFile ContentFile object to be added
     * @return always true
     * @see FileSet
     **************************************************************************/
    public void addContentFile(ContentFile theFile) {

        // Create a new FileSet if there is none available.
        FileSet fs;
        if (this.digdoc.getFileSet() == null) {
            fs = new FileSet();
            this.digdoc.setFileSet(fs);
        } else {
            fs = this.digdoc.getFileSet();
        }

        // Add the file, existence check is done in FileSet.addFile() now.
        fs.addFile(theFile);

        if (this.contentFileReferences == null) {
            // Re-added this line, maybe was it's deletion an error?
            this.contentFileReferences = new LinkedList<ContentFileReference>();
        }
        // Now we can add the reference to the ContentFile, if the reference is
        // not existing yet.
        ContentFileReference cfr = new ContentFileReference();
        cfr.setCf(theFile);
        if (!this.contentFileReferences.contains(cfr)) {
            this.contentFileReferences.add(cfr);
            theFile.addDocStructAsReference(this);
        }

    }

    /***************************************************************************
     * <p>
     * Adds a new ContentFile object to this DocStruct object; there is no check, if a ContentFile is already linked to this DocStruct.
     * </p>
     * <p>
     * Before adding ContentFile objects to a DocStruct, make sure they are already added to the FileSet.
     * </p>
     * 
     * 
     * @param inCF ContentFile object to be added
     * @return always true
     * @see FileSet
     **************************************************************************/
    public void addContentFile(ContentFile inCF, ContentFileArea inArea) {

        if (this.contentFileReferences == null) {
            // Re-added this line, maybe was it's deletion an error?
            this.contentFileReferences = new LinkedList<ContentFileReference>();
        }

        // Check if ContentFile belongs already to the FileSet.
        FileSet fs = this.digdoc.getFileSet();
        // Get all content files of this digital document.
        List<ContentFile> allCFs = fs.getAllFiles();
        if (!allCFs.contains(inCF)) {
            // Doesn't contain this content file.
            fs.addFile(inCF);
        }

        // Now add reference to ContentFile.
        ContentFileReference cfr = new ContentFileReference();
        cfr.setCfa(inArea);
        cfr.setCf(inCF);
        this.contentFileReferences.add(cfr);
        inCF.addDocStructAsReference(this);

    }

    /***************************************************************************
     * <p>
     * Removes links between a ContentFile object and this DocStruct object. If a single ContentFile is referenced more than once from this DocStruct
     * all links are removed.<br>
     * For that reason all attached ContentFileReference objects are searched.
     * </p>
     * 
     * @throws ContentFileNotLinkedException if ContentFile is not linked to this DocStruct
     * @param inCF to be removed
     * @return true, if succeeded; otherwise false
     **************************************************************************/
    public boolean removeContentFile(ContentFile theContentFile) throws ContentFileNotLinkedException {

        boolean removed = false;

        if (this.contentFileReferences == null) {
            return false;
        }

        List<ContentFileReference> copiedContentFileReferences = new LinkedList<ContentFileReference>(this.contentFileReferences);

        for (ContentFileReference cfr : copiedContentFileReferences) {
            if (cfr.getCf() != null && cfr.getCf().equals(theContentFile)) {
                // The ContentFile is in the Reference; so remove file and
                // reference.
                this.contentFileReferences.remove(cfr);
                ContentFile cf = cfr.getCf();
                cf.removeDocStructAsReference(this);
                removed = true;
            }
        }

        // Given ContentFile is NOT member.
        if (!removed) {
            String message = "Content file '" + theContentFile.getLocation() + "' is not a member of DocStruct '" + this.getType().getName() + "'";
            throw new ContentFileNotLinkedException(message);
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Adds a new reference to this DocStruct instance. References are always linked both ways. Both docstruct instances are storing a reference to
     * the other DocStruct instance. This methods stores the To-Reference. The DocStruct instance given as a parameter is the target of the Reference
     * (to which is linked to). The appropriate From-Reference (from the target to the source - this DocStruct instance) is set automatically. Each
     * Reference can contain a type (string).
     * </p>
     * 
     * @param inDocStruct Target of the Reference
     * @param theType String containing any information about the type of reference
     * @return a newly created References object containing information about linking both DocStructs
     **************************************************************************/
    public Reference addReferenceTo(DocStruct inDocStruct, String theType) {

        Reference ref = new Reference();
        if (this.databaseid == 0) {
            ref.setSource(this);
        } else {
            ref.setSourceID(this.databaseid);
        }
        if (inDocStruct.databaseid == 0) {
            ref.setTarget(inDocStruct);
        } else {
            ref.setTargetID(inDocStruct.databaseid);
        }
        ref.setType(theType);
        this.docStructRefsTo.add(ref);
        inDocStruct.docStructRefsFrom.add(ref);
        return ref;
    }

    /***************************************************************************
     * <p>
     * Adds a From-Reference. The current DocStruct instance is the target of the Reference. The appropriate To-Reference is added automatically to
     * the Source-DocStruct. For more detailed information, see addReferenceTo method.
     * </p>
     * 
     * @param inDocStruct DocStruct object, which is the source of the reference.
     * @param theType any kind of linking information
     * @return a newly created References object containing information about linking both DocStructs
     **************************************************************************/
    public Reference addReferenceFrom(DocStruct inDocStruct, String theType) {

        Reference ref = new Reference();
        if (this.databaseid == 0) {
            ref.setTarget(this);
        } else {
            ref.setTargetID(this.databaseid);
        }
        if (inDocStruct.databaseid == 0) {
            ref.setSource(inDocStruct);
        } else {
            ref.setSourceID(inDocStruct.databaseid);
        }
        ref.setType(theType);
        this.docStructRefsFrom.add(ref);
        inDocStruct.docStructRefsTo.add(ref);
        return ref;
    }

    /***************************************************************************
     * <p>
     * Removes a To-Reference (a reference to another docstruct instance). The corresponding From-Reference in the Target-Docstruct object is also
     * deleted. The References object is not used anymore and will be deleted at the next garbage collection.
     * </p>
     * 
     * @param inStruct target-DocStruct
     * @return true, if successful
     **************************************************************************/
    public boolean removeReferenceTo(DocStruct inStruct) {

        List<Reference> ll = new LinkedList<Reference>(this.docStructRefsTo);

        for (Reference ref : ll) {
            if (ref.getTarget().equals(inStruct)) {
                // Remove reference from this instance.
                this.docStructRefsTo.remove(ref);
                DocStruct targetStruct = ref.getTarget();
                List<Reference> ll2 = targetStruct.docStructRefsFrom;
                // Remove the reference from target.
                if (ll2 != null) {
                    ll2.remove(ref);
                }
            }
        }

        return true;
    }

    /**************************************************************************
     * <p>
     * Removes a From-Reference (a reference from another docstruct instance to this one). The corresponding To-Reference in this DocStruct
     * (source-Docstruct object) is also deleted. The References object is not used anymore and will be deleted at the next garbage collection.
     * </p>
     * 
     * @param inStruct Source-DocStruct
     * @return true, if successful
     **************************************************************************/
    public boolean removeReferenceFrom(DocStruct inStruct) {

        List<Reference> ll = new LinkedList<Reference>(this.docStructRefsFrom);

        for (Reference ref : ll) {
            if (ref.getTarget().equals(inStruct)) {
                // Remove reference from this instance.
                this.docStructRefsFrom.remove(ref);
                DocStruct targetStruct = ref.getTarget();
                List<Reference> ll2 = targetStruct.docStructRefsTo;
                // Remove the reference from source.
                if (ll2 != null) {
                    ll2.remove(ref);
                }
            }
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadataGroup Metadata object to be added.
     * @return TRUE if metadata was added successfully, FALSE otherwise.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
    public boolean addMetadataGroup(MetadataGroup theMetadataGroup) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException {

        MetadataGroupType inMdType = theMetadataGroup.getType();
        String inMdName = inMdType.getName();
        // Integer, number of metadata allowed for this metadatatype.
        String maxnumberallowed;
        // Integer, number of metadata already available.
        int number;
        // Metadata can only be inserted if set to true.
        boolean insert = false;
        // Prefs MetadataType.
        MetadataGroupType prefsMdType;

        // First get MetadataType object for the DocStructType to which this
        // document structure belongs to get global MDType.
        if (this.type == null) {
            String message = "Error occurred while adding metadata group of type '" + inMdName + "' to " + identify(this) + " DocStruct: DocStruct has no type.";
            LOGGER.error(message);
            throw new DocStructHasNoTypeException(message);
        }

        prefsMdType = this.type.getMetadataGroupByGroup(inMdType);

        // Ask DocStructType instance to get MetadataType by Type. At this point
        // we are creating a local copy of the MetadataType object.
        if (prefsMdType == null && !(inMdName.startsWith(HIDDEN_METADATA_CHAR))) {
            MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(null, this.getType());
            LOGGER.error(e.getMessage());
            throw e;
        }

        // Check, if it's an internal MetadataType - all internal types begin
        // with the HIDDEN_METADATA_CHAR, we can have as many as we want.
        if (inMdName.startsWith(HIDDEN_METADATA_CHAR)) {
            maxnumberallowed = "*";
            prefsMdType = inMdType;
        } else {
            maxnumberallowed = this.type.getNumberOfMetadataGroups(prefsMdType);
        }

        // Check, if another Metadata instance is allowed.
        //
        // How many metadata are already available.
        number = countMDofthisType(inMdName);

        // As many as we want (zero or more).
        if (maxnumberallowed.equals("*")) {
            insert = true;
        }

        // Once or more.
        if (maxnumberallowed.equals("+") || maxnumberallowed.equals("+")) {
            insert = true;
        }

        // Only one, if we have already one, we cannot add it.
        if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // Add metadata.
        if (insert) {
            // Set type to MetadataType of the DocStructType.
            theMetadataGroup.setType(prefsMdType);
            // Set this document structure as myDocStruct.
            theMetadataGroup.setDocStruct(this);
            if (this.allMetadataGroups == null) {
                // Create list, if not already available.
                this.allMetadataGroups = new LinkedList<MetadataGroup>();
            }
            this.allMetadataGroups.add(theMetadataGroup);
        } else {
            LOGGER.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(null, this.getType());
            LOGGER.error(mtnae.getMessage());
            throw mtnae;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed. By setting the second parameter to true, this behaviour can be influenced. This can
     * be necessary e.g. when programming user interfaces etc.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param theMd Metadata object which should be removed
     * @param force set to true, the Metadata is removed even if it is not allowed to. You can create not validateable documents.
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroup theMd, boolean force) {

        MetadataGroupType inMdType;
        String maxnumbersallowed;
        int typesavailable;

        // Get Type of inMD.
        inMdType = theMd.getType();

        // How many metadata of this type do we have already.
        typesavailable = countMDofthisType(inMdType.getName());

        // How many types must be at least available.
        maxnumbersallowed = this.type.getNumberOfMetadataGroups(inMdType);

        if (force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }
        if (force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        theMd.myDocStruct = null;

        if (this.removedMetadataGroups == null) {
            this.removedMetadataGroups = new LinkedList<MetadataGroup>();
        }

        this.removedMetadataGroups.add(theMd);
        this.allMetadataGroups.remove(theMd);

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param inMD Metadata object which should be removed
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroup inMD) {
        // Just calls removeMetadata with force set to false.
        return removeMetadataGroup(inMD, false);
    }

    /***************************************************************************
     * <p>
     * Exchanges a Metadata object against an old one. Only metadata objects of the same type (of the same MetadataType object) can be exchanged. The
     * Metadata-Type object of the new Metadata object is copied locally (as it is done, when adding metadata).
     * </p>
     * 
     * <p>
     * OLD COMMENT? : exchanges two metadata objects; can be used instead of doing a remove and an add later on. Must be used, if a Metadata object
     * cannot be removed because of DTD (there must always be at least one object). Therefore we can only change Metadata objects of the same
     * MetadataType.
     * </p>
     * 
     * @param theOldMd Metadata object which should be replaced.
     * @param theNewMd New Metadata object.
     * @return True, if Metadata object could be exchanged; otherwise false.
     **************************************************************************/
    public boolean changeMetadataGroup(MetadataGroup theOldMd, MetadataGroup theNewMd) {

        MetadataGroupType oldMdt;
        MetadataGroupType newMdt;
        String oldName;
        String newName;
        int counter = 0;

        // Get MetadataTypes.
        oldMdt = theOldMd.getType();
        newMdt = theNewMd.getType();

        // Get names.
        oldName = oldMdt.getName();
        newName = newMdt.getName();

        if (oldName.equals(newName)) {
            // Different metadata types.
            return false;
        }

        // Remove old object; get place of old object in list.
        for (MetadataGroup m : this.allMetadataGroups) {
            // Found old metadata object.
            if (m.equals(theOldMd)) {
                // Get out of loop.
                break;
            }
            counter++;
        }

        // Ask DocStructType instance to get a new MetadataType object of the
        // same kind.
        MetadataGroupType mdType = this.type.getMetadataGroupByGroup(theOldMd.getType());
        theNewMd.setType(mdType);

        this.allMetadataGroups.remove(theOldMd);
        this.allMetadataGroups.add(counter, theNewMd);

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all Metadata object, which belong to this DocStruct and have a special type. Can be used to get all titles, authors etc... includes
     * Persons!
     * </p>
     * 
     * PLEASE NOTE This method no longer returns NULL, if no MetadataTypes are available! An empty list is returned now!
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, an empty list is returned.
     **************************************************************************/
    public List<MetadataGroup> getAllMetadataGroupsByType(MetadataGroupType inType) {

        List<MetadataGroup> resultList = new LinkedList<MetadataGroup>();

        // Check all metadata.
        if (inType != null && this.allMetadataGroups != null) {
            for (MetadataGroup md : this.allMetadataGroups) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Adds a metadata object to this instance; The method checks, if it is allowed to add one (based on the configuration). If so, the object is
     * added and returns true; otherwise it returns false.
     * </p>
     * <p>
     * The Metadata object must already include all necessary information as MetadataType and value.
     * </p>
     * <p>
     * For internal reasons this method changes the MetadataType object against a local copy, which is retrieved from the appropriate DocStructType of
     * this DocStruct instance. The internal name of both MetadataType objects must be identical. If a local copy cannot be found (which means, the
     * metadata type is NOT valid for this kind of DocStruct object), false is returned.
     * </p>
     * 
     * @param theMetadata Metadata object to be added.
     * @return TRUE if metadata was added successfully, FALSE otherwise.
     * @throws MetadataTypeNotAllowedException If the DocStructType of this DocStruct instance does not allow the MetadataType or if the maximum
     *             number of Metadata (of this type) is already available.
     * @throws DocStructHasNoTypeException If no DocStruct Type is set for the DocStruct object; for this reason the metadata can't be added, because
     *             we cannot check, wether if the metadata type is allowed or not.
     * @see Metadata
     **************************************************************************/
    public boolean addMetadata(Metadata theMetadata) throws MetadataTypeNotAllowedException, DocStructHasNoTypeException {

        MetadataType inMdType = theMetadata.getType();
        String inMdName = inMdType.getName();
        // Integer, number of metadata allowed for this metadatatype.
        String maxnumberallowed;
        // Integer, number of metadata already available.
        int number;
        // Metadata can only be inserted if set to true.
        boolean insert = false;
        // Prefs MetadataType.
        MetadataType prefsMdType;

        // First get MetadataType object for the DocStructType to which this
        // document structure belongs to get global MDType.
        if (this.type == null) {
            String message = "Error occurred while adding metadata of type '" + inMdName + "' to " + identify(this) + " DocStruct: DocStruct has no type.";
            LOGGER.error(message);
            throw new DocStructHasNoTypeException(message);
        }

        prefsMdType = this.type.getMetadataTypeByType(inMdType);

        // Ask DocStructType instance to get MetadataType by Type. At this point
        // we are creating a local copy of the MetadataType object.
        if (prefsMdType == null && !(inMdName.startsWith(HIDDEN_METADATA_CHAR))) {
            MetadataTypeNotAllowedException e = new MetadataTypeNotAllowedException(inMdType, this.getType());
            LOGGER.error(e.getMessage());
            throw e;
        }

        // Check, if it's an internal MetadataType - all internal types begin
        // with the HIDDEN_METADATA_CHAR, we can have as many as we want.
        if (inMdName.startsWith(HIDDEN_METADATA_CHAR)) {
            maxnumberallowed = "*";
            prefsMdType = inMdType;
        } else {
            maxnumberallowed = this.type.getNumberOfMetadataType(prefsMdType);
        }

        // Check, if another Metadata instance is allowed.
        //
        // How many metadata are already available.
        number = countMDofthisType(inMdName);

        // As many as we want (zero or more).
        if (maxnumberallowed.equals("*")) {
            insert = true;
        }

        // Once or more.
        if (maxnumberallowed.equals("+") || maxnumberallowed.equals("+")) {
            insert = true;
        }

        // Only one, if we have already one, we cannot add it.
        if (maxnumberallowed.equalsIgnoreCase("1m") || maxnumberallowed.equalsIgnoreCase("1o")) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // Add metadata.
        if (insert) {
            // Set type to MetadataType of the DocStructType.
            theMetadata.setType(prefsMdType);
            // Set this document structure as myDocStruct.
            theMetadata.setDocStruct(this);
            if (this.allMetadata == null) {
                // Create list, if not already available.
                this.allMetadata = new LinkedList<Metadata>();
            }
            this.allMetadata.add(theMetadata);
        } else {
            LOGGER.debug("Not allowed to add metadata '" + inMdName + "'");
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException(inMdType, this.getType());
            LOGGER.error(mtnae.getMessage());
            throw mtnae;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed. By setting the second parameter to true, this behaviour can be influenced. This can
     * be necessary e.g. when programming user interfaces etc.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param theMd Metadata object which should be removed
     * @param force set to true, the Metadata is removed even if it is not allowed to. You can create not validateable documents.
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadata(Metadata theMd, boolean force) {

        MetadataType inMdType;
        String maxnumbersallowed;
        int typesavailable;

        // Get Type of inMD.
        inMdType = theMd.getType();

        // How many metadata of this type do we have already.
        typesavailable = countMDofthisType(inMdType.getName());

        // How many types must be at least available.
        maxnumbersallowed = this.type.getNumberOfMetadataType(inMdType);

        if (force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }
        if (force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        theMd.myDocStruct = null;

        if (this.removedMetadata == null) {
            this.removedMetadata = new LinkedList<Metadata>();
        }

        this.removedMetadata.add(theMd);
        this.allMetadata.remove(theMd);

        return true;
    }

    /***************************************************************************
     * <p>
     * Removes Metadata from this DocStruct object. If there must be at least one Metadata object of this kind, attached to this DocStruct instance
     * (according to configuration), the metadata is NOT removed.
     * </p>
     * <p>
     * If you want to remove Metadata of a specific type temporarily (e.g. to replace it), use the changeMetadata method instead.
     * </p>
     * 
     * @param inMD Metadata object which should be removed
     * @return true, if data can be removed; otherwise false
     * @see #canMetadataBeRemoved
     **************************************************************************/
    public boolean removeMetadata(Metadata inMD) {
        // Just calls removeMetadata with force set to false.
        return removeMetadata(inMD, false);
    }

    /***************************************************************************
     * <p>
     * Exchanges a Metadata object against an old one. Only metadata objects of the same type (of the same MetadataType object) can be exchanged. The
     * Metadata-Type object of the new Metadata object is copied locally (as it is done, when adding metadata).
     * </p>
     * 
     * <p>
     * OLD COMMENT? : exchanges two metadata objects; can be used instead of doing a remove and an add later on. Must be used, if a Metadata object
     * cannot be removed because of DTD (there must always be at least one object). Therefore we can only change Metadata objects of the same
     * MetadataType.
     * </p>
     * 
     * @param theOldMd Metadata object which should be replaced.
     * @param theNewMd New Metadata object.
     * @return True, if Metadata object could be exchanged; otherwise false.
     **************************************************************************/
    public boolean changeMetadata(Metadata theOldMd, Metadata theNewMd) {

        MetadataType oldMdt;
        MetadataType newMdt;
        String oldName;
        String newName;
        int counter = 0;

        // Get MetadataTypes.
        oldMdt = theOldMd.getType();
        newMdt = theNewMd.getType();

        // Get names.
        oldName = oldMdt.getName();
        newName = newMdt.getName();

        if (oldName.equals(newName)) {
            // Different metadata types.
            return false;
        }

        // Remove old object; get place of old object in list.
        for (Metadata m : this.allMetadata) {
            // Found old metadata object.
            if (m.equals(theOldMd)) {
                // Get out of loop.
                break;
            }
            counter++;
        }

        // Ask DocStructType instance to get a new MetadataType object of the
        // same kind.
        MetadataType mdType = this.type.getMetadataTypeByType(theOldMd.getType());
        theNewMd.setType(mdType);

        this.allMetadata.remove(theOldMd);
        this.allMetadata.add(counter, theNewMd);

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all Metadata object, which belong to this DocStruct and have a special type. Can be used to get all titles, authors etc... includes
     * Persons!
     * </p>
     * 
     * PLEASE NOTE This method no longer returns NULL, if no MetadataTypes are available! An empty list is returned now!
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, an empty list is returned.
     **************************************************************************/
    public List<? extends Metadata> getAllMetadataByType(MetadataType inType) {

        List<Metadata> resultList = new LinkedList<Metadata>();

        // Check all metadata.
        if (inType != null && this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }

        // Check all persons.
        if (inType != null && this.persons != null) {
            for (Metadata md : this.persons) {
                if (md.getType() != null && md.getType().getName().equals(inType.getName())) {
                    resultList.add(md);
                }
            }
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Retrieves all Person object, which belong to this DocStruct and have a special type. Persons only!
     * </p>
     * 
     * @param inType MetadataType we are looking for.
     * @return List containing Metadata objects; if no metadata ojects are available, null is returned.
     **************************************************************************/
    public List<Person> getAllPersonsByType(MetadataType inType) {

        List<Person> resultList = new LinkedList<Person>();

        if (inType == null) {
            return null;
        }

        // Check all persons.
        if (this.persons != null) {
            for (Person per : this.persons) {
                if (per.getType() != null && per.getType().getName().equals(inType.getName())) {
                    resultList.add(per);
                }
            }
        }

        // List is empty.
        if (resultList.size() == 0) {
            return null;
        }

        return resultList;
    }

    /***************************************************************************
     * <p>
     * Gets all Metadata for the current DocStruct, which shall be displayed, what includes all metadata that are not starting with the
     * HIDDEN_METADATA_CHAR.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<Metadata> getAllVisibleMetadata() {

        // Start with the list of all metadata.
        List<Metadata> result = new LinkedList<Metadata>();

        // Iterate over all metadata.
        if (getAllMetadata() != null) {
            for (Metadata md : getAllMetadata()) {
                // If the metadata does not start with the HIDDEN_METADATA_CHAR,
                // add it to the result list.
                if (md.getType().getName() != null && !md.getType().getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    result.add(md);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataTypes, which shall ALWAYS be displayed, even though they have no value.<br/>
     * 
     * Includes all metadata with attributes defaultDisplay="true" in the prefs. Hidden metadata, which start with the HIDDEN_METADATA_CHAR, will not
     * be included.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<MetadataGroupType> getDefaultDisplayMetadataGroupTypes() {

        List<MetadataGroupType> result = new LinkedList<MetadataGroupType>();

        if (this.type == null) {
            return null;
        }

        // Start with the list of MetadataTypes, which are having the
        // "defaultDisplay" attribute set.
        List<MetadataGroupType> allDefaultMdTypes = this.type.getAllDefaultDisplayMetadataGroups();

        if (allDefaultMdTypes != null) {
            // Iterate over all defaultDisplay metadata types and check, if
            // metadata of this type is already available.
            for (MetadataGroupType mdt : allDefaultMdTypes) {
                if (!hasMetadataGroup(mdt.getName()) && !mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    // If none of these metadata is available, AND it is not a
                    // hidden metadata type, add it to the result list.
                    result.add(mdt);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * See getDefaultDisplayMetadataTypes().
     * </p>
     * 
     * @deprecated
     * @return List containing MetadataType objects
     **************************************************************************/
    @Deprecated
    public List<MetadataGroupType> getDisplayMetadataGroupTypes() {
        return getDefaultDisplayMetadataGroupTypes();
    }

    /***************************************************************************
     * @param metadataTypeName
     * @return
     **************************************************************************/
    private boolean hasMetadataGroup(String metadataGroupTypeName) {

        if (this.allMetadataGroups != null) {
            for (MetadataGroup md : this.allMetadataGroups) {
                MetadataGroupType mdt = md.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataGroupTypeName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Gets all MetadataTypes, which shall ALWAYS be displayed, even though they have no value.<br/>
     * 
     * Includes all metadata with attributes defaultDisplay="true" in the prefs. Hidden metadata, which start with the HIDDEN_METADATA_CHAR, will not
     * be included.
     * </p>
     * 
     * @return List containing MetadataType objects
     **************************************************************************/
    public List<MetadataType> getDefaultDisplayMetadataTypes() {

        List<MetadataType> result = new LinkedList<MetadataType>();

        if (this.type == null) {
            return null;
        }

        // Start with the list of MetadataTypes, which are having the
        // "defaultDisplay" attribute set.
        List<MetadataType> allDefaultMdTypes = this.type.getAllDefaultDisplayMetadataTypes();

        if (allDefaultMdTypes != null) {
            // Iterate over all defaultDisplay metadata types and check, if
            // metadata of this type is already available.
            for (MetadataType mdt : allDefaultMdTypes) {
                if (!hasMetadata(mdt.getName()) && !mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                    // If none of these metadata is available, AND it is not a
                    // hidden metadata type, add it to the result list.
                    result.add(mdt);
                }
            }
        }

        if (result.isEmpty()) {
            result = null;
        }

        return result;
    }

    /***************************************************************************
     * <p>
     * See getDefaultDisplayMetadataTypes().
     * </p>
     * 
     * @deprecated
     * @return List containing MetadataType objects
     **************************************************************************/
    @Deprecated
    public List<MetadataType> getDisplayMetadataTypes() {
        return getDefaultDisplayMetadataTypes();
    }

    /***************************************************************************
     * @param metadataTypeName
     * @return
     **************************************************************************/
    private boolean hasMetadata(String metadataTypeName) {

        if (this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                MetadataType mdt = md.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataTypeName)) {
                    return true;
                }
            }
        }

        if (this.persons != null) {
			for (Person per : this.persons) {
                MetadataType mdt = per.getType();
                if (mdt == null) {
                    continue;
                }
                String name = mdt.getName();
                if (name.equals(metadataTypeName)) {
                    return true;
                }
            }
		}

        return false;
    }

    /***************************************************************************
     * <p>
     * Gives number of Metadata elements belonging to this DocStruct of a specific type. The type must be given by the unique (internal) name as it is
     * retrievable from MetadataType's getName method.
     * </p>
     * <p>
     * This method does not only get the number of Metadata elements, but also the number of person objects belonging to one <code>DocStruct</code>
     * object.
     * </p>
     * 
     * @param inTypeName Internal name of object as String
     * @return Number of metadata as integer
     **************************************************************************/
    public int countMDofthisType(String inTypeName) {

        MetadataType testtype;
        int counter = 0;

        if (this.allMetadata != null) {
            for (Metadata md : this.allMetadata) {
                testtype = md.getType();
                if (testtype != null && testtype.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        if (allMetadataGroups != null) {
            for (MetadataGroup mdg : allMetadataGroups) {
                MetadataGroupType mgt = mdg.getType();
                if (mgt != null && mgt.getName().equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }

        }

        if (this.persons != null) {
            for (Person per : this.persons) {
                testtype = per.getType();
                if (testtype != null && testtype.equals(inTypeName)) {
                    // Another one is available.
                    counter++;
                }
            }
        }

        return counter;
    }

    /***************************************************************************
     * <p>
     * Get all metadatatypes, which can be added to a DocStruct. This method considers already added metadata (and persons!); e.g. metadata types
     * which can only be available once cannot be added a second time. Therefore this metadata type will not be included in this list.<br/>
     * 
     * "Internal" metadata, which start with the HIDDEN_METADATA_CHAR, will also not be included.
     * </p>
     * 
     * @return List containing MetadataType objects.
     **************************************************************************/
    public List<MetadataGroupType> getAddableMetadataGroupTypes() {

        // If e.g. the topstruct has no Metadata, or something...
        if (this.type == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataGroupType> addableMetadata = new LinkedList<MetadataGroupType>();
        List<MetadataGroupType> allTypes = this.type.getAllMetadataGroupTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataGroupType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (!mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                String maxnumber = this.type.getNumberOfMetadataGroups(mdt);

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                    // Check metadata here only.
                    List<? extends MetadataGroup> availableMD = this.getAllMetadataGroupsByType(mdt);

                    if (availableMD.size() < 1) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt);
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt);
                }
            }
        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }

    public List<MetadataGroupType> getPossibleMetadataGroupTypes() {
        // If e.g. the topstruct has no Metadata, or something...
        if (this.type == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataGroupType> addableMetadata = new LinkedList<MetadataGroupType>();
        List<MetadataGroupType> allTypes = this.type.getAllMetadataGroupTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataGroupType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            // if (!mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
            String maxnumber = this.type.getNumberOfMetadataGroups(mdt);

            // Metadata can only be available once; so we have to check if
            // it is already available.
            if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                // Check metadata here only.
                List<? extends MetadataGroup> availableMD = this.getAllMetadataGroupsByType(mdt);

                if (availableMD.size() < 1) {
                    // Metadata is NOT available; we are allowed to add it.
                    addableMetadata.add(mdt);
                }
            } else {
                // We can add as many metadata as we want (+ or *).
                addableMetadata.add(mdt);
            }

        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }

    /***************************************************************************
     * <p>
     * Get all metadatatypes, which can be added to a DocStruct. This method considers already added metadata (and persons!); e.g. metadata types
     * which can only be available once cannot be added a second time. Therefore this metadata type will not be included in this list.<br/>
     * 
     * "Internal" metadata, which start with the HIDDEN_METADATA_CHAR, will also not be included.
     * </p>
     * 
     * @return List containing MetadataType objects.
     **************************************************************************/
    public List<MetadataType> getAddableMetadataTypes() {

        // If e.g. the topstruct has no Metadata, or something...
        if (this.type == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataType> addableMetadata = new LinkedList<MetadataType>();
        List<MetadataType> allTypes = this.type.getAllMetadataTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            if (!mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
                String maxnumber = this.type.getNumberOfMetadataType(mdt);

                // Metadata can only be available once; so we have to check if
                // it is already available.
                if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                    // Check metadata here only.
                    List<? extends Metadata> availableMD = this.getAllMetadataByType(mdt);

                    if (!mdt.isPerson && (availableMD.size() < 1)) {
                        // Metadata is NOT available; we are allowed to add it.
                        addableMetadata.add(mdt);
                    }

                    // Then check persons here.
                    boolean used = false;
                    if (mdt.getIsPerson() && this.getAllPersons() != null) {
                        for (Person per : this.getAllPersons()) {
                            // If the person of the current metadata type is
                            // already used, set the flag.
                            if (per.getRole().equals(mdt.getName())) {
                                used = true;
                            }
                        }

                        // Only add the metadata type, if the person was not
                        // already used.
                        if (!used) {
                            addableMetadata.add(mdt);
                        }
                    }
                } else {
                    // We can add as many metadata as we want (+ or *).
                    addableMetadata.add(mdt);
                }
            }
        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }

    public List<MetadataType> getPossibleMetadataTypes() {
        // If e.g. the topstruct has no Metadata, or something...
        if (this.type == null) {
            return null;
        }

        // Get all Metadatatypes for my DocStructType.
        List<MetadataType> addableMetadata = new LinkedList<MetadataType>();
        List<MetadataType> allTypes = this.type.getAllMetadataTypes();

        // Get all metadata types which are known, iterate over them and check,
        // if they are still addable.
        for (MetadataType mdt : allTypes) {

            // Metadata beginning with the HIDDEN_METADATA_CHAR are internal
            // metadata are not user addable.
            // if (!mdt.getName().startsWith(HIDDEN_METADATA_CHAR)) {
            String maxnumber = this.type.getNumberOfMetadataType(mdt);

            // Metadata can only be available once; so we have to check if
            // it is already available.
            if (maxnumber.equals("1m") || maxnumber.equals("1o")) {
                // Check metadata here only.
                List<? extends Metadata> availableMD = this.getAllMetadataByType(mdt);

                if (!mdt.isPerson && (availableMD.size() < 1)) {
                    // Metadata is NOT available; we are allowed to add it.
                    addableMetadata.add(mdt);
                }

                // Then check persons here.
                boolean used = false;
                if (mdt.getIsPerson() && this.getAllPersons() != null) {
                    for (Person per : this.getAllPersons()) {
                        // If the person of the current metadata type is
                        // already used, set the flag.
                        if (per.getRole().equals(mdt.getName())) {
                            used = true;
                        }
                    }

                    // Only add the metadata type, if the person was not
                    // already used.
                    if (!used) {
                        addableMetadata.add(mdt);
                    }
                }
            } else {
                // We can add as many metadata as we want (+ or *).
                addableMetadata.add(mdt);
            }

        }

        if (addableMetadata == null || addableMetadata.isEmpty()) {
            return null;
        }

        return addableMetadata;
    }

    //
    // Handle children.
    //
    // All methods to add, remove, modify or change the position of children in
    // the tree are in here.
    //

    /***************************************************************************
     * <p>
     * Adds a DocStruct object as a child to this instance. The new child will automatically become the last child in the list. When adding a
     * DocStruct, configuration is checked, wether a DocStruct of this type can be added. If not, a TypeNotAllowedAsChildException is thrown. The parent of
     * this child (this instance) is set automatically.
     * </p>
     * 
     * @param inchild DocStruct to be added
     * @return wether inchild isn’t null and its type isn’t null
     * @throws TypeNotAllowedAsChildException if a child should be added, but it's DocStruct type isn't member of this instance's DocStruct type
     **************************************************************************/
    public boolean addChild(DocStruct inchild) throws TypeNotAllowedAsChildException {
    	return addChild((Integer) null, inchild);
    }

	/**
	 * Adds a DocStruct object as a child to this instance. The new child will
	 * become the element at the specified position in the child list while the
	 * element currently at that position (if any) and any subsequent elements
	 * are shifted to the right (so that one gets added to their indices), or
	 * the last child in the list if index is null. When adding a DocStruct,
	 * configuration is checked, wether a DocStruct of this type can be added.
	 * If not, a TypeNotAllowedAsChildException is thrown. The parent of this
	 * child (this instance) is set automatically.
	 * 
	 * @param index
	 *            index at which the child is to be inserted
	 * @param inchild
	 *            DocStruct to be added
	 * @return wether inchild isn’t null and its type isn’t null
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
    public boolean addChild(Integer index, DocStruct inchild) throws TypeNotAllowedAsChildException {

        if (inchild == null || inchild.getType() == null) {
            LOGGER.warn("DocStruct or DocStructType is null");
            return false;
        }

        DocStructType childtype;
        boolean allowed = false;

        // Check, if type of child is allowed.
        childtype = inchild.getType();
        // Get all allowed types.
        for (String tempType : this.type.getAllAllowedDocStructTypes()) {
            if ((childtype.getName()).equals(tempType)) {
                allowed = true;
            }
        }

        if (!allowed) {
            TypeNotAllowedAsChildException tnaace = new TypeNotAllowedAsChildException(childtype);
            LOGGER.error("DocStruct type '" + childtype + "' not allowed as child of type '" + this.getType().getName() + "'");
            throw tnaace;
        }

        // Create List for children, if not already available.
        if (this.children == null) {
            this.children = new LinkedList<DocStruct>();
        }

        // Set status to logical or physical.
        if (this.isLogical()) {
            inchild.setLogical(true);
        }
        if (this.isPhysical()) {
            inchild.setPhysical(true);
        }

		inchild.setParent(this);

		if (index == null) {
			// Add child to end of List.
			children.add(inchild);
		} else {
			children.add(index.intValue(), inchild);
		}

		// Child was added.
		return true;
    }

	/**
	 * Adds a DocStruct object to a child of this instance, where is the
	 * position to add it. The new child will automatically become the last
	 * child in the list. When adding a DocStruct, configuration is checked,
	 * wether a DocStruct of this type can be added. If not, it is not added and
	 * false is returned. The parent of this child is set automatically.
	 * 
	 * @param where
	 *            where to add the DocStruct
	 * @param inchild
	 *            DocStruct to be added
	 * @return true, if child was added; otherwise false
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	public boolean addChild(String where, DocStruct inchild) throws TypeNotAllowedAsChildException {
		if (where == null || inchild == null || inchild.getType() == null) {
			LOGGER.warn("DocStruct or DocStructType is null");
			return false;
		}

		// get next position of index
		int next = where.indexOf(44) + 1;

		return next != 0 ? children.get(Integer.parseInt(where.substring(0, next - 1))).addChild(where.substring(next),
				inchild) : addChild(Integer.valueOf(where), inchild);
	}

    /***************************************************************************
     * <p>
     * Removes a child from this DocStruct object.
     * </p>
     * 
     * @return true, if child was returned; false, if it was (e.g. didn't belong to the children of this DocStruct instance).
     **************************************************************************/
    public boolean removeChild(DocStruct inchild) {

        if (this.children.remove(inchild)) {
            // Delete reference to parent.
            inchild.setParent(null);
            // It's not in the logical tree anymore.
            if (this.isLogical()) {
                inchild.setLogical(false);
            }
            // It's not in the physical tree anymore.
            if (this.isPhysical()) {
                inchild.setPhysical(false);
            }

            // Delete the parent reference.
            inchild.setParent(null);
            return true;
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. The DocStruct to be moved must already be child of this DocStruct.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param position first child has position 1
     * @return true, if successful; otherwise false
     **************************************************************************/
    public boolean moveChild(DocStruct inchild, int position) {

        if (position < 0) {
            return false;
        }
        if (position > this.children.size()) {
            position = this.children.size();
        }
        // Remove child first.
        if (!this.children.remove(inchild)) {
            return false;
        }

        // Add to the new position.
        try {
            this.children.add(position, inchild);
        } catch (UnsupportedOperationException uoe) {
            return false;
        } catch (ClassCastException cce) {
            return false;
        } catch (IllegalArgumentException iae) {
            return false;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. Moves the DocStruct after another child. Both DocStruct objects must already be
     * child of this docstruct object.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param afterchild child after which the DocStruct should be moved to.
     * @return true, if it worked; otherwise false
     **************************************************************************/
    public boolean moveChildafter(DocStruct inchild, DocStruct afterchild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(afterchild)) {
                if (moveChild(inchild, i + 1)) {
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Moves a child to another position in the list of all children. Moves the DocStruct before another child. Both DocStruct objects must already be
     * child of this docstruct object.
     * </p>
     * 
     * @param inchild DocStruct to be moved
     * @param beforechild child before the DocStruct should be moved to.
     * @return true, if it worked; otherwise false
     **************************************************************************/
    public boolean moveChildbefore(DocStruct inchild, DocStruct beforechild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(beforechild)) {
                if (moveChild(inchild, i)) {
                    return true;
                }
                return false;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves the position of a child in the list of all children.
     * </p>
     * 
     * @param inchild DocStruct object, whose position should be retrieved
     * @return position as an integer or -1 if child is not in the list
     **************************************************************************/
    public int getPositionofChild(DocStruct inchild) {

        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(inchild)) {
                return i;
            }
        }

        return -1;
    }

    /***************************************************************************
     * <p>
     * Get the next Child in the list of all children. If the given DocStruct object is NOT a child of the current DocStruct instance, null is
     * returned.
     * </p>
     * 
     * @param inChild DocStruct object
     * @return the next DocStruct object after inChild; if none is available null is returned
     **************************************************************************/
    public DocStruct getNextChild(DocStruct inChild) {

        DocStruct nextchild;
        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // Child found.
            if (test.equals(inChild)) {
                if (i != this.children.size()) {
                    nextchild = this.children.get(i + 1);
                    return nextchild;
                }

                // This is already the last child.
                return null;
            }
        }

        // inChild is not member of children.
        return null;
    }

    /***************************************************************************
     * <p>
     * getPreviousChild returns the previous child.
     * </p>
     * 
     * If there is no previous child or given DocStruct object isn't a child at all null is returned
     * 
     * @param inChild
     * @return
     **************************************************************************/
    public DocStruct getPreviousChild(DocStruct inChild) {

        DocStruct prevchild;
        DocStruct test;

        for (int i = 0; i < this.children.size(); ++i) {
            test = this.children.get(i);
            // CHild found.
            if (test.equals(inChild)) {
                if (i != 0) {
                    prevchild = this.children.get(i - 1);
                    return prevchild;
                }

                // This is already the last child.
                return null;
            }
        }

        // inChild is not member of children.
        return null;
    }

    /***************************************************************************
     * <p>
     * Checks if this structure entity can have another entity of a special kind as a child. The child is NOT added by this method.
     * </p>
     * 
     * @param inType the <code>DocStructType</code> of the child
     * @return true, if it can be added; otherwise false
     **************************************************************************/
    public boolean isDocStructTypeAllowedAsChild(DocStructType inType) {

        List<String> allTypes = this.type.getAllAllowedDocStructTypes();
        String typename = inType.getName();
        String testname;

        for (int i = 0; i < allTypes.size(); ++i) {
            testname = allTypes.get(i);
            // Jep, it's in here.
            if (testname.equals(typename)) {
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Checks, if Metadata of a special kind can be removed. There is ni special function, to check wether persons can be removed. As the
     * <code>Person</code> object is just inheirited from the <code>Metadata</code> it has a <code>MetadataType</code>. Therefor this method can be
     * used, to check if a person is removable or not.
     * </p>
     * 
     * @see #removeMetadata
     * @see #removePerson
     * @param inMDType MetadataType object
     * @return true, if it can be removed; otherwise false
     **************************************************************************/
    public boolean canMetadataGroupBeRemoved(MetadataGroupType inMDType) {

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = this.type.getNumberOfMetadataGroups(inMDType);

        if (typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }

        if (typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Checks, if Metadata of a special kind can be removed. There is ni special function, to check wether persons can be removed. As the
     * <code>Person</code> object is just inheirited from the <code>Metadata</code> it has a <code>MetadataType</code>. Therefor this method can be
     * used, to check if a person is removable or not.
     * </p>
     * 
     * @see #removeMetadata
     * @see #removePerson
     * @param inMDType MetadataType object
     * @return true, if it can be removed; otherwise false
     **************************************************************************/
    public boolean canMetadataBeRemoved(MetadataType inMDType) {

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = this.type.getNumberOfMetadataType(inMDType);

        if (typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }

        if (typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        return true;
    }

    public boolean addPerson(Person in) throws MetadataTypeNotAllowedException, IncompletePersonObjectException {

        // Max number of persons (from configuration).
        String maxnumberallowed = null;
        // Number of persons currently available.
        int number = 0;
        // Store, wether we can or cannot add information.
        boolean insert = false;

        // Check, if person is complete.
        if (in.getType() == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            LOGGER.error("Incomplete data for person metadata");
            throw ipoe;
        }

        // Get MetadataType of this person get MetadataType from docstructType
        // object with the same name.
        MetadataType mdtype = this.type.getMetadataTypeByType(in.getType());
        if (mdtype == null) {
            MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
            LOGGER.error("MetadataType " + in.getType().getName() + " is not available for DocStruct '" + this.getType().getName() + "'");
            throw mtnae;
        }

        // Check, if docstruct may have this person ??? depends on the role
        // value of person.
        maxnumberallowed = this.type.getNumberOfMetadataType(mdtype);

        // Check, if another Person of this type is allowed. How many persons
        // are already available.
        number = countMDofthisType(mdtype.getName());

        // As many as we want (zero or more).
        if (maxnumberallowed.equals("*")) {
            insert = true;
        }
        // One or more.
        if (maxnumberallowed.equals("+") || maxnumberallowed.equals("+")) {
            insert = true;
        }
        // Only one, if we have already one, we cannot add it.
        if (maxnumberallowed.equals("1m") || maxnumberallowed.equals("1o")) {
            if (number < 1) {
                insert = true;
            } else {
                insert = false;
            }
        }

        // We can add this person.
        if (insert) {
            if (this.persons == null) {
                this.persons = new LinkedList<Person>();
            }
            this.persons.add(in);

            return true;
        }

        MetadataTypeNotAllowedException mtnae = new MetadataTypeNotAllowedException();
        LOGGER.error("Person MetadataType '" + in.getType().getName() + "' not allowed for DocStruct '" + this.getType().getName() + "'");
        throw mtnae;
    }

    /***************************************************************************
     * <p>
     * Removes a Person object.
     * </p>
     * 
     * @param in Person object to be removed
     * @param force if set to true, person is removed, even if invalid document is the result
     * @return true, if removed; otherwise false
     * @throws IncompletePersonObjectException if the first parameter is not a complete person object
     **************************************************************************/
    public boolean removePerson(Person in, boolean force) throws IncompletePersonObjectException {

        if (this.persons == null) {
            return false;
        }

        MetadataType inMDType = in.getType();
        // Incomplete person.
        if (inMDType == null) {
            IncompletePersonObjectException ipoe = new IncompletePersonObjectException();
            LOGGER.error("Incomplete data for person metadata '" + in.getType().getName() + "'");
            throw ipoe;
        }

        // How many metadata of this type do we have already.
        int typesavailable = countMDofthisType(inMDType.getName());
        // How many types must be at least available.
        String maxnumbersallowed = this.type.getNumberOfMetadataType(inMDType);

        if (force && typesavailable == 1 && maxnumbersallowed.equals("+")) {
            // There must be at least one.
            return false;
        }
        if (force && typesavailable == 1 && maxnumbersallowed.equals("1m")) {
            // There must be at least one.
            return false;
        }

        this.persons.remove(in);

        return true;
    }

    /***************************************************************************
     * @param in
     * @return true, if removed; otherwise false
     * @throws IncompletePersonObjectException
     **************************************************************************/
    public boolean removePerson(Person in) throws IncompletePersonObjectException {
        return removePerson(in, false);
    }

    /***************************************************************************
     * <p>
     * Get a list of all Person objects.
     * </p>
     * 
     * @return List containing Person objects; if no such objects are available null is returned.
     **************************************************************************/
    public List<Person> getAllPersons() {

        if (this.persons == null || this.persons.isEmpty()) {
            return null;
        }

        return this.persons;
    }

    /***************************************************************************
     * @return the logical
     **************************************************************************/
    public boolean isLogical() {
        return this.logical;
    }

    /***************************************************************************
     * @param logical the logical to set
     **************************************************************************/
    public void setLogical(boolean logical) {

        this.logical = logical;

        List<DocStruct> childList = this.getAllChildren();
        if (childList != null) {
            for (DocStruct ds : childList) {
                ds.setLogical(logical);
            }
        }
    }

    /***************************************************************************
     * @deprecated
     * @return the orig_object
     **************************************************************************/
    @Deprecated
    public Object getOrig_object() {
        return this.origObject;
    }

    /***************************************************************************
     * @param orig_object the orig_object to set
     * @deprecated
     **************************************************************************/
    @Deprecated
    public void setOrig_object(Object theOrigObject) {
        this.origObject = theOrigObject;
    }

    /***************************************************************************
     * @return theOrigObject
     **************************************************************************/
    public Object getOrigObject() {
        return this.origObject;
    }

    /***************************************************************************
     * @param theOrigObject theOrigObject to set
     **************************************************************************/
    public void setOrigObject(Object theOrigObject) {
        this.origObject = theOrigObject;
    }

    /***************************************************************************
     * @return the physical
     **************************************************************************/
    public boolean isPhysical() {
        return this.physical;
    }

    /***************************************************************************
     * @param physical the physical to set
     **************************************************************************/
    public void setPhysical(boolean physical) {

        this.physical = physical;

        List<DocStruct> childList = this.getAllChildren();
        if (childList != null) {
            for (DocStruct ds : childList) {
                ds.setPhysical(physical);
            }
        }
    }

    /***************************************************************************
     * <p>
     * Creates a list of metadata and persons to be displayed in a MetadataForm. The list is based on the <code>DefaultDisplay</code> attribute in the
     * preference file (in each metadata element). This list includes metadata and person objects which already exist (and have content) and empty
     * objects (objects without any content), which are created by this method. These emtpy objects are not only added to the list, but also to the
     * internal Metadata list and person list of the this DocStruct instance. After the form has been displayed an processed, you may want to call the
     * method <code>deleteUnusedPersonsAndMetadata()</code> to delete unused objects created by this method.
     * </p>
     * 
     * @param lang language name to be used for sorting the list
     * @param personsTop if true, person objects are at the beginning of the list, otherwise at the end
     * @return a List containing Metadata and Person objects
     * @throws MetadataTypeNotAllowedException
     **************************************************************************/
    public List<Metadata> showMetadataForm(String lang, boolean personsTop) throws MetadataTypeNotAllowedException {

        // Get all MetadataType elements which have the DefaultDisplay attribute
        // set.
        List<MetadataType> dmt = this.getDefaultDisplayMetadataTypes();

        List<Metadata> allMDs = this.getAllMetadata();
        // No default metadata.
        if (dmt == null) {
            return null;
        }

        // Iterator over DMT.
        Iterator<MetadataType> mdtIterator = dmt.iterator();
        while (mdtIterator.hasNext()) {
            MetadataType mdt = mdtIterator.next();

            // Check, if mdt is already in the allMDs Metadata list.
            boolean notIncluded = true;
            for (int i = 0; i < allMDs.size(); i++) {
                Metadata md = allMDs.get(i);
                MetadataType mdt2 = md.getType();

                // Compare the display MetadataType and the type of current
                // Metadata.
                if (mdt.getName().equals(mdt2.getName())) {
                    // Is included; need not to be displayed seperatly.
                    notIncluded = false;
                    break;
                }
            }

            // Create new Metadata or Person element.
            if (notIncluded) {
                if (mdt.isPerson) {
                    // It's a person, create person element.
                    Person psFoo = new Person(mdt);
                    // The role is the name of the metadata type.
                    psFoo.setRole(mdt.getName());
                    try {
                        // Add this new metadata element.
                        this.addPerson(psFoo);
                    } catch (DocStructHasNoTypeException e) {
                        continue;
                    } catch (MetadataTypeNotAllowedException e) {
                        continue;
                    }
                } else {
                    // It's metadata, so create a new Metadata element.
                    Metadata metaFoo = new Metadata(mdt);
                    try {
                        // Add this new metadata element.
                        this.addMetadata(metaFoo);
                    } catch (DocStructHasNoTypeException e) {
                        continue;
                    } catch (MetadataTypeNotAllowedException e) {
                        continue;
                    }
                }
            }
        }

        // Sort all Metadata by typename.
        LinkedList<Metadata> resultList = new LinkedList<Metadata>();

        for (Metadata md : this.getAllMetadata()) {
            // If nothing is in the result list, just add it.
            if (resultList.size() == 0) {
                resultList.add(md);
                // Continue with next iteration.
                continue;
            }

            String compare = md.getType().getNameByLanguage(lang);

            // Iterate over result list and find position for the metadata.
            boolean elementinserted = false;
            for (int i = 0; i < resultList.size(); i++) {
                Metadata mdcomp = resultList.get(i);
                String mdcompLang = mdcomp.getType().getNameByLanguage(lang);

                // Compare both strings.
                if (compare.compareTo(mdcompLang) < 0 || compare.compareTo(mdcompLang) == 0) {
                    // Add md before mdcomp.
                    resultList.add(i, md);
                    elementinserted = true;
                    // Get out of loop.
                    break;
                }
            }

            // If metadata element has not been inserted, we insert it to the
            // end.
            if (!elementinserted) {
                resultList.addLast(md);
            }

        }

        // Currently we don't sort Persons; we simple add Persons on the top or
        // the end of the resultList.
        if (this.getAllPersons() != null && !this.getAllPersons().isEmpty()) {
            // Just add persons, if any person is available.
            if (personsTop) {
                // On top of list.
                resultList.addAll(0, this.getAllPersons());
            } else {
                // At end of list..
                resultList.addAll(this.getAllPersons());
            }
        }

        // The Result list contains Persons and Metadata in one list.
        return resultList;
    }

    /***************************************************************************
     * <p>
     * This method cleans the metadata list and person list of instances which do not have a value (empty objects). This method is usually used in
     * connection with the <code>showMetadataForm</code> method. After the <code>showMetadataForm</code> has been called and the form has been
     * displayed, this method should be called to delete the created empty metadata instances by the <code>showMetadataForm</code> method.
     * </p>
     * 
     * <p>
     * An empty metadata instance is:
     * 
     * <ul>
     * <li>A metadata object with a value of null.</li>
     * <li>A person object with neither a lastname nor a firstname nor an identifier nor an institution.</li>
     * </ul>
     * 
     * </p>
     **************************************************************************/
    public void deleteUnusedPersonsAndMetadata() {

        // Handle Persons first: Person objects are available.
        if (this.getAllPersons() != null) {
            List<Person> personlist = this.getAllPersons();
            // Copy person list, so we can iterate over this list and delete
            // from the persons list.
            List<Person> iteratorList = new LinkedList<Person>(personlist);
            for (Person per : iteratorList) {
                if (per.getLastname() == null && per.getFirstname() == null && per.getInstitution() == null) {
                    // Delete this person from list of all Persons.
                    if (this.getAllPersons() != null) {
                        this.getAllPersons().remove(per);
                    }
                }
            }
        }

        // Handle Metadata: Metadata objects are available.
        if (this.getAllMetadata() != null) {
            List<Metadata> metadatalist = this.getAllMetadata();
            // Copy Metadata list, so we can iterate over this list and delete
            // from the metadata list.
            List<Metadata> iteratorList = new LinkedList<Metadata>(metadatalist);
            for (Metadata md : iteratorList) {
                if (md.getValue() == null) {
                    if (this.getAllMetadata() != null) {
                        // Delete the metadata element.
                        this.getAllMetadata().remove(md);
                    }
                }
            }
        }

        if (this.getAllMetadataGroups() != null) {
            List<MetadataGroup> metadatalist = this.getAllMetadataGroups();

            List<MetadataGroup> iteratorList = new LinkedList<MetadataGroup>(metadatalist);
            for (MetadataGroup md : iteratorList) {
                boolean isEmpty = true;
                for (Metadata meta : md.getMetadataList()) {
                    if (meta.getValue() != null) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    this.getAllMetadataGroups().remove(md);
                }
            }
        }
    }

    /***************************************************************************
     * <p>
     * Sorts the metadata and persons in the current DocStruct according to their occurrence in the preferences file.
     * </p>
     * 
     * @param thePrefs
     **************************************************************************/
    public synchronized void sortMetadata(Prefs thePrefs) {

        List<Metadata> newMetadata = new LinkedList<Metadata>();
        List<Person> newPersons = new LinkedList<Person>();
        List<Metadata> oldMetadata = new LinkedList<Metadata>();
        List<Person> oldPersons = new LinkedList<Person>();

        if (this.allMetadata != null) {
            oldMetadata = new LinkedList<Metadata>(this.allMetadata);
        }
        if (this.persons != null) {
            oldPersons = new LinkedList<Person>(this.persons);
        }

        // Get all MetadataTypes defined in the prefs for this DocStruct.
        DocStructType docStructType = thePrefs.getDocStrctTypeByName(this.getType().getName());

        // If the DocStructType is NOT existing, we have no metadata to sort,
        // just do return.
        if (docStructType == null) {
            return;
        }

        List<MetadataType> prefsMetadataTypeList = docStructType.getAllMetadataTypes();

        // Iterate over all that metadata types.
        for (MetadataType mType : prefsMetadataTypeList) {

            // Go through all persons of the current DocStruct.
            List<Person> op = this.getAllPersons();
            if (op != null && !op.isEmpty()) {
                for (Person p : op) {
                    if (p.getType() != null && mType.getName().equals(p.getType().getName())) {
                        // Add to the new list and remove from the old, if names
                        // do match.
                        newPersons.add(p);
                        oldPersons.remove(p);
                    }
                }
            }

            // Go throught all metadata of the curretn DocStruct.
            List<Metadata> om = this.getAllMetadata();
            if (om != null && !om.isEmpty()) {
                for (Metadata m : om) {
                    if (mType.getName().equals(m.getType().getName())) {
                        // Add to the new list and remove from the old, if names
                        // do match.
                        newMetadata.add(m);
                        oldMetadata.remove(m);
                    }
                }
            }
        }

        // Add left-over types.
        if (oldPersons != null && oldPersons.size() > 0) {
            newPersons.addAll(oldPersons);
        }
        if (oldMetadata != null && oldMetadata.size() > 0) {
            newMetadata.addAll(oldMetadata);
        }

        // Re-set the lists.
        this.allMetadata = newMetadata;
        this.persons = newPersons;

        // TODO groups
    }

    /***************************************************************************
     * <p>
     * Sorts the metadata and persons in the current DocStruct alphabetically.
     * </p>
     **************************************************************************/
    public synchronized void sortMetadataAbcdefg() {

        // Create empty (sorted) TreeSets and lists.
        TreeSet<Metadata> newMetadata = new TreeSet<Metadata>(new MetadataComparator());
        TreeSet<Person> newPersons = new TreeSet<Person>(new MetadataComparator());
        List<Metadata> metadataList = new LinkedList<Metadata>();
        List<Person> personList = new LinkedList<Person>();

        // Add all metadata to the new TreeSets (sorted).
        if (this.allMetadata != null) {
            newMetadata.addAll(this.allMetadata);
        }
        if (this.persons != null) {
            newPersons.addAll(this.persons);
        }

        // Re-transfer the sorted sets to the linked lists.
        metadataList.addAll(newMetadata);
        personList.addAll(newPersons);

        // Re-set the lists.
        this.allMetadata = metadataList;
        this.persons = personList;
        // TODO groups
    }

    /****************************************************************************
     * <p>
     * Used to register a signature the first time a DocStruct Object runs into the non hierarchial branch of referenced DocStruct Objects by way of
     * the equals method.
     * </p>
     * 
     * @author Wulf Riebensahm
     * @param docStruct
     ****************************************************************************/
    private boolean registerToRef(DocStruct docStruct) {

        if (this.signaturesForEqualsMethodRefsTo == null) {
            this.signaturesForEqualsMethodRefsTo = new HashMap<String, Object>();
        }

        // If not null then we have the case of looping, then we must return
        // false here.
        if (this.signaturesForEqualsMethodRefsTo.get(docStruct.toString()) != null) {
            return false;
        }

        this.signaturesForEqualsMethodRefsTo.put(docStruct.toString(), docStruct);
        return true;
    }

    /****************************************************************************
     * <p>
     * Used to register a signature the first time a DocStruct Object runs into the non hierarchial branch of DocStruct Objects referencing this
     * DocStruct by way of the equals method.
     * </p>
     * 
     * @author Wulf Riebensahm
     * @param docStruct
     ****************************************************************************/
    private boolean registerFromRef(DocStruct docStruct) {

        if (this.signaturesForEqualsMethodRefsFrom == null) {
            this.signaturesForEqualsMethodRefsFrom = new HashMap<String, Object>();
        }

        // If not null then we have the case of looping, then we must return
        // false here.
        if (this.signaturesForEqualsMethodRefsFrom.get(docStruct.toString()) != null) {
            return false;
        }

        this.signaturesForEqualsMethodRefsFrom.put(docStruct.toString(), docStruct);
        return true;
    }

    /**************************************************************************
     * @author Wulf Riebensahm
     * @param docStruct
     **************************************************************************/
    private void unregisterToRefs(DocStruct docStruct) {

        this.signaturesForEqualsMethodRefsTo.remove(docStruct.toString());
        // Set to null if no element is left.
        if (this.signaturesForEqualsMethodRefsTo.size() == 0) {
            this.signaturesForEqualsMethodRefsTo = null;
        }
    }

    /**************************************************************************
     * @author Wulf Riebensahm
     * @param docStruct
     **************************************************************************/
    private void unregisterFromRefs(DocStruct docStruct) {

        this.signaturesForEqualsMethodRefsFrom.remove(docStruct.toString());
        // Sset to null if no element is left.
        if (this.signaturesForEqualsMethodRefsFrom.size() == 0) {
            this.signaturesForEqualsMethodRefsFrom = null;
        }
    }

    /**************************************************************************
     * @author Wulf Riebensahm
     * @param docStruct
     * @return
     **************************************************************************/
    public boolean equals(DocStruct docStruct) {

        LOGGER.debug("\r\n" + this.getClass() + " ->id:" + this.getType().getName() + " other:" + docStruct.getType().getName() + "\r\n");

        // Simple attributes.
        if (this.isLogical() != docStruct.isLogical()) {
            LOGGER.debug("isLogical=false");
            return false;
        }

        if (this.isPhysical() != docStruct.isPhysical()) {
            LOGGER.debug("isPhysical=false");
            return false;
        }

        if (!((this.getReferenceToAnchor() == null && docStruct.getReferenceToAnchor() == null) || this.getReferenceToAnchor().equals(
                docStruct.getReferenceToAnchor()))) {
            LOGGER.debug("getreferenceAnchor=false");
            return false;
        }

        // Compare types.
        if (!this.getType().equals(docStruct.getType())) {
            LOGGER.debug("getType=false");
            return false;
        }

        // ListPairCheck.isNotEqual is returned, if one List Object is null
        // while the other List Object refers to an instance. In this case
        // equals can already return false.
        // If needsFurtherChecking is returned we need to compare the instances,
        // or rather the instances of the listed Objects.
        // For a quick test in this case we first compare the number referenced
        // objects contained in the lists: If the number of referenced objects
        // already differs, equals again can return false already.
        // Only if also the number of Objects in the lists is the same we need
        // an exhausting in depth comparism of the Objects contained.
        // Simply using the List.equals method doesn't help us, because the
        // lists may only have two separate instances of equal objects but
        // never the same instances.
        ListPairCheck lpcResult = null;

        // Metadata.
        lpcResult = DigitalDocument.quickPairCheck(this.getAllMetadata(), docStruct.getAllMetadata());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("1 false returned");
            return false;
        }
        if (lpcResult == ListPairCheck.needsFurtherChecking && this.getAllMetadata().size() != docStruct.getAllMetadata().size()) {
            LOGGER.debug("2 false returned");
            return false;
        }

        // DocStructs (children).
        lpcResult = DigitalDocument.quickPairCheck(this.getAllChildren(), docStruct.getAllChildren());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("3 false returned");
            return false;
        }
        if (lpcResult == ListPairCheck.needsFurtherChecking && this.getAllChildren().size() != docStruct.getAllChildren().size()) {
            LOGGER.debug("4 false returned");
            return false;
        }

        // FileReferences.
        lpcResult = DigitalDocument.quickPairCheck(this.getAllContentFileReferences(), docStruct.getAllContentFileReferences());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("5 false returned");
            return false;
        }

        if (lpcResult == ListPairCheck.needsFurtherChecking
                && this.getAllContentFileReferences().size() != docStruct.getAllContentFileReferences().size()) {
            LOGGER.debug("6 false returned");
            return false;
        }

        // Persons.
        lpcResult = DigitalDocument.quickPairCheck(this.getAllPersons(), docStruct.getAllPersons());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("7 false returned");
            return false;
        }
        if (lpcResult == ListPairCheck.needsFurtherChecking && this.getAllPersons().size() != docStruct.getAllPersons().size()) {
            LOGGER.debug("8 false returned");
            return false;
        }

        // To references.
        lpcResult = DigitalDocument.quickPairCheck(this.getAllToReferences(), docStruct.getAllToReferences());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("9 false returned");
            return false;
        }
        if (lpcResult == ListPairCheck.needsFurtherChecking && this.getAllToReferences().size() != docStruct.getAllToReferences().size()) {
            LOGGER.debug("10 false returned");
            return false;
        }

        // From references.
        lpcResult = DigitalDocument.quickPairCheck(this.getAllFromReferences(), docStruct.getAllFromReferences());
        if (lpcResult == ListPairCheck.isNotEqual) {
            LOGGER.debug("11 false returned");
            return false;
        }
        if (lpcResult == ListPairCheck.needsFurtherChecking && this.getAllFromReferences().size() != docStruct.getAllFromReferences().size()) {
            LOGGER.debug("12 false returned");
            return false;
        }

        // If we got this far we need to take a deeper look into the referenced
        // Objects trying to find a match, only if no match is found we can
        // exclude that the compared Objects are equal.
        boolean flagFound = false;

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllChildren(), docStruct.getAllChildren()) != DigitalDocument.ListPairCheck.isEqual) {

            for (DocStruct ds1 : this.getAllChildren()) {
                int i = this.getAllChildren().indexOf(ds1);
                if (!ds1.equals(docStruct.getAllChildren().get(i))) {
                    return false;
                }
            }
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllMetadata(), docStruct.getAllMetadata()) != DigitalDocument.ListPairCheck.isEqual) {
            for (Metadata md1 : this.getAllMetadata()) {
                flagFound = false;

                for (Metadata md2 : docStruct.getAllMetadata()) {
                    if (md1.equals(md2)) {
                        LOGGER.debug("equals=true: MD1=" + md1.getType().getName() + ";MD2=" + md2.getType().getName());
                        flagFound = true;
                        break;
                    }

                    LOGGER.debug("equals=false: MD1=" + md1.getType().getName() + ", MD2=" + md2.getType().getName());
                }

                // If equal Metadata couldn't be found this DocStruct cannot be
                // equal either.
                if (!flagFound) {
                    return false;
                }
            }
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllMetadataGroups(), docStruct.getAllMetadataGroups()) != DigitalDocument.ListPairCheck.isEqual) {

            for (MetadataGroup md1 : this.getAllMetadataGroups()) {
                flagFound = false;

                for (MetadataGroup md2 : docStruct.getAllMetadataGroups()) {
                    if (md1.equals(md2)) {
                        LOGGER.debug("equals=true: MD1=" + md1.getType().getName() + ";MD2=" + md2.getType().getName());
                        flagFound = true;
                        break;
                    }

                    LOGGER.debug("equals=false: MD1=" + md1.getType().getName() + ", MD2=" + md2.getType().getName());
                }

                // If equal Metadata couldn't be found this DocStruct cannot be
                // equal either.
                if (!flagFound) {
                    return false;
                }
            }
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllPersons(), docStruct.getAllPersons()) != DigitalDocument.ListPairCheck.isEqual) {

            for (Person p1 : this.getAllPersons()) {
                flagFound = false;
                for (Person p2 : docStruct.getAllPersons()) {
                    if (p1.equals(p2)) {
                        flagFound = true;
                        break;
                    }
                }
                // If equal Person couldn't be found this DocStruct cannot be
                // equal either.
                if (!flagFound) {
                    LOGGER.debug("15 false returned");
                    return false;
                }
            }
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllContentFileReferences(), docStruct.getAllContentFileReferences()) != DigitalDocument.ListPairCheck.isEqual) {

            // flagFound = true;
            for (ContentFileReference cfr1 : this.getAllContentFileReferences()) {
                int i = this.getAllContentFileReferences().indexOf(cfr1);
                if (!cfr1.equals(docStruct.getAllContentFileReferences().get(i))) {
                    return false;
                }

                /*
                 * for (ContentFileReference cfr2 : docStruct .getAllContentFileReferences()) { if (cfr1.equals(cfr2)) { flagFound = true; break; } }
                 * if (!flagFound) { LOGGER.debug("16 false returned"); return false; }
                 */
            }
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllFromReferences(), docStruct.getAllFromReferences()) != DigitalDocument.ListPairCheck.isEqual) {// now
            // The tricky part: before we go in to the equal method of the next
            // DocStruct we have to register the signature and respectively
            // check if signature is already listed if signature is already
            // listed then we entered a loop and equals has to return true.

            // This interrupts the loop acknowledging that docStruct had been
            // here before - unregister is done one loop down the stack.
            if (!registerFromRef(docStruct)) {
                return true;
            }

            for (Reference rf1 : this.getAllFromReferences()) {
                flagFound = false;

                for (Reference rf2 : docStruct.getAllFromReferences()) {
                    if (rf1.getTarget().equals(rf2.getTarget())) {
                        flagFound = true;
                        break;
                    }
                }

                if (!flagFound) {
                    unregisterFromRefs(docStruct);
                    LOGGER.debug("17 false returned");
                    return false;
                }
            }

            unregisterFromRefs(docStruct);
        }

        // If both lists are null, isEqual is returned, no in depth check
        // needed.
        if (DigitalDocument.quickPairCheck(this.getAllToReferences(), docStruct.getAllToReferences()) != DigitalDocument.ListPairCheck.isEqual) {

            // Interrupt the loop.
            if (!registerToRef(docStruct)) {
                return true;
            }

            for (Reference rt1 : this.getAllToReferences()) {
                flagFound = false;

                for (Reference rt2 : docStruct.getAllToReferences()) {
                    if (rt1.getTarget().equals(rt2.getTarget())) {
                        flagFound = true;
                        break;
                    }
                }

                if (!flagFound) {
                    unregisterToRefs(docStruct);
                    LOGGER.debug("18 false returned");
                    return false;
                }
            }

            unregisterToRefs(docStruct);
        }

        // Finally we are through and can assume that this DocStruct is the same
        // as parameter docStruct and we return true.
        return true;
    }

    /***************************************************************************
     * <p>
     * The metadata comparator. Simply compares metadata (and persons) according to their type names alphabetically.
     * </p>
     * 
     * @author funk
     **************************************************************************/
    class MetadataComparator implements Comparator<Object> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
		public int compare(Object o1, Object o2) {

            Metadata m1 = (Metadata) o1;
            Metadata m2 = (Metadata) o2;

            if (m1.getType().getName().equals(m2.getType().getName())) {
                return 0;
            }

            return m1.getType().getName().compareTo(m2.getType().getName());
        }

    }

    /***************************************************************************
     * <p>
     * The metadata comparator. Simply compares metadata (and persons) according to their type names alphabetically.
     * </p>
     * 
     * @author funk
     **************************************************************************/
    class MetadataGroupComparator implements Comparator<Object> {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
		public int compare(Object o1, Object o2) {

            MetadataGroup m1 = (MetadataGroup) o1;
            MetadataGroup m2 = (MetadataGroup) o2;

            return m1.getType().getName().compareTo(m2.getType().getName());
        }

    }

    public AmdSec getAmdSec() {
        return amdSec;
    }

    public void setAmdSec(AmdSec amdSec) {
        this.amdSec = amdSec;
    }

    public List<Md> getTechMds() {
        return techMdList;
    }

    public void addTechMd(Md techMd) {
        if (techMdList == null) {
            techMdList = new ArrayList<Md>();
        }
        if (techMd != null) {
            techMdList.add(techMd);
        }
    }

    public void setTechMds(List<Md> mds) {
        if (mds != null) {
            this.techMdList = mds;
        }
    }

    public String getImageName() {
        if (contentFileReferences != null && !contentFileReferences.isEmpty()) {
            for (ContentFileReference cfr : contentFileReferences) {
                if (cfr.getCf() != null) {
                    String location = cfr.getCf().getLocation();
                    if (location != null && location.length() > 0) {
                        File imagefile = new File(location);
                        return imagefile.getName();
                    }
                }
            }
        }
        return null;
    }

    public void setImageName(String newfilename) {
        if (contentFileReferences != null && !contentFileReferences.isEmpty()) {
            for (ContentFileReference cfr : contentFileReferences) {
                if (cfr.getCf() != null) {
                    cfr.getCf().setLocation(newfilename);
                    return;
                } else {
                    ContentFile cf = new ContentFile();
                    cf.setLocation(newfilename);
                    cfr.setCf(cf);
                    return;
                }
            }
        } else {
            ContentFile cf = new ContentFile();
            cf.setLocation(newfilename);
            this.addContentFile(cf);
        }
    }

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this DocStruct, or throws an exception. More formally, returns the lowest
	 * index of the DocStruct in this DocStruct. If there is no such index, a
	 * NoSuchElementException will be thrown.
	 * 
	 * @param d
	 *            DocStruct to search for
	 * @return the index of the first occurrence of the specified DocStruct in
	 *         this DocStruct, separated by comma
	 * @throws NoSuchElementException
	 *             if this DocStruct does not contain the DocStruct
	 */
	public String indexOf(DocStruct d) throws NoSuchElementException {
		return indexOf(d, null);
	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this DocStruct after the specified index or throws an exception. More
	 * formally, returns the lowest index of the DocStruct in this DocStruct
	 * after the index. If there is no such index, a NoSuchElementException will
	 * be thrown. The search will be fast-forwarded to the specified element so
	 * that the search will continue right after the specified element. If there
	 * is no such index, a NoSuchElementException will be thrown.
	 * 
	 * @param d
	 *            DocStruct to search for
	 * @param afterIndex
	 *            index after which to start searching
	 * @return the index of the first occurrence of the specified DocStruct in
	 *         this DocStruct, separated by comma
	 * @throws NoSuchElementException
	 *             if this DocStruct does not contain the DocStruct
	 */
	public String indexOf(DocStruct d, String afterIndex) throws NoSuchElementException {
		int from = 0;
		String subIndex = null;
		if (afterIndex != null) {
			int comma = afterIndex.indexOf(',');
			if (comma >= 0) {
				from = Integer.parseInt(afterIndex.substring(0, comma));
				subIndex = afterIndex.substring(comma + 1);
			} else {
				from = Integer.parseInt(afterIndex) + 1;
			}
		}

		if (children != null) {
			for (int i = from; i < children.size(); i++) {
				DocStruct child = children.get(i);
				if (subIndex == null && child.equals(d)) {
					return Integer.toString(i);
				}
				try {
					return Integer.toString(i) + ',' + child.indexOf(d, subIndex);
				} catch (NoSuchElementException go_on) {
				}
				subIndex = null;
			}
		}
		throw new NoSuchElementException("No " + d + " in " + this);
	}

	/**
	 * Retrieves the name of the anchor structure, if any, or null otherwise.
	 * Anchors are a special type of document structure, which group other
	 * structure entities together, but have no own content. Imagine a
	 * periodical as such an anchor. The periodical itself is a virtual
	 * structure entity without any own content, but groups all years of
	 * appearance together. Years may be anchors again for volumes, etc.
	 * 
	 * @return String, which is null, if it cannot be used as an anchor
	 */
	public String getAnchorClass() {
		if (type == null) {
			return null;
		}
		return type.getAnchorClass();
	}

	/**
	 * The function getAllAnchorClasses() traverses the structure tree and
	 * returns an ordered list of all anchor classes that are used by this
	 * structure.
	 * 
	 * @return an ordered collection of all used anchors
	 * @throws PreferencesException
	 *             if an anchor class name is encountered a second time after
	 *             having been descending right into a hierarchy to be
	 *             maintained in another anchor class already
	 */
	public Collection<String> getAllAnchorClasses() throws PreferencesException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		String anchorClass = getAnchorClass();
		if (anchorClass != null) {
			result.add(anchorClass);
			List<DocStruct> docStructs = getAllRealSuccessors();
			do {
				anchorClass = null;
				List<DocStruct> nextLevel = new LinkedList<DocStruct>();
				for (DocStruct docStruct : docStructs) {
					String ancora = docStruct.getAnchorClass();
					if (ancora != null) {
						if (anchorClass == null) {
							anchorClass = ancora;
						} else if (!anchorClass.equals(ancora)) {
							throw new PreferencesException(
									"All real successors of an anchor class that are of an anchor class themselves "
											+ "must belong to the same anchor class. The given logical document "
											+ "structure in combination with the anchor names configured would result "
											+ "in the hierarchical level " + docStruct.getParent().getType().getName()
											+ "\u200A\u2014\u200Abelonging to the anchor class "
											+ docStruct.getParent().getType().getAnchorClass() + "\u200A\u2014\u200Ato"
											+ " have children which belong to the different anchor classes "
											+ anchorClass + " and " + ancora + ", which is not supported.");
						}
						nextLevel.addAll(docStruct.getAllRealSuccessors());
					}
				}
				if(anchorClass != null && !result.add(anchorClass)) {
					String last = "";
					for (String entry : result) {
						last = entry;
					}
					throw new PreferencesException(
							"All levels of the logical document structure that belong to the same anchor file must "
									+ "immediately  follow each other as children. The given logical document "
									+ "structure in combination with the anchor names configured would result in an "
									+ "interruption of the elements being stored in the " + anchorClass + " anchor by "
									+ "elements to be stored in the " + last + " anchor,  which isn’t possible.");
				}
				docStructs = nextLevel;
			} while (docStructs.size() > 0);
		}
		return result;
	}

	/**
	 * The function getChild() returns a child element from this structural
	 * entity by numeric reference.
	 * 
	 * @param reference
	 *            reference to the child entity to get
	 * @return child entity, if found
	 * @throws IndexOutOfBoundsException
	 *             if the child indicated cannot be reached
	 */
	public DocStruct getChild(String reference) {
		int fieldSeparator;
		if ((fieldSeparator = reference.indexOf(',')) > -1) {
			int index = Integer.parseInt(reference.substring(0, fieldSeparator));
			return children.get(index).getChild(reference.substring(fieldSeparator + 1));
		} else {
			return children.get(Integer.parseInt(reference));
		}
	}

	/**
	 * The function addMetadata() adds a meta data field with the given name to
	 * this DocStruct and sets it to the given value.
	 * 
	 * @param fieldName
	 *            name of the meta data field to add
	 * @param value
	 *            value to set the field to
	 * @return the object, to be able to write several statements in-line
	 * @throws MetadataTypeNotAllowedException
	 *             if no corresponding MetadataType object is returned by
	 *             getAddableMetadataTypes()
	 */
	public DocStruct addMetadata(String fieldName, String value) throws MetadataTypeNotAllowedException {
		boolean success = false;
		for (MetadataType fieldType : type.getAllMetadataTypes()) {
			if (fieldType.getName().equals(fieldName)) {
				Metadata field = new Metadata(fieldType);
				field.setValue(value);
				addMetadata(field);
				success = true;
				break;
			}
		}
		if (!success) {
			throw new MetadataTypeNotAllowedException("Couldn’t add " + fieldName + " to " + type.getName()
					+ ": No corresponding MetadataType object in result of DocStruc.getAllMetadataTypes().");
		}
		return this;
	}

	/**
	 * The function createChild() creates a child DocStruct below a DocStruct.
	 * This is a convenience function to add a DocStruct by its type name
	 * string.
	 * 
	 * @param type
	 *            structural type of the child to create
	 * @param caudexDigitalis
	 *            act to create the child in
	 * @param ruleset
	 *            rule set the act is based on
	 * @return the child created
	 * @throws TypeNotAllowedForParentException
	 *             is thrown, if this DocStruct is not allowed for a parent
	 * @throws TypeNotAllowedAsChildException
	 *             if a child should be added, but it's DocStruct type isn't
	 *             member of this instance's DocStruct type
	 */
	public DocStruct createChild(String type, DigitalDocument caudexDigitalis, Prefs ruleset)
			throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
		DocStruct result = caudexDigitalis.createDocStruct(ruleset.getDocStrctTypeByName(type));
		addChild(result);
		return result;
	}

	/**
	 * The function getChild() returns a child of a DocStruct, identified by its
	 * type and an identifier in a meta data field of choice. More formally,
	 * returns the first child matching the given conditions and does not work
	 * recursively. If no matching child is found, throws
	 * NoSuchElementException.
	 * 
	 * @param type
	 *            structural type of the child to locate
	 * @param identifierField
	 *            meta data field that holds the identifer to locate the child
	 * @param identifier
	 *            identifier of the child to locate
	 * @return the child, if found
	 * @throws NoSuchElementException
	 *             if no matching child is found
	 */
	public DocStruct getChild(String type, String identifierField, String identifier) throws NoSuchElementException {
		List<DocStruct> children = getAllChildrenByTypeAndMetadataType(type, identifierField);
		if (children == null) {
			children = Collections.emptyList();
		}
		for (DocStruct child : children) {
			for (Metadata metadataElement : child.getAllMetadata()) {
				if (metadataElement.getType().getName().equals(identifierField)
						&& metadataElement.getValue().equals(identifier)) {
					return child;
				}
			}
		}
		throw new NoSuchElementException("No child " + type + " with " + identifierField + " = " + identifier + " in "
				+ this + '.');
	}

	/**
	 * The function getMetadataByType() returns a list of all meta data elements
	 * that are associated with this element and of a given type.
	 * 
	 * @param typeName
	 *            name of the type of meta data to look for
	 * @return a list of all meta data elements of that type
	 */
	public List<Metadata> getMetadataByType(String typeName) {
		LinkedList<Metadata> result = new LinkedList<Metadata>();
		if (allMetadata != null) {
			for (Metadata metadata : allMetadata) {
				if (metadata.getType().getName().equals(typeName)) {
					result.add(metadata);
				}
			}
		}
		return result;
	}

	/**
	 * The function toString() returns a concise but informative representation
	 * of this DocStruct that is easy for a person to read.
	 * 
	 * The toString method for class DocStruct returns a string consisting of
	 * the type name of which the DocStruct is an instance, an (optionally
	 * truncated) identifier, if one is found, and the children of the
	 * DocStruct, if any.
	 * 
	 * @return a string representation of the DocStruct
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int EM_DASH = 0x2014;
		final int HORIZONTAL_ELLIPSIS = 0x2026;
		final short MAX_CHARS = 12;

		StringBuilder result = new StringBuilder();
		if (type == null || type.getName() == null) {
			result.append(identify(this));
		} else {
			result.append(type.getName());
		}
		if(type != null) {
			result.append(' ');
		}
		result.append('(');
		if (allMetadata == null) {
			result.appendCodePoint(EM_DASH);
		} else {
			String out = null;
			Iterator<String> iter = IDENTIFIER_METADATA_FIELDS_FOR_TOSTRING.iterator();
			while (out == null && iter.hasNext()) {
				Iterator<Metadata> mdIter = getMetadataByType(iter.next()).iterator();
				while (mdIter.hasNext() && out == null) {
					out = mdIter.next().getValue();
				}
			}
			if (out != null && out.length() > MAX_CHARS) {
				result.append(out.substring(0, MAX_CHARS - 1));
				result.appendCodePoint(HORIZONTAL_ELLIPSIS);
			} else if (out != null) {
				result.append(out);
			} else {
				result.append("\u2026 ");
				result.append(allMetadata.size());
				result.append(" \u2026");
			}
		}
		result.append(')');
		if(children == null) {
			result.append("[\u2014]");
		} else {
			result.append(children.toString());
		}
		return result.toString();
	}

	/**
	 * Returns whether a downwards METS pointer must be written. This is the
	 * case if the parent docStruct is of the the anchor class of the file thas
	 * is currently written, but this docStruct isn’t.
	 * 
	 * @param fileClass
	 *            anchor class of the file to write
	 * @return whether a downwards METS pointer must be written
	 */
	public boolean mustWriteDownwardsMptrIn(String fileClass) {
		if (fileClass == null || parent == null) {
			return false;
		}
		return fileClass.equals(parent.getType().getAnchorClass())
				&& !fileClass.equals(type.getAnchorClass());
	}

	/**
	 * Returns whether an upwards METS pointer must be written. This is the case
	 * if the metadata of this docStruct is not kept in the file currently under construction, and either
	 * this docStruct has no parent and the anchor class of the file to create
	 * is different from the anchor class of this docStruct, or if the parent of
	 * this docStruct belongs to a different anchor class and the anchor class
	 * of the file to create appears after the anchor class of the parent of
	 * this docStruct in the list of anchor classes for the logical document
	 * structure.
	 * 
	 * @param fileClass
	 *            anchor class of the file to write
	 * @return whether an upwards METS pointer must be written
	 * @throws PreferencesException
	 *             if an anchor class name is encountered a second time after
	 *             having been descending right into a hierarchy to be
	 *             maintained in another anchor class already
	 */
	public boolean mustWriteUpwardsMptrIn(String fileClass) throws PreferencesException {
		String anchorClass = type.getAnchorClass();
		if (fileClass == null && anchorClass == null || fileClass != null && fileClass.equals(anchorClass)) {
			return false;
		}
		if (this.parent == null) {
			return anchorClass == null ? false : !anchorClass.equals(fileClass);
		}
		String parentClass = parent.getType().getAnchorClass();
		if (parentClass == null || parentClass.equals(anchorClass)) {
			return false;
		}
		Collection<String> anchorChain = getTopStruct().getAllAnchorClasses();
		anchorChain.add(null);
		Iterator<String> capstan = anchorChain.iterator();
		String link;
		do {
			link = capstan.next();
			if (link.equals(fileClass)) {
				return false;
			}
		} while (!link.equals(parentClass));
		return true;
	}

	/**
	 * Returns the topmost DocStruct
	 * 
	 * @return the topmost DocStruct
	 */
	public DocStruct getTopStruct() {
		return parent == null ? this : parent.getTopStruct();
	}
	
	/**
	 * Returns a readable name for a DocStruct.
	 * 
	 * @param obj
	 *            DocStruct whose name is to return
	 * @return a readable name for the DocStruct
	 */
	private static String identify(DocStruct obj) {
		DocStructType objectType = obj.getType();
		if (objectType != null && objectType.getName() != null) {
			return "'" + objectType.getName() + "'";
		}
		DocStruct parent = obj.getParent();
		if (parent == null) {
			return "top level";
		}
		List<DocStruct> parentsChildren = parent.getAllChildren();
		if (parentsChildren == null || parentsChildren.isEmpty()) {
			return "orphan";
		}
		int position = parentsChildren.indexOf(obj);
		if (position < 0) {
			return "orphan";
		}
		String childOfParent = " child of " + identify(parent);
		if (position == 0) {
			return "first" + childOfParent;
		}
		int childNo = position + 1;
		if (childNo == parentsChildren.size()) {
			return "last" + childOfParent;
		}
		String childIndex = Integer.toString(childNo);
		switch (Integer.valueOf(childIndex.substring(childIndex.length() - 1))) {
		case 1:
			return childIndex + "st" + childOfParent;
		case 2:
			return childIndex + "nd" + childOfParent;
		case 3:
			return childIndex + "rd" + childOfParent;
		default:
			return childIndex + "th" + childOfParent;
		}
	}
}
