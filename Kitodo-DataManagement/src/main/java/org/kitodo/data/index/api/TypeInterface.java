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

package org.kitodo.data.index.api;

import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;

import org.kitodo.data.database.beans.BaseBean;

/**
 * Interface for serving types which are added to the index.
 *
 * <p>Note:
 * MySQL         -> Databases -> Tables -> Columns/Rows
 * ElasticSearch -> Indices   -> Types  -> Documents with Properties</p>
 */
public interface TypeInterface {

    HttpEntity createDocument(BaseBean baseBean);

    HashMap<Integer, HttpEntity> createDocuments(List<BaseBean> baseBeans);
}
