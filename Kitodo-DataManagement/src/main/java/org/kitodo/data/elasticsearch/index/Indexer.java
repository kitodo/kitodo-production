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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.kitodo.data.database.beans.BaseIndexedBean;
import org.kitodo.data.elasticsearch.Index;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.type.BaseType;
import org.kitodo.data.exceptions.DataException;

/**
 * Implementation of ElasticSearch Indexer for index package.
 */
public class Indexer<T extends BaseIndexedBean, S extends BaseType> extends Index {

    private String method;
    private static final String INCORRECT_HTTP = "Incorrect HTTP method!";

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
     * Perform request depending on given parameters of HTTP Method.
     *
     * @param baseIndexedBean
     *            bean object which will be added or deleted from index
     * @param baseType
     *            type on which will be called method createDocument()
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    @SuppressWarnings("unchecked")
    public void performSingleRequest(T baseIndexedBean, S baseType, boolean forceRefresh)
            throws CustomResponseException, DataException, IOException {
        IndexRestClient restClient = initiateRestClient();

        if (method.equals(HttpMethod.PUT)) {
            Map<String, Object> document = baseType.createDocument(baseIndexedBean);
            restClient.addDocument(this.type, document, baseIndexedBean.getId(), forceRefresh);
        } else if (method.equals(HttpMethod.DELETE)) {
            restClient.deleteDocument(this.type, baseIndexedBean.getId(), forceRefresh);
        } else {
            throw new CustomResponseException(INCORRECT_HTTP);
        }

    }

    /**
     * Perform delete request depending on given id of the bean.
     *
     * @param beanId
     *            response from the server
     * @param forceRefresh
     *            force index refresh - if true, time of execution is longer but
     *            object is right after that available for display
     */
    public void performSingleRequest(Integer beanId, boolean forceRefresh) throws CustomResponseException, DataException {
        IndexRestClient restClient = initiateRestClient();

        if (method.equals(HttpMethod.DELETE)) {
            restClient.deleteDocument(this.type, beanId, forceRefresh);
        } else {
            throw new CustomResponseException(INCORRECT_HTTP);
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
    public void performMultipleRequests(List<T> baseIndexedBeans, S baseType, boolean async) throws CustomResponseException {
        IndexRestClient restClient = initiateRestClient();

        if (method.equals(HttpMethod.PUT)) {
            Map<Integer, Map<String, Object>> documents = baseType.createDocuments(baseIndexedBeans);
            if (async) {
                restClient.addTypeAsync(this.type, documents);
            } else {
                restClient.addTypeSync(this.type, documents);
            }
        } else {
            throw new CustomResponseException(INCORRECT_HTTP);
        }
    }

    private IndexRestClient initiateRestClient() {
        IndexRestClient restClient = IndexRestClient.getInstance();
        restClient.setIndexBase(index);
        return restClient;
    }

    /**
     * Get type of method which will be used during performing request.
     *
     * @return method for request
     */
    public String getMethod() {
        return method;
    }

    /**
     * Set up type of method which will be used during performing request.
     *
     * @param method
     *            Determines if we want to add (update) or delete document - true
     *            add, false delete
     */
    public void setMethod(String method) {
        this.method = method;
    }
}
