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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.BatchDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BatchType;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.BatchDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.TitleSearchService;
import org.primefaces.model.SortOrder;

public class BatchService extends TitleSearchService<Batch, BatchDTO, BatchDAO> {

    private static BatchService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private BatchService() {
        super(new BatchDAO(), new BatchType(), new Indexer<>(Batch.class), new Searcher(Batch.class));
    }

    /**
     * Return singleton variable of type BatchService.
     *
     * @return unique instance of BatchService
     */
    public static BatchService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (BatchService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new BatchService();
                }
            }
        }
        return instance;
    }

    /**
     * Method saves processes related to modified batch.
     * 
     * @param batch
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(Batch batch) throws CustomResponseException, DataException, IOException {
        if (batch.getIndexAction() == IndexAction.DELETE) {
            for (Process process : batch.getProcesses()) {
                process.getBatches().remove(batch);
                ServiceManager.getProcessService().saveToIndex(process, false);
            }
        } else {
            for (Process process : batch.getProcesses()) {
                ServiceManager.getProcessService().saveToIndex(process, false);
            }
        }
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Batch");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Batch WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public Long countResults(Map filters) throws DataException {
        return countDocuments(QueryBuilders.matchAllQuery());
    }

    @Override
    public List<Batch> getAllNotIndexed() {
        return getByQuery("FROM Batch WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<Batch> getAllForSelectedClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Batch> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Remove all passed batches.
     * 
     * @param batches
     *            to remove
     */
    public void removeAll(Iterable<Batch> batches) throws DAOException {
        for (Batch batch : batches) {
            dao.remove(batch);
        }
    }

    /**
     * Find batches with exact title and type. Necessary to assure that user pickup
     * type from the list which contains enums.
     *
     * @param title
     *            of the searched batches
     * @param type
     *            of the searched batches
     * @return list of JSON objects with batches of exact type
     */
    public List<Map<String, Object>> findByTitleAndType(String title, org.kitodo.data.database.helper.enums.BatchType type) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(BatchTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.must(createSimpleQuery(BatchTypeField.TYPE.getKey(), type.toString(), true));
        return findDocuments(query);
    }

    /**
     * Find batch with exact title or type.
     *
     * @param title
     *            of the searched batch
     * @param type
     *            of the searched batch
     * @return search result
     */
    public List<Map<String, Object>> findByTitleOrType(String title, org.kitodo.data.database.helper.enums.BatchType type) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.should(createSimpleQuery(BatchTypeField.TITLE.getKey(), title, true, Operator.AND));
        query.should(createSimpleQuery(BatchTypeField.TYPE.getKey(), type.toString(), true));
        return findDocuments(query);
    }

    /**
     * Find batches by id of process.
     *
     * @param id
     *            of process
     * @return list of JSON objects with batches for specific process id
     */
    public List<Map<String, Object>> findByProcessId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("processes.id", id, true);
        return findDocuments(query);
    }

    /**
     * Find batches by title of process.
     *
     * @param title
     *            of process
     * @return list of JSON objects with batches for specific process title
     */
    public List<Map<String, Object>> findByProcessTitle(String title) throws DataException {
        QueryBuilder query = createSimpleQuery("processes.title", title, true, Operator.AND);
        return findDocuments(query);
    }

    @Override
    public BatchDTO convertJSONObjectToDTO(Map<String, Object> jsonObject, boolean related) throws DataException {
        BatchDTO batchDTO = new BatchDTO();
        batchDTO.setId(getIdFromJSONObject(jsonObject));
        batchDTO.setTitle(BatchTypeField.TITLE.getStringValue(jsonObject));
        batchDTO.setType(BatchTypeField.TYPE.getStringValue(jsonObject));
        if (!related) {
            convertRelatedJSONObjects(jsonObject, batchDTO);
        }
        return batchDTO;
    }

    private void convertRelatedJSONObjects(Map<String, Object> jsonObject, BatchDTO batchDTO) throws DataException {
        batchDTO.setProcesses(convertRelatedJSONObjectToDTO(jsonObject, BatchTypeField.PROCESSES.getKey(),
            ServiceManager.getProcessService()));
    }

    /**
     * The function contains() returns true if the title (if set) or the
     * id-based label contain the specified sequence of char values.
     *
     * @param sequence
     *            the sequence to search for
     * @return true if the title or label contain s, false otherwise
     */
    public boolean contains(Batch batch, CharSequence sequence) {
        return Objects.isNull(sequence) || Objects.nonNull(batch.getTitle()) && batch.getTitle().contains(sequence)
                || getNumericLabel(batch).contains(sequence);
    }

    /**
     * The function getIdString() returns the identifier for the batch as
     * read-only property "idString". This method is required by Faces which
     * silently fails if you try to use the id Integer.
     *
     * @return the identifier for the batch as String
     */
    public String getIdString(Batch batch) {
        return batch.getId().toString();
    }

    /**
     * The function getLabel() returns a readable label for the batch, which is
     * either its title, if defined, or, for batches not having a title (in
     * recent versions of Production, batches didn’t support titles) its ancient
     * label, consisting of the prefix “Batch ” (in the desired translation)
     * together with its id number.
     *
     * @return a readable label for the batch
     */
    public String getLabel(Batch batch) {
        return Objects.nonNull(batch.getTitle()) ? batch.getTitle() : getNumericLabel(batch);
    }

    /**
     * The function getLabel() returns a readable label for the batch, which is
     * either its title, if defined, or, for batches not having a title (in
     * recent versions of Production, batches didn’t support titles) its ancient
     * label, consisting of the prefix “Batch ” (in the desired translation)
     * together with its id number.
     *
     * @return a readable label for the batch
     */
    public String getLabel(BatchDTO batch) {
        return Objects.nonNull(batch.getTitle()) ? batch.getTitle() : getNumericLabel(batch);
    }

    /**
     * The function getNumericLabel() returns a readable label for the batch,
     * consisting of the prefix “Batch ” (in the desired translation) together
     * with its id number.
     *
     * @return a readable label for the batch
     */
    private String getNumericLabel(Batch batch) {
        return Helper.getTranslation("batch", "Batch") + ' ' + batch.getId();
    }

    /**
     * The function getNumericLabel() returns a readable label for the batch,
     * consisting of the prefix “Batch ” (in the desired translation) together
     * with its id number.
     *
     * @return a readable label for the batch
     */
    private String getNumericLabel(BatchDTO batch) {
        return Helper.getTranslation("batch", "Batch") + ' ' + batch.getId();
    }

    /**
     * Returns the translated batch type label.
     *
     * @return the display label for the batch type
     */
    public String getTypeTranslated(Batch batch) {
        if (Objects.nonNull(batch.getType())) {
            return Helper.getTranslation("batch_type_".concat(batch.getType().toString().toLowerCase()));
        } else {
            return "";
        }
    }

    /**
     * Returns the number of processes in this batch. If this batch contains
     * more than Integer.MAX_VALUE processes, returns Integer.MAX_VALUE.
     *
     * @return the number of elements in this batch
     * @see java.util.Collection#size()
     */
    public int size(Batch batch) {
        return batch.getProcesses().size();
    }

    /**
     * The function toString() returns a concise but informative representation
     * that is easy for a person to read and that "textually represents" this
     * batch.
     *
     */
    public String toString(Batch batch) {
        try {
            StringBuilder result = new StringBuilder(Objects.nonNull(batch.getTitle()) ? batch.getTitle().length() + 20 : 30);
            try {
                if (Objects.nonNull(batch.getTitle())) {
                    result.append(batch.getTitle());
                } else if (Objects.nonNull(batch.getId())) {
                    result.append(Helper.getTranslation("batch", "Batch"));
                    result.append(' ');
                    result.append(batch.getId());
                } else {
                    result.append('−');
                }
                result.append(" (");
                String extent = Helper.getTranslation("numProzesse", "{0} processes");
                String size = Integer.toString(batch.getProcesses().size());
                result.append(extent.replaceFirst("\\{0\\}", size));
            } catch (RuntimeException unexpected) {
                result.setLength(0);
                result.append(Objects.nonNull(batch.getTitle()) ? batch.getTitle() : batch.getId());
                result.append(" (");
                result.append(batch.getProcesses().size());
            }
            result.append(')');
            if (Objects.nonNull(batch.getType())) {
                result.append(" [");
                // TODO: check out method
                result.append(getTypeTranslated(batch));
                result.append(']');
            }
            return result.toString();
        } catch (RuntimeException fallback) {
            return super.toString();
        }
    }
}
