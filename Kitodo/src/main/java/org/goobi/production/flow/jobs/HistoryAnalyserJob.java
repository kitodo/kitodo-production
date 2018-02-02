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

package org.goobi.production.flow.jobs;

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * HistoryJob proofs History of {@link Process} and creates missing
 * {@link History}s
 *
 * @author Steffen Hankiewicz
 * @author Igor Toker
 * @version 15.06.2009
 */
public class HistoryAnalyserJob extends AbstractGoobiJob {
    private static final Logger logger = LogManager.getLogger(HistoryAnalyserJob.class);
    private static final ServiceManager serviceManager = new ServiceManager();

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
     */
    @Override
    public String getJobName() {
        return "HistoryAnalyserJob";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#execute()
     */
    @Override
    public void execute() {
        updateHistoryForAllProcesses();
    }

    /**
     * Update the history if necessary. It means: - count storage difference in
     * byte <br>
     * - count imagesWork difference <br>
     * - count imagesMaster difference <br>
     * - count metadata difference <br>
     * - count docstruct difference <br>
     *
     * @param inProcess
     *            the {@link Process} to use
     *
     * @return true, if any history event is updated, so the process has to be
     *         saved to database
     */
    public static Boolean updateHistory(Process inProcess) throws IOException, DataException {
        boolean updated = false;
        /* storage */
        if (updateHistoryEvent(inProcess, HistoryTypeEnum.storageDifference, getCurrentStorageSize(inProcess))) {
            updated = true;
        }
        /* imagesWork */
        Integer numberWork = serviceManager.getFileService().getNumberOfImageFiles(
                serviceManager.getProcessService().getImagesTifDirectory(true, inProcess));
        if (updateHistoryEvent(inProcess, HistoryTypeEnum.imagesWorkDiff, numberWork.longValue())) {
            updated = true;
        }

        /* imagesMaster */
        Integer numberMaster = serviceManager.getFileService().getNumberOfImageFiles(
                serviceManager.getProcessService().getImagesOrigDirectory(true, inProcess));
        if (updateHistoryEvent(inProcess, HistoryTypeEnum.imagesMasterDiff, numberMaster.longValue())) {
            updated = true;
        }

        /* metadata */
        if (updateHistoryEvent(inProcess, HistoryTypeEnum.metadataDiff,
                inProcess.getSortHelperMetadata().longValue())) {
            updated = true;
        }

        /* docstruct */
        if (updateHistoryEvent(inProcess, HistoryTypeEnum.docstructDiff,
                inProcess.getSortHelperDocstructs().longValue())) {
            updated = true;
        }

        return updated;
    }

    /**
     * update history for each {@link Task} of given {@link Process}.
     *
     * @param inProcess
     *            given {@link Process}
     * @return true, if changes are made and have to be saved to database
     */
    @SuppressWarnings("incomplete-switch")
    private static Boolean updateHistoryForSteps(Process inProcess) {
        Boolean isDirty = false;
        History he;

        /*
         * These are the patterns, which must be set, if a pattern differs from
         * these something is wrong, timestamp pattern overrules status, in that
         * case status gets changed to match one of these pattern.
         *
         * <pre> status | begin in work work done
         * -------+------------------------------- 0 | null null null 1 | null
         * null null 2 | set set null 3 | set set set </pre>
         */
        for (Task step : inProcess.getTasks()) {
            switch (step.getProcessingStatusEnum()) {
                case DONE:
                    // fix missing start date
                    if (step.getProcessingBegin() == null) {
                        isDirty = true;
                        if (step.getProcessingTime() == null) {
                            step.setProcessingBegin(getTimestampFromPreviousStep(inProcess, step));
                        } else {
                            step.setProcessingBegin(step.getProcessingTime());
                        }
                    }
                    // fix missing editing date
                    if (step.getProcessingTime() == null) {
                        isDirty = true;
                        if (step.getProcessingEnd() == null) {
                            step.setProcessingTime(step.getProcessingBegin());
                        } else {
                            step.setProcessingTime(step.getProcessingEnd());
                        }
                    }
                    // fix missing end date
                    if (step.getProcessingEnd() == null) {
                        isDirty = true;
                        step.setProcessingEnd(step.getProcessingTime());
                    }
                    // attempts to add a history event,
                    // exists method returns null if event already exists
                    he = addHistoryEvent(step.getProcessingEnd(), step.getOrdering(), step.getTitle(),
                            HistoryTypeEnum.taskDone, inProcess);
                    if (he != null) {
                        isDirty = true;
                    }
                    // for each step done we need to create a step open event on
                    // that step based on
                    // the latest timestamp for the previous step
                    he = addHistoryEvent(getTimestampFromPreviousStep(inProcess, step), step.getOrdering(),
                            step.getTitle(), HistoryTypeEnum.taskOpen, inProcess);
                    if (he != null) {
                        isDirty = true;
                    }
                    break;
                case INWORK:
                    // fix missing start date
                    if (step.getProcessingBegin() == null) {
                        isDirty = true;
                        if (step.getProcessingTime() == null) {
                            step.setProcessingBegin(getTimestampFromPreviousStep(inProcess, step));
                        } else {
                            step.setProcessingBegin(step.getProcessingTime());
                        }
                    }
                    // fix missing editing date
                    if (step.getProcessingTime() == null) {
                        isDirty = true;
                        step.setProcessingTime(step.getProcessingBegin());
                    }
                    // enc date must be null
                    if (step.getProcessingEnd() != null) {
                        step.setProcessingEnd(null);
                        isDirty = true;
                    }
                    he = addHistoryEvent(step.getProcessingBegin(), step.getOrdering(), step.getTitle(),
                            HistoryTypeEnum.taskInWork, inProcess);
                    if (he != null) {
                        isDirty = true;
                    }
                    // for each step inwork we need to create a step open event
                    // on that step based on
                    // the latest timestamp from the previous step
                    he = addHistoryEvent(getTimestampFromPreviousStep(inProcess, step), step.getOrdering(),
                            step.getTitle(), HistoryTypeEnum.taskOpen, inProcess);
                    if (he != null) {
                        isDirty = true;
                    }
                    break;
                case OPEN:
                    // fix set start date - decision is that reopened (and
                    // therfore with timestamp for begin)
                    // shouldn't be reset
                    /*
                     * if (step.getBearbeitungsbeginn() != null) {
                     * step.setBearbeitungsbeginn(null); isDirty = true; }
                     */
                    // fix missing editing date
                    if (step.getProcessingTime() == null) {
                        isDirty = true;
                        if (step.getProcessingEnd() != null) {
                            step.setProcessingTime(step.getProcessingEnd());
                        } else {
                            // step.setBearbeitungsbeginn(getTimestampFromPreviousStep(inProcess,
                            // step));
                            step.setProcessingTime(getTimestampFromPreviousStep(inProcess, step));
                        }
                    }
                    // fix set end date
                    if (step.getProcessingEnd() != null) {
                        step.setProcessingEnd(null);
                        isDirty = true;
                    }
                    he = addHistoryEvent(step.getProcessingTime(), step.getOrdering(), step.getTitle(),
                            HistoryTypeEnum.taskOpen, inProcess);
                    if (he != null) {
                        isDirty = true;
                    }
                    break;
            }

            // check corrections timestamp this clearly only works on past
            // correction events done in the german
            // language current corrections directly adds to the history

            // adds for each step a step locked on the basis of the process
            // creation timestamp (new in 1.6)
            he = addHistoryEvent(inProcess.getCreationDate(), step.getOrdering(), step.getTitle(),
                    HistoryTypeEnum.taskLocked, inProcess);

            if (he != null) {
                isDirty = true;
            }

        }

        // this method removes duplicate items from the history list, which
        // already happened to be there, isDirty will be automatically be set
        if (getHistoryEventDuplicated(inProcess)) {
            isDirty = true;
        }

        return isDirty;
    }

    /**
     * Add HistoryEvent.
     *
     * @param timeStamp
     *            Date
     * @param stepOrder
     *            Integer
     * @param stepName
     *            String
     * @param type
     *            HistoryTypeEnum object
     * @param inProcess
     *            Process object
     * @return History event if event needs to be added, null if event(same
     *         kind, same time, same process) already exists
     */
    private static History addHistoryEvent(Date timeStamp, Integer stepOrder, String stepName, HistoryTypeEnum type,
            Process inProcess) {
        History he = new History(timeStamp, stepOrder, stepName, type, inProcess);

        if (!getHistoryContainsEventAlready(he, inProcess)) {
            inProcess.getHistory().add(he);
            return he;
        } else {
            return null;
        }
    }

    /**
     * check if history already contains given event.
     *
     * @param inEvent
     *            given {@link History}
     * @param inProcess
     *            given {@link Process}
     * @return true, if {@link History} already exists
     */
    private static Boolean getHistoryContainsEventAlready(History inEvent, Process inProcess) {
        for (History historyItem : inProcess.getHistory()) {
            if (inEvent != historyItem) { // this is required, in case items
                // from the same list are compared
                if (historyItem.equals(inEvent)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get stored value (all diffs as sum) from history.
     *
     * @return stored value as Long
     */
    private static Long getStoredValue(Process inProcess, HistoryTypeEnum inType) {
        long storedValue = 0;
        for (History historyItem : inProcess.getHistory()) {
            if (historyItem.getHistoryType() == inType) {
                storedValue += historyItem.getNumericValue().longValue();
            }
        }
        return storedValue;
    }

    /**
     * update history, if current value is different to stored value.
     *
     * @return true if value is different and history got updated, else false
     */
    private static Boolean updateHistoryEvent(Process inProcess, HistoryTypeEnum inType, Long inCurrentValue)
            throws DataException {
        long storedValue = getStoredValue(inProcess, inType);
        long diff = inCurrentValue - storedValue;

        // if storedValue is different to current value - update history
        if (diff != 0) {
            History history = new History(new Date(), diff, null, inType, inProcess);
            serviceManager.getHistoryService().save(history);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Size of Storage in Bytes per {@link Process}.
     *
     * @return size in bytes, or 0 if error.
     */
    private static long getCurrentStorageSize(Process inProcess) throws IOException {
        URI directory = serviceManager.getProcessService().getProcessDataDirectory(inProcess);
        return serviceManager.getFileService().getSizeOfDirectory(directory);
    }

    /**
     * Update history for all processes.
     */
    public void updateHistoryForAllProcesses() {
        logger.info("start history updating for all processes");
        try {
            Session session = Helper.getHibernateSession();
            Query query = session.createQuery("from Process order by id desc");
            @SuppressWarnings("unchecked")
            Iterator<Process> it = query.iterate();
            int i = 0;
            while (it.hasNext()) {
                i++;
                Process process = it.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("updating history entries for " + process.getTitle());
                }
                try {
                    // commit transaction every 50 items
                    if (!it.hasNext() || i % 50 == 0) {
                        session.flush();
                        session.beginTransaction().commit();
                        session.clear();
                    }
                } catch (HibernateException e) {
                    logger.error("HibernateException occurred while scheduled storage calculation", e);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("An error occurred while scheduled storage calculation", e);
                    logger.error("ServletException occurred while scheduled storage calculation", e);
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Another Exception occurred while scheduled storage calculation", e);
            logger.error("Another Exception occurred while scheduled storage calculation", e);
        }
        logger.info("end history updating for all processes");
    }

    /**
     * method returns a timestamp from a previous step, iterates through the
     * steps if necessary.
     *
     * @param inProcess
     *            Process object
     * @param inStep
     *            Task object
     */
    private static Date getTimestampFromPreviousStep(Process inProcess, Task inStep) {
        Date eventTimestamp = null;
        List<Task> tempList = inProcess.getTasks();

        for (Task s : tempList) {
            // making sure that we only look for timestamps in the step below
            // this one
            int index = tempList.indexOf(s);

            if (s == inStep && index != 0) {
                Task prevStep = tempList.get(index - 1);

                if (prevStep.getProcessingEnd() != null) {
                    return prevStep.getProcessingEnd();
                }

                if (prevStep.getProcessingTime() != null) {
                    return prevStep.getProcessingTime();
                }

                if (prevStep.getProcessingBegin() != null) {
                    return prevStep.getProcessingBegin();
                }

                eventTimestamp = getTimestampFromPreviousStep(inProcess, prevStep);
            }

        }

        if (eventTimestamp == null) {
            if (inProcess.getCreationDate() != null) {
                eventTimestamp = inProcess.getCreationDate();
            } else { // if everything fails we use the current date
                Calendar cal = Calendar.getInstance();
                cal.set(2007, Calendar.JANUARY, 1, 0, 0, 0);
                eventTimestamp = cal.getTime();
                if (logger.isInfoEnabled()) {
                    logger.info("We had to use 2007-1-1 date '" + eventTimestamp.toString()
                            + "' for a history event as a fallback");
                }
            }

        }
        return eventTimestamp;
    }

    /**
     * Method iterates through the event list and checks if there are duplicate
     * entries, if so it will remove the entry and return a true.
     *
     * @param inProcess
     *            Process object
     * @return true if there are duplicate entries, false otherwise
     */
    private static Boolean getHistoryEventDuplicated(Process inProcess) {
        Boolean duplicateEventRemoved = false;
        for (History he : inProcess.getHistory()) {
            if (getHistoryContainsEventAlready(he, inProcess)) {
                inProcess.getHistory().remove(he);
                duplicateEventRemoved = true;
            }
        }
        return duplicateEventRemoved;
    }

    /**
     * Update history for process.
     *
     * @param inProc
     *            Process object
     * @return Boolean
     */
    public static Boolean updateHistoryForProcess(Process inProc) {
        Boolean updated;
        try {
            //TODO: updateHistoryForSteps overwrites result of updateHistory
            updated = updateHistory(inProc);
            updated = updateHistoryForSteps(inProc);
        } catch (Exception ex) {
            logger.warn("Updating history failed.", ex);
            updated = false;
        }
        return updated;
    }

}
