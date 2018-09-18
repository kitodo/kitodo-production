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

package org.kitodo.dataformat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataFormatVersionProvider {

    private static final Logger logger = LogManager.getLogger(DataFormatVersionProvider.class);
    private static final String XSD_VERSION_IDENTIFIER = "CURRENT VERSION:";

    /**
     * Gets the version of current used data format by reading the xsd file at
     * resources.
     * 
     * @return The version of data format.
     */
    public String getDataFormatVersion() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("xsd/kitodo.xsd")) {
            String xsdString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            int index = xsdString.indexOf(XSD_VERSION_IDENTIFIER) + XSD_VERSION_IDENTIFIER.length();
            int indexOfNextNewLine = xsdString.indexOf("\n", index);
            return xsdString.substring(index, indexOfNextNewLine).replaceAll("[^0-9?!\\.]", "");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return "error at providing version information";

        }
    }
}
