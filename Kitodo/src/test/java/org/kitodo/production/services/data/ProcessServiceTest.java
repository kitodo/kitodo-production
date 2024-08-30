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
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.services.ServiceManager;

public class ProcessServiceTest {

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() throws ParseException {
        final ProcessInterface process = new ProcessDTO();

        PropertyInterface firstProperty = new PropertyDTO();
        firstProperty.setId(1);
        firstProperty.setTitle("Korrektur notwendig");
        firstProperty.setValue("Fix it");
        firstProperty.setCreationTime(null);

        PropertyInterface secondProperty = new PropertyDTO();
        secondProperty.setId(2);
        secondProperty.setTitle("Korrektur notwendig");
        secondProperty.setValue("Fix it also");
        secondProperty.setCreationTime(null);

        PropertyInterface thirdProperty = new PropertyDTO();
        thirdProperty.setId(3);
        thirdProperty.setTitle("Other title");
        thirdProperty.setValue("Other value");
        thirdProperty.setCreationTime("2017-12-01");

        PropertyInterface fourthProperty = new PropertyDTO();
        fourthProperty.setId(4);
        fourthProperty.setTitle("Korrektur durchgef\u00FChrt");
        fourthProperty.setValue("Fixed second");
        fourthProperty.setCreationTime("2017-12-05");

        PropertyInterface fifthProperty = new PropertyDTO();
        fifthProperty.setId(5);
        fifthProperty.setTitle("Korrektur durchgef\u00FChrt");
        fifthProperty.setValue("Fixed first");
        fifthProperty.setCreationTime("2017-12-03");

        @SuppressWarnings("unchecked")
        List<PropertyInterface> properties = (List<PropertyInterface>) process.getProperties();
        properties.add(firstProperty);
        properties.add(secondProperty);
        properties.add(thirdProperty);
        properties.add(fourthProperty);
        properties.add(fifthProperty);

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
