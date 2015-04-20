package ugh.dl;

/*******************************************************************************
 * ugh.dl / Reference.java
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

/*******************************************************************************
 * <p>
 * A Reference object represents a single reference. A reference links two
 * different structure entites in a non-hierarchical way. A reference may even
 * link structure entities from different structures.
 * </p>
 * <p>
 * The most common use of a <code>References</code> object is the linking
 * between a logical structure entity (as a chapter) and a physical structure
 * entity (as a page).
 * </p>
 * <p>
 * References are always storing the source and target of the link. Besides
 * these two <code>DocStruct</code> elements a type can be stored, to give
 * information about the link.
 * </p>
 * <p>
 * Usually <code>References</code> objects need not be created manually, but are
 * used internally, when creating links. You should use the appropriate methods
 * of the <code>DocStruct</code> class to set linking between
 * <code>DocStruct</code> objects.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-11-17
 * @see DocStruct#addReferenceFrom
 * @see DocStruct#addReferenceTo
 ******************************************************************************/

public class Reference implements Serializable {

	private static final long	serialVersionUID	= 8042513549869050025L;

	private String				type;
	private DocStruct			source;

	// If sourceid has a value != 0, this must be taken; source in invalid then.
	private long				sourceid;
	private DocStruct			target;
	private long				targetid;
	private boolean				updated;
	// Database ID.
	// TODO What database ID?? I do not know!! Remove it!
	private long				dbid;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public Reference() {
		super();
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public DocStruct getSource() {
		return this.source;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public long getSourceID() {
		return this.sourceid;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public DocStruct getTarget() {
		return this.target;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public long getTargetID() {
		return this.targetid;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	public void setSource(DocStruct in) {
		this.source = in;
		this.sourceid = 0;
		this.updated = true;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	public void setSourceID(long in) {
		this.sourceid = in;
		this.source = null;
		this.updated = true;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	public void setTarget(DocStruct in) {
		this.target = in;
		this.targetid = 0;
		this.updated = true;
	}

	/***************************************************************************
	 * @param in
	 **************************************************************************/
	public void setTargetID(long in) {
		this.targetid = in;
		this.target = null;
		this.updated = true;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getType() {
		return this.type;
	}

	/***************************************************************************
	 * @param intype
	 * @return
	 **************************************************************************/
	public boolean setType(String intype) {
		// TODO Check, if it's a valid type!
		this.type = intype;

		return true;
	}

}
