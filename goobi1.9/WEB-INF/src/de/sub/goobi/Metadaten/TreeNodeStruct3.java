package de.sub.goobi.Metadaten;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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
import java.util.ArrayList;

import ugh.dl.DocStruct;
import de.sub.goobi.helper.TreeNode;

public class TreeNodeStruct3 extends TreeNode {

	private DocStruct struct;
	private String firstImage;
	private String lastImage;
	private String zblNummer;
	private String mainTitle;
	private String ppnDigital;
	private String identifier;
	private String zblSeiten;
	private boolean einfuegenErlaubt;

	/**
	 * Konstruktoren
	 */
	public TreeNodeStruct3() {
	}

	/* =============================================================== */

	public TreeNodeStruct3(boolean expanded, String label, String id) {
		this.expanded = expanded;
		this.label = label;
		this.id = id;
		//TODO: Use generics
		this.children = new ArrayList<TreeNode>();
	}

	/* =============================================================== */

	public TreeNodeStruct3(String label, DocStruct struct) {
		this.label = label;
		this.struct = struct;
	}

	/* =============================================================== */

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getMainTitle() {
		if (this.mainTitle!=null && this.mainTitle.length() > 50) {
			return this.mainTitle.substring(0, 49);
		}
		
		return this.mainTitle;
	}

	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}
	
	public String getPpnDigital() {
		return this.ppnDigital;
	}

	public void setPpnDigital(String ppnDigital) {
		this.ppnDigital = ppnDigital;
	}

	public String getFirstImage() {
		return this.firstImage;
	}

	public void setFirstImage(String firstImage) {
		this.firstImage = firstImage;
	}

	public String getLastImage() {
		return this.lastImage;
	}

	public void setLastImage(String lastImage) {
		this.lastImage = lastImage;
	}

	public DocStruct getStruct() {
		return this.struct;
	}

	public void setStruct(DocStruct struct) {
		this.struct = struct;
	}

	public String getZblNummer() {
		return this.zblNummer;
	}

	public void setZblNummer(String zblNummer) {
		this.zblNummer = zblNummer;
	}

	public String getDescription() {
		return this.label;
	}

	public void setDescription(String description) {
		this.label = description;
	}

	public boolean isEinfuegenErlaubt() {
		return this.einfuegenErlaubt;
	}

	public void setEinfuegenErlaubt(boolean einfuegenErlaubt) {
		this.einfuegenErlaubt = einfuegenErlaubt;
	}

	public String getZblSeiten() {
		return this.zblSeiten;
	}

	public void setZblSeiten(String zblSeiten) {
		this.zblSeiten = zblSeiten;
	}

}
