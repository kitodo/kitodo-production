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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataeditor.rulesetmanagement.Domain;
import org.kitodo.api.dataeditor.rulesetmanagement.InputType;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.process.TitleGenerator;
import org.primefaces.model.DefaultTreeNode;

public class ProcessDataTabTest {

    @Test
    public void testGenerationOfAtstslField() throws Exception {
        CreateProcessForm createProcessForm = new CreateProcessForm(Locale.LanguageRange.parse("en"));

        Ruleset ruleset = new Ruleset();
        ruleset.setFile("ruleset_test.xml");
        createProcessForm.updateRulesetAndDocType(ruleset);

        ProcessDataTab underTest = createProcessForm.getProcessDataTab();
        underTest.setDocType("Child");

        createProcessForm.setCurrentProcess(new TempProcess(new Process(), null));

        DefaultTreeNode titleDocMainTreeNode = getTreeNode(TitleGenerator.TITLE_DOC_MAIN, TitleGenerator.TITLE_DOC_MAIN,
            "TitleOfPa_1234567X");
        DefaultTreeNode tslatsTreeNode = getTreeNode(TitleGenerator.TSL_ATS, TitleGenerator.TSL_ATS, "");
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
        DefaultTreeNode metadataTreeNode = getTreeNode("ChildCount", "ChildCount", "8888");
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

    private static DefaultTreeNode getTreeNode(String metadataId, String metadataKey, String metadataValue) {
        DefaultTreeNode metdataTreeNode = new DefaultTreeNode();
        MetadataEntry metadataEntry = new MetadataEntry();
        metadataEntry.setKey(metadataKey);
        metadataEntry.setValue(metadataValue);
        metdataTreeNode.setData(new ProcessTextMetadata(null, getSettingsObject(metadataId), metadataEntry));
        return metdataTreeNode;
    }

    private static SimpleMetadataViewInterface getSettingsObject(String metadataId) {

        SimpleMetadataViewInterface settings = new SimpleMetadataViewInterface() {

            @Override
            public Optional<Domain> getDomain() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public String getId() {
                return metadataId;
            }

            @Override
            public String getLabel() {
                return "";
            }

            @Override
            public int getMaxOccurs() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public int getMinOccurs() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public boolean isUndefined() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Collection<String> getDefaultItems() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public InputType getInputType() {
                return InputType.ONE_LINE_TEXT;
            }

            @Override
            public int getMinDigits() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public Map<String, String> getSelectItems(List<Map<MetadataEntry, Boolean>> metadata) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public boolean isEditable() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public boolean isValid(String value, List<Map<MetadataEntry, Boolean>> metadata) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        return settings;
    }
}
