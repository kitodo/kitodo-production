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

package org.kitodo.dataeditor.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
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
        List<String> expectedNotes = new ArrayList<>();
        expectedNotes.add(noteMessage);

        MetsType.MetsHdr.Agent agent = new MetsType.MetsHdr.Agent();
        agent.setName("Kitodo");
        MetsType.MetsHdr metsHeader = new MetsType.MetsHdr();
        metsHeader.getAgent().add(0, agent);
        Mets mets = new Mets();
        mets.setMetsHdr(metsHeader);
        Mets actual = MetsKitodoHeaderHandler.addNoteToMetsHeader(noteMessage, mets);

        List<String> resultNotes = actual.getMetsHdr().getAgent().get(0).getNote();
        assertEquals(expectedNotes, resultNotes, "MetsHeader notes differ.");
    }
}
