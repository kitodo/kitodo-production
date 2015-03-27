package de.sub.goobi.forms;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.results.ChartDraw.ChartType;
import de.sub.goobi.beans.ProjectFileGroup;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.ProjektDAO;

public class ProjekteForm extends BasisForm {
	private static final long serialVersionUID = 6735912903249358786L;
	private static final Logger myLogger = Logger.getLogger(ProjekteForm.class);

	private Projekt myProjekt = new Projekt();
	private ProjectFileGroup myFilegroup;
	private final ProjektDAO dao = new ProjektDAO();

	// lists accepting the preliminary actions of adding and delting filegroups
	// it needs the execution of commit fileGroups to make these changes permanent
	private List<Integer> newFileGroups = new ArrayList<Integer>();
	private List<Integer> deletedFileGroups = new ArrayList<Integer>();

	private StatisticsManager statisticsManager1 = null;
	private StatisticsManager statisticsManager2 = null;
	private StatisticsManager statisticsManager3 = null;
	private StatisticsManager statisticsManager4 = null;
	private StatQuestProjectProgressData projectProgressData = null;

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
		this.Cancel();
	}

	/**
	 * this method deletes filegroups by their id's in the list
	 * 
	 * @param List
	 *            <Integer> fileGroups
	 */
	private void deleteFileGroups(List<Integer> fileGroups) {
		for (Integer id : fileGroups) {
			for (ProjectFileGroup f : this.myProjekt.getFilegroupsList()) {
				if (f.getId() == null ? id == null : f.getId().equals(id)) {
					this.myProjekt.getFilegroups().remove(f);
					break;
				}
			}
		}
	}

	/**
	 * this method flushes the newFileGroups List, thus makes them permanent and deletes those marked for deleting, making the removal permanent
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
	 * this needs to be executed in order to rollback adding of filegroups
	 * 
	 * @return
	 */
	public String Cancel() {
		// flushing new fileGroups
		deleteFileGroups(this.newFileGroups);
		// resetting the List of new fileGroups
		this.newFileGroups = new ArrayList<Integer>();
		// resetting the List of fileGroups marked for deletion
		this.deletedFileGroups = new ArrayList<Integer>();
		this.projectProgressImage = null;
		this.projectStatImages = null;
		this.projectStatVolumes = null;
		return "ProjekteAlle";
	}

	public String Neu() {
		this.myProjekt = new Projekt();
		return "ProjekteBearbeiten";
	}

	public String Speichern() {
		// call this to make saving and deleting permanent
		this.commitFileGroups();
		try {
			this.dao.save(this.myProjekt);
			return "ProjekteAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not save", e.getMessage());
			myLogger.error(e);
			return "";
		}
	}

	public String Apply() {
		// call this to make saving and deleting permanent
		myLogger.trace("Apply wird aufgerufen...");
		this.commitFileGroups();
		try {
			this.dao.save(this.myProjekt);
			return "";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not save", e.getMessage());
			myLogger.error(e.getMessage());
			return "";
		}
	}

	public String Loeschen() {
		if (this.myProjekt.getBenutzer().size() > 0) {
			Helper.setFehlerMeldung("userAssignedError");
			return "";
		} else {
		try {
			this.dao.remove(this.myProjekt);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("could not delete", e.getMessage());
			myLogger.error(e.getMessage());
			return "";
		}
		}
		return "ProjekteAlle";
	}

	public String FilterKein() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(Projekt.class);
			crit.addOrder(Order.asc("titel"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("could not read", he.getMessage());
			myLogger.error(he.getMessage());
			return "";
		}
		return "ProjekteAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	public String filegroupAdd() {
		this.myFilegroup = new ProjectFileGroup();
		this.myFilegroup.setProject(this.myProjekt);
		this.newFileGroups.add(this.myFilegroup.getId());
		return this.zurueck;
	}

	public String filegroupSave() {
		if (this.myProjekt.getFilegroups() == null) {
			this.myProjekt.setFilegroups(new HashSet<ProjectFileGroup>());
		}
		if (!this.myProjekt.getFilegroups().contains(this.myFilegroup)) {
			this.myProjekt.getFilegroups().add(this.myFilegroup);
		}

		return "jeniaClosePopupFrameWithAction";
	}

	public String filegroupEdit() {
		return this.zurueck;
	}

	public String filegroupDelete() {
		// to be deleted fileGroups ids are listed
		// and deleted after a commit
		this.deletedFileGroups.add(this.myFilegroup.getId());
		return "ProjekteBearbeiten";
	}

	/*
	 * Getter und Setter
	 */

	public Projekt getMyProjekt() {
		return this.myProjekt;
	}

	public void setMyProjekt(Projekt inProjekt) {
		// has to be called if a page back move was done
		this.Cancel();
		this.myProjekt = inProjekt;
	}

	/**
	 * The need to commit deleted fileGroups only after the save action requires a filter, so that those filegroups marked for delete are not shown
	 * anymore
	 * 
	 * @return modified ArrayList
	 */
	public ArrayList<ProjectFileGroup> getFileGroupList() {
		ArrayList<ProjectFileGroup> filteredFileGroupList = new ArrayList<ProjectFileGroup>(this.myProjekt.getFilegroupsList());

		for (Integer id : this.deletedFileGroups) {
			for (ProjectFileGroup f : this.myProjekt.getFilegroupsList()) {
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
	 * 
	 * @return instance of {@link StatisticsMode.PRODUCTION} {@link StatisticsManager}
	 */

	public StatisticsManager getStatisticsManager1() {
		if (this.statisticsManager1 == null) {
			this.statisticsManager1 = new StatisticsManager(StatisticsMode.PRODUCTION, new UserProjectFilter(this.myProjekt.getId()), FacesContext
					.getCurrentInstance().getViewRoot().getLocale());
		}
		return this.statisticsManager1;
	}

	/**
	 * 
	 * @return instance of {@link StatisticsMode.THROUGHPUT} {@link StatisticsManager}
	 */
	public StatisticsManager getStatisticsManager2() {
		if (this.statisticsManager2 == null) {
			this.statisticsManager2 = new StatisticsManager(StatisticsMode.THROUGHPUT, new UserProjectFilter(this.myProjekt.getId()), FacesContext
					.getCurrentInstance().getViewRoot().getLocale());
		}
		return this.statisticsManager2;
	}

	/**
	 * 
	 * @return instance of {@link StatisticsMode.CORRECTIONS} {@link StatisticsManager}
	 */
	public StatisticsManager getStatisticsManager3() {
		if (this.statisticsManager3 == null) {
			this.statisticsManager3 = new StatisticsManager(StatisticsMode.CORRECTIONS, new UserProjectFilter(this.myProjekt.getId()), FacesContext
					.getCurrentInstance().getViewRoot().getLocale());
		}
		return this.statisticsManager3;
	}

	/**
	 * 
	 * @return instance of {@link StatisticsMode.STORAGE} {@link StatisticsManager}
	 */
	public StatisticsManager getStatisticsManager4() {
		if (this.statisticsManager4 == null) {
			this.statisticsManager4 = new StatisticsManager(StatisticsMode.STORAGE, new UserProjectFilter(this.myProjekt.getId()), FacesContext
					.getCurrentInstance().getViewRoot().getLocale());
		}
		return this.statisticsManager4;
	}

	/**
	 * generates values for count of volumes and images for statistics
	 */

	@SuppressWarnings("rawtypes")
	public void GenerateValuesForStatistics() {
		Criteria crit = Helper.getHibernateSession().createCriteria(Prozess.class).add(Restrictions.eq("projekt", this.myProjekt));
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
	 * calculate pages per volume depending on given values, requested multiple times via ajax
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
	 * get calculated duration from start and end date
	 * 
	 * @return String of duration
	 */
	public Integer getCalcDuration() {
		DateTime start = new DateTime(this.myProjekt.getStartDate().getTime());
		DateTime end = new DateTime(this.myProjekt.getEndDate().getTime());
		return Months.monthsBetween(start, end).getMonths();
	}

	/**
	 * calculate throughput of volumes per year
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
	 * calculate throughput of pages per year
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
	 * calculate throughput of volumes per quarter
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
	 * calculate throughput of pages per quarter
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
	 * calculate throughput of volumes per month
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
	 * calculate throughput of pages per month
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
		myLogger.trace(weeks.getWeeks());
		int days = (weeks.getWeeks() * 5);

		if (days < 1) {
			days = 1;
		}
		double back = (double) this.myProjekt.getNumberOfVolumes() / (double) days;
		return back;
	}

	/**
	 * calculate throughput of volumes per day
	 * 
	 * @return calculation
	 */

	public Integer getCalcThroughputPerDay() {
		return Math.round(this.getThroughputPerDay().floatValue());
	}

	/**
	 * calculate throughput of pages per day
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
	 * calculate throughput of pages per day
	 * 
	 * @return calculation
	 */
	public Integer getCalcPagesPerDay() {
		return Math.round(this.getThroughputPagesPerDay().floatValue());
	}

	/**
	 * @return a StatQuestThroughputCommonFlow for the generation of projekt progress data
	 */
	public StatQuestProjectProgressData getProjectProgressInterface() {

			if (this.projectProgressData == null) { // initialize datasource with default selection
				this.projectProgressData = new StatQuestProjectProgressData();
			}
			synchronized (this.projectProgressData) {
			try {

				this.projectProgressData.setCommonWorkflow(this.myProjekt.getWorkFlow());
				this.projectProgressData.setCalculationUnit(CalculationUnit.volumes);
				this.projectProgressData.setRequiredDailyOutput(this.getThroughputPerDay());
				this.projectProgressData.setTimeFrame(this.getMyProjekt().getStartDate(), this.getMyProjekt().getEndDate());
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
	 * 
	 * @return path to rendered image of statistics
	 */
	public String getProjectProgressImage() {

		if (this.projectProgressImage == null || this.projectProgressData == null || this.projectProgressData.hasChanged()) {
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
			String localImagePath = ConfigMain.getTempImagesPathAsCompleteDirectory();

			File outputfile = new File(localImagePath + this.projectProgressImage);
			try {
				ImageIO.write(bi, "png", outputfile);
			} catch (IOException e) {
				myLogger.debug("couldn't write project progress chart to file", e);
			}
		}
	}

	/*********************************************************
	 * Static Statistics
	 *********************************************************/

	public String getProjectStatImages() throws IOException, InterruptedException {
		if (this.projectStatImages == null) {
			this.projectStatImages = System.currentTimeMillis() + "images.png";
			calcProjectStats(this.projectStatImages, true);
		}
		return this.projectStatImages;
	}

	/**
	 * 
	 * @return string of image file projectStatVolumes
	 * @throws IOException
	 * @throws InterruptedException
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

		ProjectStatusDataTable pData = new ProjectStatusDataTable(this.myProjekt.getTitel(), start, end);

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
		String localImagePath = ConfigMain.getTempImagesPathAsCompleteDirectory();
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
	
	public void CreateExcel() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (!facesContext.getResponseComplete()) {

			/*
			 *  Vorbereiten der Header-Informationen 
			 */
			HttpServletResponse response = (HttpServletResponse) facesContext
					.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext
						.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("export.xls");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition",
						"attachment;filename=\"export.xls\"");
				ServletOutputStream out = response.getOutputStream();
				HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
				wb.write(out);
				out.flush();
				facesContext.responseComplete();

			} catch (IOException e) {
				
			}
		}
	}
	

	/*************************************************************************************
	 * Getter for showStatistics
	 * 
	 * @return the showStatistics
	 *************************************************************************************/
	public boolean getShowStatistics() {
		return this.showStatistics;
	}

	/**************************************************************************************
	 * Setter for showStatistics
	 * 
	 * @param showStatistics
	 *            the showStatistics to set
	 **************************************************************************************/
	public void setShowStatistics(boolean showStatistics) {
		this.showStatistics = showStatistics;
	}

}
