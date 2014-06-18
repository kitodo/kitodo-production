/* 
 * The Fascinator - ReDBox/Mint SRU Client - NLA Identity
 * Copyright (C) 2012 Queensland Cyber Infrastructure Foundation (http://www.qcif.edu.au/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.googlecode.fascinator.redbox.sru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A basic wrapper for handling EAC-CPF formatted identities that return for
 * the National Library of Australia. This is neither a complete EAC-CPF handling
 * class, nor a complete implementation of NLA identities. It is just a utility
 * for access the things ReDBox/Mint cares about in common node.</p>
 * 
 * @author Greg Pendlebury
 */
public class NLAIdentity {
    /** Logging **/
    private static Logger log = LoggerFactory.getLogger(NLAIdentity.class);

    /** DOM4J Node for this person **/
    private Node eac;

    /** Properties we extract **/
    private String nlaId;
    private String displayName;
    private String firstName;
    private String surname;
    private String institution;
    private List<Map<String, String>> knownIds;

    /**
     * <p>Default Constructor. Extract some basic information.</p>
     * 
     * @param searchResponse A parsed DOM4J Document
     * @throws SRUException If any of the XML structure does not look like expected
     */
    public NLAIdentity(Node node) throws SRUException {
        eac = node;

        // Identity
        @SuppressWarnings("unchecked")
		List<Node> otherIds = eac.selectNodes("eac:eac-cpf/eac:control/eac:otherRecordId");
        for (Node idNode : otherIds) {
            String otherId = idNode.getText();
            if (otherId.startsWith("http://nla.gov.au")) {
                nlaId = otherId;
            }
        }
        if (nlaId == null) {
            throw new SRUException("Error processing Identity; Cannot find ID");
        }

        knownIds = getSourceIdentities();

        // Cosmetically we want to use the first row (should be the longest top-level name we found)
        firstName = knownIds.get(0).get("firstName");
        surname = knownIds.get(0).get("surname");
        displayName = knownIds.get(0).get("displayName");
        // For institution we want the first one we find that isn't NLA or Libraries Australia
        for (Map<String, String> id : knownIds) {
            if (institution == null
                    // But we'll settle for those in a pinch
                    || "National Library of Australia Party Infrastructure".equals(institution)
                    || "Libraries Australia".equals(institution)) {
                institution = id.get("institution");
            }
        }
    }

    private List<Map<String, String>> getSourceIdentities() {
        List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();

        // Top level institution
        Map<String, String> idMap = new HashMap<String, String>();
        Node institutionNode = eac.selectSingleNode("eac:eac-cpf/eac:control/eac:maintenanceAgency/eac:agencyName");
        String institutionString = institutionNode.getText();
        // Top level name
        Node nlaNamesNode = eac.selectSingleNode("eac:eac-cpf/eac:cpfDescription/eac:identity");
        // Get all the names this ID lists
        List<Map<String, String>> nameList = getNames(nlaNamesNode);
        for (Map<String, String> name : nameList) {
            // Only use the longest top-level name for display purposes
            String oldDisplayName = idMap.get("displayName");
            String thisDisplayName = name.get("displayName");
            if (oldDisplayName == null
                    || (thisDisplayName != null
                        && thisDisplayName.length() > oldDisplayName.length())) {
                // Clear any old data
                idMap.clear();
                // Store this ID
                idMap.putAll(name);
                idMap.put("institution", institutionString);
            }
        }
        // And add to the list
        returnList.add(idMap);

        // All name entities from contributing insitutions
        @SuppressWarnings("unchecked")
		List<Node> sourceIdentities = eac.selectNodes("eac:eac-cpf/eac:cpfDescription//eac:eac-cpf");
        for (Node identity : sourceIdentities) {
            // Insitution for this ID
            institutionNode = identity.selectSingleNode("*//eac:maintenanceAgency/eac:agencyName");
            institutionString = institutionNode.getText();

            // Any names for this ID
            @SuppressWarnings("unchecked")
			List<Node> idNodes = identity.selectNodes("*//eac:identity");
            for (Node idNode : idNodes) {
                // A Map for each name
                idMap = new HashMap<String, String>();
                // Get all the names this ID lists
                nameList = getNames(idNode);
                for (Map<String, String> name : nameList) {
                    idMap.putAll(name);
                }
                // Indicate the insitution for each one
                idMap.put("institution", institutionString);
                // And add to the list
                returnList.add(idMap);
            }
        }

        // Debugging
        //for (Map<String, String> id : returnList) {
        //    String display = id.get("displayName") + " (" + id.get("institution") + ")";
        //    log.debug("Identity: {}", display);
        //}

        return returnList;
    }

    private List<Map<String, String>> getNames(Node node) {
        List<Map<String, String>> nameList = new ArrayList<Map<String, String>>();

        // Any names for this ID
        @SuppressWarnings("unchecked")
		List<Node> names = node.selectNodes("eac:nameEntry");
        for (Node name : names) {
            Map<String, String> nameMap = new HashMap<String, String>();

            String thisDisplay = null;
            String thisFirstName = null;
            String thisSurname = null;
            String title = null;

            // First name
            Node firstNameNode = name.selectSingleNode("eac:part[(@localType=\"forename\") or (@localType=\"givenname\")]");
            if (firstNameNode != null) {
                thisFirstName = firstNameNode.getText();
            }

            // Surname
            Node surnameNode = name.selectSingleNode("eac:part[(@localType=\"surname\") or (@localType=\"familyname\")]");
            if (surnameNode != null) {
                thisSurname = surnameNode.getText();
            }

            // Title
            Node titleNode = name.selectSingleNode("eac:part[@localType=\"title\"]");
            if (titleNode != null) {
                title = titleNode.getText();
            }

            // Display Name
            if (thisSurname != null) {
                thisDisplay = thisSurname;
                nameMap.put("surname", thisSurname);
                if (thisFirstName != null) {
                    thisDisplay += ", " + thisFirstName;
                    nameMap.put("firstName", thisFirstName);
                }
                if (title != null) {
                    thisDisplay += " (" + title + ")";
                }
                nameMap.put("displayName", thisDisplay);
            }

            // Last ditch effort... we couldn't find simple name information from
            //  recommended values. So just concatenate what we can see.
            if (thisDisplay == null) {
                // Find every part
                @SuppressWarnings("unchecked")
				List<Node> parts = name.selectNodes("eac:part");
                for (Node part : parts) {
                    // Grab the value and type of this value
                    Element element = (Element) part;
                    String value = element.getText();
                    String type = element.attributeValue("localType");
                    // Build a display value for this part
                    if (type != null) {
                        value += " ("+type+")";
                    }
                    // And add to the display name
                    if (thisDisplay == null) {
                        thisDisplay = value;
                    } else {
                        thisDisplay += ", " + value;
                    }
                }
                nameMap.put("displayName", thisDisplay);
            }

            nameList.add(nameMap);
        }

        return nameList;
    }

    /**
     * <p>Getter for the NLA Identifier in use by this Identity.</p>
     * 
     * @return String The ID from the NLA for this Identity
     */
    public String getId() {
        return nlaId;
    }

    /**
     * <p>Getter for our best estimation on a display name for this Identity.</p>
     * 
     * @return String The display name for this Identity
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * <p>Getter for the first name for this Identity.</p>
     * 
     * @return String The first name for this Identity
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * <p>Getter for the surname for this Identity.</p>
     * 
     * @return String The surname for this Identity
     */
    public String getSurame() {
        return surname;
    }

    /**
     * <p>Getter for the institution for this Identity.</p>
     * 
     * @return String The institution for this Identity
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * <p>Getter for the List of Identities observed for this person. The return
     * Objects are Maps containing keys very similar to the methods found on the
     * top-level NLAIdentity Object.</p>
     * <ul>
     *   <li>'displayName'</li>
     *   <li>'firstName'</li>
     *   <li>'surname'</li>
     *   <li>'institution'</li>
     * </ul>
     * 
     * @return List<Map<String, String>> A List Object containing identities
     */
    public List<Map<String, String>> getKnownIdentities() {
        return knownIds;
    }

    /**
     * <p>Converts a List of DOM4J Nodes into a List of processed NLAIdentity(s).
     * Individual Nodes that fail to process will be skipped.</p>
     * 
     * @param nodes A List of Nodes to process
     * @return List<NLAIdentity> A List of processed Identities
     */
    public static List<NLAIdentity> convertNodesToIdentities(List<Node> nodes) {
        try {
            return convertNodesToIdentities(nodes, false);
        } catch (SRUException ex) {
            // Will never executre because 'false' is set above,
            // but trapping this here allows users to call this method
            // with greater ease, since they wan't need to trap.
            return null;
        }
    }

    /**
     * <p>Converts a List of DOM4J Nodes into a List of processed NLAIdentity(s).
     * Must indicate whether or not errors should cause processing to halt.</p>
     * 
     * @param nodes A List of Nodes to process
     * @param haltOnErrors Flag if a single Node failing to process should halt execution.
     * @return List<NLAIdentity> A List of processed Identities
     * @throws SRUException If 'haltOnErrors' is set to TRUE and a Node fails to process.
     */
    public static List<NLAIdentity> convertNodesToIdentities(List<Node> nodes,
            boolean haltOnErrors) throws SRUException {
        List<NLAIdentity> response = new ArrayList<NLAIdentity>();
        // Sanity check
        if (nodes == null || nodes.isEmpty()) {
            return response;
        }
        // Process each Node in turn
        for (Node node : nodes) {
            try {
                NLAIdentity newId = new NLAIdentity(node);
                response.add(newId);

            // Only halt if requested
            } catch(SRUException ex) {
                log.error("Unable to process identity: ", ex);
                if (haltOnErrors) {
                    throw ex;
                }
            }
        }
        return response;
    }
}
