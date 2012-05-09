package de.sub.goobi.Persistence.apache;

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
	
	
	public StepObject(int id, String title, int reihenfolge, int bearbeitungsstatus, Date bearbeitungszeitpunkt, Date bearbeitungsbeginn,
			Date bearbeitungsende, int bearbeitungsbenutzer, Integer editType, boolean typExport, boolean typAutomatisch, int processId) {
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
		this.setProcessId(processId);
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



}
