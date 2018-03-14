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

import java.util.List;

import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;

import static org.junit.Assert.assertEquals;

public class DigitalCollectionsIT {

    @Test
    public void shouldGetPossibleDigitalCollectionForNonExistingProject() throws Exception {
        Project project = new Project();
        project.setTitle("NonExisting");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", digitalCollections.size(), 1);

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", possibleDigitalCollection.size(), 3);
    }

    @Test
    public void shouldGetPossibleDigitalCollectionForExistingProjectWithManyCollections() throws Exception {
        Project project = new Project();
        project.setTitle("Project A");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", digitalCollections.size(), 1);

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", possibleDigitalCollection.size(), 5);
    }

    @Test
    public void shouldGetPossibleDigitalCollectionForExistingProjectWithSingleCollection() throws Exception {
        Project project = new Project();
        project.setTitle("Project B");

        Process process = new Process();
        process.setProject(project);

        DigitalCollections.possibleDigitalCollectionsForProcess(process);

        List<String> digitalCollections = DigitalCollections.getDigitalCollections();
        assertEquals("Incorrect amount of digital collections!", digitalCollections.size(), 1);

        List<String> possibleDigitalCollection = DigitalCollections.getPossibleDigitalCollection();
        assertEquals("Incorrect amount of possible digital collections!", possibleDigitalCollection.size(), 1);
    }
}
