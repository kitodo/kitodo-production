package org.goobi.webservice.processores;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jms.MapMessage;

import org.apache.log4j.Logger;
import org.goobi.webservice.ActiveMQProcessor;
import org.goobi.webservice.MapMessageObjectReader;
import org.goobi.webservice.WebServiceResult;
import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.ReportLevel;

public class CreateNewProcessProcessor extends ActiveMQProcessor {
	// private static final Logger logger = Logger
	// .getLogger(CreateNewProcessProcessor.class);

	final int WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR = 131072; // msec
	final int WAIT_AT_MOST_ON_OPAC_ERROR = 2097152; // msec

	public CreateNewProcessProcessor() {
		super(ConfigMain.getParameter("activeMQ.createNewProcess.queue", null));
	}

	@Override
	protected void process(MapMessage ticket) throws Exception {
		MapMessageObjectReader ticketReader = new MapMessageObjectReader(ticket);

		Set<String> collections = ticketReader
				.getMandatorySetOfString("collections");
		String field = ticketReader.getMandatoryString("field");
		String id = ticketReader.getMandatoryString("id");
		String opac = ticketReader.getMandatoryString("opac");
		String template = ticketReader.getMandatoryString("template");
		String value = ticketReader.getMandatoryString("value");

		createNewProcessMain(template, opac, field, value, id, collections);
	}

	public void test() {
		String template = "Digitalisierungsworkflow_fuer_Monographien";
		String opac = "GBV";
		String field = "12";
		String value = "01649377X";
		String id = "createdByTest";
		Set<String> collections = new HashSet<String>();
		collections.add("DigiWunschbuch");

		createNewProcessMain(template, opac, field, value, id, collections);
	}

	private void createNewProcessMain(String template, String opac,
			String field, String value, String id, Set<String> collections)
			throws IllegalArgumentException {

		ProzesskopieForm newProcess = newProcessFromTemplate(template);
		newProcess.setDigitalCollections(validCollectionsForProcess(
				collections, newProcess));
		getBibliorgaphicData(newProcess, id, opac, field, value);

		// 4. Enter digital id and click to generate process title

		// 5. submit create process

	}

	private ProzesskopieForm newProcessFromTemplate(String templateTitle)
			throws IllegalArgumentException {
		ProzesskopieForm result = new ProzesskopieForm();

		List<Prozess> allTemplates = allTemplatesFromDatabase();
		Prozess selectedTemplate = selectTemplateByTitle(allTemplates,
				templateTitle);
		result.setProzessVorlage(selectedTemplate);
		result.Prepare();
		return result;
	}

	private List<Prozess> allTemplatesFromDatabase() {
		Session hibernateSession = Helper.getHibernateSession();
		Criteria request = hibernateSession.createCriteria(Prozess.class);

		@SuppressWarnings("unchecked")
		List<Prozess> result = (List<Prozess>) request.list();

		return result;
	}

	private Prozess selectTemplateByTitle(List<Prozess> allTemplates,
			String templateTitle) throws IllegalArgumentException {

		Prozess result = null;
		for (Prozess aTemplate : allTemplates) {
			if (aTemplate.getTitel() == templateTitle) {
				result = aTemplate;
				break;
			}
		}
		if (result == null)
			throw new IllegalArgumentException("Bad argument: No template \""
					+ templateTitle + "\" available.");
		return result;
	}

	private List<String> validCollectionsForProcess(Set<String> collections,
			ProzesskopieForm process) throws IllegalArgumentException {

		HashSet<String> possibleCollections = new HashSet<String>(
				process.getPossibleDigitalCollections());
		if (!possibleCollections.containsAll(collections))
			throw new IllegalArgumentException(
					"Bad argument: One or more elements of \"collections\" is not available for template \""
							+ process.getProzessVorlage().getTitel() + "\".");
		return new ArrayList<String>(collections);
	}
	
	private void getBibliorgaphicData(ProzesskopieForm inputForm, String id, String opac,
			String field, String value) throws RuntimeException {
		boolean success = true;
		long millisPassed = 0;
		FacesMessage message = null;

		inputForm.setOpacKatalog(opac);
		inputForm.setOpacSuchfeld(field);
		inputForm.setOpacSuchbegriff(value);

		do {
			inputForm.OpacAuswerten();

			FacesContext context = FacesContext.getCurrentInstance();
			@SuppressWarnings("unchecked")
			Iterator<FacesMessage> messages = context.getMessages(null);
			while (messages.hasNext()) {
				message = messages.next();
				if (FacesMessage.SEVERITY_ERROR == message.getSeverity()) {
					if (!message.getSummary().startsWith(
							"Error on reading opac"))
						throw new RuntimeException(message.getSummary());
					else {
						new WebServiceResult(queueName, id, ReportLevel.WARN,
								message.getSummary()).send();
						messages.remove();
						try {
							wait(WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR);
						} catch (InterruptedException irrelevant) {
						}
						millisPassed += WAIT_BETWEEN_OPAC_REQUESTS_ON_ERROR;
						success = false;
					}
				}
			}
		} while (!success && millisPassed < WAIT_AT_MOST_ON_OPAC_ERROR);
		if(!success)
			throw new RuntimeException(message.getSummary());
	}	
}
