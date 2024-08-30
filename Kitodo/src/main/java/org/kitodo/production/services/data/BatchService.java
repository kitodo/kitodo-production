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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BatchDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.data.interfaces.DatabaseBatchServiceInterface;
import org.primefaces.model.SortOrder;

public class BatchService extends SearchDatabaseService<Batch, BatchDAO>
        implements DatabaseBatchServiceInterface {

    private static volatile BatchService instance = null;
    private static final String BATCH = "batch";

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private BatchService() {
        super(new BatchDAO());
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

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Batch");
    }

    // functions countResults() and loadData() are not used in batches
    @Override
    public Long countResults(Map<?, String> filters) throws DataException {
        return (long) 0;
    }

    @Override
    public List<Batch> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map<?, String> filters) {
        return new ArrayList<>();
    }

    /**
     * Remove all passed batches.
     *
     * @param batches
     *            to remove
     */
    @Override
    public void removeAll(Collection<Batch> batches) throws DataException {
        for (Batch batch : batches) {
            remove(batch);
        }
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
    public String getLabel(BatchInterface batch) {
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
    private String getNumericLabel(BatchInterface batch) {
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
