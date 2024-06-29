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

import java.io.Serializable;

public abstract class BaseDTO implements Serializable {

    private Integer id;

    /**
     * Get id.
     *
     * @return id as Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set id.
     *
     * @param id
     *            as Integer
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
