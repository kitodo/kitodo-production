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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kitodo.config.ConfigMain;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KitodoCustomLayoutStrategyTest {

    private static final String HIBERNATE_SEARCH_INDEX_NAME = "kitodo-process";
    private static final String SEARCH_INDEX_PREFIX = "testprefix-";
    private static final String SEARCH_INDEX_NAME = "kitodo-process-000001";

    @Test
    public void createInitialElasticsearchIndexNameWithoutSearchIndexPrefixDefined() {
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        String actual = strategy.createInitialElasticsearchIndexName(HIBERNATE_SEARCH_INDEX_NAME);
        assertEquals(SEARCH_INDEX_NAME, actual);
    }

    @Test
    public void createInitialElasticsearchIndexNameWithSearchIndexPrefixDefined() {
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            String actual = strategy.createInitialElasticsearchIndexName(HIBERNATE_SEARCH_INDEX_NAME);
            String expected = SEARCH_INDEX_PREFIX + SEARCH_INDEX_NAME;
            assertEquals(expected, actual);
        }
    }

    @Test
    public void createWriteAliasWithoutSearchIndexPrefixDefined() {
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        String actual = strategy.createWriteAlias(HIBERNATE_SEARCH_INDEX_NAME);
        String expected = "kitodo-process-write";
        assertEquals(expected, actual);
    }

    @Test
    public void createWriteAliasWithSearchIndexPrefixDefined() {
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            String actual = strategy.createWriteAlias(HIBERNATE_SEARCH_INDEX_NAME);
            String expected = SEARCH_INDEX_PREFIX + "kitodo-process-write";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void createReadAliasWithoutSearchIndexPrefixDefined() {
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        String actual = strategy.createReadAlias(HIBERNATE_SEARCH_INDEX_NAME);
        String expected = "kitodo-process-read";
        assertEquals(expected, actual);
    }

    @Test
    public void createReadAliasWithSearchIndexPrefixDefined() {
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            String actual = strategy.createReadAlias(HIBERNATE_SEARCH_INDEX_NAME);
            String expected = SEARCH_INDEX_PREFIX + "kitodo-process-read";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void extractUniqueKeyFromHibernateSearchIndexNameWithoutSearchIndexPrefixDefined() {
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        String actual = strategy.extractUniqueKeyFromHibernateSearchIndexName(HIBERNATE_SEARCH_INDEX_NAME);
        String expected = "kitodo-process";
        assertEquals(expected, actual);
    }

    @Test
    public void extractUniqueKeyFromHibernateSearchIndexNameWithSearchIndexPrefixDefined() {
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            String actual = strategy.extractUniqueKeyFromHibernateSearchIndexName(HIBERNATE_SEARCH_INDEX_NAME);
            String expected = SEARCH_INDEX_PREFIX + "kitodo-process";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void extractUniqueKeyFromElasticsearchIndexNameWithoutSearchIndexPrefixDefined() {
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        String actual = strategy.extractUniqueKeyFromElasticsearchIndexName(SEARCH_INDEX_NAME);
        String expected = "kitodo-process";
        assertEquals(expected, actual);
    }

    @Test
    public void extractUniqueKeyFromElasticsearchIndexNameWithSearchIndexPrefixDefined() {
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            String actual = strategy.extractUniqueKeyFromElasticsearchIndexName(SEARCH_INDEX_PREFIX + SEARCH_INDEX_NAME);
            String expected = "kitodo-process";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void extractUniqueKeyFromElasticsearchIndexNameWithInvalidIndexNameWithoutSearchIndexPrefixDefined() {
        String invalidIndexName = "kitodo-process_00001";
        KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> strategy.extractUniqueKeyFromElasticsearchIndexName(invalidIndexName)
            );
        assertEquals("Unrecognized index name: " + invalidIndexName, exception.getMessage());
    }

    @Test
    public void extractUniqueKeyFromElasticsearchIndexNameWithInvalidIndexNameWithSearchIndexPrefixDefined() {
        String invalidIndexName = SEARCH_INDEX_PREFIX + "kitodo-process_00001";
        try (MockedStatic<ConfigMain> config = mockStatic(ConfigMain.class)) {
            config.when(() -> ConfigMain.getParameter("searchindex.prefix", ""))
                .thenReturn(SEARCH_INDEX_PREFIX);
            KitodoCustomLayoutStrategy strategy = new KitodoCustomLayoutStrategy();
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> strategy.extractUniqueKeyFromElasticsearchIndexName(invalidIndexName)
            );
            assertEquals("Unrecognized index name: " + invalidIndexName, exception.getMessage());
        }
    }
}
