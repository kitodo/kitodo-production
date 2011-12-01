package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.ImportFormat;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public interface IImportPlugin extends IPlugin {

	public ImportFormat getFormat();
	
	public void setPrefs(Prefs prefs);
	
	public void setData(String data);
	
	public Fileformat convertData();
	
	public String getImportFolder();
	
	public String getProcessTitle();
}
