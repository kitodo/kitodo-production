/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.config;

import de.sub.goobi.beans.Prozess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.goobi.production.constants.FileNames;
import org.goobi.production.constants.Parameters;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DigitalCollections {

	/**
	 * @param process add description
	 * @return add description
	 * @throws JDOMException add description
	 * @throws IOException add description
	 */
	@SuppressWarnings("unchecked")
	public static List<String> possibleDigitalCollectionsForProcess(Prozess process) throws JDOMException, IOException {

		List<String> result = new ArrayList<String>();
		String filename = FilenameUtils.concat(ConfigMain.getParameter(Parameters.CONFIG_DIR),
				FileNames.DIGITAL_COLLECTIONS_FILE);
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
			Element projekt = iter.next();
			List<Element> projektnamen = projekt.getChildren("name");
			for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
				Element projektname = iterator.next();

				/*
				 * wenn der Projektname aufgeführt wird, dann alle Digitalen Collectionen in die Liste
				 */
				if (projektname.getText().equalsIgnoreCase(process.getProjekt().getTitel())) {
					List<Element> myCols = projekt.getChildren("DigitalCollection");
					for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
						Element col = it2.next();
						result.add(col.getText());
					}
				}
			}
		}
		// If result is empty, get „default“
		if (result.size() == 0) {
			List<Element> primaryChildrenIterator = root.getChildren();
			for (Iterator<Element> iter = primaryChildrenIterator.iterator(); iter.hasNext();) {
				Element child = iter.next();
				if (child.getName().equals("default")) {
					List<Element> myCols = child.getChildren("DigitalCollection");
					for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
						Element col = it2.next();
						result.add(col.getText());
					}
				}
			}
		}
		return result;
	}
}
