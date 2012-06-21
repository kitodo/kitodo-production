package org.goobi.webservice.processors;

import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Schritt;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.AktuelleSchritteForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.SchrittDAO;

/**
 * This is a web service interface to close steps. You have to provide the step
 * id as “id”; you can add a field “message” which will be added to the wiki
 * field.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class FinaliseStepProcessor extends ActiveMQProcessor {

	public FinaliseStepProcessor() {
		super(ConfigMain.getParameter("activeMQ.finaliseStep.queue", null));
	}

	protected void process(MapMessageObjectReader ticket) throws Exception {
		AktuelleSchritteForm dialog = getStep(ticket.getMandatoryInteger("id"));
		if (ticket.hasField("message"))
			addMessageToWikiField(dialog, ticket.getString("message"));
		dialog.SchrittDurchBenutzerAbschliessen();
	}

	protected AktuelleSchritteForm getStep(Integer stepID) throws DAOException {
		AktuelleSchritteForm result = new AktuelleSchritteForm();
//		Session hibernateSession = Helper.getHibernateSession();
//		Criteria databaseRequest = hibernateSession.createCriteria(Schritt.class).add(Restrictions.eq("id", stepID));
//		Schritt step = (Schritt) databaseRequest.list().get(0);
		Schritt step = new SchrittDAO().get(stepID);
		result.setMySchritt(step);
		return result;
	}

	protected void addMessageToWikiField(AktuelleSchritteForm form, String message) {
		StringBuilder composer = new StringBuilder();
		String wikiField = form.getWikiField();
		if (wikiField != null && wikiField.length() > 0){
			composer.append(wikiField);
			composer.append("\r\n");
		}
		composer.append("<p>");
		composer.append(message);
		composer.append("</p>");
		form.setWikiField(composer.toString());
		return;
	}

}
