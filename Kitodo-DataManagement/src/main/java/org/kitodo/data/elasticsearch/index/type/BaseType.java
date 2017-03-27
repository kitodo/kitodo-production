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

package org.kitodo.data.elasticsearch.index.type;

import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.elasticsearch.api.TypeInterface;

/**
 * Abstract class for Type class.
 */
public abstract class BaseType<T extends BaseBean> implements TypeInterface<T> {

    @Override
    public abstract HttpEntity createDocument(T baseBean);

    @Override
    public HashMap<Integer, HttpEntity> createDocuments(List<T> baseBeans) {
        HashMap<Integer, HttpEntity> documents = new HashMap<>();
        for (T bean : baseBeans) {
            documents.put(bean.getId(), createDocument(bean));
        }
        return documents;
    }
}
