/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

package org.goobi.mq.processors;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.goobi.mq.ActiveMQProcessor;
import org.goobi.mq.MapMessageObjectReader;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.ProcessProperty;

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
	 * goobi_config.properties. If that is not configured and “null” is passed to
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
	 * @see org.goobi.mq.ActiveMQProcessor#process(org.goobi.mq.MapMessageObjectReader)
	 */
	protected void process(MapMessageObjectReader ticket) throws Exception {
		AktuelleSchritteForm dialog = new AktuelleSchritteForm();
		Integer stepID = ticket.getMandatoryInteger("id");
		dialog.setMySchritt(new SchrittDAO().get(stepID));
		if (ticket.hasField("properties")) updateProperties(dialog, ticket.getMapOfStringToString("properties"));
		if (ticket.hasField("message")) addMessageToWikiField(dialog, ticket.getString("message"));
		dialog.SchrittDurchBenutzerAbschliessen();
	}

	/**
	 * The method updateProperties() transfers the properties to set into
	 * Goobi’s data model.
	 * 
	 * @param dialog
	 *            The AktuelleSchritteForm that we work with
	 * @param propertiesToSet
	 *            A Map with the properties to set
	 */
	protected void updateProperties(AktuelleSchritteForm dialog, Map<String, String> propertiesToSet) {
		List<ProcessProperty> availableProperties = dialog.getProcessProperties();
		for (int position = 0; position < availableProperties.size(); position++) {
			ProcessProperty propertyAtPosition = availableProperties.get(position);
			String key = propertyAtPosition.getName();
			if (propertiesToSet.containsKey(key)) {
				String desiredValue = propertiesToSet.get(key);
				AccessCondition permissions = propertyAtPosition.getCurrentStepAccessCondition();
				if (AccessCondition.WRITE.equals(permissions) || AccessCondition.WRITEREQUIRED.equals(permissions)) {
					propertyAtPosition.setValue(desiredValue);
					if (dialog.getContainer() == null || dialog.getContainer() == 0) {
						dialog.setProcessProperty(propertyAtPosition);
					} else
						availableProperties.set(position, propertyAtPosition);
					dialog.saveCurrentProperty();
				}
			}
		}
	}

	/**
	 * Inspects a list of ProcessProperty for a ProcessProperty with given name
	 * and sufficient AccessCondition and sets it to newValue. This method is
	 * separated because it has to be called for the AktuelleSchritteForm’s
	 * containerlessProperties and each of the containers as well.
	 * 
	 * @param propertyList
	 *            List of ProcessProperty to inspect
	 * @param key
	 *            name value of the ProcessProperty to modify
	 * @param newValue
	 *            new value to set the ProcessProperty to
	 */
	protected void setProperty(List<ProcessProperty> propertyList, String key, String newValue) {

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
