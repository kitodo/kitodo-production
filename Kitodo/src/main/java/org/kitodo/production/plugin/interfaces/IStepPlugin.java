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

package org.kitodo.production.plugin.interfaces;

import java.util.HashMap;

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.persistence.apache.StepObject;
import org.kitodo.production.enums.PluginGuiType;
import org.kitodo.production.enums.StepReturnValue;

public interface IStepPlugin extends IPlugin {

    public void initialize(Task step, String returnPath);

    public void initialize(StepObject stepobject, String returnPath);

    public boolean execute();

    public String cancel();

    public String finish();

    public HashMap<String, StepReturnValue> validate();

    public Task getStep();

    public PluginGuiType getPluginGuiType();

}
