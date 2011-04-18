package org.goobi.production.plugin.interfaces;

import java.util.HashMap;

import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.StepReturnValue;

import de.sub.goobi.Beans.Schritt;

public interface IStepPlugin extends IPlugin {
	
	public void initialize(Schritt step, String returnPath);

	public void execute();

	public String cancel();

	public String finish();

	public HashMap<String, StepReturnValue> validate();

	public Schritt getStep();
	
	public PluginGuiType getPluginGuiType();
	
}
