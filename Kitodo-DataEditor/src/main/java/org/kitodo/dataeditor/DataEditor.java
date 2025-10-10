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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import jakarta.xml.bind.JAXBException;

import org.kitodo.api.dataeditor.DataEditorInterface;

/**
 * The main class of this module which is implementing the main interface.
 */
public class DataEditor implements DataEditorInterface {

    private MetsKitodoWrapper metsKitodoWrapper;

    @Override
    public void readData(URI xmlFileUri, URI xsltFileUri) throws IOException {
        try {
            this.metsKitodoWrapper = new MetsKitodoWrapper(xmlFileUri, xsltFileUri);
        } catch (JAXBException  | TransformerException | DatatypeConfigurationException e) {
            // TODO add also message for module frontend, when it is ready!
            // For now we wrap exceptions in an IOException so that we don't need to
            // implement JAXB to core
            throw new IOException("Unable to read file", e);
        }
    }

    @Override
    public boolean editData(URI xmlFileUri, URI rulesetFileUri) {
        return false;
    }
}
