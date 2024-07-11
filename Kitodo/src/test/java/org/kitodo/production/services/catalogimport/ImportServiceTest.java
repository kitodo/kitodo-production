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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertTrue("'Skip hit list' should return 'true' for OAI configurations",
                ImportService.skipHitlist(oaiConfiguration, null));
    }

    /**
     * Test whether hit list is not skipped for FTP type import configurations.
     */
    @Test
    public void shouldNotSkipHitListForFtpImportConfiguration() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        Assert.assertFalse("'Skip hit list' should return 'false' for FTP configurations",
                ImportService.skipHitlist(ftpConfiguration, null));
    }

    /**
     * Test whether all visible search fields of an SRU import configurations are retrieved or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromSruImportConfigurations() {
        ImportConfiguration sruConfiguration = createImportConfiguration(SearchInterfaceType.SRU);
        sruConfiguration.setSearchFields(createSearchFields(sruConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(sruConfiguration);
        Assert.assertEquals("Wrong number of visible search fields in SRU configuration", 2, visibleSearchFields.size());
        Assert.assertTrue("Title search field should be visible", visibleSearchFields.contains(TITLE));
        Assert.assertFalse("Parent ID search field should be invisible", visibleSearchFields.contains(PARENT_ID));
    }

    /**
     * Test whether ImportService only returns valid 'recordId' search field for OAI ImportConfigurations or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromOaiImportConfiguration() {
        ImportConfiguration oaiConfiguration = createImportConfiguration(SearchInterfaceType.OAI);
        oaiConfiguration.setSearchFields(createSearchFields(oaiConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(oaiConfiguration);
        Assert.assertEquals("Wrong number of visible search fields in OAI configuration", 1, visibleSearchFields.size());
        Assert.assertFalse("Title search field is not allowed in OAI configuration", visibleSearchFields.contains(TITLE));
        Assert.assertTrue("OAI configuration should only contain 'recordId' search field", visibleSearchFields.contains(RECORD_ID_LABEL));
    }

    /**
     * Test whether ImportService only returns valid 'filename' search field for FTP ImportConfigurations or not.
     */
    @Test
    public void shouldRetrieveSearchFieldsFromFtpImportConfiguration() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        ftpConfiguration.setSearchFields(createSearchFields(ftpConfiguration));
        List<String> visibleSearchFields = ServiceManager.getImportService().getAvailableSearchFields(ftpConfiguration);
        Assert.assertEquals("Wrong number of visible search fields in FTP configuration", 1, visibleSearchFields.size());
        Assert.assertFalse("Title search field is not allowed in FTP configuration", visibleSearchFields.contains(TITLE));
        Assert.assertTrue("OAI configuration should only contain 'filename' search field", visibleSearchFields.contains(FILENAME_LABEL));
    }

    /**
     * Test whether ImportService returns 'recordId' as default search field of OAI import configuration or not.
     */
    @Test
    public void shouldRetrieveOaiDefaultSearchField() {
        ImportConfiguration oaiConfiguration = createImportConfiguration(SearchInterfaceType.OAI);
        String defaultSearchField = ImportService.getDefaultSearchField(oaiConfiguration);
        Assert.assertEquals("OAI configuration should have 'recordId' as default search field", RECORD_ID_LABEL, defaultSearchField);
    }

    /**
     * Test whether ImportService returns 'filename' as default search field of OAI import configuration or not.
     */
    @Test
    public void shouldRetrieveFtpDefaultSearchField() {
        ImportConfiguration ftpConfiguration = createImportConfiguration(SearchInterfaceType.FTP);
        String defaultSearchField = ImportService.getDefaultSearchField(ftpConfiguration);
        Assert.assertEquals("FTP configuration should have 'filename' as default search field", FILENAME_LABEL, defaultSearchField);
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
        Assert.assertEquals(String.format("Delimited search term should be %s", SEARCH_TERM), delimited, searchTermOfConfigurationWithDelimiter);
        ImportConfiguration configurationWithoutDelimiter = createImportConfiguration(SearchInterfaceType.SRU);
        String searchTermOfConfigurationWithoutDelimiter = ServiceManager.getImportService().getSearchTermWithDelimiter(SEARCH_TERM, configurationWithoutDelimiter);
        Assert.assertEquals("Search term should not be delimited", searchTermOfConfigurationWithoutDelimiter, SEARCH_TERM);
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
        Assert.assertEquals("Wrong processing end for closed task before update", date,
                process.getTasks().get(0).getProcessingEnd());
        ImportService.updateTasks(process);
        Assert.assertTrue("Processing end of CLOSED task should have been updated",
                process.getTasks().get(0).getProcessingEnd().after(date));
        Assert.assertNull("Processing end of OPEN task should remain null after update",
                process.getTasks().get(1).getProcessingEnd());
        Assert.assertNull("Processing end of LOCKED task should remain null after update",
                process.getTasks().get(2).getProcessingEnd());
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
