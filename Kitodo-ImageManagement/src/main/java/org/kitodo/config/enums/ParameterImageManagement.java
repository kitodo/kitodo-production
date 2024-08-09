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

package org.kitodo.config.enums;

public enum ParameterImageManagement implements ParameterInterface {

    DIR_TMP("ImageManagement.tmpDir"),
    SEARCH_PATH("ImageManagement.searchPath"),
    TIMEOUT_SEC("ImageManagement.timeoutSec"),
    SSH_HOST("ImageManagement.sshHosts");

    private String name;

    /**
     * Private constructor to hide the implicit public one.
     *
     * @param name
     *            of parameter
     */
    ParameterImageManagement(String name) {
        this.name = name;
    }

    @Override
    public java.lang.String toString() {
        return this.name;
    }
}
