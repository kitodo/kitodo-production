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

package org.kitodo.production.helper.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;

import org.kitodo.production.services.ServiceManager;

public class MetadataHelper {

    private MetadataHelper() {
    }

    /**
     * prüfen, ob es sich hier um eine rdf- oder um eine mets-Datei handelt.
     */
    public static String getMetaFileType(URI file) throws IOException {
        /*
         * Typen und Suchbegriffe festlegen
         */
        HashMap<String, String> types = new HashMap<>();
        types.put("metsmods", "ugh.fileformats.mets.MetsModsImportExport".toLowerCase());
        types.put("mets", "www.loc.gov/METS/".toLowerCase());
        types.put("rdf", "<RDF:RDF ".toLowerCase());
        types.put("xstream", "<ugh.dl.DigitalDocument>".toLowerCase());

        try (InputStreamReader input = new InputStreamReader(ServiceManager.getFileService().read(file),
                StandardCharsets.UTF_8); BufferedReader bufRead = new BufferedReader(input)) {
            char[] buffer = new char[200];
            while (bufRead.read(buffer) >= 0) {
                String temp = new String(buffer).toLowerCase();
                for (Entry<String, String> entry : types.entrySet()) {
                    if (temp.contains(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }
        return "-";
    }
}
