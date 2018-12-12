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

package org.goobi.production.flow.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.services.ServiceManager;

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
        List<ProcessDTO> processDTOS = new ArrayList<>();
        BoolQueryBuilder query = new BoolQueryBuilder();

        try {
            query = ServiceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS, false,
                    false);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        if (!this.showClosedProcesses) {
            query.mustNot(ServiceManager.getProcessService().getQuerySortHelperStatus(true));
        }
        if (!this.showInactiveProjects) {
            query.mustNot(ServiceManager.getProcessService().getQueryProjectActive(false));
        }

        try {
            processDTOS = ServiceManager.getProcessService().findByQuery(query,
                    ServiceManager.getProcessService().sortByTitle(SortOrder.ASC), false);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        List<Process> processes = new ArrayList<>();
        try {
            processes = ServiceManager.getProcessService().convertDtosToBeans(processDTOS);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Search results");

        HSSFRow title = sheet.createRow(0);
        title.createCell(0).setCellValue(this.filter);
        title.createCell(1).setCellValue("");
        title.createCell(2).setCellValue("");
        title.createCell(3).setCellValue("");
        title.createCell(4).setCellValue("");
        title.createCell(5).setCellValue("");
        title.createCell(6).setCellValue("");
        title.createCell(7).setCellValue("");
        title.createCell(8).setCellValue("");

        HSSFRow rowHeader = sheet.createRow(1);
        rowHeader.createCell(0).setCellValue(Helper.getTranslation("title"));
        rowHeader.createCell(1).setCellValue(Helper.getTranslation("ID"));
        rowHeader.createCell(2).setCellValue(Helper.getTranslation("Datum"));
        rowHeader.createCell(3).setCellValue(Helper.getTranslation("CountImages"));
        rowHeader.createCell(4).setCellValue(Helper.getTranslation("CountMetadata"));
        rowHeader.createCell(5).setCellValue(Helper.getTranslation("Project"));
        rowHeader.createCell(6).setCellValue(Helper.getTranslation("Status"));
        rowHeader.createCell(7).setCellValue(Helper.getTranslation("AltRefNo"));
        rowHeader.createCell(8).setCellValue(Helper.getTranslation("b-number"));

        int rowCounter = 2;
        for (Process p : processes) {
            HSSFRow row = sheet.createRow(rowCounter);
            row.createCell(0).setCellValue(p.getTitle());
            row.createCell(1).setCellValue(p.getId());
            DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            String gmtCreationDate = df.format(p.getCreationDate());
            row.createCell(2).setCellValue(gmtCreationDate);
            row.createCell(3).setCellValue(p.getSortHelperImages());
            row.createCell(4).setCellValue(p.getSortHelperDocstructs());
            row.createCell(5).setCellValue(p.getProject().getTitle());
            String sortHelperStatus = "";
            if (Objects.nonNull(p.getSortHelperStatus())) {
                sortHelperStatus = p.getSortHelperStatus().substring(0, 3) + " / " + p.getSortHelperStatus().substring(3, 6)
                        + " / " + p.getSortHelperStatus().substring(6);
            }
            row.createCell(6).setCellValue(sortHelperStatus);
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue("");
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue("");
            for (Property property : p.getProperties()) {
                if (property.getTitle().equals("AltRefNo")) {
                    cell7.setCellValue(property.getValue());
                } else if (property.getTitle().equals("b-number")) {
                    cell8.setCellValue(property.getValue());
                }
            }
            rowCounter++;
        }
        return wb;
    }
}
