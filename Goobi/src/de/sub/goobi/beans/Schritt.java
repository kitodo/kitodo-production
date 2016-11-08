package de.sub.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.HibernateUtilOld;

public class Schritt implements Serializable {
	private static final long serialVersionUID = 6831844584239811846L;
	private Integer id;
	private String titel;
	private Integer prioritaet;
	private Integer reihenfolge;
	private Integer bearbeitungsstatus;
	private Date bearbeitungszeitpunkt;
	private Date bearbeitungsbeginn;
	private Date bearbeitungsende;
	private Integer editType;
	private Benutzer bearbeitungsbenutzer;
	private short homeverzeichnisNutzen;

	private boolean typMetadaten = false;
	private boolean typAutomatisch = false;
	private boolean typImportFileUpload = false;
	private boolean typExportRus = false;
	private boolean typImagesLesen = false;
	private boolean typImagesSchreiben = false;
	private boolean typExportDMS = false;
	private boolean typBeimAnnehmenModul = false;
	private boolean typBeimAnnehmenAbschliessen = false;
	private boolean typBeimAnnehmenModulUndAbschliessen = false;
	private Boolean typScriptStep = false;
	private String scriptname1;
	private String typAutomatischScriptpfad;
	private String scriptname2;
	private String typAutomatischScriptpfad2;
	private String scriptname3;
	private String typAutomatischScriptpfad3;
	private String scriptname4;
	private String typAutomatischScriptpfad4;
	private String scriptname5;
	private String typAutomatischScriptpfad5;
	private String typModulName;
	private boolean typBeimAbschliessenVerifizieren = false;
	private Boolean batchStep = false;

	private Prozess prozess;
	private Set<Benutzer> benutzer;
	private Set<Benutzergruppe> benutzergruppen;
	private boolean panelAusgeklappt = false;
	private boolean selected = false;
	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");
	
	private String stepPlugin;
	private String validationPlugin;

	public Schritt() {
		this.titel = "";
		this.benutzer = new HashSet<Benutzer>();
		this.benutzergruppen = new HashSet<Benutzergruppe>();
		this.prioritaet = Integer.valueOf(0);
		this.reihenfolge = Integer.valueOf(0);
		setBearbeitungsstatusEnum(StepStatus.LOCKED);
	}

	/*
	 * Getter und Setter
	 */

	public Date getBearbeitungsbeginn() {
		return this.bearbeitungsbeginn;
	}

	public String getBearbeitungsbeginnAsFormattedString() {
		return Helper.getDateAsFormattedString(this.bearbeitungsbeginn);
	}

	public void setBearbeitungsbeginn(Date bearbeitungsbeginn) {
		this.bearbeitungsbeginn = bearbeitungsbeginn;
	}

	public String getStartDate() {
		if (this.bearbeitungsbeginn != null) {
			return this.formatter.format(this.bearbeitungsbeginn);
		}
		return "";
	}

	public Date getBearbeitungsende() {
		return this.bearbeitungsende;
	}

	public String getEndDate() {
		if (this.bearbeitungsende != null) {
			return this.formatter.format(this.bearbeitungsende);
		}
		return "";
	}

	public String getBearbeitungsendeAsFormattedString() {
		return Helper.getDateAsFormattedString(this.bearbeitungsende);
	}

	public void setBearbeitungsende(Date bearbeitungsende) {
		this.bearbeitungsende = bearbeitungsende;
	}

	/**
	 * getter for editType set to private for hibernate
	 * 
	 * for use in program use getEditTypeEnum instead
	 * 
	 * @return editType as integer
	 */
	@SuppressWarnings("unused")
	private Integer getEditType() {
		return this.editType;
	}

	/**
	 * set editType to defined integer. only for internal use through hibernate, for changing editType use setEditTypeEnum instead
	 * 
	 * @param editType
	 *            as Integer
	 */
	@SuppressWarnings("unused")
	private void setEditType(Integer editType) {
		this.editType = editType;
	}

	/**
	 * set editType to specific value from {@link StepEditType}
	 * 
	 * @param inType
	 *            as {@link StepEditType}
	 */
	public void setEditTypeEnum(StepEditType inType) {
		this.editType = inType.getValue();
	}

	/**
	 * get editType as {@link StepEditType}
	 * 
	 * @return current bearbeitungsstatus
	 */
	public StepEditType getEditTypeEnum() {
		return StepEditType.getTypeFromValue(this.editType);
	}

	/**
	 * getter for bearbeitungsstatus (set to private for hibernate)
	 * 
	 * for use in program use getBearbeitungsstatusEnum instead
	 * 
	 * @return bearbeitungsstatus as integer
	 */
	@SuppressWarnings("unused")
	private Integer getBearbeitungsstatus() {
		return this.bearbeitungsstatus;
	}

	/**
	 * set bearbeitungsstatus to defined integer. only for internal use through hibernate, for changing bearbeitungsstatus use
	 * setBearbeitungsstatusEnum instead
	 * 
	 * @param bearbeitungsstatus
	 *            as Integer
	 */
	@SuppressWarnings("unused")
	private void setBearbeitungsstatus(Integer bearbeitungsstatus) {
		this.bearbeitungsstatus = bearbeitungsstatus;
	}

	/**
	 * set bearbeitungsstatus to specific value from {@link StepStatus}
	 * 
	 * @param inStatus
	 *            as {@link StepStatus}
	 */
	public void setBearbeitungsstatusEnum(StepStatus inStatus) {
		this.bearbeitungsstatus = inStatus.getValue();
	}

	/**
	 * get bearbeitungsstatus as {@link StepStatus}
	 * 
	 * @return current bearbeitungsstatus
	 */
	public StepStatus getBearbeitungsstatusEnum() {
		return StepStatus.getStatusFromValue(this.bearbeitungsstatus);
	}

	public String getBearbeitungszeitpunktAsFormattedString() {
		return Helper.getDateAsFormattedString(this.bearbeitungszeitpunkt);
	}

	public Date getBearbeitungszeitpunkt() {
		return this.bearbeitungszeitpunkt;
	}

	public void setBearbeitungszeitpunkt(Date bearbeitungszeitpunkt) {
		this.bearbeitungszeitpunkt = bearbeitungszeitpunkt;
	}

	// a parameter is given here (even if not used) because jsf expects setter convention
	public void setBearbeitungszeitpunktNow(int in) {
		this.bearbeitungszeitpunkt = new Date();
	}

	public int getBearbeitungszeitpunktNow() {
		return 1;
	}

	public Benutzer getBearbeitungsbenutzer() {
		return this.bearbeitungsbenutzer;
	}

	public void setBearbeitungsbenutzer(Benutzer bearbeitungsbenutzer) {
		this.bearbeitungsbenutzer = bearbeitungsbenutzer;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPrioritaet() {
		return this.prioritaet;
	}

	public void setPrioritaet(Integer prioritaet) {
		this.prioritaet = prioritaet;
	}

	/*
	 * if you change anything in the logic of priorities make sure that you catch dependencies on this system which are not directly related to
	 * priorities
	 */
	public Boolean isCorrectionStep() {
		return (this.prioritaet == 10);
	}

	public void setCorrectionStep() {
		this.prioritaet = 10;
	}

	public Prozess getProzess() {
		return this.prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public Integer getReihenfolge() {
		return this.reihenfolge;
	}

	public void setReihenfolge(Integer reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public String getTitelLokalisiert() {
		return Helper.getTranslation(this.titel);
	}

	public String getTitel() {
		return this.titel;
	}

	public String getNormalizedTitle() {
		return this.titel.replace(" ", "_");
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public boolean isPanelAusgeklappt() {
		return this.panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Set<Benutzer> getBenutzer() {
		return this.benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public Set<Benutzergruppe> getBenutzergruppen() {
		return this.benutzergruppen;
	}

	public void setBenutzergruppen(Set<Benutzergruppe> benutzergruppen) {
		this.benutzergruppen = benutzergruppen;
	}

	/*
	 *  Helper
	 */

	public int getBenutzerSize() {
		try {
			Hibernate.initialize(this.benutzer);
		} catch (HibernateException e) {
		}
		if (this.benutzer == null) {
			return 0;
		} else {
			return this.benutzer.size();
		}
	}

	public List<Benutzer> getBenutzerList() {
		try {
			Hibernate.initialize(this.benutzer);
		} catch (HibernateException e) {
		}
		if (this.benutzer == null) {
			return new ArrayList<Benutzer>();
		}
		return new ArrayList<Benutzer>(this.benutzer);
	}

	public int getBenutzergruppenSize() {
		try {
			Hibernate.initialize(this.benutzergruppen);
		} catch (HibernateException e) {
		}
		if (this.benutzergruppen == null) {
			return 0;
		} else {
			return this.benutzergruppen.size();
		}
	}

	public List<Benutzergruppe> getBenutzergruppenList() {
		try {
			Hibernate.initialize(this.benutzergruppen);
		} catch (HibernateException e) {
		}
		if (this.benutzergruppen == null) {
			return new ArrayList<Benutzergruppe>();
		}
		return new ArrayList<Benutzergruppe>(this.benutzergruppen);
	}

	public void setBearbeitungsstatusUp() {
		if (getBearbeitungsstatusEnum() != StepStatus.DONE) {
			this.bearbeitungsstatus = Integer.valueOf(this.bearbeitungsstatus.intValue() + 1);
		}
	}

	public void setBearbeitungsstatusDown() {
		if (getBearbeitungsstatusEnum() != StepStatus.LOCKED) {
			this.bearbeitungsstatus = Integer.valueOf(this.bearbeitungsstatus.intValue() - 1);
		}
	}

	public short getHomeverzeichnisNutzen() {
		return this.homeverzeichnisNutzen;
	}

	public void setHomeverzeichnisNutzen(short homeverzeichnisNutzen) {
		this.homeverzeichnisNutzen = homeverzeichnisNutzen;
	}

	public boolean isTypExportRus() {
		return this.typExportRus;
	}

	public void setTypExportRus(boolean typExportRus) {
		this.typExportRus = typExportRus;
	}

	public boolean isTypImagesLesen() {
		return this.typImagesLesen;
	}

	public void setTypImagesLesen(boolean typImagesLesen) {
		this.typImagesLesen = typImagesLesen;
	}

	public boolean isTypImagesSchreiben() {
		return this.typImagesSchreiben;
	}

	public void setTypImagesSchreiben(boolean typImagesSchreiben) {
		this.typImagesSchreiben = typImagesSchreiben;
		if (typImagesSchreiben) {
			this.typImagesLesen = true;
		}
	}

	public boolean isTypExportDMS() {
		return this.typExportDMS;
	}

	public void setTypExportDMS(boolean typExportDMS) {
		this.typExportDMS = typExportDMS;
	}

	public boolean isTypImportFileUpload() {
		return this.typImportFileUpload;
	}

	public void setTypImportFileUpload(boolean typImportFileUpload) {
		this.typImportFileUpload = typImportFileUpload;
	}

	public boolean isTypMetadaten() {
		return this.typMetadaten;
	}

	public void setTypMetadaten(boolean typMetadaten) {
		this.typMetadaten = typMetadaten;
	}

	public boolean isTypBeimAnnehmenAbschliessen() {
		return this.typBeimAnnehmenAbschliessen;
	}

	public void setTypBeimAnnehmenAbschliessen(boolean typBeimAnnehmenAbschliessen) {
		this.typBeimAnnehmenAbschliessen = typBeimAnnehmenAbschliessen;
	}

	public boolean isTypBeimAnnehmenModul() {
		return this.typBeimAnnehmenModul;
	}

	public void setTypBeimAnnehmenModul(boolean typBeimAnnehmenModul) {
		this.typBeimAnnehmenModul = typBeimAnnehmenModul;
	}

	public boolean isTypBeimAnnehmenModulUndAbschliessen() {
		return this.typBeimAnnehmenModulUndAbschliessen;
	}

	public void setTypBeimAnnehmenModulUndAbschliessen(boolean typBeimAnnehmenModulUndAbschliessen) {
		this.typBeimAnnehmenModulUndAbschliessen = typBeimAnnehmenModulUndAbschliessen;
	}

	public boolean isTypAutomatisch() {
		return this.typAutomatisch;
	}

	public void setTypAutomatisch(boolean typAutomatisch) {
		this.typAutomatisch = typAutomatisch;
	}

	public boolean isTypBeimAbschliessenVerifizieren() {
		return this.typBeimAbschliessenVerifizieren;
	}

	public void setTypBeimAbschliessenVerifizieren(boolean typBeimAbschliessenVerifizieren) {
		this.typBeimAbschliessenVerifizieren = typBeimAbschliessenVerifizieren;
	}

	public String getTypModulName() {
		return this.typModulName;
	}

	public void setTypModulName(String typModulName) {
		this.typModulName = typModulName;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/*
	 * Helper
	 */

	/**
	 * @return Rückgabe des Schritttitels sowie (sofern vorhanden) den Benutzer mit vollständigem Namen
	 */
	public String getTitelMitBenutzername() {
		String rueckgabe = this.titel;
		if (this.bearbeitungsbenutzer != null && this.bearbeitungsbenutzer.getId() != null && this.bearbeitungsbenutzer.getId().intValue() != 0) {
			rueckgabe += " (" + this.bearbeitungsbenutzer.getNachVorname() + ")";
		}
		return rueckgabe;
	}

	public String getBearbeitungsstatusAsString() {
		return String.valueOf(this.bearbeitungsstatus.intValue());
	}

	public void setBearbeitungsstatusAsString(String inbearbeitungsstatus) {
		this.bearbeitungsstatus = Integer.parseInt(inbearbeitungsstatus);
	}

	public void setTypScriptStep(Boolean typScriptStep) {
		this.typScriptStep = typScriptStep;
	}

	public Boolean getTypScriptStep() {
		if (this.typScriptStep == null) {
			this.typScriptStep = false;
		}
		return this.typScriptStep;
	}

	public void setScriptname1(String scriptname1) {
		this.scriptname1 = scriptname1;
	}

	public String getScriptname1() {
		return this.scriptname1;
	}

	public String getTypAutomatischScriptpfad() {
		return this.typAutomatischScriptpfad;
	}

	public void setTypAutomatischScriptpfad(String typAutomatischScriptpfad) {
		this.typAutomatischScriptpfad = typAutomatischScriptpfad;
	}

	public void setScriptname2(String scriptname2) {
		this.scriptname2 = scriptname2;
	}

	public String getScriptname2() {
		return this.scriptname2;
	}

	public void setTypAutomatischScriptpfad2(String typAutomatischScriptpfad2) {
		this.typAutomatischScriptpfad2 = typAutomatischScriptpfad2;
	}

	public String getTypAutomatischScriptpfad2() {
		return this.typAutomatischScriptpfad2;
	}

	public void setScriptname3(String scriptname3) {
		this.scriptname3 = scriptname3;
	}

	public String getScriptname3() {
		return this.scriptname3;
	}

	public void setTypAutomatischScriptpfad3(String typAutomatischScriptpfad3) {
		this.typAutomatischScriptpfad3 = typAutomatischScriptpfad3;
	}

	public String getTypAutomatischScriptpfad3() {
		return this.typAutomatischScriptpfad3;
	}

	public void setScriptname4(String scriptname4) {
		this.scriptname4 = scriptname4;
	}

	public String getScriptname4() {
		return this.scriptname4;
	}

	public void setTypAutomatischScriptpfad4(String typAutomatischScriptpfad4) {
		this.typAutomatischScriptpfad4 = typAutomatischScriptpfad4;
	}

	public String getTypAutomatischScriptpfad4() {
		return this.typAutomatischScriptpfad4;
	}

	public void setScriptname5(String scriptname5) {
		this.scriptname5 = scriptname5;
	}

	public String getScriptname5() {
		return this.scriptname5;
	}

	public void setTypAutomatischScriptpfad5(String typAutomatischScriptpfad5) {
		this.typAutomatischScriptpfad5 = typAutomatischScriptpfad5;
	}

	public String getTypAutomatischScriptpfad5() {
		return this.typAutomatischScriptpfad5;
	}

	public ArrayList<String> getAllScriptPaths() {
		ArrayList<String> answer = new ArrayList<String>();
		if (this.typAutomatischScriptpfad != null && !this.typAutomatischScriptpfad.equals("")) {
			answer.add(this.typAutomatischScriptpfad);
		}
		if (this.typAutomatischScriptpfad2 != null && !this.typAutomatischScriptpfad2.equals("")) {
			answer.add(this.typAutomatischScriptpfad2);
		}
		if (this.typAutomatischScriptpfad3 != null && !this.typAutomatischScriptpfad3.equals("")) {
			answer.add(this.typAutomatischScriptpfad3);
		}
		if (this.typAutomatischScriptpfad4 != null && !this.typAutomatischScriptpfad4.equals("")) {
			answer.add(this.typAutomatischScriptpfad4);
		}
		if (this.typAutomatischScriptpfad5 != null && !this.typAutomatischScriptpfad5.equals("")) {
			answer.add(this.typAutomatischScriptpfad5);
		}
		return answer;
	}

	public HashMap<String, String> getAllScripts() {
		HashMap<String, String> answer = new HashMap<String, String>();
		if (this.typAutomatischScriptpfad != null && !this.typAutomatischScriptpfad.equals("")) {
			answer.put(this.scriptname1, this.typAutomatischScriptpfad);
		}
		if (this.typAutomatischScriptpfad2 != null && !this.typAutomatischScriptpfad2.equals("")) {
			answer.put(this.scriptname2, this.typAutomatischScriptpfad2);
		}
		if (this.typAutomatischScriptpfad3 != null && !this.typAutomatischScriptpfad3.equals("")) {
			answer.put(this.scriptname3, this.typAutomatischScriptpfad3);
		}
		if (this.typAutomatischScriptpfad4 != null && !this.typAutomatischScriptpfad4.equals("")) {
			answer.put(this.scriptname4, this.typAutomatischScriptpfad4);
		}
		if (this.typAutomatischScriptpfad5 != null && !this.typAutomatischScriptpfad5.equals("")) {
			answer.put(this.scriptname5, this.typAutomatischScriptpfad5);
		}
		return answer;
	}

	public void setAllScripts(HashMap<String, String> paths) {
		Set<String> keys = paths.keySet();
		ArrayList<String> keyList = new ArrayList<String>();
		for (String key : keys) {
			keyList.add(key);
		}
		int size = keyList.size();
		if (size > 0) {
			this.scriptname1 = keyList.get(0);
			this.typAutomatischScriptpfad = paths.get(keyList.get(0));
		}
		if (size > 1) {
			this.scriptname2 = keyList.get(1);
			this.typAutomatischScriptpfad2 = paths.get(keyList.get(1));
		}
		if (size > 2) {
			this.scriptname3 = keyList.get(2);
			this.typAutomatischScriptpfad3 = paths.get(keyList.get(2));
		}
		if (size > 3) {
			this.scriptname4 = keyList.get(3);
			this.typAutomatischScriptpfad4 = paths.get(keyList.get(3));
		}
		if (size > 4) {
			this.scriptname5 = keyList.get(4);
			this.typAutomatischScriptpfad5 = paths.get(keyList.get(4));
		}
	}

	public String getListOfPaths() {
		String answer = "";
		if (this.scriptname1 != null) {
			answer += this.scriptname1;
		}
		if (this.scriptname2 != null) {
			answer = answer + "; " + this.scriptname2;
		}
		if (this.scriptname3 != null) {
			answer = answer + "; " + this.scriptname3;
		}
		if (this.scriptname4 != null) {
			answer = answer + "; " + this.scriptname4;
		}
		if (this.scriptname5 != null) {
			answer = answer + "; " + this.scriptname5;
		}
		return answer;

	}

	/*
	 * batch step information
	 */

	public Boolean getBatchStep() {
		if (this.batchStep == null) {
			this.batchStep = Boolean.FALSE;
		}
		return this.batchStep;
	}

	public Boolean isBatchStep() {
		if (this.batchStep == null) {
			this.batchStep = Boolean.FALSE;
		}
		return this.batchStep;
	}

	public void setBatchStep(Boolean batchStep) {
		if (batchStep == null) {
			batchStep = Boolean.FALSE;
		}
		this.batchStep = batchStep;
	}

	/**
	 * Get the current object for this row.
	 * 
	 * @return Employee The current object representing a row.
	 */
	public Schritt getCurrent() {
		boolean hasOpen = HibernateUtilOld.hasOpenSession();
		Session sess = Helper.getHibernateSession();

		Schritt current = (Schritt) sess.get(Schritt.class, this.getId());
		if (current == null) {
			current = (Schritt) sess.load(Schritt.class, this.getId());
		}
		if (!hasOpen) {
			current.benutzer.size();
			current.benutzergruppen.size();
			sess.close();
		}
		return current;
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

	/**
	 * Returns whether this is a step of a process that is part of at least one
	 * batch as read-only property "batchSize".
	 * 
	 * @return whether this step’s process is in a batch
	 */
	public boolean isBatchSize() {
		return prozess.getBatchesInitialized().size() > 0;
	}
}
