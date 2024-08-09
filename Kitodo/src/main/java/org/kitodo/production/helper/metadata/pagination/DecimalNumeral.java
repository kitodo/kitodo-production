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

package org.kitodo.production.helper.metadata.pagination;

import java.util.Objects;

/**
 * A decimal numeral as part of a pagination sequence.
 */
public class DecimalNumeral implements Fragment {

    /**
     * A format string to create at least the number of digits as the initial value
     * had.
     *
     * @see java.util.Formatter
     */
    private String digits;

    /**
     * The increment associated with this numeral.
     */
    private HalfInteger increment;

    /**
     * The initial value of this numeral.
     */
    private int value;

    DecimalNumeral(String value) {
        this.value = Integer.parseInt(value);
        this.digits = "%0" + value.length() + "d";
    }

    /**
     * Returns the value formatted as decimal numeral. The result will have at least
     * the number of digits of the initial value.
     */
    @Override
    public String format(HalfInteger value) {
        return String.format(digits, value.intValue());
    }

    @Override
    public HalfInteger getIncrement() {
        return increment;
    }

    @Override
    public Integer getInitialValue() {
        return value;
    }

    @Override
    public void setIncrement(HalfInteger increment) {
        this.increment = increment;
    }

    /**
     * Returns a concise string representation of this instance.
     *
     * @return a string representing this instance
     */
    @Override
    public String toString() {
        return String.format(digits, value) + (Objects.nonNull(increment) ? " (" + increment + ")" : " (default)");
    }
}
