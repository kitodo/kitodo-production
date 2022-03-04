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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.kitodo.api.Metadata;
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
     * @param selectedCatalog
     *            the catalog to import from.
     * @param csvInputStream the inputStream to parse.
     * @param projectId the project id.
     * @param templateId the template id.
     * @param metadata metadata edited via frontend to add to the imported metadata.
     */
    public void importFromCSV(String selectedCatalog, InputStream csvInputStream, int projectId, int templateId,
                              Collection<Metadata> metadata) throws IOException, ImportException {
        CSVReader reader;
        List<String> ppns = new ArrayList<>();
        reader = new CSVReader(new InputStreamReader(csvInputStream));
        String[] line;
        while ((line = reader.readNext()) != null) {
            ppns.add(line[0]);
        }
        importPPNs(selectedCatalog, ppns, projectId, templateId, metadata);
    }

    /**
     * Import Processes from given commaseparated text.
     * @param selectedCatalog
     *            the catalog to import from
     * @param ppnString the ppn string from textfield.
     * @param projectId the project id.
     * @param templateId the template id.
     * @param metadata metadata edited via frontend to add to the imported metadata.
     */
    public void importFromText(String selectedCatalog, String ppnString, int projectId, int templateId, Collection<Metadata> metadata)
            throws ImportException {
        List<String> ppns = Arrays.asList(ppnString.replaceAll("\\s","").split(","));
        importPPNs(selectedCatalog, ppns, projectId, templateId, metadata);
    }

    private void importPPNs(String selectedCatalog, List<String> ppns, int projectId, int templateId, Collection<Metadata> metadata)
            throws ImportException {
        ImportService importService = ServiceManager.getImportService();
        for (String ppn : ppns) {
            importService.importProcess(ppn, projectId, templateId, selectedCatalog, metadata);
        }
    }
}
