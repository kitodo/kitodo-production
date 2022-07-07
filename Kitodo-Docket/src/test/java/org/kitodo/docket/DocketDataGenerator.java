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

package org.kitodo.docket;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.Property;

public class DocketDataGenerator {

    public DocketData createDocketData(String processID, String signatur, String docType) {
        DocketData docketdata = new DocketData();
        docketdata.setCreationDate("01.01.2100");
        docketdata.setMetadataFile("src/test/resources/metadata/" + processID + "/meta.xml");
        docketdata.setProcessId(processID);
        docketdata.setProcessName("ProcessTitle");
        docketdata.setProjectName("projectTitle");
        docketdata.setRulesetName("RulesetTitle");
        docketdata.setComment("A comment");

        List<Property> templateProperties = new ArrayList<>();
        Property propertyForDocket = new Property();
        propertyForDocket.setId(12345);
        propertyForDocket.setTitle("Signatur");
        propertyForDocket.setValue(signatur);

        templateProperties.add(propertyForDocket);

        docketdata.setTemplateProperties(templateProperties);

        List<Property> workpieceProperties = new ArrayList<>();
        Property workpiecePropertyForDocket = new Property();
        workpiecePropertyForDocket.setId(12345);
        workpiecePropertyForDocket.setTitle("docType");
        workpiecePropertyForDocket.setValue(docType);

        workpieceProperties.add(workpiecePropertyForDocket);

        docketdata.setWorkpieceProperties(workpieceProperties);

        List<org.kitodo.api.docket.Property> processProperties = new ArrayList<>();
        org.kitodo.api.docket.Property processPropertyForDocket = new org.kitodo.api.docket.Property();
        processPropertyForDocket.setId(12345);
        processPropertyForDocket.setTitle("digitalCollection");
        processPropertyForDocket.setValue("Musik");

        processProperties.add(processPropertyForDocket);

        docketdata.setProcessProperties(processProperties);
        return docketdata;
    }

    public List<DocketData> createDocketData(List<String> processIds) {
        List<DocketData> docketData = new ArrayList<>();
        for (String processId : processIds) {
            docketData.add(createDocketData(processId, "AZ-234", "manuscript"));
        }
        return docketData;
    }

}
