package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFile.java
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*******************************************************************************
 * <p>
 * A ContentFile represents a file which must be accessible via the file system
 * and contains the content of the <code>DigitalDocument</code>. A ContentFile
 * belongs always to <code>FileSet</code>, which provides methods to add and
 * remove content files (<code>addFile</code> and <code>removeFile</code>.
 * ContentFile objects are not only be part of a FileSet but must also be linked
 * to structure elements. Therefore references to <code>DocStruct</code> objects
 * exists.
 * </p>
 * 
 * @author Markus Enders
 * @author Stefan E. Funk
 * @author Robert Sehr
 * @version 2010-02-14
 * @since 2009-09-23
 * @see FileSet#addFile
 * @see FileSet#removeFile
 * 
 *      CHANGELOG
 * 
 *      14.02.2010 --- Funk --- Added method toString().
 * 
 *      13.02.2010 --- Funk --- Minor changes.
 * 
 *      17.11.2009 --- Funk --- Refactored some things for Sonar improvement.
 * 
 *      16.11.2009 --- Funk --- Added some "private"'s to class attributes. ---
 *      Removed some unused private variables. --- Refactored the get and
 *      setMimetype method.
 * 
 ******************************************************************************/

public class ContentFile implements Serializable {

	private static final long	serialVersionUID	= 367830986928498143L;

	// Contains metadata for this image.
	private List<Metadata>		allMetadata;
	private List<Metadata>		removedMetadata;
	// All physical document structures this ContentFile is referenced from.
	private List<DocStruct>		referencedDocStructs;

	// Location of the pyshical image; URL or filename.
	private String				Location;
	// Is the MimeType of an Image stored as string.
	private String				MimeType;
	// Can store subtype information; e.g. specify what kind of xml file it is;
	// what kind of compression is used in an imagefile etc.
	private String				SubType;
	// Can store an offset value in the file specified by location.
	private String				offset;
	// Type of offset (if it's byte offset, time code, etc.
	private String				offsetType;
	private String				identifier;
	//the list of techMd sections referenced by this File
	private List<Md> techMdList;
	
    private boolean isRepresentative = false;

	/***************************************************************************
	 * <p>
	 * Constructor.
	 * </p>
	 **************************************************************************/
	public ContentFile() {
		super();
	}

	/***************************************************************************
	 * @param inMD
	 * @return
	 **************************************************************************/
	public boolean addMetadata(Metadata inMD) {
		this.allMetadata.add(inMD);
		return true;
	}

	/***************************************************************************
	 * @param inMD
	 * @return
	 **************************************************************************/
	public boolean removeMetadata(Metadata inMD) {
		this.allMetadata.remove(inMD);
		this.removedMetadata.add(inMD);
		return true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public List<Metadata> getAllMetadata() {
		return this.allMetadata;
	}

	/***************************************************************************
	 * <p>
	 * Sets the filename of the ContentFile; file must at least be readable from
	 * this posision.
	 * </p>
	 * 
	 * TODO Check, if file is really available!
	 * 
	 * @param in
	 * @return always true
	 **************************************************************************/
	public boolean setLocation(String in) {
		this.Location = in;
		return true;
	}

	/***************************************************************************
	 * <p>
	 * Retrieves the filename of the ContentFile. The filename is always an
	 * absolute file.
	 * </p>
	 * 
	 * @return filename
	 **************************************************************************/
	public String getLocation() {
		return this.Location;
	}

	/**************************************************************************
	 * @param in
	 * @return
	 **************************************************************************/
	public boolean setMimetype(String in) {
		this.MimeType = in;
		return true;
	}

	/***************************************************************************
	 * @param in
	 * @deprecated
	 * @return
	 **************************************************************************/
	@Deprecated
	public boolean setMimeType(String in) {
		return setMimetype(in);
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getMimetype() {
		return this.MimeType;
	}

	/***************************************************************************
	 * @deprecated
	 * @return
	 **************************************************************************/
	@Deprecated
	public String getMimeType() {
		return getMimetype();
	}

	/***************************************************************************
	 * @param in
	 * @return
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
	 * @return
	 **************************************************************************/
	public List<DocStruct> getReferencedDocStructs() {
		return this.referencedDocStructs;
	}

	/***************************************************************************
	 * <p>
	 * Adds a reference to the current DocStruct element.
	 * </p>
	 * 
	 * @param inStruct
	 * @return true, if adding was successful
	 **************************************************************************/
	protected boolean addDocStructAsReference(DocStruct inStruct) {

		if (this.referencedDocStructs == null) {
			this.referencedDocStructs = new LinkedList<DocStruct>();
		}
		this.referencedDocStructs.add(inStruct);

		return true;
	}

	/***************************************************************************
	 * @param inStruct
	 * @return
	 **************************************************************************/
	protected boolean removeDocStructAsReference(DocStruct inStruct) {

		if (this.referencedDocStructs == null) {
			// No references available.
			return false;
		}
		this.referencedDocStructs.remove(inStruct);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ContentFile (ID: " + this.getIdentifier() + "): '"
				+ this.getLocation() + "' (" + this.getMimetype() + ")" + "\n";
	}

	/***************************************************************************
	 * <p>
	 * Overloaded equals method compares this ContentFile with parameter
	 * contentFile.
	 * </p>
	 * 
	 * @author Wulf Riebensahm
	 * @return TRUE if type and value are the same.
	 * @param ContentFile
	 *            contentFile
	 **************************************************************************/
	public boolean equals(ContentFile contentFile) {

		// Compare theses class variables. processing Strings in a try block.
		try {
			if (!((this.getMimetype() == null && contentFile.getMimetype() == null) || this
					.getMimetype().equals(contentFile.getMimetype()))) {
				return false;
			}

			if (!((this.getLocation() == null && contentFile.getLocation() == null) || this
					.getLocation().equals(contentFile.getLocation()))) {
				return false;
			}

			if (!((this.getIdentifier() == null && contentFile.getIdentifier() == null) || this
					.getIdentifier().equals(contentFile.getIdentifier()))) {
				return false;
			}
		}
		// TODO Teldemokles says: "Do never catch a NullPointerException"!
		catch (NullPointerException npe) {
			return false;
		}

		// Cchecking if same number of metadata exists.
		if (this.getAllMetadata() == null
				&& contentFile.getAllMetadata() == null) {
			return true;
		}
		if ((this.getAllMetadata() == null && contentFile.getAllMetadata() != null)
				|| (this.getAllMetadata() != null && contentFile
						.getAllMetadata() == null)) {
			return false;
		}

		if (this.getAllMetadata().size() != contentFile.getAllMetadata().size()) {
			return false;
		}

		// In detail check comparing metadata. Iterating through metadata and
		// trying to find a match, if a match is found each time.
		boolean flagFound;
		for (Metadata md1 : this.getAllMetadata()) {
			flagFound = false;
			for (Metadata md2 : contentFile.getAllMetadata()) {
				if (md1.equals(md2)) {
					flagFound = true;
					break;
				}
			}
			if (!flagFound) {
				return false;
			}
		}

		return true;
	}
	
	public List<Md> getTechMds() {
		return techMdList;
	}
	
	public void addTechMd(Md techMd) {
		if(techMdList == null) {
			techMdList = new ArrayList<Md>();
		}
		if(techMd != null) {
			techMdList.add(techMd);
		}
	}
	
	public void setTechMds(List<Md> mds) {
		if(mds != null) {			
			this.techMdList = mds;
		}
	}

    public boolean isRepresentative() {
        return isRepresentative;
    }

    public void setRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

}
