package org.goobi.production.plugin.interfaces;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.goobi.production.Import.Record;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.ImportType;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public interface IImportPlugin extends IPlugin {
	
	public void setPrefs(Prefs prefs);
	
	public void setData(Record r);
	
	public Fileformat convertData();
	
	public String getImportFolder();
	
	public String getProcessTitle();

	public List<Record> splitRecords(String records);
	
	public HashMap<String, ImportReturnValue> generateFiles(List<Record> records);
	
	public void setImportFolder(String folder);
	
	public List<Record> generateRecordsFromFile();
	
	public void setFile(File importFile);
	
	public List<String> splitIds(String ids);
	
	public List<ImportType> getImportTypes();
	
}
