package de.sub.goobi.helper.enums;

import ugh.dl.Fileformat;
import ugh.fileformats.excel.RDFFile;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.XStream;

public enum MetadataFormat {
/*
 *
 */
	
	RDF("Rdf", true, RDFFile.class),
	METS("Mets", true, MetsMods.class),
	XSTREAM("XStream", true, XStream.class),
	METS_AND_RDF("Mets & Rdf", false, null);

	private final String name;
	private final boolean usableForInternal;
	private final Class<? extends Fileformat> clazz;

	MetadataFormat(String inName, boolean inUsableForInternal, Class<? extends Fileformat> implClass) {
		name = inName;
		usableForInternal = inUsableForInternal;
		clazz = implClass;
	}

	public String getName() {
		return name;
	}
	
	public boolean isUsableForInternal() {
		return usableForInternal;
	}

	public static MetadataFormat findFileFormatsHelperByName(String inName){
		for (MetadataFormat s : MetadataFormat.values()) {
			if (s.getName().equals(inName)) {
				return s;
			}
		}
		return XSTREAM;
	}

	public static MetadataFormat getDefaultFileFormat() {
		return XSTREAM;
	}
	
	public Class<? extends Fileformat> getImplClass () {
		return clazz;
	}
	
}