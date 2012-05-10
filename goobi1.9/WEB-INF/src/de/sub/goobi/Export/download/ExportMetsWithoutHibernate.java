package de.sub.goobi.Export.download;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
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
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.ProjectFileGroup;
import de.sub.goobi.Forms.LoginForm;
import de.sub.goobi.Persistence.apache.FolderInformation;
import de.sub.goobi.Persistence.apache.ProcessManager;
import de.sub.goobi.Persistence.apache.ProcessObject;
import de.sub.goobi.Persistence.apache.ProjectManager;
import de.sub.goobi.Persistence.apache.ProjectObject;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacerWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

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
	public void startExport(ProcessObject process) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
			WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
			TypeNotAllowedForParentException {
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		String benutzerHome = "";
		if (login != null) {
			benutzerHome = login.getMyBenutzer().getHomeDir();
		}
		startExport(process, benutzerHome);
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
	public void startExport(ProcessObject process, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
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
		writeMetsFile(process, targetFileName, gdzfile, false);
		Helper.setMeldung(null, process.getTitle() + ": ", "Export finished");
	}

	/**
	 * prepare user directory
	 * 
	 * @param inTargetFolder
	 *            the folder to proove and maybe create it
	 */
	protected String prepareUserDirectory(String inTargetFolder) {
		String target = inTargetFolder;
		Benutzer myBenutzer = (Benutzer) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
		try {
			this.help.createUserDirectory(target, myBenutzer.getLogin());
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
	protected void writeMetsFile(ProcessObject process, String targetFileName, Fileformat gdzfile, boolean writeLocalFilegroup)
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
			return;
		}

		/*
		 * get the topstruct element of the digital document depending on anchor property
		 */
		DocStruct topElement = dd.getLogicalDocStruct();
		if (this.myPrefs.getDocStrctTypeByName(topElement.getType().getName()).isAnchor()) {
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
				Helper.setMeldung(process.getTitle() + ": could not found any referenced images, export aborted");
				dd = null;
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
			// Replace all pathes with the given VariableReplacer, also the file
			// group pathes!
			VariableReplacerWithoutHibernate vp = new VariableReplacerWithoutHibernate(mm.getDigitalDocument(), this.myPrefs, process, null);
			List<ProjectFileGroup> myFilegroups = ProjectManager.getFilegroupsForProjectId(this.project.getId());

			if (myFilegroups != null && myFilegroups.size() > 0) {
				for (ProjectFileGroup pfg : myFilegroups) {
					// check if source files exists
					if (pfg.getFolder() != null && pfg.getFolder().length() > 0) {
						File folder = new File(this.fi.getMethodFromName(pfg.getFolder()));
						if (folder.exists() && folder.list().length > 0) {
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

			String pointer = this.project.getMetsPointerPath();
			pointer = vp.replace(pointer);
			mm.setMptrUrl(pointer);

			String anchor = this.project.getMetsPointerPathAnchor();
			pointer = vp.replace(anchor);

			// if (!ConfigMain.getParameter("ImagePrefix", "\\d{8}").equals("\\d{8}")) {
			List<String> images = new ArrayList<String>();
			try {
				// TODO andere Dateigruppen nicht mit image Namen ersetzen
				images = this.fi.getDataFiles();
				dd.overrideContentFiles(images);
			} catch (IndexOutOfBoundsException e) {
				myLogger.error(e);
			} catch (InvalidImagesException e) {
				myLogger.error(e);
			}
			mm.write(targetFileName);
		}
	}
}
