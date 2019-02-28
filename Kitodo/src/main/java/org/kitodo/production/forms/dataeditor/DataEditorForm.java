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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
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
import org.kitodo.production.metadata.pagination.enums.Mode;
import org.kitodo.production.metadata.pagination.enums.Scope;
import org.kitodo.production.metadata.pagination.enums.Type;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.workflow.Problem;
import org.primefaces.event.DragDropEvent;
import org.primefaces.model.TreeNode;

@Named("DataEditorForm")
@SessionScoped
public class DataEditorForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Indicates to JSF to navigate to the web page containing the meta-data
     * editor.
     */
    private static final String PAGE_METADATA_EDITOR = "/pages/metadataEditor?faces-redirect=true";
    private static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    private static final String FULLSIZE_FOLDER_NAME = "fullsize";
    private static final Logger logger = LogManager.getLogger(DataEditorForm.class);

    /**
     * A filter on the rule set depending on the workflow step. So far this is
     * not configurable anywhere and is therefore on “edit”.
     */
    private String acquisitionStage = "edit";

    /**
     * All file system locks that the user is currently holding.
     */
    private LockResult lock;

    /**
     * The path to the main file, to save it later.
     */
    private URI mainFileUri;

    /**
     * The language preference list of the editing user for displaying the
     * meta-data labels. We cache this because it’s used thousands of times and
     * otherwise the access would always go through the search engine, which
     * would delay page creation.
     */
    private List<LanguageRange> priorityList;

    /**
     * Process whose workpiece is under edit.
     */
    private Process process;

    /**
     * The ruleset that the file is based on.
     */
    private RulesetManagementInterface ruleset;

    /**
     * Backing bean for the structure panel.
     */
    private final StructurePanel structurePanel;

    /**
     * User sitting in front of the editor.
     */
    private User user;

    /**
     * The file content.
     */
    private Workpiece workpiece;

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
    private String selectedImage;

    /**
     * ID of the process to open. The ID must be set previous to calling
     * {@link #open()} by using a {@code setPropertyActionListener}.
     */
    private int processId;

    /**
     * Public constructor.
     */
    public DataEditorForm() {
        this.structurePanel = new StructurePanel(this);
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
    public String open(int id, String referringView) {
        try {
            if (Objects.nonNull(lock)) {
                lock.close();
            }
            this.referringView = referringView;
            Helper.getRequestParameter("referringView");
            this.process = ServiceManager.getProcessService().getById(id);
            this.user = ServiceManager.getUserService().getCurrentUser();

            ruleset = openRulesetFile(process.getTemplate().getRuleset().getFile());
            if (!openMetsFile("meta.xml")) {
                return referringView;
            }
            populatePanels();
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return referringView;
        }
        return PAGE_METADATA_EDITOR;
    }

    /**
     * Opens the METS file.
     *
     * @param fileName
     *            file name to open
     * @return whether successful. False, if the file cannot be locked.
     * @throws URISyntaxException
     *             if the file URI cannot be built (due to invalid characters in
     *             the directory path)
     * @throws IOException
     *             if filesystem I/O fails
     */
    private boolean openMetsFile(String fileName) throws URISyntaxException, IOException {
        final long begin = System.nanoTime();
        URI workPathUri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
        String workDirectoryPath = workPathUri.getPath();
        mainFileUri = new URI(workPathUri.getScheme(), workPathUri.getUserInfo(), workPathUri.getHost(),
                workPathUri.getPort(), workDirectoryPath.endsWith("/") ? workDirectoryPath.concat(fileName)
                        : workDirectoryPath + '/' + fileName,
                workPathUri.getQuery(), null);

        lock = ServiceManager.getFileService().tryLock(mainFileUri, LockingMode.EXCLUSIVE);
        if (!lock.isSuccessful()) {
            Helper.setErrorMessage("cannotObtainLock", String.join(" ; ", lock.getConflicts().get(mainFileUri)));
            return lock.isSuccessful();
        }

        try (InputStream in = ServiceManager.getFileService().read(mainFileUri, lock)) {
            workpiece = ServiceManager.getMetsService().load(in);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Reading METS took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        ServiceManager.getFileService().searchForMedia(process, workpiece);
        return true;
    }

    private RulesetManagementInterface openRulesetFile(String fileName) throws IOException {
        final long begin = System.nanoTime();
        String metadataLanguage = user.getMetadataLanguage();
        priorityList = LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
        RulesetManagementInterface ruleset = ServiceManager.getRulesetManagementService().getRulesetManagement();
        ruleset.load(new File(FilenameUtils.concat(ConfigCore.getParameter(ParameterCore.DIR_RULESETS), fileName)));
        if (logger.isTraceEnabled()) {
            logger.trace("Reading ruleset took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return ruleset;
    }

    private void populatePanels() {
        final long begin = System.nanoTime();
        structurePanel.show(workpiece);
        if (logger.isTraceEnabled()) {
            logger.trace("Populating panels took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
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
     * Returns the acquisition stage, so that the individual panels can access
     * it.
     *
     * @return the acquisition stage
     */
    String getAcquisitionStage() {
        return acquisitionStage;
    }

    /**
     * Returns the language preference list of the editing user, so that the
     * individual panels can access it.
     *
     * @return the language preference list
     */

    List<LanguageRange> getPriorityList() {
        return priorityList;
    }

    /**
     * Returns the rule set, so that the individual panels can access it.
     *
     * @return the rule set
     */
    RulesetManagementInterface getRuleset() {
        return ruleset;
    }

    public StructurePanel getStructurePanel() {
        return structurePanel;
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
     * Get metadata value.
     *
     * @return String object
     */
    public String getMetadataImplValue() {
        MetadataImpl metadataImpl = getMetadata();
        return metadataImpl != null ? metadataImpl.getValue() : "";
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
    Process getProcess() {
        return process;
    }

    /**
     * Get process title.
     *
     * @return value of process title
     */
    public String getProcessTitle() {
        return process.getTitle();
    }

    /**
     * Sets the ID of the process whose meta-data file is to be edited. This
     * method must be called using a {@code setPropertyActionListener} before
     * the meta-data editor is opened.
     *
     * @param processId
     *            ID of the process whose meta-data file is to be edited
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
     * Get pagination mode.
     *
     * @return value of pagination mode
     */
    public Mode getPaginationMode() {
        return paginator.getPaginationMode();
    }

    /**
     * Set paginationMode.
     *
     * @param paginationMode
     *            as org.kitodo.production.metadata.pagination.enums.Mode
     */
    public void setPaginationMode(Mode paginationMode) {
        paginator.setPaginationMode(paginationMode);
    }

    /**
     * Get pagination modes.
     *
     * @return value of pagination modes
     */
    public Mode[] getPaginationModes() {
        return paginator.getPaginationModes();
    }

    /**
     * Get pagination scope.
     *
     * @return value of pagination scope
     */
    public Scope getPaginationScope() {
        return paginator.getPaginationScope();
    }

    /**
     * Set paginationScope.
     *
     * @param paginationMode
     *            as org.kitodo.production.metadata.pagination.enums.Scope
     */
    public void setPaginationScope(Scope paginationScope) {
        paginator.setPaginationScope(paginationScope);
    }

    /**
     * Get pagination scopes.
     *
     * @return value of pagination scopes
     */
    public Scope[] getPaginationScopes() {
        return paginator.getPaginationScopes();
    }

    /**
     * Get pagination type.
     *
     * @return value of pagination type
     */
    public Type getPaginationType() {
        return paginator.getPaginationType();
    }

    /**
     * Set paginationType.
     *
     * @param paginationType
     *            as org.kitodo.production.metadata.pagination.enums.Type
     */
    public void setPaginationType(Type paginationType) {
        paginator.setPaginationType(paginationType);
    }

    /**
     * Get pagination types.
     *
     * @return value of pagination types
     */
    public Type[] getPaginationTypes() {
        return paginator.getPaginationTypes();
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
     * Get problem ID.
     *
     * @return value of problem ID
     */
    public Integer getProblemId() {
        return problem.getId();
    }

    /**
     * Get problem message.
     *
     * @return value of problem message
     */
    public String getProblemMessage() {
        return problem.getMessage();
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
     * Ensures that all locks are released when the user leaves the meta-data
     * editor in an unusual way.
     */
    @Override
    protected void finalize() throws Throwable {
        if (lock != null) {
            try {
                lock.close();
            } catch (Throwable any) {
                /* make sure finalize() can run through */
            }
        }
        super.finalize();
    }
}
