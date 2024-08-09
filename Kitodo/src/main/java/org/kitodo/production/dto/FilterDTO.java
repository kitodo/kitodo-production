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

/**
 * Filter DTO object.
 */
public class FilterDTO extends BaseDTO {

    private String value;

    /**
     * Get value.
     * @return value as String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value as String
     */
    public void setValue(String value) {
        this.value = value;
    }
}
