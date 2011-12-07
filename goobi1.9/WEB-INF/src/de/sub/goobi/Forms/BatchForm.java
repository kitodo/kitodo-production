package de.sub.goobi.Forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.Helper;

public class BatchForm extends BasisForm {

	private static final long serialVersionUID = 8234897225425856549L;

	private static final Logger logger = Logger.getLogger(BatchForm.class);

	private List<Prozess> currentProcesses;
	private List<Prozess> selectedProcesses;
	private List<Integer> currentBatches;
	private List<Integer> selectedBatches;
	private String batchfilter;
	private String processfilter;
	private IEvaluableFilter myFilteredDataSource;
	private int MAX_HITS = 100;

	public List<Prozess> getCurrentProcesses() {
		return this.currentProcesses;
	}

	public void setCurrentProcesses(List<Prozess> currentProcesses) {
		this.currentProcesses = currentProcesses;
	}

	private void cleanData() {
		this.processfilter = "";
		this.batchfilter = "";
		this.selectedBatches = new ArrayList<Integer>();
		this.selectedProcesses = new ArrayList<Prozess>();
	}

	public void loadBatchData() {
		this.selectedBatches = new ArrayList<Integer>();
		this.currentBatches = new ArrayList<Integer>();
		for (Prozess p : this.selectedProcesses) {
			if (p.getBatchID() != null && !this.currentBatches.contains(p.getBatchID())) {
				this.currentBatches.add(p.getBatchID());
			}
		}
		cleanData();
	}

	public void loadProcessData() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Prozess.class);
		crit.setMaxResults(this.MAX_HITS);
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		if (this.selectedBatches.size() > 0) {
			crit.add(Restrictions.in("batchID", this.selectedBatches));
		}
		this.currentProcesses = crit.list();
		cleanData();
	}

	public void filterProcesses() {

		if (this.processfilter == null) {
			this.processfilter = "";
		}
		this.myFilteredDataSource = new UserDefinedFilter(this.processfilter);
		Criteria crit = this.myFilteredDataSource.getCriteria();
		crit.add(Restrictions.eq("istTemplate", Boolean.valueOf(false)));
		crit.setMaxResults(this.MAX_HITS);
		this.currentProcesses = crit.list();
		cleanData();
	}

	public void filterBatches() {
		Integer number = null;
		try {
			number = new Integer(this.batchfilter);
		} catch (Exception e) {
			logger.warn("NAN Exception: " + this.batchfilter);
		}
		if (number != null) {
			Session session = Helper.getHibernateSession();
			Query query = session.createQuery("select distinct batchID from Prozess");
			query.setMaxResults(this.MAX_HITS);
			List<Integer> allBatches = query.list();
			this.currentBatches = new ArrayList<Integer>();
			for (Integer in : allBatches) {
				if (in != null && Integer.toString(in).contains(this.batchfilter)) {
					this.currentBatches.add(in);
				}
			}
		} else {
			Session session = Helper.getHibernateSession();
			Query query = session.createQuery("select distinct batchID from Prozess");
			query.setMaxResults(this.MAX_HITS);
			this.currentBatches = query.list();
			if (this.currentBatches.contains(null)) {
				this.currentBatches.remove(null);
			}
		}
		cleanData();
	}

	public List<SelectItem> getCurrentProcessesAsSelectItems() {
		List<SelectItem> answer = new ArrayList<SelectItem>();
		for (Prozess p : this.currentProcesses) {
			answer.add(new SelectItem(p, p.getTitel()));
		}
		return answer;
	}

	public List<SelectItem> getCurrentBatchesAsSelectItems() {
		List<SelectItem> answer = new ArrayList<SelectItem>();
		Session session = Helper.getHibernateSession();
		for (int in : this.currentBatches) {
			Criteria crit = session.createCriteria(Prozess.class);
			crit.add(Restrictions.eq("batchID", in));
			// TODO text aus message generieren
			String text = "Batch " + in + " (" + crit.list().size() + " Vorgänge)";
			answer.add(new SelectItem(in, text));
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

	public List<Integer> getCurrentBatches() {
		return this.currentBatches;
	}

	public void setCurrentBatches(List<Integer> currentBatches) {
		this.currentBatches = currentBatches;
	}

	public List<Prozess> getSelectedProcesses() {
		return this.selectedProcesses;
	}

	public void setSelectedProcesses(List<Prozess> selectedProcesses) {
		this.selectedProcesses = selectedProcesses;
	}

	public List<Integer> getSelectedBatches() {
		return this.selectedBatches;
	}

	public void setSelectedBatches(List<Integer> selectedBatches) {
		this.selectedBatches = selectedBatches;
	}

	public String FilterAlleStart() {
		filterBatches();
		filterProcesses();
		return "BatchesAll";
	}
}
