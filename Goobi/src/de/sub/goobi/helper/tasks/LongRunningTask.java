package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.Helper;

/**
 * @author unascribed
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 * @deprecated New task implementations should directly implement EmptyTask.
 */
@Deprecated
public abstract class LongRunningTask extends EmptyTask implements INameableTask {
	/**
	 * No-argument constructor. Creates an empty long running task. Must be made
	 * explicit because a constructor taking an argument is present.
	 * 
	 * @deprecated New task implementations should directly implement EmptyTask.
	 */
	@Deprecated
	public LongRunningTask() {
		super((String) null);
	}

	/**
	 * The clone constructor creates a new instance of this object. This is
	 * necessary for Threads that have terminated in order to render to run them
	 * again possible.
	 * 
	 * @param master
	 *            copy master to create a clone of
	 */
	public LongRunningTask(LongRunningTask master) {
		super(master);
		initialize(master.prozess);
	}

	protected static final Logger logger = Logger.getLogger(LongRunningTask.class);

	private Prozess prozess;
	private boolean isSingleThread = true;

	public void initialize(Prozess inProzess) {
		this.prozess = inProzess;
	}

	/**
	 * The method setShowMessages() can be used to set a flag whether this long
	 * running task is executing asynchronously or not, in the latter case it
	 * shall show messages to the user using
	 * {@link de.sub.goobi.helper.Helper#setMeldung(String)}, otherwise not.
	 * 
	 * @param show
	 *            whether to show messages to the user
	 */
	public void setShowMessages(boolean show) {
		isSingleThread = !show;
	}

	/**
	 * @deprecated Replaced by {@link Thread#interrupt()}.
	 */
	@Deprecated
	public void cancel() {
		this.interrupt();
	}

	/**
	 * Calls the clone constructor to create a not yet executed instance of this
	 * thread object. This is necessary for threads that have terminated in
	 * order to render possible to restart them.
	 * 
	 * @return a not-yet-executed replacement of this thread
	 * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
	 */
	@Override
	public abstract EmptyTask replace();

	/**
	 * The method stopped() had been used to record that the thread has stopped.
	 * 
	 * @deprecated The method stopped() has become redundant due to newer
	 *             development. The thread state is now directly derived from
	 *             {@link Thread#getState()} which is reliable in determining
	 *             whether the thread has died, independent of whether it ever
	 *             managed to call stopped() or died before.
	 */
	@Deprecated
	protected void stopped() {
	}

	/**
	 * Returns the display name of the task to show to the user.
	 * 
	 * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
	 */
	@Override
	public abstract String getDisplayName();

	/**
	 * Prozess-Getter
	 * ================================================================
	 */
	public Prozess getProzess() {
		return this.prozess;
	}

	/**
	 * Status des Tasks in Angabe von Prozent
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link EmptyTask#getProgress()}.
	 */
	@Deprecated
	public int getStatusProgress() {
		if (super.getException() != null) {
			return -1;
		}
		return super.getProgress();
	}

	/**
	 * Meldung über den aktuellen Task
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link EmptyTask#getTaskState()}.
	 */
	@Deprecated
	public String getStatusMessage() {
		return super.getTaskState().toString().toLowerCase();
	}

	/**
	 * Titel des aktuellen Task
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link Thread#getName()}.
	 */
	@Deprecated
	public String getTitle() {
		return super.getName();
	}

	/**
	 * Setter für Fortschritt nur für vererbte Klassen
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link EmptyTask#setProgress(int)}.
	 */
	@Deprecated
	protected void setStatusProgress(int statusProgress) {
		super.setProgress(statusProgress);
	}

	/**
	 * @deprecated Replaced by {@link EmptyTask#setProgress(double)}.
	 */
	@Deprecated
	protected void setStatusProgress(double statusProgress) {
		super.setProgress(statusProgress);
	}

	/**
	 * Setter für Statusmeldung nur für vererbte Klassen
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link EmptyTask#setWorkDetail(String)}.
	 */
	@Deprecated
	protected void setStatusMessage(String statusMessage) {
		super.setWorkDetail(statusMessage);
		if (!this.isSingleThread) {
			Helper.setMeldung(statusMessage);
			logger.debug(statusMessage);
		}
	}

	/**
	 * Setter für Titel nur für vererbte Klassen
	 * ================================================================
	 * 
	 * @deprecated Replaced by {@link EmptyTask#EmptyTask(String)}.
	 */
	@Deprecated
	protected void setTitle(String title) {
		super.setNameDetail(title);
	}

	/**
	 * Setter für Prozess nur für vererbte Klassen
	 * ================================================================
	 */
	protected void setProzess(Prozess prozess) {
		this.prozess = prozess;
		setNameDetail(prozess.getTitel());
	}

	/**
	 * @deprecated Replaced by {@link EmptyTask#setWorkDetail(String)}.
	 */
	@Deprecated
	public void setLongMessage(String inlongMessage) {
		super.setWorkDetail(inlongMessage);
	}

}
