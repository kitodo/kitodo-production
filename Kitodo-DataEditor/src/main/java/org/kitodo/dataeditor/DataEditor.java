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
import javax.xml.transform.TransformerException;

import org.kitodo.api.dataeditor.DataEditorInterface;

public class DataEditor implements DataEditorInterface {

    private MetsKitodoWrap metsKitodoWrap;

    @Override
    public void readData(URI xmlFileUri) throws IOException {
        try {
            this.metsKitodoWrap = new MetsKitodoWrap(xmlFileUri);
        } catch (JAXBException  | TransformerException e) {
            // TODO add also message for modul frontend, when it is ready!
            // For now we wrap exceptions in an IOExecption so that we dont need to
            // implement JAXB to core
            throw new IOException("Unable to read file", e);
        }
    }

    @Override
    public boolean editData(URI xmlFileUri, URI rulesetFileUri) {
        return false;
    }
}
