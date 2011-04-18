package de.sub.goobi.helper;

import org.apache.log4j.Logger;

import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;

//TODO: Replace this with a generic container for external tasks.
public class ScriptThread extends Thread {
	// private static final Logger myLogger = Logger.getLogger(AgoraImportThread.class);
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
			if (automatic) {
				Helper.getHibernateSession().close();
			}
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