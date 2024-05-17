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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.production.services.ServiceManager;

public class SearchResultGeneration {

    private String filter;
    private boolean showClosedProcesses;
    private boolean showInactiveProjects;
    private static final Logger logger = LogManager.getLogger(SearchResultGeneration.class);

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
    public HSSFWorkbook getResult() {
        return getWorkbook();
    }

    private List<Process> getResultsWithFilter() {
        List<Process> processInterfaces;
        processInterfaces = ServiceManager.getProcessService().getByQuery(getQueryForFilter(Process.class)
                    + " ORDER BY id ASC");
        return processInterfaces;
    }

    /**
     * Gets the query with filters.
     *
     * @param objectType
     *            Type of object that should be filtered
     * @return A query
     */
    public String getQueryForFilter(Class<? extends BaseBean> objectType) {
        String query = "FROM Process AS process";
        String operator = " WHERE";
        if (!StringUtils.isBlank(filter)) {
            query += operator + " process.title LIKE '%" + filter + "%'";
            operator = " AND";
        }
        if (!this.showClosedProcesses) {
            query += operator + " process.sortHelperStatus != '100000000000'";
            operator = " AND";
        }
        if (!this.showInactiveProjects) {
            query += operator + " process.project.active = 0";
            operator = " AND";
        }
        return query;
    }

    private HSSFWorkbook getWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Search results");

        HSSFRow title = sheet.createRow(0);
        title.createCell(0).setCellValue(this.filter);
        for (int i = 1; i < 8; i++) {
            title.createCell(i).setCellValue("");
        }

        setRowHeader(sheet);

        insertRowData(sheet);

        return workbook;
    }

    private void insertRowData(HSSFSheet sheet) {
        int rowCounter = 2;
        List<? extends ProcessInterface> resultsWithFilter = getResultsWithFilter();
        for (ProcessInterface processDTO : resultsWithFilter) {
            prepareRow(rowCounter, sheet, processDTO);
            rowCounter++;
        }
    }

    private void setRowHeader(HSSFSheet sheet) {
        HSSFRow rowHeader = sheet.createRow(1);
        rowHeader.createCell(0).setCellValue(Helper.getTranslation("title"));
        rowHeader.createCell(1).setCellValue(Helper.getTranslation("ID"));
        rowHeader.createCell(2).setCellValue(Helper.getTranslation("Datum"));
        rowHeader.createCell(3).setCellValue(Helper.getTranslation("CountImages"));
        rowHeader.createCell(4).setCellValue(Helper.getTranslation("CountStructuralElements"));
        rowHeader.createCell(5).setCellValue(Helper.getTranslation("CountMetadata"));
        rowHeader.createCell(6).setCellValue(Helper.getTranslation("Project"));
        rowHeader.createCell(7).setCellValue(Helper.getTranslation("Status"));
    }

    private void prepareRow(int rowCounter, HSSFSheet sheet, ProcessInterface process) {
        HSSFRow row = sheet.createRow(rowCounter);
        row.createCell(0).setCellValue(process.getTitle());
        row.createCell(1).setCellValue(process.getId());
        row.createCell(2).setCellValue(process.getCreationTime());
        row.createCell(3).setCellValue(process.getNumberOfImages());
        row.createCell(4).setCellValue(process.getNumberOfStructures());
        row.createCell(5).setCellValue(process.getNumberOfMetadata());
        row.createCell(6).setCellValue(process.getProject().getTitle());
        row.createCell(7).setCellValue(process.getSortHelperStatus());
    }
}
