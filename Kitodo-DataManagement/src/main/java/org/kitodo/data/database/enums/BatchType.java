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

package org.kitodo.data.database.enums;

/**
 * Enum for batch type. Type of batch:
 *
 * <dl>
 * <dt>LOGISTIC</dt>
 * <dd>facilitates the logistics of excavation and processing in the
 * digitisation centre</dd>
 * <dt>NEWSPAPER</dt>
 * <dd>forms the complete edition of a newspaper</dd>
 * <dt>SERIAL</dt>
 * <dd>forms the complete edition of a serial publication</dd>
 * </dl>
 */
public enum BatchType {
    LOGISTIC,
    NEWSPAPER,
    SERIAL
}
