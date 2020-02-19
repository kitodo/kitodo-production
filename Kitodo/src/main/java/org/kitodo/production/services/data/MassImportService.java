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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.UploadedFile;
import org.xml.sax.SAXException;

public class MassImportService {

    private static volatile MassImportService instance = null;

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
     */
    public void importFromCSV(String selectedCatalog, UploadedFile file, int projectId, int templateId) throws IOException {
        CSVReader reader = null;
        List<String> ppns = new ArrayList<>();
        reader = new CSVReader(new InputStreamReader(file.getInputstream()));
        String[] line;
        while ((line = reader.readNext()) != null) {
            ppns.add(line[0]);
        }
        importPPNs(selectedCatalog, ppns, projectId, templateId);
    }

    /**
     * Import Processes from given commaseparated text.
     *  @param selectedCatalog
     *            the catalog to import from
     * @param ppnString the ppn string from textfield.
     * @param projectId the project id.
     * @param templateId the template id.
     */
    public void importFromText(String selectedCatalog, String ppnString, int projectId, int templateId) {
        List<String> ppns = Arrays.asList(ppnString.split(","));
        importPPNs(selectedCatalog, ppns, projectId, templateId);

    }

    private void importPPNs(String selectedCatalog, List<String> ppns, int projectId, int templateId) {
        ImportService importService = ServiceManager.getImportService();
        for (String ppn : ppns) {
            // import
        }
    }

}
