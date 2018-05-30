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
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.FilterString;
import org.kitodo.enums.ObjectMode;
import org.kitodo.services.ServiceManager;

@Named("SearchForm")
@RequestScoped
public class SearchForm {

    /**
     * Logger instance.
     */
    private static final Logger logger = LogManager.getLogger(SearchForm.class);

    private List<String> projects = new ArrayList<>(); // proj:
    private String project = "";

    private List<String> processPropertyTitles = new ArrayList<>(); // processeig:
    private String processPropertyTitle = "";
    private String processPropertyValue = "";

    private List<String> masterpiecePropertyTitles = new ArrayList<>(); // werk:
    private String masterpiecePropertyTitle = "";
    private String masterpiecePropertyValue = "";

    private List<String> templatePropertyTitles = new ArrayList<>();// vorl:
    private String templatePropertyTitle = "";
    private String templatePropertyValue = "";

    private List<String> stepTitles = new ArrayList<>(); // step:
    private List<TaskStatus> stepstatus = new ArrayList<>();
    private String status = "";
    private String stepname = "";

    private List<User> user = new ArrayList<>();
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

    private ServiceManager serviceManager = new ServiceManager();

    @Inject
    BeanManager beanManager;

    /**
     * Initialise drop down list of master piece property titles.
     */
    protected void initMasterpiecePropertyTitles() {
        List<String> workpiecePropertiesTitlesDistinct = new ArrayList<>();
        try {
            workpiecePropertiesTitlesDistinct = serviceManager.getPropertyService()
                    .findWorkpiecePropertiesTitlesDistinct();
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.masterpiecePropertyTitles = workpiecePropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of projects.
     */
    protected void initProjects() {
        List<Project> projects;
        // TODO Change to check the corresponding authority
        if (serviceManager.getSecurityAccessService().isAdmin()) {
            projects = serviceManager.getProjectService().getAllActiveProjectsSortedByTitle();
        } else {
            projects = serviceManager.getProjectService().getAllProjectsSortedByTitle();
        }
        for (Project project : projects) {
            this.projects.add(project.getTitle());
        }
    }

    /**
     * Initialise drop down list of process property titles.
     */
    protected void initProcessPropertyTitles() {
        List<String> processPropertiesTitlesDistinct = new ArrayList<>();
        try {
            processPropertiesTitlesDistinct = serviceManager.getPropertyService()
                    .findProcessPropertiesTitlesDistinct();
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.processPropertyTitles = processPropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of step status.
     */
    protected void initStepStatus() {
        this.stepstatus.addAll(Arrays.asList(TaskStatus.values()));
    }

    /**
     * Initialise drop down list of task titles.
     */
    protected void initStepTitles() {
        List<String> taskTitles = new ArrayList<>();
        try {
            taskTitles = serviceManager.getTaskService().findTaskTitlesDistinct();
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.stepTitles = taskTitles;
    }

    /**
     * Initialise drop down list of template property titles.
     */
    protected void initTemplatePropertyTitles() {
        List<String> templatePropertiesTitlesDistinct = new ArrayList<>();
        try {
            templatePropertiesTitlesDistinct = serviceManager.getPropertyService()
                    .findTemplatePropertiesTitlesDistinct();
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        this.templatePropertyTitles = templatePropertiesTitlesDistinct;
    }

    /**
     * Initialise drop down list of user list.
     */
    protected void initUserList() {
        try {
            this.user.addAll(serviceManager.getUserService().getAllActiveUsersSortedByNameAndSurname());
        } catch (RuntimeException rte) {
            logger.warn("RuntimeException caught. List of users could be empty!");
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("aktiveBenutzer") });
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
    @SuppressWarnings("unchecked")
    public String filter() {
        String search = "";
        if (!this.processTitle.isEmpty()) {

            search += "\"" + this.processOperand + this.processTitle + "\" ";
        }
        if (!this.idin.isEmpty()) {
            search += "\"" + FilterString.ID.getFilterEnglish() + this.idin + "\" ";
        }
        if (!this.project.isEmpty()) {
            search += "\"" + this.projectOperand + FilterString.PROJECT.getFilterEnglish() + this.project + "\" ";
        }
        if (!this.processPropertyValue.isEmpty()) {
            if (!this.processPropertyTitle.isEmpty()) {
                search += "\"" + this.processPropertyOperand + FilterString.PROCESSPROPERTY.getFilterEnglish()  + this.processPropertyTitle
                        + ":" + this.processPropertyValue + "\" ";
            } else {
                search += "\"" + this.processPropertyOperand + FilterString.PROCESSPROPERTY.getFilterEnglish()  + this.processPropertyValue
                        + "\" ";
            }
        }
        if (!this.masterpiecePropertyValue.isEmpty()) {
            if (!this.masterpiecePropertyTitle.isEmpty()) {
                search += "\"" + this.masterpiecePropertyOperand + FilterString.WORKPIECE.getFilterEnglish()
                        + this.masterpiecePropertyTitle + ":" + this.masterpiecePropertyValue + "\" ";
            } else {
                search += "\"" + this.masterpiecePropertyOperand + FilterString.WORKPIECE.getFilterEnglish()
                        + this.masterpiecePropertyValue + "\" ";
            }
        }
        if (!this.templatePropertyValue.isEmpty()) {
            if (!this.templatePropertyTitle.isEmpty()) {
                search += "\"" + this.templatePropertyOperand + FilterString.TEMPLATE.getFilterEnglish()  + this.templatePropertyTitle + ":"
                        + this.templatePropertyValue + "\" ";
            } else {
                search += "\"" + this.templatePropertyOperand + FilterString.TEMPLATE.getFilterEnglish()  + this.templatePropertyValue
                        + "\" ";
            }
        }

        if (!this.stepname.isEmpty()) {
            search += "\"" + this.stepOperand + this.status + ":" + this.stepname + "\" ";
        }
        if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty()
                && ConfigCore.getBooleanParameter("withUserStepDoneSearch")) {
            search += "\"" + FilterString.TASKDONEUSER.getFilterEnglish() + this.stepdoneuser + "\" \""
                    + FilterString.TASKDONETITLE.getFilterEnglish() + this.stepdonetitle + "\" ";
        }

        Bean<ProzessverwaltungForm> bean = (Bean<ProzessverwaltungForm>) beanManager
                .resolve(beanManager.getBeans(ProzessverwaltungForm.class));
        ProzessverwaltungForm form = beanManager.getContext(bean.getScope()).get(bean,
                beanManager.createCreationalContext(bean));

        if (form != null) {
            form.filter = search;
            form.setDisplayMode(ObjectMode.PROCESS);
            return form.processListPath;
        }
        return null;
    }

    /**
     * Get operands.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getOperands() {
        List<SelectItem> answer = new ArrayList<>();
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
