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

package org.kitodo.data.index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.search.backend.elasticsearch.index.layout.IndexLayoutStrategy;
import org.kitodo.config.ConfigMain;

/**
 * Class usage is defined in hibernate.properties file as a value of hibernate.search.backend.layout.strategy key.
 * Class is similar to the SimpleIndexLayoutStrategy implementation from Hibernate-Search but add an optional prefix
 * in front of the index names.
 */
public class KitodoCustomLayoutStrategy implements IndexLayoutStrategy {

    private String getSearchIndexPrefix() {
        return ConfigMain.getParameter("searchindex.prefix", "");
    }

    @Override
    public String createInitialElasticsearchIndexName(String hibernateSearchIndexName) {
        return getSearchIndexPrefix() + hibernateSearchIndexName + "-000001";
    }

    @Override
    public String createWriteAlias(String hibernateSearchIndexName) {
        return getSearchIndexPrefix() + hibernateSearchIndexName + "-write";
    }

    @Override
    public String createReadAlias(String hibernateSearchIndexName) {
        return getSearchIndexPrefix() + hibernateSearchIndexName + "-read";
    }

    @Override
    public String extractUniqueKeyFromHibernateSearchIndexName(String hibernateSearchIndexName) {
        return getSearchIndexPrefix() + hibernateSearchIndexName;
    }

    @Override
    public String extractUniqueKeyFromElasticsearchIndexName(String elasticsearchIndexName) {
        String patternString = getSearchIndexPrefix() + "(.*)-\\d{6}";
        Pattern uniqueKeyExtractionPattern = Pattern.compile(patternString);
        Matcher matcher = uniqueKeyExtractionPattern.matcher(elasticsearchIndexName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unrecognized index name: " + elasticsearchIndexName);
        }
        return matcher.group(1);
    }
}
