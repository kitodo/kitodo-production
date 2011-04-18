package org.goobi.production.plugin.interfaces;

import java.util.HashMap;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;

import de.sub.goobi.Beans.Schritt;

@PluginImplementation
public abstract class AbstractStepPlugin implements IStepPlugin {

	private static final Logger logger = Logger.getLogger(AbstractStepPlugin.class);

	protected String id = "abstract_step";
	protected String name = "Abstract Step Plugin";
	protected String version = "1.0";
	protected String description = "Abstract description for abstract step";

	protected Schritt myStep;
	protected String returnPath;

	@Override
	public void initialize(Schritt inStep, String inReturnPath) {
		myStep = inStep;
		returnPath = inReturnPath;
	}

	@Override
	public String getTitle() {
		return name + " v" + version;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public PluginType getType() {
		return PluginType.Step;
	}

	@Override
	public Schritt getStep() {
		return myStep;
	}

	@Override
	public String finish() {
		logger.debug("finish called");
		return returnPath;
	}

	@Override
	public String cancel() {
		logger.debug("cancel called");
		return returnPath;
	}

	@Override
	public HashMap<String, StepReturnValue> validate() {
		return null;
	}

}
