package de.sub.goobi.Beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.sub.goobi.helper.enums.MetadataFormat;

//TODO Serialize this as XML to be able to copy the settings.

public class Projekt implements Serializable {
	private static final long serialVersionUID = -8543713331407761617L;
	private Integer id;
	private String titel;
	private Set<Benutzer> benutzer;
	private Set<Prozess> prozesse;
	private Set<ProjectFileGroup> filegroups;

	private boolean useDmsImport = false;
	private Integer dmsImportTimeOut = 20000;
	private String dmsImportRootPath;
	private String dmsImportImagesPath;
	private String dmsImportSuccessPath;
	private String dmsImportErrorPath;
	private Boolean dmsImportCreateProcessFolder = false;

	private String fileFormatInternal;
	private String fileFormatDmsExport;

	private String metsRightsOwner = "";
	private String metsRightsOwnerLogo = "";
	private String metsRightsOwnerSite = "";
	private String metsRightsOwnerMail = "";
	private String metsDigiprovReference = "";
	private String metsDigiprovPresentation = "";
	private String metsDigiprovReferenceAnchor = "";
	private String metsDigiprovPresentationAnchor = "";
	private String metsPointerPath = "";
	private String metsPointerPathAnchor = "";
	private String metsPurl = "";
	private String metsContentIDs = "";

	public Projekt() {
		prozesse = new HashSet<Prozess>();
		benutzer = new HashSet<Benutzer>();
		useDmsImport = false;
		dmsImportTimeOut = 0;
		dmsImportImagesPath = "";
		dmsImportRootPath = "";
		dmsImportSuccessPath = "";
		dmsImportCreateProcessFolder = false;
		fileFormatInternal = MetadataFormat.getDefaultFileFormat().getName();
		fileFormatDmsExport = MetadataFormat.getDefaultFileFormat()
				.getName();
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Getter und
	 * Setter ## #####################################################
	 * ####################################################
	 */

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Set<Benutzer> getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public Set<Prozess> getProzesse() {
		return prozesse;
	}

	public void setProzesse(Set<Prozess> prozesse) {
		this.prozesse = prozesse;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getDmsImportImagesPath() {
		return dmsImportImagesPath;
	}

	public void setDmsImportImagesPath(String dmsImportImagesPath) {
		this.dmsImportImagesPath = dmsImportImagesPath;
	}

	public String getDmsImportRootPath() {
		return dmsImportRootPath;
	}

	public void setDmsImportRootPath(String dmsImportRootPath) {
		this.dmsImportRootPath = dmsImportRootPath;
	}

	public String getDmsImportSuccessPath() {
		return dmsImportSuccessPath;
	}

	public void setDmsImportSuccessPath(String dmsImportSuccessPath) {
		this.dmsImportSuccessPath = dmsImportSuccessPath;
	}

	public Integer getDmsImportTimeOut() {
		return dmsImportTimeOut;
	}

	public void setDmsImportTimeOut(Integer dmsImportTimeOut) {
		this.dmsImportTimeOut = dmsImportTimeOut;
	}

	public boolean isUseDmsImport() {
		return useDmsImport;
	}

	public void setUseDmsImport(boolean useDmsImport) {
		this.useDmsImport = useDmsImport;
	}

	public String getDmsImportErrorPath() {
		return dmsImportErrorPath;
	}

	public void setDmsImportErrorPath(String dmsImportErrorPath) {
		this.dmsImportErrorPath = dmsImportErrorPath;
	}

	/**
	 * here differet Getters and Setters for the same value, because Hibernate
	 * does not like bit-Fields with null Values (thats why Boolean) and MyFaces
	 * seams not to like Boolean (thats why boolean for the GUI)
	 * ================================================================
	 */
	public boolean isDmsImportCreateProcessFolder() {
		if (dmsImportCreateProcessFolder == null)
			dmsImportCreateProcessFolder = false;
		return dmsImportCreateProcessFolder;
	}

	public void setDmsImportCreateProcessFolder(boolean inFolder) {
		dmsImportCreateProcessFolder = inFolder;
	}

	public Boolean isDmsImportCreateProcessFolderHibernate() {
		return dmsImportCreateProcessFolder;
	}

	public void setDmsImportCreateProcessFolderHibernate(Boolean inFolder) {
		dmsImportCreateProcessFolder = inFolder;
	}

	public boolean isDeleteAble() {
		return prozesse.size() == 0;
	}

	public Set<ProjectFileGroup> getFilegroups() {
		return filegroups;
	}

	public void setFilegroups(Set<ProjectFileGroup> filegroups) {
		this.filegroups = filegroups;
	}

	public ArrayList<ProjectFileGroup> getFilegroupsList() {
		if (filegroups == null)
			filegroups = new HashSet<ProjectFileGroup>();
		return new ArrayList<ProjectFileGroup>(filegroups);
	}

	public String getMetsRightsOwner() {
		return metsRightsOwner;
	}

	public void setMetsRightsOwner(String metsRightsOwner) {
		this.metsRightsOwner = metsRightsOwner;
	}

	public String getMetsRightsOwnerLogo() {
		return metsRightsOwnerLogo;
	}

	public void setMetsRightsOwnerLogo(String metsRightsOwnerLogo) {
		this.metsRightsOwnerLogo = metsRightsOwnerLogo;
	}

	public String getMetsRightsOwnerSite() {
		return metsRightsOwnerSite;
	}

	public void setMetsRightsOwnerSite(String metsRightsOwnerSite) {
		this.metsRightsOwnerSite = metsRightsOwnerSite;
	}
	/**
	 * @return the metsRigthsOwnerMail
	 */
	public String getMetsRightsOwnerMail() {
		return metsRightsOwnerMail;
	}

	/**
	 * @param metsRigthsOwnerMail the metsRigthsOwnerMail to set
	 */
	public void setMetsRightsOwnerMail(String metsRigthsOwnerMail) {
		this.metsRightsOwnerMail = metsRigthsOwnerMail;
	}
	
	public String getMetsDigiprovReference() {
		return metsDigiprovReference;
	}

	public void setMetsDigiprovReference(String metsDigiprovReference) {
		this.metsDigiprovReference = metsDigiprovReference;
	}

	public String getMetsDigiprovReferenceAnchor() {
		return metsDigiprovReferenceAnchor;
	}

	public void setMetsDigiprovReferenceAnchor(String metsDigiprovReferenceAnchor) {
		this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
	}

	public String getMetsDigiprovPresentation() {
		return metsDigiprovPresentation;
	}

	public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
		this.metsDigiprovPresentation = metsDigiprovPresentation;
	}

	public String getMetsDigiprovPresentationAnchor() {
		return metsDigiprovPresentationAnchor;
	}

	public void setMetsDigiprovPresentationAnchor(String metsDigiprovPresentationAnchor) {
		this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
	}

	public String getMetsPointerPath() {
		return this.metsPointerPath ;
	}

	public void setMetsPointerPath(String metsPointerPath) {
		this.metsPointerPath = metsPointerPath;
	}

	public void setMetsPointerPathAnchor(String metsPointerPathAnchor) {
		this.metsPointerPathAnchor = metsPointerPathAnchor;
	}
	
	public String getMetsPointerPathAnchor() {
		return metsPointerPathAnchor;
	}

	public void setMetsPurl(String metsPurl) {
		this.metsPurl = metsPurl;
	}
	
	public String getMetsPurl() {
		return metsPurl;
	}

	public void setMetsContentIDs(String contentIDs) {
		this.metsContentIDs = contentIDs;
	}
	
	public String getMetsContentIDs() {
		return metsContentIDs;
	}

	public String getFileFormatInternal() {
		return fileFormatInternal;
	}

	public void setFileFormatInternal(String fileFormatInternal) {
		this.fileFormatInternal = fileFormatInternal;
	}

	public String getFileFormatDmsExport() {
		return fileFormatDmsExport;
	}

	public void setFileFormatDmsExport(String fileFormatDmsExport) {
		this.fileFormatDmsExport = fileFormatDmsExport;
	}

}
