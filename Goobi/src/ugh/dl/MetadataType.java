package ugh.dl;

/*******************************************************************************
 * ugh.dl / MetadataType.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*******************************************************************************
 * <p>
 * When using, storing, writing or reading metadata, groups or classes of
 * special metadata objects can be formed, which have something in common. They
 * are all of the same kind. Metadata of the same kind can be stored using the
 * same MetadataType object. Each MetadataType object can be identified easily
 * by using its internal name.
 * </p>
 * 
 * <p>
 * Besides the internal name, a MetadataType object contains information, about
 * it occurrences; some metadata may occur just once, other may occur many times.
 * </p>
 * <p>
 * E.g. for all titles of a document there can be a separate MetadataType
 * element, which contains information about this class of metadata elements.
 * Information which they share are information about their occurrences; each
 * structure entity can only have a single title.<BR>
 * MetadataType objects can occur in two different ways:
 * </p>
 * 
 * <ul>
 * <li>globally
 * <li>locally
 * </ul>
 * 
 * <p>
 * <b>Global</b> <code>MetadataType</code> objects can be retrieved from the
 * <code>Prefs</code> object by giving the internal name. Some of the
 * information of a MetadataType object depends on the context in which it is
 * used. Context means it depends on the <code>DocStructType</code> object, in
 * which a MetadataType object is used. When adding a <code>MetadataType</code>
 * object to a <DocStructType> object, an internal copy is created and stored
 * with the <code>DocStructType</code> object. This copy is called locally and
 * may store information about its occurrences in this special
 * <code>DocStructType</code> object. The <code>DocStructType</code> class
 * contains methods to retrieve local <code>MetadataType</code> objects from
 * global ones.
 * </p>
 * <p>
 * <code>MetadataType</code> objects are used, to create new
 * <code>Metadata</code> objects. They are the only parameter in the constructor
 * of the <code>Metadata</code> object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-13
 * @see Metadata#setType
 * @see DocStructType#getMetadataTypeByType
 * 
 *      CHANGELOG
 * 
 *      13.02.2010 -- Funk --- Refactored some whiles and iterators.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      06.05.2009 --- Wulf Riebensahm --- equals() method overloaded.
 * 
 ******************************************************************************/

public class MetadataType implements Serializable {

	private static final long		serialVersionUID	= 1285824825128157626L;

	// Unique name of MetadataType.
	private String					name;

	// Maximum number of occurrences of this MetadataType for one DocStrct (can
	// be 1 (1), one or more (+) or as many as you want (*).
	private String					max_number;

	// Hash containing all languages.
	private HashMap<String, String>	allLanguages;

	// Is set to true, if metadata is a person.
	protected boolean				isPerson			= false;

	// Is set to true, if this MetadataType acts as an element; which means,
	// that a metadata with the same value cannot be available twice.
	protected boolean				isIdentifier		= false;
	
	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public MetadataType() {
		super();
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
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setNum(String in) {

		if (!in.equals("1m") && !in.equals("1o") && !in.equals("+")
				&& !in.equals("*")) {
			// Unknown syntax.
			return false;
		}
		this.max_number = in;

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Creates an exact copy of this MetadataType instance. This method is used,
	 * when adding a MetadataType to a <code>DocStructType</code>.
	 * </p>
	 * <p>
	 * The copy contains the same languages, the same internal name and the
	 * number of possible occurrences as the original.
	 * </p>
	 * 
	 * @return the newly created MetadataType
	 * @see DocStructType
	 **************************************************************************/
	public MetadataType copy() {

		MetadataType newMDType = new MetadataType();

		newMDType.setAllLanguages(this.allLanguages);
		newMDType.setName(this.name);
		if (this.max_number != null) {
			newMDType.setNum(this.max_number);
		}
		newMDType.setIdentifier(this.isIdentifier());
		newMDType.setIsPerson(this.isPerson);
		return newMDType;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getName() {
		return this.name;
	}

	/***************************************************************************
	 * <p>
	 * If the MetadataType is an identifier, a Metadata instance of this type
	 * and a specific value can only be available once. Usually the identifier
	 * should be unique at least within the document. The identifier can be used
	 * to reference from other documents / parts of documents to a DocStruct
	 * instance.
	 * </p>
	 * 
	 * @return the isIdentifier
	 **************************************************************************/
	public boolean isIdentifier() {
		return this.isIdentifier;
	}

	/***************************************************************************
	 * @param isIdentifier
	 *            the isIdentifier to set
	 **************************************************************************/
	public void setIdentifier(boolean isIdentifier) {
		this.isIdentifier = isIdentifier;
	}

	/***************************************************************************
	 * <p>
	 * Retrieves the number of possible Metadata objects for a DocStruct. This
	 * is now based on the type of DocStruct and is therefor stored in the
	 * DocStructType.
	 * </p>
	 * 
	 * TODO Was set to deprecated, who knows why?
	 * 
	 * @return number of MetadataType
	 **************************************************************************/
	public String getNum() {
		return this.max_number;
	}

	/***************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setAllLanguages(HashMap<String, String> in) {
		this.allLanguages = in;
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public HashMap<String, String> getAllLanguages() {
		return this.allLanguages;
	}

	/***************************************************************************
	 * <p>
	 * Adds a name (in the given language) for this instance of MetadataType.
	 * </p>
	 * 
	 * @param lang
	 *            language code
	 * @param value
	 *            name of the metadata type in the given language
	 * @return true, if successful
	 **************************************************************************/
	public boolean addLanguage(String theLanguage, String theValue) {

		// Check, if language already is available, if not, put it in.
		for (Map.Entry<String, String> lang : this.allLanguages.entrySet()) {
			if (lang.getKey().equals(theLanguage)) {
				return false;
			}
		}

		this.allLanguages.put(theLanguage, theValue);

		return true;
	}

	/***************************************************************************
	 * <p>
	 * Retrieves the name for a certain language.
	 * </p>
	 * 
	 * @param lang
	 *            language code
	 * @return the translation of this MetadataType; or null, if it has no
	 *         translation for this language.
	 **************************************************************************/
	public String getNameByLanguage(String lang) {

		if (this.allLanguages.get(lang) == null) {
			return null;
		}

		return this.allLanguages.get(lang);
	}

	/***************************************************************************
	 * <p>
	 * Changes the name of this instance for a certain language.
	 * </p>
	 * 
	 * @param lang
	 *            language code
	 * @param content
	 *            new name
	 **************************************************************************/
	public void changeLanguageByName(String lang, String content) {
		removeLanguage(lang);
		addLanguage(lang, content);
	}

	/***************************************************************************
	 * <p>
	 * Removes a language for this MetadataType instance.
	 * </p>
	 * 
	 * @param lang
	 *            language code
	 * @return true, if successful
	 **************************************************************************/
	public boolean removeLanguage(String theLanguage) {

		// Check, if language already is available, if so, remove it.
		for (Map.Entry<String, String> lang : this.allLanguages.entrySet()) {
			if (lang.getKey().equals(theLanguage)) {
				this.allLanguages.remove(lang);
				return true;
			}
		}

		// Language unavailable, could not be removed.
		return false;
	}

	/***************************************************************************
	 * @param language
	 * @return
	 **************************************************************************/
	public String getLanguage(String theLanguage) {

		// Find language "inLanguage".
		for (Map.Entry<String, String> lang : getAllLanguages().entrySet()) {
			if (lang.getKey().equals(theLanguage)) {
				return lang.getValue();
			}
		}

		return null;
	}

	/***************************************************************************
	 * @param value
	 **************************************************************************/
	public void setIsPerson(boolean value) {
		this.isPerson = value;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public boolean getIsPerson() {
		return this.isPerson;
	}

	/***************************************************************************
	 * <p>
	 * Compares this MetadataType with parameter metadataType.
	 * </p>
	 * 
	 * @author Wulf Riebensahm
	 * @param MetadataType
	 *            metadataType
	 * @return TRUE if isPerson, isIdentifier and name is the same.
	 **************************************************************************/
	public boolean equals(MetadataType metadataType) {

		try {
			if (!((this.getName() == null && metadataType.getName() == null) || this
					.getName().equals(metadataType.getName()))) {
				return false;
			}
		}
		// TODO Teldemokles says: "Do never catch a NullPointerException"!
		catch (NullPointerException npe) {
			return false;
		}

		if (this.isIdentifier == metadataType.isIdentifier
				&& metadataType.isPerson == this.isPerson) {
			return true;
		}

		return false;
	}


}
