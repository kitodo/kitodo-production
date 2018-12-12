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

package org.kitodo.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Project;

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
     * @param projectBean
     *            object
     */
    @SuppressWarnings("unchecked")
    public static void possibleDigitalCollectionsForProcess(Project projectBean) throws JDOMException, IOException {
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
                iterateOverAllProjects(project, projectBean);
            }
        }

        if (possibleDigitalCollection.isEmpty()) {
            possibleDigitalCollection = defaultCollections;
        }
    }

    private static Element getRoot() throws JDOMException, IOException {
        KitodoConfigFile file = KitodoConfigFile.DIGITAL_COLLECTIONS;
        if (!(file.exists())) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        // import file and determine root
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file.getFile());
        return doc.getRootElement();
    }

    @SuppressWarnings("unchecked")
    private static void iterateOverAllProjects(Element project, Project projectBean) {
        // run through all project name elements
        List<Element> projectNames = project.getChildren("name");
        for (Element projectName : projectNames) {
            // all all collections to list
            if (projectName.getText().equalsIgnoreCase(projectBean.getTitle())) {
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
