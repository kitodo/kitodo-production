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

package org.kitodo.dataformat.service;

import java.util.Arrays;
import java.util.List;

import org.kitodo.api.dataformat.mets.AgentXmlElementAccessInterface;
import org.kitodo.dataformat.metskitodo.MetsType.MetsHdr.Agent;

public class ProcessingNote implements AgentXmlElementAccessInterface {
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
     * The name of the entity causing the entry.
     */
    private String name;

    /**
     * The text of the processing note.
     */
    private String note;

    /**
     * The role of the entity causing the entry. For a better understanding cf.
     * {@link #KNOWN_ROLES}.
     */
    private String role;

    /**
     * The type of the entity causing the entry. For a better understanding cf.
     * {@link #KNOWN_TYPES}.
     */
    private String type;

    public ProcessingNote() {
    }

    ProcessingNote(Agent agent) {
        this.name = agent.getName();
        this.note = String.join(System.lineSeparator(), agent.getNote());
        this.role = "OTHER".equals(agent.getROLE()) ? agent.getOTHERROLE() : agent.getROLE();
        this.type = "OTHER".equals(agent.getTYPE()) ? agent.getOTHERTYPE() : agent.getROLE();
    }

    /**
     * Returns the name of the entity causing the entry.
     * 
     * @return the name of the entity causing the entry
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the text of the entry.
     * 
     * @return the text of the entry
     */
    public String getNote() {
        return note;
    }

    /**
     * Returns the role of the entity causing the entry.
     * 
     * @return the role of the entity causing the entry
     */
    public String getRole() {
        return role;
    }

    /**
     * Returns the type of the entity causing the entry.
     * 
     * @return the type of the entity causing the entry
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the name of the entity causing the entry. This may be the name of a
     * person, an institution, the software, or similar.
     * 
     * @param name
     *            the name of the entity causing the entry
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the text of the processing note.
     * 
     * @param note
     *            the text of the processing note
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Sets the role of the entity causing the edit comment. If possible, it
     * should be one of the following strings that have semantics in METS: (It
     * can also be other text.)
     * <ul>
     * <li>{@code CREATOR} − a person or institution responsible for the METS
     * document
     * <li>{@code EDITOR} − a person or institution that prepares the meta-data
     * for encoding
     * <li>{@code ARCHIVIST} − a person institution responsible for the
     * document/collection
     * <li>{@code PRESERVATION} − a person or institution responsible for
     * preservation functions
     * <li>{@code DISSEMINATOR} − a person or institution responsible for
     * dissemination functions
     * <li>{@code CUSTODIAN} − a person or institution charged with the
     * oversight of a document/collection
     * <li>{@code IPOWNER} − <i>Intellectual Property Owner</i>: a person or
     * institution holding copyright, trade or service marks or other
     * intellectual property rights for the object
     * </ul>
     * 
     * @param role
     *            role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the type of the entity causing the edit comment. If possible, it
     * should be one of the following strings that have semantics in METS: (It
     * can also be other text.)
     * <ul>
     * <li>{@code INDIVIDUAL} − if an individual has served as the agent
     * <li>{@code ORGANIZATION} − if an institution, corporate body,
     * association, non-profit enterprise, government, religious body, etc. has
     * served as the agent
     * </ul>
     * 
     * @param type
     *            type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Converts this editing note into a METS agent.
     * 
     * @return a METS agent for this edit note
     */
    Agent toAgent() {
        Agent agent = new Agent();
        agent.setName(name);
        for (String paragraph : note.split(System.lineSeparator())) {
            agent.getNote().add(paragraph);
        }
        if (KNOWN_ROLES.contains(role)) {
            agent.setROLE(role);
        } else {
            agent.setROLE("OTHER");
            agent.setOTHERROLE(role);
        }
        if (KNOWN_TYPES.contains(type)) {
            agent.setTYPE(type);
        } else {
            agent.setTYPE("OTHER");
            agent.setOTHERTYPE(type);
        }
        return agent;
    }
}
