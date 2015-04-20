package ugh.fileformats.mets;

/*******************************************************************************
 * ugh.fileformats.mets / Namespace.java
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

/*******************************************************************************
 * <p>
 * Namespace.
 * </p>
 ******************************************************************************/

public class Namespace {

	protected String	prefix					= null;
	protected String	uri						= null;
	protected String	schemalocation			= null;
	protected Boolean	defaultNS				= false;
	// If the namespace is the default namespace, a container Element can be
	// named here.
	protected String	containerElementName	= "";

	/***************************************************************************
	 * @return the prefix
	 **************************************************************************/
	public String getPrefix() {
		return this.prefix;
	}

	/***************************************************************************
	 * @param prefix
	 *            the prefix to set
	 **************************************************************************/
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/***************************************************************************
	 * @return the uri
	 **************************************************************************/
	public String getUri() {
		return this.uri;
	}

	/***************************************************************************
	 * @param uri
	 *            the uri to set
	 **************************************************************************/
	public void setUri(String uri) {
		this.uri = uri;
	}

	/***************************************************************************
	 * @return the schemalocation
	 **************************************************************************/
	public String getSchemalocation() {
		return this.schemalocation;
	}

	/***************************************************************************
	 * @param schemalocation
	 *            the schemalocation to set
	 **************************************************************************/
	public void setSchemalocation(String schemalocation) {
		this.schemalocation = schemalocation;
	}

	/***************************************************************************
	 * @return the defaultNS
	 **************************************************************************/
	public Boolean getDefaultNS() {
		return this.defaultNS;
	}

	/***************************************************************************
	 * @param defaultNS
	 *            the defaultNS to set
	 **************************************************************************/
	public void setDefaultNS(Boolean defaultNS) {
		this.defaultNS = defaultNS;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public String getContainerElementName() {
		return this.containerElementName;
	}

	/***************************************************************************
	 * @param containerElementName
	 **************************************************************************/
	public void setContainerElementName(String containerElementName) {
		this.containerElementName = containerElementName;
	}

}
