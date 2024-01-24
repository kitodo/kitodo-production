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

import java.util.Locale;

import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.process.TitleGenerator;
import org.kitodo.test.utils.ProcessTestUtils;
import org.primefaces.model.DefaultTreeNode;

public class ProcessDataTabTest {

    /**
     * Test the generation of atstsl fields.
     *
     * @throws Exception
     *         the exceptions thrown in the test
     */
    @Test
    public void testGenerationOfAtstslField() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm(Locale.LanguageRange.parse("en"));

        Ruleset ruleset = new Ruleset();
        ruleset.setFile("ruleset_test.xml");
        createProcessForm.updateRulesetAndDocType(ruleset);

        ProcessDataTab underTest = createProcessForm.getProcessDataTab();
        underTest.setDocType("Child");

        createProcessForm.setCurrentProcess(new TempProcess(new Process(), null));

        DefaultTreeNode titleDocMainTreeNode = ProcessTestUtils.getTreeNode(TitleGenerator.TITLE_DOC_MAIN,
                TitleGenerator.TITLE_DOC_MAIN, "TitleOfPa_1234567X");
        DefaultTreeNode tslatsTreeNode = ProcessTestUtils.getTreeNode(TitleGenerator.TSL_ATS, TitleGenerator.TSL_ATS,
                "");
        ProcessFieldedMetadata processDetails = new ProcessFieldedMetadata() {
            {
                treeNode.getChildren().add(titleDocMainTreeNode);
                treeNode.getChildren().add(tslatsTreeNode);
            }
        };
        createProcessForm.getProcessMetadata().setProcessDetails(processDetails);

        underTest.generateAtstslFields();
        assertEquals("TSL/ATS does not match expected value", "Titl",
                createProcessForm.getCurrentProcess().getAtstsl());

    }

    /**
     * Test if childprocess title should prefixed by parent title.
     *
     * @throws Exception
     *         the exceptions thrown in the test
     */
    @Test
    public void shouldCreateChildProcessTitlePrefixedByParentItile() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm(Locale.LanguageRange.parse("en"));

        Ruleset ruleset = new Ruleset();
        ruleset.setFile("shouldCreateChildProcessTitlePrefixedByParentTitle.xml");
        createProcessForm.updateRulesetAndDocType(ruleset);

        Process parentProcess = new Process();
        parentProcess.setTitle("TitlOfPa_1234567X");
        createProcessForm.getTitleRecordLinkTab().setTitleRecordProcess(parentProcess);

        ProcessDataTab underTest = createProcessForm.getProcessDataTab();
        underTest.setDocType("Child");

        createProcessForm.setCurrentProcess(new TempProcess(new Process(), null));
        DefaultTreeNode metadataTreeNode = ProcessTestUtils.getTreeNode("ChildCount", "ChildCount", "8888");
        ProcessFieldedMetadata processDetails = new ProcessFieldedMetadata() {
            {
                treeNode.getChildren().add(metadataTreeNode);
            }
        };
        createProcessForm.getProcessMetadata().setProcessDetails(processDetails);

        underTest.generateAtstslFields();

        assertEquals("Process title could not be build", "TitlOfPa_1234567X_8888",
                createProcessForm.getCurrentProcess().getTiffHeaderDocumentName());
    }
}
