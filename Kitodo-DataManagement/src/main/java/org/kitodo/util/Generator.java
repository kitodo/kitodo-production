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

package org.kitodo.util;

import java.util.List;

import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Task;

/**
 * An encapsulation to access the generator properties of the task.
 */
public class Generator {
    /**
     * Folder represented by this generator switch.
     */
    private Folder folder;

    /**
     * Modifyable list containing enabled generators. This list is member of the
     * {@link Task} and saves the generator state when the task is saved.
     */
    private List<Folder> generateContents;

    /**
     * Creates a new generator for this task.
     *
     * @param folder
     *            folder represented by this toggle switch
     * @param generateContents
     *            modifyable list of enabled toggle switches
     */
    public Generator(Folder folder, List<Folder> generateContents) {
        this.folder = folder;
        this.generateContents = generateContents;
    }

    /**
     * Returns a label for the folder.
     *
     * @return a label for the folder
     */
    public String getLabel() {
        return folder.toString();
    }

    /**
     * Returns the toggle switch value by looking into the list of folders to
     * generate.
     *
     * @returns the value for the toggle switch
     */
    public boolean isValue() {
        return generateContents.contains(folder);
    }

    /**
     * Sets the boolean value by updating the list of folders to generate.
     *
     * @param value
     *            value to set
     */
    public void setValue(boolean value) {
        if (!value) {
            generateContents.remove(folder);
        } else if (!generateContents.contains(folder)) {
            generateContents.add(folder);
        }
    }
}
