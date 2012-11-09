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

package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.*;

import org.goobi.production.flow.statistics.StepInformation;

import de.sub.goobi.helper.ProjectHelper;
import de.sub.goobi.helper.enums.MetadataFormat;

@XmlAccessorType(XmlAccessType.NONE)
// This annotation is to instruct the Jersey API not to generate arbitrary XML
// elements. Further XML elements can be added as needed by annotating with
// @XmlElement, but their respective names should be wisely chosen according to
// the Coding Guidelines (e.g. *english* names).
@XmlType(propOrder = { "titel", "template" })
// This annotation declares the desired order of XML elements generated and
// rather serves for better legibility of the generated XML. The list must be
// exhaustive and the properties have to be named according to their respective
// getter function, e.g. @XmlElement(name="title") getTitel() must be referenced
// as "titel" here, not "title" as one might expect.
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

	private List<StepInformation> commonWorkFlow = null;
	private Date startDate;
	private Date endDate;
	private Integer numberOfPages;
	private Integer numberOfVolumes;
	
	@XmlElement(name = "template")
	public List<Prozess> template; // The ‘template’ variable is populated from
									// org.goobi.webapi.resources.Projects when
									// calling ${SERVLET_CONTEXT}/rest/projects
									// to output the templates available within
									// a project as XML child nodes of the
									// respective project.
	
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

	@XmlAttribute(name="recordNumber") // ‘id’ should be unique over all XML elements
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

	@XmlElement(name="title")
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

	/**
	 * 
	 * @return a list with informations for each step on workflow
	 */
	
	public List<StepInformation> getWorkFlow () {
		if (this.commonWorkFlow == null) {
			if (this.id != null) {
				this.commonWorkFlow = ProjectHelper.getProjectWorkFlowOverview(this);
			} else {
				this.commonWorkFlow = new ArrayList<StepInformation>();
			}
		}
		return this.commonWorkFlow;
	}
	
	/**
	 * 
	 * @return number of volumes for this project
	 */
	
	public Integer getNumberOfVolumes() {
		if (numberOfVolumes == null) {
			numberOfVolumes = 0;
		}
		return numberOfVolumes;
	}

	/**
	 * 
	 * @param numberOfVolumes for this project
	 */
	
	public void setNumberOfVolumes(Integer numberOfVolumes) {
		this.numberOfVolumes = numberOfVolumes;
	}


	/*************************************************************************************
	 * Getter for NumberOfPages
	 * 
	 * @return the NumberOfPages
	 *************************************************************************************/
	public Integer getNumberOfPages() {
		if (numberOfPages == null) {
			numberOfPages = 0;
		}
		return numberOfPages;
	}

	/**************************************************************************************
	 * Setter for NumberOfPages
	 * 
	 * @param NumberOfPages
	 *            the NumberOfPages to set
	 **************************************************************************************/
	public void setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
	}


	/*************************************************************************************
	 * Getter for StartDate
	 * 
	 * @return the StartDate
	 *************************************************************************************/
	public Date getStartDate() {
		if (startDate == null){
			startDate = new Date();
		}
		return startDate;
	}

	/**************************************************************************************
	 * Setter for StartDate
	 * 
	 * @param StartDate
	 *            the StartDate to set
	 **************************************************************************************/
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/*************************************************************************************
	 * Getter for EndDate
	 * 
	 * @return the EndDate
	 *************************************************************************************/
	public Date getEndDate() {
		if (endDate == null){
			endDate= new Date();
		}
		return endDate;
	}

	/**************************************************************************************
	 * Setter for EndDate
	 * 
	 * @param EndDate
	 *            the EndDate to set
	 **************************************************************************************/
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
