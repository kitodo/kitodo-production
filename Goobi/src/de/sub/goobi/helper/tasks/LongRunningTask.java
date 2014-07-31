package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

public class LongRunningTask extends EmptyTask {
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

	@Deprecated
	public void cancel() {
		this.interrupt();
	}

	/**
	 * The function clone() creates a new instance of this object. This is
	 * necessary for Threads that have terminated in order to render to run them
	 * again possible.
	 * 
	 * @see de.sub.goobi.helper.tasks.EmptyTask#clone()
	 */
	@Override
	public EmptyTask clone() {
		LongRunningTask lrt = null;
		try {
			lrt = getClass().newInstance();
			lrt.initialize(prozess);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		}
		return lrt;
	}

	/**
	 * The method stopped() has become redundant due to newer development. It
	 * had been used to record that the thread has stopped. The thread state is
	 * now directly derived from {@link Thread#getState()} which is reliable in
	 * determining whether the thread has died, independent of whether it ever
	 * managed to call stopped() or died before.
	 */
	@Deprecated
	protected void stopped() {
	}

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
	 */
	@Deprecated
	public String getStatusMessage() {
		return super.getTaskState().toString().toLowerCase();
	}

	/**
	 * Titel des aktuellen Task
	 * ================================================================
	 */
	@Deprecated
	public String getTitle() {
		return super.getName();
	}

	/**
	 * Setter für Fortschritt nur für vererbte Klassen
	 * ================================================================
	 */
	@Deprecated
	protected void setStatusProgress(int statusProgress) {
		super.setProgress(statusProgress);
	}

	/**
	 * Setter für Statusmeldung nur für vererbte Klassen
	 * ================================================================
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
	}

	@Deprecated
	public void setLongMessage(String inlongMessage) {
		super.setWorkDetail(inlongMessage);
	}

}
