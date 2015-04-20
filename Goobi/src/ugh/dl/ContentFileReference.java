package ugh.dl;

/*******************************************************************************
 * ugh.dl / ContentFileReference.java
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
 * A ContentFileReference stores a single reference from a DocStruct to a
 * ContentFile. This reference can contain additional information as the an area
 * information An Area defines a special part of the ContentFile to which it is
 * linked to. An Area is defined in a <code>ContentFileArea</code> object.
 * </p>
 * 
 * @author Markus Enders
 * @version 2009-12-09
 * @see ContentFileArea
 ******************************************************************************/

public class ContentFileReference implements Serializable {

	private static final long	serialVersionUID	= 3878365395668660681L;

	// Contentfile Area.
	private ContentFileArea		cfa					= null;
	// ContentFile object.
	private ContentFile			cf					= null;

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public ContentFile getCf() {
		return this.cf;
	}

	/***************************************************************************
	 * @param cf
	 **************************************************************************/
	public void setCf(ContentFile cf) {
		this.cf = cf;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public ContentFileArea getCfa() {
		return this.cfa;
	}

	/***************************************************************************
	 * @param cfa
	 **************************************************************************/
	public void setCfa(ContentFileArea cfa) {
		this.cfa = cfa;
	}

	/***************************************************************************
	 * <p>
	 * Passing on to referred ContentFile.
	 * </p>
	 * 
	 * TODO Find out, if same ContentFileArea would be a requirement.
	 * 
	 * @author Wulf Riebensahm
	 * @param ContentFileReference
	 *            contentFileReference
	 ***************************************************************************/
	public boolean equals(ContentFileReference contentFileReference) {
		return this.getCf().equals(contentFileReference.getCf());
	}

}
