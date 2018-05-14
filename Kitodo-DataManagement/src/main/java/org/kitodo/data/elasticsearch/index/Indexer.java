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
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.elasticsearch.Index;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.type.BaseType;

/**
 * Implementation of Elastic Search Indexer for index package.
 */
public class Indexer<T extends BaseIndexedBean, S extends BaseType> extends Index {

    private HTTPMethods method;
    private static final Logger logger = LogManager.getLogger(Indexer.class);

    /**
     * Constructor for indexer with type names equal to table names.
     *
     * @param beanClass
     *            as Class
     */
    public Indexer(Class<?> beanClass) {
        super(beanClass);
    }

    /**
     * Constructor for indexer with type names not equal to table names.
     *
     * @param type
     *            as String
     */
    public Indexer(String type) {
        super(type);
    }

    /**
     * Perform request depending on given parameters of HTTPMethods.
     *
     * @param baseIndexedBean
     *            bean object which will be added or deleted from index
     * @param baseType
     *            type on which will be called method createDocument()
     */
    @SuppressWarnings("unchecked")
    public void performSingleRequest(T baseIndexedBean, S baseType) throws IOException, CustomResponseException {
        IndexRestClient restClient = initiateRestClient();

        if (method == HTTPMethods.PUT) {
            HttpEntity document = baseType.createDocument(baseIndexedBean);
            restClient.addDocument(document, baseIndexedBean.getId());
        } else if (method == HTTPMethods.DELETE) {
            restClient.deleteDocument(baseIndexedBean.getId());
        } else {
            throw new CustomResponseException("Incorrect HTTP method!");
        }

    }

    /**
     * Perform delete request depending on given id of the bean.
     *
     * @param beanId
     *            response from the server
     */
    public void performSingleRequest(Integer beanId) throws IOException, CustomResponseException {
        IndexRestClient restClient = initiateRestClient();

        if (method == HTTPMethods.DELETE) {
            restClient.deleteDocument(beanId);
        } else {
            throw new CustomResponseException("Incorrect HTTP method!");
        }
    }

    /**
     * This function is called directly by the administrator of the system.
     *
     * @param baseIndexedBeans
     *            list of bean objects which will be added to index
     * @param baseType
     *            type on which will be called method createDocument()
     */
    @SuppressWarnings("unchecked")
    public void performMultipleRequests(List<T> baseIndexedBeans, S baseType)
            throws InterruptedException, CustomResponseException {
        IndexRestClient restClient = initiateRestClient();

        if (method == HTTPMethods.PUT) {
            Map<Integer, HttpEntity> documents = baseType.createDocuments(baseIndexedBeans);
            restClient.addType(documents);
        } else {
            throw new CustomResponseException("Incorrect HTTP method!");
        }
    }

    private IndexRestClient initiateRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndex(index);
        restClient.setType(type);
        return restClient;
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
}
