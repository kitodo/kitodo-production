package ugh.dl;

/*******************************************************************************
 * ugh.dl / Person.java
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

import ugh.exceptions.MetadataTypeNotAllowedException;

/*******************************************************************************
 * <p>
 * A person is a very special kind of metadata. For this reason it is inheirited
 * from <code>Metadata</code> class.
 * </p>
 * 
 * <p>
 * A person has several metadata and not only a single value. The person's name
 * can be splitted into:
 * </p>
 * <ul>
 * <li>firstname
 * <li>lastname
 * <li>affilication (company they are working at...}
 * <li>a role, when creating or producing a book, article etc.; usually this is
 * a MetadataType.
 * </ul>
 * <p>
 * This class provides methods to store and retrieve these additional
 * information.
 * </p>
 * <p>
 * Most file formats are not able to serialize / store all information from a
 * Person object.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-14
 * @see Metadata
 * 
 *      CHANGELOG
 * 
 *      14.02.2010 --- Funk --- Added method toString().
 * 
 *      30.11.2009 --- Funk --- Again removed deprecated Person() constructor.
 * 
 *      17.11.2009 --- Funk --- Changed method person() to constructor Person().
 *      --- Refactored some things for Sonar improvement.
 * 
 *      10.11.2009 --- Funk --- Removed deprecated Person() constructor.
 * 
 *      06.10.2009 --- Funk --- Adapted metadata and person constructors. Left
 *      the old constructors where they were, to not confuse the intranda
 *      peoples :-)
 * 
 *      06.05.2009 --- Wulf Riebensahm --- Method equals() overloaded .
 * 
 ******************************************************************************/

public class Person extends Metadata {

	private static final long	serialVersionUID	= -3667880952431707982L;

	private String				firstname			= null;
	private String				lastname			= null;
	private String				displayname			= null;
	private String				affiliation			= null;
	private String				institution			= null;
//	private String				identifier			= null;
//	private String				identifierType		= null;
	private String				role				= null;
	private String				persontype			= null;
	private boolean				isCorporation		= false;

	/***************************************************************************
	 * <p>
	 * Constructor with a MetadataType is needed.
	 * </p>
	 * 
	 * @param theType
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	public Person(MetadataType theType) throws MetadataTypeNotAllowedException {
		super(theType);
	}

	/***************************************************************************
	 * <p>
	 * Creates a person; each person has usually a first and a lastname. For
	 * this reason, both must be given in this constructor. If one is not
	 * available; simply set one or both parameters to null.
	 * </p>
	 * 
	 * @param in1
	 *            firstname of the person
	 * @param in2
	 *            lastname of the person
	 * @throws MetadataTypeNotAllowedException
	 **************************************************************************/
	public Person(MetadataType theType, String in1, String in2)
			throws MetadataTypeNotAllowedException {

		super(theType);
		this.firstname = in1;
		this.lastname = in2;
	}

	/***************************************************************************
	 * <p>
	 * Store the firstname of this person.
	 * </p>
	 * 
	 * @param in
	 * @return true is always returned
	 **************************************************************************/
	public boolean setFirstname(String in) {
		this.firstname = in;
		return true;
	}

	/***************************************************************************
	 * <p>
	 * Retrieves the firstname of this person.
	 * </p>
	 * 
	 * @return firstname of this person
	 **************************************************************************/
	public String getFirstname() {
		return this.firstname;
	}

	/***************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setLastname(String in) {
		this.lastname = in;
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getLastname() {
		return this.lastname;
	}

	/***************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setInstitution(String in) {
		this.institution = in;
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getInstitution() {
		return this.institution;
	}

	/***************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setAffiliation(String in) {
		this.affiliation = in;
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getAffiliation() {
		return this.affiliation;
	}

//	/***************************************************************************
//	 * @param in
//	 * @return
//	 **************************************************************************/
//	public boolean setIdentifier(String in) {
//		this.identifier = in;
//		return true;
//	}
//
//	/***************************************************************************
//	 * @return
//	 **************************************************************************/
//	public String getIdentifier() {
//		return this.identifier;
//	}
//
//	/***************************************************************************
//	 * @param in
//	 * @return
//	 **************************************************************************/
//	public boolean setIdentifierType(String in) {
//		this.identifierType = in;
//		return true;
//	}
//
//	/***************************************************************************
//	 * @return
//	 **************************************************************************/
//	public String getIdentifierType() {
//		return this.identifierType;
//	}

	/***************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setRole(String in) {
		this.role = in;
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getRole() {
	    if (role == null) {
	        role = MDType.getName();
	    }
		return this.role;
	}

	/***************************************************************************
	 * <p>
	 * Sets the type of a person. The type of a person, may distinguish between
	 * normal/natural persons, juristic persons (as companies) or virtual
	 * persons (e.g. as conferences etc...).
	 * </p>
	 * 
	 * @param in
	 * @return always true;
	 **************************************************************************/
	public boolean setPersontype(String in) {
		this.persontype = in;
		return true;
	}

	/***************************************************************************
	 * <p>
	 * Gets the type of a person.
	 * </p>
	 * 
	 * @return type of person as String
	 **************************************************************************/
	public String getPersontype() {
		return this.persontype;
	}

	/***************************************************************************
	 * <p>
	 * Gets the displayname of a person. The displayname is the Name, which can
	 * be displayed and should contain the right order of lastname, firstname
	 * and affiliation of a person.
	 * </p>
	 * 
	 * @return the displayname
	 **************************************************************************/
	public String getDisplayname() {
		return this.displayname;
	}

	/***************************************************************************
	 * <p>
	 * Sets the displayname of a person.
	 * </p>
	 * 
	 * @param displayname
	 *            the displayname to set
	 **************************************************************************/
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	/***************************************************************************
	 * @return the isCorporation
	 **************************************************************************/
	public boolean isCorporation() {
		return this.isCorporation;
	}

	/***************************************************************************
	 * @param isCorporation
	 *            the isCorporation to set
	 **************************************************************************/
	public void setCorporation(boolean isCorporation) {
		this.isCorporation = isCorporation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		String result = "";

		if (this.getType() != null && this.getLastname() != null
				&& !this.getLastname().equals("")) {
			// Get person type and value.
			result += "Person ("
					+ this.getType().getName()
					+ "): "
					+ (this.getLastname() == null ? "NULL" : "\""
							+ this.getLastname() + "\"")
					+ ", "
					+ (this.getFirstname() == null ? "NULL" : "\""
							+ this.getFirstname() + "\"") + "\n";
		} else if (this.getType() == null) {
			result += "Person (WITHOUT TYPE!!): "
					+ (this.getLastname() == null ? "NULL" : "\""
							+ this.getLastname() + "\"")
					+ ", "
					+ (this.getFirstname() == null ? "NULL" : "\""
							+ this.getFirstname() + "\"") + "\n";
		}

		return result;
	}

	/***************************************************************************
	 * <p>
	 * Compares this Person with parameter person.
	 * </p>
	 * 
	 * @return TRUE if ...
	 * @param Person
	 *            person
	 **************************************************************************/
	public boolean equals(Person person) {

		// First check the underlying Metadata Object.
		if (!super.equals(person)) {
			return false;
		}

		// Ccompare all attributes of person: If attribute of this is null,
		// while respective attribute of person is not null a nullpointer
		// exception is thrown indicating that compared objects are different.
		try {
			if (!((this.getFirstname() == null && person.getFirstname() == null) || this
					.getFirstname().equals(person.getFirstname()))) {
				return false;
			}

			if (!((this.getLastname() == null && person.getLastname() == null) || this
					.getLastname().equals(person.getLastname()))) {
				return false;
			}

			if (!((this.getAffiliation() == null && person.getAffiliation() == null) || this
					.getAffiliation().equals(person.getAffiliation()))) {
				return false;
			}

			if (!((this.getDisplayname() == null && person.getDisplayname() == null) || this
					.getDisplayname().equals(person.getDisplayname()))) {
				return false;
			}

			if (!((this.getPersontype() == null && person.getPersontype() == null) || this
					.getPersontype().equals(person.getPersontype()))) {
				return false;
			}

			if (!((this.getInstitution() == null && person.getInstitution() == null) || this
					.getInstitution().equals(person.getInstitution()))) {
				return false;
			}

			if (!((this.getRole() == null && person.getRole() == null) || this
					.getRole().equals(person.getRole()))) {
				return false;
			}

			if (!((this.getAuthorityValue() == null && person.getAuthorityValue() == null) || this
					.getAuthorityValue().equals(person.getAuthorityValue()))) {
				return false;
			}

	         if (!((this.getAuthorityURI() == null && person.getAuthorityURI() == null) || this
	                    .getAuthorityURI().equals(person.getAuthorityURI()))) {
	                return false;
	            }

			
			if (!((this.getAuthorityID() == null && person
					.getAuthorityID() == null) || this.getAuthorityID()
					.equals(person.getAuthorityID()))) {
				return false;
			}

			if (!((this.getPersontype() == null && person.getPersontype() == null) || this
					.getPersontype().equals(person.getPersontype()))) {
				return false;
			}

			if (this.isCorporation() != person.isCorporation()) {
				return false;
			}
		}
		// TODO Teldemokles says: "Do never catch a NullPointerException"!
		catch (NullPointerException npe) {
			return false;
		}

		return true;
	}

}
