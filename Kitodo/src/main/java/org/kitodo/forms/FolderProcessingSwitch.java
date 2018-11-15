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

package org.kitodo.forms;

import java.util.List;

import org.kitodo.data.database.beans.SubfolderType;
import org.kitodo.data.database.beans.Task;

/**
 * An encapsulation to access the content creation properties of a task from
 * JSF.
 *
 * <p>
 * The content creation properties of a task are stored internally differently
 * than they are displayed in the user interface. The user interface displays a
 * switch for each folder, for which a generator function has been configured.
 * Internally, however, the configuration of the activated generator functions
 * is stored in a list of the task in which all folders are contained, in which
 * the generator function is activated, that is, the switch is selected.
 * Conversely, the task knows nothing about which non-selected folders with
 * configured generator function there.
 */
public class FolderProcessingSwitch {
    /**
     * Folder represented by this generator switch.
     */
    private SubfolderType subfolderType;

    /**
     * Modifyable list containing enabled generators. This list is member of the
     * {@link Task} and saves the generator state when the task is saved.
     */
    private List<SubfolderType> contentFolders;

    /**
     * Creates a new generator for this task.
     *
     * @param subfolderType
     *            folder represented by this toggle switch
     * @param contentFolders
     *            modifiable list of enabled toggle switches
     */
    public FolderProcessingSwitch(SubfolderType subfolderType, List<SubfolderType> contentFolders) {
        this.subfolderType = subfolderType;
        this.contentFolders = contentFolders;
    }

    /**
     * Returns a label for the folder.
     *
     * @return a label for the folder
     */
    public String getLabel() {
        return subfolderType.toString();
    }

    /**
     * Returns the toggle switch value by looking into the list of folders to
     * generate.
     *
     * @return the value for the toggle switch
     */
    public boolean isValue() {
        return contentFolders.contains(subfolderType);
    }

    /**
     * Sets the boolean value by updating the list of folders to generate.
     *
     * @param value
     *            value to set
     */
    public void setValue(boolean value) {
        if (!value) {
            contentFolders.remove(subfolderType);
        } else if (!contentFolders.contains(subfolderType)) {
            contentFolders.add(subfolderType);
        }
    }
}
