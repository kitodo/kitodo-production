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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.jupiter.api.Test;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.services.ServiceManager;
import org.xml.sax.SAXException;

public class VariableReplacerTest {

    int projectId = 12;

    @Test
    public void shouldReplaceTitle() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-title (processtitle) -hardcoded test");
        String expected = "-title Replacement -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplacePrefs() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-prefs (prefs) -hardcoded test");
        String expected = "-prefs src/test/resources/rulesets/ruleset_test.xml -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplaceProcessPath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-processpath (processpath) -hardcoded test");
        String expected = "-processpath 2 -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplaceProjectId() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-processpath (projectid) -hardcoded test");
        String expected = "-processpath " + projectId + " -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplaceTitleAndFilename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String testFilenameWithPath = "src/testFile.txt";
        String testFilename = "testFile.txt";
        String replaced = variableReplacer.replaceWithFilename(
                "-title (processtitle) -filename (filename) -hardcoded test", testFilenameWithPath);
        String expected = "-title Replacement -filename " + testFilename + " -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplaceFilename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String testFilenameWithPath = "src/testFile.txt";
        String testFilename = "testFile.txt";

        String replaced = variableReplacer.replaceWithFilename("-filename (filename) -hardcoded test",
                testFilenameWithPath);
        String expected = "-filename " + testFilename + " -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReplaceBasename() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String testFilename = "src/testFilename.txt";

        String replaced = variableReplacer.replaceWithFilename("-basename (basename) -hardcoded test", testFilename);
        String expected = "-basename testFilename -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }
    
    @Test
    public void shouldReplaceRelativePath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String testFilenameWithPath = "src/testFile.txt";

        String replaced = variableReplacer.replaceWithFilename("-filename (relativepath) -hardcoded test",
                testFilenameWithPath);
        String expected = "-filename " + testFilenameWithPath + " -hardcoded test";

        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldContainFile() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String toBeMatched = "src/(basename)/test.txt";

        assertTrue(variableReplacer.containsFiles(toBeMatched), "String does not match as containing file variables!");
    }

    @Test
    public void shouldNotContainFile() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String toBeMatched = "src/(projectid)/test.txt";

        assertFalse(variableReplacer.containsFiles(toBeMatched), "String should not match as containing file variables!");
    }

    @Test
    public void shouldReplaceGeneratorSource() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-filename (generatorsource) -hardcoded test");
        String expected = "-filename " + "images/Replacementscans" + " -hardcoded test";
        assertEquals(expected, replaced, "String should not match as containing file variables!");
    }

    @Test
    public void shouldReplaceGeneratorSourcePath() {
        VariableReplacer variableReplacer = new VariableReplacer(null, prepareProcess(2, "2"), null);

        String replaced = variableReplacer.replace("-filename (generatorsourcepath) -hardcoded test");
        String expected = "-filename " + KitodoConfig.getKitodoDataDirectory() + "2/" + "images/Replacementscans" + " -hardcoded test";
        assertEquals(expected, replaced, "String should not match as containing file variables!");
    }

    @Test
    public void shouldReplaceOcrdWorkflowId() {
        Process process = prepareProcess(2, "2");
        Template template = new Template();
        template.setOcrdWorkflowId("/template-ocrd-workflow.sh");
        process.setTemplate(template);

        VariableReplacer variableReplacerTemplate = new VariableReplacer(null, process, null);
        String replaced = variableReplacerTemplate.replace("-title (ocrdworkflowid) -hardcoded test");
        String expected = "-title " + template.getOcrdWorkflowId() + " -hardcoded test";
        assertEquals(expected, replaced, "String was replaced incorrectly!");

        process.setOcrdWorkflowId("/process-ocrd-workflow.sh");
        VariableReplacer variableReplacerProcess = new VariableReplacer(null, process, null);
        replaced = variableReplacerProcess.replace("-title (ocrdworkflowid) -hardcoded test");
        expected = "-title " + process.getOcrdWorkflowId() + " -hardcoded test";
        assertEquals(expected, replaced, "String was replaced incorrectly!");
    }

    @Test
    public void shouldReturnMetadataOfNewspaperIssue() throws IOException, SAXException, FileStructureValidationException {
        Process process = prepareProcess(2, "variableReplacer/newspaperIssue");
        Workpiece workpiece = ServiceManager.getProcessService().readMetadataFile(process).getWorkpiece();
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);

        String replaced = variableReplacer.replace("-language $(meta.DocLanguage) -scriptType $(meta.slub_script)");
        String expected = "-language ger -scriptType Antiqua";
        assertEquals(expected, replaced, "String should contain expected metadata!");
    }

    @Test
    public void shouldReturnMetadataOfPeriodicalVolume() throws IOException, SAXException, FileStructureValidationException {
        Process process = prepareProcess(2, "variableReplacer/periodicalVolume");
        Workpiece workpiece = ServiceManager.getProcessService().readMetadataFile(process).getWorkpiece();
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);

        String replaced = variableReplacer.replace("-language $(meta.DocLanguage) -scriptType $(meta.slub_script)");
        String expected = "-language ger -scriptType Fraktur";
        assertEquals(expected, replaced, "String should contain expected metadata!");
    }

    @Test
    public void shouldReturnMetadataOfMonograph() throws IOException, SAXException, FileStructureValidationException {
        Process process = prepareProcess(2, "variableReplacer/monograph");
        Workpiece workpiece = ServiceManager.getProcessService().readMetadataFile(process).getWorkpiece();
        VariableReplacer variableReplacer = new VariableReplacer(workpiece, process, null);

        String replaced = variableReplacer.replace("-language $(meta.DocLanguage) -scriptType $(meta.slub_script)");
        // missing meta data element will be replaced by emtpy string and a warning message appear in the log
        String expected = "-language  -scriptType keine_OCR";
        assertEquals(expected, replaced, "String should contain expected metadata!");
    }

    private Process prepareProcess(int processId, String processFolder) {
        Process process = new Process();
        process.setId(processId);
        process.setTitle("Replacement");
        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);
        ruleset.setFile("ruleset_test.xml");
        process.setRuleset(ruleset);
        process.setProcessBaseUri(URI.create(processFolder));
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
