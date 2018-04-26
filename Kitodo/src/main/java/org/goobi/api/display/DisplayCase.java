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

package org.goobi.api.display;

import java.util.ArrayList;

import org.goobi.api.display.enums.BindState;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDispayRules;
import org.kitodo.data.database.beans.Process;

public class DisplayCase {
    private DisplayType displayType;
    private ArrayList<Item> itemList = new ArrayList<>();
    private ConfigDispayRules configDisplay;
    private Process myProcess;
    private String metaName;
    private BindState myBindState;

    /**
     * gets items with current bind state.
     *
     * @param inProcess
     *            input process object
     * @param metaType
     *            String
     */

    public DisplayCase(Process inProcess, String metaType) {
        metaName = metaType;
        myProcess = inProcess;
        myBindState = Modes.getBindState();
        try {
            configDisplay = ConfigDispayRules.getInstance();
            if (configDisplay != null) {
                displayType = configDisplay.getElementTypeByName(myProcess.getProject().getTitle(),
                    myBindState.getTitle(), metaName);
                itemList = configDisplay.getItemsByNameAndType(myProcess.getProject().getTitle(),
                    myBindState.getTitle(), metaName, displayType);
            } else {
                // no ruleset file
                displayType = DisplayType.getByTitle("textarea");
                itemList.add(new Item(metaName, "", false));
            }
        } catch (RuntimeException e) {
            // incorrect ruleset file
            displayType = DisplayType.getByTitle("textarea");
            itemList.add(new Item(metaName, "", false));
        }
    }

    /**
     * gets items with given bind state.
     *
     * @param inProcess
     *            input process object
     * @param bind
     *            String
     * @param metaType
     *            String
     */
    public DisplayCase(Process inProcess, String bind, String metaType) {
        metaName = metaType;
        myProcess = inProcess;
        myBindState = Modes.getBindState();
        try {
            configDisplay = ConfigDispayRules.getInstance();
            if (configDisplay != null) {
                displayType = configDisplay.getElementTypeByName(myProcess.getProject().getTitle(), bind, metaName);
                itemList = configDisplay.getItemsByNameAndType(myProcess.getProject().getTitle(), bind, metaName,
                    displayType);
            } else {
                // no ruleset file
                displayType = DisplayType.getByTitle("textarea");
                itemList.add(new Item(metaName, "", false));
            }
        } catch (RuntimeException e) {
            // incorrect ruleset file
            displayType = DisplayType.getByTitle("textarea");
            itemList.add(new Item(metaName, "", false));
        }
    }

    /**
     * Get display type.
     *
     * @return current DisplayType
     */
    public DisplayType getDisplayType() {
        return displayType;
    }

    /**
     * Set item list.
     *
     * @param itemList
     *            ArrayList with items for metadata
     */
    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }

    /**
     * Get item list.
     *
     * @return ArrayList with items for metadata
     */

    public ArrayList<Item> getItemList() {
        return itemList;
    }
}
