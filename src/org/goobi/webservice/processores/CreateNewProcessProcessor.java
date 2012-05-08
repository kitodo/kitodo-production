package org.goobi.webservice.processores;

import java.util.Set;

import javax.jms.MapMessage;

import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;

import de.sub.goobi.config.ConfigMain;

public class CreateNewProcessProcessor extends ActiveMQProcessor {
	public CreateNewProcessProcessor() {
		super(ConfigMain.getParameter("activeMQ.createNewProcess.queue", null));
	}

	@Override
	protected void process(MapMessage ticket) throws Exception {
		MapMessageObjectReader ticketReader = new MapMessageObjectReader(ticket);

		Set<String> collections = ticketReader.getMandatorySetOfString("collections");
		String field = ticketReader.getMandatoryString("field");
		String id = ticketReader.getMandatoryString("id");
		String opac = ticketReader.getMandatoryString("opac");
		String template = ticketReader.getMandatoryString("template");
		String value = ticketReader.getMandatoryString("value");

		// Call process templates page and click on blue asterisk
		
		
		// Select collection(s)

		
		// Choose catalogue and search field, enter search string and click button
		
		
		// Enter digital id and click to generate process title
		
		
		// submit create process
		
		
	}
}
