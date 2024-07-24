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

package org.kitodo.export;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.xmlunit.matchers.CompareMatcher;

public class XsltHelperTest {

    private static final String META_XML = "testMetadataFileServiceTest.xml";

    @Test
    public void shouldTransformKitodoToMods() throws Exception {
        final String path = "src/test/resources/";

        ByteArrayOutputStream outputStream = XsltHelper.transformXmlByXslt(
            new StreamSource(path + "metadata/metadataFiles/" + META_XML), URI.create(path + "xslt/kitodo2mods.xsl"));

        File expected = new File(path + "metsFromKitodo.xml");
        File result = new File(path + "mets.xml");
        FileUtils.writeStringToFile(result, outputStream.toString(), StandardCharsets.UTF_8);

        assertThat(result, CompareMatcher.isIdenticalTo(expected).ignoreWhitespace());

        FileUtils.deleteQuietly(result);
    }
}
