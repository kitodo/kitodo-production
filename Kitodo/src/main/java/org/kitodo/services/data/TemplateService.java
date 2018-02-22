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

package org.kitodo.services.data;

import de.sub.goobi.helper.Helper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProcessDAO;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.ProcessType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.forms.TemplateForm;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.TitleSearchService;

public class TemplateService extends TitleSearchService<Process, ProcessDTO, ProcessDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static TemplateService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private TemplateService() {
        super(new ProcessDAO(), new ProcessType(), new Indexer<>(Process.class), new Searcher(Process.class));
    }

    /**
     * Return singleton variable of type TemplateService.
     *
     * @return unique instance of TemplateService
     */
    public static TemplateService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (TemplateService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new TemplateService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return serviceManager.getProcessService().countDatabaseRows();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProcessDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        Map<String, String> filterMap = (Map<String, String>) filters;

        BoolQueryBuilder query;

        if (Objects.equals(filters, null) || filters.isEmpty()) {
            return convertJSONObjectsToDTOs(serviceManager.getProcessService().findBySort(false, false, true, sort, offset, size), false);
        }

        query = readFilters(filterMap);

        String queryString = "";
        if (!Objects.equals(query, null)) {
            queryString = query.toString();
        }
        return convertJSONObjectsToDTOs(searcher.findDocuments(queryString, sort, offset, size), false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String createCountQuery(Map filters) throws DataException {
        Map<String, String> filterMap = (Map<String, String>) filters;

        BoolQueryBuilder query;

        if (Objects.equals(filters, null) || filters.isEmpty()) {
            query = new BoolQueryBuilder();
            query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
            query.must(serviceManager.getProcessService().getQueryProjectArchived(false));
            query.must(getQueryTemplate(true));
        } else {
            query = readFilters(filterMap);
        }

        if (Objects.nonNull(query)) {
            return query.toString();
        }
        return "";
    }

    private BoolQueryBuilder readFilters(Map<String, String> filterMap) throws DataException {
        TemplateForm form = (TemplateForm) Helper.getManagedBeanValue("#{TemplateForm}");
        if (Objects.isNull(form)) {
            form = new TemplateForm();
        }
        BoolQueryBuilder query = null;

        for (Map.Entry<String, String> entry : filterMap.entrySet()) {
            query = serviceManager.getFilterService().queryBuilder(entry.getValue(), ObjectType.PROCESS, true, false, false);
            if (!form.isShowClosedProcesses()) {
                query.must(serviceManager.getProcessService().getQuerySortHelperStatus(false));
            }
            if (!form.isShowArchivedProjects()) {
                query.must(serviceManager.getProcessService().getQueryProjectArchived(false));
            }
        }
        return query;
    }

    @Override
    public ProcessDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        return serviceManager.getProcessService().convertJSONObjectToDTO(jsonObject, related);
    }

    /**
     * Count amount of templates for process.
     *
     * @return amount of templates for process as Long
     */
    public Long countTemplates() throws DataException {
        return count(getQueryTemplate(true).toString());
    }

    /**
     * Find all templates sorted according to sort query.
     *
     * @param sort
     *            possible sort query according to which results will be sorted
     * @return the list of sorted processes as ProcessDTO objects
     */
    public List<ProcessDTO> findAllTemplates(String sort) throws DataException {
        return convertJSONObjectsToDTOs(findByTemplate(true, sort), false);
    }

    /**
     * Get query for template.
     *
     * @param template
     *            true or false
     * @return query as QueryBuilder
     */
    public QueryBuilder getQueryTemplate(boolean template) {
        return createSimpleQuery("template", template, true);
    }

    List<JSONObject> findByTemplate(boolean template, String sort) throws DataException {
        return searcher.findDocuments(getQueryTemplate(template).toString(), sort);
    }
}
