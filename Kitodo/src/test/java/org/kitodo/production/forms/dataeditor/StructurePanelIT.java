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

import java.lang.reflect.Method;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.primefaces.model.DefaultTreeNode;

public class StructurePanelIT {

    @Test
    public void testAddParentLinksRecursive() throws Exception {
        DataEditorForm dummyDataEditorForm = new DataEditorForm();
        final StructurePanel underTest = new StructurePanel(dummyDataEditorForm);

        Process parent = new Process();
        parent.setProcessBaseUri(URI.create("2"));
        Process child = new Process();
        child.setId(42);
        child.setParent(parent);
        DefaultTreeNode result = new DefaultTreeNode();

        Method addParentLinksRecursive = StructurePanel.class.getDeclaredMethod("addParentLinksRecursive",
            Process.class, DefaultTreeNode.class);
        addParentLinksRecursive.setAccessible(true);
        addParentLinksRecursive.invoke(underTest, child, result);

        Assert.assertTrue(((StructureTreeNode) result.getChildren().get(0).getData()).isLinked());
    }
}
