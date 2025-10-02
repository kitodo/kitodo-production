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

    /**
     * Get result.
     *
     * @return HSSFWorkbook
     */
    public XSSFWorkbook getResult() {
        return getWorkbook();
    }

    private List<ProcessExportDTO> getResultsWithFilter() {
        return ServiceManager.getProcessService().getProcessesForExport(
                filter,
                this.showClosedProcesses,
                this.showInactiveProjects,
                ServiceManager.getUserService().getSessionClientId()
        );
    }

    private XSSFWorkbook getWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Search results");

        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue(this.filter);
        for (int i = 1; i < 8; i++) {
            title.createCell(i).setCellValue("");
        }

        setRowHeader(sheet);

        insertRowData(sheet);

        return workbook;
    }

    private void insertRowData(Sheet sheet) {
        int rowCounter = 2;
        List<ProcessExportDTO> results = getResultsWithFilter();
        for (ProcessExportDTO rowData : results) {
            prepareRow(rowCounter, sheet, rowData);
            rowCounter++;
        }
    }

    private void setRowHeader(Sheet sheet) {
        Row rowHeader = sheet.createRow(1);
        rowHeader.createCell(0).setCellValue(Helper.getTranslation("title"));
        rowHeader.createCell(1).setCellValue(Helper.getTranslation("ID"));
        rowHeader.createCell(2).setCellValue(Helper.getTranslation("Datum"));
        rowHeader.createCell(3).setCellValue(Helper.getTranslation("CountImages"));
        rowHeader.createCell(4).setCellValue(Helper.getTranslation("CountStructuralElements"));
        rowHeader.createCell(5).setCellValue(Helper.getTranslation("CountMetadata"));
        rowHeader.createCell(6).setCellValue(Helper.getTranslation("Project"));
        rowHeader.createCell(7).setCellValue(Helper.getTranslation("Status"));
    }

    private void prepareRow(int rowCounter, Sheet sheet, ProcessExportDTO data) {
        Row row = sheet.createRow(rowCounter);
        row.createCell(0).setCellValue(data.getTitle());
        row.createCell(1).setCellValue(data.getId());
        row.createCell(2).setCellValue(
                data.getCreationDate() != null ? dateFormatter.format(data.getCreationDate()) : ""
        );
        row.createCell(3).setCellValue(data.getSortHelperImages());
        row.createCell(4).setCellValue(data.getSortHelperDocstructs());
        row.createCell(5).setCellValue(data.getSortHelperMetadata());
        row.createCell(6).setCellValue(data.getProjectTitle());
        row.createCell(7).setCellValue(data.getStatus());
    }
}
