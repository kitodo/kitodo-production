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

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.production.services.data.ImportService;

public class ImportServiceTest {

    /**
     * Test whether hit list is properly skipped for OAI type import configurations.
     */
    @Test
    public void shouldSkipHitListForOaiImportConfiguration() {
        ImportConfiguration ftpConfiguration = createOaiImportConfiguration();
        Assert.assertTrue("'Skip hit list' should return 'true' for OAI configurations",
                ImportService.skipHitlist(ftpConfiguration, null));
    }

    /**
     * Create and return an ImportConfiguration with configuration type `OPAC_SEARCH` and search interface type 'OAI'.
     */
    private ImportConfiguration createOaiImportConfiguration() {
        ImportConfiguration genericFtpConfiguration = new ImportConfiguration();
        genericFtpConfiguration.setTitle("OAI example configuration");
        genericFtpConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.name());
        genericFtpConfiguration.setInterfaceType(SearchInterfaceType.OAI.name());
        return genericFtpConfiguration;
    }
}
