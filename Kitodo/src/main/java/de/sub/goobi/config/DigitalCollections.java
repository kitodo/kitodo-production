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
    private static final String DEFAULT = "default";

    /**
     * Private constructor to hide the implicit public one.
     */
    private DigitalCollections() {

    }

    /**
     * Get list of digital collections.
     * 
     * @return list of digital collections as String
     */
    public static List<String> getDigitalCollections() {
        if (digitalCollections == null) {
            digitalCollections = new ArrayList<>();
        }
        return digitalCollections;
    }

    /**
     * Get list of possible digital collections.
     * 
     * @return list of possible digital collections as String
     */
    public static List<String> getPossibleDigitalCollection() {
        if (possibleDigitalCollection == null) {
            possibleDigitalCollection = new ArrayList<>();
        }
        return DigitalCollections.possibleDigitalCollection;
    }

    /**
     * Prepare digital collections and possible digital collections for process.
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

        // run through all project elements
        List<Element> projects = root.getChildren();
        for (Element project : projects) {
            // collect default collections
            if (project.getName().equals(DEFAULT)) {
                List<Element> myCols = project.getChildren("DigitalCollection");
                for (Element digitalCollection : myCols) {
                    if (digitalCollection.getAttribute(DEFAULT) != null
                            && digitalCollection.getAttributeValue(DEFAULT).equalsIgnoreCase("true")) {
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
        String fileName = FilenameUtils.concat(ConfigCore.getKitodoConfigDirectory(),
            FileNames.DIGITAL_COLLECTIONS_FILE);
        if (!(new File(fileName).exists())) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        // import file and determine root
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new File(fileName));
        return doc.getRootElement();
    }

    @SuppressWarnings("unchecked")
    private static void iterateOverAllProjects(Element project, Process process) {
        // run through all project name elements
        List<Element> projectNames = project.getChildren("name");
        for (Element projectName : projectNames) {
            // all all collections to list
            if (projectName.getText().equalsIgnoreCase(process.getProject().getTitle())) {
                List<Element> myCols = project.getChildren("DigitalCollection");
                for (Element digitalCollection : myCols) {
                    if (digitalCollection.getAttribute(DEFAULT) != null
                            && digitalCollection.getAttributeValue(DEFAULT).equalsIgnoreCase("true")) {
                        digitalCollections.add(digitalCollection.getText());
                    }
                    possibleDigitalCollection.add(digitalCollection.getText());
                }
            }
        }
    }
}
