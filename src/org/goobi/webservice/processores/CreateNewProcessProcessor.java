package org.goobi.webservice.processores;

import java.util.List;
import java.util.Set;

import javax.jms.MapMessage;

import org.goobi.production.flow.statistics.hibernate.UserProcessesFilter;
import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;
import org.hibernate.Criteria;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.ProzesskopieForm;
import dubious.sub.goobi.helper.Page;

public class CreateNewProcessProcessor extends ActiveMQProcessor {
	public CreateNewProcessProcessor() {
		super(ConfigMain.getParameter("activeMQ.createNewProcess.queue", null));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void process(MapMessage ticket) throws Exception {
		MapMessageObjectReader ticketReader = new MapMessageObjectReader(ticket);

		Set<String> collectionsArg = ticketReader
				.getMandatorySetOfString("collections");
		String fieldArg = ticketReader.getMandatoryString("field");
		String idArg = ticketReader.getMandatoryString("id");
		String opacArg = ticketReader.getMandatoryString("opac");
		String templateArg = ticketReader.getMandatoryString("template");
		String valueArg = ticketReader.getMandatoryString("value");

		// 1. Call process templates page and click on blue asterisk

		// 1a) Get a list of all templates
		List<Prozess> allTemplates = (List<Prozess>) new UserProcessesFilter()
				.getCriteria().setFirstResult(0)
				.setMaxResults(Integer.MAX_VALUE).list();

		// 1b) Find matching template
		Prozess selectedTemplate = null;
		for (Prozess aTemplate : allTemplates) {
			if (aTemplate.getTitel().equals(templateArg))
				selectedTemplate = aTemplate;
		}
		if (selectedTemplate == null)
			throw new IllegalArgumentException(
					"Bad argument: No template's title matches \""
							+ templateArg + "\".");

		// 1c) “Click asterisk”
		ProzesskopieForm newProcess = new ProzesskopieForm();
		newProcess.setProzessVorlage(selectedTemplate);
		newProcess.Prepare();

		// 2. Select collection(s)

		// 3. Choose catalogue and search field, enter search string and click
		// button

		newProcess.setOpacKatalog(opacArg);
		newProcess.setOpacSuchfeld(fieldArg);
		newProcess.setOpacSuchbegriff(valueArg);

		newProcess.OpacAuswerten();

		// 4. Enter digital id and click to generate process title

		// 5. submit create process

	}
}
