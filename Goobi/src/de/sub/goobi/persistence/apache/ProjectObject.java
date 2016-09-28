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
