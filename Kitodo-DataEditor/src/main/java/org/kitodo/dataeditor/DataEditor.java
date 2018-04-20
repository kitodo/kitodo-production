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

import java.net.URI;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.DataEditorInterface;

public class DataEditor implements DataEditorInterface {

    private MetsKitodoWrap metsKitodoWrap;
    private static final Logger logger = LogManager.getLogger(DataEditor.class);

    @Override
    public void readData(URI xmlFileUri) {
        try {
            this.metsKitodoWrap = new MetsKitodoWrap(xmlFileUri);
        } catch (JAXBException | XMLStreamException e) {
            // TODO add also message for frontend, when it is ready!
            logger.error(e);
        }
    }

    @Override
    public boolean editData(URI xmlFileUri, URI rulesetFileUri) {
        return false;
    }
}
