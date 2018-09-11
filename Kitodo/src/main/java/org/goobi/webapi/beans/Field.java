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

package org.goobi.webapi.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.goobi.webapi.beans.Label.KeyAttribute;
import org.kitodo.config.ConfigProjects;
import org.kitodo.data.database.beans.Project;

@XmlType(propOrder = {"required", "from", "option", "ughbinding", "docstruct" })
public class Field {

    @XmlAttribute
    private String key;
    @XmlElement
    private boolean required;
    @XmlElement
    private List<Label> option;
    @XmlElement(name = "source")
    private String from;
    @XmlElement
    private Boolean ughbinding;
    @XmlElement(name = "insertionLevel")
    private String docstruct;

    /**
     * Get field config for project.
     *
     * @param project
     *            object
     * @return list of fields
     */
    public static List<Field> getFieldConfigForProject(Project project) throws IOException {
        List<Field> fields = new ArrayList<>();

        ConfigProjects projectConfig = new ConfigProjects(project.getTitle());
        Integer numFields = projectConfig.getParamList("createNewProcess.itemlist.item").size();

        for (Integer field = 0; field < numFields; field++) {
            Field fieldConfig = new Field();
            String fieldRef = "createNewProcess.itemlist.item(" + field + ")";
            fieldConfig.key = projectConfig.getParamString(fieldRef);

            fieldConfig.from = projectConfig.getParamString(fieldRef + "[@from]");
            if (projectConfig.getParamBoolean(fieldRef + "[@ughbinding]")) {
                fieldConfig.ughbinding = Boolean.TRUE;
                fieldConfig.docstruct = projectConfig.getParamString(fieldRef + "[@docstruct]");
            } else {
                fieldConfig.ughbinding = Boolean.FALSE;
            }
            Integer selectEntries = projectConfig.getParamList(fieldRef + ".select").size();
            if (selectEntries > 0) {
                Map<String, String> selectConfig = new HashMap<>();
                for (Integer selectEntry = 0; selectEntry < selectEntries; selectEntry++) {
                    String key = projectConfig.getParamString(fieldRef + ".select(" + selectEntry + ")");
                    String value = projectConfig.getParamString(fieldRef + ".select(" + selectEntry + ")[@label]");
                    selectConfig.put(key, value);
                }
                fieldConfig.option = Label.toListOfLabels(selectConfig, KeyAttribute.LABEL);
            }
            fieldConfig.required = projectConfig.getParamBoolean(fieldRef + "[@required]");
            fields.add(fieldConfig);
        }
        return fields;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setOption(List<Label> option) {
        this.option = option;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setUghbinding(Boolean ughbinding) {
        this.ughbinding = ughbinding;
    }

    public void setDocstruct(String docstruct) {
        this.docstruct = docstruct;
    }

}
