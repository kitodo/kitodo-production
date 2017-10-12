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
 * The {@code http://www.loc.gov/METS/} namespace.
 */
public class Mets {

    public static final NodeReference ADMID = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/ADMID");

    public static final NodeReference AGENT = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/agent");

    public static final NodeReference AMD_SEC = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/amdSec");

    public static final NodeReference DIV = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/div");

    public static final NodeReference DMD_SEC = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/dmdSec");

    public static final NodeReference DMDID = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/DMDID");

    public static final NodeReference F_LOCAT = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/FLocat");

    public static final NodeReference FILE = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/file");

    public static final NodeReference FILE_GRP = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/fileGrp");

    public static final NodeReference FILE_SEC = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/fileSec");

    public static final NodeReference FILEID = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/FILEID");

    public static final NodeReference FPTR = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/fptr");

    public static final NodeReference ID = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/ID");

    public static final NodeReference LABEL = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/LABEL");

    public static final NodeReference LASTMODDATE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/LASTMODDATE");

    public static final NodeReference LOCTYPE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/LOCTYPE");

    public static final NodeReference MD_WRAP = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/mdWrap");

    public static final NodeReference METS = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/mets");

    public static final NodeReference METS_HDR = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/metsHdr");

    public static final NodeReference MIMETYPE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/MIMETYPE");

    public static final NodeReference NAME = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/name");

    public static final String NAMESPACE = "http://www.loc.gov/METS/";

    public static final NodeReference NOTE = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/note");

    public static final NodeReference ORDER = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/ORDER");

    public static final NodeReference ORDERLABEL = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/ORDERLABEL");

    public static final NodeReference OTHERTYPE = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/OTHERTYPE");

    /**
     * A reverse map of the URLs to the constants.
     */
    public static final Map<String, NodeReference> reversed;

    public static final NodeReference ROLE = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/ROLE");

    public static final NodeReference SM_LINK = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/smLink");

    public static final NodeReference STRUCT_LINK = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/structLink");

    public static final NodeReference STRUCT_MAP = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/structMap");

    public static final NodeReference TYPE = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/TYPE");

    public static final NodeReference USE = MemoryStorage.INSTANCE.createNodeReference("http://www.loc.gov/METS/USE");

    public static final NodeReference XML_DATA = MemoryStorage.INSTANCE
            .createNodeReference("http://www.loc.gov/METS/xmlData");

    /**
     * Populates the reverse map of the URLs to the constants.
     */
    static {
        reversed = new HashMap<>(43);
        for (NodeReference value : new NodeReference[] {ADMID, AGENT, AMD_SEC, DIV, DMD_SEC, DMDID, F_LOCAT, FILE,
                FILE_GRP, FILE_SEC, FILEID, FPTR, ID, LABEL, LASTMODDATE, LOCTYPE, MD_WRAP, METS, METS_HDR, MIMETYPE,
                NAME, NOTE, ORDER, ORDERLABEL, OTHERTYPE, ROLE, SM_LINK, STRUCT_LINK, STRUCT_MAP, TYPE, USE,
                XML_DATA }) {
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

    private Mets() {
    }
}
