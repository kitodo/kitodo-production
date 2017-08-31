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

package org.goobi.production.flow.statistics.hibernate;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.forms.LoginForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.IlikeExpression;
import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter.Parameters;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.enums.FilterString;
import org.kitodo.services.ServiceManager;

/**
 * class provides methods used by implementations of IEvaluableFilter.
 *
 * @author Wulf Riebensahm
 */
public class FilterHelper {
    private static final Logger logger = LogManager.getLogger(FilterHelper.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * limit query to project (formerly part of ProzessverwaltungForm).
     */
    protected static void limitToUserAccessRights(Conjunction con) {
        /* restriction to specific projects if not with admin rights */
        LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        User aktuellerNutzer = null;
        try {
            if (loginForm != null && loginForm.getMyBenutzer() != null) {
                aktuellerNutzer = serviceManager.getUserService().getById(loginForm.getMyBenutzer().getId());
            }
        } catch (DAOException e) {
            logger.warn("DAOException", e);
        } catch (Exception e) {
            logger.trace("Exception", e);
        }
        if (aktuellerNutzer != null) {
            if (loginForm.getMaximaleBerechtigung() > 1) {
                Disjunction dis = Restrictions.disjunction();
                for (Project proj : aktuellerNutzer.getProjects()) {
                    dis.add(Restrictions.eq("project", proj));
                }
                con.add(dis);
            }
        }
    }

    private static void limitToUserAssignedSteps(Conjunction con, Boolean stepOpenOnly, Boolean userAssignedStepsOnly) {
        /* show only open Steps or those in use by current user */

        Session session = Helper.getHibernateSession();
        /* identify current user */
        LoginForm login = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
        if (login == null || login.getMyBenutzer() == null) {
            return;
        }
        /* init id-list, preset with item 0 */
        List<Integer> idList = new ArrayList<>();
        idList.add(0);

        /*
         * hits by user groups
         */
        Criteria critGroups = session.createCriteria(Task.class);

        if (stepOpenOnly) {
            critGroups.add(Restrictions.eq("processingStatus", 1));
        } else if (userAssignedStepsOnly) {
            critGroups.add(Restrictions.eq("processingStatus", 2));
            critGroups.add(Restrictions.eq("processingUser.id", login.getMyBenutzer().getId()));
        } else {
            critGroups.add(
                    Restrictions.or(Restrictions.eq("processingStatus", 1), Restrictions.like("processingStatus", 2)));
        }

        /* only processes which are not templates */
        Criteria template = critGroups.createCriteria("process", "process");
        critGroups.add(Restrictions.eq("process.template", Boolean.FALSE));

        /* only assigned projects */
        template.createCriteria("project", "project").createCriteria("users", "projectUsers");
        critGroups.add(Restrictions.eq("projectUsers.id", login.getMyBenutzer().getId()));

        /*
         * only steps assigned to the user groups the current user is member of
         */
        critGroups.createCriteria("userGroups", "userGroups").createCriteria("users", "users");
        critGroups.add(Restrictions.eq("users.id", login.getMyBenutzer().getId()));

        /* collecting the hits */
        critGroups.setProjection(Projections.id());
        for (Object o : critGroups.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list()) {
            idList.add((Integer) o);
        }

        /*
         * Users only
         */
        Criteria critUser = session.createCriteria(Task.class);

        if (stepOpenOnly) {
            critUser.add(Restrictions.eq("processingStatus", 1));
        } else if (userAssignedStepsOnly) {
            critUser.add(Restrictions.eq("processingStatus", 2));
            critUser.add(Restrictions.eq("processingUser.id", login.getMyBenutzer().getId()));
        } else {
            critUser.add(
                    Restrictions.or(Restrictions.eq("processingStatus", 1), Restrictions.like("processingStatus", 2)));
        }

        /* exclude templates */
        Criteria secondTemplate = critUser.createCriteria("process", "process");
        critUser.add(Restrictions.eq("process.template", Boolean.FALSE));

        /* check project assignment */
        secondTemplate.createCriteria("project", "project").createCriteria("users", "projectUsers");
        critUser.add(Restrictions.eq("projectUsers.id", login.getMyBenutzer().getId()));

        /* only steps where the user is assigned to */
        critUser.createCriteria("processingUser", "processingUser");
        critUser.add(Restrictions.eq("processingUser.id", login.getMyBenutzer().getId()));

        /* collecting the hits */

        critUser.setProjection(Projections.id());
        for (Object o : critUser.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list()) {
            idList.add((Integer) o);
        }

        /*
         * only taking the hits by restricting to the ids
         */
        con.add(Restrictions.in("id", idList));
    }

    /**
     * This functions extracts the Integer from the parameters passed with the
     * step filter in first position.
     *
     * @param parameter
     *            the string, where the integer should be extracted
     * @return Integer
     */
    protected static Integer getStepStart(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[0]);
    }

    /**
     * This functions extracts the Integer from the parameters passed with the
     * step filter in last position.
     *
     * @param parameter
     *            String
     * @return Integer
     */
    private static Integer getStepEnd(String parameter) {
        String[] strArray = parameter.split("-");
        return Integer.parseInt(strArray[1]);
    }

    /**
     * This function analyzes the parameters on a step filter and returns a
     * StepFilter enum to direct further processing it reduces the necessity to
     * apply some filter keywords.
     *
     * @param parameters
     *            String
     * @return StepFilter
     */
    private static StepFilter getStepFilter(String parameters) {

        if (parameters.contains("-")) {
            String[] strArray = parameters.split("-");
            if (!(strArray.length < 2)) {
                if (strArray[0].length() == 0) {
                    return StepFilter.max;
                } else {
                    return StepFilter.range;
                }
            } else {
                return StepFilter.min;
            }
        } else if (!parameters.contains("-")) {
            try {
                // check if parseInt throws an exception
                Integer.parseInt(parameters);
                return StepFilter.exact;
            } catch (NumberFormatException e) {
                return StepFilter.name;
            }
        }
        return StepFilter.unknown;
    }

    /**
     * This enum represents the result of parsing the step&lt;modifier&gt;:
     * filter Restrictions.
     */
    private enum StepFilter {
        exact, range, min, max, name, unknown
    }

    /**
     * Filter processes for done steps range.
     *
     * @param con
     *            Conjunction object
     * @param parameters
     *            String
     * @param inStatus
     *            TaskStatus object
     * @param negate
     *            boolean
     * @param prefix
     *            {@link TaskStatus} of searched step
     */
    private static void filterStepRange(Conjunction con, String parameters, TaskStatus inStatus, boolean negate,
            String prefix) {
        if (!negate) {
            con.add(Restrictions.and(
                    Restrictions.and(Restrictions.ge(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                            Restrictions.le(prefix + "ordering", FilterHelper.getStepEnd(parameters))),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue())));
        } else {
            con.add(Restrictions.not(Restrictions.and(
                    Restrictions.and(Restrictions.ge(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                            Restrictions.le(prefix + "ordering", FilterHelper.getStepEnd(parameters))),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue()))));
        }
    }

    /**
     * Filter processes for steps name with given status.
     *
     * @param inStatus
     *            {@link TaskStatus} of searched step
     * @param parameters
     *            part of filter string to use
     */
    private static void filterStepName(Conjunction con, String parameters, TaskStatus inStatus, boolean negate,
            String prefix) {
        if (con == null) {
            con = Restrictions.conjunction();
        }
        if (!negate) {
            con.add(Restrictions.and(Restrictions.like(prefix + "title", "%" + parameters + "%"),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue())));
        } else {
            con.add(Restrictions.not(Restrictions.and(Restrictions.like(prefix + "title", "%" + parameters + "%"),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue()))));
        }
    }

    /**
     * Filter processes for steps name with given status.
     */
    private static void filterAutomaticSteps(Conjunction con, String tok, boolean flagSteps) {
        if (con == null) {
            con = Restrictions.conjunction();
        }
        if (!flagSteps) {
            if (tok.substring(tok.indexOf(":") + 1).equalsIgnoreCase("true")) {
                con.add(Restrictions.eq("steps.typeAutomatic", true));
            } else {
                con.add(Restrictions.eq("steps.typeAutomatic", false));
            }
        } else {
            if (tok.substring(tok.indexOf(":") + 1).equalsIgnoreCase("true")) {
                con.add(Restrictions.eq("typeAutomatic", true));
            } else {
                con.add(Restrictions.eq("typeAutomatic", false));
            }
        }
    }

    /**
     * Filter processes for done steps min.
     *
     * @param parameters
     *            part of filter string to use
     * @param inStatus
     *            {@link TaskStatus} of searched step
     */
    private static void filterStepMin(Conjunction con, String parameters, TaskStatus inStatus, boolean negate,
            String prefix) {
        if (con == null) {
            con = Restrictions.conjunction();
        }
        if (!negate) {
            con.add(Restrictions.and(Restrictions.ge(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue())));
        } else {
            con.add(Restrictions
                    .not(Restrictions.and(Restrictions.ge(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                            Restrictions.eq(prefix + "processingStatus", inStatus.getValue()))));
        }
    }

    /**
     * Filter processes for done steps max.
     *
     * @param parameters
     *            part of filter string to use
     * @param inStatus
     *            {@link TaskStatus} of searched step
     */
    private static void filterStepMax(Conjunction con, String parameters, TaskStatus inStatus, boolean negate,
            String prefix) {
        if (con == null) {
            con = Restrictions.conjunction();
        }
        if (!negate) {
            con.add(Restrictions.and(Restrictions.le(prefix + "ordering", FilterHelper.getStepEnd(parameters)),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue())));
        } else {
            con.add(Restrictions
                    .not(Restrictions.and(Restrictions.le(prefix + "ordering", FilterHelper.getStepEnd(parameters)),
                            Restrictions.eq(prefix + "processingStatus", inStatus.getValue()))));
        }
    }

    /**
     * Filter processes for done steps exact.
     *
     * @param parameters
     *            part of filter string to use
     * @param inStatus
     *            {@link TaskStatus} of searched step
     */
    private static void filterStepExact(Conjunction con, String parameters, TaskStatus inStatus, boolean negate,
            String prefix) {
        if (!negate) {
            con.add(Restrictions.and(Restrictions.eq(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                    Restrictions.eq(prefix + "processingStatus", inStatus.getValue())));
        } else {
            con.add(Restrictions
                    .not(Restrictions.and(Restrictions.eq(prefix + "ordering", FilterHelper.getStepStart(parameters)),
                            Restrictions.eq(prefix + "processingStatus", inStatus.getValue()))));
        }
    }

    /**
     * Filter processes for done steps by user.
     *
     * @param tok
     *            part of filter string to use
     */
    private static void filterStepDoneUser(Conjunction con, String tok) {
        /*
         * filtering by a certain done step, which the current user finished
         */
        String login = tok.substring(tok.indexOf(":") + 1);
        con.add(Restrictions.eq("user.login", login));
    }

    /**
     * Filter processes by project.
     *
     * @param tok
     *            part of filter string to use
     */
    private static void filterProject(Conjunction con, String tok, boolean negate) {
        /* filter according to linked project */
        if (!negate) {
            con.add(Restrictions.like("project.title", "%" + tok.substring(tok.indexOf(":") + 1) + "%"));
        } else {
            con.add(Restrictions
                    .not(Restrictions.like("project.title", "%" + tok.substring(tok.indexOf(":") + 1) + "%")));
        }
    }

    /**
     * Filter processes by scan template.
     *
     * @param tok
     *            part of filter string to use
     */
    private static void filterScanTemplate(Conjunction con, String tok, boolean negate) {
        /* Filtering by signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                con.add(Restrictions.and(Restrictions.like("vorleig.value", "%" + ts[1] + "%"),
                        Restrictions.like("vorleig.title", "%" + ts[0] + "%")));
            } else {
                con.add(Restrictions.like("vorleig.value", "%" + ts[0] + "%"));
            }
        } else {
            if (ts.length > 1) {
                con.add(Restrictions.not(Restrictions.and(Restrictions.like("vorleig.value", "%" + ts[1] + "%"),
                        Restrictions.like("vorleig.title", "%" + ts[0] + "%"))));
            } else {
                con.add(Restrictions.not(Restrictions.like("vorleig.value", "%" + ts[0] + "%")));
            }
        }
    }

    private static void filterProcessProperty(Conjunction con, String tok, boolean negate) {
        /* Filtering by signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                con.add(Restrictions.and(Restrictions.like("prozesseig.value", "%" + ts[1] + "%"),
                        Restrictions.like("prozesseig.title", "%" + ts[0] + "%")));
            } else {
                con.add(Restrictions.like("prozesseig.value", "%" + ts[0] + "%"));
            }
        } else {
            if (ts.length > 1) {
                con.add(Restrictions.not(Restrictions.and(Restrictions.like("prozesseig.value", "%" + ts[1] + "%"),
                        Restrictions.like("prozesseig.title", "%" + ts[0] + "%"))));
            } else {
                con.add(Restrictions.not(Restrictions.like("prozesseig.value", "%" + ts[0] + "%")));
            }
        }
    }

    /**
     * Filter processes by Ids.
     *
     * @param tok
     *            part of filter string to use
     */
    private static void filterIds(Conjunction con, String tok) {
        /* filtering by ids */
        List<Integer> listIds = new ArrayList<>();
        if (tok.substring(tok.indexOf(":") + 1).length() > 0) {
            String[] tempids = tok.substring(tok.indexOf(":") + 1).split(" ");
            for (String tempId : tempids) {
                try {
                    int tempid = Integer.parseInt(tempId);
                    listIds.add(tempid);
                } catch (NumberFormatException e) {
                    Helper.setFehlerMeldung(tempId + Helper.getTranslation("NumberFormatError"));
                }
            }
        }
        if (listIds.size() > 0) {
            con.add(Restrictions.in("id", listIds));
        }
    }

    /**
     * Filter processes by workpiece.
     *
     * @param tok
     *            part of filter string to use
     */
    private static void filterWorkpiece(Conjunction con, String tok, boolean negate) {
        /* filter according signature */
        String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
        if (!negate) {
            if (ts.length > 1) {
                con.add(Restrictions.and(Restrictions.like("werkeig.value", "%" + ts[1] + "%"),
                        Restrictions.like("werkeig.title", "%" + ts[0] + "%")));
            } else {

                con.add(Restrictions.like("werkeig.value", "%" + ts[0] + "%"));
            }
        } else {
            if (ts.length > 1) {
                con.add(Restrictions.not(Restrictions.and(Restrictions.like("werkeig.value", "%" + ts[1] + "%"),
                        Restrictions.like("werkeig.title", "%" + ts[0] + "%"))));
            } else {

                con.add(Restrictions.not(Restrictions.like("werkeig.value", "%" + ts[0] + "%")));
            }
        }
    }

    /**
     * This method builds a criteria depending on a filter string and some other
     * parameters passed on along the initial criteria. The filter is parsed and
     * depending on which data structures are used for applying filtering
     * restrictions conjunctions are formed and collect the restrictions and
     * then will be applied on the corresponding criteria. A criteria is only
     * added if needed for the presence of filters applying to it.
     *
     * @param inFilter
     *            String
     * @param crit
     *            PaginatingCriteria object
     * @param isTemplate
     *            Boolean
     * @param returnParameters
     *            Object containing values which need to be set and returned to
     *            UserDefinedFilter
     * @param userAssignedStepsOnly
     *            Boolean
     * @param stepOpenOnly
     *            boolean
     * @return String used to pass on error messages about errors in the filter
     *         expression
     */
    public static String criteriaBuilder(Session session, String inFilter, PaginatingCriteria crit, Boolean isTemplate,
            Parameters returnParameters, Boolean stepOpenOnly, Boolean userAssignedStepsOnly, boolean clearSession) {

        if (ConfigCore.getBooleanParameter("DatabaseAutomaticRefreshList", true) && clearSession) {
            session.clear();
        }
        // for ordering the lists there are some
        // criteria, which needs to be added even no
        // restrictions apply, to avoid multiple analysis
        // of the criteria it is only done here once and
        // to set flags which are subsequently used
        Boolean flagSteps = false;
        Boolean flagProcesses = false;
        @SuppressWarnings("unused")
        Boolean flagSetCritProjects = false;
        String filterPrefix = "";
        if (crit.getClassName().equals(Process.class.getName())) {
            flagProcesses = true;
            filterPrefix = "steps.";
        }

        if (crit.getClassName().equals(Task.class.getName())) {
            flagSteps = true;
        }

        // keeping a reference to the passed criteria
        Criteria inCrit = crit;
        @SuppressWarnings("unused")
        Criteria critProject = null;
        Criteria critProcess = null;

        // to collect and return feedback about erroneous use of filter
        // expressions
        StringBuilder message = new StringBuilder();

        StrTokenizer tokenizer = new StrTokenizer(inFilter, ' ', '\"');

        // conjunctions collecting conditions
        Conjunction conjWorkPiece = null;
        Conjunction conjProjects = null;
        Conjunction conjSteps = null;
        Conjunction conjProcesses = null;
        Conjunction conjTemplates = null;
        Conjunction conjUsers = null;
        Conjunction conjProcessProperties = null;
        Conjunction conjBatches = null;

        // this is needed if we filter processes
        if (flagProcesses) {
            conjProjects = Restrictions.conjunction();
            limitToUserAccessRights(conjProjects);
            // in case nothing is set here it needs to be removed again
            // happens if user has admin rights
            if (conjProjects.toString().equals("()")) {
                conjProjects = null;
                flagSetCritProjects = true;
            }
        }

        // this is needed if we filter steps
        if (flagSteps) {
            conjSteps = Restrictions.conjunction();
            limitToUserAssignedSteps(conjSteps, stepOpenOnly, userAssignedStepsOnly);
            // in case nothing is set here conjunction needs to be set to null
            // again
            if (conjSteps.toString().equals("()")) {
                conjSteps = null;
            }
        }

        // this is needed for the template filter (true) and the undefined
        // processes filter (false) in any other case it needs to be null
        if (isTemplate != null) {
            conjProcesses = Restrictions.conjunction();
            if (!isTemplate) {
                conjProcesses.add(Restrictions.eq("template", Boolean.FALSE));
            } else {
                conjProcesses.add(Restrictions.eq("template", Boolean.TRUE));
            }
        }

        // this is needed for evaluating a filter string
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken().trim();
            String tokLowerCase = tok.toLowerCase(Locale.GERMANY);

            if (evaluateFilterString(tokLowerCase, FilterString.PROCESSPROPERTY, null)) {
                if (conjProcessProperties == null) {
                    conjProcessProperties = Restrictions.conjunction();
                }
                FilterHelper.filterProcessProperty(conjProcessProperties, tok, false);
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASK, null)) {
                // search over steps
                // original filter, is left here for compatibility reason
                // doesn't fit into new keyword scheme
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createHistoricFilter(conjSteps, tok, flagSteps));

            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKINWORK, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.INWORK, false, filterPrefix));

                // new keyword stepLocked implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.PROCESSPROPERTY, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.LOCKED, false, filterPrefix));

                // new keyword stepOpen implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKOPEN, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.OPEN, false, filterPrefix));

                // new keyword stepDone implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKDONE, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.DONE, false, filterPrefix));

                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKDONETITLE, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                FilterHelper.filterStepName(conjSteps, stepTitel, TaskStatus.DONE, false, filterPrefix);

            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKDONEUSER, null)
                    && ConfigCore.getBooleanParameter("withUserStepDoneSearch")) {
                if (conjUsers == null) {
                    conjUsers = Restrictions.conjunction();
                }
                FilterHelper.filterStepDoneUser(conjUsers, tok);
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKAUTOMATIC, null)) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                FilterHelper.filterAutomaticSteps(conjSteps, tok, flagSteps);
            } else if (evaluateFilterString(tokLowerCase, FilterString.PROJECT, null)) {
                if (conjProjects == null) {
                    conjProjects = Restrictions.conjunction();
                }
                FilterHelper.filterProject(conjProjects, tok, false);

            } else if (evaluateFilterString(tokLowerCase, FilterString.TEMPLATE, null)) {
                if (conjTemplates == null) {
                    conjTemplates = Restrictions.conjunction();
                }
                FilterHelper.filterScanTemplate(conjTemplates, tok, false);

            } else if (tokLowerCase.startsWith(FilterString.ID.getFilterEnglish())) {
                if (conjProcesses == null) {
                    conjProcesses = Restrictions.conjunction();
                }
                FilterHelper.filterIds(conjProcesses, tok);

            } else if (evaluateFilterString(tokLowerCase, FilterString.PROCESS, null)) {
                if (conjProcesses == null) {
                    conjProcesses = Restrictions.conjunction();
                }
                conjProcesses
                        .add(Restrictions.like("title", "%" + "proc:" + tok.substring(tok.indexOf(":") + 1) + "%"));
            } else if (evaluateFilterString(tokLowerCase, FilterString.BATCH, null)) {
                if (conjBatches == null) {
                    conjBatches = Restrictions.conjunction();
                }
                int value = Integer.parseInt(tok.substring(tok.indexOf(":") + 1));
                conjBatches.add(Restrictions.eq("bat.id", value));
            } else if (evaluateFilterString(tokLowerCase, FilterString.WORKPIECE, null)) {
                if (conjWorkPiece == null) {
                    conjWorkPiece = Restrictions.conjunction();
                }
                FilterHelper.filterWorkpiece(conjWorkPiece, tok, false);

            } else if (evaluateFilterString(tokLowerCase, FilterString.PROCESSPROPERTY, "-")) {
                if (conjProcessProperties == null) {
                    conjProcessProperties = Restrictions.conjunction();
                }
                FilterHelper.filterProcessProperty(conjProcessProperties, tok, true);
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKINWORK, "-")) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.INWORK, true, filterPrefix));

                // new keyword stepLocked implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKLOCKED, "-")) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.LOCKED, true, filterPrefix));

                // new keyword stepOpen implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKOPEN, "-")) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.OPEN, true, filterPrefix));

                // new keyword stepDone implemented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKDONE, "-")) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                message.append(createStepFilters(returnParameters, conjSteps, tok, TaskStatus.DONE, true, filterPrefix));

                // new keyword stepDoneTitle implemented, replacing so far
                // undocumented
            } else if (evaluateFilterString(tokLowerCase, FilterString.TASKDONETITLE, "-")) {
                if (conjSteps == null) {
                    conjSteps = Restrictions.conjunction();
                }
                String stepTitel = tok.substring(tok.indexOf(":") + 1);
                FilterHelper.filterStepName(conjSteps, stepTitel, TaskStatus.DONE, true, filterPrefix);

            } else if (evaluateFilterString(tokLowerCase, FilterString.PROJECT, "-")) {
                if (conjProjects == null) {
                    conjProjects = Restrictions.conjunction();
                }
                FilterHelper.filterProject(conjProjects, tok, true);

            } else if (evaluateFilterString(tokLowerCase, FilterString.TEMPLATE, "-")) {
                if (conjTemplates == null) {
                    conjTemplates = Restrictions.conjunction();
                }
                FilterHelper.filterScanTemplate(conjTemplates, tok, true);

            } else if (evaluateFilterString(tokLowerCase, FilterString.WORKPIECE, "-")) {
                if (conjWorkPiece == null) {
                    conjWorkPiece = Restrictions.conjunction();
                }
                FilterHelper.filterWorkpiece(conjWorkPiece, tok, true);

            } else if (tokLowerCase.startsWith("-")) {

                if (conjProcesses == null) {
                    conjProcesses = Restrictions.conjunction();
                }

                conjProcesses.add(Restrictions.not(Restrictions.like("title", "%" + tok.substring(1) + "%")));

            } else {

                /* standard-search parameter */
                if (conjProcesses == null) {
                    conjProcesses = Restrictions.conjunction();
                }

                conjProcesses.add(IlikeExpression.ilike("title", "*" + tok + "*", '!'));
            }
        }

        if (conjProcesses != null || flagSteps) {
            if (!flagProcesses) {

                critProcess = crit.createCriteria("process", "proc");

                if (conjProcesses != null) {
                    critProcess.add(conjProcesses);
                }
            } else {
                if (conjProcesses != null) {
                    inCrit.add(conjProcesses);
                }
            }
        }

        if (flagSteps) {

            critProject = critProcess.createCriteria("project", "project");

            if (conjProjects != null) {
                inCrit.add(conjProjects);
            }
        } else {
            inCrit.createCriteria("project", "project");
            if (conjProjects != null) {
                inCrit.add(conjProjects);
            }
        }

        if (conjSteps != null) {
            if (!flagSteps) {
                crit.createCriteria("tasks", "steps");
                crit.add(conjSteps);
            } else {
                inCrit.add(conjSteps);
            }
        }

        if (conjTemplates != null) {
            if (flagSteps) {
                critProcess.createCriteria("templates", "vorl");
                critProcess.createAlias("vorl.eigenschaften", "vorleig");
                critProcess.add(conjTemplates);
            } else {
                crit.createCriteria("templates", "vorl");
                crit.createAlias("vorl.eigenschaften", "vorleig");
                inCrit.add(conjTemplates);
            }
        }

        if (conjProcessProperties != null) {
            if (flagSteps) {
                critProcess.createAlias("proc.eigenschaften", "prozesseig");
                critProcess.add(conjProcessProperties);
            } else {

                inCrit.createAlias("properties", "prozesseig");
                inCrit.add(conjProcessProperties);
            }
        }

        if (conjWorkPiece != null) {
            if (flagSteps) {
                critProcess.createCriteria("workpiece", "werk");
                critProcess.createAlias("werk.eigenschaften", "werkeig");
                critProcess.add(conjWorkPiece);
            } else {
                inCrit.createCriteria("workpiece", "werk");
                inCrit.createAlias("werk.eigenschaften", "werkeig");
                inCrit.add(conjWorkPiece);
            }
        }
        if (conjUsers != null) {
            if (flagSteps) {
                critProcess.createCriteria("processingUser", "processingUser");
                critProcess.add(conjUsers);
            } else {
                inCrit.createAlias("steps.processingUser", "processingUser");
                inCrit.add(conjUsers);
            }
        }
        if (conjBatches != null) {
            if (flagSteps) {
                critProcess.createCriteria("batches", "bat");
                critProcess.add(conjBatches);
            } else {
                crit.createCriteria("batches", "bat");
                inCrit.add(conjBatches);
            }
        }
        return message.toString();
    }

    private static boolean evaluateFilterString(String lowerCaseFilterString, FilterString filterString, String prefix) {
        if (prefix != null) {
            return lowerCaseFilterString.startsWith(prefix + filterString.getFilterEnglish())
                    || lowerCaseFilterString.startsWith(prefix + filterString.getFilterGerman());
        }
        return lowerCaseFilterString.startsWith(filterString.getFilterEnglish())
                || lowerCaseFilterString.startsWith(filterString.getFilterGerman());
    }

    /**
     * Create historic filer.
     *
     * @param conjSteps
     *            Conjunction object
     * @param filterPart
     *            String
     * @return empty string
     */
    private static String createHistoricFilter(Conjunction conjSteps, String filterPart, Boolean stepCriteria) {
        /* filtering by a certain minimal status */
        Integer stepReihenfolge;

        String stepTitle = filterPart.substring(filterPart.indexOf(":") + 1);
        // if the criteria is build on steps the table need not be identified
        String tableIdentifier;
        if (stepCriteria) {
            tableIdentifier = "";
        } else {
            tableIdentifier = "steps.";
        }
        try {
            stepReihenfolge = Integer.parseInt(stepTitle);
        } catch (NumberFormatException e) {
            stepTitle = filterPart.substring(filterPart.indexOf(":") + 1);
            if (stepTitle.startsWith("-")) {
                stepTitle = stepTitle.substring(1);
                conjSteps.add(Restrictions.and(
                        Restrictions.not(Restrictions.like(tableIdentifier + "title", "%" + stepTitle + "%")),
                        Restrictions.ge(tableIdentifier + "processingStatus", TaskStatus.OPEN.getValue())));
                return "";
            } else {
                conjSteps.add(Restrictions.and(Restrictions.like(tableIdentifier + "title", "%" + stepTitle + "%"),
                        Restrictions.ge(tableIdentifier + "processingStatus", TaskStatus.OPEN.getValue())));
                return "";
            }
        }
        conjSteps.add(Restrictions.and(Restrictions.eq(tableIdentifier + "ordering", stepReihenfolge),
                Restrictions.ge(tableIdentifier + "processingStatus", TaskStatus.OPEN.getValue())));
        return "";
    }

    /**
     * Create task filters.
     *
     * @param returnParameters
     *            Parameters object
     * @param con
     *            Conjunction object
     * @param filterPart
     *            String
     * @return String
     */
    private static String createStepFilters(Parameters returnParameters, Conjunction con, String filterPart,
            TaskStatus inStatus, boolean negate, String filterPrefix) {
        // extracting the substring into parameter (filter parameters e.g. 5,
        // -5,
        // 5-10, 5- or "Qualitätssicherung")

        String parameters = filterPart.substring(filterPart.indexOf(":") + 1);
        String message = "";
        /*
         * Analyzing the parameters and what user intended (5->exact, -5 ->max,
         * 5-10 ->range, 5- ->min., Qualitätssicherung ->name) handling the
         * filter according to the parameters
         */

        switch (FilterHelper.getStepFilter(parameters)) {
            case exact:
                try {
                    FilterHelper.filterStepExact(con, parameters, inStatus, negate, filterPrefix);
                    returnParameters.setStepDone(FilterHelper.getStepStart(parameters));
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    logger.error(e);
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart
                            + "' caused an error\n";
                }
                break;
            case max:
                try {
                    FilterHelper.filterStepMax(con, parameters, inStatus, negate, filterPrefix);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart
                            + "' caused an error\n";
                }
                break;
            case min:
                try {
                    FilterHelper.filterStepMin(con, parameters, inStatus, negate, filterPrefix);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart
                            + "' caused an error\n";
                }
                break;
            case name:
                /* filter for a specific done step by it's name (Titel) */
                // myObservable.setMessage("Filter 'stepDone:" + parameters
                // + "' is not yet implemented and will be ignored!");
                try {
                    FilterHelper.filterStepName(con, parameters, inStatus, negate, filterPrefix);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart
                            + "' caused an error\n";
                }
                break;
            case range:
                try {
                    FilterHelper.filterStepRange(con, parameters, inStatus, negate, filterPrefix);
                } catch (NullPointerException e) {
                    message = "stepdone is preset, don't use 'step' filters";
                } catch (NumberFormatException e) {
                    try {
                        FilterHelper.filterStepName(con, parameters, inStatus, negate, filterPrefix);
                    } catch (NullPointerException e1) {
                        message = "stepdone is preset, don't use 'step' filters";
                    } catch (Exception e1) {
                        message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '"
                                + filterPart + "' caused an error\n";
                    }
                } catch (Exception e) {
                    message = "filterpart '" + filterPart.substring(filterPart.indexOf(":") + 1) + "' in '" + filterPart
                            + "' caused an error\n";
                }
                break;
            case unknown:
                message = message + ("Filter '" + filterPart + "' is not known!\n");
                break;
            default:
                break;
        }
        return message;
    }

}
