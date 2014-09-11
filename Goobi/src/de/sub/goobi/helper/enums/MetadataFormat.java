package de.sub.goobi.helper.enums;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
