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
