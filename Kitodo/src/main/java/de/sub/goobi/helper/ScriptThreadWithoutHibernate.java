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

package de.sub.goobi.helper;

import de.sub.goobi.helper.tasks.EmptyTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.kitodo.data.database.persistence.apache.MySQLHelper;
import org.kitodo.data.database.persistence.apache.StepManager;
import org.kitodo.data.database.persistence.apache.StepObject;

public class ScriptThreadWithoutHibernate extends EmptyTask {
    HelperSchritteWithoutHibernate hs = new HelperSchritteWithoutHibernate();
    private final StepObject step;
    private static final Logger logger = Logger.getLogger(ScriptThreadWithoutHibernate.class);

    public ScriptThreadWithoutHibernate(StepObject step) {
        super(getNameDetail(step));
        this.step = step;
        hs.setTask(this);
    }

    /**
     * The function getNameDetail() returns a human-readable name for this
     * thread.
     * 
     * @param step
     *            StepObject that the name depends on.
     * @return a name for this thread
     */
    private final static String getNameDetail(StepObject step) {
        String function = null;
        if (StepManager.loadScripts(step.getId()).size() > 0) {
            function = "executeAllScriptsForStep";
        } else if (step.isTypeExport()) {
            function = "executeDmsExport";
        } else if ((step.getStepPlugin() != null) && (step.getStepPlugin().length() > 0)) {
            function = "executeStepPlugin";
        }
        List<String> parameterList = new ArrayList<String>(1);
        try {
            parameterList.add(MySQLHelper.getProcessObjectForId(step.getProcessId()).getTitle());
        } catch (SQLException e) {
            parameterList.add(e.getMessage());
        }
        return function != null ? Helper.getTranslation(function, parameterList) : null;
    }

    /**
     * The clone constructor creates a new instance of this object. This is
     * necessary for Threads that have terminated in order to render to run them
     * again possible.
     * 
     * @param origin
     *            copy master to create a clone of
     */
    public ScriptThreadWithoutHibernate(ScriptThreadWithoutHibernate origin) {
        super(origin);
        step = origin.step;
        hs.setTask(this);
    }

    /**
     * Returns the display name of the task to show to the user.
     * 
     * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("ScriptThreadWithoutHibernate");
    }

    @Override
    public void run() {

        boolean automatic = this.step.isTypeAutomatic();
        if (logger.isDebugEnabled()) {
            logger.debug("step is automatic: " + automatic);
        }
        List<String> scriptPaths = StepManager.loadScripts(this.step.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("found " + scriptPaths.size() + " scripts");
        }
        if (scriptPaths.size() > 0) {
            this.hs.executeAllScriptsForStep(this.step, automatic);
        } else if (this.step.isTypeExport()) {
            this.hs.executeDmsExport(this.step, automatic);
        } else if ((this.step.getStepPlugin() != null) && (this.step.getStepPlugin().length() > 0)) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
            isp.initialize(step, "");
            if (isp.execute()) {
                hs.CloseStepObjectAutomatic(step);
            }
        }
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     * 
     * @return a not-yet-executed replacement of this thread
     * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
     */
    @Override
    public ScriptThreadWithoutHibernate replace() {
        return new ScriptThreadWithoutHibernate(this);
    }
}
