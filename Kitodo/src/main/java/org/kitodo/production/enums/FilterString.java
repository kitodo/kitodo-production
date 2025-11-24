/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
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
    PROJECT_LOOSE("project_loose:", "projekt_trunkiert:"),
    ID("id:", "id:"),
    PARENTPROCESSID("parentprocessid:", "elternprozessid:"),
    PROCESS("process:", "prozess:"),
    PROCESS_LOOSE("process_loose:", "prozess_unscharf:"),
    BATCH("batch:", "gruppe:"),
    TASKAUTOMATIC("stepautomatic:", "schrittautomatisch:"),
    PROPERTY("property:","eigenschaft:"),
    SEARCH("search:", "suche:");

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
