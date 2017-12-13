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
import java.util.ArrayList;
import java.util.List;
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

    private String filter = "";
    private boolean showClosedProcesses = false;
    private boolean showArchivedProjects = false;
    private static ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(SearchResultGeneration.class);

    /**
     * Constructor.
     *
     * @param filter
     *            String
     * @param showClosedProcesses
     *            boolean
     * @param showArchivedProjects
     *            boolean
     */
    public SearchResultGeneration(String filter, boolean showClosedProcesses, boolean showArchivedProjects) {
        this.filter = filter;
        this.showClosedProcesses = showClosedProcesses;
        this.showArchivedProjects = showArchivedProjects;
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
            query = serviceManager.getFilterService().queryBuilder(this.filter, ObjectType.PROCESS, false, false,
                false);
        } catch (DataException e) {
            logger.error(e);
        }
        query.must(serviceManager.getProcessService().getQueryTemplate(false));

        if (!this.showClosedProcesses) {
            query.mustNot(serviceManager.getProcessService().getQuerySortHelperStatus(true));
        }
        if (!this.showArchivedProjects) {
            try {
                query.mustNot(serviceManager.getProcessService().getQueryProjectArchived(true));
            } catch (DataException e) {
                logger.error(e);
            }
        }

        try {
            processDTOS = serviceManager.getProcessService().findByQuery(query,
                serviceManager.getProcessService().sortByTitle(SortOrder.ASC), false);
        } catch (DataException e) {
            logger.error(e);
        }

        List<Process> processes = new ArrayList<>();
        try {
            processes = serviceManager.getProcessService().convertDtosToBeans(processDTOS);
        } catch (DAOException e) {
            logger.error(e);
        }
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Search results");

        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell[] titleCells = new HSSFCell[] {titleRow.createCell(0), titleRow.createCell(1), titleRow.createCell(2),
                                                titleRow.createCell(3), titleRow.createCell(4), titleRow.createCell(5),
                                                titleRow.createCell(6), titleRow.createCell(7),
                                                titleRow.createCell(8) };
        titleCells[0].setCellValue(this.filter);
        titleCells[1].setCellValue("");
        titleCells[2].setCellValue("");
        titleCells[3].setCellValue("");
        titleCells[4].setCellValue("");
        titleCells[5].setCellValue("");
        titleCells[6].setCellValue("");
        titleCells[7].setCellValue("");
        titleCells[8].setCellValue("");

        HSSFRow headerRow = sheet.createRow(1);
        HSSFCell[] headercells = new HSSFCell[] {headerRow.createCell(0), headerRow.createCell(1),
                                                 headerRow.createCell(2), headerRow.createCell(3),
                                                 headerRow.createCell(4), headerRow.createCell(5),
                                                 headerRow.createCell(6), headerRow.createCell(7),
                                                 headerRow.createCell(8) };
        headercells[0].setCellValue(Helper.getTranslation("title"));
        headercells[1].setCellValue(Helper.getTranslation("ID"));
        headercells[2].setCellValue(Helper.getTranslation("Datum"));
        headercells[3].setCellValue(Helper.getTranslation("CountImages"));
        headercells[4].setCellValue(Helper.getTranslation("CountMetadata"));
        headercells[5].setCellValue(Helper.getTranslation("Project"));
        headercells[6].setCellValue(Helper.getTranslation("Status"));
        headercells[7].setCellValue(Helper.getTranslation("AltRefNo"));
        headercells[8].setCellValue(Helper.getTranslation("b-number"));

        int rowcounter = 2;
        for (Process p : processes) {
            HSSFRow row = sheet.createRow(rowcounter);
            HSSFCell[] cells = new HSSFCell[] {row.createCell(0), row.createCell(1), row.createCell(2),
                                               row.createCell(3), row.createCell(4), row.createCell(5),
                                               row.createCell(6), row.createCell(7), row.createCell(8) };
            cells[0].setCellValue(p.getTitle());
            cells[1].setCellValue(p.getId());
            cells[2].setCellValue(p.getCreationDate().toGMTString());
            cells[3].setCellValue(p.getSortHelperImages());
            cells[4].setCellValue(p.getSortHelperDocstructs());
            cells[5].setCellValue(p.getProject().getTitle());
            cells[6].setCellValue(p.getSortHelperStatus().substring(0, 3) + " / "
                    + p.getSortHelperStatus().substring(3, 6) + " / " + p.getSortHelperStatus().substring(6));
            cells[7].setCellValue("");
            cells[8].setCellValue("");
            if (p.getProperties().size() > 0) {
                for (Property property : p.getProperties()) {
                    if (property.getTitle().equals("AltRefNo")) {
                        cells[7].setCellValue(property.getValue());
                    } else if (property.getTitle().equals("b-number")) {
                        cells[7].setCellValue(property.getValue());
                    }
                }
            }
            rowcounter++;
        }
        return wb;
    }
}
