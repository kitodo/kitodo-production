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

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;

import org.kitodo.data.database.beans.Task;

@PluginImplementation
public abstract class AbstractStepPlugin implements IStepPlugin {

    private static final Logger logger = Logger.getLogger(AbstractStepPlugin.class);

    protected String name = "Abstract Step Plugin";
    protected String version = "1.0";
    protected String description = "Abstract description for abstract step";

    protected Task myStep;
    protected String returnPath;

    @Override
    public void initialize(Task inStep, String inReturnPath) {
        this.myStep = inStep;
        this.returnPath = inReturnPath;
    }

    @Override
    public String getTitle() {
        return this.name + " v" + this.version;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public Task getStep() {
        return this.myStep;
    }

    @Override
    public String finish() {
        logger.debug("finish called");
        return this.returnPath;
    }

    @Override
    public String cancel() {
        logger.debug("cancel called");
        return this.returnPath;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

}
