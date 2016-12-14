/***************************************************************
 * Copyright notice
 *
 * (c) 2013 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

package de.sub.goobi.metadaten;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.goobi.io.SafeFile;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import ugh.dl.ContentFile;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedForParentException;

public class FileManipulation {
    private static final Logger logger = Logger.getLogger(FileManipulation.class);
    private Metadaten metadataBean;

    public FileManipulation(Metadaten metadataBean) {
        this.metadataBean = metadataBean;
    }

    // insert new file after this page
    private String insertPage = "";

    private String imageSelection = "";

    // mode of insert (uncounted or into pagination sequence)
    private String insertMode = "uncounted";

    private UploadedFile uploadedFile = null;

    private String uploadedFileName = null;

    private List<String> selectedFiles = new ArrayList<String>();

    private boolean deleteFilesAfterMove = false;

    private boolean moveFilesInAllFolder = true;

    private List<String> allImportFolder = new ArrayList<String>();

    private String currentFolder = "";

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        ByteArrayInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung("noFileSelected");
                return;
            }

            String basename = this.uploadedFile.getName();
            if (basename.startsWith(".")) {
                basename = basename.substring(1);
            }
            if (basename.contains("/")) {
                basename = basename.substring(basename.lastIndexOf("/") + 1);
            }
            if (basename.contains("\\")) {
                basename = basename.substring(basename.lastIndexOf("\\") + 1);
            }

            if (StringUtils.isNotBlank(uploadedFileName)) {
                String fileExtension = Metadaten.getFileExtension(basename);
                if (!fileExtension.isEmpty() && !uploadedFileName.endsWith(fileExtension)) {
                    uploadedFileName = uploadedFileName + fileExtension;
                }
                basename = uploadedFileName;
                
            }
            if(logger.isTraceEnabled()){
            	logger.trace("folder to import: " + currentFolder);
            }
            String filename = metadataBean.getMyProzess().getImagesDirectory() + currentFolder + File.separator + basename;

            if(logger.isTraceEnabled()){
            	logger.trace("filename to import: " + filename);
            }

            if (new SafeFile(filename).exists()) {
                List<String> parameterList = new ArrayList<String>();
                parameterList.add(basename);
                Helper.setFehlerMeldung(Helper.getTranslation("fileExists", parameterList));
                return;
            }

            inputStream = new ByteArrayInputStream(this.uploadedFile.getBytes());
            outputStream = new FileOutputStream(filename);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            if(logger.isTraceEnabled()){
            	logger.trace(filename + " was imported");
            }
            // if file was uploaded into media folder, update pagination sequence
            if (metadataBean.getMyProzess().getImagesTifDirectory(false).equals(
                    metadataBean.getMyProzess().getImagesDirectory() + currentFolder + File.separator)) {
            	if(logger.isTraceEnabled()){
            		logger.trace("update pagination for " + metadataBean.getMyProzess().getTitel());
            	}
                updatePagination(filename);

            }

            Helper.setMeldung(Helper.getTranslation("metsEditorFileUploadSuccessful"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (SwapException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (TypeNotAllowedForParentException e) {
            logger.error(e);
            Helper.setFehlerMeldung("uploadFailed", e);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        metadataBean.retrieveAllImages();
        metadataBean.BildErmitteln(0);
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    private void updatePagination(String filename) throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException,
            DAOException, MetadataTypeNotAllowedException {
        if (!matchesFileConfiguration(filename)) {
            return;
        }

        if (insertPage.equals("lastPage")) {
            metadataBean.createPagination();
        } else {

            Prefs prefs = metadataBean.getMyProzess().getRegelsatz().getPreferences();
            DigitalDocument doc = metadataBean.getDocument();
            DocStruct physical = doc.getPhysicalDocStruct();

            List<DocStruct> pageList = physical.getAllChildren();

            int indexToImport = Integer.parseInt(insertPage);
            DocStructType newPageType = prefs.getDocStrctTypeByName("page");
            DocStruct newPage = doc.createDocStruct(newPageType);
            MetadataType physicalPageNoType = prefs.getMetadataTypeByName("physPageNumber");
            MetadataType logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
            for (int index = 0; index < pageList.size(); index++) {

                if (index == indexToImport) {
                    DocStruct oldPage = pageList.get(index);

                    // physical page no for new page

                    Metadata mdTemp = new Metadata(physicalPageNoType);
                    mdTemp.setValue(String.valueOf(indexToImport + 1));
                    newPage.addMetadata(mdTemp);

                    // new physical page no for old page
                    oldPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(indexToImport + 2));

                    // logical page no
                    // logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
                    mdTemp = new Metadata(logicalPageNoType);

                    if (insertMode.equalsIgnoreCase("uncounted")) {
                        mdTemp.setValue("uncounted");
                    } else {
                        // set new logical no. for new and old page 
                        Metadata oldPageNo = oldPage.getAllMetadataByType(logicalPageNoType).get(0);
                        mdTemp.setValue(oldPageNo.getValue());
                        if (index + 1 < pageList.size()) {
                            Metadata pageNoOfFollowingElement = pageList.get(index + 1).getAllMetadataByType(logicalPageNoType).get(0);
                            oldPageNo.setValue(pageNoOfFollowingElement.getValue());
                        } else {
                            oldPageNo.setValue("uncounted");
                        }
                    }

                    newPage.addMetadata(mdTemp);
                    doc.getLogicalDocStruct().addReferenceTo(newPage, "logical_physical");

                    ContentFile cf = new ContentFile();
                    cf.setLocation(filename);
                    newPage.addContentFile(cf);
                    doc.getFileSet().addFile(cf);

                }
                if (index > indexToImport) {
                    DocStruct currentPage = pageList.get(index);
                    // check if element is last element
                    currentPage.getAllMetadataByType(physicalPageNoType).get(0).setValue(String.valueOf(index + 2));
                    if (!insertMode.equalsIgnoreCase("uncounted")) {
                        if (index + 1 == pageList.size()) {
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue("uncounted");
                        } else {
                            DocStruct followingPage = pageList.get(index + 1);
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setValue(
                                    followingPage.getAllMetadataByType(logicalPageNoType).get(0).getValue());
                        }
                    }
                }
            }
            pageList.add(indexToImport, newPage);

        }
    }

    public UploadedFile getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getInsertPage() {
        return insertPage;
    }

    public void setInsertPage(String insertPage) {
        this.insertPage = insertPage;
    }

    public String getInsertMode() {
        return insertMode;
    }

    public void setInsertMode(String insertMode) {
        this.insertMode = insertMode;
    }

    /**
     * download file
     */

    public String getImageSelection() {
        return imageSelection;
    }

    public void setImageSelection(String imageSelection) {
        this.imageSelection = imageSelection;
    }

    public void downloadFile() {
        SafeFile downloadFile = null;

        int imageOrder = Integer.parseInt(imageSelection);
        DocStruct page = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren().get(imageOrder);
        String imagename = page.getImageName();
        String filenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
        try {
            SafeFile[] filesInFolder = new SafeFile(metadataBean.getMyProzess().getImagesDirectory() + currentFolder).listFiles();
            for (SafeFile currentFile : filesInFolder) {
                String currentFileName = currentFile.getName();
                String currentFileNamePrefix = currentFileName.substring(0, currentFileName.lastIndexOf("."));
                if (filenamePrefix.equals(currentFileNamePrefix)) {
                    downloadFile = currentFile;
                    break;
                }
            }
        } catch (SwapException e1) {
            logger.error(e1);
        } catch (DAOException e1) {
            logger.error(e1);
        } catch (IOException e1) {
            logger.error(e1);
        } catch (InterruptedException e1) {
            logger.error(e1);
        }

        if (downloadFile == null || !downloadFile.exists()) {
            List<String> paramList = new ArrayList<String>();
            //           paramList.add(metadataBean.getMyProzess().getTitel());
            paramList.add(filenamePrefix);
            paramList.add(currentFolder);
            Helper.setFehlerMeldung(Helper.getTranslation("MetsEditorMissingFile", paramList));
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            String fileName = downloadFile.getName();
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            InputStream in = null;
            ServletOutputStream out = null;
            try {
                in = downloadFile.createFileInputStream();
                out = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            } catch (IOException e) {
                logger.error("IOException while exporting run note", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
            }

            facesContext.responseComplete();
        }

    }

    /**
     * move files on server folder
     */

    public void exportFiles() {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        List<DocStruct> allPages = metadataBean.getDocument().getPhysicalDocStruct().getAllChildren();
        List<String> filenamesToMove = new ArrayList<String>();

        for (String fileIndex : selectedFiles) {
            try {
                int index = Integer.parseInt(fileIndex);
                filenamesToMove.add(allPages.get(index).getImageName());
            } catch (NumberFormatException e) {

            }
        }
        String tempDirectory = ConfigMain.getParameter("tempfolder", "/usr/local/goobi/tmp/");
        SafeFile fileuploadFolder = new SafeFile(tempDirectory + "fileupload");
        if (!fileuploadFolder.exists()) {
            fileuploadFolder.mkdir();
        }
        SafeFile destination = new SafeFile(fileuploadFolder.getAbsolutePath() + File.separator + metadataBean.getMyProzess().getTitel());
        if (!destination.exists()) {
            destination.mkdir();
        }

        for (String filename : filenamesToMove) {
            String prefix = filename.replace(Metadaten.getFileExtension(filename), "");
            String processTitle = metadataBean.getMyProzess().getTitel();
            for (String folder : metadataBean.getAllTifFolders()) {
                try {
                    SafeFile[] filesInFolder = new SafeFile(metadataBean.getMyProzess().getImagesDirectory() + folder).listFiles();
                    for (SafeFile currentFile : filesInFolder) {

                        String filenameInFolder = currentFile.getName();
                        String filenamePrefix = filenameInFolder.replace(Metadaten.getFileExtension(filenameInFolder), "");
                        if (filenamePrefix.equals(prefix)) {
                            SafeFile tempFolder = new SafeFile(destination.getAbsolutePath() + File.separator + folder);
                            if (!tempFolder.exists()) {
                                tempFolder.mkdir();
                            }

                            SafeFile destinationFile = new SafeFile(tempFolder, processTitle + "_" + currentFile.getName());

                            //                            if (deleteFilesAfterMove) {
                            //                                currentFile.renameTo(destinationFile);
                            //                            } else {
                            currentFile.copyFile(destinationFile);
                            //                            }
                            break;

                        }

                    }

                } catch (SwapException e) {
                    logger.error(e);
                } catch (DAOException e) {
                    logger.error(e);
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        }
        if (deleteFilesAfterMove) {
            String[] pagesArray = new String[selectedFiles.size()];
            selectedFiles.toArray(pagesArray);
            metadataBean.setAlleSeitenAuswahl(pagesArray);
            metadataBean.deleteSeltectedPages();
            selectedFiles = new ArrayList<String>();
            deleteFilesAfterMove = false;
        }

        metadataBean.retrieveAllImages();
        metadataBean.BildErmitteln(0);
    }

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public boolean isDeleteFilesAfterMove() {
        return deleteFilesAfterMove;
    }

    public void setDeleteFilesAfterMove(boolean deleteFilesAfterMove) {
        this.deleteFilesAfterMove = deleteFilesAfterMove;
    }

    public boolean isMoveFilesInAllFolder() {
        return moveFilesInAllFolder;
    }

    public void setMoveFilesInAllFolder(boolean moveFilesInAllFolder) {
        this.moveFilesInAllFolder = moveFilesInAllFolder;
    }

    /**
     * import files from folder
     * 
     */

    public List<String> getAllImportFolder() {

        String tempDirectory = ConfigMain.getParameter("tempfolder", "/usr/local/goobi/tmp/");
        SafeFile fileuploadFolder = new SafeFile(tempDirectory + "fileupload");

        allImportFolder = new ArrayList<String>();
        if (fileuploadFolder.isDirectory()) {
            allImportFolder.addAll(Arrays.asList(fileuploadFolder.list(directoryFilter)));
        }
        return allImportFolder;
    }

    public void setAllImportFolder(List<String> allImportFolder) {
        this.allImportFolder = allImportFolder;
    }

    private static FilenameFilter directoryFilter = new FilenameFilter() {
        @Override
        public boolean accept(final java.io.File dir, final String name) {
            SafeFile toTest = new SafeFile(dir, name);
            return toTest.isDirectory();
        }
    };

    public void importFiles() {

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        String tempDirectory = ConfigMain.getParameter("tempfolder", "/usr/local/goobi/tmp/");

        String masterPrefix = "";
        boolean useMasterFolder = false;
        if (ConfigMain.getBooleanParameter("useOrigFolder", true)) {
            useMasterFolder = true;
            masterPrefix = ConfigMain.getParameter("DIRECTORY_PREFIX", "orig");
        }
        Prozess currentProcess = metadataBean.getMyProzess();
        List<String> importedFilenames = new ArrayList<String>();
        for (String importName : selectedFiles) {
            SafeFile importfolder = new SafeFile(tempDirectory + "fileupload" + File.separator + importName);
            SafeFile[] subfolderList = importfolder.listFiles();
            for (SafeFile subfolder : subfolderList) {

                if (useMasterFolder) {
                    // check if current import folder is master folder
                    if (subfolder.getName().startsWith(masterPrefix)) {
                        try {
                            String masterFolderName = currentProcess.getImagesOrigDirectory(false);
                            SafeFile masterDirectory = new SafeFile(masterFolderName);
                            if (!masterDirectory.exists()) {
                                masterDirectory.mkdir();
                            }
                            SafeFile[] objectInFolder = subfolder.listFiles();
                            List<SafeFile> sortedList = Arrays.asList(objectInFolder);
                            Collections.sort(sortedList);
                           for (SafeFile object : sortedList) {
                        	   object.copyFileToDirectory(masterDirectory);
                            }
                        } catch (SwapException e) {
                            logger.error(e);
                            Helper.setFehlerMeldung("", e);
                        } catch (DAOException e) {
                            logger.error(e);
                        } catch (IOException e) {
                            logger.error(e);
                        } catch (InterruptedException e) {
                            logger.error(e);
                        }
                    } else {
                        if (subfolder.getName().contains("_")) {
                            String folderSuffix = subfolder.getName().substring(subfolder.getName().lastIndexOf("_") + 1);
                            String folderName = currentProcess.getMethodFromName(folderSuffix);
                            if (folderName != null) {
                                try {
                                    SafeFile directory = new SafeFile(folderName);
                                    SafeFile[] objectInFolder = subfolder.listFiles();
                                    List<SafeFile> sortedList = Arrays.asList(objectInFolder);
                                    Collections.sort(sortedList);
                                    for (SafeFile object : sortedList) {
                                        if (currentProcess.getImagesTifDirectory(false).equals(folderName + File.separator)) {
                                            importedFilenames.add(object.getName());
                                        }
                                        object.copyFileToDirectory(directory);
                                    }

                                }

                                catch (IOException e) {
                                    logger.error(e);
                                } catch (SwapException e) {
                                    logger.error(e);
                                } catch (DAOException e) {
                                    logger.error(e);
                                } catch (InterruptedException e) {
                                    logger.error(e);
                                }

                            }
                        }
                    }

                } else {
                    if (subfolder.getName().contains("_")) {
                        String folderSuffix = subfolder.getName().substring(subfolder.getName().lastIndexOf("_") + 1);
                        String folderName = currentProcess.getMethodFromName(folderSuffix);
                        if (folderName != null) {
                            SafeFile directory = new SafeFile(folderName);
                            SafeFile[] objectInFolder = subfolder.listFiles();
                            List<SafeFile> sortedList = Arrays.asList(objectInFolder);
                            Collections.sort(sortedList);
                            for (SafeFile object : sortedList) {
                                try {
                                    if (currentProcess.getImagesTifDirectory(false).equals(folderName + File.separator)) {
                                        importedFilenames.add(object.getName());
                                    }
                                    object.copyFileToDirectory(directory);
                                } catch (IOException e) {
                                    logger.error(e);
                                } catch (SwapException e) {
                                    logger.error(e);
                                } catch (DAOException e) {
                                    logger.error(e);
                                } catch (InterruptedException e) {
                                    logger.error(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        // update pagination
        try {
            if (insertPage == null || insertPage.isEmpty() || insertPage.equals("lastPage")) {
                metadataBean.createPagination();
            } else {
                int indexToImport = Integer.parseInt(insertPage);
                for (String filename : importedFilenames) {
                    updatePagination(filename);
                    insertPage = String.valueOf(++indexToImport);
                }
            }
        } catch (TypeNotAllowedForParentException e) {
            logger.error(e);
        } catch (SwapException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        }

        // delete folder

        for (String importName : selectedFiles) {
            SafeFile importfolder = new SafeFile(tempDirectory + "fileupload" + File.separator + importName);
           	importfolder.deleteQuietly();
        }
        metadataBean.retrieveAllImages();
        metadataBean.BildErmitteln(0);
    }

    public String getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    private static boolean matchesFileConfiguration(String filename) {

        if (filename == null) {
            return false;
        }

        String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        String afterLastBackslash = afterLastSlash.substring(afterLastSlash.lastIndexOf('\\') + 1);

        String prefix = ConfigMain.getParameter("ImagePrefix", "\\d{8}");
        if (!afterLastBackslash.matches(prefix + "\\..+")) {
            return false;
        }

        return true;
    }

}
