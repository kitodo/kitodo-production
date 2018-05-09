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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.kitodo.exceptions.UnreachableCodeException;

/**
 * The Label class provides serialization for Map &lt;String,String&gt; objects
 * where keys are language identifiers (examples include “en”, “de”, …) and
 * values are texts in the respective language. This is necessary because Maps
 * unfortunately do not natively serialize to XML.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Label {
    public enum KeyAttribute {
        LABEL,
        LANGUAGE
    }

    @XmlAttribute(name = "label")
    public String label;

    @XmlAttribute(name = "lang")
    public String language;

    @XmlValue
    public String value;

    /**
     * Default constructor is required to be explicitly coded because copy
     * constructor is given. Java only provides an implicit default constructor
     * as long as no other constructors are given.
     */
    public Label() {
        // there is nothing to do
    }

    /**
     * Copy Constructor to instantiate an already populated Label.
     *
     * @param toCopy
     *            Field to create a copy from
     */
    public Label(Label toCopy) {
        this.label = toCopy.label;
        this.language = toCopy.language;
        this.value = toCopy.value;
    }

    /**
     * To list of labels.
     *
     * @param data
     *            Map of Strings
     * @param keyAttribute
     *            KeyAttribute object
     * @return list of Label objects
     */
    public static List<Label> toListOfLabels(Map<String, String> data, KeyAttribute keyAttribute) {
        List<Label> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            Label label = new Label();
            switch (keyAttribute) {
                case LABEL:
                    label.label = key;
                    break;
                case LANGUAGE:
                    label.language = key;
                    break;
                default:
                    throw new UnreachableCodeException();
            }
            label.value = entry.getValue();
            result.add(label);
        }
        return result;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
