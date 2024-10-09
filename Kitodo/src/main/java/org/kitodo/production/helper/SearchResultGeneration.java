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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.services.ServiceManager;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.sort.SortOrder;

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

    private List<ProcessDTO> getResultsWithFilter() {
        List<ProcessDTO> processDTOS = new ArrayList<>();
        try {
            processDTOS = ServiceManager.getProcessService().findByQuery(getQueryForFilter(ObjectType.PROCESS),
                ServiceManager.getProcessService().sortById(SortOrder.ASC), true);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        return processDTOS;
    }

    /**
     * Gets the query with filters.
     *
     * @param objectType Type of object that should be filtered
     * @return A BoolQueryBuilder
     */
    public BoolQueryBuilder getQueryForFilter(ObjectType objectType) {
        BoolQueryBuilder query = new BoolQueryBuilder();

        try {
            query = ServiceManager.getFilterService().queryBuilder(this.filter, objectType, false, false);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }

        if (!this.showClosedProcesses) {
            query.mustNot(ServiceManager.getProcessService().getQueryForClosedProcesses());
        }
        if (!this.showInactiveProjects) {
            query.mustNot(ServiceManager.getProcessService().getQueryProjectActive(false));
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
        long numberOfProcessedProcesses = 0;
        int elasticsearchLimit = 9999;
        try {
            Long numberOfExpectedProcesses = ServiceManager.getProcessService()
                    .count(getQueryForFilter(ObjectType.PROCESS));
            if (numberOfExpectedProcesses > elasticsearchLimit) {
                List<ProcessDTO> processDTOS;
                int queriedIds = 0;
                while (numberOfProcessedProcesses < numberOfExpectedProcesses) {
                    RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder(ProcessTypeField.ID.toString());
                    rangeQueryBuilder.gte(queriedIds).lt(queriedIds + elasticsearchLimit);
                    BoolQueryBuilder queryForFilter = getQueryForFilter(ObjectType.PROCESS);
                    queryForFilter.must(rangeQueryBuilder);
                    processDTOS = ServiceManager.getProcessService().findByQuery(queryForFilter,
                        ServiceManager.getProcessService().sortById(SortOrder.ASC), true);
                    queriedIds += elasticsearchLimit;
                    for (ProcessDTO processDTO : processDTOS) {
                        prepareRow(rowCounter, sheet, processDTO);
                        rowCounter++;
                    }
                    numberOfProcessedProcesses += processDTOS.size();
                }
            } else {
                List<ProcessDTO> resultsWithFilter = getResultsWithFilter();
                for (ProcessDTO processDTO : resultsWithFilter) {
                    prepareRow(rowCounter, sheet, processDTO);
                    rowCounter++;
                }
            }
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
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

    private void prepareRow(int rowCounter, HSSFSheet sheet, ProcessDTO processDTO) {
        HSSFRow row = sheet.createRow(rowCounter);
        row.createCell(0).setCellValue(processDTO.getTitle());
        row.createCell(1).setCellValue(processDTO.getId());
        row.createCell(2).setCellValue(processDTO.getCreationDate());
        row.createCell(3).setCellValue(processDTO.getNumberOfImages());
        row.createCell(4).setCellValue(processDTO.getNumberOfStructures());
        row.createCell(5).setCellValue(processDTO.getNumberOfMetadata());
        row.createCell(6).setCellValue(processDTO.getProject().getTitle());
        row.createCell(7).setCellValue(processDTO.getSortHelperStatus());
    }
}
