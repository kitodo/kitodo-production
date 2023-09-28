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

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.IndexAction;
import org.kitodo.data.database.exceptions.DAOException;
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

    private static volatile BatchService instance = null;
    private static final String BATCH = "batch";

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
        BatchService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (BatchService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new BatchService();
                    instance = localReference;
                }
            }
        }
        return localReference;
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
    public List<Batch> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Remove all passed batches.
     *
     * @param batches
     *            to remove
     */
    public void removeAll(Iterable<Batch> batches) throws DataException {
        for (Batch batch : batches) {
            remove(batch);
        }
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
     * Returns true if the title (if set) or the
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
     * Returns the identifier for the batch as
     * read-only property "idString". This method is required by Faces which
     * silently fails if you try to use the id Integer.
     *
     * @return the identifier for the batch as String
     */
    public String getIdString(Batch batch) {
        return batch.getId().toString();
    }

    /**
     * Returns a readable label for the batch, which is
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
     * Returns a readable label for the batch, which is
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
     * Returns a readable label for the batch,
     * consisting of the prefix “Batch ” (in the desired translation) together
     * with its id number.
     *
     * @return a readable label for the batch
     */
    private String getNumericLabel(Batch batch) {
        return Helper.getTranslation(BATCH) + ' ' + batch.getId();
    }

    /**
     * Returns a readable label for the batch,
     * consisting of the prefix “Batch ” (in the desired translation) together
     * with its id number.
     *
     * @return a readable label for the batch
     */
    private String getNumericLabel(BatchDTO batch) {
        return Helper.getTranslation(BATCH) + ' ' + batch.getId();
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
     * The function creates label as informative representation that is easy for
     * a person to read and that "textually represents" this batch.
     *
     * @param batch
     *            for which label is going to be created
     */
    public String createLabel(Batch batch) {
        return prepareLabel(batch);
    }

    private String prepareLabel(Batch batch) {
        StringBuilder result = new StringBuilder();
        try {
            if (Objects.nonNull(batch.getTitle())) {
                result.append(batch.getTitle());
            } else if (Objects.nonNull(batch.getId())) {
                result.append(Helper.getTranslation(BATCH));
                result.append(' ');
                result.append(batch.getId());
            } else {
                result.append('−');
            }
            result.append(" (");
            result.append(Helper.getTranslation("numProcesses", Integer.toString(batch.getProcesses().size())));
        } catch (RuntimeException unexpected) {
            result.setLength(0);
            result.append(Objects.nonNull(batch.getTitle()) ? batch.getTitle() : batch.getId());
            result.append(" (");
            result.append(batch.getProcesses().size());
        }
        result.append(')');
        return result.toString();
    }
}
