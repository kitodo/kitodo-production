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

package org.kitodo.production.lugh;

import java.io.*;
import java.net.*;
import java.util.*;

import org.goobi.production.constants.Parameters;
import org.kitodo.production.lugh.ld.*;

import com.hp.hpl.jena.rdf.model.*;

import de.sub.goobi.config.ConfigMain;
import ugh.dl.*;

/**
 * Utilities to access authority files.
 *
 * @see "https://en.wikipedia.org/wiki/Authority_control"
 *
 * @author Matthias Ronge
 */
public class AuthorityFileUtil {

    /**
     * Downloads an Authority record.
     * 
     * @param recordURI
     *            record URI, like {@code http://d-nb.info/gnd/118514768}
     * @return a node with the data retrieved
     * @throws IOException
     *             if an I/O exception occurs
     * @throws URISyntaxException
     *             if the URL created is not formatted strictly according to to
     *             RFC2396 and cannot be converted to a URI.
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no head node in the
     *             result, {@link AmbiguousDataException} if there are several
     */
    public static Node downloadAuthorityRecord(String recordURI)
            throws IOException, URISyntaxException, LinkedDataException {
        String namespace = Namespace.namespaceOf(recordURI);
        String prefix = ConfigMain.getParameterMap(Parameters.NAMESPACE_MAP, false, false).get(namespace);
        String parameter = Parameters.AUTHORITY_DATA_URL_TAIL.replaceFirst("\\{0\\}",
                prefix != null ? prefix : namespace);
        String urlTail = ConfigMain.getParameter(parameter, null);
        URL dataURL = new URL(urlTail == null ? recordURI : recordURI.concat(urlTail));
        Model apacheJenaModel = ModelFactory.createDefaultModel();
        try (InputStream input = dataURL.openStream()) {
            apacheJenaModel.read(input, dataURL.toURI().toASCIIString());
        }
        return Result.createFrom(apacheJenaModel, false).node();
    }

    /**
     * Returns the record URI from this meta-data group.
     * 
     * @param metaDataGroup
     */
    public static String getRecordURI(MetadataGroup metaDataGroup) {
        String[] valueMetaData = ConfigMain.getStringArrayParameter(Parameters.AUTHORITY_RECORD_URI_FIELD);
        Set<String> valueMetaDataSet = new HashSet<>(Arrays.asList(valueMetaData));
        for (Metadata metaDatum : metaDataGroup.getMetadataList()) {
            String name = metaDatum.getType() != null ? metaDatum.getType().getName() : null;
            if (valueMetaDataSet.contains(name)) {
                return metaDatum.getValue();
            }
        }
        return null;
    }
}
