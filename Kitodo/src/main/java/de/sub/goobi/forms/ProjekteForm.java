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

package de.sub.goobi.forms;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.results.ChartDraw.ChartType;
import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.goobi.production.chart.IProjectTask;
import org.goobi.production.chart.IProvideProjectTaskList;
import org.goobi.production.chart.ProjectStatusDataTable;
import org.goobi.production.chart.ProjectStatusDraw;
import org.goobi.production.chart.WorkflowProjectTaskList;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.StatQuestProjectProgressData;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.services.ServiceManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Named("ProjekteForm")
@SessionScoped
public class ProjekteForm extends BasisForm {
    private static final long serialVersionUID = 6735912903249358786L;
    private static final Logger logger = LogManager.getLogger(ProjekteForm.class);

    private static final HashMap<String, String> configurationFiles;
    static {
        configurationFiles = new HashMap<>();
        configurationFiles.put("PROJECT", "kitodo_projects.xml");
        configurationFiles.put("DISPLAY_RULES", "kitodo_metadataDisplayRules.xml");
        configurationFiles.put("DIGITAL_COLLECTIONS", "kitodo_digitalCollections.xml");
    }

    private String currentConfigurationFile = "";

    private Project myProjekt = new Project();
    private ProjectFileGroup myFilegroup;
    private transient ServiceManager serviceManager = new ServiceManager();

    // lists accepting the preliminary actions of adding and delting filegroups
    // it needs the execution of commit fileGroups to make these changes
    // permanent
    private List<Integer> newFileGroups = new ArrayList<>();
    private List<Integer> deletedFileGroups = new ArrayList<>();

    private StatisticsManager statisticsManagerForProduction = null;
    private StatisticsManager statisticsManagerForThroughput = null;
    private StatisticsManager statisticsManagerForCorrections = null;
    private StatisticsManager statisticsManagerForStorage = null;
    private final StatQuestProjectProgressData projectProgressData = new StatQuestProjectProgressData();

    private String projectProgressImage;
    private String projectStatImages;
    private String projectStatVolumes;
    private boolean showStatistics;

    private int itemId;

    private String xmlConfigurationString = "";

    private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder = null;

    public ProjekteForm() {
        super();
    }

    // making sure its cleaned up
    @Override
    protected void finalize() {
        this.cancel();
    }

    /**
     * this method deletes filegroups by their id's in the list.
     *
     * @param fileGroups
     *            List
     */
    private void deleteFileGroups(List<Integer> fileGroups) {
        for (Integer id : fileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getProjectFileGroups()) {
                if (f.getId() == null ? id == null : f.getId().equals(id)) {
                    this.myProjekt.getProjectFileGroups().remove(f);
                    break;
                }
            }
        }
    }

    /**
     * this method flushes the newFileGroups List, thus makes them permanent and
     * deletes those marked for deleting, making the removal permanent.
     */
    private void commitFileGroups() {
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // deleting the fileGroups marked for deletion
        deleteFileGroups(this.deletedFileGroups);
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
    }

    /**
     * this needs to be executed in order to rollback adding of filegroups.
     */
    public String cancel() {
        // flushing new fileGroups
        deleteFileGroups(this.newFileGroups);
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<>();
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<>();
        this.projectProgressImage = null;
        this.projectStatImages = null;
        this.projectStatVolumes = null;
        return "/pages/ProjekteAlle";
    }

    /**
     * Create new project.
     *
     * @return page address
     */
    public String newProject() {
        this.myProjekt = new Project();
        this.itemId = 0;
        return "/pages/ProjekteBearbeiten?faces-redirect=true";
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String save() {
        // call this to make saving and deleting permanent
        this.commitFileGroups();
        try {
            serviceManager.getProjectService().save(this.myProjekt);
            return filterKein();
        } catch (DataException e) {
            Helper.setFehlerMeldung("Project could not be save: ", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    /**
     * Apply.
     *
     * @return String
     */
    public String apply() {
        // call this to make saving and deleting permanent
        logger.trace("apply wird aufgerufen...");
        this.commitFileGroups();
        try {
            serviceManager.getProjectService().save(this.myProjekt);
            return null;
        } catch (DataException e) {
            Helper.setFehlerMeldung("Project could not be save: ", e.getMessage());
            logger.error(e);
            return null;
        }
    }

    /**
     * Remove.
     *
     * @return String
     */
    public String delete() {
        if (this.myProjekt.getUsers().size() > 0) {
            Helper.setFehlerMeldung("userAssignedError");
            return null;
        } else {
            try {
                serviceManager.getProjectService().remove(this.myProjekt);
            } catch (DataException e) {
                Helper.setFehlerMeldung("Project could not be delete: ", e.getMessage());
                logger.error(e);
                return null;
            }
        }
        return filterKein();
    }

    /**
     * No filter.
     *
     * @return page or empty String
     */
    public String filterKein() {
        List<ProjectDTO> projects = new ArrayList<>();
        try {
            projects = serviceManager.getProjectService().findAll();
        } catch (DataException e) {
            logger.error(e);
        }
        this.page = new Page<>(0, projects);
        return "/pages/ProjekteAlle";
    }

    /**
     * This method initializes the project list without any filters whenever the
     * bean is constructed.
     */
    @PostConstruct
    public void initializeProjectList() {
        filterKein();
        loadXMLConfiguration("PROJECT");
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("ERROR: unable to instantiate document builder: " + e.getMessage());
        }
    }

    /**
     * No filter back.
     *
     * @return String
     */
    public String filterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /**
     * Add file group.
     *
     * @return String
     */
    public String filegroupAdd() {
        this.myFilegroup = new ProjectFileGroup();
        this.myFilegroup.setProject(this.myProjekt);
        this.newFileGroups.add(this.myFilegroup.getId());
        return this.zurueck;
    }

    /**
     * Save file group.
     *
     * @return page
     */
    public String filegroupSave() {
        if (this.myProjekt.getProjectFileGroups() == null) {
            this.myProjekt.setProjectFileGroups(new ArrayList<>());
        }
        if (!this.myProjekt.getProjectFileGroups().contains(this.myFilegroup)) {
            this.myProjekt.getProjectFileGroups().add(this.myFilegroup);
        }

        return "jeniaClosePopupFrameWithAction";
    }

    public String filegroupEdit() {
        return this.zurueck;
    }

    /**
     * Delete file group.
     *
     * @return page
     */
    public String filegroupDelete() {
        // to be deleted fileGroups ids are listed
        // and deleted after a commit
        this.deletedFileGroups.add(this.myFilegroup.getId());
        return "/pages/ProjekteBearbeiten";
    }

    /*
     * Getter und Setter
     */

    public Project getMyProjekt() {
        return this.myProjekt;
    }

    /**
     * Set my project.
     *
     * @param inProjekt
     *            Project object
     */
    public void setMyProjekt(Project inProjekt) {
        // has to be called if a page back move was done
        this.cancel();
        this.myProjekt = inProjekt;
    }

    /**
     * The need to commit deleted fileGroups only after the save action requires a
     * filter, so that those filegroups marked for delete are not shown anymore.
     *
     * @return modified ArrayList
     */
    public ArrayList<ProjectFileGroup> getFileGroupList() {
        ArrayList<ProjectFileGroup> filteredFileGroupList = new ArrayList<>(this.myProjekt.getProjectFileGroups());

        for (Integer id : this.deletedFileGroups) {
            for (ProjectFileGroup f : this.myProjekt.getProjectFileGroups()) {
                if (f.getId() == null ? id == null : f.getId().equals(id)) {
                    filteredFileGroupList.remove(f);
                    break;
                }
            }
        }
        return filteredFileGroupList;
    }

    public ProjectFileGroup getMyFilegroup() {
        return this.myFilegroup;
    }

    public void setMyFilegroup(ProjectFileGroup myFilegroup) {
        this.myFilegroup = myFilegroup;
    }

    /**
     * Get statistic manager for production.
     *
     * @return instance of {@link StatisticsMode#PRODUCTION}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManagerForProduction() {
        if (this.statisticsManagerForProduction == null) {
            this.statisticsManagerForProduction = new StatisticsManager(StatisticsMode.PRODUCTION,
                    getProcessesForStatistics(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManagerForProduction;
    }

    /**
     * Get statistic manager for throughput.
     *
     * @return instance of {@link StatisticsMode#THROUGHPUT}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManagerForThroughput() {
        if (this.statisticsManagerForThroughput == null) {
            this.statisticsManagerForThroughput = new StatisticsManager(StatisticsMode.THROUGHPUT,
                    getProcessesForStatistics(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManagerForThroughput;
    }

    /**
     * Get statistic manager for corrections.
     *
     * @return instance of {@link StatisticsMode#CORRECTIONS}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManagerForCorrections() {
        if (this.statisticsManagerForCorrections == null) {
            this.statisticsManagerForCorrections = new StatisticsManager(StatisticsMode.CORRECTIONS,
                    getProcessesForStatistics(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManagerForCorrections;
    }

    /**
     * Get statistic manager for storage.
     *
     * @return instance of {@link StatisticsMode#STORAGE} {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManagerForStorage() {
        if (this.statisticsManagerForStorage == null) {
            this.statisticsManagerForStorage = new StatisticsManager(StatisticsMode.STORAGE,
                    getProcessesForStatistics(), FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManagerForStorage;
    }

    private List<ProcessDTO> getProcessesForStatistics() {
        try {
            return serviceManager.getProcessService().findByProjectId(this.myProjekt.getId(), false);
        } catch (DataException e) {
            logger.error(e);
            return new ArrayList<>();
        }
    }

    /**
     * generates values for count of volumes and images for statistics.
     */
    public void generateValuesForStatistics() {
        Double sumSortHelperImages = 0.0;
        Long countSortHelperImages = Long.valueOf(0);
        try {
            sumSortHelperImages = serviceManager.getProcessService().findSumForSortHelperImages(this.myProjekt.getId());
            countSortHelperImages = serviceManager.getProcessService()
                    .findCountForSortHelperImages(this.myProjekt.getId());
        } catch (DataException e) {
            logger.error(e);
        }
        this.myProjekt.setNumberOfPages(sumSortHelperImages.intValue());
        this.myProjekt.setNumberOfVolumes(countSortHelperImages.intValue());
    }

    /**
     * calculate pages per volume depending on given values, requested multiple
     * times via ajax.
     *
     * @return Integer of calculation
     */
    public Integer getCalcImagesPerVolume() {
        int volumes = this.myProjekt.getNumberOfVolumes();
        int pages = this.myProjekt.getNumberOfPages();
        if (volumes == 0) {
            return pages;
        }
        return pages / volumes;
    }

    /**
     * get calculated duration from start and end date.
     *
     * @return String of duration
     */
    public Integer getCalcDuration() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        return Months.monthsBetween(start, end).getMonths();
    }

    /**
     * calculate throughput of volumes per year.
     *
     * @return calculation
     */

    public Integer getCalcThroughputPerYear() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        int years = Years.yearsBetween(start, end).getYears();
        if (years < 1) {
            years = 1;
        }
        return this.myProjekt.getNumberOfVolumes() / years;
    }

    /**
     * calculate throughput of pages per year.
     *
     * @return calculation
     */
    public Integer getCalcThroughputPagesPerYear() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        int years = Years.yearsBetween(start, end).getYears();
        if (years < 1) {
            years = 1;
        }
        return this.myProjekt.getNumberOfPages() / years;
    }

    /**
     * calculate throughput of volumes per quarter.
     *
     * @return calculation
     */

    public Integer getCalcThroughputPerQuarter() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfVolumes() * 3 / month;
    }

    /**
     * calculate throughput of pages per quarter.
     *
     * @return calculation
     */
    public Integer getCalcTroughputPagesPerQuarter() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfPages() * 3 / month;
    }

    /**
     * calculate throughput of volumes per month.
     *
     * @return calculation
     */
    public Integer getCalcThroughputPerMonth() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfVolumes() / month;
    }

    /**
     * calculate throughput of pages per month.
     *
     * @return calculation
     */
    public Integer getCalcThroughputPagesPerMonth() {
        int month = getCalcDuration();
        if (month < 1) {
            month = 1;
        }
        return this.myProjekt.getNumberOfPages() / month;
    }

    private Double getThroughputPerDay() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
        Weeks weeks = Weeks.weeksBetween(start, end);
        logger.trace(weeks.getWeeks());
        int days = (weeks.getWeeks() * 5);

        if (days < 1) {
            days = 1;
        }
        return (double) this.myProjekt.getNumberOfVolumes() / (double) days;
    }

    /**
     * calculate throughput of volumes per day.
     *
     * @return calculation
     */

    public Integer getCalcThroughputPerDay() {
        return Math.round(this.getThroughputPerDay().floatValue());
    }

    /**
     * calculate throughput of pages per day.
     *
     * @return calculation
     */

    private Double getThroughputPagesPerDay() {
        DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
        DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());

        Weeks weeks = Weeks.weeksBetween(start, end);
        int days = (weeks.getWeeks() * 5);
        if (days < 1) {
            days = 1;
        }
        return (double) this.myProjekt.getNumberOfPages() / (double) days;
    }

    /**
     * calculate throughput of pages per day.
     *
     * @return calculation
     */
    public Integer getCalcPagesPerDay() {
        return Math.round(this.getThroughputPagesPerDay().floatValue());
    }

    /**
     * Get project progress interface.
     *
     * @return a StatQuestThroughputCommonFlow for the generation of project
     *         progress data
     */
    public StatQuestProjectProgressData getProjectProgressInterface() {
        synchronized (this.projectProgressData) {
            try {
                this.projectProgressData
                        .setCommonWorkflow(serviceManager.getProjectService().getWorkFlow(this.myProjekt));
                this.projectProgressData.setCalculationUnit(CalculationUnit.volumes);
                this.projectProgressData.setRequiredDailyOutput(this.getThroughputPerDay());
                this.projectProgressData.setTimeFrame(this.getMyProjekt().getStartDate(),
                        this.getMyProjekt().getEndDate());
                this.projectProgressData.setDataSource(getProcessesForStatistics());

                if (this.projectProgressImage == null) {
                    this.projectProgressImage = "";
                }
            } catch (Exception e) {
                // this.projectProgressData = null;
            }
        }
        return this.projectProgressData;
    }

    /**
     * Get progress calculated.
     *
     * @return true if calculation is finished
     */

    public Boolean getIsProgressCalculated() {
        if (this.projectProgressData == null) {
            return false;
        }
        return this.projectProgressData.isDataComplete();
    }

    /**
     * Get project progress image.
     *
     * @return path to rendered image of statistics
     */
    public String getProjectProgressImage() {

        if (this.projectProgressImage == null || this.projectProgressData == null
                || this.projectProgressData.hasChanged()) {
            try {
                calcProgressCharts();
            } catch (Exception e) {
                Helper.setFehlerMeldung("noImageRendered");
            }
        }
        return this.projectProgressImage;
    }

    private void calcProgressCharts() {
        if (this.getProjectProgressInterface().isDataComplete()) {
            ChartRenderer cr = new ChartRenderer();
            cr.setChartType(ChartType.LINE);
            cr.setDataTable(this.projectProgressData.getSelectedTable());
            BufferedImage bi = (BufferedImage) cr.getRendering();
            this.projectProgressImage = System.currentTimeMillis() + ".png";
            URI localImagePath = ConfigCore.getTempImagesPathAsCompleteDirectory();

            File outputfile = new File(localImagePath.resolve(this.projectProgressImage));
            try {
                ImageIO.write(bi, "png", outputfile);
            } catch (IOException e) {
                logger.debug("couldn't write project progress chart to file", e);
            }
        }
    }

    /**
     * Static Statistics.
     */
    public String getProjectStatImages() throws IOException {
        if (this.projectStatImages == null) {
            this.projectStatImages = System.currentTimeMillis() + "images.png";
            calcProjectStats(this.projectStatImages, true);
        }
        return this.projectStatImages;
    }

    /**
     * Get project stat volumes.
     *
     * @return string of image file projectStatVolumes
     */

    public String getProjectStatVolumes() throws IOException {
        if (this.projectStatVolumes == null) {
            this.projectStatVolumes = System.currentTimeMillis() + "volumes.png";
            calcProjectStats(this.projectStatVolumes, false);
        }
        return this.projectStatVolumes;
    }

    private synchronized void calcProjectStats(String inName, Boolean countImages) throws IOException {
        int width = 750;
        Date start = this.myProjekt.getStartDate();
        Date end = this.myProjekt.getEndDate();

        Integer inMax;
        if (countImages) {
            inMax = this.myProjekt.getNumberOfPages();
        } else {
            inMax = this.myProjekt.getNumberOfVolumes();
        }

        ProjectStatusDataTable pData = new ProjectStatusDataTable(this.myProjekt.getTitle(), start, end);

        IProvideProjectTaskList ptl = new WorkflowProjectTaskList();

        List<? extends IProjectTask> tasklist = ptl.calculateProjectTasks(this.myProjekt, countImages, inMax);
        for (IProjectTask pt : tasklist) {
            pData.addTask(pt);
        }

        // Determine height of the image
        int height = ProjectStatusDraw.getImageHeight(pData.getNumberOfTasks());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        ProjectStatusDraw projectStatusDraw = new ProjectStatusDraw(pData, g2d, width, height);
        projectStatusDraw.paint();

        // write image to temporary file
        URI localImagePath = ConfigCore.getTempImagesPathAsCompleteDirectory();
        File outputfile = new File(localImagePath.resolve(inName));
        ImageIO.write(image, "png", outputfile);
    }

    private StatisticsRenderingElement myCurrentTable;

    public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
        this.myCurrentTable = myCurrentTable;
    }

    public StatisticsRenderingElement getMyCurrentTable() {
        return this.myCurrentTable;
    }

    /**
     * Create excel.
     */
    public void createExcel() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {

            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
                ServletOutputStream out = response.getOutputStream();
                HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    /**
     * Getter for showStatistics.
     *
     * @return the showStatistics
     */
    public boolean getShowStatistics() {
        return this.showStatistics;
    }

    /**
     * Setter for showStatistics.
     *
     * @param showStatistics
     *            the showStatistics to set
     */
    public void setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
    }

    /**
     * Method being used as viewAction for project edit form. If 'itemId' is '0',
     * the form for creating a new project will be displayed.
     */
    public void loadProject() {
        try {
            if (!Objects.equals(this.itemId, 0)) {
                setMyProjekt(this.serviceManager.getProjectService().getById(this.itemId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving project with ID '" + this.itemId + "'; ", e.getMessage());
        }

    }

    public void setItemId(int id) {
        this.itemId = id;
    }

    public int getItemId() {
        return this.itemId;
    }

    /**
     * Return list of projects
     *
     * @return list of projects
     */
    public List<ProjectDTO> getProjects() {
        try {
            return serviceManager.getProjectService().findAll();
        } catch (DataException e) {
            logger.error("Unable to load projects: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Get the XML configuration string.
     *
     * @return the XML configuration string
     */
    public String getXMLConfiguration() {
        return this.xmlConfigurationString;
    }

    /**
     * Set the XML configuration string.
     *
     * @param configuration
     *            the XML configuration string
     */
    public void setXMLConfiguration(String configuration) {
        this.xmlConfigurationString = configuration;
    }

    /**
     * Load the content of the XML configuration file denoted by given String
     * 'configurationName'. This must be one of the keys in the static HashMap
     * 'configurationFiles'.
     *
     * @param configurationName
     *            name of the configuration to be loaded
     */
    public void loadXMLConfiguration(String configurationName) {
        try (StringWriter stringWriter = new StringWriter()) {
            currentConfigurationFile = configurationFiles.get(configurationName);
            XMLConfiguration currentConfiguration = new XMLConfiguration(
                    ConfigCore.getKitodoConfigDirectory() + currentConfigurationFile);
            currentConfiguration.save(stringWriter);
            this.xmlConfigurationString = stringWriter.toString();
        } catch (ConfigurationException e) {
            String errorMessage = "ERROR: Unable to load configuration file for '" + configurationName + "'.";
            logger.error(errorMessage + " " + e.getMessage());
            this.xmlConfigurationString = errorMessage;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Save the String 'xmlConfigurationString' to the XML file denoted by
     * 'configurationFile'.
     */
    public void saveXMLConfiguration() {
        try {
            Document document = documentBuilder.parse(new InputSource(new StringReader(this.xmlConfigurationString)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            File xmlConfigurationFile = new File(ConfigCore.getKitodoConfigDirectory() + currentConfigurationFile);
            FileOutputStream outputStream = new FileOutputStream(xmlConfigurationFile, false);
            StreamResult streamResult = new StreamResult(new PrintWriter(outputStream));
            logger.info("Saving configuration to file " + currentConfigurationFile);
            transformer.transform(domSource, streamResult);
            outputStream.close();
        } catch (SAXException e) {
            logger.error("ERROR: error parsing given XML string: " + e.getMessage());
        } catch (IOException e) {
            logger.error("ERROR: could not save XML configuration: " + e.getMessage());
        } catch (TransformerConfigurationException e) {
            logger.error("ERROR: transformer configuration exception: " + e.getMessage());
        } catch (TransformerException e) {
            logger.error("ERROR: transformation failed: " + e.getMessage());
        }
    }

    /**
     * Check and return whether the given String 'xmlCode' contains well formed XML
     * code or not.
     *
     * @param facesContext
     *            the current FacesContext
     * @param uiComponent
     *            the component containing the String that is being validated
     * @param xmlCode
     *            XML code that will be validated
     * @return whether 'xmlCode' is well formed or not
     */
    public boolean validateXML(FacesContext facesContext, UIComponent uiComponent, String xmlCode) {
        if (!Objects.equals(documentBuilder, null)) {
            InputSource inputSource = new InputSource(new StringReader(xmlCode));
            try {
                documentBuilder.parse(inputSource);
                return true;
            } catch (SAXParseException e) {
                // parse method throwing an SAXParseException means given xml code is not well
                // formed!
                String errorString = "Error while parsing XML: line = " + e.getLineNumber() + ", column = "
                        + e.getColumnNumber() + ": " + e.getMessage();
                FacesMessage errorMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "XML parsing error",
                        errorString);
                FacesContext currentFacesContext = FacesContext.getCurrentInstance();
                currentFacesContext.addMessage(uiComponent.getClientId(), errorMessage);
                logger.error(errorString);
                return false;
            } catch (SAXException e) {
                logger.error("SAXException: " + e.getMessage());
                return false;
            } catch (IOException e) {
                logger.error("IOException: " + e.getMessage());
                return false;
            }
        } else {
            logger.error("ERROR: document builder is null!");
            return false;
        }
    }

    /**
     * Get name of configuration file currently loaded into frontend editor.
     *
     * @return configuration file name
     */
    public String getCurrentConfigurationFile() {
        return currentConfigurationFile;
    }
}
