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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.tasks.ExportBatchTask;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.persistence.BatchDAO;
import de.sub.goobi.persistence.ProzessDAO;

public class BatchForm extends BasisForm {

	private static final long serialVersionUID = 8234897225425856549L;

	private static final Logger logger = Logger.getLogger(BatchForm.class);

	private List<Prozess> currentProcesses;
	private List<Prozess> selectedProcesses;
	private List<Batch> currentBatches;
	private List<String> selectedBatches;
	private String batchfilter;
	private String processfilter;
	private IEvaluableFilter myFilteredDataSource;
	
	private final ProzessDAO dao = new ProzessDAO();
	private String modusBearbeiten = "";

	private String batchTitle;

	private int getBatchMaxSize(){
		int batchsize =ConfigMain.getIntParameter("batchMaxSize",100);
		return batchsize;
	}
	
	public List<Prozess> getCurrentProcesses() {
		return this.currentProcesses;
	}

	public void setCurrentProcesses(List<Prozess> currentProcesses) {
		this.currentProcesses = currentProcesses;
	}

	public void loadBatchData() {
		if (selectedProcesses == null || selectedProcesses.size() == 0) {
			this.currentBatches = BatchDAO.readAll();
			this.selectedBatches = new ArrayList<String>();
		} else {
			selectedBatches = new ArrayList<String>();
			HashSet<Batch> batchesToSelect = new HashSet<Batch>();
			for (Prozess process : selectedProcesses)
				batchesToSelect.addAll(process.getBatchesInitialized());
			for (Batch batch : batchesToSelect)
				selectedBatches.add(batch.getIdString());
		}
	}

	public void loadProcessData() {
		Set<Prozess> processes = new HashSet<Prozess>();
		try {
			for (String b : selectedBatches)
				processes.addAll(BatchDAO.read(Integer.parseInt(b)).getProcesses());
			currentProcesses = new ArrayList<Prozess>(processes);
		} catch (Exception e) { // NumberFormatException, DAOException
			logger.error(e);
			Helper.setFehlerMeldung("fehlerBeimEinlesen");
			return;
		}
	}

	@SuppressWarnings("unchecked")
	public void filterProcesses() {

		if (this.processfilter == null) {
			this.processfilter = "";
		}
		this.myFilteredDataSource = new UserDefinedFilter(this.processfilter);
		Criteria crit = this.myFilteredDataSource.getCriteria();
		crit.addOrder(Order.desc("erstellungsdatum"));
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.setMaxResults(getBatchMaxSize());
		try {
			this.currentProcesses = crit.list();
		} catch (HibernateException e) {
			this.currentProcesses = new ArrayList<Prozess>();
		}
	}

	public void filterBatches() {
		currentBatches = new ArrayList<Batch>();
		for (Batch batch : BatchDAO.readAll()) {
			if (batch.contains(batchfilter))
				currentBatches.add(batch);
		}
	}

	public List<SelectItem> getCurrentProcessesAsSelectItems() {
		List<SelectItem> answer = new ArrayList<SelectItem>();
		for (Prozess p : this.currentProcesses) {
			answer.add(new SelectItem(p, p.getTitel()));
		}
		return answer;
	}

	public String getBatchfilter() {
		return this.batchfilter;
	}

	public void setBatchfilter(String batchfilter) {
		this.batchfilter = batchfilter;
	}

	public String getBatchName() {
		return batchTitle;
	}

	public void setBatchName(String batchName) {
		batchTitle = batchName;
	}

	public String getProcessfilter() {
		return this.processfilter;
	}

	public void setProcessfilter(String processfilter) {
		this.processfilter = processfilter;
	}

	public List<Batch> getCurrentBatches() {
		return this.currentBatches;
	}

	public void setCurrentBatches(List<Batch> currentBatches) {
		this.currentBatches = currentBatches;
	}

	public List<Prozess> getSelectedProcesses() {
		return this.selectedProcesses;
	}

	public void setSelectedProcesses(List<Prozess> selectedProcesses) {
		this.selectedProcesses = selectedProcesses;
	}

	public List<String> getSelectedBatches() {
		return this.selectedBatches;
	}

	public void setSelectedBatches(List<String> selectedBatches) {
		this.selectedBatches = selectedBatches;
	}

	public String FilterAlleStart() {
		filterBatches();
		filterProcesses();
		return "BatchesAll";
	}

	public String downloadDocket() {
		logger.debug("generate docket for process list");
		String rootpath = ConfigMain.getParameter("xsltFolder");
		File xsltfile = new File(rootpath, "docket_multipage.xsl");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Set<Prozess> docket = Collections.emptySet();
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
		} else if (this.selectedBatches.size() == 1) {
			try {
				docket = BatchDAO.read(Integer.valueOf(selectedBatches.get(0))).getProcesses();
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("fehlerBeimEinlesen");
				return "";
			}
		} else {
			Helper.setFehlerMeldung("tooManyBatchesSelected");
		}
		if (docket.size() > 0) {
			if (!facesContext.getResponseComplete()) {
				HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
				String fileName = "batch_docket" + ".pdf";
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType(fileName);
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

				try {
					ServletOutputStream out = response.getOutputStream();
					ExportDocket ern = new ExportDocket();
					ern.startExport(docket, out, xsltfile.getAbsolutePath());
					out.flush();
				} catch (IOException e) {
					logger.error("IOException while exporting run note", e);
				}

				facesContext.responseComplete();
			}
		}
		return "";
	}

	/**
	 * The method deleteBatch() is called if the user clicks the action link to
	 * delete batches. It runs the deletion of the batches.
	 */
	public void deleteBatch() {
		int selectedBatchesSize = this.selectedBatches.size();
		if (selectedBatchesSize == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		}
		List<Integer> ids = new ArrayList<Integer>(selectedBatchesSize);
		for (String entry : this.selectedBatches)
			ids.add(Integer.parseInt(entry));
		try {
			BatchDAO.deleteAll(ids);
			FilterAlleStart();
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
		}
	}

	public void addProcessesToBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		}
		if (this.selectedProcesses.size() == 0) {
			Helper.setFehlerMeldung("noProcessSelected");
			return;
		}
		try {
			for (String entry : this.selectedBatches) {
				Batch batch = BatchDAO.read(Integer.parseInt(entry));
				batch.addAll(this.selectedProcesses);
				BatchDAO.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Prozess p : this.selectedProcesses)
						p.addToWikiField("debug",
								Helper.getTranslation("addToBatch", Arrays.asList(new String[] { batch.getLabel() })));
					this.dao.saveList(this.selectedProcesses);
				}
			}
			return;
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
		}
	}

	public void removeProcessesFromBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		}
		if (this.selectedProcesses.size() == 0) {
			Helper.setFehlerMeldung("noProcessSelected");
			return;
		}
		try {
			for (String entry : this.selectedBatches) {
				Batch batch = BatchDAO.read(Integer.parseInt(entry));
				batch.removeAll(this.selectedProcesses);
				BatchDAO.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Prozess p : this.selectedProcesses)
						p.addToWikiField(
								"debug",
								Helper.getTranslation("removeFromBatch",
										Arrays.asList(new String[] { batch.getLabel() })));
					this.dao.saveList(this.selectedProcesses);
				}
			}
		} catch (DAOException e) {
			logger.error(e);
			Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
			return;
		}
		FilterAlleStart();
	}

	public void renameBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return;
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("tooManyBatchesSelected");
			return;
		} else {
			try {
				Batch batch = BatchDAO.read(Integer.valueOf(selectedBatches.get(0)));
				batch.setTitle(batchTitle == null || batchTitle.trim().length() == 0 ? null : batchTitle);
				BatchDAO.save(batch);
			} catch (DAOException e) {
				Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
				logger.error(e);
				return;
			}
		}
	}

	public void createNewBatch() {
		if (selectedProcesses.size() > 0) {
			Batch batch = null;
			if(batchTitle != null && batchTitle.trim().length() > 0){
				batch = new Batch(batchTitle.trim(), selectedProcesses);
			}else{
				batch = new Batch(selectedProcesses);
			}
			try {
				BatchDAO.save(batch);
				if (ConfigMain.getBooleanParameter("batches.logChangesToWikiField", false)) {
					for (Prozess p : selectedProcesses)
						p.addToWikiField("debug",
								Helper.getTranslation("addToBatch", Arrays.asList(new String[] { batch.getLabel() })));
					this.dao.saveList(selectedProcesses);
				}
			} catch (DAOException e) {
				Helper.setFehlerMeldung("fehlerNichtAktualisierbar", e.getMessage());
				logger.error(e);
				return;
			}
		}
		FilterAlleStart();
	}

	/*
	 * properties
	 */
	private BatchProcessHelper batchHelper;

	public String editProperties() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return "";
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("tooManyBatchesSelected");
			return "";
		} else {
			if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
				Batch batch;
				try {
					batch = BatchDAO.read(Integer.valueOf(selectedBatches.get(0)));
					this.batchHelper = new BatchProcessHelper(batch);
					return "BatchProperties";
				} catch (DAOException e) {
					logger.error(e);
					Helper.setFehlerMeldung("fehlerBeimEinlesen");
					return "";
				}
			} else {
				Helper.setFehlerMeldung("noBatchSelected");
				return "";
			}
		}
	}

	public BatchProcessHelper getBatchHelper() {
		return this.batchHelper;
	}

	public void setBatchHelper(BatchProcessHelper batchHelper) {
		this.batchHelper = batchHelper;
	}

	public String getModusBearbeiten() {
		return this.modusBearbeiten;
	}

	public void setModusBearbeiten(String modusBearbeiten) {
		this.modusBearbeiten = modusBearbeiten;
	}

	/**
	 * The function exportBatch() is called by Faces if the user clicks the
	 * action link to DMS-export one or more batches. For each batch, a long
	 * running task will be created and the user will be redirected to the long
	 * running task manager page where it can observe the task progressing.
	 * 
	 * @return the next page to show as named in a &lt;from-outcome&gt; element
	 *         in faces_config.xml
	 */
	public String exportBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return "";
		}
		LinkedList<ExportBatchTask> batches = new LinkedList<ExportBatchTask>();
		for (String batchID : selectedBatches) {
			try {
				Batch batch = BatchDAO.read(Integer.valueOf(batchID));
				ExportBatchTask exportBatch = new ExportBatchTask(batch);
				batches.add(exportBatch);
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("fehlerBeimEinlesen");
				return "";
			}
		}
		for (ExportBatchTask task : batches)
			LongRunningTaskManager.getInstance().addTask(task);
		return "taskmanager";
	}
}
