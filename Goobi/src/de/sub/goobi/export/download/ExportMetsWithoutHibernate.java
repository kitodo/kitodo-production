package de.sub.goobi.export.download;

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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsModsImportExport;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacerWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.apache.FolderInformation;
import de.sub.goobi.persistence.apache.ProcessManager;
import de.sub.goobi.persistence.apache.ProcessObject;
import de.sub.goobi.persistence.apache.ProjectManager;
import de.sub.goobi.persistence.apache.ProjectObject;

public class ExportMetsWithoutHibernate {
	protected Helper help = new Helper();
	protected Prefs myPrefs;
	private FolderInformation fi;
	private ProjectObject project;

	protected static final Logger myLogger = Logger.getLogger(ExportMetsWithoutHibernate.class);

	/**
	 * DMS-Export in das Benutzer-Homeverzeichnis
	 * 
	 * @param process
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws DAOException
	 * @throws SwapException
	 * @throws ReadException
	 * @throws UghHelperException
	 * @throws ExportFileException
	 * @throws MetadataTypeNotAllowedException
	 * @throws WriteException
	 * @throws PreferencesException
	 * @throws DocStructHasNoTypeException
	 * @throws TypeNotAllowedForParentException
	 */
	public boolean startExport(ProcessObject process) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
			WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
			TypeNotAllowedForParentException {
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		String benutzerHome = "";
		if (login != null) {
			benutzerHome = login.getMyBenutzer().getHomeDir();
		}
		return startExport(process, benutzerHome);
	}

	/**
	 * DMS-Export an eine gewünschte Stelle
	 * 
	 * @param process
	 * @param zielVerzeichnis
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws PreferencesException
	 * @throws WriteException
	 * @throws UghHelperException
	 * @throws ExportFileException
	 * @throws MetadataTypeNotAllowedException
	 * @throws DocStructHasNoTypeException
	 * @throws DAOException
	 * @throws SwapException
	 * @throws ReadException
	 * @throws TypeNotAllowedForParentException
	 */
	public boolean startExport(ProcessObject process, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
			WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
			SwapException, DAOException, TypeNotAllowedForParentException {

		/*
		 * -------------------------------- Read Document --------------------------------
		 */
		this.myPrefs = ProcessManager.getRuleset(process.getRulesetId()).getPreferences();

		this.project = ProjectManager.getProjectById(process.getProjekteID());
		String atsPpnBand = process.getTitle();
		this.fi = new FolderInformation(process.getId(), process.getTitle());
		Fileformat gdzfile = process.readMetadataFile(this.fi.getMetadataFilePath(), this.myPrefs);

		String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);

		String targetFileName = zielVerzeichnis + atsPpnBand + "_mets.xml";
		return writeMetsFile(process, targetFileName, gdzfile, false);

	}

	/**
	 * prepare user directory
	 * 
	 * @param inTargetFolder
	 *            the folder to prove and maybe create it
	 */
	protected String prepareUserDirectory(String inTargetFolder) {
		String target = inTargetFolder;
		Benutzer myBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		try {
            	FilesystemHelper.createDirectoryForUser(target, myBenutzer.getLogin());
            } catch (Exception e) {
			Helper.setFehlerMeldung("Export canceled, could not create destination directory: " + inTargetFolder, e);
		}
		return target;
	}

	/**
	 * write MetsFile to given Path
	 * 
	 * @param process
	 *            the Process to use
	 * @param targetFileName
	 *            the filename where the metsfile should be written
	 * @param gdzfile
	 *            the FileFormat-Object to use for Mets-Writing
	 * @throws DAOException
	 * @throws SwapException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TypeNotAllowedForParentException
	 */
	@SuppressWarnings("deprecation")
	protected boolean writeMetsFile(ProcessObject process, String targetFileName, Fileformat gdzfile, boolean writeLocalFilegroup)
			throws PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException,
			TypeNotAllowedForParentException {
		this.fi = new FolderInformation(process.getId(), process.getTitle());
		this.myPrefs = ProcessManager.getRuleset(process.getRulesetId()).getPreferences();
		this.project = ProjectManager.getProjectById(process.getProjekteID());
		MetsModsImportExport mm = new MetsModsImportExport(this.myPrefs);
		mm.setWriteLocal(writeLocalFilegroup);
		String imageFolderPath = this.fi.getImagesDirectory();
		File imageFolder = new File(imageFolderPath);
		/*
		 * before creating mets file, change relative path to absolute -
		 */
		DigitalDocument dd = gdzfile.getDigitalDocument();
		if (dd.getFileSet() == null) {
			Helper.setFehlerMeldung(process.getTitle() + ": digital document does not contain images; aborting");
			return false;
		}

		/*
		 * get the topstruct element of the digital document depending on anchor property
		 */
		DocStruct topElement = dd.getLogicalDocStruct();
		if (this.myPrefs.getDocStrctTypeByName(topElement.getType().getName()).getAnchorClass() != null) {
			if (topElement.getAllChildren() == null || topElement.getAllChildren().size() == 0) {
				throw new PreferencesException(process.getTitle()
						+ ": the topstruct element is marked as anchor, but does not have any children for physical docstrucs");
			} else {
				topElement = topElement.getAllChildren().get(0);
			}
		}

		/*
		 * -------------------------------- if the top element does not have any image related, set them all --------------------------------
		 */
		if (topElement.getAllToReferences("logical_physical") == null || topElement.getAllToReferences("logical_physical").size() == 0) {
			if (dd.getPhysicalDocStruct() != null && dd.getPhysicalDocStruct().getAllChildren() != null) {
				Helper.setMeldung(process.getTitle()
						+ ": topstruct element does not have any referenced images yet; temporarily adding them for mets file creation");
				for (DocStruct mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
					topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
				}
			} else {
				Helper.setFehlerMeldung(process.getTitle() + ": could not find any referenced images, export aborted");
				dd = null;
				return false;
			}
		}

		if (dd != null) {
			for (ContentFile cf : dd.getFileSet().getAllFiles()) {
				String location = cf.getLocation();
				// If the file's location string shoes no sign of any protocol,
				// use the file protocol.
				if (!location.contains("://")) {
					location = "file://" + location;
				}
				URL url = new URL(location);
				File f = new File(imageFolder, url.getFile());
				cf.setLocation(f.toURI().toString());
			}

			mm.setDigitalDocument(dd);

			/*
			 * -------------------------------- wenn Filegroups definiert wurden, werden diese jetzt in die Metsstruktur übernommen
			 * --------------------------------
			 */
			// Replace all paths with the given VariableReplacer, also the file
			// group paths!
			VariableReplacerWithoutHibernate vp = new VariableReplacerWithoutHibernate(mm.getDigitalDocument(), this.myPrefs, process, null);
			List<ProjectFileGroup> myFilegroups = ProjectManager.getFilegroupsForProjectId(this.project.getId());

			if (myFilegroups != null && myFilegroups.size() > 0) {
				for (ProjectFileGroup pfg : myFilegroups) {
					// check if source files exists
					if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
						File folder = new File(this.fi.getMethodFromName(pfg.getFolder()));
						if (folder != null && folder.exists() && folder.list().length > 0) {
							VirtualFileGroup v = new VirtualFileGroup();
							v.setName(pfg.getName());
							v.setPathToFiles(vp.replace(pfg.getPath()));
							v.setMimetype(pfg.getMimetype());
							v.setFileSuffix(pfg.getSuffix());
							mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
						}
					} else {

						VirtualFileGroup v = new VirtualFileGroup();
						v.setName(pfg.getName());
						v.setPathToFiles(vp.replace(pfg.getPath()));
						v.setMimetype(pfg.getMimetype());
						v.setFileSuffix(pfg.getSuffix());
						mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
					}
				}
			}

			// Replace rights and digiprov entries.
			mm.setRightsOwner(vp.replace(this.project.getMetsRightsOwner()));
			mm.setRightsOwnerLogo(vp.replace(this.project.getMetsRightsOwnerLogo()));
			mm.setRightsOwnerSiteURL(vp.replace(this.project.getMetsRightsOwnerSite()));
			mm.setRightsOwnerContact(vp.replace(this.project.getMetsRightsOwnerMail()));
			mm.setDigiprovPresentation(vp.replace(this.project.getMetsDigiprovPresentation()));
			mm.setDigiprovReference(vp.replace(this.project.getMetsDigiprovReference()));
			mm.setDigiprovPresentationAnchor(vp.replace(this.project.getMetsDigiprovPresentationAnchor()));
			mm.setDigiprovReferenceAnchor(vp.replace(this.project.getMetsDigiprovReferenceAnchor()));

			mm.setPurlUrl(vp.replace(this.project.getMetsPurl()));
			mm.setContentIDs(vp.replace(this.project.getMetsContentIDs()));

			// Set mets pointers. MetsPointerPathAnchor or mptrAnchorUrl is the
			// pointer used to point to the superordinate (anchor) file, that is
			// representing a “virtual” group such as a series. Several anchors
			// pointer paths can be defined/ since it is possible to define several
			// levels of superordinate structures (such as the complete edition of
			// a daily newspaper, one year ouf of that edition, …)
			String anchorPointersToReplace = this.project.getMetsPointerPath();
			mm.setMptrUrl(null);
			for (String anchorPointerToReplace : anchorPointersToReplace.split(Projekt.ANCHOR_SEPARATOR)) {
				String anchorPointer = vp.replace(anchorPointerToReplace);
				mm.setMptrUrl(anchorPointer);
			}

			// metsPointerPathAnchor or mptrAnchorUrl is the pointer used to point
			// from the (lowest) superordinate (anchor) file to the lowest level
			// file (the non-anchor file). 
			String anchor = this.project.getMetsPointerPathAnchor();
			String pointer = vp.replace(anchor);
			mm.setMptrAnchorUrl(pointer);

			List<String> images = new ArrayList<String>();
			try {
				// TODO andere Dateigruppen nicht mit image Namen ersetzen
				images = this.fi.getDataFiles();
				if (images != null) {
					int sizeOfPagination = dd.getPhysicalDocStruct().getAllChildren().size();
					int sizeOfImages = images.size();
					if (sizeOfPagination == sizeOfImages) {
						dd.overrideContentFiles(images);
					} else {
						List<String> param = new ArrayList<String>();
						param.add(String.valueOf(sizeOfPagination));
						param.add(String.valueOf(sizeOfImages));
						Helper.setFehlerMeldung(Helper.getTranslation("imagePaginationError", param));
						return false;
					}
				} 
			} catch (IndexOutOfBoundsException e) {

				myLogger.error(e);
			} catch (InvalidImagesException e) {
				myLogger.error(e);
			}
			mm.write(targetFileName);
			Helper.setMeldung(null, process.getTitle() + ": ", "ExportFinished");
		}
		return true;
	}
}
