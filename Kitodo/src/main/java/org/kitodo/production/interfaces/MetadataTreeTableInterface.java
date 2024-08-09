/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
