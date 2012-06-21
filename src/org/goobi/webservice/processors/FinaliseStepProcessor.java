package org.goobi.webservice.processors;

import org.apache.commons.lang.StringEscapeUtils;
import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.AktuelleSchritteForm;
import de.sub.goobi.persistence.SchrittDAO;

/**
 * This is a web service interface to close steps. You have to provide the step
 * id as “id”; you can add a field “message” which will be added to the wiki
 * field.
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class FinaliseStepProcessor extends ActiveMQProcessor {

	/**
	 * The default constructor looks up the queue name to use in
	 * GoobiConfig.properties. If that is not configured and “null” is passed to
	 * the super constructor, this will prevent
	 * ActiveMQDirector.registerListeners() from starting this service.
	 */
	public FinaliseStepProcessor() {
		super(ConfigMain.getParameter("activeMQ.finaliseStep.queue", null));
	}

	/**
	 * This is the main routine processing incoming tickets. It gets an
	 * AktuelleSchritteForm object, sets it to the appropriate step which is
	 * retrieved from the database, appends the message − if any − to the wiki
	 * field, and executes the form’s the step close function.
	 * 
	 * @param ticket
	 *            the incoming message
	 * 
	 * @see org.goobi.webservice.ActiveMQProcessor#process(org.goobi.webservice.MapMessageObjectReader)
	 */
	protected void process(MapMessageObjectReader ticket) throws Exception {
		AktuelleSchritteForm dialog = new AktuelleSchritteForm();
		Integer stepID = ticket.getMandatoryInteger("id");
		dialog.setMySchritt(new SchrittDAO().get(stepID));
		if (ticket.hasField("message"))
			addMessageToWikiField(dialog, ticket.getString("message"));
		dialog.SchrittDurchBenutzerAbschliessen();
	}

	/**
	 * The addMessageToWikiField() method is a helper method which composes the
	 * new wiki field using a StringBuilder. The message is encoded using HTML
	 * entities to prevent certain characters from playing merry havoc when the
	 * message box shall be rendered in a browser later.
	 * 
	 * @param form
	 *            the AktuelleSchritteForm which is the owner of the wiki field
	 * @param message
	 *            the message to append
	 */
	protected void addMessageToWikiField(AktuelleSchritteForm form, String message) {
		StringBuilder composer = new StringBuilder();
		String wikiField = form.getWikiField();
		if (wikiField != null && wikiField.length() > 0) {
			composer.append(wikiField);
			composer.append("\r\n");
		}
		composer.append("<p>");
		composer.append(StringEscapeUtils.escapeHtml(message));
		composer.append("</p>");
		form.setWikiField(composer.toString());
		return;
	}

}
