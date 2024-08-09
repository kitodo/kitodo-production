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

public enum FilterString {

    TASK("step:", "schritt:"),
    TASKINWORK("stepinwork:", "schrittinarbeit:"),
    TASKLOCKED("steplocked:", "schrittgesperrt:"),
    TASKOPEN("stepopen:", "schrittoffen:"),
    TASKDONE("stepdone:", "schrittabgeschlossen:"),
    TASKDONETITLE("stepdonetitle:", "abgeschlossenerschritttitel:"),
    TASKDONEUSER("stepdoneuser:", "abgeschlossenerschrittbenutzer:"),
    PROJECT("project:", "projekt:"),
    ID("id:", "id:"),
    PARENTPROCESSID("parentprocessid:", "elternprozessid:"),
    PROCESS("process:", "prozess:"),
    BATCH("batch:", "gruppe:"),
    TASKAUTOMATIC("stepautomatic:", "schrittautomatisch:"),
    PROPERTY("property:","eigenschaft:");

    private final String filterEnglish;
    private final String filterGerman;

    /**
     * Constructor.
     *
     * @param filterEnglish
     *            English version of filter string
     * @param filterGerman
     *            German version of filter string
     */
    FilterString(String filterEnglish, String filterGerman) {
        this.filterEnglish = filterEnglish;
        this.filterGerman = filterGerman;
    }

    /**
     * Get English version of filter string.
     *
     * @return English version of filter string
     */
    public String getFilterEnglish() {
        return filterEnglish;
    }

    /**
     * Get German version of filter string.
     *
     * @return German version of filter string
     */
    public String getFilterGerman() {
        return filterGerman;
    }
}
