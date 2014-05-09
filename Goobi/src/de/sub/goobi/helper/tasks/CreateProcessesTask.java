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

import org.goobi.mq.processors.CreateNewProcessProcessor;
import org.goobi.production.model.bibliography.course.IndividualIssue;

import de.sub.goobi.forms.ProzesskopieForm;

/**
 * The class CreateProcessesTask is a LongRunningTask to create processes from a
 * course of appearance.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CreateProcessesTask extends CloneableLongRunningTask {
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
	 */
	public CreateProcessesTask(ProzesskopieForm pattern, List<List<IndividualIssue>> processes) {
		super();
		this.pattern = pattern;
		setTitle(getClass().getSimpleName());
		this.processes = new ArrayList<List<IndividualIssue>>(processes.size());
		for (List<IndividualIssue> issues : processes) {
			List<IndividualIssue> process = new ArrayList<IndividualIssue>(issues.size());
			process.addAll(issues);
			this.processes.add(process);
		}
		nextProcessToCreate = 0;
		numberOfProcesses = processes.size();
		setTitle(getClass().getSimpleName());
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
						setStatusMessage("Couldn’t create process title for issue " + issues.get(0).toString());
						setStatusProgress(-1);
						return;
					}
					if (isInterrupted()) {
						stopped();
						return;
					}
					String state = newProcess.NeuenProzessAnlegen();
					if (!state.equals("ProzessverwaltungKopie3"))
						throw new RuntimeException(
								"ProzesskopieForm.NeuenProzessAnlegen() terminated with unexpected result \"" + state
										+ "\".");
					currentTitle = null;
				}
				nextProcessToCreate++;
				setStatusProgress(100 * nextProcessToCreate / numberOfProcesses);
				if (isInterrupted()) {
					stopped();
					return;
				}
			}
			setStatusMessage("done");
		} catch (Exception e) { // ReadException, PreferencesException, SwapException, DAOException, WriteException, IOException, InterruptedException from ProzesskopieForm.NeuenProzessAnlegen()
			setStatusMessage(e.getClass().getSimpleName()
					+ (currentTitle != null ? " while creating " + currentTitle : " in CreateProcessesTask") + ": "
					+ e.getMessage());
			setStatusProgress(-1);
			return;
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
	public CloneableLongRunningTask clone() {
		CreateProcessesTask copy = new CreateProcessesTask(pattern, processes);
		copy.nextProcessToCreate = nextProcessToCreate;
		return copy;
	}
}
