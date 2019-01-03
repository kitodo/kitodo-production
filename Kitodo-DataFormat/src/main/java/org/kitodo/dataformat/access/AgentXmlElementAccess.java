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

package org.kitodo.dataformat.access;

import java.util.Arrays;
import java.util.List;

import org.kitodo.api.dataformat.ProcessingNote;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr.Agent;

/**
 * A processing note that can be placed in the header of the XML file.
 */
public class AgentXmlElementAccess {
    /**
     * Known roles in METS. If the role takes one of these values, the
     * {@code ROLE} attribute is set to this value. Otherwise, the role is set
     * to {@code OTHER} and the {@code OTHERROLE} attribute is set to the value
     * of the role.
     */
    private static final List<String> KNOWN_ROLES = Arrays.asList("CREATOR", "EDITOR", "ARCHIVIST", "PRESERVATION",
        "DISSEMINATOR", "CUSTODIAN", "IPOWNER");

    /**
     * Known types in METS. If the type takes one of these values, the
     * {@code TYPE} attribute is set to this value. Otherwise, the type is set
     * to {@code OTHER} and the {@code OTHERTYPE} attribute is set to the value
     * of the type.
     */
    private static final List<String> KNOWN_TYPES = Arrays.asList("INDIVIDUAL", "ORGANIZATION");

    /**
     * The data object of this agent XML element access.
     */
    private final ProcessingNote processingNote;

    /**
     * Public constructor. This constructor is used to create a new machining
     * comment via the module loader.
     */
    public AgentXmlElementAccess() {
        processingNote = new ProcessingNote();
    }

    /**
     * Constructor with a METS agent. This constructor creates a new processing
     * note from an agent.
     * 
     * @param agent
     *            agent from which a new constructor is to be created
     */
    AgentXmlElementAccess(Agent agent) {
        this();
        processingNote.setName(agent.getName());
        processingNote.setNote(String.join(System.lineSeparator(), agent.getNote()));
        processingNote.setRole("OTHER".equals(agent.getROLE()) ? agent.getOTHERROLE() : agent.getROLE());
        processingNote.setType("OTHER".equals(agent.getTYPE()) ? agent.getOTHERTYPE() : agent.getROLE());
    }



    public AgentXmlElementAccess(ProcessingNote processingNote) {
        this.processingNote = processingNote;
    }

    ProcessingNote getProcessingNote() {
        return processingNote;
    }

    /**
     * Converts this editing note into a METS agent.
     * 
     * @return a METS agent for this edit note
     */
    Agent toAgent() {
        Agent agent = new Agent();
        agent.setName(processingNote.getName());
        for (String paragraph : processingNote.getNote().split(System.lineSeparator())) {
            agent.getNote().add(paragraph);
        }
        if (KNOWN_ROLES.contains(processingNote.getRole())) {
            agent.setROLE(processingNote.getRole());
        } else {
            agent.setROLE("OTHER");
            agent.setOTHERROLE(processingNote.getRole());
        }
        if (KNOWN_TYPES.contains(processingNote.getType())) {
            agent.setTYPE(processingNote.getType());
        } else {
            agent.setTYPE("OTHER");
            agent.setOTHERTYPE(processingNote.getType());
        }
        return agent;
    }
}
