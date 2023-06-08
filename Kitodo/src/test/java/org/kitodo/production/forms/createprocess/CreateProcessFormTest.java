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

package org.kitodo.production.forms.createprocess;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.junit.Test;
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

        assertEquals("The child count was not set correctly", "4",
            result.getLogicalStructure().getMetadata().parallelStream().filter(MetadataEntry.class::isInstance)
                    .map(MetadataEntry.class::cast)
                    .collect(Collectors.toMap(Metadata::getKey, MetadataEntry::getValue)).get("ChildCount"));
    }
}
