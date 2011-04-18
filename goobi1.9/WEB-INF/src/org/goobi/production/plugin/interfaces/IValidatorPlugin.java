package org.goobi.production.plugin.interfaces;

import de.sub.goobi.Beans.Prozess;

public interface IValidatorPlugin extends IPlugin {
	
	public void initialize(Prozess inProcess);

	public boolean validate();
	
}
