/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

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
		this.name = inName;
		this.usableForInternal = inUsableForInternal;
		this.clazz = implClass;
	}

	public String getName() {
		return this.name;
	}
	
	public boolean isUsableForInternal() {
		return this.usableForInternal;
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
		return this.clazz;
	}
	
}
