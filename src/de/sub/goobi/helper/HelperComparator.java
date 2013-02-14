/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper;

//TODO: Use generics (MetadataType)
//TODO: Check if this can be moved into UGH
import java.io.Serializable;
import java.util.Comparator;

import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;

public class HelperComparator implements Comparator<Object>, Serializable {

	private static final long serialVersionUID = -1124724462982810327L;
	private String Sortierart;

	public int compare(Object o1, Object o2) {
		// TODO: Use a Enum or Integer conts
		int rueckgabe = 0;
		if (Sortierart.equals("MetadatenTypen"))
			rueckgabe = compareMetadatenTypen(o1, o2);
		if (Sortierart.equals("Metadata"))
			rueckgabe = compareMetadata(o1, o2);
		if (Sortierart.equals("DocStructTypen"))
			rueckgabe = compareDocStructTypen(o1, o2);
		return rueckgabe;
	}

	public boolean equals(Object obj) {
		return this == obj;
	}

	public void setSortierart(String sortierart) {
		Sortierart = sortierart;
	}

	private int compareMetadatenTypen(Object o1, Object o2) {
		MetadataType s1 = (MetadataType) o1;
		MetadataType s2 = (MetadataType) o2;
		String name1 = s1.getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		String name2 = s2.getLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		if (name1 == null)
			name1 = "";
		if (name2 == null)
			name2 = "";
		return name1.compareToIgnoreCase(name2);
	}

	private int compareMetadata(Object o1, Object o2) {
		Metadata s1 = (Metadata) o1;
		Metadata s2 = (Metadata) o2;
		String name1 = s1.getType().getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		String name2 = s2.getType().getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		if (name1 == null)
			name1 = s1.getType().getName();
		if (name2 == null)
			name2 = s2.getType().getName();
		return name1.compareToIgnoreCase(name2);
	}

	private int compareDocStructTypen(Object o1, Object o2) {
		DocStructType s1 = (DocStructType) o1;
		DocStructType s2 = (DocStructType) o2;
		String name1 = s1.getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		String name2 = s2.getNameByLanguage((String) Helper.getManagedBeanValue("#{LoginForm.myBenutzer.metadatenSprache}"));
		if (name1 == null)
			name1 = "";
		if (name2 == null)
			name2 = "";
		return name1.compareToIgnoreCase(name2);
	}

}
