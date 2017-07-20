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

package org.kitodo.docket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.kitodo.api.docket.DocketData;
import org.kitodo.api.docket.DocketInterface;

public class Docket implements DocketInterface {

    @Override
    public File generateDocket(DocketData docketData, URI xslFileUri) throws IOException {
        ExportDocket exportDocket = new ExportDocket();

        File file = new File("docket.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        exportDocket.startExport(docketData, fileOutputStream, new File(xslFileUri));
        fileOutputStream.flush();
        fileOutputStream.close();

        return file;
    }

    @Override
    public File generateMultipleDockets(Collection<DocketData> docketData, URI xslFileUri) throws IOException {
        ExportDocket exportDocket = new ExportDocket();

        File file = new File("docket_multipage.pdf");
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        exportDocket.startExport(docketData, fileOutputStream, new File(xslFileUri));
        fileOutputStream.flush();
        fileOutputStream.close();

        return file;
    }
}
