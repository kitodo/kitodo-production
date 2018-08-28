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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

public class VersionProvider {

    private static final String XSD_VERSION_IDENTIFIER = "CURRENT VERSION:";

    public String getDataFormatVersion() {
        try {
            URL xsdFile = this.getClass().getClassLoader().getResource("xsd/kitodo.xsd");
            if (Objects.nonNull(xsdFile)) {
                String xsdString = FileUtils.readFileToString(new File(xsdFile.toURI()), StandardCharsets.UTF_8);
                int index = xsdString.indexOf(XSD_VERSION_IDENTIFIER) + XSD_VERSION_IDENTIFIER.length();
                int indexOfNextNewLine = xsdString.indexOf("\n", index);
                return xsdString.substring(index, indexOfNextNewLine).replaceAll("[^0-9?!\\.]","");
            } else {
                return "no version information";
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return "error at providing version information";
        }


    }
}
