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

package org.kitodo.production.services.dataformat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class MetsService {

    private static volatile MetsService instance = null;
    private MetsXmlElementAccessInterface metsXmlElementAccess;

    /**
     * Return singleton variable of type MetsService.
     *
     * @return unique instance of MetsService
     */
    public static MetsService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (MetsService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new MetsService();
                }
            }
        }
        return instance;
    }

    private MetsService() {
        metsXmlElementAccess = (MetsXmlElementAccessInterface) new KitodoServiceLoader<>(
                MetsXmlElementAccessInterface.class).loadModule();
    }

    public Workpiece load(InputStream in) throws IOException {
        return metsXmlElementAccess.read(in);
    }

    public void save(Workpiece workpiece, OutputStream out) throws IOException {
        metsXmlElementAccess.save(workpiece, out);
    }
}
