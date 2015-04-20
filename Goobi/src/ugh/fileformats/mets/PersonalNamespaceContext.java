package ugh.fileformats.mets;

/*******************************************************************************
 * ugh.fileformats.mets / PersonalNamespaceContext.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/*******************************************************************************
 * <p>
 * PersonalNamespaceContext.
 * </p>
 ******************************************************************************/

public class PersonalNamespaceContext implements NamespaceContext {

	// Key is the prefix; value is the URI (as a String).
	private HashMap<String, Namespace>	namespaceHash	= null;

	/***************************************************************************
	 * @return the namespaceHash
	 **************************************************************************/
	public HashMap<String, Namespace> getNamespaceHash() {
		return this.namespaceHash;
	}

	/***************************************************************************
	 * @param namespaceHash
	 *            the namespaceHash to set
	 **************************************************************************/
	public void setNamespaceHash(HashMap<String, Namespace> namespaceHash) {
		this.namespaceHash = namespaceHash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This method is only be called, if there is a prefix to an element.
	 * 
	 * @see
	 * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix) {

		String uri = null;

		// System.out.println("Calling getNamespaceURI for prefix: " +
		// prefix);

		if (prefix == null) {
			throw new NullPointerException("No prefix given; prefix is null!");
		}

		if (prefix.equals("")) {
			// We are asking for the default namespace.
			Namespace ns = this.getDefaultNamespace();
			// System.out.println(" return:"+ns.getUri());
			return ns.getUri();
		}

		Namespace ns = this.getNamespaceHash().get(prefix);
		if (ns != null) {
			uri = ns.getUri();
		}
		if (uri != null) {
			// System.out.println(" return:"+uri);
			return uri;
		}

		// System.out.println("
		// return:"+XMLConstants.DEFAULT_NS_PREFIX);
		return XMLConstants.DEFAULT_NS_PREFIX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(String uri) {

		if (uri == null) {
			return null;
		}

		HashMap<String, Namespace> hm = this.getNamespaceHash();
		Set<String> keyset = hm.keySet();

		// System.out.println("Get prefix for URI "+uri);

		// Iterate over keyset.
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String key = it.next();
			// Get the uri for the key.
			Namespace keysns = hm.get(key);
			String keysuri = keysns.getUri();
			if (uri.equals(keysuri)) {
				// System.out.println("returning: "+key);
				// This is the right uri, so key is the prefix we are
				// looking for.
				return key;
			}
		}

		// No uri was found.
		return null;
	}

	/***************************************************************************
	 * @return
	 **************************************************************************/
	public Namespace getDefaultNamespace() {

		HashMap<String, Namespace> hm = this.getNamespaceHash();
		Set<String> keyset = hm.keySet();

		// Iterate over keyset.
		Iterator<String> it = keyset.iterator();
		while (it.hasNext()) {
			String key = it.next();
			// Get the uri for the key.
			Namespace keysns = hm.get(key);
			// Is this the default namespace?
			if (keysns.getDefaultNS().booleanValue()) {
				return keysns;
			}
		}

		// No uri was found.
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	public Iterator<String> getPrefixes(String uri) {
		throw new UnsupportedOperationException();
	}

}
