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

package org.kitodo.api.dataeditor;

import java.io.IOException;
import java.net.URI;

/**
 * Enables the user to read and write Metadata in an editor.
 */
public interface DataEditorInterface {

    /**
     * Opens an editor to read an xmlfile.
     *
     * @param xmlFileUri
     *            The URI to the xml file to read.
     * @param xsltFileUri
     *            The URI to the xsl file for transformation of old format goobi metadata files.
     */
    void readData(URI xmlFileUri, URI xsltFileUri) throws IOException;
}
