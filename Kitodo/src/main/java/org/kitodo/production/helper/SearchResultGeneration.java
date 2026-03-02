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

package org.kitodo.production.helper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kitodo.data.database.beans.BaseTemplateBean;
import org.kitodo.production.dto.ProcessExportDTO;
import org.kitodo.production.services.ServiceManager;

public class SearchResultGeneration {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(BaseTemplateBean.DATE_FORMAT);
    private String filter;
    private boolean showClosedProcesses;
    private boolean showInactiveProjects;

    /**
     * Constructor.
     *
     * @param filter
     *            String
     * @param showClosedProcesses
     *            boolean
     * @param showInactiveProjects
     *            boolean
     */
    public SearchResultGeneration(String filter, boolean showClosedProcesses, boolean showInactiveProjects) {
        this.filter = filter;
        this.showClosedProcesses = showClosedProcesses;
        this.showInactiveProjects = showInactiveProjects;
    }


    public void writeExcel(OutputStream out) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Search results");

            Row title = sheet.createRow(0);
            title.createCell(0).setCellValue(this.filter);
            Row headerRow = sheet.createRow(1);
            String[] header = getHeader();
            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }
            int rowCounter = 2;
            for (ProcessExportDTO data : getResultsWithFilter()) {
                Row row = sheet.createRow(rowCounter++);
                String[] mapped = mapRow(data);
                for (int i = 0; i < mapped.length; i++) {
                    row.createCell(i).setCellValue(mapped[i]);
                }
            }
            workbook.write(out);
        }
    }

    public void writeCsv(OutputStream out) throws IOException {

        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
             CSVWriter writer = new CSVWriter(bufferedWriter)) {
            writer.writeNext(getHeader());
            for (ProcessExportDTO data : getResultsWithFilter()) {
                writer.writeNext(mapRow(data));
            }
        }
    }

    public String[] getHeader() {
        return new String[]{
                Helper.getTranslation("title"),
                Helper.getTranslation("ID"),
                Helper.getTranslation("Datum"),
                Helper.getTranslation("CountImages"),
                Helper.getTranslation("CountStructuralElements"),
                Helper.getTranslation("CountMetadata"),
                Helper.getTranslation("Project"),
                Helper.getTranslation("Status")
        };
    }

    public String[] mapRow(ProcessExportDTO data) {
        return new String[]{
                data.getTitle(),
                String.valueOf(data.getId()),
                data.getCreationDate() != null
                        ? dateFormatter.format(data.getCreationDate())
                        : "",
                String.valueOf(data.getSortHelperImages()),
                String.valueOf(data.getSortHelperDocstructs()),
                String.valueOf(data.getSortHelperMetadata()),
                data.getProjectTitle(),
                data.getStatus()
        };
    }

    public List<ProcessExportDTO> getResultsWithFilter() {
        return ServiceManager.getProcessService().getProcessesForExport(
                filter,
                this.showClosedProcesses,
                this.showInactiveProjects,
                ServiceManager.getUserService().getSessionClientId()
        );
    }
}
