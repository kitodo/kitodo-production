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

package org.kitodo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.jupiter.api.Test;

class OPACConfigTest {

    @Test
    void getKitodoOpacConfigurationReturnsConfigurationObject() throws ConfigurationException {
        assertNotNull(OPACConfig.getKitodoOpacConfiguration());
        assertInstanceOf(XMLConfiguration.class, OPACConfig.getKitodoOpacConfiguration());
    }

    @Test
    void getCatalogsIsReturnListOfCatalogs() {
        List<String> catalogs = OPACConfig.getCatalogs();
        assertFalse(catalogs.isEmpty());
        assertEquals(3, catalogs.size());
    }

    @Test
    void getCatalogIsReturningASpecificCatalog() {
        HierarchicalConfiguration<ImmutableNode> gbvCatalog = OPACConfig.getCatalog("GBV");
        assertNotNull(gbvCatalog);
        assertEquals("Gemeinsamer Bibliotheksverbund", OPACConfig.getOPACDescription("GBV"));
    }

    @Test
    void getDefaultSearchFieldReturnsCorrectValue() {
        String current = OPACConfig.getDefaultSearchField("GBV");
        assertNotNull(current);
        assertEquals("PPN", current);
    }

    @Test
    void getXsltMappingFilesIsReturnRightList() {
        List<String> mappingFiles = OPACConfig.getXsltMappingFiles("Kalliope");
        assertFalse(mappingFiles.isEmpty());
        assertEquals(1, mappingFiles.size());
        assertEquals("mods2kitodo.xsl", mappingFiles.getFirst());
    }

    @Test
    void isPrestructuredImportIsReturingCorrectValue() {
        assertFalse(OPACConfig.isPrestructuredImport("K10Plus"));
    }

    @Test
    void getXsltMappingFilesIsThrowingExceptionOnMissingMappingFilesEntry() {
        Exception exception = assertThrows(ConfigurationRuntimeException.class,
                () -> OPACConfig.getXsltMappingFiles("K10Plus")
            );

        String expectedMessage = "Passed in key must select exactly one node (found 0): mappingFiles";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}
