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

package org.kitodo.production.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;

public class VariableReplacerTest {

    int projectId = 12;

    @Test
    public void shouldReplaceTitle() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-title (processtitle) -hardcoded test");
        String expected = "-title Replacement -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplacePrefs() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-prefs (prefs) -hardcoded test");
        String expected = "-prefs src/test/resources/rulesets/ruleset_test.xml -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceProcessPath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-processpath (processpath) -hardcoded test");
        String expected = "-processpath 2 -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceProjectId() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-processpath (projectid) -hardcoded test");
        String expected = "-processpath " + projectId + " -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceTitleAndFilename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String testFilenameWithPath = "src/testFile.txt";
        String testFilename = "testFile.txt";
        String replaced = variableReplacer.replaceWithFilename(
                "-title (processtitle) -filename (filename) -hardcoded test", testFilenameWithPath);
        String expected = "-title Replacement -filename " + testFilename + " -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceFilename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String testFilenameWithPath = "src/testFile.txt";
        String testFilename = "testFile.txt";

        String replaced = variableReplacer.replaceWithFilename("-filename (filename) -hardcoded test",
                testFilenameWithPath);
        String expected = "-filename " + testFilename + " -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldReplaceBasename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String testFilename = "src/testFilename.txt";

        String replaced = variableReplacer.replaceWithFilename("-basename (basename) -hardcoded test", testFilename);
        String expected = "-basename testFilename -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }
    
    @Test
    public void shouldReplaceRelativePath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String testFilenameWithPath = "src/testFile.txt";

        String replaced = variableReplacer.replaceWithFilename("-filename (relativepath) -hardcoded test",
                testFilenameWithPath);
        String expected = "-filename " + testFilenameWithPath + " -hardcoded test";

        assertEquals("String was replaced incorrectly!", expected, replaced);
    }

    @Test
    public void shouldContainFile() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String toBeMatched = "src/(basename)/test.txt";

        assertTrue("String does not match as containing file variables!", variableReplacer.containsFiles(toBeMatched));
    }

    @Test
    public void shouldNotContainFile() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String toBeMatched = "src/(projectid)/test.txt";

        assertFalse("String should not match as containing file variables!",
                variableReplacer.containsFiles(toBeMatched));
    }

    @Test
    public void shouldReplaceGeneratorSource() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-filename (generatorsource) -hardcoded test");
        String expected = "-filename " + "images/Replacementscans" + " -hardcoded test";
        assertEquals("String should not match as containing file variables!", expected,
                replaced);
    }

    @Test
    public void shouldReplaceGeneratorSourcePath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(), null);

        String replaced = variableReplacer.replace("-filename (generatorsourcepath) -hardcoded test");
        String expected = "-filename " + KitodoConfig.getKitodoDataDirectory() + "2/" + "images/Replacementscans" + " -hardcoded test";
        assertEquals("String should not match as containing file variables!", expected,
                replaced);
    }

    private Process prepareProcess() {
        Process process = new Process();
        process.setId(2);
        process.setTitle("Replacement");
        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);
        ruleset.setFile("ruleset_test.xml");
        process.setRuleset(ruleset);
        process.setProcessBaseUri(URI.create("2"));
        Folder scansFolder = new Folder();
        scansFolder.setFileGroup("SOURCE");
        scansFolder.setPath("images/(processtitle)scans");
        Project project = new Project();
        project.setId(projectId);
        process.setProject(project);
        scansFolder.setProject(project);
        project.getFolders().add(scansFolder);
        project.setGeneratorSource(scansFolder);

        return process;
    }
}
