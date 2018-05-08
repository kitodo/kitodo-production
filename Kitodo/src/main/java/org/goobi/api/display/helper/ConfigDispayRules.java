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

package org.goobi.api.display.helper;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;

public final class ConfigDispayRules {

    private static ConfigDispayRules instance = new ConfigDispayRules();
    private static XMLConfiguration config;
    private static String configPfad;
    private final Helper helper = new Helper();
    private final HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>>> allValues = new HashMap<>();

    /**
     * reads given xml file into XMLConfiguration.
     */

    private ConfigDispayRules() {
        configPfad = ConfigCore.getKitodoConfigDirectory() + "kitodo_metadataDisplayRules.xml";
        try {
            config = new XMLConfiguration(configPfad);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            getDisplayItems();
        } catch (ConfigurationException e) {
            /*
             * no configuration file found, default configuration (textarea)
             * will be used, nothing to do here
             */
        }
    }

    public static ConfigDispayRules getInstance() {
        return instance;
    }

    /**
     * creates hierarchical HashMap with values for each element of given data.
     */
    private synchronized void getDisplayItems() {
        if (this.allValues.isEmpty() && config != null) {
            int countRuleSet = config.getMaxIndex("ruleSet");
            for (int i = 0; i <= countRuleSet; i++) {
                int projectContext = config.getMaxIndex("ruleSet(" + i + ").context");
                for (int j = 0; j <= projectContext; j++) {
                    HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = new HashMap<>();
                    HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>> bindstate = new HashMap<>();
                    String projectName = config.getString("ruleSet(" + i + ").context(" + j + ")[@projectName]");
                    String bind = config.getString("ruleSet(" + i + ").context(" + j + ").bind");
                    int anotherCountSelect = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").anotherSelect");
                    int countSelect = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").select");
                    int countTextArea = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").textarea");
                    int countInput = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").input");
                    int countReadOnly = config.getMaxIndex("ruleSet(" + i + ").context(" + j + ").readonly");
                    HashMap<String, ArrayList<Item>> anotherSelect = new HashMap<>();
                    HashMap<String, ArrayList<Item>> select = new HashMap<>();
                    HashMap<String, ArrayList<Item>> input = new HashMap<>();
                    HashMap<String, ArrayList<Item>> textarea = new HashMap<>();
                    HashMap<String, ArrayList<Item>> readonly = new HashMap<>();
                    for (int k = 0; k <= anotherCountSelect; k++) {
                        String elementName = config
                                .getString("ruleSet(" + i + ").context(" + j + ").anotherSelect(" + k + ")[@tns:ref]");
                        ArrayList<Item> items = getSelectOneByElementName(projectName, bind, elementName);
                        anotherSelect.put(elementName, items);
                    }
                    for (int k = 0; k <= countSelect; k++) {
                        String elementName = config
                                .getString("ruleSet(" + i + ").context(" + j + ").select(" + k + ")[@tns:ref]");
                        ArrayList<Item> items = getSelectByElementName(projectName, bind, elementName);
                        select.put(elementName, items);
                    }
                    for (int k = 0; k <= countTextArea; k++) {
                        String elementName = config
                                .getString("ruleSet(" + i + ").context(" + j + ").textarea(" + k + ")[@tns:ref]");
                        ArrayList<Item> items = getTextareaByElementName(projectName, bind, elementName);
                        textarea.put(elementName, items);
                    }
                    for (int k = 0; k <= countInput; k++) {
                        String elementName = config
                                .getString("ruleSet(" + i + ").context(" + j + ").input(" + k + ")[@tns:ref]");
                        ArrayList<Item> items = getInputByElementName(projectName, bind, elementName);
                        input.put(elementName, items);
                    }
                    for (int k = 0; k <= countReadOnly; k++) {
                        String elementName = config
                                .getString("ruleSet(" + i + ").context(" + j + ").readonly(" + k + ")[@tns:ref]");
                        ArrayList<Item> items = getReadOnlyByElementName(projectName, bind, elementName);
                        readonly.put(elementName, items);
                    }

                    itemsByType.put("anotherSelect", anotherSelect);
                    itemsByType.put("select", select);
                    itemsByType.put("input", input);
                    itemsByType.put("textarea", textarea);
                    itemsByType.put("readonly", readonly);
                    if (this.allValues.get(projectName) == null) {
                        bindstate.put(bind, itemsByType);
                        this.allValues.put(projectName, bindstate);
                    } else {
                        bindstate = this.allValues.get(projectName);
                        bindstate.put(bind, itemsByType);
                    }
                }
            }
        }

    }

    /**
     *
     * @param project
     *            name of project as String
     * @param bind
     *            create or edit
     * @param elementName
     *            name of the select1 element
     * @return ArrayList with all items and its values of given select1 element.
     */

    private ArrayList<Item> getSelectOneByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex("ruleSet.context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("ruleSet.context(" + i + ")[@projectName]");
            String myBind = config.getString("ruleSet.context(" + i + ").bind");
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = config.getMaxIndex("ruleSet.context(" + i + ").select1");
                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("ruleSet.context(" + i + ").select1(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        int item = config.getMaxIndex("ruleSet.context(" + i + ").select1(" + j + ").item");
                        for (int k = 0; k <= item; k++) {
                            Item myItem = new Item(
                                    config.getString(
                                            "ruleSet.context(" + i + ").select1(" + j + ").item(" + k + ").label"),
                                    // the displayed value
                                    config.getString(
                                            // the internal value, which will be
                                            // taken if label is selected
                                            "ruleSet.context(" + i + ").select1(" + j + ").item(" + k + ").value"),
                                    config.getBoolean(
                                            // indicates wheter given item is
                                            // preselected or not
                                            "ruleSet.context(" + i + ").select1(" + j + ").item(" + k
                                                    + ")[@tns:selected]"));
                            listOfItems.add(myItem);
                        }
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     *
     * @param project
     *            name of project as String
     * @param bind
     *            create or edit
     * @param elementName
     *            name of the select element
     * @return ArrayList with all items and its values of given select1 element.
     */
    private ArrayList<Item> getSelectByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex("ruleSet.context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("ruleSet.context(" + i + ")[@projectName]");
            String myBind = config.getString("ruleSet.context(" + i + ").bind");
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = config.getMaxIndex("ruleSet.context(" + i + ").select");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("ruleSet.context(" + i + ").select(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        int item = config.getMaxIndex("ruleSet.context(" + i + ").select(" + j + ").item");

                        for (int k = 0; k <= item; k++) {
                            Item myItem = new Item(
                                    config.getString(
                                            // the displayed value
                                            "ruleSet.context(" + i + ").select(" + j + ").item(" + k + ").label"),
                                    config.getString(
                                            // the internal value, which will be
                                            // taken if label is selected
                                            "ruleSet.context(" + i + ").select(" + j + ").item(" + k + ").value"),
                                    config.getBoolean(
                                            // indicated wheter given item is
                                            // preselected or not
                                            "ruleSet.context(" + i + ").select(" + j + ").item(" + k
                                                    + ")[@tns:selected]"));
                            listOfItems.add(myItem);
                        }
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     *
     * @param project
     *            name of project as String
     * @param bind
     *            create or edit
     * @param elementName
     *            name of the input element
     * @return item of given input element.
     */

    private ArrayList<Item> getInputByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex("ruleSet.context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("ruleSet.context(" + i + ")[@projectName]");
            String myBind = config.getString("ruleSet.context(" + i + ").bind");
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = config.getMaxIndex("ruleSet.context(" + i + ").input");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("ruleSet.context(" + i + ").input(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        // the displayed value
                        Item myItem = new Item(config.getString("ruleSet.context(" + i + ").input(" + j + ").label"),
                                config.getString("ruleSet.context(" + i + ").input(" + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * @param project
     *            name of project as String
     * @param bind
     *            create or edit
     * @param elementName
     *            name of the textarea element
     * @return item of given textarea element.
     */

    private ArrayList<Item> getTextareaByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex("ruleSet.context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("ruleSet.context(" + i + ")[@projectName]");
            String myBind = config.getString("ruleSet.context(" + i + ").bind");
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = config.getMaxIndex("ruleSet.context(" + i + ").textarea");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("ruleSet.context(" + i + ").textarea(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        // the displayed value
                        Item myItem = new Item(config.getString("ruleSet.context(" + i + ").textarea(" + j + ").label"),
                                config.getString("ruleSet.context(" + i + ").textarea(" + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    private ArrayList<Item> getReadOnlyByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex("ruleSet.context");
        for (int i = 0; i <= count; i++) {
            String myProject = config.getString("ruleSet.context(" + i + ")[@projectName]");
            String myBind = config.getString("ruleSet.context(" + i + ").bind");
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = config.getMaxIndex("ruleSet.context(" + i + ").readonly");

                for (int j = 0; j <= type; j++) {
                    String myElementName = config.getString("ruleSet.context(" + i + ").readonly(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        // the displayed value
                        Item myItem = new Item(config.getString("ruleSet.context(" + i + ").readonly(" + j + ").label"),
                                config.getString("ruleSet.context(" + i + ").readonly(" + j + ").label"), false);
                        listOfItems.add(myItem);
                    }
                }
            }
        }
        return listOfItems;
    }

    /**
     * Get element type by name.
     *
     * @param myproject
     *            project of element
     * @param mybind
     *            create or edit
     * @param myelementName
     *            name of element
     * @return type of element
     */

    public DisplayType getElementTypeByName(String myproject, String mybind, String myelementName) {
        synchronized (this.allValues) {
            if (this.allValues.isEmpty() && config != null) {
                getDisplayItems();
            } else if (config == null) {

                return DisplayType.textarea;
            }
            HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>> bind = this.allValues.get(myproject);
            if (bind == null) {
                return DisplayType.textarea;
            }
            HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = bind.get(mybind);
            if (itemsByType == null) {

                return DisplayType.textarea;
            }
            Set<String> itemTypes = itemsByType.keySet();
            for (String type : itemTypes) {
                HashMap<String, ArrayList<Item>> typeList = itemsByType.get(type);
                Set<String> names = typeList.keySet();
                for (String name : names) {
                    if (name.equals(myelementName)) {

                        return DisplayType.getByTitle(type);
                    }
                }
            }
        }
        return DisplayType.textarea;
    }

    /**
     * Get items by name and type.
     *
     * @param myproject
     *            name of project as String
     * @param mybind
     *            create or edit
     * @param myelementName
     *            name of the element
     * @param mydisplayType
     *            type of the element
     * @return ArrayList with all values of given element
     */

    public List<Item> getItemsByNameAndType(String myproject, String mybind, String myelementName, DisplayType mydisplayType) {
        List<Item> values = new ArrayList<>();
        synchronized (this.allValues) {
            if (this.allValues.isEmpty() && config != null) {
                getDisplayItems();
            } else if (config == null) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
            HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>> bind = this.allValues.get(myproject);
            if (bind.isEmpty()) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
            HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = bind.get(mybind);
            if (itemsByType.isEmpty()) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
            HashMap<String, ArrayList<Item>> typeList = itemsByType.get(mydisplayType.getTitle());
            if (typeList.isEmpty()) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
            values = typeList.get(myelementName);
            if (values.isEmpty()) {
                values.add(new Item(myelementName, "", false));
                return values;
            }
        }
        return values;
    }

    /**
     * refreshes the hierarchical HashMap with values from xml file. If HashMap
     * is used by another thread, the function will wait until.
     */
    public void refresh() {
        if (config != null && !this.allValues.isEmpty()) {
            synchronized (this.allValues) {
                this.allValues.clear();
                getDisplayItems();
            }
        }
    }
}
