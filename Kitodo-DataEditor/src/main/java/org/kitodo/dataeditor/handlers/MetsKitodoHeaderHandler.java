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

package org.kitodo.dataeditor.handlers;

import java.util.List;

import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;

/**
 * General utilities for handling of generated mets-kitodo class content.
 */
public class MetsKitodoHeaderHandler {

    /**
     * Adds a note to the first {@code agent} element in mets header. Does nothing
     * if no {@code agent} element exists.
     *
     * @param noteMessage
     *            The note message.
     * @param mets
     *            The Mets object.
     * @return The Mets object with added note.
     */
    public static Mets addNoteToMetsHeader(String noteMessage, Mets mets) {
        List<MetsType.MetsHdr.Agent> agents = mets.getMetsHdr().getAgent();
        if (!agents.isEmpty()) {
            agents.get(0).getNote().add(noteMessage);
        }
        return mets;
    }
}
