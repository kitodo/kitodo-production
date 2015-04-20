package ugh.fileformats.mets;

import java.util.LinkedHashMap;
import java.util.Map;

/*******************************************************************************
 * ugh.fileformats.mets / MatchingMetadataObject.java
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

/*******************************************************************************
 * @author Stefan Funk
 * @author Robert Sehr
 * @version 2009-12-21
 * @since 2009-05-09
 * 
 *        TODOLOG
 * 
 *        CHANGELOG
 * 
 *        21.12.2009 --- Funk --- Added modsGrouping.
 * 
 *        20.11.2009 --- Funk --- Changed all method modifiers to protected.
 * 
 ******************************************************************************/

public class MatchingMetadataObject {

	// Internal name and some skipped boolean.
	private String	internalName			= null;
	private boolean	skipped					= false;

	// RegExp value replacing.
	private String	valueCondition			= null;
	private String	valueRegExp				= null;

	// Role of a person.
	private String	role					= null;

	// Variables used for writing and reading MODS.
	private String	readxQuery				= null;
	private String	writexQuery				= null;
	private String	writemodsName			= null;

	// Additional XQueries are only used for persons.
	private String	firstnameXQuery			= null;
	private String	lastnameXQuery			= null;
	private String	affiliationXQuery		= null;
	private String	identifierXQuery		= null;
	private String	identifierTypeXQuery	= null;
	private String	authorityIDXquery   	= null;
	private String  authorityURIXquery       = null;
	private String  authorityValueXquery    = null;
	private String	displayNameXQuery		= null;
	private String	persontypeXQuery		= null;

	// All these are official MODS attributes used for reading.
	private String	readmodsName			= null;
	private String	modstype				= null;
	private String	modsencoding			= null;
	private String	modsauthority			= null;
	private String	modsID					= null;
	private String	modstransliteration		= null;
//	private String	modsscript				= null;
	private String	modslang				= null;
	private String	modsxmllang				= null;

	// Used for grouping of MODS subtags.
	private String	modsGrouping			= null;
	
	private Map<String, Map<String, String>> metadataGroupXQueries = new LinkedHashMap<String, Map<String, String>>();
	
	private String database = null;
	private String identifier = null;

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getReadModsName() {
		return this.readmodsName;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getWriteModsName() {
		return this.writemodsName;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getInternalName() {
		return this.internalName;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSType() {
		return this.modstype;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSEncoding() {
		return this.modsencoding;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSAuthority() {
		return this.modsauthority;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSID() {
		return this.modsID;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSTransliteration() {
		return this.modstransliteration;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSScript() {
		return this.modstransliteration;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSLang() {
		return this.modslang;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getMODSXMLLang() {
		return this.modsxmllang;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected boolean isSkipped() {
		return this.skipped;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	protected String getRole() {
		return this.role;
	}

	/***************************************************************************
	 * @return Returns the isSortingtitle.
	 **************************************************************************/
	protected void setReadModsName(String in) {
		this.readmodsName = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setWriteModsName(String in) {
		this.writemodsName = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setInternalName(String in) {
		this.internalName = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSType(String in) {
		this.modstype = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSEncoding(String in) {
		this.modsencoding = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSAuthority(String in) {
		this.modsauthority = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSID(String in) {
		this.modsID = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSTransliteration(String in) {
		this.modstransliteration = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSScript(String in) {
		this.modstransliteration = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSLang(String in) {
		this.modslang = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setMODSXMLLang(String in) {
		this.modsxmllang = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void isSkipped(boolean in) {
		this.skipped = in;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	protected void setRole(String in) {
		this.role = in;
	}

	/***************************************************************************
	 * @return the firstnameXQuery
	 **************************************************************************/
	protected String getFirstnameXQuery() {
		return this.firstnameXQuery;
	}

	/***************************************************************************
	 * @param firstnameXQuery
	 *            the firstnameXQuery to set
	 **************************************************************************/
	protected void setFirstnameXQuery(String firstnameXQuery) {
		this.firstnameXQuery = firstnameXQuery;
	}

	/***************************************************************************
	 * @return the lastnameXQuery
	 **************************************************************************/
	protected String getLastnameXQuery() {
		return this.lastnameXQuery;
	}

	/***************************************************************************
	 * @param lastnameXQuery
	 *            the lastnameXQuery to set
	 **************************************************************************/
	protected void setLastnameXQuery(String lastnameXQuery) {
		this.lastnameXQuery = lastnameXQuery;
	}

	/***************************************************************************
	 * @return the affiliationXQuery
	 **************************************************************************/
	protected String getAffiliationXQuery() {
		return this.affiliationXQuery;
	}

	/***************************************************************************
	 * @param affiliationXQuery
	 *            the affiliationXQuery to set
	 **************************************************************************/
	protected void setAffiliationXQuery(String affiliationXQuery) {
		this.affiliationXQuery = affiliationXQuery;
	}

	/***************************************************************************
	 * @return the persontypeXQuery
	 **************************************************************************/
	protected String getPersontypeXQuery() {
		return this.persontypeXQuery;
	}

	/***************************************************************************
	 * @param persontypeXQuery
	 *            the persontypeXQuery to set
	 **************************************************************************/
	protected void setPersontypeXQuery(String persontypeXQuery) {
		this.persontypeXQuery = persontypeXQuery;
	}

	/***************************************************************************
	 * @return the xQuery
	 **************************************************************************/
	protected String getReadXQuery() {
		return this.readxQuery;
	}

	/***************************************************************************
	 * @param query
	 *            the xQuery to set
	 **************************************************************************/
	protected void setReadXQuery(String query) {
		this.readxQuery = query;
	}

	/***************************************************************************
	 * Returns the write XQuery only, if available; otherwise the read XQuery is
	 * returned
	 * 
	 * @return the writexQuery
	 **************************************************************************/
	protected String getWriteXPath() {
		if (this.writexQuery == null) {
			return this.readxQuery;
		}
		return this.writexQuery;
	}

	/***************************************************************************
	 * @param query
	 *            the xQuery to set
	 **************************************************************************/
	protected void setWriteXQuery(String query) {
		this.writexQuery = query;
	}

	/***************************************************************************
	 * @return the authorityFileIDXquery
	 **************************************************************************/
	protected String getAuthorityIDXquery() {
		return this.authorityIDXquery;
	}

	/***************************************************************************
	 * @param authorityFileIDXquery
	 *            the authorityFileIDXquery to set
	 **************************************************************************/
	protected void setAuthorityIDXquery(String authorityIDXquery) {
		this.authorityIDXquery = authorityIDXquery;
	}
	
	protected String getAuthorityURIXquery() {
        return this.authorityURIXquery;
    }
    
    protected void setAuthorityURIXquery(String authorityURIXquery) {
        this.authorityURIXquery = authorityURIXquery;
    }
	
	protected String getAuthorityValueXquery() {
        return this.authorityValueXquery;
    }
	
	protected void setAuthorityValueXquery(String authorityValueXquery) {
        this.authorityValueXquery = authorityValueXquery;
    }
	
	/***************************************************************************
	 * @return the displayNameXQuery
	 **************************************************************************/
	protected String getDisplayNameXQuery() {
		return this.displayNameXQuery;
	}

	/***************************************************************************
	 * @param displayNameXQuery
	 *            the displayNameXQuery to set
	 **************************************************************************/
	protected void setDisplayNameXQuery(String displayNameXQuery) {
		this.displayNameXQuery = displayNameXQuery;
	}

	/***************************************************************************
	 * @return the identifierTypeXQuery
	 **************************************************************************/
	protected String getIdentifierTypeXQuery() {
		return this.identifierTypeXQuery;
	}

	/***************************************************************************
	 * @param identifierTypeXQuery
	 *            the identifierTypeXQuery to set
	 **************************************************************************/
	protected void setIdentifierTypeXQuery(String identifierTypeXQuery) {
		this.identifierTypeXQuery = identifierTypeXQuery;
	}

	/***************************************************************************
	 * @return the identifierXQuery
	 **************************************************************************/
	protected String getIdentifierXQuery() {
		return this.identifierXQuery;
	}
	
	protected void setDatabaseXQuery(String databaseXQuery) {
		this.database = databaseXQuery;
	}
	
	protected String getDatabaseXQuery() {
		return database;
	}
	

	/***************************************************************************
	 * @param identifierXQuery
	 *            the identifierXQuery to set
	 **************************************************************************/
	protected void setIdentifierXQuery(String identifierXQuery) {
		this.identifierXQuery = identifierXQuery;
	}

	/***************************************************************************
	 * @return the valueCondition
	 **************************************************************************/
	protected String getValueCondition() {
		return this.valueCondition;
	}

	/***************************************************************************
	 * @param valueCondition
	 *            the valueCondition to set
	 **************************************************************************/
	protected void setValueCondition(String valueCondition) {
		this.valueCondition = valueCondition;
	}

	/***************************************************************************
	 * @return the valueRegExp
	 **************************************************************************/
	protected String getValueRegExp() {
		return this.valueRegExp;
	}

	/***************************************************************************
	 * @param valueCondition
	 *            the valueCondition to set
	 **************************************************************************/
	protected void setValueRegExp(String valueRegExp) {
		this.valueRegExp = valueRegExp;
	}

	/**************************************************************************
	 * @return
	 **************************************************************************/
	protected String getModsGrouping() {
		return this.modsGrouping;
	}

	/**************************************************************************
	 * @param modsGrouping
	 **************************************************************************/
	protected void setModsGrouping(String modsGrouping) {
		this.modsGrouping = modsGrouping;
	}
	

	

	public Map<String, Map<String, String>> getMetadataGroupXQueries() {
        return metadataGroupXQueries;
    }

    public void setMetadataGroupXQueries(Map<String, Map<String, String>> metadataGroupXQueries) {
        this.metadataGroupXQueries = metadataGroupXQueries;
    }

    public void addToMap(String key, Map<String, String> value) {
        metadataGroupXQueries.put(key, value);
    }
    
    /***************************************************************************
	 * @param checkObject
	 * @return
	 **************************************************************************/
	protected boolean equals(MatchingMetadataObject checkObject) {

		// Check ReadModsName.
		if ((checkObject.getReadModsName() != null)
				&& (this.getReadModsName() != null)) {
			if (!(this.getReadModsName().equals(checkObject.getReadModsName()))) {
				// The two values are NOT identical.
				return false;
			}
		} else {
			// One of the two objects does not have ReadModsName value.
			return false;
		}

		// Check Role.
		if ((checkObject.getRole() != null) && (this.getRole() != null)) {
			if (!(this.getRole().equals(checkObject.getRole()))) {
				// The two values are NOT identical.
				return false;
			}
		} else {
			// One of the two objects does not have ReadModsName value.
			return false;
		}

		return true;
	}

}
