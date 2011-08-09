package de.sub.goobi.Forms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Beans.Schritteigenschaft;
import de.sub.goobi.Beans.Vorlageeigenschaft;
import de.sub.goobi.Beans.Werkstueckeigenschaft;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

public class SearchForm {

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

	private List<String> stepPropertyTitles = new ArrayList<String>(); // stepeig:
	private String stepPropertyTitle = "";
	private String stepPropertyValue = "";

	private List<String> stepTitles = new ArrayList<String>(); // step:
	private List<String> stepstatus = new ArrayList<String>();
	private String status = "";
	private String stepname = "";

	private List<Benutzer> user = new ArrayList<Benutzer>();
	private String stepdonetitle = "";
	private String stepdoneuser = "";

	private String idin = "";
	private String processTitle = ""; // proc:

	public SearchForm() {
		for (StepStatus s : StepStatus.values()) {
			this.stepstatus.add(s.getTitle());
		}
//long start = System.currentTimeMillis();
		Session session = Helper.getHibernateSession();

		// projects
		Criteria crit = session.createCriteria(Projekt.class);
		crit.addOrder(Order.asc("titel"));
		this.projects.add(Helper.getTranslation("notSelected"));
		@SuppressWarnings("unchecked")
		List<Projekt> projektList = crit.list();
		for (Projekt p : projektList) {
			this.projects.add(p.getTitel());
		}

		crit = session.createCriteria(Werkstueckeigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.masterpiecePropertyTitles.add(Helper.getTranslation("notSelected"));
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			this.masterpiecePropertyTitles.add((String) it.next());
		}

		crit = session.createCriteria(Vorlageeigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.templatePropertyTitles.add(Helper.getTranslation("notSelected"));
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			this.templatePropertyTitles.add((String) it.next());
		}

		crit = session.createCriteria(Prozesseigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.processPropertyTitles.add(Helper.getTranslation("notSelected"));
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			this.processPropertyTitles.add((String) it.next());
		}

		crit = session.createCriteria(Schritteigenschaft.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.stepPropertyTitles.add(Helper.getTranslation("notSelected"));
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			this.stepPropertyTitles.add((String) it.next());
		}

		crit = session.createCriteria(Schritt.class);
		crit.addOrder(Order.asc("titel"));
		crit.setProjection(Projections.distinct(Projections.property("titel")));
		this.stepTitles.add(Helper.getTranslation("notSelected"));
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
			this.stepTitles.add((String) it.next());
		}

		crit = session.createCriteria(Benutzer.class);
		crit.add(Restrictions.isNull("isVisible"));
		crit.add(Restrictions.eq("istAktiv", true));
		crit.addOrder(Order.asc("nachname"));
		crit.addOrder(Order.asc("vorname"));
//		this.user.add(Helper.getTranslation("notSelected"));
		this.user.addAll(crit.list()); 
//		 long end = System.currentTimeMillis();
//		 System.out.print("dauer: ");
//		 System.out.print(end-start);

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

	public List<String> getStepPropertyTitles() {
		return this.stepPropertyTitles;
	}

	public void setStepPropertyTitles(List<String> stepPropertyTitles) {
		this.stepPropertyTitles = stepPropertyTitles;
	}

	public List<String> getStepTitles() {
		return this.stepTitles;
	}

	public void setStepTitles(List<String> stepTitles) {
		this.stepTitles = stepTitles;
	}

	public List<String> getStepstatus() {
		return this.stepstatus;
	}

	public void setStepstatus(List<String> stepstatus) {
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

	public String getStepPropertyTitle() {
		return this.stepPropertyTitle;
	}

	public void setStepPropertyTitle(String stepPropertyTitle) {
		this.stepPropertyTitle = stepPropertyTitle;
	}

	public String getStepPropertyValue() {
		return this.stepPropertyValue;
	}

	public void setStepPropertyValue(String stepPropertyValue) {
		this.stepPropertyValue = stepPropertyValue;
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
			search += this.processTitle + " ";
		}
		if (!this.project.isEmpty() && !this.project.equals(Helper.getTranslation("notSelected"))) {
			search += "\"proj:" + this.project + "\" ";
		}
		if (!this.processPropertyValue.isEmpty()) {
			if (!this.processPropertyTitle.isEmpty() && ! this.processPropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search+="\"processeig:"+this.processPropertyTitle+":"+this.processPropertyValue + "\" ";
			} else {
				search+="\"processeig:"+this.processPropertyValue + "\" ";
			}
		}
		if (!this.masterpiecePropertyValue.isEmpty()) {
			if (!this.masterpiecePropertyTitle.isEmpty() && !this.masterpiecePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search+="\"werk:"+this.masterpiecePropertyTitle+":"+this.masterpiecePropertyValue + "\" ";
			} else {
				search+="\"werk:"+this.masterpiecePropertyValue + "\" ";
			}
		}
		if (!this.templatePropertyValue.isEmpty()) {
			if (!this.templatePropertyTitle.isEmpty() && !this.templatePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search+="\"vorl:"+this.templatePropertyTitle+":"+this.templatePropertyValue + "\" ";
			} else {
				search+="\"vorl:"+this.templatePropertyValue + "\" ";
			}
		}
		if (!this.stepPropertyValue.isEmpty() && !this.stepname.isEmpty()) {
			if (!this.stepPropertyTitle.isEmpty() && !this.stepPropertyTitle.equals(Helper.getTranslation("notSelected"))) {
				search+="\"stepeig:"+this.stepPropertyTitle+":"+this.stepPropertyValue + "\" ";
			} else {
				search+="\"stepeig:"+this.stepPropertyValue + "\" ";
			}
		}
		
		if (!this.stepname.isEmpty() && !this.stepname.equals(Helper.getTranslation("notSelected"))) {
			search += "\"" + this.status + ":" + this.stepname + "\" ";
		}
		if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty() && !this.stepdonetitle.equals(Helper.getTranslation("notSelected"))) {
			search += "\"stepdoneuser:"  + this.stepdoneuser + "\" \"stepDoneTitle:" + this.stepdonetitle + "\" ";
		}
		
		ProzessverwaltungForm form = (ProzessverwaltungForm) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get("ProzessverwaltungForm");
		if (form!=null) {
			form.filter = search;
			return form.FilterAlleStart();
//		} else {
//			form = new ProzessverwaltungForm();
//			form.filter = search;
//			form.setShowArchivedProjects(true);
//			return form.FilterAlleStart();
		}
		
//		System.out.println(search);
		return "";
	}
	
	
}
