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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.hibernate.FilterString;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

public class SearchForm {

	/**
	 * Logger instance.
	 */
	private static final Logger logger = Logger.getLogger(SearchForm.class);

	private List<String> projects = new ArrayList<String>(); // proj:
	private String project = "";

	private List<String> processPropertyTitles = new ArrayList<String>(); // processeig:
	private String processPropertyTitle = "";
	private String processPropertyValue = "";

	private List<String> masterpiecePropertyTitles = new ArrayList<String>(); // werk:
	private String masterpiecePropertyTitle = "";
	private String masterpiecePropertyValue = "";

	private List<String> templatePropertyTitles = new ArrayList<String>();// vorl:
	private String templatePropertyTitle = "";
	private String templatePropertyValue = "";

	private List<String> stepTitles = new ArrayList<String>(); // step:
	private List<StepStatus> stepstatus = new ArrayList<StepStatus>();
	private String status = "";
	private String stepname = "";

	private List<Benutzer> user = new ArrayList<Benutzer>();
	private String stepdonetitle = "";
	private String stepdoneuser = "";

	private String idin = "";
	private String processTitle = ""; // proc:

	private String projectOperand = "";
	private String processOperand ="";
	private String processPropertyOperand = "";
	private String masterpiecePropertyOperand = "";
	private String templatePropertyOperand = "";
	private String stepOperand = "";

	/**
	 * Initialise drop down list of master piece property titles
	 */
	protected void initMasterpiecePropertyTitles() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Werkstueckeigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.masterpiecePropertyTitles.add(Helper.getTranslation("notSelected"));
		try {
			@SuppressWarnings("unchecked")
			List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
			for (String result : results) {
				this.masterpiecePropertyTitles.add(result);
			}
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException.");
		}
	}

	/**
	 * Initialise drop down list of projects
	 */
	protected void initProjects() {
		int restriction = ((LoginForm) Helper.getManagedBeanValue("#{LoginForm}")).getMaximaleBerechtigung();
		Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Projekt.class);

		crit.addOrder(Order.asc("titel"));
		if (restriction > 2) {
			crit.add(Restrictions.not(Restrictions.eq("projectIsArchived", true)));
		}
		this.projects.add(Helper.getTranslation("notSelected"));

		try {
			@SuppressWarnings("unchecked")
			List<Projekt> projektList = crit.list();
			for (Projekt p : projektList) {
				this.projects.add(p.getTitel());
			}
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException.");
		}
	}

	/**
	 * Initialise drop down list of step status
	 */
	protected void initStepStatus() {
		for (StepStatus s : StepStatus.values()) {
			this.stepstatus.add(s);
		}
	}

	/**
	 * Initialise drop down list of template property titles
	 */
	protected void initTemplatePropertyTitles() {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Vorlageeigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.templatePropertyTitles.add(Helper.getTranslation("notSelected"));
		try {
			@SuppressWarnings("unchecked")
			List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
			for (String result : results) {
				this.templatePropertyTitles.add(result);
			}
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException.");
		}

	}

	@SuppressWarnings("unchecked")
	public SearchForm() {

		List<String> results;

		initStepStatus();
		initProjects();
		initMasterpiecePropertyTitles();
		initTemplatePropertyTitles();

		Session session = Helper.getHibernateSession();
		Criteria crit;

		crit = session.createCriteria(Prozesseigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.processPropertyTitles.add(Helper.getTranslation("notSelected"));
		try {
			results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
			for (String itstr : results) {
				if (itstr != null) {
					this.processPropertyTitles.add(itstr);
				}
			}
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException. Hibernate session maybe corrupted - recreating new hibernate session!");
			session = Helper.getHibernateSession();
		}

		crit = session.createCriteria(Schritt.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.stepTitles.add(Helper.getTranslation("notSelected"));
		try {
			results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
			for (String result : results) {
				this.stepTitles.add(result);
			}
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException. Hibernate session maybe corrupted - recreating new hibernate session!");
			session = Helper.getHibernateSession();
		}

		crit = session.createCriteria(Benutzer.class);
		crit.add(Restrictions.isNull("isVisible"));
		crit.add(Restrictions.eq("istAktiv", true));
		crit.addOrder(Order.asc("nachname"));
		crit.addOrder(Order.asc("vorname"));
		try {
			this.user.addAll(crit.list());
		} catch (RuntimeException rte) {
			logger.warn("Catched RuntimeException. Hibernate session maybe corrupted - recreating new hibernate session!");
		}

	}

	public List<String> getProjects() {
		return this.projects;
	}

	public void setProjects(List<String> projects) {
		this.projects = projects;
	}

	public List<String> getMasterpiecePropertyTitles() {
		return this.masterpiecePropertyTitles;
	}

	public void setMasterpiecePropertyTitles(List<String> masterpiecePropertyTitles) {
		this.masterpiecePropertyTitles = masterpiecePropertyTitles;
	}

	public List<String> getTemplatePropertyTitles() {
		return this.templatePropertyTitles;
	}

	public void setTemplatePropertyTitles(List<String> templatePropertyTitles) {
		this.templatePropertyTitles = templatePropertyTitles;
	}

	public List<String> getProcessPropertyTitles() {
		return this.processPropertyTitles;
	}

	public void setProcessPropertyTitles(List<String> processPropertyTitles) {
		this.processPropertyTitles = processPropertyTitles;
	}

	public List<String> getStepTitles() {
		return this.stepTitles;
	}

	public void setStepTitles(List<String> stepTitles) {
		this.stepTitles = stepTitles;
	}

	public List<StepStatus> getStepstatus() {
		return this.stepstatus;
	}

	public void setStepstatus(List<StepStatus> stepstatus) {
		this.stepstatus = stepstatus;
	}

	public String getStepdonetitle() {
		return this.stepdonetitle;
	}

	public void setStepdonetitle(String stepdonetitle) {
		this.stepdonetitle = stepdonetitle;
	}

	public String getStepdoneuser() {
		return this.stepdoneuser;
	}

	public void setStepdoneuser(String stepdoneuser) {
		this.stepdoneuser = stepdoneuser;
	}

	public String getIdin() {
		return this.idin;
	}

	public void setIdin(String idin) {
		this.idin = idin;
	}

	public String getProject() {
		return this.project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getProcessTitle() {
		return this.processTitle;
	}

	public void setProcessTitle(String processTitle) {
		this.processTitle = processTitle;
	}

	public String getProcessPropertyTitle() {
		return this.processPropertyTitle;
	}

	public void setProcessPropertyTitle(String processPropertyTitle) {
		this.processPropertyTitle = processPropertyTitle;
	}

	public String getProcessPropertyValue() {
		return this.processPropertyValue;
	}

	public void setProcessPropertyValue(String processPropertyValue) {
		this.processPropertyValue = processPropertyValue;
	}

	public String getMasterpiecePropertyTitle() {
		return this.masterpiecePropertyTitle;
	}

	public void setMasterpiecePropertyTitle(String masterpiecePropertyTitle) {
		this.masterpiecePropertyTitle = masterpiecePropertyTitle;
	}

	public String getMasterpiecePropertyValue() {
		return this.masterpiecePropertyValue;
	}

	public void setMasterpiecePropertyValue(String masterpiecePropertyValue) {
		this.masterpiecePropertyValue = masterpiecePropertyValue;
	}

	public String getTemplatePropertyTitle() {
		return this.templatePropertyTitle;
	}

	public void setTemplatePropertyTitle(String templatePropertyTitle) {
		this.templatePropertyTitle = templatePropertyTitle;
	}

	public String getTemplatePropertyValue() {
		return this.templatePropertyValue;
	}

	public void setTemplatePropertyValue(String templatePropertyValue) {
		this.templatePropertyValue = templatePropertyValue;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStepname() {
		return this.stepname;
	}

	public void setStepname(String stepname) {
		this.stepname = stepname;
	}

	public List<Benutzer> getUser() {
		return this.user;
	}

	public void setUser(List<Benutzer> user) {
		this.user = user;
	}

	public String filter() {
		String search = "";
		if (!this.processTitle.isEmpty()) {
			
			search += "\"" + this.processOperand +  this.processTitle + "\" ";
		}
		if (!this.idin.isEmpty()) {
			search += "\"" + FilterString.ID + this.idin + "\" ";
		}
		if (!this.project.isEmpty() && !this.project.equals(Helper.getTranslation("notSelected"))) {
			search += "\""+ this.projectOperand + FilterString.PROJECT + this.project + "\" ";
		}
		if (!this.processPropertyValue.isEmpty()) {
			if (!this.processPropertyTitle.isEmpty() && !this.processPropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search += "\""+ this.processPropertyOperand + FilterString.PROCESSPROPERTY + this.processPropertyTitle + ":" + this.processPropertyValue + "\" ";
			} else {
				search += "\""+ this.processPropertyOperand + FilterString.PROCESSPROPERTY + this.processPropertyValue + "\" ";
			}
		}
		if (!this.masterpiecePropertyValue.isEmpty()) {
			if (!this.masterpiecePropertyTitle.isEmpty() && !this.masterpiecePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search += "\""+ this.masterpiecePropertyOperand + FilterString.WORKPIECE + this.masterpiecePropertyTitle + ":" + this.masterpiecePropertyValue + "\" ";
			} else {
				search += "\""+ this.masterpiecePropertyOperand + FilterString.WORKPIECE + this.masterpiecePropertyValue + "\" ";
			}
		}
		if (!this.templatePropertyValue.isEmpty()) {
			if (!this.templatePropertyTitle.isEmpty() && !this.templatePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search += "\""+ this.templatePropertyOperand + FilterString.TEMPLATE + this.templatePropertyTitle + ":" + this.templatePropertyValue + "\" ";
			} else {
				search += "\""+ this.templatePropertyOperand + FilterString.TEMPLATE + this.templatePropertyValue + "\" ";
			}
		}

		if (!this.stepname.isEmpty() && !this.stepname.equals(Helper.getTranslation("notSelected"))) {
			search += "\""+ this.stepOperand +  this.status + ":" + this.stepname + "\" ";
		}
		if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty() && !this.stepdonetitle.equals(Helper.getTranslation("notSelected"))) {
			search += "\"" + FilterString.STEPDONEUSER + this.stepdoneuser + "\" \"" + FilterString.STEPDONETITLE + this.stepdonetitle + "\" ";
		}
		ProzessverwaltungForm form = (ProzessverwaltungForm) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get("ProzessverwaltungForm");
		if (form != null) {
			form.filter = search;
			form.setModusAnzeige("aktuell");
			return form.FilterAlleStart();
		}
		return "";
	}

	public List<SelectItem> getOperands() {
		List<SelectItem> answer = new ArrayList<SelectItem>();
		SelectItem and = new SelectItem("", Helper.getTranslation("AND"));
		SelectItem not = new SelectItem("-", Helper.getTranslation("NOT"));
		answer.add(and);
		answer.add(not);
		return answer;
	}

	public String getProjectOperand() {
		return this.projectOperand;
	}

	public void setProjectOperand(String projectOperand) {
		this.projectOperand = projectOperand;
	}

	public String getProcessPropertyOperand() {
		return this.processPropertyOperand;
	}

	public void setProcessPropertyOperand(String processPropertyOperand) {
		this.processPropertyOperand = processPropertyOperand;
	}

	public String getMasterpiecePropertyOperand() {
		return this.masterpiecePropertyOperand;
	}

	public void setMasterpiecePropertyOperand(String masterpiecePropertyOperand) {
		this.masterpiecePropertyOperand = masterpiecePropertyOperand;
	}

	public String getTemplatePropertyOperand() {
		return this.templatePropertyOperand;
	}

	public void setTemplatePropertyOperand(String templatePropertyOperand) {
		this.templatePropertyOperand = templatePropertyOperand;
	}

	public String getStepOperand() {
		return this.stepOperand;
	}

	public void setStepOperand(String stepOperand) {
		this.stepOperand = stepOperand;
	}

	public String getProcessOperand() {
		return this.processOperand;
	}

	public void setProcessOperand(String processOperand) {
		this.processOperand = processOperand;
	}

}
