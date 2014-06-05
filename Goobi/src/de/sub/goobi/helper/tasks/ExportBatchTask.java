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

import java.util.Iterator;

import org.hibernate.HibernateException;

import com.sharkysoft.util.NotImplementedException;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Prozess;

public class ExportBatchTask extends CloneableLongRunningTask {

	/**
	 * The field batch holds the batch whose processes are to export.
	 */
	private final Batch batch;

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
	 * @throws HibernateException
	 *             if the batch isn’t attached to a Hibernate session and cannot
	 *             be reattached either
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() throws HibernateException {
		if (action == 1) {
			while (processesIterator.hasNext()) {
				if (isInterrupted()) {
					stopped();
					return;
				}
				aggregateDataFromProcess(processesIterator.next());
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
				exportProcess(processesIterator.next());
				setStatusProgress(50 + 50 * ++dividend / divisor);
			}
	}

	/**
	 * The function aggregateDataFromProcess() extracts …
	 * 
	 * @param process
	 *            process to examine
	 */
	private static void aggregateDataFromProcess(Prozess process) {
		/* TODO */throw new NotImplementedException();
	}

	/**
	 * The method exportProcess() …
	 * 
	 * @param process
	 *            process to export
	 */
	private static void exportProcess(Prozess process) {
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
		copy.processesIterator = processesIterator;
		copy.dividend = dividend;
		copy.divisor = divisor;
		return copy;
	}
}