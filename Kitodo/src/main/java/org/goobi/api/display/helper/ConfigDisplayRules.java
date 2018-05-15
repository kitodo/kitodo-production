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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;

public final class ConfigDisplayRules {

    private static ConfigDisplayRules instance = new ConfigDisplayRules();
    private static XMLConfiguration config;
    private final HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>>> allValues = new HashMap<>();
    private static final String RULESET = "ruleSet";
    private static final String CONTEXT = "context";
    private static final String RULESET_CONTEXT = RULESET + "." + CONTEXT;

    /**
     * Reads given xml file into XMLConfiguration.
     */
    private ConfigDisplayRules() {
        String configPath = ConfigCore.getKitodoConfigDirectory() + "kitodo_metadataDisplayRules.xml";
        try {
            config = new XMLConfiguration(configPath);
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            getDisplayItems();
        } catch (ConfigurationException e) {
            /*
             * no configuration file found, default configuration (textarea) will be used,
             * nothing to do here
             */
        }
    }

    public static ConfigDisplayRules getInstance() {
        return instance;
    }

    /**
     * Creates hierarchical HashMap with values for each element of given data.
     */
    private synchronized void getDisplayItems() {
        if (this.allValues.isEmpty() && config != null) {
            int countRuleSet = config.getMaxIndex(RULESET);
            for (int i = 0; i <= countRuleSet; i++) {
                int projectContext = config.getMaxIndex(RULESET + "(" + i + ")." + CONTEXT);
                for (int j = 0; j <= projectContext; j++) {
                    HashMap<String, HashMap<String, ArrayList<Item>>> itemsByType = new HashMap<>();
                    HashMap<String, HashMap<String, HashMap<String, ArrayList<Item>>>> bindState = new HashMap<>();
                    String projectName = config.getString(RULESET + "(" + i + ")." + CONTEXT + "(" + j + ")[@projectName]");
                    String bind = config.getString(RULESET + "(" + i + ")." + CONTEXT + "(" + j + ").bind");

                    itemsByType.put("anotherSelect", getAnotherSelectItems(i, j, projectName, bind));
                    itemsByType.put("select", getSelectItems(i, j, projectName, bind));
                    itemsByType.put("input", getInputItems(i, j, projectName, bind));
                    itemsByType.put("textarea", getTextAreaItems(i, j, projectName, bind));
                    itemsByType.put("readonly", getReadOnlyItems(i, j, projectName, bind));
                    if (this.allValues.get(projectName) == null) {
                        bindState.put(bind, itemsByType);
                        this.allValues.put(projectName, bindState);
                    } else {
                        bindState = this.allValues.get(projectName);
                        bindState.put(bind, itemsByType);
                    }
                }
            }
        }
    }

    private HashMap<String, ArrayList<Item>> getAnotherSelectItems(int i, int j, String projectName, String bind) {
        int countAnotherSelect = getAmountOfElements(i, j, "anotherSelect");
        HashMap<String, ArrayList<Item>> anotherSelect = new HashMap<>();

        for (int k = 0; k <= countAnotherSelect; k++) {
            String elementName = getElementName(i, j, k, "anotherSelect");
            ArrayList<Item> items = getSelectOneByElementName(projectName, bind, elementName);
            anotherSelect.put(elementName, items);
        }

        return anotherSelect;
    }

    private HashMap<String, ArrayList<Item>> getSelectItems(int i, int j, String projectName, String bind) {
        int countSelect = getAmountOfElements(i, j, "select");
        HashMap<String, ArrayList<Item>> select = new HashMap<>();

        for (int k = 0; k <= countSelect; k++) {
            String elementName = getElementName(i, j, k, "select");
            ArrayList<Item> items = getSelectByElementName(projectName, bind, elementName);
            select.put(elementName, items);
        }

        return select;
    }

    private HashMap<String, ArrayList<Item>> getInputItems(int i, int j, String projectName, String bind) {
        int countInput = getAmountOfElements(i, j, "input");
        HashMap<String, ArrayList<Item>> input = new HashMap<>();

        for (int k = 0; k <= countInput; k++) {
            String elementName = getElementName(i, j, k, "input");
            ArrayList<Item> items = getInputByElementName(projectName, bind, elementName);
            input.put(elementName, items);
        }

        return input;
    }

    private HashMap<String, ArrayList<Item>> getTextAreaItems(int i, int j, String projectName, String bind) {
        int countTextArea = getAmountOfElements(i, j, "textarea");
        HashMap<String, ArrayList<Item>> textarea = new HashMap<>();

        for (int k = 0; k <= countTextArea; k++) {
            String elementName = getElementName(i, j, k, "textarea");
            ArrayList<Item> items = getTextareaByElementName(projectName, bind, elementName);
            textarea.put(elementName, items);
        }

        return textarea;
    }

    private HashMap<String, ArrayList<Item>> getReadOnlyItems(int i, int j, String projectName, String bind) {
        int countReadOnly = getAmountOfElements(i, j, "readonly");
        HashMap<String, ArrayList<Item>> readOnly = new HashMap<>();

        for (int k = 0; k <= countReadOnly; k++) {
            String elementName = getElementName(i, j, k, "readonly");
            ArrayList<Item> items = getReadOnlyByElementName(projectName, bind, elementName);
            readOnly.put(elementName, items);
        }
        return readOnly;
    }

    private int getAmountOfElements(int i, int j, String label) {
        return config.getMaxIndex(RULESET + "(" + i + ")." + CONTEXT + "(" + j + ")." + label);
    }

    private String getElementName(int i, int j, int k, String label) {
        return config.getString(RULESET + "(" + i + ")." + CONTEXT + "(" + j + ")." + label + "(" + k + ")[@tns:ref]");
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
        return getSelectByElementName(project, bind, elementName, "select1");
    }

    /**
     *
     * @param project
     *            name of project as String
     * @param bind
     *            create or edit
     * @param elementName
     *            name of the select element
     * @return ArrayList with all items and its values of given select element.
     */
    private ArrayList<Item> getSelectByElementName(String project, String bind, String elementName) {
        return getSelectByElementName(project, bind, elementName, "select");
    }

    private ArrayList<Item> getSelectByElementName(String project, String bind, String elementName, String select) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex(RULESET_CONTEXT);
        for (int i = 0; i <= count; i++) {
            String myProject = getProject(i);
            String myBind = getBind(i);
            if (myProject.equals(project) && myBind.equals(bind)) {
                int type = getAmountOfElements(i, select);
                for (int j = 0; j <= type; j++) {
                    String myElementName = config
                            .getString(RULESET_CONTEXT + "(" + i + ")." + select + "(" + j + ")[@tns:ref]");
                    if (myElementName.equals(elementName)) {
                        int item = config.getMaxIndex(RULESET_CONTEXT + "(" + i + ")." + select + "(" + j + ").item");
                        for (int k = 0; k <= item; k++) {
                            Item myItem = new Item(
                                    // the displayed value
                                    config.getString(RULESET_CONTEXT + "(" + i + ")." + select + "(" + j + ").item(" + k
                                            + ").label"),
                                    // the internal value, which will be taken if label is selected
                                    config.getString(RULESET_CONTEXT + "(" + i + ")." + select + "(" + j + ").item(" + k
                                            + ").value"),
                                    // indicated wheter given item is preselected or not
                                    config.getBoolean(RULESET_CONTEXT + "(" + i + ")." + select + "(" + j + ").item("
                                            + k + ")[@tns:selected]"));
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
        int count = config.getMaxIndex(RULESET_CONTEXT);
        for (int i = 0; i <= count; i++) {
            String myProject = getProject(i);
            String myBind = getBind(i);
            if (myProject.equals(project) && myBind.equals(bind)) {
                listOfItems = getListOfItems(i, elementName, "input");
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
        int count = config.getMaxIndex(RULESET_CONTEXT);
        for (int i = 0; i <= count; i++) {
            String myProject = getProject(i);
            String myBind = getBind(i);
            if (myProject.equals(project) && myBind.equals(bind)) {
                listOfItems = getListOfItems(i, elementName, "textarea");
            }
        }
        return listOfItems;
    }

    private ArrayList<Item> getReadOnlyByElementName(String project, String bind, String elementName) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int count = config.getMaxIndex(RULESET_CONTEXT);
        for (int i = 0; i <= count; i++) {
            String myProject = getProject(i);
            String myBind = getBind(i);
            if (myProject.equals(project) && myBind.equals(bind)) {
                listOfItems = getListOfItems(i, elementName, "readonly");
            }
        }
        return listOfItems;
    }

    private ArrayList<Item> getListOfItems(int i, String elementName, String label) {
        ArrayList<Item> listOfItems = new ArrayList<>();
        int type = getAmountOfElements(i, label);
        for (int j = 0; j <= type; j++) {
            String readElementName = config
                    .getString(RULESET_CONTEXT + "(" + i + ")." + label + "(" + j + ")[@tns:ref]");
            if (readElementName.equals(elementName)) {
                // the displayed value
                // TODO: here two times label is read - why?
                Item item = new Item(config.getString(RULESET_CONTEXT + "(" + i + ")." + label + "(" + j + ").label"),
                        config.getString(RULESET_CONTEXT + "(" + i + ")." + label + "(" + j + ").label"), false);
                listOfItems.add(item);
            }
        }
        return listOfItems;
    }

    private String getProject(int i) {
        return config.getString(RULESET_CONTEXT + "(" + i + ")[@projectName]");
    }

    private String getBind(int i) {
        return config.getString(RULESET_CONTEXT + "(" + i + ").bind");
    }

    private int getAmountOfElements(int i, String label) {
        return config.getMaxIndex(RULESET_CONTEXT + "(" + i + ")." + label);
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

    public List<Item> getItemsByNameAndType(String myproject, String mybind, String myelementName,
            DisplayType mydisplayType) {
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
     * refreshes the hierarchical HashMap with values from xml file. If HashMap is
     * used by another thread, the function will wait until.
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
