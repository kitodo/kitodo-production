package org.kitodo.impl.docket;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.TemplateProperty;
import org.kitodo.api.docket.WorkpieceProperty;

import java.util.ArrayList;

public class DocketDataGenerator {

    public DocketData createDocketData(String processID, String signatur, String docType) {
        DocketData docketdata = new DocketData();
        docketdata.setCreationDate("01.01.2100");
        docketdata.setProcessId(processID);
        docketdata.setProcessName("ProcessTitle");
        docketdata.setProjectName("projectTitle");
        docketdata.setRulesetName("RulesetTitle");
        docketdata.setComment("A comment");

        ArrayList<TemplateProperty> templateProperties = new ArrayList<>();
        TemplateProperty propertyForDocket = new TemplateProperty();
        propertyForDocket.setTemplateId(12345);
        propertyForDocket.setTitle("Signatur");
        propertyForDocket.setValue(signatur);

        templateProperties.add(propertyForDocket);

        docketdata.setTemplateProperties(templateProperties);

        ArrayList<WorkpieceProperty> workpieceProperties = new ArrayList<>();
        WorkpieceProperty workpiecePropertyForDocket = new WorkpieceProperty();
        workpiecePropertyForDocket.setWorkpieceId(12345);
        workpiecePropertyForDocket.setTitle("docType");
        workpiecePropertyForDocket.setValue(docType);

        workpieceProperties.add(workpiecePropertyForDocket);

        docketdata.setWorkpieceProperties(workpieceProperties);

        ArrayList<org.kitodo.api.docket.ProcessProperty> processProperties = new ArrayList<>();
        org.kitodo.api.docket.ProcessProperty processPropertyForDocket = new org.kitodo.api.docket.ProcessProperty();
        processPropertyForDocket.setProcessId(12345);
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
