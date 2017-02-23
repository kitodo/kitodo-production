package org.kitodo.impl.docket;

import java.util.ArrayList;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.Property;

public class DocketDataGenerator {

    public DocketData createDocketData(String processID, String signatur, String docType) {
        DocketData docketdata = new DocketData();
        docketdata.setCreationDate("01.01.2100");
        docketdata.setProcessId(processID);
        docketdata.setProcessName("ProcessTitle");
        docketdata.setProjectName("projectTitle");
        docketdata.setRulesetName("RulesetTitle");
        docketdata.setComment("A comment");

        ArrayList<Property> templateProperties = new ArrayList<>();
        Property propertyForDocket = new Property();
        propertyForDocket.setId(12345);
        propertyForDocket.setTitle("Signatur");
        propertyForDocket.setValue(signatur);

        templateProperties.add(propertyForDocket);

        docketdata.setTemplateProperties(templateProperties);

        ArrayList<Property> workpieceProperties = new ArrayList<>();
        Property workpiecePropertyForDocket = new Property();
        workpiecePropertyForDocket.setId(12345);
        workpiecePropertyForDocket.setTitle("docType");
        workpiecePropertyForDocket.setValue(docType);

        workpieceProperties.add(workpiecePropertyForDocket);

        docketdata.setWorkpieceProperties(workpieceProperties);

        ArrayList<org.kitodo.api.docket.Property> processProperties = new ArrayList<>();
        org.kitodo.api.docket.Property processPropertyForDocket = new org.kitodo.api.docket.Property();
        processPropertyForDocket.setId(12345);
        processPropertyForDocket.setTitle("digitalCollection");
        processPropertyForDocket.setValue("Musik");

        processProperties.add(processPropertyForDocket);

        docketdata.setProcessProperties(processProperties);
        return docketdata;
    }

    public ArrayList<DocketData> createDocketData(ArrayList<String> processIds) {
        ArrayList<DocketData> docketDatas = new ArrayList<>();
        for (String processId : processIds) {
            docketDatas.add(createDocketData(processId, "AZ-234", "manuscript"));
        }
        return docketDatas;
    }

}
