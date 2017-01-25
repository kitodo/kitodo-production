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

package org.kitodo.services;

import de.sub.goobi.helper.Helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.data.database.persistence.TaskDAO;

public class TaskService {

	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");

	private TaskDAO taskDao = new TaskDAO();

	public void save(Task task) throws DAOException {
		taskDao.save(task);
	}

	public Task find(Integer id) throws DAOException {
		return taskDao.find(id);
	}

	public void remove(Task task) throws DAOException {
		taskDao.remove(task);
	}

	public void remove(Integer id) throws DAOException {
		taskDao.remove(id);
	}
	public List<Task> search(String query) throws DAOException {
		return taskDao.search(query);
	}

	public Long count(String query) throws DAOException {
		return taskDao.count(query);
	}

	/**
	 * Convert Date start date to String.
	 *
	 * @param task object
	 * @return formatted date string
	 */
	public String getStartDate(Task task) {
		if (task.getProcessingBegin() != null) {
			return this.formatter.format(task.getProcessingBegin());
		}
		return "";
	}

	/**
	 * Convert Date end date to String.
	 *
	 * @param task object
	 * @return formatted date string
	 */
	public String getEndDate(Task task) {
		if (task.getProcessingEnd() != null) {
			return this.formatter.format(task.getProcessingEnd());
		}
		return "";
	}

	/**
	 * Convert Date start date to String.
	 *
	 * @param task object
	 * @return formatted date string
	 */
	public String getProcessingEndAsFormattedString(Task task) {
		return Helper.getDateAsFormattedString(task.getProcessingEnd());
	}

	/**
	 * Convert Date end date to String.
	 *
	 * @param task object
	 * @return formatted date string
	 */
	public String getProcessingTimeAsFormattedString(Task task) {
		return Helper.getDateAsFormattedString(task.getProcessingTime());
	}

	// a parameter is given here (even if not used) because jsf expects setter convention
	//TODO: check what it means
	/*public void setBearbeitungszeitpunktNow(int in) {
		this.bearbeitungszeitpunkt = new Date();
	}

	public int getBearbeitungszeitpunktNow() {
		return 1;
	}*/

	/**
	 * If you change anything in the logic of priorities make sure that you catch dependencies on this system
	 * which are not directly related to priorities.
	 * TODO: check it!
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

	public int getUsersSize(Task task) {
		if (task.getUsers() == null) {
			return 0;
		} else {
			return task.getUsers().size();
		}
	}

	public int getUserGroupsSize(Task task) {
		if (task.getUserGroups() == null) {
			return 0;
		} else {
			return task.getUserGroups().size();
		}
	}

	public Task setProcessingStatusUp(Task task) {
		if (task.getProcessingStatusEnum() != TaskStatus.DONE) {
			task.setProcessingStatus(task.getProcessingStatus() + 1);
		}
		return task;
	}

	public Task setProcessingStatusDown(Task task) {
		if (task.getProcessingStatusEnum() != TaskStatus.LOCKED) {
			task.setProcessingStatus(task.getProcessingStatus() - 1);
		}
		return task;
	}

	/**
	 * Get title with user.
	 *
	 * @return des Schritttitels sowie (sofern vorhanden) den Benutzer mit vollständigem Namen
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
	 * @return Employee The current object representing a row.
	 */
	public Task getCurrent() {
		boolean hasOpen = HibernateUtilOld.hasOpenSession();
		Session session = Helper.getHibernateSession();
		Task task = new Task();

		Task current = (Task) session.get(Task.class, task.getId());
		if (current == null) {
			current = (Task) session.load(Task.class, task.getId());
		}
		if (!hasOpen) {
			current.getUsers().size();
			current.getUserGroups().size();
			session.close();
		}
		return current;
	}

	/**
	 * Returns whether this is a step of a process that is part of at least one batch as read-only property
	 * "batchSize".
	 *
	 * @return whether this step’s process is in a batch
	 */
	public boolean isBatchSize(Task task) {
		ProcessService processService = new ProcessService();
		return processService.getBatchesInitialized(task.getProcess()).size() > 0;
	}
}
