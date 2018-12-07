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

package org.kitodo.api.dataformat.mets;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Interface for a service that handles access to the {@code <mets:mets>}
 * element.
 *
 * <p>
 * METS is a schema that describes possible structures of an XML file. Despite
 * many definitions, it is so variable that the same piece of information can be
 * stored in various ways in the file, so that an extended technical use of the
 * information is not readily possible. To fix this shortcoming, so-called
 * application profiles are defined for the individual uses of METS. This
 * interface implements a specific METS application profile, the
 * Kitodo.Production application profile, which is closely related to the ZVDD
 * DFG-Viewer METS Profile, which was defined by the Central Directory of
 * Digitized Prints (ZVDD), an institution of the Lower Saxony State and
 * University Library of the Georg-August-University Goettingen in Germany, and
 * is widely used in Germany. Although this interface uses METS terminology, it
 * can only be meaningfully used to read and write METS XML files that
 * correspond to the Kitodo.Production METS application profile. For this
 * purpose, only external functionality was made available, which is necessary
 * for editing METS XML files in the Kitodo.Production application profile. This
 * saves the user from the internal complexity and richness of the METS file
 * format.
 */
public interface MetsXmlElementAccessInterface {
    /**
     * Returns a service to access the {@code <fileGrp>}. A file grp is an
     * ordered list of media units used to digitally represent a cultural work.
     * The order is of minor importance at this point. It rather describes the
     * order in which the media units are displayed on the workstation of the
     * compiler, which determines the presentation form intended for the
     * consumer (and thus also their presentation order). Mostly this is the
     * order in which each digital part was recorded. The file grp was
     * abstracted from its use at this point, so there is only one file per
     * file. The file elements then provide F locats for various uses. The file
     * grp implements a list of services to access the file.
     *
     * @return a service to access the {@code <fileGrp>}
     */
    List<FileXmlElementAccessInterface> getFileGrp();

    /**
     * Returns a service to access to the logical {@code <structMap>}. The
     * logical struct map is a tree-like structure and forms, similar to a
     * structured table of contents, the entry for the viewer of the finished
     * digital representation of the work of art.
     *
     * <p>
     * From a technical point of view, there may be several logical struct maps
     * in a METS file if the digital representations of a work of art can be
     * combined into several different works of art, as is the case with a
     * palimpsest. This is not currently supported at this point, but could
     * later be expanded simply by extending this getter into a collection of
     * div service interfaces.
     *
     * @return service to access to the logical {@code <structMap>}
     */
    DivXmlElementAccessInterface getStructMap();

    /**
     * Reads a METS file into this service.
     *
     * @param in
     *            open input channel for reading the file
     */
    void read(InputStream in);

    /**
     * Writes the current state of this service to a METS file.
     *
     * @param out
     *            open output channel for writing the file
     */
    void save(OutputStream out);
}
