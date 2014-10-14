/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.helper.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goobi.mq.processors.CreateNewProcessProcessor;
import org.goobi.production.model.bibliography.course.Granularity;
import org.goobi.production.model.bibliography.course.IndividualIssue;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Batch.Type;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.BatchDAO;

/**
 * The class CreateProcessesTask is a LongRunningTask to create processes from a
 * course of appearance.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CreateProcessesTask extends EmptyTask {
	/**
	 * The field batchLabel is set in addToBatches() on the first function call
	 * which finds it to be null, and is used and set back to null in
	 * flushLogisticsBatch() to create the batches’ specific part of the
	 * identifier (put in parentheses behind the shared part).
	 */
	private String batchLabel;

	/**
	 * The field createBatches holds a granularity level that is used to create
	 * batches out of the given processes. The field may be null which disables
	 * the feature.
	 */
	private final Granularity createBatches;

	/**
	 * The field currentBreakMark holds an integer hash value which, for a given
	 * Granularity, shall indicate for two neighboring processes whether they
	 * form the same logistics batch (break mark is equal) or to different
	 * processes (break mark differs).
	 */
	private Integer currentBreakMark;

	/**
	 * The field fullBatch holds a batch that all issues will be assigned to.
	 */
	private Batch fullBatch = new Batch(Type.NEWSPAPER);

	/**
	 * The field logisticsBatch holds a batch that all issues of the same
	 * logistics unit will be assigned to.
	 */
	private Batch logisticsBatch = new Batch(Type.LOGISTIC);

	/**
	 * The field nextProcessToCreate holds the index of the next process to
	 * create. Because long running tasks are interruptible is a field so the
	 * thread will continue to work with the next process after being continued.
	 */
	private int nextProcessToCreate;

	/**
	 * The field numberOfProcesses holds the processes’ size to prevent calling
	 * size() over and over again
	 */
	private final int numberOfProcesses;

	/**
	 * The field pattern holds a ProzesskopieForm instance that will be used as
	 * pattern for the creation of processes.
	 */
	private final ProzesskopieForm pattern;

	/**
	 * The field processes holds a List of List of IndividualIssue objects that
	 * processes will be created from. Each list object, which is a list itself,
	 * represents a process to create. Each process can consist of many issues
	 * which will be part of that process.
	 */
	private final List<List<IndividualIssue>> processes;

	/**
	 * The class CreateProcessesTask is a LongRunningTask to create processes
	 * from a course of appearance.
	 * 
	 * @param pattern
	 *            a ProzesskopieForm to use for creating processes
	 * @param processes
	 *            a list of processes to create
	 * @param batchGranularity
	 *            a granularity level at which baches shall be created
	 */
	public CreateProcessesTask(ProzesskopieForm pattern, List<List<IndividualIssue>> processes,
			Granularity batchGranularity) {
		super(pattern.getProzessVorlageTitel());
		this.pattern = pattern;
		this.processes = new ArrayList<List<IndividualIssue>>(processes.size());
		this.createBatches = batchGranularity;
		for (List<IndividualIssue> issues : processes) {
			List<IndividualIssue> process = new ArrayList<IndividualIssue>(issues.size());
			process.addAll(issues);
			this.processes.add(process);
		}
		nextProcessToCreate = 0;
		numberOfProcesses = processes.size();
	}

	/**
	 * The copy constructor creates a new thread from a given one. This is
	 * required to call the copy constructor of the parent.
	 * 
	 * @param master
	 *            copy master
	 */
	public CreateProcessesTask(CreateProcessesTask master) {
		super(master);
		this.pattern = master.pattern;
		this.processes = master.processes;
		this.createBatches = master.createBatches;
		this.logisticsBatch = master.logisticsBatch;
		this.currentBreakMark = master.currentBreakMark;
		this.batchLabel = master.batchLabel;
		this.fullBatch = master.fullBatch;
		this.nextProcessToCreate = master.nextProcessToCreate;
		this.numberOfProcesses = master.numberOfProcesses;
	}

	/**
	 * The function run() is the main function of this task (which is a thread).
	 * 
	 * It will create a new process for each entry from the field “processes”.
	 * 
	 * Therefore it makes use of
	 * CreateNewProcessProcessor.newProcessFromTemplate() to once again load a
	 * ProzesskopieForm from Hibernate for each process to create, sets the
	 * required fields accordingly, then triggers the calculation of the process
	 * title and finally initiates the process creation one by one. The
	 * statusProgress variable is being updated to show the operator how far the
	 * task has proceeded.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		String currentTitle = null;
		try {
			while (nextProcessToCreate < numberOfProcesses) {
				List<IndividualIssue> issues = processes.get(nextProcessToCreate);
				if (issues.size() > 0) {
					ProzesskopieForm newProcess = CreateNewProcessProcessor.newProcessFromTemplate(pattern
							.getProzessVorlage().getTitel());
					newProcess.setDigitalCollections(pattern.getDigitalCollections());
					newProcess.setDocType(pattern.getDocType());
					newProcess.setAdditionalFields(pattern.getAdditionalFields());
					currentTitle = newProcess.generateTitle(issues.get(0).getGenericFields());
					if (currentTitle == "") {
						setException(new RuntimeException("Couldn’t create process title for issue "
								+ issues.get(0).toString()));
						return;
					}
					if (isInterrupted()) {
						return;
					}
					String state = newProcess.NeuenProzessAnlegen();
					if (!state.equals("ProzessverwaltungKopie3")) {
						throw new RuntimeException(String.valueOf(Helper.getLastMessage()).replaceFirst(":\\?*$", ""));
					}
					addToBatches(newProcess.getProzessKopie(), issues, currentTitle);
				}
				nextProcessToCreate++;
				setProgress(100 * nextProcessToCreate / (numberOfProcesses + 2));
				if (isInterrupted()) {
					return;
				}
			}
			flushLogisticsBatch(currentTitle);
			setProgress((100 * nextProcessToCreate + 1) / (numberOfProcesses + 2));
			saveFullBatch(currentTitle);
			setProgress(100);
		} catch (Exception e) { // ReadException, PreferencesException, SwapException, DAOException, WriteException, IOException, InterruptedException from ProzesskopieForm.NeuenProzessAnlegen()
			setException(new RuntimeException(e.getClass().getSimpleName()
					+ (currentTitle != null ? " while creating " + currentTitle : " in CreateProcessesTask") + ": "
					+ e.getMessage(), e));
			return;
		}
	}

	/**
	 * The method addToBatches() adds a given process to the allover and the
	 * annual batch. If the break mark changes, the logistics batch will be
	 * flushed and the process will be added to a new logistics batch.
	 * 
	 * @param process
	 *            process to add
	 * @param issues
	 *            list of individual issues in the process
	 * @param processTitle
	 *            the title of the process
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback
	 */
	private void addToBatches(Prozess process, List<IndividualIssue> issues, String processTitle) throws DAOException {
		if (createBatches != null) {
			int lastIndex = issues.size() - 1;
			int breakMark = issues.get(lastIndex).getBreakMark(createBatches);
			if (currentBreakMark != null && breakMark != currentBreakMark) {
				flushLogisticsBatch(processTitle);
			}
			if (batchLabel == null) {
				batchLabel = createBatches.format(issues.get(lastIndex).getDate());
			}
			logisticsBatch.add(process);
			currentBreakMark = breakMark;
		}
		fullBatch.add(process);
	}

	/**
	 * The method flushLogisticsBatch() sets the title for the logistics batch,
	 * saves it to hibernate and then populates the global variable with a new,
	 * empty batch.
	 * 
	 * @param processTitle
	 *            the title of the process
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback
	 */
	private void flushLogisticsBatch(String processTitle) throws DAOException {
		logisticsBatch.setTitle(firstGroupFrom(processTitle) + " (" + batchLabel + ')');
		BatchDAO.save(logisticsBatch);
		logisticsBatch = new Batch(Type.LOGISTIC);
		currentBreakMark = null;
		batchLabel = null;
	}

	/**
	 * The method saveFullBatch() sets the title for the allover batch and saves
	 * it to hibernate.
	 * 
	 * @param theProcessTitle
	 *            the title of the process
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback
	 */
	private void saveFullBatch(String theProcessTitle) throws DAOException {
		fullBatch.setTitle(firstGroupFrom(theProcessTitle));
		BatchDAO.save(fullBatch);
	}

	/**
	 * The function firstGroupFrom() extracts the first sequence of charachters
	 * that are no punctuation characters
	 * (<kbd>!&quot;#$%&amp;'()*+,-./:;&lt;=&gt;?@[\]^_`{|}~</kbd>) from the
	 * given string.
	 * 
	 * @param s
	 *            string to parse
	 * @return the first sequence of characters that are no punctuation
	 *         characters
	 */
	private String firstGroupFrom(String s) {
		final Pattern p = Pattern.compile("^[\\p{Punct}\\p{Space}]*([^\\p{Punct}]+)");
		Matcher m = p.matcher(s);
		if (m.find()) {
			return m.group(1).trim();
		} else {
			return s.trim();
		}
	}

	/**
	 * The function clone() creates a copy of this CreateProcessesTask for
	 * providing the possibility to restart it because a Thread can only be
	 * started once.
	 * 
	 * @see de.sub.goobi.helper.tasks.CloneableLongRunningTask#clone()
	 */
	@Override
	public CreateProcessesTask clone() {
		return new CreateProcessesTask(this);
	}
}
