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

package org.kitodo.production.forms;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.PositionOfNewDocStrucElement;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.metadata.MetadataImpl;
import org.kitodo.production.metadata.pagination.Paginator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.Problem;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.TreeNode;

@Named("DataEditorForm")
@ViewScoped
public class DataEditorForm implements Serializable {

    /**
     * Indicates to JSF to navigate to the web page containing the meta-data
     * editor.
     */
    private static final String PAGE_METADATA_EDITOR = "/pages/metadataEditor?faces-redirect=true";
    private static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    private static final String FULLSIZE_FOLDER_NAME = "fullsize";
    private static final Logger logger = LogManager.getLogger(DataEditorForm.class);

    private Process process;
    private User user;
    private String referringView = "desktop";
    private String galleryViewMode = "list";
    private boolean showPagination = false;
    private boolean showNewComment = false;

    // pages
    private String[] allPages;
    private String[] selectedPages;
    private String selectedFirstPage;
    private String selectedLastPage;

    // pagination
    private int numberOfImagesToAdd;
    private Paginator paginator = new Paginator();
    private String paginationValue;
    private boolean paginationFictitious = false;

    // structure
    private TreeNode logicalStructure;
    private TreeNode selectedLogicalTreeNode;
    private PositionOfNewDocStrucElement selectedNewLogicalPosition;
    private String selectedNewLogicalType;
    private boolean addMultipleLogicalElements = false;
    private int numberOfLogicalElements;
    private String selectedMetadataType;
    private String metadataValue;
    private String selectedFirstPageForAssignment;
    private String selectedLastPageForAssignment;
    private String[] selectedPagesForAssignment;
    private String[] selectedPagesOfSelectedLogicalTreeNode;

    // metadata
    private List<Void> metaPersonList;
    private Void selectedPerson;
    private String newPersonFirstName;
    private String newPersonLastName;
    private String newPersonRecord;
    private String newPersonRole;
    private List<MetadataImpl> selectedLogicalTreeNodeMetadataList;
    private MetadataImpl selectedMetadata;
    private String selectedNewMetadataType;
    private MetadataImpl newMetadata;
    private List<MetadataImpl> newMetadataList;

    // comments
    private boolean newCommentIsCorrection = false;
    private String newComment;
    private Problem problem = new Problem();

    // gallery
    private int pageIndex;
    private int numberOfPngImages;
    private int numberOfTifImages;
    private String selectedImage;
    private URI selectedTifDirectory;

    /**
     * ID of the process to open. The ID must be set previous to calling
     * {@link #open()} by using a {@code setPropertyActionListener}.
     */
    private int processId;

    /**
     * Public constructor.
     */
    public DataEditorForm() {
        // TODO implement
        this.process = new Process();
    }

    /**
     * This method must be called to start the meta-data editor. When this
     * method is executed, the meta-data editor is not yet open in the browser,
     * but the previous page is still displayed. Three variables are expected to
     * have been set in advance using property action listeners: ‘process’,
     * ‘user’, and ‘referringView’.
     *
     * @return which page JSF should navigate to
     */
    public String open(int processId) {
        try {
            this.process = ServiceManager.getProcessService().getById(processId);
            // TODO implement
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return referringView;
        }
        return PAGE_METADATA_EDITOR;
    }

    /**
     * Save the structure and metadata.
     * 
     * @return navigation target
     */
    public String save() {
        try {
            // TODO implement: save metadata here
            return referringView;
        } catch (RuntimeException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return "";
        }

    }

    /**
     * Validate the structure and metadata.
     */
    public void validate() {
        // TODO implement: validate metadata here
        Helper.setMessage("Validation result");
    }

    /**
     * Generate PNG preview images.
     */
    public void generatePngs() {
        // TODO implement: generate PNGs
    }

    /**
     * Add a single new logical Element and set the specified pages.
     */
    public void addLogicalNode() {
        // TODO implement
    }

    /**
     * Add multiple new logical Elements.
     */
    public void addMultipleLogicalNodes() {
        // TODO implement
        /* use this.selectedLogicalTreeNode
               this.selectedNewLogicalPosition
               this.selectedNewLogicalType
               this.numberOfLogicalElements
               this.selectedMetadataType
               this.metadataValue
        */
    }

    /**
     * Delete the currently selected logical TreeNode.
     */
    public void deleteLogicalNode() {
        // TODO implement: delete node from selectedLogicalTreeNode
    }

    /**
     * Get possible positions of new element relative to its parent.
     * 
     * @return list of enums
     */
    public PositionOfNewDocStrucElement[] getNewElementPositionList() {
        return PositionOfNewDocStrucElement.values();
    }

    /**
     * Get possible element types for a new element at the selected position.
     * 
     * @return List of possible element types
     */
    public SelectItem[] getNewLogicalTypeList() {
        // TODO implement
        return new SelectItem[0];
    }

    /**
     * Get list of possible metadata for the selected element type.
     * 
     * @return list of possible metadata fields
     */
    public List<SelectItem> getNewLogicalMetadataList() {
        // TODO implement
        // get list of possible metadata for element in this.selectedNewLogicalType
        return new ArrayList<>();
    }

    /**
     * Assign all pages to the currently selected logical TreeNode that are already assigned to its children.
     */
    public void assignPagesFromChildren() {
        // TODO implement
        // assign all pages of this.selectedLogicalTreeNode's children to this.selectedLogicalTreeNode
    }

    /**
     * Assign all selected pages to the currently selected logical TreeNode.
     */
    public void assignPagesFromSelection() {
        // TODO implement
        // assign all pages within the selection of this.selectedFirstPageForAssignment and this.selectedLastPageForAssignment to this.selectedLogicalTreeNode
    }

    /**
     * Get all pages assigned to the currently selected logical TreeNode.
     * 
     * @return SelectItem array containing all assigned pages
     */
    public SelectItem[] getPagesOfSelectedLogicalTreeNode() {
        // TODO implement
        // get pages assigned to this.selectedLogicalTreeNode
        return new SelectItem[0];
    }

    /**
     * Assign selected pages to the currently selected logical TreeNode.
     */
    public void addPagesToLogicalTreeNode() {
        // TODO implement
        // assign pages in this.selectedPagesForAssignment to this.selectedLogicalTreeNode (they might already be assigned to that element)
    }

    /**
     * Remove selected pages from the currently selected logical TreeNode.
     */
    public void removePagesFromLogicalTreeNode() {
        // TODO implement
        // remove pages in this.selectedPagesForAssignment from this.selectedLogicalTreeNode
    }

    /**
     * TODO add javaDoc.
     * 
     * @param event TreeDragDropEvent triggered by logical node being dropped
     */
    public void onLogicalNodeDragDrop(TreeDragDropEvent event) {
        // TODO implement
    }

    /**
     * TODO add javaDoc.
     * 
     * @param event NodeSelectEvent triggered by logical node being selected
     */
    public void onLogicalNodeSelect(NodeSelectEvent event) {
        // TODO implement
    }

    /**
     * Create dummy images and paginate by configured default setting.
     */
    public void addNewImagesAndPaginate() {
        try {
            // TODO implement
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Apply the pagination settings to the selected pages.
     */
    public void applyPagination() {
        try {
            // TODO implement
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * TODO add javaDoc.
     */
    public void applyPaginationReadFromImages() {
        try {
            // TODO implement
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Copy the currently selected person.
     */
    public void copyPerson() {
        // TODO implement
        // copy person from this.selectedPerson
    }

    /**
     * Delete the currently selected person.
     */
    public void deletePerson() {
        // TODO implement
        // delete the person selected in this.selectedPerson
    }

    /**
     * Prepare form to insert a new person.
     */
    public void newPerson() {
        this.newPersonFirstName = "";
        this.newPersonLastName = "";
        this.newPersonRecord = ConfigCore.getParameter(ParameterCore.AUTHORITY_DEFAULT, "");
    }

    /**
     * Save the entered data as a new person.
     */
    public void savePerson() {
        try {
            // TODO implement
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Get allowed roles.
     *
     * @return list of allowed roles as SelectItems
     */
    public List<SelectItem> getAddableRoles() {
        // TODO implement
        // get list of all roles allowed for this.selectedLogicalTreeNode
        return new ArrayList<>();
    }

    /**
     * Get metadata.
     *
     * @return MetadataImpl object
     */
    public MetadataImpl getMetadata() {

        if (this.selectedMetadata == null) {
            getAddableMetadataTypes();
            if (Objects.nonNull(this.newMetadataList) && !this.newMetadataList.isEmpty()) {
                this.selectedMetadata = this.newMetadataList.get(0);
            }
        }
        return this.selectedMetadata;
    }

    /**
     * Copy the metadata.
     */
    public void copyMetadata() {
        try {
            // TODO implement
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Delete the metadata.
     */
    public void deleteMetadata() {
        // TODO implement
        // delete current metadata in this.selectedMetadata
    }

    public void saveMetadata() {
        // TODO implement
        // save metadata with this.selectedNewMetadataType and this.newMetadata.value to this.selectedLogicalTreeNode
    }

    /**
     * Get List of allowed addable metadata types for the currently selected logical TreeNode.
     * @return List of SelectItems containing the allowed types of metadata
     */
    public List<SelectItem> getAddableMetadataTypes() {
        // TODO implement
        // get possible types for metadata of this.selectedLogicalTreeNode
        return new ArrayList<>();
    }

    public String[] getCommentList() {
        try {
            refreshProcess(this.process);
            String wiki = this.process.getWikiField();
            if (!wiki.isEmpty()) {
                wiki = wiki.replace("</p>", "");
                String[] comments = wiki.split("<p>");
                if (comments[0].isEmpty()) {
                    List<String> list = new ArrayList<>(Arrays.asList(comments));
                    list.remove(list.get(0));
                    comments = list.toArray(new String[list.size()]);
                }
                return comments;
            }
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Mark the reported problem solved.
     */
    public void solveProblem(String comment) {
        BatchTaskHelper batchStepHelper = new BatchTaskHelper();
        batchStepHelper.solveProblemForSingle(ServiceManager.getProcessService().getCurrentTask(this.process));
        refreshProcess(this.process);
        String wikiField = this.process.getWikiField();
        wikiField = wikiField.replace(comment.trim(), comment.trim().replace("Red K", "Orange K "));
        ServiceManager.getProcessService().setWikiField(wikiField, this.process);
        try {
            ServiceManager.getProcessService().save(this.process);
        } catch (DataException e) {
            Helper.setErrorMessage("correctionSolveProblem", logger, e);
        }
        refreshProcess(this.process);
    }

    /**
     * Get a list of all previous tasks that could be used for correction purposes.
     * 
     * @return list of previous tasks
     */
    public List<Task> getPreviousStepsForCorrection() {
        refreshProcess(this.process);
        return ServiceManager.getTaskService().getPreviousTasksForProblemReporting(
                ServiceManager.getProcessService().getCurrentTask(this.process).getOrdering(), this.process.getId());
    }

    /**
     * Get number of previous tasks that could be used for correction purposes.
     * 
     * @return number of previous tasks
     */
    public int getNumberOfPreviousStepsForCorrection() {
        return getPreviousStepsForCorrection().size();
    }

    /**
     * Save new comment.
     */
    public void saveNewComment() {
        if (this.newComment != null && this.newComment.length() > 0) {
            String comment = ServiceManager.getUserService().getFullName(getCurrentUser()) + ": " + this.newComment;
            ServiceManager.getProcessService().addToWikiField(comment, this.process);
            this.newComment = "";
            try {
                ServiceManager.getProcessService().save(process);
                refreshProcess(process);
            } catch (DataException e) {
                Helper.setErrorMessage("errorReloading", new Object[] {Helper.getTranslation("wikiField") }, logger, e);
            }
        }
        showNewComment = false;
    }

    /**
     * Save new comment and set process to specified task for correction purposes.
     */
    public void saveNewCommentForCorrection() {
        List<Task> taskList = new ArrayList<>();
        taskList.add(ServiceManager.getProcessService().getCurrentTask(this.process));
        BatchTaskHelper batchStepHelper = new BatchTaskHelper(taskList);
        batchStepHelper.setProblem(this.problem);
        batchStepHelper.reportProblemForSingle();
        refreshProcess(this.process);
        this.showNewComment = false;
        this.newCommentIsCorrection = false;
        this.problem = new Problem();
    }

    /**
     * Get the list of image file paths for the current process.
     * 
     * @return List of fullsize PNG images
     */
    public List<String> getImageList() {
        // TODO implement
        return new ArrayList<>();
    }

    /**
     * Determines whether the list of images exists and contains at least one image path.
     * 
     * @return List of images
     */
    public boolean getImageListExistent() {
        // TODO implement
        // call method like getImages() (MetadataProcessor:2593) to get List of image paths for PNGs
        // return !getImages().isEmpty();
        return false;
    }

    /**
     * Get list of TIF directories available for this process.
     * 
     * @return List of URIs
     */
    public List<URI> getTifDirectoryList() {
        // TODO implement
        // get list of all available tif directories for this process
        return new ArrayList<>();
    }

    /**
     * TODO add javaDoc.
     */
    public void updateImageDirectory() {
        // TODO implement
    }

    /**
     * Get list of all logical structure elements for this process.
     * 
     * @return List of logical elements
     */
    public List<LegacyDocStructHelperInterface> getLogicalElementList() {
        // TODO implement
        // get list of all logical structure elements
        return new ArrayList<>();
    }

    /**
     * Get list of all pages allocated to the passed logical element.
     * 
     * @param logicalElement // TODO add param description
     * @return List of all allocated pages
     */
    public List<LegacyDocStructHelperInterface> getLogicalElementPageList(
            LegacyDocStructHelperInterface logicalElement) {
        // TODO implement
        // get list of all pages allocated to the logicalElement
        return new ArrayList<>();
    }

    /**
     * Get file path to png image for passed LegacyDocStructHelperInterface
     * 'page' representing a single scanned image.
     * 
     * @param page
     *            LegacyDocStructHelperInterface for which the corresponding png
     *            image file path is returned
     * @return File path to the png image
     */
    public String getPageImageFilePath(LegacyDocStructHelperInterface page) {
        // TODO implement
        // like getPageImageFilePath(LegacyDocStructHelperInterface
        // pageDocStruct) in MetadataProcessor:2560?
        return "";
    }

    /**
     * Get the physical page number for the passed
     * LegacyDocStructHelperInterface 'page'.
     * 
     * @param page
     *            LegacyDocStructHelperInterface which physical page number is
     *            returned
     * @return physical page number
     */
    public int getPhysicalPageNumber(LegacyDocStructHelperInterface page) {
        // TODO implement
        return 0;
    }

    /**
     * Get the logical page number for a paginated docstruct.
     * 
     * @param docStruct The DocStruct object
     * @return The logical page number
     */
    public String getLogicalPageNumber(LegacyDocStructHelperInterface docStruct) {
        // TODO implement
        return "";
    }

    /**
     * Get the path to the thumbnail for the with the passed image path.
     * 
     * @param imagePath Path to the fullsize PNG image
     * @return Path to the thumnail PNG image
     */
    public String getThumbnail(String imagePath) {
        File imageFile = new File(imagePath);
        String filename = imageFile.getName();
        return imagePath.replace(FULLSIZE_FOLDER_NAME, THUMBNAIL_FOLDER_NAME).replace(filename, "thumbnail." + filename);
    }

    /**
     * Handle event of page being dragged and dropped.
     * @param event TODO add param description
     */
    public void onPageDrop(DragDropEvent event) {
        // TODO implement
        // like method onPageDrop(DragDropEvent dragDropEvent) in MetadataProcessor:2453?
    }

    /**
     * Checks and returns whether access is granted to the image with the given filepath "imagePath".
     *
     * @param imagePath the filepath of the image for which access rights are checked
     * @return true if access is granted and false otherwise
     */
    public boolean getImageAccessible(String imagePath) {
        // TODO implement
        // like method isAccessGranted(String imagePath) in MetadataProcessor:2643?
        return false;
    }

    /**
     * Get current user.
     *
     * @return User
     */
    public User getCurrentUser() {
        if (this.user == null) {
            this.user = ServiceManager.getUserService().getAuthenticatedUser();
        }
        return this.user;
    }

    /**
     * Refresh the process in the session.
     *
     * @param process
     *            Object process to refresh
     */
    private void refreshProcess(Process process) {
        try {
            if (process.getId() != 0) {
                ServiceManager.getProcessService().refresh(process);
                this.process = ServiceManager.getProcessService().getById(process.getId());
            }

        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find process with ID " + process.getId(), logger, e);
        }
    }

    /**
     * Get logger.
     *
     * @return value of logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Get process.
     *
     * @return value of process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Sets the ID of the process whose meta-data file is to be edited. This
     * method must be called using a {@code setPropertyActionListener} before
     * the meta-data editor is opened.
     *
     * @param process
     *            whose meta-data file is to be edited
     */
    public void setProcessId(int processId) {
        this.processId = processId;
    }

    /**
     * Get user.
     *
     * @return value of user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user running the editor. This method must be called using a
     * {@code setPropertyActionListener} before the meta-data editor is opened
     * to set the user.
     *
     * @param user
     *            user object
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get referringView.
     *
     * @return value of referringView
     */
    public String getReferringView() {
        return referringView;
    }

    /**
     * Sets the referring view. This method must be called using a
     * {@code setPropertyActionListener} before the meta-data editor is opened
     * to set the JSF view the user shall return to when he or she closes the
     * editor, or when opening fails.
     *
     * @param referringView
     *            view to return to
     */
    public void setReferringView(String referringView) {
        this.referringView = referringView;
    }

    /**
     * Get galleryViewMode.
     *
     * @return value of galleryViewMode
     */
    public String getGalleryViewMode() {
        return galleryViewMode;
    }

    /**
     * Set galleryViewMode.
     *
     * @param galleryViewMode as java.lang.String
     */
    public void setGalleryViewMode(String galleryViewMode) {
        this.galleryViewMode = galleryViewMode;
    }

    /**
     * Get showPagination.
     *
     * @return value of showPagination
     */
    public boolean isShowPagination() {
        return showPagination;
    }

    /**
     * Set showPagination.
     *
     * @param showPagination as boolean
     */
    public void setShowPagination(boolean showPagination) {
        this.showPagination = showPagination;
    }

    /**
     * Get showNewComment.
     *
     * @return value of showNewComment
     */
    public boolean isShowNewComment() {
        return showNewComment;
    }

    /**
     * Set showNewComment.
     *
     * @param showNewComment as boolean
     */
    public void setShowNewComment(boolean showNewComment) {
        this.showNewComment = showNewComment;
    }

    /**
     * Get allPages.
     *
     * @return value of allPages
     */
    public String[] getAllPages() {
        return allPages;
    }

    /**
     * Set allPages.
     *
     * @param allPages as java.lang.String[]
     */
    public void setAllPages(String[] allPages) {
        this.allPages = allPages;
    }

    /**
     * Get selectedPages.
     *
     * @return value of selectedPages
     */
    public String[] getSelectedPages() {
        return selectedPages;
    }

    /**
     * Set selectedPages.
     *
     * @param selectedPages as java.lang.String[]
     */
    public void setSelectedPages(String[] selectedPages) {
        this.selectedPages = selectedPages;
    }

    /**
     * Get selectedFirstPage.
     *
     * @return value of selectedFirstPage
     */
    public String getSelectedFirstPage() {
        return selectedFirstPage;
    }

    /**
     * Set selectedFirstPage.
     *
     * @param selectedFirstPage as java.lang.String
     */
    public void setSelectedFirstPage(String selectedFirstPage) {
        this.selectedFirstPage = selectedFirstPage;
    }

    /**
     * Get selectedLastPage.
     *
     * @return value of selectedLastPage
     */
    public String getSelectedLastPage() {
        return selectedLastPage;
    }

    /**
     * Set selectedLastPage.
     *
     * @param selectedLastPage as java.lang.String
     */
    public void setSelectedLastPage(String selectedLastPage) {
        this.selectedLastPage = selectedLastPage;
    }

    /**
     * Get numberOfImagesToAdd.
     *
     * @return value of numberOfImagesToAdd
     */
    public int getNumberOfImagesToAdd() {
        return numberOfImagesToAdd;
    }

    /**
     * Set numberOfImagesToAdd.
     *
     * @param numberOfImagesToAdd as int
     */
    public void setNumberOfImagesToAdd(int numberOfImagesToAdd) {
        this.numberOfImagesToAdd = numberOfImagesToAdd;
    }

    /**
     * Get paginator.
     *
     * @return value of paginator
     */
    public Paginator getPaginator() {
        return paginator;
    }

    /**
     * Set paginator.
     *
     * @param paginator as org.kitodo.production.metadata.pagination.Paginator
     */
    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

    /**
     * Get paginationValue.
     *
     * @return value of paginationValue
     */
    public String getPaginationValue() {
        return paginationValue;
    }

    /**
     * Set paginationValue.
     *
     * @param paginationValue as java.lang.String
     */
    public void setPaginationValue(String paginationValue) {
        this.paginationValue = paginationValue;
    }

    /**
     * Get paginationFictitious.
     *
     * @return value of paginationFictitious
     */
    public boolean isPaginationFictitious() {
        return paginationFictitious;
    }

    /**
     * Set paginationFictitious.
     *
     * @param paginationFictitious as boolean
     */
    public void setPaginationFictitious(boolean paginationFictitious) {
        this.paginationFictitious = paginationFictitious;
    }

    /**
     * Get logicalStructure.
     *
     * @return value of logicalStructure
     */
    public TreeNode getLogicalStructure() {
        return logicalStructure;
    }

    /**
     * Set logicalStructure.
     *
     * @param logicalStructure as org.primefaces.model.TreeNode
     */
    public void setLogicalStructure(TreeNode logicalStructure) {
        this.logicalStructure = logicalStructure;
    }

    /**
     * Get selectedLogicalTreeNode.
     *
     * @return value of selectedLogicalTreeNode
     */
    public TreeNode getSelectedLogicalTreeNode() {
        return selectedLogicalTreeNode;
    }

    /**
     * Set selectedLogicalTreeNode.
     *
     * @param selectedLogicalTreeNode as org.primefaces.model.TreeNode
     */
    public void setSelectedLogicalTreeNode(TreeNode selectedLogicalTreeNode) {
        this.selectedLogicalTreeNode = selectedLogicalTreeNode;
    }

    /**
     * Get selectedNewLogicalPosition.
     *
     * @return value of selectedNewLogicalPosition
     */
    public PositionOfNewDocStrucElement getSelectedNewLogicalPosition() {
        return selectedNewLogicalPosition;
    }

    /**
     * Set selectedNewLogicalPosition.
     *
     * @param selectedNewLogicalPosition as org.kitodo.production.enums.PositionOfNewDocStrucElement
     */
    public void setSelectedNewLogicalPosition(PositionOfNewDocStrucElement selectedNewLogicalPosition) {
        this.selectedNewLogicalPosition = selectedNewLogicalPosition;
    }

    /**
     * Get selectedNewLogicalType.
     *
     * @return value of selectedNewLogicalType
     */
    public String getSelectedNewLogicalType() {
        return selectedNewLogicalType;
    }

    /**
     * Set selectedNewLogicalType.
     *
     * @param selectedNewLogicalType as java.lang.String
     */
    public void setSelectedNewLogicalType(String selectedNewLogicalType) {
        this.selectedNewLogicalType = selectedNewLogicalType;
    }

    /**
     * Get addMultipleLogicalElements.
     *
     * @return value of addMultipleLogicalElements
     */
    public boolean isAddMultipleLogicalElements() {
        return addMultipleLogicalElements;
    }

    /**
     * Set addMultipleLogicalElements.
     *
     * @param addMultipleLogicalElements as boolean
     */
    public void setAddMultipleLogicalElements(boolean addMultipleLogicalElements) {
        this.addMultipleLogicalElements = addMultipleLogicalElements;
    }

    /**
     * Get numberOfLogicalElements.
     *
     * @return value of numberOfLogicalElements
     */
    public int getNumberOfLogicalElements() {
        return numberOfLogicalElements;
    }

    /**
     * Set numberOfLogicalElements.
     *
     * @param numberOfLogicalElements as int
     */
    public void setNumberOfLogicalElements(int numberOfLogicalElements) {
        this.numberOfLogicalElements = numberOfLogicalElements;
    }

    /**
     * Get selectedMetadataType.
     *
     * @return value of selectedMetadataType
     */
    public String getSelectedMetadataType() {
        return selectedMetadataType;
    }

    /**
     * Set selectedMetadataType.
     *
     * @param selectedMetadataType as java.lang.String
     */
    public void setSelectedMetadataType(String selectedMetadataType) {
        this.selectedMetadataType = selectedMetadataType;
    }

    /**
     * Get metadataValue.
     *
     * @return value of metadataValue
     */
    public String getMetadataValue() {
        return metadataValue;
    }

    /**
     * Set metadataValue.
     *
     * @param metadataValue as java.lang.String
     */
    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    /**
     * Get selectedFirstPageForAssignment.
     *
     * @return value of selectedFirstPageForAssignment
     */
    public String getSelectedFirstPageForAssignment() {
        return selectedFirstPageForAssignment;
    }

    /**
     * Set selectedFirstPageForAssignment.
     *
     * @param selectedFirstPageForAssignment as java.lang.String
     */
    public void setSelectedFirstPageForAssignment(String selectedFirstPageForAssignment) {
        this.selectedFirstPageForAssignment = selectedFirstPageForAssignment;
    }

    /**
     * Get selectedLastPageForAssignment.
     *
     * @return value of selectedLastPageForAssignment
     */
    public String getSelectedLastPageForAssignment() {
        return selectedLastPageForAssignment;
    }

    /**
     * Set selectedLastPageForAssignment.
     *
     * @param selectedLastPageForAssignment as java.lang.String
     */
    public void setSelectedLastPageForAssignment(String selectedLastPageForAssignment) {
        this.selectedLastPageForAssignment = selectedLastPageForAssignment;
    }

    /**
     * Get selectedPagesForAssignment.
     *
     * @return value of selectedPagesForAssignment
     */
    public String[] getSelectedPagesForAssignment() {
        return selectedPagesForAssignment;
    }

    /**
     * Set selectedPagesForAssignment.
     *
     * @param selectedPagesForAssignment as java.lang.String[]
     */
    public void setSelectedPagesForAssignment(String[] selectedPagesForAssignment) {
        this.selectedPagesForAssignment = selectedPagesForAssignment;
    }

    /**
     * Get selectedPagesOfSelectedLogicalTreeNode.
     *
     * @return value of selectedPagesOfSelectedLogicalTreeNode
     */
    public String[] getSelectedPagesOfSelectedLogicalTreeNode() {
        return selectedPagesOfSelectedLogicalTreeNode;
    }

    /**
     * Set selectedPagesOfSelectedLogicalTreeNode.
     *
     * @param selectedPagesOfSelectedLogicalTreeNode as java.lang.String[]
     */
    public void setSelectedPagesOfSelectedLogicalTreeNode(String[] selectedPagesOfSelectedLogicalTreeNode) {
        this.selectedPagesOfSelectedLogicalTreeNode = selectedPagesOfSelectedLogicalTreeNode;
    }

    /**
     * Get metaPersonList.
     *
     * @return value of metaPersonList
     */
    public List<Void> getMetaPersonList() {
        return metaPersonList;
    }

    /**
     * Set metaPersonList.
     *
     * @param metaPersonList as java.util.List<org.kitodo.production.metadata.Void>
     */
    public void setMetaPersonList(List<Void> metaPersonList) {
        this.metaPersonList = metaPersonList;
    }

    /**
     * Get selectedPerson.
     *
     * @return value of selectedPerson
     */
    public Void getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * Set selectedPerson.
     *
     * @param selectedPerson as org.kitodo.production.metadata.Void
     */
    public void setSelectedPerson(Void selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    /**
     * Get newPersonFirstName.
     *
     * @return value of newPersonFirstName
     */
    public String getNewPersonFirstName() {
        return newPersonFirstName;
    }

    /**
     * Set newPersonFirstName.
     *
     * @param newPersonFirstName as java.lang.String
     */
    public void setNewPersonFirstName(String newPersonFirstName) {
        this.newPersonFirstName = newPersonFirstName;
    }

    /**
     * Get newPersonLastName.
     *
     * @return value of newPersonLastName
     */
    public String getNewPersonLastName() {
        return newPersonLastName;
    }

    /**
     * Set newPersonLastName.
     *
     * @param newPersonLastName as java.lang.String
     */
    public void setNewPersonLastName(String newPersonLastName) {
        this.newPersonLastName = newPersonLastName;
    }

    /**
     * Get newPersonRecord.
     *
     * @return value of newPersonRecord
     */
    public String getNewPersonRecord() {
        return newPersonRecord;
    }

    /**
     * Set newPersonRecord.
     *
     * @param newPersonRecord as java.lang.String
     */
    public void setNewPersonRecord(String newPersonRecord) {
        this.newPersonRecord = newPersonRecord;
    }

    /**
     * Get newPersonRole.
     *
     * @return value of newPersonRole
     */
    public String getNewPersonRole() {
        return newPersonRole;
    }

    /**
     * Set newPersonRole.
     *
     * @param newPersonRole as java.lang.String
     */
    public void setNewPersonRole(String newPersonRole) {
        this.newPersonRole = newPersonRole;
    }

    /**
     * Get selectedLogicalTreeNodeMetadataList.
     *
     * @return value of selectedLogicalTreeNodeMetadataList
     */
    public List<MetadataImpl> getSelectedLogicalTreeNodeMetadataList() {
        return selectedLogicalTreeNodeMetadataList;
    }

    /**
     * Set selectedLogicalTreeNodeMetadataList.
     *
     * @param selectedLogicalTreeNodeMetadataList as java.util.List<org.kitodo.production.metadata.MetadataImpl>
     */
    public void setSelectedLogicalTreeNodeMetadataList(List<MetadataImpl> selectedLogicalTreeNodeMetadataList) {
        this.selectedLogicalTreeNodeMetadataList = selectedLogicalTreeNodeMetadataList;
    }

    /**
     * Get selectedMetadata.
     *
     * @return value of selectedMetadata
     */
    public MetadataImpl getSelectedMetadata() {
        return selectedMetadata;
    }

    /**
     * Set selectedMetadata.
     *
     * @param selectedMetadata as org.kitodo.production.metadata.MetadataImpl
     */
    public void setSelectedMetadata(MetadataImpl selectedMetadata) {
        this.selectedMetadata = selectedMetadata;
    }

    /**
     * Get selectedNewMetadataType.
     *
     * @return value of selectedNewMetadataType
     */
    public String getSelectedNewMetadataType() {
        return selectedNewMetadataType;
    }

    /**
     * Set selectedNewMetadataType.
     *
     * @param selectedNewMetadataType as java.lang.String
     */
    public void setSelectedNewMetadataType(String selectedNewMetadataType) {
        this.selectedNewMetadataType = selectedNewMetadataType;
    }

    /**
     * Get newMetadata.
     *
     * @return value of newMetadata
     */
    public MetadataImpl getNewMetadata() {
        return newMetadata;
    }

    /**
     * Set newMetadata.
     *
     * @param newMetadata as org.kitodo.production.metadata.MetadataImpl
     */
    public void setNewMetadata(MetadataImpl newMetadata) {
        this.newMetadata = newMetadata;
    }

    /**
     * Get newMetadataList.
     *
     * @return value of newMetadataList
     */
    public List<MetadataImpl> getNewMetadataList() {
        return newMetadataList;
    }

    /**
     * Set newMetadataList.
     *
     * @param newMetadataList as java.util.List<org.kitodo.production.metadata.MetadataImpl>
     */
    public void setNewMetadataList(List<MetadataImpl> newMetadataList) {
        this.newMetadataList = newMetadataList;
    }

    /**
     * Get newCommentIsCorrection.
     *
     * @return value of newCommentIsCorrection
     */
    public boolean isNewCommentIsCorrection() {
        return newCommentIsCorrection;
    }

    /**
     * Set newCommentIsCorrection.
     *
     * @param newCommentIsCorrection as boolean
     */
    public void setNewCommentIsCorrection(boolean newCommentIsCorrection) {
        this.newCommentIsCorrection = newCommentIsCorrection;
    }

    /**
     * Get newComment.
     *
     * @return value of newComment
     */
    public String getNewComment() {
        return newComment;
    }

    /**
     * Set newComment.
     *
     * @param newComment as java.lang.String
     */
    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    /**
     * Get problem.
     *
     * @return value of problem
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Set problem.
     *
     * @param problem as org.kitodo.production.workflow.Problem
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    /**
     * Get pageIndex.
     *
     * @return value of pageIndex
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Set pageIndex.
     *
     * @param pageIndex as int
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Get numberOfPngImages.
     *
     * @return value of numberOfPngImages
     */
    public int getNumberOfPngImages() {
        return numberOfPngImages;
    }

    /**
     * Set numberOfPngImages.
     *
     * @param numberOfPngImages as int
     */
    public void setNumberOfPngImages(int numberOfPngImages) {
        this.numberOfPngImages = numberOfPngImages;
    }

    /**
     * Get numberOfTifImages.
     *
     * @return value of numberOfTifImages
     */
    public int getNumberOfTifImages() {
        return numberOfTifImages;
    }

    /**
     * Set numberOfTifImages.
     *
     * @param numberOfTifImages as int
     */
    public void setNumberOfTifImages(int numberOfTifImages) {
        this.numberOfTifImages = numberOfTifImages;
    }

    /**
     * Get selectedImage.
     *
     * @return value of selectedImage
     */
    public String getSelectedImage() {
        return selectedImage;
    }

    /**
     * Set selectedImage.
     *
     * @param selectedImage as java.lang.String
     */
    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

    /**
     * Get selectedTifDirectory.
     *
     * @return value of selectedTifDirectory
     */
    public URI getSelectedTifDirectory() {
        return selectedTifDirectory;
    }

    /**
     * Set selectedTifDirectory.
     *
     * @param selectedTifDirectory as java.net.URI
     */
    public void setSelectedTifDirectory(URI selectedTifDirectory) {
        this.selectedTifDirectory = selectedTifDirectory;
    }

}
