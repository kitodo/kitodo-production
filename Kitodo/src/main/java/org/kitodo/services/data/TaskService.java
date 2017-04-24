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

package org.kitodo.services.data;

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.TaskType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.SearchService;

public class TaskService extends SearchService {
    private TaskDAO taskDao = new TaskDAO();
    private TaskType taskType = new TaskType();
    private Indexer<Task, TaskType> indexer = new Indexer<>(Task.class);

    /**
     * Constructor with searcher's assigning.
     */
    public TaskService() {
        super(new Searcher(Task.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param task
     *            object
     */
    public void save(Task task) throws DAOException, IOException, CustomResponseException {
        taskDao.save(task);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(task, taskType);
    }

    public Task find(Integer id) throws DAOException {
        return taskDao.find(id);
    }

    public List<Task> findAll() throws DAOException {
        return taskDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param task
     *            object
     */
    public void remove(Task task) throws DAOException, IOException, CustomResponseException {
        taskDao.remove(task);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(task, taskType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws DAOException, IOException, CustomResponseException {
        taskDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    public List<Task> search(String query) throws DAOException {
        return taskDao.search(query);
    }

    public Long count(String query) throws DAOException {
        return taskDao.count(query);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException, CustomResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), taskType);
    }

    /**
     * Convert date of processing begin to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingBeginAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingBegin());
    }

    /**
     * Convert date of processing end to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingEndAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingEnd());
    }

    /**
     * Convert date of processing day to formatted String.
     *
     * @param task
     *            object
     * @return formatted date string
     */
    public String getProcessingTimeAsFormattedString(Task task) {
        return Helper.getDateAsFormattedString(task.getProcessingTime());
    }

    // a parameter is given here (even if not used) because jsf expects setter
    // convention
    public void setProcessingTimeNow(Task task) {
        task.setProcessingTime(new Date());
    }

    public int getProcessingTimeNow() {
        return 1;
    }

    /**
     * If you change anything in the logic of priorities make sure that you
     * catch dependencies on this system which are not directly related to
     * priorities. TODO: check it!
     */
    public Boolean isCorrectionStep(Task task) {
        return (task.getPriority() == 10);
    }

    public Task setCorrectionStep(Task task) {
        task.setPriority(10);
        return task;
    }

    public String getLocalizedTitle(Task task) {
        return Helper.getTranslation(task.getTitle());
    }

    public String getNormalizedTitle(Task task) {
        return task.getTitle().replace(" ", "_");
    }

    /**
     * Get users' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUsersSize(Task task) {
        if (task.getUsers() == null) {
            return 0;
        } else {
            return task.getUsers().size();
        }
    }

    /**
     * Get user groups' list size.
     *
     * @param task
     *            object
     * @return size
     */
    public int getUserGroupsSize(Task task) {
        if (task.getUserGroups() == null) {
            return 0;
        } else {
            return task.getUserGroups().size();
        }
    }

    /**
     * Set processing status up.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusUp(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
            task.setProcessingStatus(task.getProcessingStatus() + 1);
        }
        return task;
    }

    /**
     * Set processing status down.
     *
     * @param task
     *            object
     * @return task object
     */
    public Task setProcessingStatusDown(Task task) {
        if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
            task.setProcessingStatus(task.getProcessingStatus() - 1);
        }
        return task;
    }

    /**
     * Get title with user.
     *
     * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit
     *         vollständigem Namen
     */
    public String getTitleWithUserName(Task task) {
        String result = task.getTitle();
        UserService userService = new UserService();
        if (task.getProcessingUser() != null && task.getProcessingUser().getId() != null
                && task.getProcessingUser().getId() != 0) {
            result += " (" + userService.getFullName(task.getProcessingUser()) + ")";
        }
        return result;
    }

    public String getProcessingStatusAsString(Task task) {
        return String.valueOf(task.getProcessingStatus().intValue());
    }

    public Integer setProcessingStatusAsString(String inputProcessingStatus) {
        return Integer.parseInt(inputProcessingStatus);
    }

    /**
     * Get all script paths.
     *
     * @param task
     *            object
     * @return array list
     */
    public ArrayList<String> getAllScriptPaths(Task task) {
        ArrayList<String> answer = new ArrayList<String>();
        if (task.getTypeAutomaticScriptPath() != null && !task.getTypeAutomaticScriptPath().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath());
        }
        if (task.getTypeAutomaticScriptPath2() != null && !task.getTypeAutomaticScriptPath2().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath2());
        }
        if (task.getTypeAutomaticScriptPath3() != null && !task.getTypeAutomaticScriptPath3().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath3());
        }
        if (task.getTypeAutomaticScriptPath4() != null && !task.getTypeAutomaticScriptPath4().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath4());
        }
        if (task.getTypeAutomaticScriptPath5() != null && !task.getTypeAutomaticScriptPath5().equals("")) {
            answer.add(task.getTypeAutomaticScriptPath5());
        }
        return answer;
    }

    /**
     * Get all scripts adn their paths.
     *
     * @param task
     *            object
     * @return hash map
     */
    public HashMap<String, String> getAllScripts(Task task) {
        HashMap<String, String> answer = new HashMap<>();
        if (task.getTypeAutomaticScriptPath() != null && !task.getTypeAutomaticScriptPath().equals("")) {
            answer.put(task.getScriptName1(), task.getTypeAutomaticScriptPath());
        }
        if (task.getTypeAutomaticScriptPath2() != null && !task.getTypeAutomaticScriptPath2().equals("")) {
            answer.put(task.getScriptName2(), task.getTypeAutomaticScriptPath2());
        }
        if (task.getTypeAutomaticScriptPath3() != null && !task.getTypeAutomaticScriptPath3().equals("")) {
            answer.put(task.getScriptName3(), task.getTypeAutomaticScriptPath3());
        }
        if (task.getTypeAutomaticScriptPath4() != null && !task.getTypeAutomaticScriptPath4().equals("")) {
            answer.put(task.getScriptName4(), task.getTypeAutomaticScriptPath4());
        }
        if (task.getTypeAutomaticScriptPath5() != null && !task.getTypeAutomaticScriptPath5().equals("")) {
            answer.put(task.getScriptName5(), task.getTypeAutomaticScriptPath5());
        }
        return answer;
    }

    /**
     * Set all scripts and their paths.
     *
     * @param paths
     *            hash map of strings
     * @param task
     *            object
     * @return task object
     */
    public Task setAllScripts(HashMap<String, String> paths, Task task) {
        Set<String> keys = paths.keySet();
        ArrayList<String> keyList = new ArrayList<>();
        for (String key : keys) {
            keyList.add(key);
        }
        int size = keyList.size();
        if (size > 0) {
            task.setScriptName1(keyList.get(0));
            task.setTypeAutomaticScriptPath(paths.get(keyList.get(0)));
        }
        if (size > 1) {
            task.setScriptName2(keyList.get(1));
            task.setTypeAutomaticScriptPath2(paths.get(keyList.get(1)));
        }
        if (size > 2) {
            task.setScriptName3(keyList.get(2));
            task.setTypeAutomaticScriptPath3(paths.get(keyList.get(2)));
        }
        if (size > 3) {
            task.setScriptName4(keyList.get(3));
            task.setTypeAutomaticScriptPath4(paths.get(keyList.get(3)));
        }
        if (size > 4) {
            task.setScriptName5(keyList.get(4));
            task.setTypeAutomaticScriptPath5(paths.get(keyList.get(4)));
        }
        return task;
    }

    /**
     * Get list of paths. TODO: inappropriate name of method - change during
     * next phase of refactoring
     * 
     * @param task
     *            object
     * @return string containing paths.
     */
    public String getListOfPaths(Task task) {
        String answer = "";
        if (task.getScriptName1() != null) {
            answer += task.getScriptName1();
        }
        if (task.getScriptName2() != null) {
            answer = answer + "; " + task.getScriptName2();
        }
        if (task.getScriptName3() != null) {
            answer = answer + "; " + task.getScriptName3();
        }
        if (task.getScriptName4() != null) {
            answer = answer + "; " + task.getScriptName4();
        }
        if (task.getScriptName5() != null) {
            answer = answer + "; " + task.getScriptName5();
        }
        return answer;

    }

    /**
     * Get the current object for this row.
     *
     * @return the current object representing a row.
     */
    public Task getCurrent(Task task) {
        boolean hasOpen = HibernateUtilOld.hasOpenSession();
        Session session = Helper.getHibernateSession();

        Task current = (Task) session.get(Task.class, task.getId());
        if (current == null) {
            current = (Task) session.load(Task.class, task.getId());
        }
        if (!hasOpen) {
            session.close();
        }
        return current;
    }

    /**
     * Returns whether this is a step of a process that is part of at least one
     * batch as read-only property "batchSize".
     *
     * @return whether this step’s process is in a batch
     */
    public boolean isBatchSize(Task task) {
        ProcessService processService = new ProcessService();
        return processService.getBatchesInitialized(task.getProcess()).size() > 0;
    }
}
