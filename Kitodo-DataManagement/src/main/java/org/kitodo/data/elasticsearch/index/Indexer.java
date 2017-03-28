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

package org.kitodo.data.elasticsearch.index;

import com.sun.research.ws.wadl.HTTPMethods;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Table;

import org.apache.http.HttpEntity;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.index.type.BaseType;

/**
 * Implementation of Elastic Search Indexer for index package.
 */
public class Indexer<T extends BaseBean, S extends BaseType> {

    private String index;

    private HTTPMethods method;

    private String type;

    public Indexer(String index, Class<?> beanClass) {
        this.setIndex(index);
        this.setType(beanClass);
    }

    /**
     * Perform request depending on given parameters of HTTPMethods.
     *
     * @param baseBean
     *            bean object which will be added or deleted from index
     * @param baseType
     *            type on which will be called method createDocument()
     * @return response from the server
     */
    @SuppressWarnings("unchecked")
    public String performSingleRequest(T baseBean, S baseType) throws DAOException, IOException {
        IndexRestClient restClient = new IndexRestClient();
        String response;

        restClient.initiateClient();
        restClient.setIndex(index);
        restClient.setType(type);

        if (method == HTTPMethods.PUT) {
            HttpEntity document = baseType.createDocument(baseBean);
            response = restClient.addDocument(document, baseBean.getId());
        } else if (method == HTTPMethods.DELETE) {
            response = restClient.deleteDocument(baseBean.getId());
        } else {
            response = "Incorrect HTTP method!";
        }

        restClient.closeClient();

        return response;
    }

    /**
     * Perform delete request depending on given id of the bean.
     *
     * @param beanId
     *            response from the server
     */
    public String performSingleRequest(Integer beanId) throws IOException {
        IndexRestClient restClient = new IndexRestClient();
        String response;

        restClient.initiateClient();
        restClient.setIndex(index);
        restClient.setType(type);

        if (method == HTTPMethods.DELETE) {
            response = restClient.deleteDocument(beanId);
        } else {
            response = "Incorrect HTTP method!";
        }

        restClient.closeClient();

        return response;
    }

    /**
     * This function is called directly by the administrator of the system.
     *
     * @return response from the server
     * @throws InterruptedException
     *             add description
     */
    @SuppressWarnings("unchecked")
    public String performMultipleRequests(List<T> baseBeans, S baseType)
            throws DAOException, IOException, InterruptedException {
        IndexRestClient restClient = new IndexRestClient();
        String response;

        restClient.initiateClient();
        restClient.setIndex(index);
        restClient.setType(type);

        if (method == HTTPMethods.PUT) {
            HashMap<Integer, HttpEntity> documents = baseType.createDocuments(baseBeans);
            response = restClient.addType(documents);
        } else if (method == HTTPMethods.DELETE) {
            response = restClient.deleteType();
        } else {
            response = "Incorrect HTTP method!";
        }

        restClient.closeClient();

        return response;
    }

    /**
     * Get name of the index.
     *
     * @return index's name
     */
    public String getIndex() {
        return index;
    }

    /**
     * Set name of the index.
     *
     * @param index
     *            name
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Get type of method which will be used during performing request.
     *
     * @return method for request
     */
    public HTTPMethods getMethod() {
        return method;
    }

    /**
     * Set up type of method which will be used during performing request.
     *
     * @param method
     *            Determines if we want to add (update) or delete document -
     *            true add, false delete
     */
    public void setMethod(HTTPMethods method) {
        this.method = method;
    }

    /**
     * Get name of the type.
     *
     * @return type's name
     */
    public String getType() {
        return type;
    }

    /**
     * Set up type name. It could be private if it would for generic type (T)
     *
     * @param beanClass
     *            beans' class
     */
    public void setType(Class<?> beanClass) {
        Table table = beanClass.getAnnotation(Table.class);
        this.type = table.name();
    }
}
