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

package org.kitodo.production.enums;

/**
 * These are the possible states for output to “activeMQ.results.topic”.
 */
public enum ReportLevel {
    FATAL,
    ERROR,
    WARN,
    INFO,
    SUCCESS,
    DEBUG,
    VERBOSE,
    LUDICROUS;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
