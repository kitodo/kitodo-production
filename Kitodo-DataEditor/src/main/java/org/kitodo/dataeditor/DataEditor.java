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

package org.kitodo.dataeditor;

import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.kitodo.api.dataeditor.DataEditorInterface;

/**
 * The main class of this module which is implementing the main interface.
 */
public class DataEditor implements DataEditorInterface {

    @Override
    public void readData(URI xmlFileUri, URI xsltFileUri) throws IOException {
        try {
            MetsKitodoReader.readAndValidateUriToMets(xmlFileUri, xsltFileUri);
        } catch (JAXBException  | TransformerException e) {
            throw new IOException("Unable to read file", e);
        }
    }
}
