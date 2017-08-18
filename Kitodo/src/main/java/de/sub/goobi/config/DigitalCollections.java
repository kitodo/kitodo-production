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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.goobi.production.constants.FileNames;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.data.database.beans.Process;

public class DigitalCollections {

    /**
     * Get possible digital collections for process.
     *
     * @param process
     *            object
     * @return list of Strings
     */
    @SuppressWarnings("unchecked")
    public static List<String> possibleDigitalCollectionsForProcess(Process process) throws JDOMException, IOException {

        List<String> result = new ArrayList<>();
        String filename = FilenameUtils.concat(ConfigCore.getKitodoConfigDirectory(),
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
        for (Element project : projekte) {
            List<Element> projektnamen = project.getChildren("name");
            for (Element projectName : projektnamen) {
                /*
                 * wenn der Projektname aufgeführt wird, dann alle Digitalen
                 * Collectionen in die Liste
                 */
                if (projectName.getText().equalsIgnoreCase(process.getProject().getTitle())) {
                    List<Element> myCols = project.getChildren("DigitalCollection");
                    for (Element digitalCollection : myCols) {
                        result.add(digitalCollection.getText());
                    }
                }
            }
        }
        // If result is empty, get „default“
        if (result.size() == 0) {
            List<Element> children = root.getChildren();
            for (Element child : children) {
                if (child.getName().equals("default")) {
                    List<Element> myCols = child.getChildren("DigitalCollection");
                    for (Element digitalCollection : myCols) {
                        result.add(digitalCollection.getText());
                    }
                }
            }
        }
        return result;
    }
}
