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

package org.kitodo.production.metadata;

public enum InsertionPosition {
    BEFORE_CURRENT_ELEMENT,
    AFTER_CURRENT_ELEMENT,
    FIRST_CHILD_OF_CURRENT_ELEMENT,
    LAST_CHILD_OF_CURRENT_ELEMENT,
    CURRENT_POSITION,
    PARENT_OF_CURRENT_ELEMENT
}
