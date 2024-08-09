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

package org.kitodo.production.forms.createprocess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.production.services.ServiceManager;

public class CreateProcessFormTest {

    @Test
    public void shouldSetChildCount() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm(Locale.LanguageRange.parse("en"));

        Ruleset ruleset = new Ruleset();
        ruleset.setFile("shouldSetChildCount.xml");
        createProcessForm.updateRulesetAndDocType(ruleset);

        Process parentProcess = new Process();
        parentProcess.setChildren(Arrays.asList(new Process(), new Process(), new Process()));
        createProcessForm.getTitleRecordLinkTab().setTitleRecordProcess(parentProcess);

        Workpiece result = new Workpiece();
        CreateProcessForm.setChildCount(parentProcess, ServiceManager.getRulesetService().openRuleset(ruleset), result);

        assertEquals("4", result.getLogicalStructure().getMetadata().parallelStream().filter(MetadataEntry.class::isInstance)
                .map(MetadataEntry.class::cast)
                .collect(Collectors.toMap(Metadata::getKey, MetadataEntry::getValue)).get("ChildCount"), "The child count was not set correctly");
    }
}
