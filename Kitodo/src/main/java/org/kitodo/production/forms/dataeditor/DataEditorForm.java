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
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Structure;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.filemanagement.LockResult;
import org.kitodo.api.filemanagement.LockingMode;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.enums.PositionOfNewDocStrucElement;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.metadata.MetadataImpl;
import org.kitodo.production.metadata.pagination.Paginator;
import org.kitodo.production.metadata.pagination.enums.Mode;
import org.kitodo.production.metadata.pagination.enums.Scope;
import org.kitodo.production.metadata.pagination.enums.Type;
import org.kitodo.production.services.ServiceManager;
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
    private static final Logger logger = LogManager.getLogger(DataEditorForm.class);

    /**
     * A filter on the rule set depending on the workflow step. So far this is
     * not configurable anywhere and is therefore on “edit”.
     */
    private String acquisitionStage = "edit";

    /**
     * Backing bean for the comment panel.
     */
    private final CommentPanel commentPanel;

    /**
     * Backing bean for the gallery panel.
     */
    private final GalleryPanel galleryPanel;

    /**
     * All file system locks that the user is currently holding.
     */
    private LockResult locks;

    /**
     * The path to the main file, to save it later.
     */
    private URI mainFileUri;

    private final MetadataPanel metadataPanel;

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

    private String referringView = "desktop";

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

    private boolean showPagination = false;

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

    /**
     * Public constructor.
     */
    public DataEditorForm() {
        this.structurePanel = new StructurePanel(this);
        this.metadataPanel = new MetadataPanel(this);
        this.galleryPanel = new GalleryPanel(this);
        this.commentPanel = new CommentPanel(this);
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
            if (Objects.nonNull(locks)) {
                locks.close();
            }
            this.referringView = referringView;
            Helper.getRequestParameter("referringView");
            this.process = ServiceManager.getProcessService().getById(id);
            this.user = ServiceManager.getUserService().getCurrentUser();

            ruleset = openRulesetFile(process.getTemplate().getRuleset().getFile());
            if (!openMetsFile("meta.xml")) {
                return referringView;
            }
            init();
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

        locks = ServiceManager.getFileService().tryLock(mainFileUri, LockingMode.EXCLUSIVE);
        if (!locks.isSuccessful()) {
            Helper.setErrorMessage("cannotObtainLock", String.join(" ; ", locks.getConflicts().get(mainFileUri)));
            return locks.isSuccessful();
        }

        try (InputStream in = ServiceManager.getFileService().read(mainFileUri, locks)) {
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

    private void init() throws IOException {
        final long begin = System.nanoTime();

        structurePanel.show(workpiece);
        metadataPanel.show(structurePanel.getSelectedStructure());
        galleryPanel.show(workpiece);
        commentPanel.show(workpiece);

        if (logger.isTraceEnabled()) {
            logger.trace("Initializing editor beans took {} ms",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * Releases the locks and clears all remaining content from the data editor
     * form.
     *
     * @return the referring view, to return there
     */
    public String close() {
        try {
            locks.close();
            locks = null;

            commentPanel.clear();
            metadataPanel.clear();
            structurePanel.clear();
            workpiece = null;
            mainFileUri = null;
            ruleset = null;
            process = null;
            user = null;
            return referringView;
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return null;
        }
    }

    /**
     * Ensures that all locks are released when the user leaves the meta-data
     * editor in an unusual way.
     */
    @Override
    protected void finalize() throws Throwable {
        if (locks != null) {
            try {
                locks.close();
            } catch (Throwable any) {
                /* make sure finalize() can run through */
            }
        }
        super.finalize();
    }

    /**
     * Validate the structure and metadata.
     */
    public void validate() {
        try {
            ValidationResult validationResult = ServiceManager.getMetadataValidationService().validate(workpiece,
                ruleset);
            State state = validationResult.getState();
            if (!State.ERROR.equals(state)) {
                Helper.setMessage("dataEditor.validation.state.".concat(state.toString().toLowerCase()));
                for (String message : validationResult.getResultMessages()) {
                    Helper.setMessage(message);
                }
            } else {
                Helper.setErrorMessage("dataEditor.validation.state.error");
                for (String message : validationResult.getResultMessages()) {
                    Helper.setErrorMessage(message);
                }
            }
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Save the structure and metadata.
     *
     * @return navigation target
     */
    public String save() {
        try {
            metadataPanel.preserve();
            structurePanel.preserve();
            try (OutputStream out = ServiceManager.getFileService().write(mainFileUri, locks)) {
                ServiceManager.getMetsService().save(workpiece, out);
            }
            close();
            return referringView;
        } catch (Exception e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return null;
        }

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

    public CommentPanel getCommentPanel() {
        return commentPanel;
    }

    public GalleryPanel getGalleryPanel() {
        return galleryPanel;
    }

    public MetadataPanel getMetadataPanel() {
        return metadataPanel;
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

    void setProcess(Process process) {
        this.process = process;
    }

    void switchTheMetadataPanelTo(Structure structure)
            throws InvalidMetadataValueException, NoSuchMetadataFieldException {
        metadataPanel.preserve();
        metadataPanel.show(structure);
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
     * Get logger.
     *
     * @return value of logger
     */
    public static Logger getLogger() {
        return logger;
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

    LockResult getLocks() {
        return this.locks;
    }
}
