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

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

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
        List<ProcessDTO> resultsWithFilter = getResultsWithFilter();

        List<Process> processes = new ArrayList<>();
        try {
            processes = ServiceManager.getProcessService().convertDtosToBeans(resultsWithFilter);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }

        return getWorkbook(processes);
    }

    private List<ProcessDTO> getResultsWithFilter() {
        List<ProcessDTO> processDTOS = new ArrayList<>();

        try {
            processDTOS = ServiceManager.getProcessService().findByQuery(getQueryForFilter(ObjectType.PROCESS),
                    ServiceManager.getProcessService().sortByTitle(SortOrder.ASC), false);
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

    private HSSFWorkbook getWorkbook(List<Process> processes) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Search results");

        HSSFRow title = sheet.createRow(0);
        title.createCell(0).setCellValue(this.filter);
        for (int i = 1; i < 8; i++) {
            title.createCell(i).setCellValue("");
        }

        HSSFRow rowHeader = sheet.createRow(1);
        rowHeader.createCell(0).setCellValue(Helper.getTranslation("title"));
        rowHeader.createCell(1).setCellValue(Helper.getTranslation("ID"));
        rowHeader.createCell(2).setCellValue(Helper.getTranslation("Datum"));
        rowHeader.createCell(3).setCellValue(Helper.getTranslation("CountImages"));
        rowHeader.createCell(4).setCellValue(Helper.getTranslation("CountStructuralElements"));
        rowHeader.createCell(5).setCellValue(Helper.getTranslation("CountMetadata"));
        rowHeader.createCell(6).setCellValue(Helper.getTranslation("Project"));
        rowHeader.createCell(7).setCellValue(Helper.getTranslation("Status"));

        int rowCounter = 2;
        for (Process process : processes) {
            prepareRow(rowCounter, sheet, process);
            rowCounter++;
        }
        return workbook;
    }

    private void prepareRow(int rowCounter, HSSFSheet sheet, Process process) {
        HSSFRow row = sheet.createRow(rowCounter);
        row.createCell(0).setCellValue(process.getTitle());
        row.createCell(1).setCellValue(process.getId());
        DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String gmtCreationDate = df.format(process.getCreationDate());
        row.createCell(2).setCellValue(gmtCreationDate);

        URI metadataFilePath;
        int numberOfProcessImages = 0;
        int numberOfProcessStructuralElements = 0;
        int numberOfProcessMetadata = 0;
        try {
            metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
            Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            numberOfProcessImages = (int) Workpiece.treeStream(workpiece.getMediaUnit())
                    .filter(mediaUnit -> Objects.equals(mediaUnit.getType(), MediaUnit.TYPE_PAGE)).count();
            numberOfProcessStructuralElements = (int) Workpiece.treeStream(workpiece.getRootElement()).count();
            numberOfProcessMetadata = Math.toIntExact(MetsService.countLogicalMetadata(workpiece));

        } catch (IOException e) {
            logger.debug("Metadata file not found for process with id: " + process.getId());
        }

        row.createCell(3).setCellValue(numberOfProcessImages);
        row.createCell(4).setCellValue(numberOfProcessStructuralElements);
        row.createCell(5).setCellValue(numberOfProcessMetadata);
        row.createCell(6).setCellValue(process.getProject().getTitle());
        String sortHelperStatus = "";
        if (Objects.nonNull(process.getSortHelperStatus())) {
            sortHelperStatus = process.getSortHelperStatus().substring(0, 3) + " / "
                    + process.getSortHelperStatus().substring(3, 6) + " / "
                    + process.getSortHelperStatus().substring(6);
        }
        row.createCell(7).setCellValue(sortHelperStatus);
    }
}
