package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.tasks.EmptyTask;
import de.sub.goobi.helper.tasks.INameableTask;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.StepManager;
import de.sub.goobi.persistence.apache.StepObject;

public class ScriptThreadWithoutHibernate extends EmptyTask implements INameableTask {
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
		} else if (step.isTypExport()) {
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

		boolean automatic = this.step.isTypAutomatisch();
		if(logger.isDebugEnabled()){
			logger.debug("step is automatic: " + automatic);
		}
		List<String> scriptPaths = StepManager.loadScripts(this.step.getId());
		if(logger.isDebugEnabled()){
			logger.debug("found " + scriptPaths.size() + " scripts");
		}
		if (scriptPaths.size() > 0) {
			this.hs.executeAllScriptsForStep(this.step, automatic);
		} else if (this.step.isTypExport()) {
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
