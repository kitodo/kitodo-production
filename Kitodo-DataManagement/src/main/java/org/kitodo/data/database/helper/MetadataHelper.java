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

package org.kitodo.data.database.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class created for use in org.kitodo.persistence.apache.ProcessObject.
 */
public class MetadataHelper {

    /**
     * This method is copied from de.sub.goobi.metadaten.MetadatenHelper. It
     * check whether this is a rdf or mets file.
     *
     * @return meta file type
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

        try (InputStreamReader input = new InputStreamReader(file.toURL().openStream(), StandardCharsets.UTF_8);
                BufferedReader bufRead = new BufferedReader(input);) {
            char[] buffer = new char[200];
            while ((bufRead.read(buffer)) >= 0) {
                String temp = new String(buffer).toLowerCase();
                Iterator<Map.Entry<String, String>> i = types.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<String, String> entry = i.next();
                    if (temp.contains(entry.getValue())) {
                        return entry.getKey();
                    }
                }
            }
        }

        return "-";
    }
}
