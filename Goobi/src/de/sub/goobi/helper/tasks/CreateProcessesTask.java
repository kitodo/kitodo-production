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

	@Override
	public CreateProcessesTask clone() {
		CreateProcessesTask copy = new CreateProcessesTask(pattern, processes);
		copy.nextProcessToCreate = nextProcessToCreate;
		return copy;
	}
}
