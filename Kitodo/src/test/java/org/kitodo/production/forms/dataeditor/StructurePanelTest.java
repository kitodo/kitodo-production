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

package org.kitodo.production.forms.dataeditor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.DummyRulesetManagement;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.mets.LinkedMetsResource;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class StructurePanelTest {

    @Test
    public void testBuildStructureTreeRecursively() throws Exception {
        DataEditorForm dummyDataEditorForm = new DataEditorForm();
        Process process = new Process();
        Template template = new Template();
        template.setWorkflow(new Workflow());
        process.setTemplate(template);
        dummyDataEditorForm.setProcess(process);
        Field ruleset = DataEditorForm.class.getDeclaredField("ruleset");
        ruleset.setAccessible(true);
        ruleset.set(dummyDataEditorForm, new DummyRulesetManagement());
        final StructurePanel underTest = new StructurePanel(dummyDataEditorForm);

        LogicalDivision structure = new LogicalDivision();
        LinkedMetsResource link = new LinkedMetsResource();
        link.setUri(URI.create("database://?process.id=42"));
        structure.setLink(link);
        Map<Integer, String> processTypeMap = new HashMap<>();
        Map<String, StructuralElementViewInterface> viewCache = new HashMap<>();
        processTypeMap.put(ServiceManager.getProcessService().processIdFromUri(link.getUri()), "Monograph");
        TreeNode<Object> result = new DefaultTreeNode<>();

        Method buildStructureTreeRecursively = StructurePanel.class.getDeclaredMethod("buildStructureTreeRecursively",
            LogicalDivision.class, TreeNode.class, Map.class, Map.class);
        buildStructureTreeRecursively.setAccessible(true);
        buildStructureTreeRecursively.invoke(underTest, structure, result, processTypeMap, viewCache);

        assertTrue(((StructureTreeNode) result.getChildren().getFirst().getData()).isLinked());
    }

    @Test
    public void preventNullPointerExceptionInIsSeparateMediaOnNotFullInitializedDataEditorForm() {
        DataEditorForm dummyDataEditorForm = new DataEditorForm();
        final StructurePanel underTest = new StructurePanel(dummyDataEditorForm);
        assertFalse(underTest.isSeparateMedia());
    }
}
