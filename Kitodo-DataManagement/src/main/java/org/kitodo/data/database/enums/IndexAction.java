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
 * Enum for determining current state of ElasticSearch index. According to it
 * necessary operation must be performed on ElasticSearch index.
 */
public enum IndexAction {
    INDEX,
    DELETE,
    DONE
}
