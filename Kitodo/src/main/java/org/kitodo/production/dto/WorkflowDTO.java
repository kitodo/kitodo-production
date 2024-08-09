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

package org.kitodo.production.dto;

public class WorkflowDTO extends BaseDTO {

    private String title;
    private String status;

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get status.
     *
     * @return value of status
     */
    public String getStatus() {
        return status.toLowerCase();
    }

    /**
     * Set status.
     *
     * @param status
     *            as String
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
