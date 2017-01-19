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

package org.goobi.production.plugin.interfaces;

import java.util.HashMap;

import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.StepReturnValue;

import org.kitodo.data.database.beans.Schritt;
import de.sub.goobi.persistence.apache.StepObject;

public interface IStepPlugin extends IPlugin {

	public void initialize(Schritt step, String returnPath);

	public void initialize(StepObject stepobject, String returnPath);

	public boolean execute();

	public String cancel();

	public String finish();

	public HashMap<String, StepReturnValue> validate();

	public Schritt getStep();

	public PluginGuiType getPluginGuiType();

}
