/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
        File file = File.createTempFile("docket.pdf", ".tmp");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            new ExportDocket(new File(xslFileUri)).startExport(docketData, fileOutputStream);
        }

        return file;
    }

    @Override
    public File generateMultipleDockets(Collection<DocketData> docketData, URI xslFileUri) throws IOException {
        File file = File.createTempFile("docket_multipage.pdf", ".tmp");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            new ExportDocket(new File(xslFileUri)).startExport(docketData, fileOutputStream);
        }

        return file;
    }

    @Override
    public void exportXmlLog(DocketData docketData, String destination) throws IOException {
        File file = new File(destination);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            new ExportXmlLog(docketData).startExport(fileOutputStream);
        }
    }
}
