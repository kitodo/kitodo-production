package de.sub.goobi.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import de.sub.goobi.beans.Prozess;

public class DigitalCollections {

	@SuppressWarnings("unchecked")
	public static List<String> possibleDigitalCollectionsForProcess(
			Prozess process) throws JDOMException, IOException {
		
		List<String> result = new ArrayList<String>();
		String filename = ConfigMain.getParameter("KonfigurationVerzeichnis") + "digitalCollections.xml";
		if (!(new File(filename).exists())) {
			throw new FileNotFoundException("File not found: " + filename);
		}
		
		/* Datei einlesen und Root ermitteln */
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new File(filename));
		Element root = doc.getRootElement();
		/* alle Projekte durchlaufen */
		List<Element> projekte = root.getChildren();
		for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
			Element projekt = (Element) iter.next();
			List<Element> projektnamen = projekt.getChildren("name");
			for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
				Element projektname = (Element) iterator.next();

				/*
				 * wenn der Projektname aufgeführt wird, dann alle Digitalen Collectionen in die Liste
				 */
				if (projektname.getText().equalsIgnoreCase(process.getProjekt().getTitel())) {
					List<Element> myCols = projekt.getChildren("DigitalCollection");
					for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
						Element col = (Element) it2.next();
						result.add(col.getText());
					}
				}
			}
		}
		return result;
	}
}
