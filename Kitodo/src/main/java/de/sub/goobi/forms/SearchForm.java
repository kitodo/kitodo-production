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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.hibernate.FilterString;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.TaskStatus;

public class SearchForm {

    /**
     * Logger instance.
     */
    private static final Logger logger = LogManager.getLogger(SearchForm.class);

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
    private List<TaskStatus> stepstatus = new ArrayList<TaskStatus>();
    private String status = "";
    private String stepname = "";

    private List<User> user = new ArrayList<User>();
    private String stepdonetitle = "";
    private String stepdoneuser = "";

    private String idin = "";
    private String processTitle = ""; // proc:

    private String projectOperand = "";
    private String processOperand = "";
    private String processPropertyOperand = "";
    private String masterpiecePropertyOperand = "";
    private String templatePropertyOperand = "";
    private String stepOperand = "";

    /**
     * Initialise drop down list of master piece property titles.
     */
    protected void initMasterpiecePropertyTitles() {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Property.class);
        crit.addOrder(Order.asc("titel"));
        crit.setProjection(Projections.distinct(Projections.property("title")));
        this.masterpiecePropertyTitles.add(Helper.getTranslation("notSelected"));
        try {
            @SuppressWarnings("unchecked")
            List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
            for (String result : results) {
                this.masterpiecePropertyTitles.add(result);
            }
        } catch (HibernateException hbe) {
            logger.warn("Catched HibernateException. List of master piece property titles could be empty!");
        }
    }

    /**
     * Initialise drop down list of projects.
     */
    protected void initProjects() {
        int restriction = ((LoginForm) Helper.getManagedBeanValue("#{LoginForm}")).getMaximaleBerechtigung();
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Project.class);

        crit.addOrder(Order.asc("title"));
        if (restriction > 2) {
            crit.add(Restrictions.not(Restrictions.eq("projectIsArchived", true)));
        }
        this.projects.add(Helper.getTranslation("notSelected"));

        try {
            @SuppressWarnings("unchecked")
            List<Project> projektList = crit.list();
            for (Project p : projektList) {
                this.projects.add(p.getTitle());
            }
        } catch (HibernateException hbe) {
            logger.warn("Catched HibernateException. List of projects could be empty!");
        }
    }

    /**
     * Initialise drop down list of process property titles.
     */
    protected void initProcessPropertyTitles() {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Property.class);
        crit.addOrder(Order.asc("title"));
        crit.setProjection(Projections.distinct(Projections.property("title")));
        this.processPropertyTitles.add(Helper.getTranslation("notSelected"));
        try {
            @SuppressWarnings("unchecked")
            List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
            for (String itstr : results) {
                if (itstr != null) {
                    this.processPropertyTitles.add(itstr);
                }
            }
        } catch (HibernateException hbe) {
            logger.warn("Catched HibernateException. List of process property titles could be empty!");
        }
    }

    /**
     * Initialise drop down list of step status.
     */
    protected void initStepStatus() {
        for (TaskStatus s : TaskStatus.values()) {
            this.stepstatus.add(s);
        }
    }

    /**
     * Initialise drop down list of step titles.
     */
    protected void initStepTitles() {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Task.class);
        crit.addOrder(Order.asc("title"));
        crit.setProjection(Projections.distinct(Projections.property("title")));
        this.stepTitles.add(Helper.getTranslation("notSelected"));
        try {
            @SuppressWarnings("unchecked")
            List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
            for (String result : results) {
                this.stepTitles.add(result);
            }
        } catch (HibernateException hbe) {
            logger.warn("Catched HibernateException. List of step titles could be empty!");
        }
    }

    /**
     * Initialise drop down list of template property titles.
     */
    protected void initTemplatePropertyTitles() {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Property.class);
        crit.addOrder(Order.asc("title"));
        crit.setProjection(Projections.distinct(Projections.property("title")));
        this.templatePropertyTitles.add(Helper.getTranslation("notSelected"));
        try {
            @SuppressWarnings("unchecked")
            List<String> results = crit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list();
            for (String result : results) {
                this.templatePropertyTitles.add(result);
            }
        } catch (HibernateException hbe) {
            logger.warn("Catched HibernateException. List of template property titles could be empty!");
        }
    }

    /**
     * Initialise drop down list of user list.
     */
    protected void initUserList() {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(User.class);
        crit.add(Restrictions.isNull("isVisible"));
        crit.add(Restrictions.eq("istActive", true));
        crit.addOrder(Order.asc("surname"));
        crit.addOrder(Order.asc("name"));
        try {
            this.user.addAll(crit.list());
        } catch (RuntimeException rte) {
            logger.warn("Catched RuntimeException. List of users could be empty!");
        }
    }

    /**
     * Constructor.
     */
    public SearchForm() {
        initStepStatus();
        initProjects();
        initMasterpiecePropertyTitles();
        initTemplatePropertyTitles();
        initProcessPropertyTitles();
        initStepTitles();
        initUserList();
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

    public List<TaskStatus> getStepstatus() {
        return this.stepstatus;
    }

    public void setStepstatus(List<TaskStatus> stepstatus) {
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

    public List<User> getUser() {
        return this.user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    /**
     * Filter.
     *
     * @return String
     */
    public String filter() {
        String search = "";
        if (!this.processTitle.isEmpty()) {

            search += "\"" + this.processOperand + this.processTitle + "\" ";
        }
        if (!this.idin.isEmpty()) {
            search += "\"" + FilterString.ID + this.idin + "\" ";
        }
        if (!this.project.isEmpty() && !this.project.equals(Helper.getTranslation("notSelected"))) {
            search += "\"" + this.projectOperand + FilterString.PROJECT + this.project + "\" ";
        }
        if (!this.processPropertyValue.isEmpty()) {
            if (!this.processPropertyTitle.isEmpty()
                    && !this.processPropertyTitle.equals(Helper.getTranslation("notSelected"))) {
                search += "\"" + this.processPropertyOperand + FilterString.PROCESSPROPERTY + this.processPropertyTitle
                        + ":" + this.processPropertyValue + "\" ";
            } else {
                search += "\"" + this.processPropertyOperand + FilterString.PROCESSPROPERTY + this.processPropertyValue
                        + "\" ";
            }
        }
        if (!this.masterpiecePropertyValue.isEmpty()) {
            if (!this.masterpiecePropertyTitle.isEmpty()
                    && !this.masterpiecePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
                search += "\"" + this.masterpiecePropertyOperand + FilterString.WORKPIECE
                        + this.masterpiecePropertyTitle + ":" + this.masterpiecePropertyValue + "\" ";
            } else {
                search += "\"" + this.masterpiecePropertyOperand + FilterString.WORKPIECE
                        + this.masterpiecePropertyValue + "\" ";
            }
        }
        if (!this.templatePropertyValue.isEmpty()) {
            if (!this.templatePropertyTitle.isEmpty()
                    && !this.templatePropertyTitle.equals(Helper.getTranslation("notSelected"))) {
                search += "\"" + this.templatePropertyOperand + FilterString.TEMPLATE + this.templatePropertyTitle + ":"
                        + this.templatePropertyValue + "\" ";
            } else {
                search += "\"" + this.templatePropertyOperand + FilterString.TEMPLATE + this.templatePropertyValue
                        + "\" ";
            }
        }

        if (!this.stepname.isEmpty() && !this.stepname.equals(Helper.getTranslation("notSelected"))) {
            search += "\"" + this.stepOperand + this.status + ":" + this.stepname + "\" ";
        }
        if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty()
                && !this.stepdonetitle.equals(Helper.getTranslation("notSelected"))
                && ConfigCore.getBooleanParameter("withUserStepDoneSearch")) {
            search += "\"" + FilterString.STEPDONEUSER + this.stepdoneuser + "\" \"" + FilterString.STEPDONETITLE
                    + this.stepdonetitle + "\" ";
        }
        ProzessverwaltungForm form = (ProzessverwaltungForm) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("ProzessverwaltungForm");
        if (form != null) {
            form.filter = search;
            form.setModusAnzeige("aktuell");
            return form.FilterAlleStart();
        }
        return null;
    }

    /**
     * Get operands.
     *
     * @return list of SelectItem objects
     */
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
