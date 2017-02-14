package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportDocket;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Batch;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
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
	
	private ProzessDAO dao = new ProzessDAO();
	private String modusBearbeiten = "";

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
		this.currentBatches = new ArrayList<Batch>();
		this.selectedBatches = new ArrayList<String>();
		for (Prozess p : this.selectedProcesses) {
			if (p.getBatchID() != null && !this.currentBatches.contains(p.getBatchID())) {
				this.currentBatches.add(generateBatch(p.getBatchID()));
			}
		}
	}

	private Batch generateBatch(Integer id) {
		Session session = Helper.getHibernateSession();

		Criteria crit = session.createCriteria(Prozess.class);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		if (id != null) {
			crit.add(Restrictions.eq("batchID", id));
		} else {
			crit.add(Restrictions.isNull("batchID"));
		}
		crit.setProjection(Projections.rowCount());
		
		String msg1 = Helper.getTranslation("batch");
		String msg2 = Helper.getTranslation("prozesse");
		if (id != null) {
			String text = msg1 + " " + id + " (" + crit.uniqueResult() + " " + msg2 + ")";
			return new Batch(id, text);
		} else {
			String text = Helper.getTranslation("withoutBatch") + " (" + crit.uniqueResult() + " " + msg2 + ")";
			return new Batch(null, text);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadProcessData() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.setMaxResults(getBatchMaxSize());
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		List<Integer> ids = new ArrayList<Integer>();
		for (String s : this.selectedBatches) {
			if (s != null && !s.equals("") && !s.equals("null")) {
				ids.add(new Integer(s));
			}
		}
		if (this.selectedBatches.size() > 0) {
			if (this.selectedBatches.contains(null) || this.selectedBatches.contains("null")) {
				crit.add(Restrictions.isNull("batchID"));
			} else {
				crit.add(Restrictions.in("batchID", ids));
			}
		}
		this.currentProcesses = crit.list();
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

	@SuppressWarnings("unchecked")
	public void filterBatches() {
		Integer number = null;
		try {
			number = new Integer(this.batchfilter);
		} catch (Exception e) {
			logger.warn("NAN Exception: " + this.batchfilter);
		}
		if (number != null) {
			Session session = Helper.getHibernateSession();
			Query query = session.createQuery("select distinct batchID from Prozess order by batchID desc");
			query.setMaxResults(getBatchMaxSize());

			List<Integer> allBatches = query.list();
			this.currentBatches = new ArrayList<Batch>();
			for (Integer in : allBatches) {
				if (in != null && Integer.toString(in).contains(this.batchfilter)) {
					this.currentBatches.add(generateBatch(in));
				}
			}
		} else {
			Session session = Helper.getHibernateSession();
			Query query = session.createQuery("select distinct batchID from Prozess order by batchID desc");
			query.setMaxResults(getBatchMaxSize());
			List<Integer> ids = query.list();
			this.currentBatches = new ArrayList<Batch>();
			for (Integer in : ids) {
				this.currentBatches.add(generateBatch(in));
			}
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

	@SuppressWarnings("unchecked")
	public String downloadDocket() {
		logger.debug("generate docket for process list");
		String rootpath = ConfigMain.getParameter("xsltFolder");
		File xsltfile = new File(rootpath, "docket_multipage.xsl");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		List<Prozess> docket = new ArrayList<Prozess>();
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
		} else if (this.selectedBatches.size() == 1) {
			Session session = Helper.getHibernateSession();
			Criteria crit = session.createCriteria(Prozess.class);
			crit.setMaxResults(getBatchMaxSize());
			crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
//			List<Integer> ids = new ArrayList<Integer>();
			crit.add(Restrictions.eq("batchID", new Integer(this.selectedBatches.get(0))));
			docket = crit.list();
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

	@SuppressWarnings("unchecked")
	public void deleteBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
		} else if (this.selectedBatches.size() == 1) {
			if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
				Session session = Helper.getHibernateSession();
				Criteria crit = session.createCriteria(Prozess.class);
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
//				List<Integer> ids = new ArrayList<Integer>();
				crit.add(Restrictions.eq("batchID", new Integer(this.selectedBatches.get(0))));
				List<Prozess> deleteList = crit.list();
				{
					for (Prozess p : deleteList) {
						p.setBatchID(null);
//						try {
//							session.saveOrUpdate(p);
//						} catch (Exception e) {
//							Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//							logger.error(e);
//						}
					}
					try {
					this.dao.saveList(deleteList);
//						session.flush();
//						session.connection().commit();
					} catch (DAOException e) {
						Helper.setFehlerMeldung("Error, could not update", e.getMessage());
						logger.error(e);
					}
				}
			} else {
				Helper.setFehlerMeldung("noBatchSelected");
			}
		} else {
			Helper.setFehlerMeldung("toḾanyBatchesSelected");
		}
		FilterAlleStart();

	}

	public void addProcessesToBatch() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("toḾanyBatchesSelected");
		} else {
			try {
//				Session session = Helper.getHibernateSession();
				Integer batchid = new Integer(this.selectedBatches.get(0));
				for (Prozess p : this.selectedProcesses) {
					p.setBatchID(batchid);
					p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "added process to batch " + batchid));
//					try {
//						session.saveOrUpdate(p);
//					} catch (Exception e) {
//						Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//						logger.error(e);
//					}
				}
				this.dao.saveList(this.selectedProcesses);
//				try {
//					session.flush();
//					session.connection().commit();
//				} catch (HibernateException e) {
//					Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//					logger.error(e);
//				} catch (SQLException e) {
//					Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//					logger.error(e);
//				}
			} catch (Exception e) {
				Helper.setFehlerMeldung("noBatchSelected");
			}
		}
		FilterAlleStart();
	}

	public void removeProcessesFromBatch() {
//		Session session = Helper.getHibernateSession();
		for (Prozess p : this.selectedProcesses) {
			p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "removed process from batch " + p.getBatchID()));
			p.setBatchID(null);

//			try {
//				session.saveOrUpdate(p);
//			} catch (Exception e) {
//				Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//				logger.error(e);
//			}
		}
		try {
		this.dao.saveList(this.selectedProcesses);
//			session.flush();
//			session.connection().commit();
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not update", e.getMessage());
			logger.error(e);
//		} catch (SQLException e) {
//			Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//			logger.error(e);
		}
		FilterAlleStart();
	}

	public void createNewBatch() {
		if (this.selectedProcesses.size() > 0) {
			Session session = Helper.getHibernateSession();
			Integer newBatchId = 1;
			try {
				newBatchId += (Integer) session.createQuery("select max(batchID) from Prozess").uniqueResult();
			} catch (Exception e1) {
			}

			for (Prozess p : this.selectedProcesses) {
				p.setBatchID(newBatchId);
				p.setWikifield(WikiFieldHelper.getWikiMessage(p, p.getWikifield(), "debug", "added process to batch " + newBatchId));
//				try {
//					session.saveOrUpdate(p);
//				} catch (Exception e) {
//					Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//					logger.error(e);
//				}
			}
			try {
			this.dao.saveList(this.selectedProcesses);
//				session.flush();
//				session.connection().commit();
//			} catch (HibernateException e) {
//				Helper.setFehlerMeldung("Error, could not update", e.getMessage());
//				logger.error(e);
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Error, could not update", e.getMessage());
				logger.error(e);
			}
		}
		FilterAlleStart();
	}

	/*
	 * properties
	 */
	private BatchProcessHelper batchHelper;

	@SuppressWarnings("unchecked")
	public String editProperties() {
		if (this.selectedBatches.size() == 0) {
			Helper.setFehlerMeldung("noBatchSelected");
			return "";
		} else if (this.selectedBatches.size() > 1) {
			Helper.setFehlerMeldung("toḾanyBatchesSelected");
			return "";
		} else {
			if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals("") && !this.selectedBatches.get(0).equals("null")) {
				Session session = Helper.getHibernateSession();
				Criteria crit = session.createCriteria(Prozess.class);
				crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
//				List<Integer> ids = new ArrayList<Integer>();
				crit.add(Restrictions.eq("batchID", new Integer(this.selectedBatches.get(0))));
				List<Prozess> propertyBatch = crit.list();
				this.batchHelper = new BatchProcessHelper(propertyBatch);
				return "BatchProperties";
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
}
