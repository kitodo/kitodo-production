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

package de.sub.goobi.metadaten;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.filemanagement.ProcessSubType;
import org.kitodo.api.filemanagement.filters.IsDirectoryFilter;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.TypeNotAllowedForParentException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.legacy.UghImplementation;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class FileManipulation {
    private static final Logger logger = LogManager.getLogger(FileManipulation.class);
    private Metadaten metadataBean;
    private static final ServiceManager serviceManager = new ServiceManager();
    private static final FileService fileService = serviceManager.getFileService();

    public FileManipulation(Metadaten metadataBean) {
        this.metadataBean = metadataBean;
    }

    // insert new file after this page
    private String insertPage = "";

    private String imageSelection = "";

    // mode of insert (uncounted or into pagination sequence)
    private String insertMode = "uncounted";

    private File uploadedFile = null;

    private String uploadedFileName = null;

    private List<String> selectedFiles = new ArrayList<>();

    private boolean deleteFilesAfterMove = false;

    private List<URI> allImportFolder = new ArrayList<>();

    private String currentFolder = "";

    /**
     * File upload with binary copying.
     */
    public void uploadFile() {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (this.uploadedFile == null) {
                Helper.setFehlerMeldung("noFileSelected");
                return;
            }

            String baseName = this.uploadedFile.getName();
            if (baseName.startsWith(".")) {
                baseName = baseName.substring(1);
            }
            if (baseName.contains("/")) {
                baseName = baseName.substring(baseName.lastIndexOf('/') + 1);
            }
            if (baseName.contains("\\")) {
                baseName = baseName.substring(baseName.lastIndexOf('\\') + 1);
            }

            if (StringUtils.isNotBlank(uploadedFileName)) {
                String fileExtension = Metadaten.getFileExtension(baseName);
                if (!fileExtension.isEmpty() && !uploadedFileName.endsWith(fileExtension)) {
                    uploadedFileName = uploadedFileName + fileExtension;
                }
                baseName = uploadedFileName;

            }
            logger.trace("folder to import: {}", currentFolder);
            URI filename = serviceManager.getFileService().getProcessSubTypeURI(metadataBean.getProcess(),
                    ProcessSubType.IMAGE, currentFolder + File.separator + baseName);
            logger.trace("filename to import: {}", filename);

            if (fileService.fileExist(filename)) {
                List<String> parameterList = new ArrayList<>();
                parameterList.add(baseName);
                Helper.setFehlerMeldung(Helper.getTranslation("fileExists", parameterList));
                return;
            }

            inputStream = fileService.read(uploadedFile.toURI());
            outputStream = fileService.write(filename);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            logger.trace("{} was imported", filename);
            // if file was uploaded into media folder, update pagination
            // sequence
            if (serviceManager.getProcessService().getImagesTifDirectory(false, metadataBean.getProcess())
                    .equals(serviceManager.getFileService().getProcessSubTypeURI(metadataBean.getProcess(),
                            ProcessSubType.IMAGE, currentFolder + File.separator))) {
                logger.trace("update pagination for {}", metadataBean.getProcess().getTitle());
                updatePagination(filename);
            }

            Helper.setMeldung(Helper.getTranslation("metsEditorFileUploadSuccessful"));
        } catch (IOException | TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
            Helper.setErrorMessage("uploadFailed", logger, e);
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
        metadataBean.identifyImage(1);
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    private void updatePagination(URI filename)
            throws TypeNotAllowedForParentException, IOException, MetadataTypeNotAllowedException {
        if (!matchesFileConfiguration(filename)) {
            return;
        }

        if (insertPage.equals("lastPage")) {
            metadataBean.createPagination();
        } else {

            PrefsInterface prefs = serviceManager.getRulesetService()
                    .getPreferences(metadataBean.getProcess().getRuleset());
            DigitalDocumentInterface doc = metadataBean.getDigitalDocument();
            DocStructInterface physical = doc.getPhysicalDocStruct();

            List<DocStructInterface> pageList = physical.getAllChildren();

            int indexToImport = Integer.parseInt(insertPage);
            DocStructTypeInterface newPageType = prefs.getDocStrctTypeByName("page");
            DocStructInterface newPage = doc.createDocStruct(newPageType);
            MetadataTypeInterface physicalPageNoType = prefs.getMetadataTypeByName("physPageNumber");
            MetadataTypeInterface logicalPageNoType = prefs.getMetadataTypeByName("logicalPageNumber");
            for (int index = 0; index < pageList.size(); index++) {

                if (index == indexToImport) {
                    DocStructInterface oldPage = pageList.get(index);

                    // physical page no for new page

                    MetadataInterface mdTemp = UghImplementation.INSTANCE.createMetadata(physicalPageNoType);
                    mdTemp.setStringValue(String.valueOf(indexToImport + 1));
                    newPage.addMetadata(mdTemp);

                    // new physical page no for old page
                    oldPage.getAllMetadataByType(physicalPageNoType).get(0)
                            .setStringValue(String.valueOf(indexToImport + 2));

                    // logical page no
                    // logicalPageNoType =
                    // prefs.getMetadataTypeByName("logicalPageNumber");
                    mdTemp = UghImplementation.INSTANCE.createMetadata(logicalPageNoType);

                    if (insertMode.equalsIgnoreCase("uncounted")) {
                        mdTemp.setStringValue("uncounted");
                    } else {
                        // set new logical no. for new and old page
                        MetadataInterface oldPageNo = oldPage.getAllMetadataByType(logicalPageNoType).get(0);
                        mdTemp.setStringValue(oldPageNo.getValue());
                        if (index + 1 < pageList.size()) {
                            MetadataInterface pageNoOfFollowingElement = pageList.get(index + 1)
                                    .getAllMetadataByType(logicalPageNoType).get(0);
                            oldPageNo.setStringValue(pageNoOfFollowingElement.getValue());
                        } else {
                            oldPageNo.setStringValue("uncounted");
                        }
                    }

                    newPage.addMetadata(mdTemp);
                    doc.getLogicalDocStruct().addReferenceTo(newPage, "logical_physical");

                    ContentFileInterface cf = UghImplementation.INSTANCE.createContentFile();
                    cf.setLocation(fileService.getFileName(filename));
                    newPage.addContentFile(cf);
                    doc.getFileSet().addFile(cf);

                }
                if (index > indexToImport) {
                    DocStructInterface currentPage = pageList.get(index);
                    // check if element is last element
                    currentPage.getAllMetadataByType(physicalPageNoType).get(0)
                            .setStringValue(String.valueOf(index + 2));
                    if (!insertMode.equalsIgnoreCase("uncounted")) {
                        if (index + 1 == pageList.size()) {
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setStringValue("uncounted");
                        } else {
                            DocStructInterface followingPage = pageList.get(index + 1);
                            currentPage.getAllMetadataByType(logicalPageNoType).get(0).setStringValue(
                                followingPage.getAllMetadataByType(logicalPageNoType).get(0).getValue());
                        }
                    }
                }
            }
            pageList.add(indexToImport, newPage);

        }
    }

    public File getUploadedFile() {
        return this.uploadedFile;
    }

    public void setUploadedFile(File uploadedFile) {
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
     * Get image selection.
     */
    public String getImageSelection() {
        return imageSelection;
    }

    public void setImageSelection(String imageSelection) {
        this.imageSelection = imageSelection;
    }

    /**
     * Download file.
     */
    public void downloadFile() {
        URI downloadFile = null;

        int imageOrder = Integer.parseInt(imageSelection);
        DocStructInterface page = metadataBean.getDigitalDocument().getPhysicalDocStruct().getAllChildren()
                .get(imageOrder);
        String imageName = page.getImageName();
        String filenamePrefix = imageName.substring(0, imageName.lastIndexOf('.'));
        URI processSubTypeURI = serviceManager.getFileService().getProcessSubTypeURI(metadataBean.getProcess(),
                ProcessSubType.IMAGE, currentFolder);
        ArrayList<URI> filesInFolder = fileService.getSubUris(processSubTypeURI);
        for (URI currentFile : filesInFolder) {
            String currentFileName = fileService.getFileName(currentFile);
            String currentFileNamePrefix = currentFileName.substring(0, currentFileName.lastIndexOf('.'));
            if (filenamePrefix.equals(currentFileNamePrefix)) {
                downloadFile = currentFile;
                break;
            }
        }

        if (downloadFile == null || !fileService.fileExist(downloadFile)) {
            List<String> paramList = new ArrayList<>();
            // paramList.add(metadataBean.getMyProzess().getTitel());
            paramList.add(filenamePrefix);
            paramList.add(currentFolder);
            Helper.setFehlerMeldung(Helper.getTranslation("MetsEditorMissingFile", paramList));
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try (InputStream in = fileService.read(downloadFile);
                    ServletOutputStream out = response.getOutputStream()) {
                String fileName = fileService.getFileName(downloadFile);
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType(fileName);
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                out.flush();
            } catch (IOException e) {
                logger.error("IOException while exporting run note", e);
            }

            facesContext.responseComplete();
        }
    }

    /**
     * move files on server folder.
     */
    public void exportFiles() throws IOException {
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        List<DocStructInterface> allPages = metadataBean.getDigitalDocument().getPhysicalDocStruct().getAllChildren();
        List<String> filenamesToMove = new ArrayList<>();

        for (String fileIndex : selectedFiles) {
            try {
                int index = Integer.parseInt(fileIndex);
                filenamesToMove.add(allPages.get(index).getImageName());
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
        URI tempDirectory = fileService.getTemporalDirectory();
        URI fileuploadFolder = fileService.createDirectory(tempDirectory, "fileupload");

        URI destination = fileuploadFolder.resolve(File.separator + metadataBean.getProcess().getTitle());
        if (!fileService.fileExist(destination)) {
            fileService.createDirectory(fileuploadFolder, metadataBean.getProcess().getTitle());
        }

        for (String filename : filenamesToMove) {
            String prefix = filename.replace(Metadaten.getFileExtension(filename), "");
            String processTitle = metadataBean.getProcess().getTitle();
            for (URI folder : metadataBean.getAllTifFolders()) {
                ArrayList<URI> filesInFolder = fileService.getSubUris(serviceManager.getFileService()
                        .getProcessSubTypeURI(metadataBean.getProcess(), ProcessSubType.IMAGE, folder.toString()));
                for (URI currentFile : filesInFolder) {

                    String filenameInFolder = fileService.getFileName(currentFile);
                    String filenamePrefix = filenameInFolder.replace(Metadaten.getFileExtension(filenameInFolder), "");
                    if (filenamePrefix.equals(prefix)) {
                        URI tempFolder = destination.resolve(File.separator + folder);
                        if (!fileService.fileExist(tempFolder)) {
                            fileService.createDirectory(destination, folder.toString());
                        }

                        URI destinationFile = tempFolder
                                .resolve(processTitle + "_" + fileService.getFileName(currentFile));

                        // if (deleteFilesAfterMove) {
                        // currentFile.renameTo(destinationFile);
                        // } else {
                        fileService.copyFile(currentFile, destinationFile);
                        // }
                        break;

                    }

                }

            }
        }
        if (deleteFilesAfterMove) {
            String[] pagesArray = new String[selectedFiles.size()];
            selectedFiles.toArray(pagesArray);
            metadataBean.setAllPagesSelection(pagesArray);
            metadataBean.deleteSelectedPages();
            selectedFiles = new ArrayList<>();
            deleteFilesAfterMove = false;
        }

        metadataBean.retrieveAllImages();
        metadataBean.identifyImage(1);
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

    /**
     * Import files from folder.
     *
     * @return URI list of import folders
     */
    public List<URI> getAllImportFolder() {
        URI tempDirectory = new File(ConfigCore.getParameter("tempfolder", "/usr/local/kitodo/tmp/")).toURI();
        URI fileuploadFolder = tempDirectory.resolve("fileupload");

        allImportFolder = new ArrayList<>();
        if (fileService.isDirectory(fileuploadFolder)) {
            allImportFolder.addAll(fileService.getSubUris(directoryFilter, fileuploadFolder));
        }
        return allImportFolder;
    }

    public void setAllImportFolder(List<URI> allImportFolder) {
        this.allImportFolder = allImportFolder;
    }

    private static FilenameFilter directoryFilter = new IsDirectoryFilter();

    /**
     * Import files.
     */
    public void importFiles() throws IOException {

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            Helper.setFehlerMeldung("noFileSelected");
            return;
        }
        String tempDirectory = ConfigCore.getParameter("tempfolder", "/usr/local/kitodo/tmp/");

        String masterPrefix = "";
        boolean useMasterFolder = false;
        if (ConfigCore.getBooleanParameter("useOrigFolder", true)) {
            useMasterFolder = true;
            masterPrefix = ConfigCore.getParameter("DIRECTORY_PREFIX", "orig");
        }
        Process currentProcess = metadataBean.getProcess();
        List<URI> importedFileNames = new ArrayList<>();
        for (String importName : selectedFiles) {
            URI importFolderUri = fileService.createResource(tempDirectory + "fileupload/" + importName);
            ArrayList<URI> subFolderList = fileService.getSubUris(importFolderUri);
            for (URI subFolder : subFolderList) {
                if (useMasterFolder) {
                    // check if current import folder is master folder
                    if (fileService.getFileName(subFolder).startsWith(masterPrefix)) {
                        URI masterDirectory = serviceManager.getProcessService().getImagesOrigDirectory(false,
                                currentProcess);
                        ArrayList<URI> objectInFolder = fileService.getSubUris(subFolder);
                        Collections.sort(objectInFolder);
                        for (URI file : objectInFolder) {
                            fileService.copyFileToDirectory(file, masterDirectory);
                        }
                    } else {
                        importedFileNames = copyFileToDirectoryForNamesWithUnderscore(subFolder, currentProcess,
                                importedFileNames);
                    }
                } else {
                    importedFileNames = copyFileToDirectoryForNamesWithUnderscore(subFolder, currentProcess,
                            importedFileNames);
                }
            }
        }
        // update pagination
        try {
            if (insertPage == null || insertPage.isEmpty() || insertPage.equals("lastPage")) {
                metadataBean.createPagination();
            } else {
                int indexToImport = Integer.parseInt(insertPage);
                for (URI filename : importedFileNames) {
                    updatePagination(filename);
                    insertPage = String.valueOf(++indexToImport);
                }
            }
        } catch (TypeNotAllowedForParentException | MetadataTypeNotAllowedException | IOException e) {
            logger.error(e.getMessage(), e);
        }

        // delete folder

        for (String importName : selectedFiles) {
            File importFolder = new File(tempDirectory + "fileupload" + File.separator + importName);
            fileService.delete(importFolder.toURI());
        }
        metadataBean.retrieveAllImages();
        metadataBean.identifyImage(1);
    }

    private List<URI> copyFileToDirectoryForNamesWithUnderscore(URI subFolder, Process currentProcess,
            List<URI> importedFileNames) throws IOException {
        if (fileService.getFileName(subFolder).contains("_")) {
            String folderSuffix = fileService.getFileName(subFolder)
                    .substring(fileService.getFileName(subFolder).lastIndexOf('_') + 1);
            URI folderName = serviceManager.getProcessService().getMethodFromName(folderSuffix, currentProcess);
            if (folderName != null) {
                ArrayList<URI> objectInFolder = fileService.getSubUris(subFolder);
                Collections.sort(objectInFolder);
                for (URI file : objectInFolder) {
                    if (serviceManager.getProcessService().getImagesTifDirectory(false, currentProcess).getRawPath()
                            .equals(folderName + "/")) {
                        importedFileNames.add(file);
                    }
                    fileService.copyFileToDirectory(file, folderName);
                }
            }
        }
        return importedFileNames;
    }

    public String getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    private static boolean matchesFileConfiguration(URI file) {
        String fileName = fileService.getFileName(file);

        if (fileName == null) {
            return false;
        }

        String afterLastSlash = fileName.substring(fileName.lastIndexOf('/') + 1);
        String afterLastBackslash = afterLastSlash.substring(afterLastSlash.lastIndexOf('\\') + 1);
        String prefix = ConfigCore.getParameter("ImagePrefix", "\\d{8}");

        return afterLastBackslash.matches(prefix + "\\..+");
    }

}
