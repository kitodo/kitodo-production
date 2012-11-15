package de.sub.goobi.persistence.apache;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2012, intranda GmbH, Göttingen
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
import java.util.Date;

public class ProjectObject {

	private int id;
	private String Titel;
	private boolean useDmsImport;
	private int dmsImportTimeOut;
	private String dmsImportRootPath;
	private String dmsImportImagesPath;
	private String dmsImportSuccessPath;
	private String dmsImportErrorPath;
	private boolean dmsImportCreateProcessFolder;
	private String fileFormatInternal;
	private String fileFormatDmsExport;
	private String metsRightsOwner;
	private String metsRightsOwnerLogo;
	private String metsRightsOwnerSite;
	private String metsDigiprovReference;
	private String metsDigiprovPresentation;
	private String metsPointerPath;
	private String metsPointerPathAnchor;
	private String metsDigiprovReferenceAnchor;
	private String metsDigiprovPresentationAnchor;
	private String metsPurl;
	private String metsContentIDs;
	private String metsRightsOwnerMail;
	private Date startDate;
	private Date endDate;
	private int numberOfPages;
	private int numberOfVolumes;
	private boolean projectIsArchived;

	public ProjectObject(int projekteID, String titel, boolean useDmsImport, int dmsImportTimeOut, String dmsImportRootPath,
			String dmsImportImagesPath, String dmsImportSuccessPath, String dmsImportErrorPath, boolean dmsImportCreateProcessFolder,
			String fileFormatInternal, String fileFormatDmsExport, String metsRightsOwner, String metsRightsOwnerLogo, String metsRightsOwnerSite,
			String metsDigiprovReference, String metsDigiprovPresentation, String metsPointerPath, String metsPointerPathAnchor,
			String metsDigiprovReferenceAnchor, String metsDigiprovPresentationAnchor, String metsPurl, String metsContentIDs,
			String metsRightsOwnerMail, Date startDate, Date endDate, int numberOfPages, int numberOfVolumes, boolean projectIsArchived) {
		this.id = projekteID;
		this.Titel = titel;
		this.useDmsImport = useDmsImport;
		this.dmsImportTimeOut = dmsImportTimeOut;
		this.dmsImportRootPath = dmsImportRootPath;
		this.dmsImportImagesPath = dmsImportImagesPath;
		this.dmsImportSuccessPath = dmsImportSuccessPath;
		this.dmsImportErrorPath = dmsImportErrorPath;
		this.dmsImportCreateProcessFolder = dmsImportCreateProcessFolder;
		this.fileFormatInternal = fileFormatInternal;
		this.fileFormatDmsExport = fileFormatDmsExport;
		this.metsRightsOwner = metsRightsOwner;
		this.metsRightsOwnerLogo = metsRightsOwnerLogo;
		this.metsRightsOwnerSite = metsRightsOwnerSite;
		this.metsDigiprovReference = metsDigiprovReference;
		this.metsDigiprovPresentation = metsDigiprovPresentation;
		this.metsPointerPath = metsPointerPath;
		this.metsPointerPathAnchor = metsPointerPathAnchor;
		this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
		this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
		this.metsPurl = metsPurl;
		this.metsContentIDs = metsContentIDs;
		this.metsRightsOwnerMail = metsRightsOwnerMail;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numberOfPages = numberOfPages;
		this.numberOfVolumes = numberOfVolumes;
		this.projectIsArchived = projectIsArchived;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int projekteID) {
		this.id = projekteID;
	}

	public String getTitel() {
		return this.Titel;
	}

	public void setTitel(String titel) {
		this.Titel = titel;
	}

	public boolean isUseDmsImport() {
		return this.useDmsImport;
	}

	public void setUseDmsImport(boolean useDmsImport) {
		this.useDmsImport = useDmsImport;
	}

	public int getDmsImportTimeOut() {
		return this.dmsImportTimeOut;
	}

	public void setDmsImportTimeOut(int dmsImportTimeOut) {
		this.dmsImportTimeOut = dmsImportTimeOut;
	}

	public String getDmsImportRootPath() {
		return this.dmsImportRootPath;
	}

	public void setDmsImportRootPath(String dmsImportRootPath) {
		this.dmsImportRootPath = dmsImportRootPath;
	}

	public String getDmsImportImagesPath() {
		return this.dmsImportImagesPath;
	}

	public void setDmsImportImagesPath(String dmsImportImagesPath) {
		this.dmsImportImagesPath = dmsImportImagesPath;
	}

	public String getDmsImportSuccessPath() {
		return this.dmsImportSuccessPath;
	}

	public void setDmsImportSuccessPath(String dmsImportSuccessPath) {
		this.dmsImportSuccessPath = dmsImportSuccessPath;
	}

	public String getDmsImportErrorPath() {
		return this.dmsImportErrorPath;
	}

	public void setDmsImportErrorPath(String dmsImportErrorPath) {
		this.dmsImportErrorPath = dmsImportErrorPath;
	}

	public boolean isDmsImportCreateProcessFolder() {
		return this.dmsImportCreateProcessFolder;
	}

	public void setDmsImportCreateProcessFolder(boolean dmsImportCreateProcessFolder) {
		this.dmsImportCreateProcessFolder = dmsImportCreateProcessFolder;
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

	public String getMetsDigiprovReference() {
		return this.metsDigiprovReference;
	}

	public void setMetsDigiprovReference(String metsDigiprovReference) {
		this.metsDigiprovReference = metsDigiprovReference;
	}

	public String getMetsDigiprovPresentation() {
		return this.metsDigiprovPresentation;
	}

	public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
		this.metsDigiprovPresentation = metsDigiprovPresentation;
	}

	public String getMetsPointerPath() {
		return this.metsPointerPath;
	}

	public void setMetsPointerPath(String metsPointerPath) {
		this.metsPointerPath = metsPointerPath;
	}

	public String getMetsPointerPathAnchor() {
		return this.metsPointerPathAnchor;
	}

	public void setMetsPointerPathAnchor(String metsPointerPathAnchor) {
		this.metsPointerPathAnchor = metsPointerPathAnchor;
	}

	public String getMetsDigiprovReferenceAnchor() {
		return this.metsDigiprovReferenceAnchor;
	}

	public void setMetsDigiprovReferenceAnchor(String metsDigiprovReferenceAnchor) {
		this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
	}

	public String getMetsDigiprovPresentationAnchor() {
		return this.metsDigiprovPresentationAnchor;
	}

	public void setMetsDigiprovPresentationAnchor(String metsDigiprovPresentationAnchor) {
		this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
	}

	public String getMetsPurl() {
		return this.metsPurl;
	}

	public void setMetsPurl(String metsPurl) {
		this.metsPurl = metsPurl;
	}

	public String getMetsContentIDs() {
		return this.metsContentIDs;
	}

	public void setMetsContentIDs(String metsContentIDs) {
		this.metsContentIDs = metsContentIDs;
	}

	public String getMetsRightsOwnerMail() {
		return this.metsRightsOwnerMail;
	}

	public void setMetsRightsOwnerMail(String metsRightsOwnerMail) {
		this.metsRightsOwnerMail = metsRightsOwnerMail;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getNumberOfPages() {
		return this.numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public int getNumberOfVolumes() {
		return this.numberOfVolumes;
	}

	public void setNumberOfVolumes(int numberOfVolumes) {
		this.numberOfVolumes = numberOfVolumes;
	}

	public boolean isProjectIsArchived() {
		return this.projectIsArchived;
	}

	public void setProjectIsArchived(boolean projectIsArchived) {
		this.projectIsArchived = projectIsArchived;
	}
}
