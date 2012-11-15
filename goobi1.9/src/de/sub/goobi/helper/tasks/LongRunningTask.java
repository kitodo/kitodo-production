package de.sub.goobi.helper.tasks;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
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

public class LongRunningTask extends Thread {
	protected static final Logger logger = Logger.getLogger(LongRunningTask.class);

	private int statusProgress = 0;
	private String statusMessage = "";
	private String longMessage = "";
	private String title = "MasterTask";
	private Prozess prozess;
	private boolean isSingleThread = true;

	public void initialize(Prozess inProzess) {
		this.prozess = inProzess;
	}

	public void execute() {
		this.statusProgress = 1;
		this.statusMessage = "running";
		this.isSingleThread = false;
		run();
	}

	public void cancel() {
		this.statusMessage = "stopping";
		this.interrupt();
	}

	protected void stopped() {
		this.statusMessage = "stopped";
		this.statusProgress = -1;
	}

	@Override
	public void run() {
		/*
		 * --------------------- Simulierung einer lang laufenden Aufgabe
		 * -------------------
		 */
		for (int i = 0; i < 100; i++) {
			/*
			 * prüfen, ob der Thread unterbrochen wurde, wenn ja, stopped()
			 */
			if (this.isInterrupted()) {
				stopped();
				return;
			}
			/* lang dauernde Schleife zur Simulierung einer langen Aufgabe */
			for (double j = 0; j < 10000000; j++) {
			}
			setStatusProgress(i);
		}
		setStatusMessage("done");
		setStatusProgress(100);
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
	public int getStatusProgress() {
		return this.statusProgress;
	}

	/**
	 * Meldung über den aktuellen Task
	 * ================================================================
	 */
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * Titel des aktuellen Task
	 * ================================================================
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Setter für Fortschritt nur für vererbte Klassen
	 * ================================================================
	 */
	protected void setStatusProgress(int statusProgress) {
		this.statusProgress = statusProgress;
	}

	/**
	 * Setter für Statusmeldung nur für vererbte Klassen
	 * ================================================================
	 */
	protected void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		if (!this.isSingleThread) {
			Helper.setMeldung(statusMessage);
			logger.debug(statusMessage);
		}
	}

	/**
	 * Setter für Titel nur für vererbte Klassen
	 * ================================================================
	 */
	protected void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Setter für Prozess nur für vererbte Klassen
	 * ================================================================
	 */
	protected void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public String getLongMessage() {
		return this.longMessage;
	}

	public void setLongMessage(String inlongMessage) {
		this.longMessage = inlongMessage;
	}

}
