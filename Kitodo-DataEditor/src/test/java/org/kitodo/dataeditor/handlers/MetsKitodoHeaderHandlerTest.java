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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;


public class MetsKitodoHeaderHandlerTest {

    @Test
    public void missingMetsHeaderShouldNotCreteNullPointerException() {
        String noteMessage = "test note";
        Mets mets = new Mets();

        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);
        assertEquals(mets, actual, "Mets object differ.");
    }

    @Test
    public void missingMetsHeaderAgentSectionDidNotChangeAnything() {
        String noteMessage = "test note";
        Mets mets = new Mets();

        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);
        assertEquals(mets, actual, "Mets object differ.");
    }

    @Test
    public void addNoteToExistingAgentSection() {
        String noteMessage = "test note";

        MetsType.MetsHdr.Agent agent = new MetsType.MetsHdr.Agent();
        agent.setName("Kitodo");
        MetsType.MetsHdr metsHeader = new MetsType.MetsHdr();
        metsHeader.getAgent().addFirst(agent);
        Mets mets = new Mets();
        mets.setMetsHdr(metsHeader);
        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);

        List<MetsType.MetsHdr.Agent.Note> resultNotes = actual.getMetsHdr().getAgent().getFirst().getNote();
        assertEquals(1, resultNotes.size(), "Expecting one note in mets header agent section.");
        assertEquals(noteMessage, resultNotes.getFirst().getValue(), "Note in mets header agent section differ.");
    }
}
