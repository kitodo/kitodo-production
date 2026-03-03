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

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kitodo.data.database.beans.BaseTemplateBean;
import org.kitodo.production.dto.ProcessExportDTO;
import org.kitodo.production.services.ServiceManager;

public class SearchResultGeneration {

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat(BaseTemplateBean.DATE_FORMAT);
    private final String filter;
    private final boolean showClosedProcesses;
    private final boolean showInactiveProjects;

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


    /**
     * Writes the search results to the given output stream as an Excel file.
     * @param out the output stream to write to.
     */
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

    /**
     * Writes the search results to the given output stream as a CSV file.
     * @param out the output stream to write to.
     */
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

    /**
     * Writes search results to output stream as PDF.
     * @param out output stream
     */
    public void writePdf(OutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A3.rotate());
        PdfWriter.getInstance(document, out);
        document.open();
        PdfPTable table = new PdfPTable(getHeader().length);
        for (String column : getHeader()) {
            table.addCell(new PdfPCell(new Phrase(column)));
        }
        for (ProcessExportDTO data : getResultsWithFilter()) {
            String[] row = mapRow(data);
            for (String value : row) {
                table.addCell(new PdfPCell(new Phrase(value)));
            }
        }
        document.add(table);
        document.close();
    }

    /**
     * Returns the localized header for the export.
     * @return array of header column names.
     */
    private String[] getHeader() {
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

    /**
     * Maps the given export DTO to a string array for export.
     * @param data the process data to map.
     * @return array representing one export row.
     */
    private String[] mapRow(ProcessExportDTO data) {
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

    /**
     * Retrieves the filtered list of processes prepared for export.
     * @return list of ProcessExportDTO objects.
     */
    private List<ProcessExportDTO> getResultsWithFilter() {
        return ServiceManager.getProcessService().getProcessesForExport(
                filter,
                this.showClosedProcesses,
                this.showInactiveProjects,
                ServiceManager.getUserService().getSessionClientId()
        );
    }
}
