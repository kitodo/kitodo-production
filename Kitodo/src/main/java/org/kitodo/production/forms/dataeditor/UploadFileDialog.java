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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.View;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.exceptions.InvalidImagesException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.production.enums.GenerationMode;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.metadata.InsertionPosition;
import org.kitodo.production.metadata.MetadataEditor;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.SubfolderFactoryService;
import org.kitodo.production.services.image.ImageGenerator;
import org.kitodo.production.thread.TaskImageGeneratorThread;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

public class UploadFileDialog {
    private static final Logger logger = LogManager.getLogger(UploadFileDialog.class);

    private final DataEditorForm dataEditor;
    private UploadedFile file;
    private String mimeType;
    private String fileExtension;
    private String use;
    private URI sourceFolderURI;
    private List<SelectItem> possiblePositions = new ArrayList<>();
    private InsertionPosition selectedPosition;
    private Folder sourceFolder;
    private List<Folder> contentFolders = new ArrayList<>();
    private MediaVariant mediaVariant;
    private int indexSelectedMedia;
    private IncludedStructuralElement parent;
    private MediaUnit mediaUnit;
    private URI uploadFileUri;
    private Subfolder generatorSource;
    private int fileLimit = ConfigCore.getIntParameter(ParameterCore.METS_EDITOR_MAX_UPLOADED_MEDIA);
    private List<Pair<MediaUnit, IncludedStructuralElement>> selectedMedia = new LinkedList<>();
    private Integer progress;
    private List<EmptyTask> generateMediaTasks = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param dataEditor Instance of DataEditorForm where this instance of UploadFileDialog was created.
     */
    UploadFileDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Get fileExtension.
     *
     * @return value of fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Set fileExtension.
     *
     * @param fileExtension as java.lang.String
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Get fileLimit.
     *
     * @return value of fileLimit
     */
    public int getFileLimit() {
        return fileLimit;
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
     * @param possiblePositions as java.util.List of SelectItem
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

    /**
     * Get progress.
     *
     * @return value of progress
     */
    public int getProgress() {
        progress = updateProgress();
        return progress;
    }

    /**
     * Set progress.
     *
     * @param progress as int
     */
    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    private Integer updateProgress() {
        if (!generateMediaTasks.isEmpty() && progress != 100) {
            return generateMediaTasks.stream().mapToInt(w -> w.getProgress()).sum() / generateMediaTasks.size();
        } else {
            return progress;
        }
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

    private boolean setUpFolders() {
        VariableReplacer variableReplacer = new VariableReplacer(null, dataEditor.getProcess(), null);
        sourceFolder = dataEditor.getProcess().getProject().getGeneratorSource();
        Folder mediaView = dataEditor.getProcess().getProject().getMediaView();
        Folder preview = dataEditor.getProcess().getProject().getPreview();

        sourceFolder.setPath(variableReplacer.replace(sourceFolder.getRelativePath()));
        mediaView.setPath(variableReplacer.replace(mediaView.getRelativePath()));
        preview.setPath(variableReplacer.replace(preview.getRelativePath()));

        if (folderExists(sourceFolder) && folderExists(mediaView) && folderExists(preview)) {
            sourceFolderURI = getFolderURI(sourceFolder);
            contentFolders.add(mediaView);
            contentFolders.add(preview);
            return true;
        }
        return false;
    }

    private void sortViews(List<View> views) {
        views.sort(Comparator.comparing(v -> FilenameUtils.getBaseName(
                v.getMediaUnit().getMediaFiles().entrySet().iterator().next().getValue().getPath())));
    }

    private boolean folderExists(Folder folder) {
        URI folderURI = getFolderURI(folder);
        if (!ServiceManager.getFileService().fileExist(folderURI)) {
            Helper.setErrorMessage("errorDirectoryNotFound", new Object[]{folderURI});
            return false;
        }
        return true;
    }

    private URI getFolderURI(Folder folder) {
        return Paths.get(ConfigCore.getKitodoDataDirectory(),
                ServiceManager.getProcessService().getProcessDataDirectory(dataEditor.getProcess()).getPath(),
                folder.getRelativePath()).toUri();
    }

    private void initPosition() {
        TreeNode selectedLogicalNode = dataEditor.getStructurePanel().getSelectedLogicalNode();
        if (Objects.nonNull(selectedLogicalNode)
                && selectedLogicalNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
            if (structureTreeNode.getDataObject() instanceof View) {
                if (Objects.nonNull(selectedLogicalNode.getParent())
                        && selectedLogicalNode.getParent().getData() instanceof StructureTreeNode
                        && Objects.nonNull(((StructureTreeNode) selectedLogicalNode.getParent().getData()).getDataObject())
                        && ((StructureTreeNode) selectedLogicalNode.getParent().getData()).getDataObject()
                        instanceof IncludedStructuralElement) {
                    parent =
                            (IncludedStructuralElement) ((StructureTreeNode) selectedLogicalNode.getParent().getData()).getDataObject();
                    indexSelectedMedia = parent.getViews().indexOf(structureTreeNode.getDataObject());

                }
            } else if (structureTreeNode.getDataObject() instanceof IncludedStructuralElement) {
                parent = (IncludedStructuralElement) structureTreeNode.getDataObject();
            }
        }
    }

    private void preparePossiblePositions() {
        possiblePositions = new ArrayList<>();
        TreeNode selectedLogicalNode = dataEditor.getStructurePanel().getSelectedLogicalNode();
        if (Objects.nonNull(selectedLogicalNode)
                && selectedLogicalNode.getData() instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();
            if (structureTreeNode.getDataObject() instanceof View) {
                possiblePositions.add(new SelectItem(InsertionPosition.BEFORE_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.beforeCurrentElement")));
                possiblePositions.add(new SelectItem(InsertionPosition.AFTER_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.afterCurrentElement")));
            } else {
                possiblePositions.add(new SelectItem(InsertionPosition.FIRST_CHILD_OF_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.asFirstChildOfCurrentElement")));
                possiblePositions.add(new SelectItem(InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT,
                        Helper.getTranslation("dataEditor.position.asLastChildOfCurrentElement")));
            }
        }
    }

    /**
     * Prepare popup dialog.
     */
    public void prepare() {
        progress = 0;
        generateMediaTasks = new ArrayList<>();
        selectedMedia = new LinkedList<>();
        if (!setUpFolders()) { return; }
        generatorSource = new Subfolder(dataEditor.getProcess(), sourceFolder);
        mimeType = sourceFolder.getMimeType();
        use = sourceFolder.getFileGroup();
        mediaVariant = getMediaVariant();
        fileExtension = generatorSource.getFileFormat().getExtension(false);
        preparePossiblePositions();
        initPosition();
        PrimeFaces.current().executeScript("PF('uploadFileDialog').show()");
    }

    /**
     * Upload media.
     *
     * @param event as FileUploadEvent
     */
    public void uploadMedia(FileUploadEvent event) {
        if (event.getFile() != null) {

            mediaUnit = MetadataEditor.addMediaUnit(getPhysicalDivType(), dataEditor.getWorkpiece(),
                    dataEditor.getWorkpiece().getMediaUnit(), InsertionPosition.LAST_CHILD_OF_CURRENT_ELEMENT);
            uploadFileUri = sourceFolderURI.resolve(event.getFile().getFileName());

            //TODO: Find a better way to avoid overwriting an existing file
            if (ServiceManager.getFileService().fileExist(uploadFileUri)) {
                String newFileName = ServiceManager.getFileService().getFileName(uploadFileUri)
                        + "_" + Helper.generateRandomString(3) + "." + fileExtension;
                uploadFileUri = sourceFolderURI.resolve(newFileName);
            }
            mediaUnit.getMediaFiles().put(mediaVariant, uploadFileUri);
            //upload file in sourceFolder
            try (OutputStream outputStream = ServiceManager.getFileService().write(uploadFileUri)) {
                IOUtils.copy(event.getFile().getInputstream(), outputStream);
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
            dataEditor.getUnsavedUploadedMedia().add(mediaUnit);
            selectedMedia.add(new ImmutablePair(mediaUnit, parent));
            PrimeFaces.current().executeScript("PF('notifications').renderMessage({'summary':'"
                    + Helper.getTranslation("mediaUploaded", Collections.singletonList(event.getFile().getFileName()))
                    + "','severity':'info'});");
        }
    }

    /**
     * Generate newly uploaded media.
     */
    public void generateNewUploadedMedia() {
        List<Subfolder> outputs = SubfolderFactoryService.createAll(dataEditor.getProcess(), contentFolders);
        ImageGenerator imageGenerator;
        if (generatorSource.listContents().isEmpty()) {
            imageGenerator = new ImageGenerator(generatorSource, GenerationMode.ALL, outputs);
        } else {
            imageGenerator = new ImageGenerator(generatorSource, GenerationMode.MISSING, outputs);
        }
        EmptyTask emptyTask = new TaskImageGeneratorThread(dataEditor.getProcess().getTitle(), imageGenerator);
        TaskManager.addTask(emptyTask);
        generateMediaTasks.add(emptyTask);
    }

    /**
     * Reset the progress bar after generating media is completed and update the workpiece.
     */
    public void updateWorkpiece() throws InvalidImagesException, NoSuchMetadataFieldException {
        progress = 0;
        generateMediaTasks.clear();
        addMediaToWorkpiece();
        refresh();
        Helper.setMessage(Helper.getTranslation("uploadMediaCompleted"));
    }

    private void addMediaToWorkpiece() throws InvalidImagesException {
        ServiceManager.getFileService().searchForMedia(dataEditor.getProcess(), dataEditor.getWorkpiece());

        List<View> views = selectedMedia.stream()
                .map(v -> MetadataEditor.createUnrestrictedViewOn(v.getKey()))
                .collect(Collectors.toList());
        sortViews(views);
        switch (selectedPosition) {
            case FIRST_CHILD_OF_CURRENT_ELEMENT:
                parent.getViews().addAll(0, views);
                break;
            case LAST_CHILD_OF_CURRENT_ELEMENT:
                parent.getViews().addAll(views);
                break;
            case AFTER_CURRENT_ELEMENT:
                parent.getViews().addAll(indexSelectedMedia + 1, views);
                break;
            case BEFORE_CURRENT_ELEMENT:
                parent.getViews().addAll(indexSelectedMedia, views);
                break;
            default:
                throw new IllegalArgumentException("Position of new div element is not supported");
        }
    }

    /**
     * Refresh the metadataeditor after uploading media.
     */
    public void refresh() throws NoSuchMetadataFieldException {
        if (uploadFileUri != null && ServiceManager.getFileService().fileExist(uploadFileUri)) {
            selectedMedia.sort((Comparator.comparing(v -> FilenameUtils.getBaseName(
                    v.getKey().getMediaFiles().entrySet().iterator().next().getValue().getPath()))));
            dataEditor.getSelectedMedia().clear();
            dataEditor.getSelectedMedia().addAll(selectedMedia);
            dataEditor.getStructurePanel().show();
            dataEditor.getStructurePanel().preserve();
            dataEditor.refreshStructurePanel();
            dataEditor.getGalleryPanel().show();
            dataEditor.getStructurePanel().updateLogicalNodeSelection(
                    dataEditor.getGalleryPanel().getGalleryMediaContent(selectedMedia.get(selectedMedia.size() - 1).getKey()), parent);
            StructureTreeNode structureTreeNode = new StructureTreeNode(selectedMedia.get(selectedMedia.size() - 1).getKey().getLabel(),
                    false, false,
                    MetadataEditor.createUnrestrictedViewOn(selectedMedia.get(selectedMedia.size() - 1).getKey()));
            dataEditor.switchStructure(structureTreeNode, false);
            dataEditor.getPaginationPanel().show();
            uploadFileUri = null;
        }
    }


}
