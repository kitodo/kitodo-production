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

package org.kitodo.helper;

import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.Helper;

import java.util.Objects;

import org.goobi.production.plugin.CataloguePlugin.Hit;

/**
 * The class SelectableHit represents a hit on the hit list that shows up if a
 * catalogue search yielded more than one result. We need an inner class for
 * this because Faces is strictly object oriented and the always argument-less
 * actions can only be executed relatively to the list entry in question this
 * way if they are concerning elements that are rendered by iterating along a
 * list.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class SelectableHit {
    /**
     * The field hit holds the hit to be rendered as a list entry.
     */
    private final Hit hit;

    /**
     * The field error holds an error message to be rendered as a list entry in case
     * that retrieving the hit failed within the plug-in used for catalogue access.
     */
    private final String error;

    /**
     * Selectable hit constructor. Creates a new SelectableHit object with a hit to
     * show.
     *
     * @param hit
     *            Hit to show
     */
    public SelectableHit(Hit hit) {
        this.hit = hit;
        error = null;
    }

    /**
     * Selectable hit constructor. Creates a new SelectableHit object with an error
     * message to show.
     *
     * @param error
     *            error message
     */
    public SelectableHit(String error) {
        hit = null;
        this.error = error;
    }

    /**
     * The function getBibliographicCitation() returns a summary of this hit in
     * bibliographic citation style as HTML as read-only property
     * “bibliographicCitation”.
     *
     * @return a summary of this hit in bibliographic citation style as HTML
     */
    public String getBibliographicCitation() {
        if (Objects.nonNull(hit)) {
            return hit.getBibliographicCitation();
        }

        return "";
    }

    /**
     * The function getErrorMessage() returns an error if that had occurred when
     * trying to retrieve that hit from the catalogue as read-only property
     * “errorMessage”.
     *
     * @return an error message to be rendered as a list entry
     */
    public String getErrorMessage() {
        return error;
    }

    /**
     * The function isError() returns whether an error occurred when trying to
     * retrieve that hit from the catalogue as read-only property “error”.
     *
     * @return whether an error occurred when retrieving that hit
     */
    public boolean isError() {
        return hit == null;
    }

    /**
     * The function selectClick() is called if the user clicks on a catalogue hit
     * summary in order to import it into Production.
     *
     * @return always "", indicating to Faces to stay on that page
     */
    public String selectClick() {
        ProzesskopieForm prozesskopieForm = (ProzesskopieForm) Helper.getManagedBeanValue("#{ProzesskopieForm}");
        if (Objects.nonNull(prozesskopieForm)) {
            try {
                prozesskopieForm.importHit(hit);
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error on reading opac ", e);
            } finally {
                prozesskopieForm.setHitlistPage(-1);
            }
        }
        return null;
    }
}
