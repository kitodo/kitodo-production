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

package de.sub.goobi.helper;

import java.net.URI;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;

public class VariableReplacerTest {

    @Test
    public void shouldReplaceTitle() {
        VariableReplacer variableReplacer = new VariableReplacer(prepareProcess());

        String replaced = variableReplacer.replace("-title (processtitle) -hardcoded test");
        String expected = "-title Replacement -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplacePrefs() {
        VariableReplacer variableReplacer = new VariableReplacer(prepareProcess());

        String replaced = variableReplacer.replace("-prefs (prefs) -hardcoded test");
        String expected = "-prefs src/test/resources/rulesets/ruleset_test.xml -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceProcessPath() {
        VariableReplacer variableReplacer = new VariableReplacer(prepareProcess());

        String replaced = variableReplacer.replace("-processpath (processpath) -hardcoded test");
        String expected = "-processpath somePath -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    private Process prepareProcess() {
        Process process = new Process();
        process.setId(1);
        process.setTitle("Replacement");
        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);
        ruleset.setFile("ruleset_test.xml");
        process.setRuleset(ruleset);
        process.setProcessBaseUri(URI.create("somePath"));

        return process;
    }
}
