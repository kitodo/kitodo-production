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

package org.kitodo.lugh;

import java.util.*;

/**
 * The {@code http://www.loc.gov/mods/v3} namespace.
 */
public class Mods {

    public static final NodeReference AUTHORITY = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#authority");

    public static final NodeReference CLASSIFICATION = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#classification");

    public static final NodeReference DATE_ISSUED = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#dateIssued");

    public static final NodeReference DISPLAY_FORM = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#displayForm");

    public static final NodeReference ENCODING = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#encoding");

    public static final NodeReference EXTENT = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#extent");

    public static final NodeReference IDENTIFIER = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#identifier");

    public static final NodeReference LANGUAGE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#language");

    public static final NodeReference LANGUAGE_TERM = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#languageTerm");

    public static final NodeReference MODS = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#mods");

    public static final NodeReference NAME = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#name");

    public static final NodeReference NAME_PART = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#namePart");

    public static final String NAMESPACE = "http://www.loc.gov/mods/v3#";

    public static final NodeReference PHYSICAL_DESCRIPTION = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#physicalDescription");

    public static final NodeReference PLACE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#place");

    public static final NodeReference PLACE_TERM = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#placeTerm");

    public static final NodeReference PUBLISHER = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#publisher");

    public static final NodeReference RECORD_IDENTIFIER = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#recordIdentifier");

    public static final NodeReference RECORD_INFO = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#recordInfo");

    /**
     * A reverse map of the URLs to the constants.
     */
    private static final Map<String, NodeReference> reversed;

    public static final NodeReference ROLE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#role");

    public static final NodeReference ROLE_TERM = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#roleTerm");

    public static final NodeReference SOURCE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#source");

    public static final NodeReference TITLE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#title");

    public static final NodeReference TITLE_INFO = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#titleInfo");

    public static final NodeReference TYPE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/mods/v3#type");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(32);
        for (NodeReference value : new NodeReference[] {AUTHORITY, CLASSIFICATION, DATE_ISSUED, DISPLAY_FORM, ENCODING,
                EXTENT, IDENTIFIER, LANGUAGE, LANGUAGE_TERM, MODS, NAME, NAME_PART, PHYSICAL_DESCRIPTION, PLACE,
                PLACE_TERM, PUBLISHER, RECORD_IDENTIFIER, RECORD_INFO, ROLE, ROLE_TERM, SOURCE, TITLE, TITLE_INFO,
                TYPE }) {
            reversed.put(value.getIdentifier(), value);
        }
    }

    /**
     * Returns a constant by its URL value
     *
     * @param url
     *            URL to resolve
     * @return the enum constant
     * @throws IllegalArgumentException
     *             if the URL is not mappped in the RDF namespace
     */
    public static NodeReference valueOf(String url) {
        if (!reversed.containsKey(url)) {
            throw new IllegalArgumentException("Unknown URL: " + url);
        }
        return reversed.get(url);
    }

    private Mods() {
    }
}
