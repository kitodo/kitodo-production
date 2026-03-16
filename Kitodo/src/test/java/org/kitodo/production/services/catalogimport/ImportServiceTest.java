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

package org.kitodo.production.services.catalogimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.test.utils.TestConstants;

public class ImportServiceTest {

    private static final String TITLE = "Title";
    private static final String RECORD_ID = "ID";
    private static final String PARENT_ID = "Parent ID";
    private static final String RECORD_ID_LABEL = Helper.getTranslation("recordId");
    private static final String FILENAME_LABEL = Helper.getTranslation("filename");
    private static final String DELIMITER = "'";
    private static final String SEARCH_TERM = "Hamburg";

    /**
     * Test whether hit list is properly skipped for OAI type import configurations.
     */
    @Test
    public void shouldSkipHitListForOaiImportConfiguration() {
        ImportConfiguration oaiConfiguration = createImportConfiguration(SearchInterfaceType.OAI);
        assertTrue(ImportService.skipHitlist(oaiConfiguration, null), "'Skip hit list' should return 'true' for OAI configurations");
    }

    /**
     * Test whether hit list is not skipped for FTP type import configurations.
     */
    @Test
    public void shouldNotSkipHitListForFtpImportConfiguration() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        assertFalse(ImportService.skipHitlist(ftpConfiguration, null), "'Skip hit list' should return 'false' for FTP configurations");
    }

    /**
     * Test whether all visible search fields of an SRU import configurations are retrieved or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromSruImportConfigurations() {
        ImportConfiguration sruConfiguration = createImportConfiguration(SearchInterfaceType.SRU);
        sruConfiguration.setSearchFields(createSearchFields(sruConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(sruConfiguration);
        assertEquals(2, visibleSearchFields.size(), "Wrong number of visible search fields in SRU configuration");
        assertTrue(visibleSearchFields.contains(TITLE), "Title search field should be visible");
        assertFalse(visibleSearchFields.contains(PARENT_ID), "Parent ID search field should be invisible");
    }

    /**
     * Test whether ImportService only returns valid 'recordId' search field for OAI ImportConfigurations or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromOaiImportConfiguration() {
        ImportConfiguration oaiConfiguration = createImportConfiguration(SearchInterfaceType.OAI);
        oaiConfiguration.setSearchFields(createSearchFields(oaiConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(oaiConfiguration);
        assertEquals(1, visibleSearchFields.size(), "Wrong number of visible search fields in OAI configuration");
        assertFalse(visibleSearchFields.contains(TITLE), "Title search field is not allowed in OAI configuration");
        assertTrue(visibleSearchFields.contains(RECORD_ID_LABEL), "OAI configuration should only contain 'recordId' search field");
    }

    /**
     * Test whether ImportService only returns valid 'filename' search field for FTP ImportConfigurations or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromFtpImportConfiguration() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        ftpConfiguration.setSearchFields(createSearchFields(ftpConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(ftpConfiguration);
        assertEquals(1, visibleSearchFields.size(), "Wrong number of visible search fields in FTP configuration");
        assertFalse(visibleSearchFields.contains(TITLE), "Title search field is not allowed in FTP configuration");
        assertTrue(visibleSearchFields.contains(FILENAME_LABEL), "OAI configuration should only contain 'filename' search field");
    }

    /**
     * Test whether ImportService returns 'recordId' as default search field of OAI import configuration or not.
     */
    @Test
    public void shouldRetrieveOaiDefaultSearchField() {
        ImportConfiguration oaiConfiguration = createImportConfiguration(SearchInterfaceType.OAI);
        String defaultSearchField = ImportService.getDefaultSearchField(oaiConfiguration);
        assertEquals(RECORD_ID_LABEL, defaultSearchField, "OAI configuration should have 'recordId' as default search field");
    }

    /**
     * Test whether ImportService returns 'filename' as default search field of OAI import configuration or not.
     */
    @Test
    public void shouldRetrieveFtpDefaultSearchField() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        String defaultSearchField = ImportService.getDefaultSearchField(ftpConfiguration);
        assertEquals(FILENAME_LABEL, defaultSearchField, "FTP configuration should have 'filename' as default search field");
    }

    /**
     * Test whether search term is properly delimited when delimiter is configured in ImportConfiguration.
     */
    @Test
    public void shouldReturnSearchTermWithDelimiter() {
        ImportConfiguration configurationWithDelimiter = createImportConfiguration(SearchInterfaceType.SRU);
        configurationWithDelimiter.setQueryDelimiter(DELIMITER);
        String delimited = DELIMITER + SEARCH_TERM + DELIMITER;
        String searchTermOfConfigurationWithDelimiter = ServiceManager.getImportService().getSearchTermWithDelimiter(SEARCH_TERM, configurationWithDelimiter);
        assertEquals(delimited, searchTermOfConfigurationWithDelimiter, String.format("Delimited search term should be %s", SEARCH_TERM));
        ImportConfiguration configurationWithoutDelimiter = createImportConfiguration(SearchInterfaceType.SRU);
        String searchTermOfConfigurationWithoutDelimiter = ServiceManager.getImportService().getSearchTermWithDelimiter(SEARCH_TERM, configurationWithoutDelimiter);
        assertEquals(SEARCH_TERM, searchTermOfConfigurationWithoutDelimiter, "Search term should not be delimited");
    }

    /**
     * Tests whether processing end dates of closed tasks in a given process are updated correctly or not.
     */
    @Test
    public void shouldUpdateTasks() {
        Process process = new Process();
        List<Task> tasks = new LinkedList<>();
        Task firstTask = new Task();
        Task secondTask = new Task();
        Task thirdTask = new Task();
        Date date = new Date(System.currentTimeMillis() - 5000L);
        firstTask.setProcessingEnd(date);
        firstTask.setProcessingStatus(TaskStatus.DONE);
        secondTask.setProcessingStatus(TaskStatus.OPEN);
        thirdTask.setProcessingStatus(TaskStatus.LOCKED);
        tasks.add(firstTask);
        tasks.add(secondTask);
        tasks.add(thirdTask);
        process.setTasks(tasks);
        assertEquals(date, process.getTasks().getFirst().getProcessingEnd(), "Wrong processing end for closed task before update");
        ImportService.updateTasks(process);
        assertTrue(process.getTasks().get(0).getProcessingEnd().after(date), "Processing end of CLOSED task should have been updated");
        assertNull(process.getTasks().get(1).getProcessingEnd(), "Processing end of OPEN task should remain null after update");
        assertNull(process.getTasks().get(2).getProcessingEnd(), "Processing end of LOCKED task should remain null after update");
    }

    /**
     * Create and return an ImportConfiguration with configuration type `OPAC_SEARCH` and given search interface type.
     *
     * @param interfaceType as SearchInterfaceType
     * @return ImportConfiguration of given SearchInterfaceType
     */
    private ImportConfiguration createImportConfiguration(SearchInterfaceType interfaceType) {
        String interfaceTypeName = interfaceType.name();
        ImportConfiguration genericConfiguration = new ImportConfiguration();
        genericConfiguration.setTitle(interfaceTypeName + " example configuration");
        genericConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.name());
        genericConfiguration.setInterfaceType(interfaceTypeName);
        return genericConfiguration;
    }

    private List<SearchField> createSearchFields(ImportConfiguration importConfiguration) {
        List<SearchField> SearchFields = new ArrayList<>();
        SearchFields.add(addSearchField(TITLE, "ead.tit", importConfiguration, true));
        SearchFields.add(addSearchField(RECORD_ID, TestConstants.EAD_ID, importConfiguration, true));
        SearchFields.add(addSearchField(PARENT_ID, TestConstants.EAD_PARENT_ID, importConfiguration, false));
        importConfiguration.setSearchFields(SearchFields);
        return SearchFields;
    }

    private SearchField addSearchField(String label, String value, ImportConfiguration config, boolean displayed) {
        SearchField searchField = new SearchField();
        searchField.setValue(value);
        searchField.setLabel(label);
        searchField.setDisplayed(displayed);
        searchField.setImportConfiguration(config);
        return searchField;
    }
}
