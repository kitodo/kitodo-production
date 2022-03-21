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

package org.kitodo.production.services.data;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.file.UploadedFile;

public class MassImportService {

    private static MassImportService instance = null;

    /**
     * Return singleton variable of type MassImportService.
     *
     * @return unique instance of MassImportService
     */
    public static MassImportService getInstance() {
        MassImportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MassImportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MassImportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Import from csvFile.
     *  @param selectedCatalog
     *            the catalog to import from.
     * @param file the file to parse.
     * @param projectId the project id.
     * @param templateId the template id.
     * @param csvSeparator character used to separate columns in CSV file
     */
    public void importFromCSV(String selectedCatalog, UploadedFile file, int projectId, int templateId,
                              Character csvSeparator)
            throws IOException, ImportException {
        Map<String, Map<String, String>> presetMetadata = new HashMap<>();
        CSVReader reader;
        reader = new CSVReader(new InputStreamReader(file.getInputStream()), csvSeparator);
        String[] line;
        int counter = 0;
        String[] metadataKeys = new String[0];
        while ((line = reader.readNext()) != null) {
            if (counter == 0) {
                metadataKeys = line;
            } else {
                Map<String, String> processMetadata = new HashMap<>();
                for (int i = 1; i < metadataKeys.length; i++) {
                    if (StringUtils.isNotBlank(line[i])) {
                        processMetadata.put(metadataKeys[i], line[i]);
                    }
                }
                presetMetadata.put(line[0], processMetadata);
            }
            counter++;
        }
        importPPNs(selectedCatalog, presetMetadata, projectId, templateId);
    }

    /**
     * Import Processes from given comma separated text.
     *  @param selectedCatalog
     *            the catalog to import from
     * @param ppnString the ppn string from text field.
     * @param projectId the project id.
     * @param templateId the template id.
     */
    public void importFromText(String selectedCatalog, String ppnString, int projectId, int templateId)
            throws ImportException {
        String[] ppns = ppnString.replaceAll("\\s","").split(",");
        Map<String, Map<String, String>> presetMetadata = new HashMap<>();
        for (String ppn : ppns) {
            presetMetadata.put(ppn, new HashMap<>());
        }
        importPPNs(selectedCatalog, presetMetadata, projectId, templateId);
    }

    private void importPPNs(String selectedCatalog, Map<String, Map<String, String>> processMetadata, int projectId,
                            int templateId)
            throws ImportException {
        ImportService importService = ServiceManager.getImportService();
        for (Map.Entry<String, Map<String, String>> entry : processMetadata.entrySet()) {
            importService.importProcess(entry.getKey(), projectId, templateId, selectedCatalog, entry.getValue());
        }
    }
}
