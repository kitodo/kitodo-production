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
