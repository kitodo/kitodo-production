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

package org.kitodo.services;

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.List;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.DocketType;

public class DocketService {
    private DocketDAO docketDao = new DocketDAO();
    private DocketType docketType = new DocketType();
    private Indexer<Docket, DocketType> indexer = new Indexer<>("kitodo", Docket.class);

    public Docket find(Integer id) throws DAOException {
        return docketDao.find(id);
    }

    public List<Docket> findAll() throws DAOException {
        return docketDao.findAll();
    }

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param docket object
     */
    public void save(Docket docket) throws DAOException, IOException {
        docketDao.save(docket);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(docket, docketType);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param docket object
     */
    public void remove(Docket docket) throws DAOException, IOException {
        docketDao.remove(docket);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(docket, docketType);
    }

    public List<Docket> search(String query) throws DAOException {
        return docketDao.search(query);
    }

    public Long count(String query) throws DAOException {
        return docketDao.count(query);
    }
}
