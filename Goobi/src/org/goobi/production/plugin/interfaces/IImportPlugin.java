package org.goobi.production.plugin.interfaces;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.util.List;

import org.goobi.production.enums.ImportType;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.properties.ImportProperty;

import de.sub.goobi.helper.exceptions.ImportPluginException;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public interface IImportPlugin extends IPlugin {
	
	public void setPrefs(Prefs prefs);
	
	public void setData(Record r);
	
	public Fileformat convertData() throws ImportPluginException;
	
	public String getImportFolder();
	
	public String getProcessTitle();

	public List<ImportObject> generateFiles(List<Record> records);
	
	public void setImportFolder(String folder);
	
	public List<Record> splitRecords(String records);

	public List<Record> generateRecordsFromFile();

	public List<Record> generateRecordsFromFilenames(List<String> filenames);
	
	public void setFile(File importFile);
	
	public List<String> splitIds(String ids);
	
	public List<ImportType> getImportTypes();
	
	public List<ImportProperty> getProperties();
	
	public List<String> getAllFilenames();

	public void deleteFiles(List<String> selectedFilenames);	
	
	public List<? extends DocstructElement> getCurrentDocStructs();
	
	public String deleteDocstruct();
	
	public String addDocstruct();
	
	public List<String> getPossibleDocstructs();
	
	public DocstructElement getDocstruct();
	
	public void setDocstruct(DocstructElement dse);
}
