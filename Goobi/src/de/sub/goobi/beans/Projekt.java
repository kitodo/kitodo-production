package de.sub.goobi.beans;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.goobi.org
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.webapi.beans.Field;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import de.sub.goobi.helper.ProjectHelper;
import de.sub.goobi.helper.enums.MetadataFormat;

@XmlAccessorType(XmlAccessType.NONE)
// This annotation is to instruct the Jersey API not to generate arbitrary XML
// elements. Further XML elements can be added as needed by annotating with
// @XmlElement, but their respective names should be wisely chosen according to
// the Coding Guidelines (e.g. *english* names).
@XmlType(propOrder = { "template", "fieldConfig" })
// This annotation declares the desired order of XML elements generated and
// rather serves for better legibility of the generated XML. The list must be
// exhaustive and the properties have to be named according to their respective
// getter function, e.g. @XmlElement(name="field") getFieldConfig() must be
// referenced as "fieldConfig" here, not "field" as one might expect.
public class Projekt implements Serializable, Comparable<Projekt> {
	private static final long serialVersionUID = -8543713331407761617L;

	/**
	 * The constant ANCHOR_SEPARATOR holds the character U+00A6
	 * (&ldquo;&brvbar;&rdquo;) which can be used to separate multiple anchors,
	 * if several of them are needed in one project. The anchors must then be
	 * listed the hierarchical order they have to be applied, that is the
	 * topmost anchor in first place, followed by the second one and so on.
	 */
	public static final String ANCHOR_SEPARATOR = "\u00A6";
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
	private Boolean projectIsArchived = false;

	@XmlElement(name = "template")
	public List<Prozess> template; // The ‘template’ variable is populated from org.goobi.webapi.resources.Projects when calling ${SERVLET_CONTEXT}/rest/projects to output the templates available within a project as XML child nodes of the respective project.

	public Projekt() {
		this.prozesse = new HashSet<Prozess>();
		this.benutzer = new HashSet<Benutzer>();
		this.useDmsImport = false;
		this.dmsImportTimeOut = 0;
		this.dmsImportImagesPath = "";
		this.dmsImportRootPath = "";
		this.dmsImportSuccessPath = "";
		this.dmsImportCreateProcessFolder = false;
		this.fileFormatInternal = MetadataFormat.getDefaultFileFormat().getName();
		this.fileFormatDmsExport = MetadataFormat.getDefaultFileFormat().getName();
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Getter und
	 * Setter ## #####################################################
	 * ####################################################
	 */

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Set<Benutzer> getBenutzer() {
		return this.benutzer;
	}

	public void setBenutzer(Set<Benutzer> benutzer) {
		this.benutzer = benutzer;
	}

	public Set<Prozess> getProzesse() {
		return this.prozesse;
	}

	public void setProzesse(Set<Prozess> prozesse) {
		this.prozesse = prozesse;
	}

	@XmlAttribute(name="key")
	public String getTitel() {
		return this.titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getDmsImportImagesPath() {
		return this.dmsImportImagesPath;
	}

	public void setDmsImportImagesPath(String dmsImportImagesPath) {
		this.dmsImportImagesPath = dmsImportImagesPath;
	}

	public String getDmsImportRootPath() {
		return this.dmsImportRootPath;
	}

	public void setDmsImportRootPath(String dmsImportRootPath) {
		this.dmsImportRootPath = dmsImportRootPath;
	}

	public String getDmsImportSuccessPath() {
		return this.dmsImportSuccessPath;
	}

	public void setDmsImportSuccessPath(String dmsImportSuccessPath) {
		this.dmsImportSuccessPath = dmsImportSuccessPath;
	}

	public Integer getDmsImportTimeOut() {
		return this.dmsImportTimeOut;
	}

	public void setDmsImportTimeOut(Integer dmsImportTimeOut) {
		this.dmsImportTimeOut = dmsImportTimeOut;
	}

	public boolean isUseDmsImport() {
		return this.useDmsImport;
	}

	public void setUseDmsImport(boolean useDmsImport) {
		this.useDmsImport = useDmsImport;
	}

	public String getDmsImportErrorPath() {
		return this.dmsImportErrorPath;
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
		if (this.dmsImportCreateProcessFolder == null) {
			this.dmsImportCreateProcessFolder = false;
		}
		return this.dmsImportCreateProcessFolder;
	}

	public void setDmsImportCreateProcessFolder(boolean inFolder) {
		this.dmsImportCreateProcessFolder = inFolder;
	}

	public Boolean isDmsImportCreateProcessFolderHibernate() {
		return this.dmsImportCreateProcessFolder;
	}

	public void setDmsImportCreateProcessFolderHibernate(Boolean inFolder) {
		this.dmsImportCreateProcessFolder = inFolder;
	}

	public boolean isDeleteAble() {
		return this.prozesse.size() == 0;
	}

	public Set<ProjectFileGroup> getFilegroups() {
		return this.filegroups;
	}

	public void setFilegroups(Set<ProjectFileGroup> filegroups) {
		this.filegroups = filegroups;
	}

	public ArrayList<ProjectFileGroup> getFilegroupsList() {
		try {
			Hibernate.initialize(this.filegroups);
		} catch (HibernateException e) {
		}		if (this.filegroups == null) {
			this.filegroups = new HashSet<ProjectFileGroup>();
		}
		return new ArrayList<ProjectFileGroup>(this.filegroups);
	}

	public String getMetsRightsOwner() {
		return this.metsRightsOwner;
	}

	public void setMetsRightsOwner(String metsRightsOwner) {
		this.metsRightsOwner = metsRightsOwner;
	}

	public String getMetsRightsOwnerLogo() {
		return this.metsRightsOwnerLogo;
	}

	public void setMetsRightsOwnerLogo(String metsRightsOwnerLogo) {
		this.metsRightsOwnerLogo = metsRightsOwnerLogo;
	}

	public String getMetsRightsOwnerSite() {
		return this.metsRightsOwnerSite;
	}

	public void setMetsRightsOwnerSite(String metsRightsOwnerSite) {
		this.metsRightsOwnerSite = metsRightsOwnerSite;
	}

	/**
	 * @return the metsRigthsOwnerMail
	 */
	public String getMetsRightsOwnerMail() {
		return this.metsRightsOwnerMail;
	}

	/**
	 * @param metsRigthsOwnerMail
	 *            the metsRigthsOwnerMail to set
	 */
	public void setMetsRightsOwnerMail(String metsRigthsOwnerMail) {
		this.metsRightsOwnerMail = metsRigthsOwnerMail;
	}

	public String getMetsDigiprovReference() {
		return this.metsDigiprovReference;
	}

	public void setMetsDigiprovReference(String metsDigiprovReference) {
		this.metsDigiprovReference = metsDigiprovReference;
	}

	public String getMetsDigiprovReferenceAnchor() {
		return this.metsDigiprovReferenceAnchor;
	}

	public void setMetsDigiprovReferenceAnchor(String metsDigiprovReferenceAnchor) {
		this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
	}

	public String getMetsDigiprovPresentation() {
		return this.metsDigiprovPresentation;
	}

	public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
		this.metsDigiprovPresentation = metsDigiprovPresentation;
	}

	public String getMetsDigiprovPresentationAnchor() {
		return this.metsDigiprovPresentationAnchor;
	}

	public void setMetsDigiprovPresentationAnchor(String metsDigiprovPresentationAnchor) {
		this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
	}

	public String getMetsPointerPath() {
		return this.metsPointerPath;
	}

	public void setMetsPointerPath(String metsPointerPath) {
		this.metsPointerPath = metsPointerPath;
	}

	public void setMetsPointerPathAnchor(String metsPointerPathAnchor) {
		this.metsPointerPathAnchor = metsPointerPathAnchor;
	}

	public String getMetsPointerPathAnchor() {
		return this.metsPointerPathAnchor;
	}

	public void setMetsPurl(String metsPurl) {
		this.metsPurl = metsPurl;
	}

	public String getMetsPurl() {
		return this.metsPurl;
	}

	public void setMetsContentIDs(String contentIDs) {
		this.metsContentIDs = contentIDs;
	}

	public String getMetsContentIDs() {
		return this.metsContentIDs;
	}

	public String getFileFormatInternal() {
		return this.fileFormatInternal;
	}

	public void setFileFormatInternal(String fileFormatInternal) {
		this.fileFormatInternal = fileFormatInternal;
	}

	public String getFileFormatDmsExport() {
		return this.fileFormatDmsExport;
	}

	public void setFileFormatDmsExport(String fileFormatDmsExport) {
		this.fileFormatDmsExport = fileFormatDmsExport;
	}

	/**
	 *
	 * @return a list with informations for each step on workflow
	 */

	public List<StepInformation> getWorkFlow() {
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
		if (this.numberOfVolumes == null) {
			this.numberOfVolumes = 0;
		}
		return this.numberOfVolumes;
	}

	/**
	 *
	 * @param numberOfVolumes
	 *            for this project
	 */

	public void setNumberOfVolumes(Integer numberOfVolumes) {
		this.numberOfVolumes = numberOfVolumes;
	}

	/*************************************************************************************
	 * Getter for numberOfPages
	 *
	 * @return the number of pages
	 *************************************************************************************/
	public Integer getNumberOfPages() {
		if (this.numberOfPages == null) {
			this.numberOfPages = 0;
		}
		return this.numberOfPages;
	}

	/**************************************************************************************
	 * Setter for numberOfPages
	 *
	 * @param numberOfPages
	 *            the number of pages to set
	 **************************************************************************************/
	public void setNumberOfPages(Integer numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	/*************************************************************************************
	 * Getter for startDate
	 *
	 * @return the start date
	 *************************************************************************************/
	public Date getStartDate() {
		if (this.startDate == null) {
			this.startDate = new Date();
		}
		return this.startDate;
	}

	/**************************************************************************************
	 * Setter for startDate
	 *
	 * @param startDate
	 *            the start date to set
	 **************************************************************************************/
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/*************************************************************************************
	 * Getter for endDate
	 *
	 * @return the end date
	 *************************************************************************************/
	public Date getEndDate() {
		if (this.endDate == null) {
			this.endDate = new Date();
		}
		return this.endDate;
	}

	/**************************************************************************************
	 * Setter for endDate
	 *
	 * @param endDate
	 *            the end date to set
	 **************************************************************************************/
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setProjectIsArchived(Boolean projectIsArchived) {
		if (projectIsArchived == null) {
			projectIsArchived = false;
		}
		this.projectIsArchived = projectIsArchived;
	}

	public Boolean getProjectIsArchived() {
		return this.projectIsArchived;
	}

	@Override
	public int compareTo(Projekt o) {
		return this.getTitel().compareTo(o.getTitel());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Projekt)) {
			return false;
		}
		Projekt other = (Projekt) obj;
		return this.titel == null ? other.titel == null : this.titel.equals(other.titel);
	}

	@Override
	public int hashCode() {
		return this.titel == null ? 0 : this.titel.hashCode();
	}

	@XmlElement(name = "field")
	public List<Field> getFieldConfig() throws IOException {
		return Field.getFieldConfigForProject(this);
	}
}
