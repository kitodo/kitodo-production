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

/**
 * Interfaces for services that handles access to METS XML files in the
 * Production application profile format.
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
 *
 * <p>
 * As a basis for the development of this interface, the design decision was
 * made that Production is the only editor of the Kitodo.Production METS
 * application profile, so that only components of the METS that can be produced
 * by Production itself are taken into account. Any additional information will
 * be lost.
 */
package org.kitodo.api.dataformat.mets;
