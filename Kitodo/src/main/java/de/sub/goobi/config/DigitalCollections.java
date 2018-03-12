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

    private static List<String> digitalCollections;
    private static List<String> possibleDigitalCollection;

    public static List<String> getDigitalCollections() {
        if (digitalCollections == null) {
            digitalCollections = new ArrayList<>();
        }
        return digitalCollections;
    }

    public static List<String> getPossibleDigitalCollection() {
        if (possibleDigitalCollection == null) {
            possibleDigitalCollection = new ArrayList<>();
        }
        return DigitalCollections.possibleDigitalCollection;
    }

    /**
     * Get possible digital collections for process.
     *
     * @param process
     *            object
     */
    @SuppressWarnings("unchecked")
    public static void possibleDigitalCollectionsForProcess(Process process) throws JDOMException, IOException {
        digitalCollections = new ArrayList<>();
        possibleDigitalCollection = new ArrayList<>();

        ArrayList<String> defaultCollections = new ArrayList<>();

        Element root = getRoot();

        /* alle Projekte durchlaufen */
        List<Element> projects = root.getChildren();
        for (Element project : projects) {
            // collect default collections
            if (project.getName().equals("default")) {
                List<Element> myCols = project.getChildren("DigitalCollection");
                for (Element digitalCollection : myCols) {
                    if (digitalCollection.getAttribute("default") != null
                            && digitalCollection.getAttributeValue("default").equalsIgnoreCase("true")) {
                        digitalCollections.add(digitalCollection.getText());
                    }
                    defaultCollections.add(digitalCollection.getText());
                }
            } else {
                iterateOverAllProjects(project, process);
            }
        }

        if (possibleDigitalCollection.size() == 0) {
            possibleDigitalCollection = defaultCollections;
        }
    }

    private static Element getRoot() throws JDOMException, IOException {
        String filename = FilenameUtils.concat(ConfigCore.getKitodoConfigDirectory(),
                FileNames.DIGITAL_COLLECTIONS_FILE);
        if (!(new File(filename).exists())) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        // import file and determine root
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(filename));
        return doc.getRootElement();
    }

    @SuppressWarnings("unchecked")
    private static void iterateOverAllProjects(Element project, Process process) {
        // run through the projects
        List<Element> projectNames = project.getChildren("name");
        for (Element projectName : projectNames) {
            // all all collections to list
            if (projectName.getText().equalsIgnoreCase(process.getProject().getTitle())) {
                List<Element> myCols = project.getChildren("DigitalCollection");
                for (Element digitalCollection : myCols) {
                    if (digitalCollection.getAttribute("default") != null
                            && digitalCollection.getAttributeValue("default").equalsIgnoreCase("true")) {
                        digitalCollections.add(digitalCollection.getText());
                    }
                    possibleDigitalCollection.add(digitalCollection.getText());
                }
            }
        }
    }
}
