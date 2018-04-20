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

import java.net.URI;

import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class DataEditorService {

    public void readData(URI xmlFileUri) {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class,
            ConfigCore.getParameter("moduleFolder"));
        DataEditorInterface dataEditor = serviceLoader.loadModule();

        dataEditor.readData(xmlFileUri);
    }
}
