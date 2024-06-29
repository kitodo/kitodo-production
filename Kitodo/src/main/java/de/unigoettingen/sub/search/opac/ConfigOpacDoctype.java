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

package de.unigoettingen.sub.search.opac;

public class ConfigOpacDoctype {
    private String title;
    private String rulesetType;
    private String tifHeaderType;

    ConfigOpacDoctype(String inTitle, String inRulesetType, String inTifHeaderType) {

        this.title = inTitle;
        this.rulesetType = inRulesetType;
        this.tifHeaderType = inTifHeaderType;
    }

    public String getTitle() {
        return this.title;
    }

    public String getRulesetType() {
        return this.rulesetType;
    }

    public String getTifHeaderType() {
        return this.tifHeaderType;
    }
}
