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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getDriver;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SearchResultPage extends Page<SearchResultPage> {

    private static final String SEARCH_RESULT_TAB_VIEW = "searchResultTabView";
    private static final String SEARCH_RESULT_FORM = SEARCH_RESULT_TAB_VIEW + ":searchResultForm";
    private static final String SEARCH_RESULT_TABLE = SEARCH_RESULT_FORM + ":searchResultTable";
    private static final String SEARCH_RESULT_TABLE_TITLE_COLUMN = SEARCH_RESULT_TABLE + ":titleColumn";
    private static final String FILTER_CONFIGURATION = "configureFilters";

    @SuppressWarnings("unused")
    @FindBy(id = SEARCH_RESULT_TABLE + "_data")
    private WebElement searchResultTable;

    @SuppressWarnings("unused")
    @FindBy(id = SEARCH_RESULT_TABLE_TITLE_COLUMN)
    private WebElement searchResultTableTitleColumn;

    @SuppressWarnings("unused")
    @FindBy(id = FILTER_CONFIGURATION + ":projectfilter")
    private WebElement projectsDropdown;

    @SuppressWarnings("unused")
    @FindBy(id = FILTER_CONFIGURATION + ":projectfilter_items")
    private WebElement projectsForFiltering;

    private static final String WAIT_FOR_TITLE_COLUMN_SORT = "Wait for title column sorting";

    public SearchResultPage() {
        super("searchResult.jsf");
    }

    @Override
    public SearchResultPage goTo() {
        return null;
    }

    public int getNumberOfResults() {
        List<String> tableDataByColumn = getTableDataByColumn(searchResultTable, 0);
        if (tableDataByColumn.contains("No records found.")) {
            return 0;
        }
        return getRowsOfTable(searchResultTable).size();
    }

    public String getProjectsForFilter() {
        projectsDropdown.click();
        return projectsForFiltering.getText();
    }

    public void filterByProject() {
        WebElement projectFilter = getDriver().findElementByPartialLinkText("First");
        projectFilter.click();
    }

    /**
     * Return the process title of the first search result in the table of search results.
     * 
     * @return the title or null if no results are found
     */
    public String getFirstSearchResultProcessTitle() {
        List<String> tableDataByColumn = getTableDataByColumn(searchResultTable, 3);
        if (tableDataByColumn.isEmpty() || tableDataByColumn.contains("No records found.")) {
            return null;
        }
        return tableDataByColumn.get(0);
    }

    /**
     * Clicks the header of the title column of the search result table in order to 
     * trigger sorting by title.
     */
    public void clickTitleColumnForSorting() {
        // remember aria-sort attribute of th-tag of title column
        String previousAriaSort = searchResultTableTitleColumn.getAttribute("aria-sort");

        // click title th-tag to trigger sorting
        searchResultTableTitleColumn.click();

        // wait for the sorting to be applied (which requires ajax request to backend)
        await(WAIT_FOR_TITLE_COLUMN_SORT)
            .pollDelay(100, TimeUnit.MILLISECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .ignoreExceptions()
            .until(() -> !searchResultTableTitleColumn.getAttribute("aria-sort").equals(previousAriaSort));
    }
}
