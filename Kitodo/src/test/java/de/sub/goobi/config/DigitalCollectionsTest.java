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

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;

import static org.junit.Assert.assertEquals;

public class DigitalCollectionsTest {

    @BeforeClass
    public static void setUp() throws Exception {
        FileLoader.createDigitalCollectionsFile();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileLoader.deleteDigitalCollectionsFile();
    }

    @Test
    public void shouldGetPossibleDigitalCollectionForNonExistingProject() throws Exception {
        Project project = new Project();
        project.setTitle("NonExisting");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", 1, digitalCollections.size());

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", 3, possibleDigitalCollection.size());
    }

    @Test
    public void shouldGetPossibleDigitalCollectionForExistingProjectWithManyCollections() throws Exception {
        Project project = new Project();
        project.setTitle("Project A");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", 1, digitalCollections.size());

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", 5, possibleDigitalCollection.size());
    }

    @Test
    public void shouldGetPossibleDigitalCollectionForExistingProjectWithSingleCollection() throws Exception {
        Project project = new Project();
        project.setTitle("Project B");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", 1, digitalCollections.size());

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", 1, possibleDigitalCollection.size());
    }
}
