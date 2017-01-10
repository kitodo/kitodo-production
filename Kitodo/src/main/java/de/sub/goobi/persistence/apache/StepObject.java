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

package de.sub.goobi.persistence.apache;

import java.util.Date;

public class StepObject {

	private int id;
	private String title;
	private int reihenfolge;
	private int bearbeitungsstatus;
	private Date bearbeitungszeitpunkt;
	private Date bearbeitungsbeginn;
	private Date bearbeitungsende;
	private int bearbeitungsbenutzer;
	private Integer editType;
	private boolean typAutomatisch = false;
	private boolean typExport = false;
	private int processId;
	private boolean typeReadAcces = false;
	private boolean typeWriteAcces = false;
	private boolean typeMetadataAccess = false;
	private boolean typeFinishImmediately = false;
	private String stepPlugin;
	private String validationPlugin;

	/**
	 * @param id add description
	 * @param title add description
	 * @param reihenfolge add description
	 * @param bearbeitungsstatus add description
	 * @param bearbeitungszeitpunkt add description
	 * @param bearbeitungsbeginn add description
	 * @param bearbeitungsende add description
	 * @param bearbeitungsbenutzer add description
	 * @param editType add description
	 * @param typExport add description
	 * @param typAutomatisch add description
	 * @param processId add description
	 * @param readAccess add description
	 * @param writeAccess add description
	 * @param metadataAccess add description
	 * @param typeFinishImmediately add description
	 * @param stepPlugin add description
	 * @param validationPlugin add description
	 */
	public StepObject(int id, String title, int reihenfolge, int bearbeitungsstatus, Date bearbeitungszeitpunkt,
			Date bearbeitungsbeginn, Date bearbeitungsende, int bearbeitungsbenutzer, Integer editType,
			boolean typExport, boolean typAutomatisch, int processId, boolean readAccess, boolean writeAccess,
			boolean metadataAccess, boolean typeFinishImmediately, String stepPlugin, String validationPlugin) {
		super();
		this.id = id;
		this.title = title;
		this.reihenfolge = reihenfolge;
		this.bearbeitungsstatus = bearbeitungsstatus;
		this.bearbeitungszeitpunkt = bearbeitungszeitpunkt;
		this.bearbeitungsbeginn = bearbeitungsbeginn;
		this.bearbeitungsende = bearbeitungsende;
		this.bearbeitungsbenutzer = bearbeitungsbenutzer;
		this.editType = editType;
		this.typExport = typExport;
		this.typAutomatisch = typAutomatisch;
		this.processId = processId;
		this.setTypeReadAcces(readAccess);
		this.typeWriteAcces = writeAccess;
		this.typeMetadataAccess = metadataAccess;
		this.setTypeFinishImmediately(typeFinishImmediately);
		this.stepPlugin = stepPlugin;
		this.validationPlugin = validationPlugin;

	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getReihenfolge() {
		return this.reihenfolge;
	}

	public void setReihenfolge(int reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public int getBearbeitungsstatus() {
		return this.bearbeitungsstatus;
	}

	public void setBearbeitungsstatus(int bearbeitungsstatus) {
		this.bearbeitungsstatus = bearbeitungsstatus;
	}

	public Date getBearbeitungszeitpunkt() {
		return this.bearbeitungszeitpunkt;
	}

	public void setBearbeitungszeitpunkt(Date bearbeitungszeitpunkt) {
		this.bearbeitungszeitpunkt = bearbeitungszeitpunkt;
	}

	public Date getBearbeitungsbeginn() {
		return this.bearbeitungsbeginn;
	}

	public void setBearbeitungsbeginn(Date bearbeitungsbeginn) {
		this.bearbeitungsbeginn = bearbeitungsbeginn;
	}

	public Date getBearbeitungsende() {
		return this.bearbeitungsende;
	}

	public void setBearbeitungsende(Date bearbeitungsende) {
		this.bearbeitungsende = bearbeitungsende;
	}

	public Integer getEditType() {
		return this.editType;
	}

	public void setEditType(Integer editType) {
		this.editType = editType;
	}

	public int getBearbeitungsbenutzer() {
		return this.bearbeitungsbenutzer;
	}

	public void setBearbeitungsbenutzer(int bearbeitungsbenutzer) {
		this.bearbeitungsbenutzer = bearbeitungsbenutzer;
	}

	public boolean isTypAutomatisch() {
		return this.typAutomatisch;
	}

	public void setTypAutomatisch(boolean typAutomatisch) {
		this.typAutomatisch = typAutomatisch;
	}

	public int getProcessId() {
		return this.processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public boolean isTypExport() {
		return this.typExport;
	}

	public void setTypExport(boolean typExport) {
		this.typExport = typExport;
	}

	public boolean isTypeWriteAcces() {
		return this.typeWriteAcces;
	}

	public void setTypeWriteAcces(boolean typeWriteAcces) {
		this.typeWriteAcces = typeWriteAcces;
	}

	public boolean isTypeMetadataAccess() {
		return this.typeMetadataAccess;
	}

	public void setTypeMetadataAccess(boolean typeMetadataAccess) {
		this.typeMetadataAccess = typeMetadataAccess;
	}

	public boolean isTypeReadAcces() {
		return typeReadAcces;
	}

	public void setTypeReadAcces(boolean typeReadAcces) {
		this.typeReadAcces = typeReadAcces;
	}

	public boolean isTypeFinishImmediately() {
		return typeFinishImmediately;
	}

	public void setTypeFinishImmediately(boolean typeFinishImmediately) {
		this.typeFinishImmediately = typeFinishImmediately;
	}

	public String getStepPlugin() {
		return stepPlugin;
	}

	public void setStepPlugin(String stepPlugin) {
		this.stepPlugin = stepPlugin;
	}

	public String getValidationPlugin() {
		return validationPlugin;
	}

	public void setValidationPlugin(String validationPlugin) {
		this.validationPlugin = validationPlugin;
	}

}
