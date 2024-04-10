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
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.data.interfaces.PropertyInterface;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.services.ServiceManager;

public class ProcessServiceTest {

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() {
        final ProcessInterface processInterface = new ProcessDTO();

        PropertyInterface firstPropertyInterface = new PropertyDTO();
        firstPropertyInterface.setId(1);
        firstPropertyInterface.setTitle("Korrektur notwendig");
        firstPropertyInterface.setValue("Fix it");
        firstPropertyInterface.setCreationDate(null);

        PropertyInterface secondPropertyInterface = new PropertyDTO();
        secondPropertyInterface.setId(2);
        secondPropertyInterface.setTitle("Korrektur notwendig");
        secondPropertyInterface.setValue("Fix it also");
        secondPropertyInterface.setCreationDate(null);

        PropertyInterface thirdPropertyInterface = new PropertyDTO();
        thirdPropertyInterface.setId(3);
        thirdPropertyInterface.setTitle("Other title");
        thirdPropertyInterface.setValue("Other value");
        thirdPropertyInterface.setCreationDate("2017-12-01");

        PropertyInterface fourthPropertyInterface = new PropertyDTO();
        fourthPropertyInterface.setId(4);
        fourthPropertyInterface.setTitle("Korrektur durchgef\u00FChrt");
        fourthPropertyInterface.setValue("Fixed second");
        fourthPropertyInterface.setCreationDate("2017-12-05");

        PropertyInterface fifthPropertyInterface = new PropertyDTO();
        fifthPropertyInterface.setId(5);
        fifthPropertyInterface.setTitle("Korrektur durchgef\u00FChrt");
        fifthPropertyInterface.setValue("Fixed first");
        fifthPropertyInterface.setCreationDate("2017-12-03");

        processInterface.getProperties().add(firstPropertyInterface);
        processInterface.getProperties().add(secondPropertyInterface);
        processInterface.getProperties().add(thirdPropertyInterface);
        processInterface.getProperties().add(fourthPropertyInterface);
        processInterface.getProperties().add(fifthPropertyInterface);

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
