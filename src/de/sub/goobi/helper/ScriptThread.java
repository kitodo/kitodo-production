/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.helper;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

//TODO: Replace this with a generic container for external tasks.
public class ScriptThread extends Thread {

	HelperSchritte hs = new HelperSchritte();
	private Schritt mySchritt;
	public String rueckgabe = "";
	public boolean stop = false;
	private static final Logger logger = Logger.getLogger(ScriptThread.class);

	public ScriptThread(Schritt inSchritt) {
		mySchritt = inSchritt;
		setDaemon(true);
	}

	public void run() {
		try {
			boolean automatic = mySchritt.isTypAutomatisch();
			hs.executeAllScripts(mySchritt, automatic);
		} catch (SwapException e) {
			logger.error(e);
		} catch (DAOException e) {
			logger.error(e);
		}
	}

	public void stopThread() {
		rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
		stop = true;
	}
}
