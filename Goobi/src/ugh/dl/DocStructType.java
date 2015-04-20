package ugh.dl;

/*******************************************************************************
 * ugh.dl / DocStructType.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*******************************************************************************
 * <p>
 * A <code>DocStructType</code> object defines a kind of class to which a structure entitiy (represented by a DocStruct object) belongs. All DocStruct
 * objects belonging to a similar class have something in common (e.g. possible children, special kind of metadata which can be available for a class,
 * a naming etc...). These things are stored in a DocStructType object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @version 2014-06-18
 * @see DocStruct#setType
 * 
 *      CHANGELOG
 *      
 *      18.06.2014 --- Ronge --- Change anchor to be string value & create more files when necessary
 * 
 *      13.02.2010 --- Funk --- Refcatored some overloaded methods, and set some methods deprecated.
 * 
 *      22.01.2010 --- Funk --- Improvements due to findbugs.
 * 
 *      21.12.2009 --- Funk --- Added method toString().
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      20.10.2009 --- Funk --- Added some modifiers for class attributes.
 * 
 *      11.09.2009 --- Wulf Riebensahm --- equals() method overloaded.
 * 
 *      24.10.2008 --- Funk --- Commented out the field myPrefs and its getter and setter methods. We do not need that!
 * 
 ******************************************************************************/

public class DocStructType implements Serializable {

    private static final long serialVersionUID = -3819246407494198735L;

    private String name;

	/**
	 * The field anchorClass may name an anchor (abstract super structure) class
	 * that logical structure instances of this type belong to. There may be
	 * multiple classes and several DocStructTypes may belong to one class. In
	 * this case, they must share the same anchorClass value. If the anchorClass
	 * is null, this DocStructType is a common element, not an anchor.
	 */
	private String anchorClass = null;
    // Preferences, which created this instance.
    private boolean hasfileset = true;
    private boolean topmost = false;

    // Map containing name of this DocStrctType for the appropriate languages.
    protected HashMap<String, String> allLanguages;

    // List containing all Metadatatypes (MetadataTypeForDocStructType
    // instances). PLEASE NOTE: Some tricky inheriting od something else things
    // do not let parameterise this attribute! Don't worry! Nevertheless, it
    // works!
    protected List allMetadataTypes;

    // LinkedList containing all possible document structure types which might
    // be children of this one here.
    protected List<String> allChildrenTypes;

    private final List allMetadataGroups;

    /***************************************************************************
     * <p>
     * List does not containg DocStructType objects but just the name (so just Strings).
     * </p>
     **************************************************************************/
    public DocStructType() {
        this.allChildrenTypes = new LinkedList<String>();
        this.allMetadataTypes = new LinkedList<String>();
        this.allMetadataGroups = new LinkedList<String>();
        this.allLanguages = new HashMap<String, String>();
    }

    /***************************************************************************
     * @param in
     * @return
     **************************************************************************/
    public boolean setName(String in) {
        this.name = in;
        return true;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public String getName() {
        return this.name;
    }

    /***************************************************************************
     * @deprecated replaced by isTopmost
     * @return true, if this DocStructType is the topmost element
     **************************************************************************/
    @Deprecated
    public boolean isTopMost() {
        return this.topmost;
    }

    /***************************************************************************
     * @deprecated replaced by setTopmost
     **************************************************************************/
    @Deprecated
    public void setTopMost(boolean in) {
        this.topmost = in;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public boolean hasFileSet() {
        return this.hasfileset;
    }

    /***************************************************************************
     * @param in
     **************************************************************************/
    public void setHasFileSet(boolean in) {
        this.hasfileset = in;
    }

    /***************************************************************************
     * @return the hasfileset
     **************************************************************************/
    public boolean isHasfileset() {
        return this.hasfileset;
    }

    /***************************************************************************
     * @param hasfileset the hasfileset to set
     **************************************************************************/
    public void setHasfileset(boolean hasfileset) {
        this.hasfileset = hasfileset;
    }

    /***************************************************************************
     * @return the topmost
     **************************************************************************/
    public boolean isTopmost() {
        return this.topmost;
    }

    /***************************************************************************
     * @param topmost the topmost to set
     **************************************************************************/
    public void setTopmost(boolean topmost) {
        this.topmost = topmost;
    }

    /***************************************************************************
     * <p>
     * Sets information, wether this type is an anchor (virtual structure entity) or not.
     * </p>
     * 
     * @param inBool
     **************************************************************************/
	public void setAnchorClass(String anchorClass) {
		this.anchorClass = anchorClass;
    }

    /***************************************************************************
	 * Retrieves the name of the anchor structure, if any, or null otherwise.
	 * Anchors ar a special type of document structure, which group other
	 * structure entities together, but have no own content. E.h. a periodical
	 * as such can be an anchor. The periodical itself is a virtual structure
	 * entity without any own content, but groups all years of appearance
	 * together. Years may be anchors again for volumes, etc.
	 * 
	 * @return String, which is null, if it cannot be used as an anchor
	 **************************************************************************/
	public String getAnchorClass() {
		return anchorClass;
    }

    /***************************************************************************
     * <p>
     * Set a HashMap, which contain translations of the name of a DocStructType into several languages (e.g. for display in an user-interface). The
     * key in the HashMap is the language code (iso-two-letter code) The value in the HashMap is the translation This methods replaces all other
     * language information for this DocStructType object.
     * </p>
     * 
     * @param in HashMap containing language code and value
     * @return always true
     **************************************************************************/
    public boolean setAllLanguages(HashMap<String, String> in) {

        this.allLanguages = in;

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all languages as a HashMap.
     * </p>
     * 
     * @return HashMap with key/value pairs; key= language code; value= translation in this language
     **************************************************************************/
    public HashMap<String, String> getAllLanguages() {
        return this.allLanguages;
    }

    /***************************************************************************
     * <p>
     * Adds a translation (into a given language).
     * </p>
     * 
     * @param lang two-letter code of the language
     * @param value translation of this StructType
     * @return true; if translation is already available false is returned
     **************************************************************************/
    public boolean addLanguage(String lang, String value) {

        Map.Entry<String, String> test;
        String key;

        // Check, if language already available.
        Iterator<Map.Entry<String, String>> it = this.allLanguages.entrySet().iterator();
        while (it.hasNext()) {
            test = it.next();
            key = test.getKey();
            if (key.equals(lang)) {
                // Language is already available.
                return false;
            }
        }

        this.allLanguages.put(lang, value);

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves the name for a certain language.
     * </p>
     * 
     * @param lang language code
     * @return name of this DocStructType in the specified language; or null if no translation is available
     **************************************************************************/
    public String getNameByLanguage(String lang) {

        String languageName = this.allLanguages.get(lang);
        if (languageName == null) {
            return null;
        }

        return languageName;
    }

    /***************************************************************************
     * <p>
     * Changes the name of this instance for a certain language.
     * </p>
     * 
     * @param lang language code
     * @param content new name
     **************************************************************************/
    public void changeLanguageByName(String lang, String content) {
        removeLanguage(lang);
        addLanguage(lang, content);
    }

    /***************************************************************************
     * <p>
     * Removes a translation.
     * </p>
     * 
     * @param lang two-letter code of language, which should be removed
     * @return true, if successful; otherwise false
     **************************************************************************/
    public boolean removeLanguage(String lang) {

        Map.Entry<String, String> test;
        String key;

        // Check, if language already available.
        Iterator<Map.Entry<String, String>> it = this.allLanguages.entrySet().iterator();
        while (it.hasNext()) {
            test = it.next();
            key = test.getKey();
            if (key.equals(lang)) {
                this.allLanguages.remove(lang);
                // Language is available, so remove it.
                return true;
            }
        }

        // Language unavailable, could not be removed.
        return false;
    }

    /***************************************************************************
     * <p>
     * Add and remove metadatatypes.
     * </p>
     * 
     * @param in
     * @return
     **************************************************************************/
    public boolean setAllMetadataTypes(List<MetadataType> in) {

        for (MetadataType mdt : in) {
            MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(mdt);
            this.allMetadataTypes.add(mdtfdst);
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataType objects for this DocStructType instance.
     * </p>
     * 
     * @return List containing MetadataType objects; These MetadataType-objects are just local objects
     **************************************************************************/
    public List<MetadataType> getAllMetadataTypes() {

        List<MetadataType> out = new LinkedList<MetadataType>();

        Iterator<MetadataTypeForDocStructType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            out.add(mdtfdst.getMetadataType());
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataType objects for this DocStructType instance, that have the "DefaultDisplay" attribute in the configuration set to
     * "true".
     * </p>
     * 
     * @return List containing MetadataType objects; These MetadataType objects are just local objects.
     **************************************************************************/
    public List<MetadataType> getAllDefaultDisplayMetadataTypes() {

        List<MetadataType> out = new LinkedList<MetadataType>();

        Iterator<MetadataTypeForDocStructType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataType());
            }
        }

        return out;
    }

    /**************************************************************************
     * <p>
     * Deprecated method, please use getAllDefaultDisplayMetadataTypes() in the future.
     * </p>
     * 
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public List<MetadataType> getAllDefaultMetadataTypes() {
        return getAllDefaultDisplayMetadataTypes();
    }

    /***************************************************************************
     * <p>
     * Gets the number of metadata objects, which are possible for a special MetadataType for this special document structure type. MetadataTypes are
     * compared using the internal name.
     * </p>
     * 
     * @param inType MetadataType - can be a global type
     * @return String containing the number (number can be: "1o", "1m", "*", "+")
     **************************************************************************/
    public String getNumberOfMetadataType(MetadataType inType) {

        Iterator<MetadataTypeForDocStructType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataType().getName().equals(inType.getName())) {
                return mdtfdst.getNumber();
            }
        }

        return "0";
    }

    /***************************************************************************
     * <p>
     * Gives very general information if a given MDType is allowed in a documentstructure of the type represented by this instance, or not.
     * </p>
     * 
     * @param inMDType MetadataType - can be a global type (with same internal name)
     * @return true, if it is allowed; otherwise false
     **************************************************************************/
    public boolean isMDTypeAllowed(MetadataType inMDType) {

        Iterator<MetadataType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            MetadataType mdt = it.next();
            if (mdt.getName().equals(inMDType.getName())) {
                return true; // it is already available
            }
        }

        return false; // sorry, not available
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataType object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataType objects, the MetadataType instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public MetadataType addMetadataType(MetadataType type, String inNumber) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataType myType;

        // Metadata is already available.
        if (isMetadataTypeAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        this.allMetadataTypes.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataType object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataType objects, the MetadataType instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @param isDefault if set to true, this metadatatype will be displayed (even if it's empty)
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public MetadataType addMetadataType(MetadataType type, String inNumber, boolean isDefault, boolean isInvisible) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataType myType;

        // Metadata is already available.
        if (isMetadataTypeAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataTypeForDocStructType mdtfdst = new MetadataTypeForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        mdtfdst.setDefaultdisplay(isDefault);
        mdtfdst.setInvisible(isInvisible);
        this.allMetadataTypes.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Checks, if the MetadataType has already been added and is already available in the list of all MetadataTypes.
     * </p>
     * 
     * @param type
     * @return true, if is is already available
     **************************************************************************/
    private boolean isMetadataTypeAlreadyAvailable(MetadataType type) {

        MetadataTypeForDocStructType test;
        String testname;
        String typename;

        // Check, if MetadataType is already available.
        Iterator<MetadataTypeForDocStructType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            test = it.next();
            MetadataType mdt = test.getMetadataType();
            testname = mdt.getName();
            typename = type.getName();

            if (testname.equals(typename)) {
                // It is already available.
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * <p>
     * Removes a MetadataType object.
     * </p>
     * 
     * @param type MetadataType
     * @return true if successful, otherwise false
     **************************************************************************/
    public boolean removeMetadataType(MetadataType type) {

        List<MetadataTypeForDocStructType> ll = new LinkedList(this.allMetadataTypes);

        Iterator<MetadataTypeForDocStructType> it = ll.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataType().equals(type)) {
                this.allMetadataTypes.remove(mdtfdst);
                return true;
            }
        }

        return false;
    }

    /***************************************************************************
     * @param typename
     * @return
     **************************************************************************/
    public boolean removeMetadataType(String typename) {
        return false;
    }

    /***************************************************************************
     * <p>
     * Retrieves the local MetadataType object (created when adding a global MetadataType object). This is necessary, if you just like to have the
     * global MetadataType (from Preferences).
     * </p>
     * 
     * @param inMDType global MetadataType object (from Preferences)
     * @return MetadataType or null, if not available for this DocStructType
     **************************************************************************/
    public MetadataType getMetadataTypeByType(MetadataType inMDType) {

        // Check, if MetadataType is already available.
        Iterator<MetadataTypeForDocStructType> it = this.allMetadataTypes.iterator();
        while (it.hasNext()) {
            MetadataTypeForDocStructType mdtfdst = it.next();
            MetadataType mdt = mdtfdst.getMetadataType();

            if (mdt.getName().equals(inMDType.getName())) {
                return mdt;
            }
        }

        return null;
    }

    /***************************************************************************
     * @param inString
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean addDocStructtypeasChild(String inString) {
        return addDocStructTypeAsChild(inString);
    }

    /***************************************************************************
     * <p>
     * Add another DocStructType, which might be a children only the name (as String) is stored in the list; not the DocStructType object itself.
     * </p>
     * 
     * @param inString
     * @return
     **************************************************************************/
    public boolean addDocStructTypeAsChild(String inString) {

        // Check if the DocStruct is not existing yet, and add it then.
        if (this.allChildrenTypes.isEmpty() || !this.allChildrenTypes.contains(inString)) {
            return this.allChildrenTypes.add(inString);
        }

        return false;
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean addDocStructTypeAsChild(DocStructType inType) {
        return addDocStructTypeAsChild(inType.getName());
    }

    /***************************************************************************
     * @param inType
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean addDocStructtypeasChild(DocStructType inType) {
        return addDocStructTypeAsChild(inType);
    }

    /***************************************************************************
     * @param inString name of the DocStructType
     * @deprecated
     * @return true, if it was removed, otherwise false
     **************************************************************************/
    @Deprecated
    public boolean removeDocStructtypeasChild(String inString) {
        return removeDocStructTypeAsChild(inString);
    }

    /***************************************************************************
     * <p>
     * Removes the given type from the list of allowed children for the appropriate DocStruct.
     * </p>
     * 
     * @param inString name of the DocStructType
     * @return true, if it was removed, otherwise false
     **************************************************************************/
    public boolean removeDocStructTypeAsChild(String inString) {

        if (this.allChildrenTypes.remove(inString)) {
            return true;
        }

        return false;
    }

    /***************************************************************************
     * @param inType
     * @deprecated
     * @return
     **************************************************************************/
    @Deprecated
    public boolean removeDocStructtypeasChild(DocStructType inType) {
        return removeDocStructTypeAsChild(inType);
    }

    /***************************************************************************
     * @param inType
     * @return
     **************************************************************************/
    public boolean removeDocStructTypeAsChild(DocStructType inType) {
        return removeDocStructTypeAsChild(inType.getName());
    }

    /***************************************************************************
     * <p>
     * Returns a List containing the names of all DocStructTypes which are allowed as children.
     * </p>
     * 
     * @return
     **************************************************************************/
    public List<String> getAllAllowedDocStructTypes() {
        return this.allChildrenTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return getName();
    }

    /***************************************************************************
     * <p>
     * This equals method only checks the attribute "name", because equality of the rules are not really necessary for the digital Document to be
     * equal.
     * </p>
     * 
     * @author Wulf Riebensahm
     * @param DocStructType docStructType
     ***************************************************************************/
    public boolean equals(DocStructType docStructType) {
        return this.getName().equals(docStructType.getName());
    }

    
    
    /***************************************************************************
     * <p>
     * Add and remove MetadataGroups.
     * </p>
     * 
     * @param in
     * @return
     **************************************************************************/
    public boolean setAllMetadataGroups(List<MetadataGroupType> in) {

        for (MetadataGroupType mdt : in) {
            MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(mdt);
            this.allMetadataGroups.add(mdtfdst);
        }

        return true;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataGroup objects for this DocStructType instance.
     * </p>
     * 
     * @return List containing MetadataGroup objects; These MetadataGroup-objects are just local objects
     **************************************************************************/
    public List<MetadataGroupType> getAllMetadataGroupTypes() {

        List<MetadataGroupType> out = new LinkedList<MetadataGroupType>();

        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            MetadataGroupForDocStructType mdtfdst = it.next();
            out.add(mdtfdst.getMetadataGroup());
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Retrieves all MetadataGroup objects for this DocStructType instance, that have the "DefaultDisplay" attribute in the configuration set to
     * "true".
     * </p>
     * 
     * @return List containing MetadataGroup objects; These MetadataGroup objects are just local objects.
     **************************************************************************/
    public List<MetadataGroupType> getAllDefaultDisplayMetadataGroups() {

        List<MetadataGroupType> out = new LinkedList<MetadataGroupType>();

        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            MetadataGroupForDocStructType mdtfdst = it.next();
            if (mdtfdst.isDefaultdisplay()) {
                out.add(mdtfdst.getMetadataGroup());
            }
        }

        return out;
    }

    /***************************************************************************
     * <p>
     * Gets the number of metadata objects, which are possible for a special MetadataGroup for this special document structure type. MetadataGroup are
     * compared using the internal name.
     * </p>
     * 
     * @param inType MetadataGroup - can be a global type
     * @return String containing the number (number can be: "1o", "1m", "*", "+")
     **************************************************************************/
    public String getNumberOfMetadataGroups(MetadataGroupType inType) {

        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            MetadataGroupForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataGroup().getName().equals(inType.getName())) {
                return mdtfdst.getNumber();
            }
        }

        return "0";
    }

    /***************************************************************************
     * <p>
     * Gives very general information if a given MDType is allowed in a documentstructure of the type represented by this instance, or not.
     * </p>
     * 
     * @param inMDType MetadataType - can be a global type (with same internal name)
     * @return true, if it is allowed; otherwise false
     **************************************************************************/
    public boolean isMDTGroupAllowed(MetadataGroupType inMDType) {

        Iterator<MetadataGroupType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            MetadataGroupType mdt = it.next();
            if (mdt.getName().equals(inMDType.getName())) {
                return true; // it is already available
            }
        }

        return false; // sorry, not available
    }

    /***************************************************************************
     * <p>
     * Removes a MetadataGroup object.
     * </p>
     * 
     * @param type MetadataGroup
     * @return true if successful, otherwise false
     **************************************************************************/
    public boolean removeMetadataGroup(MetadataGroupType type) {

        List<MetadataGroupForDocStructType> ll = new LinkedList(this.allMetadataGroups);

        Iterator<MetadataGroupForDocStructType> it = ll.iterator();
        while (it.hasNext()) {
            MetadataGroupForDocStructType mdtfdst = it.next();
            if (mdtfdst.getMetadataGroup().equals(type)) {
                this.allMetadataGroups.remove(mdtfdst);
                return true;
            }
        }

        return false;
    }

   

    /***************************************************************************
     * <p>
     * Retrieves the local MetadataGroup object (created when adding a global MetadataGroup object). This is necessary, if you just like to have the
     * global MetadataGroup (from Preferences).
     * </p>
     * 
     * @param inMDType global MetadataGroup object (from Preferences)
     * @return MetadataGroup or null, if not available for this DocStructType
     **************************************************************************/
    public MetadataGroupType getMetadataGroupByGroup(MetadataGroupType inMDType) {

        // Check, if MetadataType is already available.
        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            MetadataGroupForDocStructType mdtfdst = it.next();
            MetadataGroupType mdt = mdtfdst.getMetadataGroup();

            if (mdt.getName().equals(inMDType.getName())) {
                return mdt;
            }
        }

        return null;
    }

    
    /***************************************************************************
     * <p>
     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataType object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @return newly created copy of the MetadataGroup object; if not successful null is returned
     **************************************************************************/
    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataGroupType myType;

        // Metadata is already available.
        if (isMetadataGroupAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        this.allMetadataGroups.add(mdtfdst);

        return myType;
    }

    /***************************************************************************
     * <p>
     * Adds a MetadataGroup object to this DocStructType instance; this means, that all document structures of this type can have at least one metadata
     * object of this type because for each DocStructType object we have separate MetadataGroup objects, the MetadataGroup instance given as the only
     * parameter is duplicated; the copy of the given instance is added. If successful, the copy is returned - otherwise null is returned.
     * </p>
     * 
     * @param type MetadataGroup object which should be added
     * @param inNumber number, how often Metadata of type can be added to a DocStruct object of this kind
     * @param isDefault if set to true, this metadatatype will be displayed (even if it's empty)
     * @return newly created copy of the MetadataType object; if not successful null is returned
     **************************************************************************/
    public MetadataGroupType addMetadataGroup(MetadataGroupType type, String inNumber, boolean isDefault, boolean isInvisible) {

        // New MetadataType obejct which is added to this DocStructType.
        MetadataGroupType myType;

        // Metadata is already available.
        if (isMetadataGroupAlreadyAvailable(type)) {
            return null;
        }

        // Make a copy of this object and add the copy - necessary, cause we
        // have own instances for each document structure type.
        myType = type.copy();

        MetadataGroupForDocStructType mdtfdst = new MetadataGroupForDocStructType(myType);
        mdtfdst.setNumber(inNumber);
        mdtfdst.setDefaultdisplay(isDefault);
        mdtfdst.setInvisible(isInvisible);
        this.allMetadataGroups.add(mdtfdst);

        return myType;
    }
    
    /***************************************************************************
     * <p>
     * Checks, if the MetadataGroup has already been added and is already available in the list of all MetadataGroup.
     * </p>
     * 
     * @param type
     * @return true, if is is already available
     **************************************************************************/
    private boolean isMetadataGroupAlreadyAvailable(MetadataGroupType type) {

        MetadataGroupForDocStructType test;
        String testname;
        String typename;

        // Check, if MetadataType is already available.
        Iterator<MetadataGroupForDocStructType> it = this.allMetadataGroups.iterator();
        while (it.hasNext()) {
            test = it.next();
            MetadataGroupType mdt = test.getMetadataGroup();
            testname = mdt.getName();
            typename = type.getName();

            if (testname.equals(typename)) {
                // It is already available.
                return true;
            }
        }

        return false;
    }
    
    
    /***************************************************************************
     * <p>
     * Just a small class to store the MetadataType together with number (which depends on the DocStructType).
     * </p>
     **************************************************************************/

    public class MetadataTypeForDocStructType implements Serializable {

        private static final long serialVersionUID = -5908952924188415337L;

        private MetadataType mdt = null;
        // Number of metadatatypes for this docStruct.
        private String num = null;
        // Just a filter to display only default metadata types.
        private boolean defaultdisplay = false;
        // Just a filter to avoid displaying invisible fields.
        private boolean invisible = false;

        /***********************************************************************
         * @param inType
         **********************************************************************/
        public MetadataTypeForDocStructType(MetadataType inType) {
            this.mdt = inType;
        }

        /***********************************************************************
         * @param in
         **********************************************************************/
        public void setNumber(String in) {
            this.num = in;
        }

        /***********************************************************************
         * @return
         **********************************************************************/
        public String getNumber() {
            return this.num;
        }

        /***********************************************************************
         * @return
         **********************************************************************/
        public MetadataType getMetadataType() {
            return this.mdt;
        }

        /***********************************************************************
         * @return the defaultdisplay
         **********************************************************************/
        public boolean isDefaultdisplay() {
            return this.defaultdisplay;
        }

        /***********************************************************************
         * Sets the DefaultDisplay variable for this DocStructType. Dosn't make any sense at all!
         * 
         * @param inDefaultdisplay the defaultdisplay to set
         **********************************************************************/
        public void setDefaultdisplay(boolean inDefaultdisplay) {
            this.defaultdisplay = inDefaultdisplay;
        }

        /***********************************************************************
         * @return the invisible
         **********************************************************************/
        public boolean isInvisible() {
            return this.invisible;
        }

        /***********************************************************************
         * @param invisible the invisible to set
         **********************************************************************/
        public void setInvisible(boolean invisible) {
            this.invisible = invisible;
        }

    }

    
    public class MetadataGroupForDocStructType implements Serializable {

        private static final long serialVersionUID = -4571877810721395422L;

        private MetadataGroupType mdg = null;
        // Number of metadatatypes for this docStruct.
        private String num = null;
        // Just a filter to display only default metadata types.
        private boolean defaultdisplay = false;
        // Just a filter to avoid displaying invisible fields.
        private boolean invisible = false;

        /***********************************************************************
         * @param inType
         **********************************************************************/
        public MetadataGroupForDocStructType(MetadataGroupType group) {
            this.mdg = group;
        }

        /***********************************************************************
         * @param in
         **********************************************************************/
        public void setNumber(String in) {
            this.num = in;
        }

        /***********************************************************************
         * @return
         **********************************************************************/
        public String getNumber() {
            return this.num;
        }

        /***********************************************************************
         * @return
         **********************************************************************/
        public MetadataGroupType getMetadataGroup() {
            return this.mdg;
        }

        /***********************************************************************
         * @return the defaultdisplay
         **********************************************************************/
        public boolean isDefaultdisplay() {
            return this.defaultdisplay;
        }

        /***********************************************************************
         * Sets the DefaultDisplay variable for this DocStructType. Dosn't make any sense at all!
         * 
         * @param inDefaultdisplay the defaultdisplay to set
         **********************************************************************/
        public void setDefaultdisplay(boolean inDefaultdisplay) {
            this.defaultdisplay = inDefaultdisplay;
        }

        /***********************************************************************
         * @return the invisible
         **********************************************************************/
        public boolean isInvisible() {
            return this.invisible;
        }

        /***********************************************************************
         * @param invisible the invisible to set
         **********************************************************************/
        public void setInvisible(boolean invisible) {
            this.invisible = invisible;
        }
        
    }
    
    
}
