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

package org.kitodo.production.interfaces;

import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.primefaces.model.TreeNode;

public interface MetadataTreeTableInterface {

    boolean canBeDeleted(ProcessDetail processDetail);

    boolean canBeAdded(TreeNode treeNode) throws InvalidMetadataValueException;

    boolean metadataAddableToGroup(TreeNode metadataNode);

    void prepareAddableMetadataForGroup(TreeNode treeNode);
}
