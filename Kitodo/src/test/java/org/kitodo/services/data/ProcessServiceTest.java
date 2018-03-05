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

package org.kitodo.services.data;

import java.util.List;

import org.junit.Test;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.PropertyDTO;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;

public class ProcessServiceTest {

    @Test
    public void shouldGetSortedCorrectionSolutionMessages() {
        ProcessDTO processDTO = new ProcessDTO();

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
        fourthPropertyDTO.setTitle("Korrektur durchgefuehrt");
        fourthPropertyDTO.setValue("Fixed second");
        fourthPropertyDTO.setCreationDate("2017-12-05");

        PropertyDTO fifthPropertyDTO = new PropertyDTO();
        fifthPropertyDTO.setId(5);
        fifthPropertyDTO.setTitle("Korrektur durchgefuehrt");
        fifthPropertyDTO.setValue("Fixed first");
        fifthPropertyDTO.setCreationDate("2017-12-03");

        processDTO.getProperties().add(firstPropertyDTO);
        processDTO.getProperties().add(secondPropertyDTO);
        processDTO.getProperties().add(thirdPropertyDTO);
        processDTO.getProperties().add(fourthPropertyDTO);
        processDTO.getProperties().add(fifthPropertyDTO);

        List<PropertyDTO> propertiesDTO = new ServiceManager().getProcessService().getSortedCorrectionSolutionMessages(processDTO);

        assertEquals("Size of sorted correction messages is not equal to given size!", 4, propertiesDTO.size());

        assertEquals("Sorted correction messages are not sorted correctly!", null, propertiesDTO.get(0).getCreationDate());

        assertEquals("Sorted correction messages are not sorted correctly!", "2017-12-05", propertiesDTO.get(3).getCreationDate());
    }
}
