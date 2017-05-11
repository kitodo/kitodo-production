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

package org.kitodo.services.data.base;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.json.simple.parser.ParseException;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;
import org.kitodo.data.elasticsearch.search.Searcher;

/**
 * Class for implementing methods used by service classes which search for title
 * in ElasticSearch index.
 */
public abstract class TitleSearchService<T extends BaseBean> extends SearchService<T> {

    public TitleSearchService(Searcher searcher) {
        super(searcher);
    }

    /**
     * Find object matching to given title.
     *
     * @param title
     *            of the searched process
     * @param contains
     *            if true result should contain given plain text, if false it
     *            should not contain
     * @return list of search result
     */
    public List<SearchResult> findByTitle(String title, boolean contains)
            throws CustomResponseException, IOException, ParseException {
        QueryBuilder query = createSimpleQuery("title", title, contains, Operator.AND);
        return searcher.findDocuments(query.toString());
    }
}
