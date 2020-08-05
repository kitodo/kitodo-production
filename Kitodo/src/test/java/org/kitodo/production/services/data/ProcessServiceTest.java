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

package org.kitodo.production.services.data;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.services.ServiceManager;

public class ProcessServiceTest {

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() {
        final ProcessDTO processDTO = new ProcessDTO();

        PropertyDTO firstPropertyDTO = new PropertyDTO();
        firstPropertyDTO.setId(1);
        firstPropertyDTO.setTitle("Korrektur notwendig");
        firstPropertyDTO.setValue("Fix it");
        firstPropertyDTO.setCreationDate(null);

        PropertyDTO secondPropertyDTO = new PropertyDTO();
        secondPropertyDTO.setId(2);
        secondPropertyDTO.setTitle("Korrektur notwendig");
        secondPropertyDTO.setValue("Fix it also");
        secondPropertyDTO.setCreationDate(null);

        PropertyDTO thirdPropertyDTO = new PropertyDTO();
        thirdPropertyDTO.setId(3);
        thirdPropertyDTO.setTitle("Other title");
        thirdPropertyDTO.setValue("Other value");
        thirdPropertyDTO.setCreationDate("2017-12-01");

        PropertyDTO fourthPropertyDTO = new PropertyDTO();
        fourthPropertyDTO.setId(4);
        fourthPropertyDTO.setTitle("Korrektur durchgef\u00FChrt");
        fourthPropertyDTO.setValue("Fixed second");
        fourthPropertyDTO.setCreationDate("2017-12-05");

        PropertyDTO fifthPropertyDTO = new PropertyDTO();
        fifthPropertyDTO.setId(5);
        fifthPropertyDTO.setTitle("Korrektur durchgef\u00FChrt");
        fifthPropertyDTO.setValue("Fixed first");
        fifthPropertyDTO.setCreationDate("2017-12-03");

        processDTO.getProperties().add(firstPropertyDTO);
        processDTO.getProperties().add(secondPropertyDTO);
        processDTO.getProperties().add(thirdPropertyDTO);
        processDTO.getProperties().add(fourthPropertyDTO);
        processDTO.getProperties().add(fifthPropertyDTO);

    }

    @Test
    public void testGetMetadataFileUri() {
        Process process = new Process();
        process.setProcessBaseUri(URI.create("relative/path/no/ending/slash"));
        URI uri = ServiceManager.getProcessService().getMetadataFileUri(process);
        Assert.assertEquals(URI.create("relative/path/no/ending/slash/meta.xml"), uri);

        process.setProcessBaseUri(URI.create("relative/path/with/ending/slash/"));
        uri = ServiceManager.getProcessService().getMetadataFileUri(process);
        Assert.assertEquals(URI.create("relative/path/with/ending/slash/meta.xml"), uri);
    }

    @Test
    public void testGetProcessURI() {
        Process process = new Process();
        process.setId(42);
        URI uri = ServiceManager.getProcessService().getProcessURI(process);
        Assert.assertEquals(URI.create("database://?process.id=42"), uri);
    }

    @Test
    public void testProcessIdFromUri() {
        URI uri = URI.create("database://?process.id=42");
        int processId = ServiceManager.getProcessService().processIdFromUri(uri);
        Assert.assertEquals(42, processId);
    }
}
