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

import de.sub.goobi.config.Parameters;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.config.Config;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class DataEditorService {

    /**
     * Reads the data of a given file in xml format. The format of that file
     * needs to be the corresponding to the one which is referenced by the data
     * editor module as data format module.
     *
     * @param xmlFileUri
     *            The path to the metadata file as URI.
     */
    public void readData(URI xmlFileUri) throws IOException {
        DataEditorInterface dataEditor = loadDataEditorModule();
        URI xsltFile = getXsltFileFromConfig();
        dataEditor.readData(xmlFileUri, xsltFile);
    }

    private DataEditorInterface loadDataEditorModule() {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class);
        return serviceLoader.loadModule();
    }

    private URI getXsltFileFromConfig() {
        String path = getXsltFolder();
        String file = Config.getParameter(Parameters.XSLT_FILENAME_METADATA_TRANSFORMATION);
        return Paths.get(path + file).toUri();
    }

    private String getXsltFolder() {
        return Config.getParameter(Parameters.DIR_XSLT);
    }
}
