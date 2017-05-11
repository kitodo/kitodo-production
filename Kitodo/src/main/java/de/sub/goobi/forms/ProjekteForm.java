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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

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
import org.goobi.production.flow.statistics.hibernate.UserProjectFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.ProjectFileGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

@ManagedBean
@ViewScoped
public class ProjekteForm extends BasisForm {
    private static final long serialVersionUID = 6735912903249358786L;
    private static final Logger logger = LogManager.getLogger(ProjekteForm.class);

    private Project myProjekt = new Project();
    private ProjectFileGroup myFilegroup;
    private final ServiceManager serviceManager = new ServiceManager();

    // lists accepting the preliminary actions of adding and delting filegroups
    // it needs the execution of commit fileGroups to make these changes
    // permanent
    private List<Integer> newFileGroups = new ArrayList<Integer>();
    private List<Integer> deletedFileGroups = new ArrayList<Integer>();

    private StatisticsManager statisticsManager1 = null;
    private StatisticsManager statisticsManager2 = null;
    private StatisticsManager statisticsManager3 = null;
    private StatisticsManager statisticsManager4 = null;
    private final StatQuestProjectProgressData projectProgressData = new StatQuestProjectProgressData();

    private String projectProgressImage;
    private String projectStatImages;
    private String projectStatVolumes;
    private boolean showStatistics;

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
        this.newFileGroups = new ArrayList<Integer>();
        // deleting the fileGroups marked for deletion
        deleteFileGroups(this.deletedFileGroups);
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<Integer>();
    }

    /**
     * this needs to be executed in order to rollback adding of filegroups.
     */
    public String cancel() {
        // flushing new fileGroups
        deleteFileGroups(this.newFileGroups);
        // resetting the List of new fileGroups
        this.newFileGroups = new ArrayList<Integer>();
        // resetting the List of fileGroups marked for deletion
        this.deletedFileGroups = new ArrayList<Integer>();
        this.projectProgressImage = null;
        this.projectStatImages = null;
        this.projectStatVolumes = null;
        return "/newpages/ProjekteAlle";
    }

    public String newProject() {
        this.myProjekt = new Project();
        return "/newpages/ProjekteBearbeiten";
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
            return "/newpages/ProjekteAlle";
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
            logger.error(e);
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("could not insert to index", e.getMessage());
            logger.error(e);
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("incorrect response from ElasticSearch", e.getMessage());
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
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
            logger.error(e.getMessage());
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("could not insert to index", e.getMessage());
            logger.error(e);
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("incorrect response from ElasticSearch", e.getMessage());
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
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not delete", e.getMessage());
                logger.error(e.getMessage());
                return null;
            } catch (IOException e) {
                Helper.setFehlerMeldung("could not delete from index", e.getMessage());
                logger.error(e);
            } catch (CustomResponseException e) {
                Helper.setFehlerMeldung("incorrect response from ElasticSearch", e.getMessage());
                logger.error(e);
                return null;
            }
        }
        return "/newpages/ProjekteAlle";
    }

    /**
     * No filter.
     *
     * @return page or empty String
     */
    public String filterKein() {
        try {
            Session session = Helper.getHibernateSession();
            session.clear();
            Criteria crit = session.createCriteria(Project.class);
            crit.addOrder(Order.asc("title"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("could not read", he.getMessage());
            logger.error(he.getMessage());
            return null;
        }
        return "/newpages/ProjekteAlle";
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
            this.myProjekt.setProjectFileGroups(new ArrayList<ProjectFileGroup>());
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
        return "/newpages/ProjekteBearbeiten";
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
     * The need to commit deleted fileGroups only after the save action requires
     * a filter, so that those filegroups marked for delete are not shown
     * anymore.
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
     * Get statistic manager 1.
     *
     * @return instance of {@link StatisticsMode#PRODUCTION}
     *         {@link StatisticsManager}
     */

    public StatisticsManager getStatisticsManager1() {
        if (this.statisticsManager1 == null) {
            this.statisticsManager1 = new StatisticsManager(StatisticsMode.PRODUCTION,
                    new UserProjectFilter(this.myProjekt.getId()),
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManager1;
    }

    /**
     * Get statistic manager 2.
     *
     * @return instance of {@link StatisticsMode#THROUGHPUT}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager2() {
        if (this.statisticsManager2 == null) {
            this.statisticsManager2 = new StatisticsManager(StatisticsMode.THROUGHPUT,
                    new UserProjectFilter(this.myProjekt.getId()),
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManager2;
    }

    /**
     * Get statistic manager 3.
     *
     * @return instance of {@link StatisticsMode#CORRECTIONS}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager3() {
        if (this.statisticsManager3 == null) {
            this.statisticsManager3 = new StatisticsManager(StatisticsMode.CORRECTIONS,
                    new UserProjectFilter(this.myProjekt.getId()),
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManager3;
    }

    /**
     * Get statistic manager 4.
     *
     * @return instance of {@link StatisticsMode#STORAGE}
     *         {@link StatisticsManager}
     */
    public StatisticsManager getStatisticsManager4() {
        if (this.statisticsManager4 == null) {
            this.statisticsManager4 = new StatisticsManager(StatisticsMode.STORAGE,
                    new UserProjectFilter(this.myProjekt.getId()),
                    FacesContext.getCurrentInstance().getViewRoot().getLocale());
        }
        return this.statisticsManager4;
    }

    /**
     * generates values for count of volumes and images for statistics.
     */
    @SuppressWarnings("rawtypes")
    public void generateValuesForStatistics() {
        Criteria crit = Helper.getHibernateSession().createCriteria(Process.class)
                .add(Restrictions.eq("projekt", this.myProjekt));
        ProjectionList pl = Projections.projectionList();
        pl.add(Projections.sum("sortHelperImages"));
        pl.add(Projections.count("sortHelperImages"));
        crit.setProjection(pl);
        List list = crit.list();
        Long images = 0l;
        Long volumes = 0l;
        for (Object obj : list) {
            Object[] row = (Object[]) obj;
            images = (Long) row[0];
            volumes = (Long) row[1];
        }
        this.myProjekt.setNumberOfPages(images.intValue());
        this.myProjekt.setNumberOfVolumes(volumes.intValue());
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
        int i = pages / volumes;
        return i;
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
        double back = (double) this.myProjekt.getNumberOfVolumes() / (double) days;
        return back;
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
        double back = (double) this.myProjekt.getNumberOfPages() / (double) days;
        return back;
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
                this.projectProgressData.setDataSource(new UserProjectFilter(this.myProjekt.getId()));

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
            String localImagePath = ConfigCore.getTempImagesPathAsCompleteDirectory();

            File outputfile = new File(localImagePath + this.projectProgressImage);
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
    public String getProjectStatImages() throws IOException, InterruptedException {
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

    public String getProjectStatVolumes() throws IOException, InterruptedException {
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
        String localImagePath = ConfigCore.getTempImagesPathAsCompleteDirectory();
        File outputfile = new File(localImagePath + inName);
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

}
