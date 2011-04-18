package de.sub.goobi.Export.download;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

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
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Export.dms.ExportDms_CorrectRusdml;
import de.sub.goobi.Forms.LoginForm;
import de.sub.goobi.Metadaten.MetadatenImagesHelper;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;

public class ExportMets {
	protected Helper help = new Helper();
	protected Prefs myPrefs;

	protected static final Logger myLogger = Logger.getLogger(ExportMets.class);

	/**
	 * DMS-Export in das Benutzer-Homeverzeichnis
	 * 
	 * @param myProzess
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
	public void startExport(Prozess myProzess) throws IOException, InterruptedException, DocStructHasNoTypeException, PreferencesException,
			WriteException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
			TypeNotAllowedForParentException {
		LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		String benutzerHome = login.getMyBenutzer().getHomeDir();
		startExport(myProzess, benutzerHome);
	}

	/**
	 * DMS-Export an eine gewünschte Stelle
	 * 
	 * @param myProzess
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
	public void startExport(Prozess myProzess, String inZielVerzeichnis) throws IOException, InterruptedException, PreferencesException,
			WriteException, DocStructHasNoTypeException, MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException,
			SwapException, DAOException, TypeNotAllowedForParentException {

		/*
		 * -------------------------------- Read Document --------------------------------
		 */
		myPrefs = myProzess.getRegelsatz().getPreferences();
		String atsPpnBand = myProzess.getTitel();
		Fileformat gdzfile = myProzess.readMetadataFile();

		/* nur beim Rusdml-Projekt die Metadaten aufbereiten */
		ConfigProjects cp = new ConfigProjects(myProzess.getProjekt());
		if (cp.getParamList("dmsImport.check").contains("rusdml")) {
			ExportDms_CorrectRusdml expcorr = new ExportDms_CorrectRusdml(myProzess, myPrefs, gdzfile);
			atsPpnBand = expcorr.correctionStart();
		}

		String zielVerzeichnis = prepareUserDirectory(inZielVerzeichnis);

		String targetFileName = zielVerzeichnis + atsPpnBand + "_mets.xml";
		writeMetsFile(myProzess, targetFileName, gdzfile);
		Helper.setMeldung(null, myProzess.getTitel() + ": ", "Export finished");
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
			help.createUserDirectory(target, myBenutzer.getLogin());
		} catch (Exception e) {
			Helper.setFehlerMeldung("Export canceled, could not create destination directory: " + inTargetFolder, e);
		}
		return target;
	}

	/**
	 * write MetsFile to given Path
	 * 
	 * @param myProzess
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
	protected void writeMetsFile(Prozess myProzess, String targetFileName, Fileformat gdzfile) throws PreferencesException, WriteException,
			IOException, InterruptedException, SwapException, DAOException, TypeNotAllowedForParentException {

		MetsModsImportExport mm = new MetsModsImportExport(myPrefs);
		String imageFolderPath = myProzess.getImagesDirectory();
		File imageFolder = new File(imageFolderPath);
		/*
		 * before creating mets file, change relative path to absolute -
		 */
		DigitalDocument dd = gdzfile.getDigitalDocument();
		if (dd.getFileSet() == null) {
			Helper.setMeldung(myProzess.getTitel() + ": digital document does not contain images; temporarily adding them for mets file creation");

			MetadatenImagesHelper mih = new MetadatenImagesHelper(myPrefs, dd);
			mih.createPagination(myProzess);
		}

		/*
		 * get the topstruct element of the digital document depending on anchor property
		 */
		DocStruct topElement = dd.getLogicalDocStruct();
		if (myPrefs.getDocStrctTypeByName(topElement.getType().getName()).isAnchor()) {
			if (topElement.getAllChildren() == null || topElement.getAllChildren().size() == 0) {
				throw new PreferencesException(myProzess.getTitel()
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
				Helper.setMeldung(myProzess.getTitel()
						+ ": topstruct element does not have any referenced images yet; temporarily adding them for mets file creation");
				for (DocStruct mySeitenDocStruct : dd.getPhysicalDocStruct().getAllChildren()) {
					topElement.addReferenceTo(mySeitenDocStruct, "logical_physical");
				}
			} else {
				Helper.setMeldung(myProzess.getTitel() + ": could not found any referenced images, export aborted");
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
			VariableReplacer vp = new VariableReplacer(mm.getDigitalDocument(), myPrefs, myProzess, null);
			Set<ProjectFileGroup> myFilegroups = myProzess.getProjekt().getFilegroups();
			if (myFilegroups != null && myFilegroups.size() > 0) {
				for (ProjectFileGroup pfg : myFilegroups) {
					VirtualFileGroup v = new VirtualFileGroup();
					v.setName(pfg.getName());
					v.setPathToFiles(vp.replace(pfg.getPath()));
					v.setMimetype(pfg.getMimetype());
					v.setFileSuffix(pfg.getSuffix());
					mm.getDigitalDocument().getFileSet().addVirtualFileGroup(v);
				}
			}

			// Replace rights and digiprov entries.
			mm.setRightsOwner(vp.replace(myProzess.getProjekt().getMetsRightsOwner()));
			mm.setRightsOwnerLogo(vp.replace(myProzess.getProjekt().getMetsRightsOwnerLogo()));
			mm.setRightsOwnerSiteURL(vp.replace(myProzess.getProjekt().getMetsRightsOwnerSite()));
			mm.setRightsOwnerContact(vp.replace(myProzess.getProjekt().getMetsRightsOwnerMail()));
			mm.setDigiprovPresentation(vp.replace(myProzess.getProjekt().getMetsDigiprovPresentation()));
			mm.setDigiprovReference(vp.replace(myProzess.getProjekt().getMetsDigiprovReference()));
			mm.setDigiprovPresentationAnchor(vp.replace(myProzess.getProjekt().getMetsDigiprovPresentationAnchor()));
			mm.setDigiprovReferenceAnchor(vp.replace(myProzess.getProjekt().getMetsDigiprovReferenceAnchor()));

			mm.setPurlUrl(vp.replace(myProzess.getProjekt().getMetsPurl()));
			mm.setContentIDs(vp.replace(myProzess.getProjekt().getMetsContentIDs()));

			String pointer = myProzess.getProjekt().getMetsPointerPath();
			pointer = vp.replace(pointer);
			mm.setMptrUrl(pointer);

			String anchor = myProzess.getProjekt().getMetsPointerPathAnchor();
			pointer = vp.replace(anchor);
			mm.setMptrAnchorUrl(pointer);

			// if (!ConfigMain.getParameter("ImagePrefix", "\\d{8}").equals("\\d{8}")) {
			ArrayList<String> images = new ArrayList<String>();
			try {
				images = new MetadatenImagesHelper(myPrefs, dd).getImageFiles(myProzess);
				dd.overrideContentFiles(images);
			} catch (IndexOutOfBoundsException e) {
				myLogger.error(e);				
			} catch (InvalidImagesException e) {
				myLogger.error(e);
			}
			// }
			mm.write(targetFileName);
		}
	}
}
