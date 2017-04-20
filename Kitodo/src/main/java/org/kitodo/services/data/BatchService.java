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

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BatchDAO;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.BatchType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.services.data.base.SearchService;

public class BatchService extends SearchService {

    private BatchDAO batchDao = new BatchDAO();
    private BatchType batchType = new BatchType();
    private Indexer<Batch, BatchType> indexer = new Indexer<>(Batch.class);

    /**
     * Constructor with searcher's assigning.
     */
    public BatchService() {
        super(new Searcher(Batch.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param batch
     *            object
     */
    public void save(Batch batch) throws DAOException, IOException, ResponseException {
        batchDao.save(batch);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(batch, batchType);
    }

    public Batch find(Integer id) throws DAOException {
        return batchDao.find(id);
    }

    public List<Batch> findAll() throws DAOException {
        return batchDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param batch
     *            object
     */
    public void remove(Batch batch) throws DAOException, IOException, ResponseException {
        batchDao.remove(batch);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(batch, batchType);
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param id
     *            of object
     */
    public void remove(Integer id) throws DAOException, IOException, ResponseException {
        batchDao.remove(id);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(id);
    }

    public void removeAll(Iterable<Integer> ids) throws DAOException {
        batchDao.removeAll(ids);
    }

    /**
     * The function removeAll() removes all elements that are contained in the
     * given collection from this batch. TODO: Not sure if this method is
     * needed, check it
     *
     * @param processes
     *            collection containing elements to be removed from this set
     * @return true if the set of processes was changed as a result of the call
     */
    public boolean removeAll(Batch batch, Collection<?> processes) {
        return batch.getProcesses().removeAll(processes);
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException, ResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), batchType);
    }

    /**
     * The function add() adds the given process to this batch if it is not
     * already present. TODO: Not sure if this method is needed, check it
     *
     * @param process
     *            to add
     * @return true if this batch did not already contain the specified process
     */
    public boolean add(Batch batch, Process process) {
        return batch.getProcesses().add(process);
    }

    /**
     * The function addAll() adds all of the elements in the given collection to
     * this batch if they're not already present. TODO: Not sure if this method
     * is needed, check it
     *
     * @param processes
     *            collection containing elements to be added to this set
     * @return true if this set changed as a result of the call
     */
    public boolean addAll(Batch batch, Collection<? extends Process> processes) {
        return batch.getProcesses().addAll(processes);
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
        return sequence == null || batch.getTitle() != null && batch.getTitle().contains(sequence)
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
        return batch.getTitle() != null ? batch.getTitle() : getNumericLabel(batch);
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
     * Returns the translated batch type label.
     *
     * @return the display label for the batch type
     */
    public String getTypeTranslated(Batch batch) {
        if (batch.getType() != null) {
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
            StringBuilder result = new StringBuilder(batch.getTitle() != null ? batch.getTitle().length() + 20 : 30);
            try {
                if (batch.getTitle() != null) {
                    result.append(batch.getTitle());
                } else if (batch.getId() != null) {
                    result.append(Helper.getTranslation("batch", "Batch"));
                    result.append(' ');
                    result.append(batch.getId());
                } else {
                    result.append('−');
                }
                result.append(" (");
                String extent = Helper.getTranslation("numProzesse", "{0} processes");
                String size = batch.getProcesses() != null ? Integer.toString(batch.getProcesses().size()) : "−";
                result.append(extent.replaceFirst("\\{0\\}", size));
            } catch (RuntimeException unexpected) {
                result.setLength(0);
                result.append(batch.getTitle() != null ? batch.getTitle() : batch.getId());
                result.append(" (");
                result.append(batch.getProcesses() != null ? batch.getProcesses().size() : null);
            }
            result.append(')');
            if (batch.getType() != null) {
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

    /**
     * Kitodo does not keep objects around from Hibernate session to Hibernate
     * session, so this is the working approach here.
     *
     * @see "https://developer.jboss.org/wiki/EqualsandHashCode"
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
