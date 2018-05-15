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

import de.sub.goobi.helper.Helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
import org.kitodo.services.ServiceManager;

public class SearchResultGeneration {

    private String filter;
    private boolean showClosedProcesses;
    private boolean showInactiveProjects;
    private static ServiceManager serviceManager = new ServiceManager();
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
    @SuppressWarnings("deprecation")
    public HSSFWorkbook getResult() {
        List<ProcessDTO> processDTOS = new ArrayList<>();
        BoolQueryBuilder query = new BoolQueryBuilder();

        try {
            query = serviceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS, false,
                    false);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        if (!this.showClosedProcesses) {
            query.mustNot(serviceManager.getProcessService().getQuerySortHelperStatus(true));
        }
        if (!this.showInactiveProjects) {
            try {
                query.mustNot(serviceManager.getProcessService().getQueryProjectActive(false));
            } catch (DataException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            processDTOS = serviceManager.getProcessService().findByQuery(query,
                    serviceManager.getProcessService().sortByTitle(SortOrder.ASC), false);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        List<Process> processes = new ArrayList<>();
        try {
            processes = serviceManager.getProcessService().convertDtosToBeans(processDTOS);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Search results");

        HSSFRow title = sheet.createRow(0);
        HSSFCell titleCell1 = title.createCell(0);
        titleCell1.setCellValue(this.filter);
        HSSFCell titleCell2 = title.createCell(1);
        titleCell2.setCellValue("");
        HSSFCell titleCell3 = title.createCell(2);
        titleCell3.setCellValue("");
        HSSFCell titleCell4 = title.createCell(3);
        titleCell4.setCellValue("");
        HSSFCell titleCell5 = title.createCell(4);
        titleCell5.setCellValue("");
        HSSFCell titleCell6 = title.createCell(5);
        titleCell6.setCellValue("");
        HSSFCell titleCell7 = title.createCell(6);
        titleCell7.setCellValue("");
        HSSFCell titleCell8 = title.createCell(7);
        titleCell8.setCellValue("");
        HSSFCell titleCell9 = title.createCell(8);
        titleCell9.setCellValue("");

        HSSFRow row0 = sheet.createRow(1);
        HSSFCell headercell0 = row0.createCell(0);
        headercell0.setCellValue(Helper.getTranslation("title"));
        HSSFCell headercell1 = row0.createCell(1);
        headercell1.setCellValue(Helper.getTranslation("ID"));
        HSSFCell headercell2 = row0.createCell(2);
        headercell2.setCellValue(Helper.getTranslation("Datum"));
        HSSFCell headercell3 = row0.createCell(3);
        headercell3.setCellValue(Helper.getTranslation("CountImages"));
        HSSFCell headercell4 = row0.createCell(4);
        headercell4.setCellValue(Helper.getTranslation("CountMetadata"));
        HSSFCell headercell5 = row0.createCell(5);
        headercell5.setCellValue(Helper.getTranslation("Project"));
        HSSFCell headercell6 = row0.createCell(6);
        headercell6.setCellValue(Helper.getTranslation("Status"));
        HSSFCell headercell7 = row0.createCell(7);
        headercell7.setCellValue(Helper.getTranslation("AltRefNo"));
        HSSFCell headercell8 = row0.createCell(8);
        headercell8.setCellValue(Helper.getTranslation("b-number"));

        int rowCounter = 2;
        for (Process p : processes) {
            HSSFRow row = sheet.createRow(rowCounter);
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(p.getTitle());
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(p.getId());
            HSSFCell cell2 = row.createCell(2);
            DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            String gmtCreationDate = df.format(p.getCreationDate());
            cell2.setCellValue(gmtCreationDate);
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(p.getSortHelperImages());
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(p.getSortHelperDocstructs());
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(p.getProject().getTitle());
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(p.getSortHelperStatus().substring(0, 3) + " / " + p.getSortHelperStatus().substring(3, 6)
                    + " / " + p.getSortHelperStatus().substring(6));
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
