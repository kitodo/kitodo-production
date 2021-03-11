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

package org.kitodo.production.forms.dataeditor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

public class UploadFileDialog {
    private static final Logger logger = LogManager.getLogger(UploadFileDialog.class);

    private final DataEditorForm dataEditor;
    private UploadedFile file;
    private String mimeType;
    private String fileType;
    private String use;
    private URI sourceFolderURI;
    private List<SelectItem> possiblePositions;
    private InsertionPosition selectedPosition = InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT;
    private Folder sourceFolder;
    private List<Folder> contentFolders = new ArrayList<>();
    private MediaVariant mediaVariant;

    /**
     * Constructor.
     *
     * @param dataEditor Instance of DataEditorForm where this instance of UploadFileDialog was created.
     */
    UploadFileDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    public void prepare() {
        sourceFolder = dataEditor.getProcess().getProject().getGeneratorSource();
        contentFolders.add(dataEditor.getProcess().getProject().getMediaView());
        contentFolders.add(dataEditor.getProcess().getProject().getPreview());


        mimeType = sourceFolder.getMimeType();
        use = sourceFolder.getFileGroup();

        mediaVariant = getMediaVariant();
        fileType = mimeType.substring(mimeType.indexOf("/") + 1);
        sourceFolderURI = URI.create(sourceFolder.getUrlStructure().replace("$(meta.CatalogIDDigital)",
                String.valueOf(dataEditor.getProcess().getId())));
        preparePossiblePositions();
    }

    private void preparePossiblePositions() {
        possiblePositions = new ArrayList<>();
        possiblePositions.add(new SelectItem(InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("dataEditor.position.asFirstChildOfCurrentElement")));
        possiblePositions.add(new SelectItem(InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT,
                Helper.getTranslation("dataEditor.position.asLastChildOfCurrentElement")));
    }

    /**
     * Get fileType.
     *
     * @return value of fileType
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Set fileType.
     *
     * @param fileType as java.lang.String
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Get file.
     *
     * @return value of file
     */
    public UploadedFile getFile() {
        return file;
    }

    /**
     * Set file.
     *
     * @param file as org.primefaces.model.UploadedFile
     */
    public void setFile(UploadedFile file) {
        this.file = file;
    }

    /**
     * Get possiblePositions.
     *
     * @return value of possiblePositions
     */
    public List<SelectItem> getPossiblePositions() {
        return possiblePositions;
    }

    /**
     * Set possiblePositions.
     *
     * @param possiblePositions as java.util.List<javax.faces.model.SelectItem>
     */
    public void setPossiblePositions(List<SelectItem> possiblePositions) {
        this.possiblePositions = possiblePositions;
    }

    /**
     * Get selectedPosition.
     *
     * @return value of selectedPosition
     */
    public InsertionPosition getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Set selectedPosition.
     *
     * @param selectedPosition as org.kitodo.production.metadata.InsertionPosition
     */
    public void setSelectedPosition(InsertionPosition selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    private MediaVariant getMediaVariant() {
        MediaVariant mediaVariant = new MediaVariant();
        mediaVariant.setMimeType(mimeType);
        mediaVariant.setUse(use);
        return mediaVariant;
    }

    private String getPhysicalDivType() {
        if (mimeType.contains("image")) {
            return MediaUnit.TYPE_PAGE;
        }
        if (mimeType.contains("audio")) {
            return MediaUnit.TYPE_TRACK;
        }
        return MediaUnit.TYPE_OTHER;
    }

    private void generateNewUploadedImages() {
        if (Objects.isNull(sourceFolder)) {
            Helper.setErrorMessage("noSourceFolderConfiguredInProject");
            return;
        }
        if (Objects.isNull(contentFolders)) {
            Helper.setErrorMessage("noImageFolderConfiguredInProject");
            return;
        }
        Subfolder generatorSource = new Subfolder(dataEditor.getProcess(), sourceFolder);
        if (generatorSource.listContents().isEmpty()) {
            Helper.setErrorMessage("emptySourceFolder");
        } else {
            List<Subfolder> outputs = SubfolderFactoryService.createAll(dataEditor.getProcess(), contentFolders);
            ImageGenerator imageGenerator = new ImageGenerator(generatorSource, GenerationMode.MISSING, outputs);
            TaskManager.addTask(new TaskImageGeneratorThread(dataEditor.getProcess().getTitle(), imageGenerator));
        }
    }

    public void refresh() throws InvalidImagesException {
        dataEditor.refreshStructurePanel();
        dataEditor.getPaginationPanel().show();
        ServiceManager.getFileService().searchForMedia(dataEditor.getProcess(), dataEditor.getWorkpiece());
        dataEditor.getGalleryPanel().show();
    }

    public void uploadFiles(FileUploadEvent event) {
        if (event.getFile() != null) {
            //create MediaUnit and edit workpiece
            MediaUnit mediaUnit = MetadataEditor.addMediaUnit(getPhysicalDivType(), dataEditor.getWorkpiece(),
                    dataEditor.getWorkpiece().getMediaUnit(), InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT);

            URI fileUri = sourceFolderURI.resolve(event.getFile().getFileName());
            mediaUnit.getMediaFiles().put(mediaVariant, fileUri);
            switch (selectedPosition) {
                case FIRST_CHILD_OF_CURRENT_ELEMENT:
                    this.dataEditor.getStructurePanel().getSelectedStructure().get().getViews().add(0,
                            MetadataEditor.createUnrestrictedViewOn(mediaUnit));
                    break;
                case LAST_CHILD_OF_CURRENT_ELEMENT:
                    this.dataEditor.getStructurePanel().getSelectedStructure().get().getViews().add(
                            MetadataEditor.createUnrestrictedViewOn(mediaUnit));
                    break;
                default:
                    throw new IllegalArgumentException("Position of new div element is not supported");

            }
            //upload file in sourceFolder
            try (OutputStream outputStream = ServiceManager.getFileService().write(fileUri)) {
                IOUtils.copy(event.getFile().getInputstream(), outputStream);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }
        generateNewUploadedImages();
    }
}
