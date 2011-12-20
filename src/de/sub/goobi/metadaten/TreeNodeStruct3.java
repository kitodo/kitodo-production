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

package de.sub.goobi.metadaten;

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
		children = new ArrayList<TreeNode>();
	}

	/* =============================================================== */

	public TreeNodeStruct3(String label, DocStruct struct) {
		this.label = label;
		this.struct = struct;
	}

	/* =============================================================== */

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getMainTitle() {
		return mainTitle;
	}

	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}

	public String getPpnDigital() {
		return ppnDigital;
	}

	public void setPpnDigital(String ppnDigital) {
		this.ppnDigital = ppnDigital;
	}

	public String getFirstImage() {
		return firstImage;
	}

	public void setFirstImage(String firstImage) {
		this.firstImage = firstImage;
	}

	public String getLastImage() {
		return lastImage;
	}

	public void setLastImage(String lastImage) {
		this.lastImage = lastImage;
	}

	public DocStruct getStruct() {
		return struct;
	}

	public void setStruct(DocStruct struct) {
		this.struct = struct;
	}

	public String getZblNummer() {
		return zblNummer;
	}

	public void setZblNummer(String zblNummer) {
		this.zblNummer = zblNummer;
	}

	public String getDescription() {
		return label;
	}

	public void setDescription(String description) {
		this.label = description;
	}

	public boolean isEinfuegenErlaubt() {
		return einfuegenErlaubt;
	}

	public void setEinfuegenErlaubt(boolean einfuegenErlaubt) {
		this.einfuegenErlaubt = einfuegenErlaubt;
	}

	public String getZblSeiten() {
		return zblSeiten;
	}

	public void setZblSeiten(String zblSeiten) {
		this.zblSeiten = zblSeiten;
	}

}
