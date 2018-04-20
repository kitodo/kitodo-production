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

package org.kitodo.services.dataeditor;

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.net.URI;

import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class DataEditorService {

    /**
     * Reads the data of a given file in xml format. The format of that file needs
     * to be the corresponding to the one which is referenced by the data editor
     * module as data format module.
     *
     * @param xmlFileUri
     *            The path to the metadata file as URI.
     *
     */
    public void readData(URI xmlFileUri) throws IOException {
        DataEditorInterface dataEditor = loadDataEditorModule();
        dataEditor.readData(xmlFileUri);
    }

    private DataEditorInterface loadDataEditorModule() {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class,
            ConfigCore.getParameter("moduleFolder"));
        return serviceLoader.loadModule();
    }
}
