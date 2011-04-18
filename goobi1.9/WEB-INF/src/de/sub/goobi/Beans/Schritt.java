package de.sub.goobi.Beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.Beans.Property.DisplayPropertyList;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.Beans.Property.IGoobiProperty;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;

public class Schritt implements Serializable, IGoobiEntity {
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
	//   private Integer typ;
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

	private Prozess prozess;
	private Set<Schritteigenschaft> eigenschaften;
	private Set<Benutzer> benutzer;
	private Set<Benutzergruppe> benutzergruppen;
	private boolean panelAusgeklappt = false;
	private boolean selected = false;
	private DisplayPropertyList displayProperties;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");

	
	public Schritt() {
		titel = "";
		eigenschaften = new HashSet<Schritteigenschaft>();
		benutzer = new HashSet<Benutzer>();
		benutzergruppen = new HashSet<Benutzergruppe>();
		prioritaet = Integer.valueOf(0);
		reihenfolge = Integer.valueOf(0);
		setBearbeitungsstatusEnum(StepStatus.LOCKED);
	}

	/*#####################################################
	 #####################################################
	 ##                                                                                                                          
	 ##   Getter und Setter                                   
	 ##                                                                                                                 
	 #####################################################
	 ####################################################*/

	public Date getBearbeitungsbeginn() {
		return bearbeitungsbeginn;
	}

	public String getBearbeitungsbeginnAsFormattedString() {
		return Helper.getDateAsFormattedString(bearbeitungsbeginn);
	}

	public void setBearbeitungsbeginn(Date bearbeitungsbeginn) {
		this.bearbeitungsbeginn = bearbeitungsbeginn;
	}

	public String getStartDate() {
		if (bearbeitungsbeginn != null) {
			return formatter.format(bearbeitungsbeginn);
		}
		return "";
	}
	
	public Date getBearbeitungsende() {
		return bearbeitungsende;
	}
	
	public String getEndDate() {
		if (bearbeitungsende != null) {
			return formatter.format(bearbeitungsende);
		}
		return "";
	}

	public String getBearbeitungsendeAsFormattedString() {
		return Helper.getDateAsFormattedString(bearbeitungsende);
	}

	public void setBearbeitungsende(Date bearbeitungsende) {
		this.bearbeitungsende = bearbeitungsende;
	}
	
	/**
	 * getter for editType set to private for hibernate
	 * 
	 * for use in programm use getEditTypeEnum instead
	 * @return editType as integer
	 */
	@SuppressWarnings("unused")
	private Integer getEditType() {
		return editType;
	}
	
	/**
	 * set editType to defined integer. only for internal 
	 * use through hibernate, for changing editType use 
	 * setEditTypeEnum instead
	 * @param editType as Integer
	 */
	@SuppressWarnings("unused")
	private void setEditType(Integer editType) {
		this.editType = editType;
	}
	
	/**
	 * set editType to specific value from {@link StepEditType}
	 * 
	 * @param inType as {@link StepEditType}
	 */
	public void setEditTypeEnum(StepEditType inType){
		this.editType = inType.getValue();
	}
	
	/**
	 * get editType as {@link StepEditType}
	 * 
	 * @return current bearbeitungsstatus
	 */
	public StepEditType getEditTypeEnum(){
		return StepEditType.getTypeFromValue(editType);
	}
	
	/**
	 * getter for Bearbeitunsstatus set to private for hibernate
	 * 
	 * for use in programm use getBearbeitungsstatusEnum instead
	 * @return bearbeitungsstatus as integer
	 */
	@SuppressWarnings("unused")
	private Integer getBearbeitungsstatus() {
		return bearbeitungsstatus;
	}

	/**
	 * set bearbeitungsstatus to defined integer. only for internal 
	 * use through hibernate, for changing bearbeitungsstatus use 
	 * setBearbeitungsstatusEnum instead
	 * @param bearbeitungsstatus as Integer
	 */
	@SuppressWarnings("unused")
	private void setBearbeitungsstatus(Integer bearbeitungsstatus) {
		this.bearbeitungsstatus = bearbeitungsstatus;
	}
	
	/**
	 * set bearbeitungsstatus to specific value from {@link StepStatus}
	 * 
	 * @param inStatus as {@link StepStatus}
	 */
	public void setBearbeitungsstatusEnum(StepStatus inStatus){
		this.bearbeitungsstatus = inStatus.getValue();
	}
	
	/**
	 * get bearbeitungsstatus as {@link StepStatus}
	 * 
	 * @return current bearbeitungsstatus
	 */
	public StepStatus getBearbeitungsstatusEnum(){
		return StepStatus.getStatusFromValue(bearbeitungsstatus);
	}

	public String getBearbeitungszeitpunktAsFormattedString() {
		return Helper.getDateAsFormattedString(bearbeitungszeitpunkt);
	}

	public Date getBearbeitungszeitpunkt() {
		return bearbeitungszeitpunkt;
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
		return bearbeitungsbenutzer;
	}

	public void setBearbeitungsbenutzer(Benutzer bearbeitungsbenutzer) {
		this.bearbeitungsbenutzer = bearbeitungsbenutzer;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPrioritaet() {
		return prioritaet;
	}

	public void setPrioritaet(Integer prioritaet) {
		this.prioritaet = prioritaet;
	}

	/*  if you change anything in the logic of priorities
	 * make sure that you catch dependencies on this system
	 * which are not directly related to priorities
	 */
	public Boolean isCorrectionStep(){
		return (prioritaet==10);
	}

	public void setCorrectionStep(){
		prioritaet=10;
	}
	
	public Prozess getProzess() {
		return prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public Integer getReihenfolge() {
		return reihenfolge;
	}

	public void setReihenfolge(Integer reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public String getTitelLokalisiert() {
		return Helper.getTranslation(titel);
	}

	public String getTitel() {
		return titel;
	}
	
	public String getNormalizedTitle() {
		return titel.replace(" ", "_");
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	//   public Integer getTyp() {
	//      return typ;
	//   }
	//
	//   public void setTyp(Integer typ) {
	//      this.typ = typ;
	//   }

	public boolean isPanelAusgeklappt() {
		return panelAusgeklappt;
	}

	public void setPanelAusgeklappt(boolean panelAusgeklappt) {
		this.panelAusgeklappt = panelAusgeklappt;
	}

	public Set<Schritteigenschaft> getEigenschaften() {
		if (eigenschaften == null)
			eigenschaften = new HashSet<Schritteigenschaft>();
		return eigenschaften;
	}

	public void setEigenschaften(Set<Schritteigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	public Set<Benutzer> getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public Set<Benutzergruppe> getBenutzergruppen() {
		return benutzergruppen;
	}

	public void setBenutzergruppen(Set<Benutzergruppe> benutzergruppen) {
		this.benutzergruppen = benutzergruppen;
	}

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##																Helper									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	public int getEigenschaftenSize() {
		if (eigenschaften == null)
			return 0;
		else
			return eigenschaften.size();
	}

	public List<Schritteigenschaft> getEigenschaftenList() {
		if (eigenschaften == null)
			return new ArrayList<Schritteigenschaft>();
		return new ArrayList<Schritteigenschaft>(eigenschaften);
	}

	public int getBenutzerSize() {
		if (benutzer == null)
			return 0;
		else
			return benutzer.size();
	}

	public List<Benutzer> getBenutzerList() {
		if (benutzer == null)
			return new ArrayList<Benutzer>();
		return new ArrayList<Benutzer>(benutzer);
	}

	public int getBenutzergruppenSize() {
		if (benutzergruppen == null)
			return 0;
		else
			return benutzergruppen.size();
	}

	public List<Benutzergruppe> getBenutzergruppenList() {
		if (benutzergruppen == null)
			return new ArrayList<Benutzergruppe>();
		return new ArrayList<Benutzergruppe>(benutzergruppen);
	}

	public void setBearbeitungsstatusUp() {
		if (getBearbeitungsstatusEnum() != StepStatus.DONE)
			bearbeitungsstatus = Integer.valueOf(bearbeitungsstatus.intValue() + 1);
	}

	public void setBearbeitungsstatusDown() {
		if (getBearbeitungsstatusEnum() != StepStatus.LOCKED)
			bearbeitungsstatus = Integer.valueOf(bearbeitungsstatus.intValue() - 1);
	}

	public short getHomeverzeichnisNutzen() {
		return homeverzeichnisNutzen;
	}

	public void setHomeverzeichnisNutzen(short homeverzeichnisNutzen) {
		this.homeverzeichnisNutzen = homeverzeichnisNutzen;
	}

	public boolean isTypExportRus() {
		return typExportRus;
	}

	public void setTypExportRus(boolean typExportRus) {
		this.typExportRus = typExportRus;
	}

	public boolean isTypImagesLesen() {
		return typImagesLesen;
	}

	public void setTypImagesLesen(boolean typImagesLesen) {
		this.typImagesLesen = typImagesLesen;
	}

	public boolean isTypImagesSchreiben() {
		return typImagesSchreiben;
	}

	public void setTypImagesSchreiben(boolean typImagesSchreiben) {
		this.typImagesSchreiben = typImagesSchreiben;
		if (typImagesSchreiben)
			this.typImagesLesen = true;
	}

	public boolean isTypExportDMS() {
		return typExportDMS;
	}

	public void setTypExportDMS(boolean typExportDMS) {
		this.typExportDMS = typExportDMS;
	}

	public boolean isTypImportFileUpload() {
		return typImportFileUpload;
	}

	public void setTypImportFileUpload(boolean typImportFileUpload) {
		this.typImportFileUpload = typImportFileUpload;
	}

	public boolean isTypMetadaten() {
		return typMetadaten;
	}

	public void setTypMetadaten(boolean typMetadaten) {
		this.typMetadaten = typMetadaten;
	}

	public boolean isTypBeimAnnehmenAbschliessen() {
		return typBeimAnnehmenAbschliessen;
	}

	public void setTypBeimAnnehmenAbschliessen(boolean typBeimAnnehmenAbschliessen) {
		this.typBeimAnnehmenAbschliessen = typBeimAnnehmenAbschliessen;
	}

	public boolean isTypBeimAnnehmenModul() {
		return typBeimAnnehmenModul;
	}

	public void setTypBeimAnnehmenModul(boolean typBeimAnnehmenModul) {
		this.typBeimAnnehmenModul = typBeimAnnehmenModul;
	}

	public boolean isTypBeimAnnehmenModulUndAbschliessen() {
		return typBeimAnnehmenModulUndAbschliessen;
	}

	public void setTypBeimAnnehmenModulUndAbschliessen(boolean typBeimAnnehmenModulUndAbschliessen) {
		this.typBeimAnnehmenModulUndAbschliessen = typBeimAnnehmenModulUndAbschliessen;
	}

	public boolean isTypAutomatisch() {
		return typAutomatisch;
	}

	public void setTypAutomatisch(boolean typAutomatisch) {
		this.typAutomatisch = typAutomatisch;
	}

	public boolean isTypBeimAbschliessenVerifizieren() {
		return typBeimAbschliessenVerifizieren;
	}

	public void setTypBeimAbschliessenVerifizieren(boolean typBeimAbschliessenVerifizieren) {
		this.typBeimAbschliessenVerifizieren = typBeimAbschliessenVerifizieren;
	}

	public String getTypModulName() {
		return typModulName;
	}

	public void setTypModulName(String typModulName) {
		this.typModulName = typModulName;
	}



	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##											Helper									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	/**
	 * @return Rückgabe des Schritttitels sowie (sofern vorhanden) den Benutzer mit vollständigem Namen
	 */
	public String getTitelMitBenutzername() {
		String rueckgabe = titel;
		if (bearbeitungsbenutzer != null && bearbeitungsbenutzer.getId() != null
				&& bearbeitungsbenutzer.getId().intValue() != 0)
			rueckgabe += " (" + bearbeitungsbenutzer.getNachVorname() + ")";
		return rueckgabe;
	}

	public String getBearbeitungsstatusAsString() {
		return String.valueOf(bearbeitungsstatus.intValue());
	}

	public void setBearbeitungsstatusAsString(String inbearbeitungsstatus) {
		this.bearbeitungsstatus = Integer.parseInt(inbearbeitungsstatus);
	}

	public void setTypScriptStep(Boolean typScriptStep) {
		this.typScriptStep = typScriptStep;
	}

	public Boolean getTypScriptStep() {
		if (typScriptStep==null) {
			this.typScriptStep = false;
		}
		return typScriptStep;
	}

	public void setScriptname1(String scriptname1) {
		this.scriptname1 = scriptname1;
	}

	public String getScriptname1() {
		return scriptname1;
	}
	
	
	public String getTypAutomatischScriptpfad() {
		return typAutomatischScriptpfad;
	}

	public void setTypAutomatischScriptpfad(String typAutomatischScriptpfad) {
		this.typAutomatischScriptpfad = typAutomatischScriptpfad;
	}

	public void setScriptname2(String scriptname2) {
		this.scriptname2 = scriptname2;
	}

	
	public String getScriptname2() {
		return scriptname2;
	}

	public void setTypAutomatischScriptpfad2(String typAutomatischScriptpfad2) {
		this.typAutomatischScriptpfad2 = typAutomatischScriptpfad2;
	}

	public String getTypAutomatischScriptpfad2() {
		return typAutomatischScriptpfad2;
	}

	public void setScriptname3(String scriptname3) {
		this.scriptname3 = scriptname3;
	}

	public String getScriptname3() {
		return scriptname3;
	}

	public void setTypAutomatischScriptpfad3(String typAutomatischScriptpfad3) {
		this.typAutomatischScriptpfad3 = typAutomatischScriptpfad3;
	}

	public String getTypAutomatischScriptpfad3() {
		return typAutomatischScriptpfad3;
	}

	public void setScriptname4(String scriptname4) {
		this.scriptname4 = scriptname4;
	}

	public String getScriptname4() {
		return scriptname4;
	}

	public void setTypAutomatischScriptpfad4(String typAutomatischScriptpfad4) {
		this.typAutomatischScriptpfad4 = typAutomatischScriptpfad4;
	}

	public String getTypAutomatischScriptpfad4() {
		return typAutomatischScriptpfad4;
	}

	public void setScriptname5(String scriptname5) {
		this.scriptname5 = scriptname5;
	}

	public String getScriptname5() {
		return scriptname5;
	}

	public void setTypAutomatischScriptpfad5(String typAutomatischScriptpfad5) {
		this.typAutomatischScriptpfad5 = typAutomatischScriptpfad5;
	}

	public String getTypAutomatischScriptpfad5() {
		return typAutomatischScriptpfad5;
	}
	
	public ArrayList<String> getAllScriptPaths() {
		ArrayList<String> answer = new ArrayList<String>();
		if (typAutomatischScriptpfad != null) {
			answer.add(typAutomatischScriptpfad);
		}
		if (typAutomatischScriptpfad2 != null) {
			answer.add(typAutomatischScriptpfad2);
		}
		if (typAutomatischScriptpfad3 != null) {
			answer.add(typAutomatischScriptpfad3);
		}
		if (typAutomatischScriptpfad4 != null) {
			answer.add(typAutomatischScriptpfad4);
		}
		if (typAutomatischScriptpfad5 != null) {
			answer.add(typAutomatischScriptpfad5);
		}
		return answer;
	}
	
	public HashMap<String, String> getAllScripts(){
		HashMap<String,String> answer = new HashMap<String, String>();
		if (typAutomatischScriptpfad != null && !typAutomatischScriptpfad.equals("")) {
			answer.put(scriptname1, typAutomatischScriptpfad);
		}
		if (typAutomatischScriptpfad2 != null && !typAutomatischScriptpfad2.equals("")) {
			answer.put(scriptname2, typAutomatischScriptpfad2);
		}
		if (typAutomatischScriptpfad3 != null && !typAutomatischScriptpfad3.equals("")) {
			answer.put(scriptname3, typAutomatischScriptpfad3);
		}
		if (typAutomatischScriptpfad4 != null && !typAutomatischScriptpfad4.equals("")) {
			answer.put(scriptname4, typAutomatischScriptpfad4);
		}
		if (typAutomatischScriptpfad5 != null && !typAutomatischScriptpfad5.equals("")) {
			answer.put(scriptname5, typAutomatischScriptpfad5);
		}		
		return answer;
	}
	
	public void setAllScripts(HashMap<String,String> paths){
		Set<String> keys = paths.keySet();
		ArrayList<String> keyList = new ArrayList<String>();
		for (String key : keys) {
			keyList.add(key);
		}
		int size = keyList.size();
		if (size > 0){
			scriptname1 = keyList.get(0);
			typAutomatischScriptpfad = paths.get(keyList.get(0));
		}
		if (size > 1){
			scriptname2 = keyList.get(1);
			typAutomatischScriptpfad2 = paths.get(keyList.get(1));
		}
		if (size > 2){
			scriptname3 = keyList.get(2);
			typAutomatischScriptpfad3 = paths.get(keyList.get(2));
		}
		if (size > 3){
			scriptname4 = keyList.get(3);
			typAutomatischScriptpfad4 = paths.get(keyList.get(3));
		}
		if (size > 4){
			scriptname5 = keyList.get(4);
			typAutomatischScriptpfad5 = paths.get(keyList.get(4));
		}
	}
	
	public String getListOfPaths(){
		String answer = "";
		if (scriptname1!=null){
			answer+=scriptname1;
		}
		if (scriptname2!=null){
			answer= answer+"; "+scriptname2;
		}
		if (scriptname3!=null){
			answer= answer+"; "+scriptname3;
		}		
		if (scriptname4!=null){
			answer= answer+"; "+scriptname4;
		}
		if (scriptname5!=null){
			answer= answer+"; "+scriptname5;
		}
		return answer;
		
	}
	public Status getStatus() {
		return Status.getStepStatus(this);
	}
	
	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());
		return returnlist;
	}
	public void addProperty(IGoobiProperty toAdd) {
		eigenschaften.add((Schritteigenschaft) toAdd);
	}
	
	
	public void removeProperty(IGoobiProperty toRemove) {
		getEigenschaften().remove(toRemove);
		toRemove.setOwningEntity(null);
		
	}
	
	/**
	 * 
	 * @return instance of {@link DisplayPropertyList}
	 */
	public DisplayPropertyList getDisplayProperties() {
		if (displayProperties == null) {
			displayProperties = new DisplayPropertyList(this);
		}
		return displayProperties;
	}
	
	public void refreshProperties() {
		displayProperties = null;
		getDisplayProperties();
		
	}
}
