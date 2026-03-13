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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;

public class AddNodeDialog {

    protected final DataEditorForm dataEditor;

    AddNodeDialog(DataEditorForm dataEditor) {
        this.dataEditor = dataEditor;
    }

    /**
     * Determines the logical parent division that can be used as a basis for the dialog.
     *
     * <p>In case physical divisions are selected, the parent logical division is returned.</p>
     *
     * <p>In case a single logical division is selected, the logical division itself is returned.</p>
     *
     * <p>In case multiple divisions are selected, or physical divisions have multiple different parents, nothing is returned.</p>
     */
    protected Optional<LogicalDivision> getTargetLogicalDivisionFromNodeSelection() {
        Set<LogicalDivision> logicalDivisionSet =  dataEditor.getStructurePanel().getSelectedLogicalNodes().stream()
                .map(StructureTreeOperations::getTreeNodeLogicalParentOrSelf)
                .map(StructureTreeOperations::getLogicalDivisionFromTreeNode)
                .collect(Collectors.toSet());

        if (logicalDivisionSet.isEmpty()) {
            // determine logical parent division for selection of media (in case of gallery selection)
            logicalDivisionSet.addAll(
                    dataEditor.getSelectedMedia().stream()
                            .map(Pair::getRight)
                            .collect(Collectors.toSet())
            );
        }

        if (logicalDivisionSet.size() == 1) {
            LogicalDivision logicalDivision = logicalDivisionSet.iterator().next();
            return Optional.of(logicalDivision);
        }
        return Optional.empty();
    }

}
