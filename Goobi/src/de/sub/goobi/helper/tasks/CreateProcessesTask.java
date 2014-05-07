package de.sub.goobi.helper.tasks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.goobi.production.model.bibliography.course.IndividualIssue;

import de.sub.goobi.forms.ProzesskopieForm;

/**
 * The class CreateProcessesTask is a LongRunningTask to create processes from a
 * course of appearance.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CreateProcessesTask extends LongRunningTask {

	/**
	 * FieldGetter instances are used to pass implementations to access data
	 * fields on individual issues to the {@link #join()} function that
	 * processes lists of individual issues.
	 */
	private interface FieldGetter {
		public String getFrom(IndividualIssue issue);
	}

	/**
	 * The YearGetter is the command to access the year of an individual issue.
	 */
	private class YearGetter implements FieldGetter {
		@Override
		public String getFrom(IndividualIssue issue) {
			return Integer.toString(issue.getDate().getYear());
		}
	}

	/**
	 * The MonthGetter is the command to access the month of an individual
	 * issue.
	 */
	private class MonthGetter implements FieldGetter {
		@Override
		public String getFrom(IndividualIssue issue) {
			return Integer.toString(issue.getDate().getMonthOfYear());
		}
	}

	/**
	 * The DayGetter is the command to access the day of an individual issue.
	 */
	private class DayGetter implements FieldGetter {
		@Override
		public String getFrom(IndividualIssue issue) {
			return Integer.toString(issue.getDate().getDayOfMonth());
		}
	}

	/**
	 * The IssueGetter is the command to access the issue name of an individual
	 * issue.
	 */
	private class IssueGetter implements FieldGetter {
		@Override
		public String getFrom(IndividualIssue issue) {
			return issue.getHeading();
		}
	}

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
	 * The field processes holds a List of List of IndividualIssue objects that
	 * processes will be created from. Each list object, which is a list itself,
	 * represents a process to create. Each process can consist of many issues
	 * which will be part of that process.
	 */
	private final List<List<IndividualIssue>> processes;

	/**
	 * The field processCreator holds a ProzesskopieForm instance that will be
	 * used for the creation of processes.
	 */
	private final ProzesskopieForm processCreator;

	/**
	 * The class CreateProcessesTask is a LongRunningTask to create processes
	 * from a course of appearance.
	 * 
	 * @param processCreator
	 *            a ProzesskopieForm to use for creating processes
	 * @param processes
	 *            a list of processes to create
	 */
	public CreateProcessesTask(ProzesskopieForm processCreator, List<List<IndividualIssue>> processes) {
		this.processCreator = processCreator;
		this.processes = new ArrayList<List<IndividualIssue>>(processes.size());
		for (List<IndividualIssue> issues : processes) {
			List<IndividualIssue> process = new ArrayList<IndividualIssue>(issues.size());
			process.addAll(issues);
			this.processes.add(process);
		}
		nextProcessToCreate = 0;
		numberOfProcesses = processes.size();
	}

	/**
	 * The function run() is the main function of this task, which is a thread.
	 * It populates the the data fields "Issue", "PublicationDay",
	 * "PublicationMonth" and "PublicationYear"—if they are available—with the
	 * value(s) relevant to the issue(s) contained in the process being created,
	 * triggers the calculation of the process title and then initiates the
	 * process creation one by one. The statusProgress variable is being updated
	 * to show the operator how far the task has proceeded.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		String currentTitle = "";
		try {
			while (nextProcessToCreate < numberOfProcesses) {
				List<IndividualIssue> issues = processes.get(nextProcessToCreate);
				processCreator.setAdditionalField("PublicationYear", join(new YearGetter(), issues, ", "), false);
				processCreator.setAdditionalField("PublicationMonth", join(new MonthGetter(), issues, ", "), false);
				processCreator.setAdditionalField("PublicationDay", join(new DayGetter(), issues, ", "), false);
				processCreator.setAdditionalField("Issue", join(new IssueGetter(), issues, ", "), false);
				currentTitle = processCreator.generateTitle();
				processCreator.getProzessKopie().setTitel(currentTitle);
				if (isInterrupted()) {
					stopped();
					return;
				}
				if (!"ProzessverwaltungKopie3".equals(processCreator.NeuenProzessAnlegen()))
					throw new RuntimeException();
				nextProcessToCreate++;
				setStatusProgress(100 * nextProcessToCreate / numberOfProcesses);
				if (isInterrupted()) {
					stopped();
					return;
				}
			}
			setStatusMessage("done");
		} catch (Exception e) { // ReadException, PreferencesException, SwapException, DAOException, WriteException, IOException, InterruptedException
			setStatusMessage(e.getClass().getSimpleName() + " while creating " + currentTitle + ": " + e.getMessage());
			setStatusProgress(-1);
			return;
		}
	}

	/**
	 * The function join() uses a getter function to get a value from each entry
	 * of the given list of issues and returns all of the values as string,
	 * separated by a given separator. Duplicate values will be skipped.
	 * 
	 * @param getter
	 *            getter function to extract the given field
	 * @param issues
	 *            list of issues to examine
	 * @param separator
	 *            separator to use for joining the values
	 * @return all values returned by the getter as string, without duplicates
	 */
	private static String join(FieldGetter getter, List<IndividualIssue> issues, String separator) {
		int separatorLength = separator.length();
		int capacity = -separatorLength;
		LinkedHashSet<String> uniqueValues = new LinkedHashSet<String>();
		String value;
		for (IndividualIssue issue : issues) {
			if (uniqueValues.add(value = getter.getFrom(issue)))
				capacity += value.length() + separatorLength;
		}
		StringBuilder result = new StringBuilder(capacity);
		boolean maiden = true;
		for (String uniqueValue : uniqueValues) {
			if (maiden) {
				maiden = false;
			} else {
				result.append(separator);
			}
			result.append(uniqueValue);
		}
		return result.toString();
	}
}
