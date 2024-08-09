/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.elasticsearch.api;

import java.util.List;
import java.util.Map;

/**
 * Interface for serving types which are added to the index.
 *
 * <p>
 * Note: MySQL -> Databases -> Tables -> Columns/Rows ElasticSearch -> Indices
 * -> Types -> Documents with Properties
 */
public interface TypeInterface<T> {

    Map<String, Object> createDocument(T baseIndexedBean);

    Map<Integer, Map<String, Object>> createDocuments(List<T> baseIndexedBeans);
}
