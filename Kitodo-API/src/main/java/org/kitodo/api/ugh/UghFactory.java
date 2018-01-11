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

package org.kitodo.api.ugh;

import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;

public interface UghFactory {
    ContentFile createContentFile();

    DigitalDocument createDigitalDocument();

    Metadata createMetadata(MetadataType metadataType) throws MetadataTypeNotAllowedException;

    MetadataGroup createMetadataGroup(MetadataGroupType metadataGroupType) throws MetadataTypeNotAllowedException;

    MetadataGroupType createMetadataGroupType();

    MetadataType createMetadataType();

    MetsMods createMetsMods(Prefs prefs);

    MetsModsImportExport createMetsModsImportExport(Prefs prefs);

    Person createPerson(MetadataType metadataType) throws MetadataTypeNotAllowedException;

    Prefs createPrefs();

    Fileformat createRDFFile(Prefs prefs);

    RomanNumeral createRomanNumeral();

    VirtualFileGroup createVirtualFileGroup();

    Fileformat createXStream(Prefs prefs) throws PreferencesException;

    PicaPlus createPicaPlus(Prefs prefs);
}
