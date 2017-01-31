package org.kitodo.production.lugh;

import org.kitodo.production.lugh.ld.NodeReference;

/**
 * The {@code http://www.loc.gov/mods/v3} namespace.
 *
 * @author Matthias Ronge
 */
public class Mods {

    public static final NodeReference AUTHORITY = new NodeReference("http://www.loc.gov/mods/v3#authority");
    public static final NodeReference CLASSIFICATION = new NodeReference("http://www.loc.gov/mods/v3#classification");
    public static final NodeReference DATE_ISSUED = new NodeReference("http://www.loc.gov/mods/v3#dateIssued");
    public static final NodeReference DISPLAY_FORM = new NodeReference("http://www.loc.gov/mods/v3#displayForm");
    public static final NodeReference ENCODING = new NodeReference("http://www.loc.gov/mods/v3#encoding");
    public static final NodeReference EXTENT = new NodeReference("http://www.loc.gov/mods/v3#extent");
    public static final NodeReference IDENTIFIER = new NodeReference("http://www.loc.gov/mods/v3#identifier");
    public static final NodeReference LANGUAGE = new NodeReference("http://www.loc.gov/mods/v3#language");
    public static final NodeReference LANGUAGE_TERM = new NodeReference("http://www.loc.gov/mods/v3#languageTerm");
    public static final NodeReference MODS = new NodeReference("http://www.loc.gov/mods/v3#mods");
    public static final NodeReference NAME = new NodeReference("http://www.loc.gov/mods/v3#name");
    public static final NodeReference NAME_PART = new NodeReference("http://www.loc.gov/mods/v3#namePart");
    public static final String NAMESPACE = "http://www.loc.gov/mods/v3";
    public static final NodeReference PHYSICAL_DESCRIPTION = new NodeReference(
            "http://www.loc.gov/mods/v3#physicalDescription");
    public static final NodeReference PLACE = new NodeReference("http://www.loc.gov/mods/v3#place");
    public static final NodeReference PLACE_TERM = new NodeReference("http://www.loc.gov/mods/v3#placeTerm");
    public static final NodeReference PUBLISHER = new NodeReference("http://www.loc.gov/mods/v3#publisher");
    public static final NodeReference RECORD_IDENTIFIER = new NodeReference(
            "http://www.loc.gov/mods/v3#recordIdentifier");
    public static final NodeReference RECORD_INFO = new NodeReference("http://www.loc.gov/mods/v3#recordInfo");
    public static final NodeReference ROLE = new NodeReference("http://www.loc.gov/mods/v3#role");
    public static final NodeReference ROLE_TERM = new NodeReference("http://www.loc.gov/mods/v3#roleTerm");
    public static final NodeReference SOURCE = new NodeReference("http://www.loc.gov/mods/v3#source");
    public static final NodeReference TITLE = new NodeReference("http://www.loc.gov/mods/v3#title");
    public static final NodeReference TITLE_INFO = new NodeReference("http://www.loc.gov/mods/v3#titleInfo");
    public static final NodeReference TYPE = new NodeReference("http://www.loc.gov/mods/v3#type");

    private Mods() {
    }
}
