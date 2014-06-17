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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.joda.time.LocalDate;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

import com.sharkysoft.util.NotImplementedException;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.ArrayListMap;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

public class ExportBatchTask extends CloneableLongRunningTask {
	private static final Logger logger = Logger.getLogger(ExportBatchTask.class);

	/**
	 * The field batch holds the batch whose processes are to export.
	 */
	private final Batch batch;

	/**
	 * The field aggregation holds a 4-dimensional list (hierarchy: year, month,
	 * day, issue) into which the issues are aggregated.
	 */
	private ArrayListMap<org.joda.time.LocalDate, String> aggregation;

	/**
	 * The field action holds the number of the action the task currently is in.
	 * Valid values range from 1 to 2. This is to start up at the right position
	 * after an interruption again.
	 */
	private int action;

	/**
	 * The field processesIterator holds an iterator object to walk though the
	 * processes of the batch. The processes in a batch are a Set
	 * implementation, so we use an iterator to walk through and do not use an
	 * index.
	 */
	private Iterator<Prozess> processesIterator;

	/**
	 * The field dividend holds the number of processes that have been processed
	 * in this action. The fields dividend and divisor are used to display a
	 * progress bar.
	 */
	private int dividend;

	/**
	 * The field dividend holds the number of processes to process in each
	 * action. The fields dividend and divisor are used to display a progress
	 * bar.
	 */
	private int divisor;

	/**
	 * Constructor to create an ExportBatchTask.
	 * 
	 * @param batch
	 *            batch to export
	 * @throws HibernateException
	 *             if the batch isn’t attached to a Hibernate session and cannot
	 *             be reattached either
	 */
	public ExportBatchTask(Batch batch) throws HibernateException {
		this.batch = batch;
		action = 1;
		aggregation = new ArrayListMap<LocalDate, String>();
		processesIterator = batch.getProcesses().iterator();
		dividend = 0;
		divisor = batch.getProcesses().size();
	}

	/**
	 * The function run() is the main function of this task (which is a thread).
	 * It will aggregate the data from all processes and then export all
	 * processes with the recombined data. The statusProgress variable is being
	 * updated to show the operator how far the task has proceeded.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Prozess process = null;
		try {
			if (action == 1) {
				while (processesIterator.hasNext()) {
					if (isInterrupted()) {
						stopped();
						return;
					}
					process = processesIterator.next();
					DigitalDocument act = process.getDigitalDocument();
					aggregation.addAll(getIssueDates(act), getMetsPointerURL(process, act));
					setStatusProgress(50 * ++dividend / divisor);
				}
				action = 2;
				processesIterator = batch.getProcesses().iterator();
				dividend = 0;
			}
			if (action == 2)
				while (processesIterator.hasNext()) {
					if (isInterrupted()) {
						stopped();
						return;
					}
					exportProcess(process = processesIterator.next(), aggregation);
					setStatusProgress(50 + 50 * ++dividend / divisor);
				}
		} catch (Exception e) { // PreferencesException, ReadException, SwapException, DAOException, IOException, InterruptedException and some runtime exceptions
			String message = e.getClass().getSimpleName() + " while " + (action == 1 ? "examining " : "exporting ")
					+ (process != null ? process.getTitel() : "") + ": " + e.getMessage();
			logger.error(message, e);
			setStatusMessage(message);
			setStatusProgress(-1);
			return;
		}
	}

	/**
	 * The function getIssueDates() returns a list with all the dates of the
	 * issues contained in this process. The function relies on the assumption
	 * that the child elements descending from the topmost logical structure
	 * entity of the process represent year, month and day of appearance and
	 * that the immediate children of the day level represent issues. The levels
	 * year, month and day must have meta data elements named "PublicationYear",
	 * "PublicationMonth" and "PublicationDay" associated whose value can be
	 * interpreted as an integer.
	 * 
	 * @return a list with the dates of all issues in this process
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set used
	 * @throws ReadException
	 *             if the meta data file cannot be read
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 */
	private static List<LocalDate> getIssueDates(DigitalDocument act) throws PreferencesException, ReadException,
			SwapException, DAOException, IOException, InterruptedException {
		final String METADATA_ELEMENT_YEAR = "PublicationYear";
		final String METADATA_ELEMENT_MONTH = "PublicationMonth";
		final String METADATA_ELEMENT_DAY = "PublicationDay";
		List<LocalDate> result = new LinkedList<LocalDate>();
		DocStruct logicalDocStruct = act.getLogicalDocStruct();
		for (DocStruct annualNode : skipIfNull(logicalDocStruct.getAllChildren())) {
			int year = getMetadataIntValueByName(annualNode, METADATA_ELEMENT_YEAR);
			for (DocStruct monthNode : skipIfNull(annualNode.getAllChildren())) {
				int monthOfYear = getMetadataIntValueByName(monthNode, METADATA_ELEMENT_MONTH);
				for (DocStruct dayNode : skipIfNull(monthNode.getAllChildren())) {
					LocalDate appeared = new LocalDate(year, monthOfYear, getMetadataIntValueByName(dayNode,
							METADATA_ELEMENT_DAY));
					for (@SuppressWarnings("unused")
					DocStruct entry : skipIfNull(dayNode.getAllChildren()))
						result.add(appeared);
				}
			}
		}
		return result;
	}

	/**
	 * The function skipIfNull() returns the list passed in, or
	 * Collections.emptyList() if the list is null.
	 * {@link DocStruct#getAllChildren()} does return null if no children are
	 * contained. This would throw a NullPointerException if passed into a loop.
	 * Replacing null by Collections.emptyList() results in the loop to be
	 * silently skipped, so that the outer code continues normally.
	 * 
	 * @param list
	 *            list to check for being null
	 * @return the list, Collections.emptyList() if the list is null
	 */
	private static <T> List<T> skipIfNull(List<T> list) {
		if (list == null)
			list = Collections.emptyList();
		return list;
	}

	/**
	 * The function getMetadataIntValueByName() returns the value of a named
	 * meta data entry associated with a structure entity as int.
	 * 
	 * @param structureEntity
	 *            structureEntity to get the meta data value from
	 * @param name
	 *            name of the meta data element whose value is to obtain
	 * @return value of a meta data element with the given name
	 * @throws NoSuchElementException
	 *             if there is no such element
	 * @throws NumberFormatException
	 *             if the value cannot be parsed to int
	 */
	private static int getMetadataIntValueByName(DocStruct structureEntity, String name) throws NoSuchElementException,
			NumberFormatException {
		List<MetadataType> metadataTypes = structureEntity.getType().getAllMetadataTypes();
		for (MetadataType metadataType : metadataTypes)
			if (name.equals(metadataType.getName()))
				return Integer.parseInt(new HashSet<Metadata>(structureEntity.getAllMetadataByType(metadataType))
						.iterator().next().getValue());
		throw new NoSuchElementException();
	}

	/**
	 * The function getMetsPointerURL investigates the METS pointer URL of the
	 * process.
	 * 
	 * @param process
	 *            process to take values for path variables from
	 * @param act
	 *            act to take values for meta data variables from
	 * @return the METS pointer URL of the process
	 * @throws PreferencesException
	 *             if the no node corresponding to the file format is available
	 *             in the rule set used
	 * @throws ReadException
	 *             if the meta data file cannot be read
	 * @throws SwapException
	 *             if an error occurs while the process is swapped back in
	 * @throws DAOException
	 *             if an error occurs while saving the fact that the process has
	 *             been swapped back in to the database
	 * @throws IOException
	 *             if creating the process directory or reading the meta data
	 *             file fails
	 * @throws InterruptedException
	 *             if the current thread is interrupted by another thread while
	 *             it is waiting for the shell script to create the directory to
	 *             finish
	 */
	private static String getMetsPointerURL(Prozess process, DigitalDocument act) throws PreferencesException,
			ReadException, SwapException, DAOException, IOException, InterruptedException {
		VariableReplacer replacer = new VariableReplacer(act, process.getRegelsatz().getPreferences(), process, null);
		return replacer.replace(process.getProjekt().getMetsPointerPath());
	}

	private static void exportProcess(Prozess process, ArrayListMap<LocalDate, String> aggregation) {
		/* TODO */throw new NotImplementedException();
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
		ExportBatchTask copy = new ExportBatchTask(batch);
		copy.action = action;
		copy.aggregation = aggregation;
		copy.processesIterator = processesIterator;
		copy.dividend = dividend;
		copy.divisor = divisor;
		return copy;
	}
}