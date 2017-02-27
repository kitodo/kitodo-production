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

    public void setOpacCatalogue(String opacCatalogue);

    public void setGoobiConfigDirectory(String configDir);
}
