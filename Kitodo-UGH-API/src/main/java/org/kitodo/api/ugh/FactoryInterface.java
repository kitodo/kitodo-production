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

import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;

public interface FactoryInterface {
    ContentFileInterface createContentFile();

    DigitalDocumentInterface createDigitalDocument();

    MetadataInterface createMetadata(MetadataTypeInterface metadataTypeInterface) throws MetadataTypeNotAllowedException;

    MetadataGroupInterface createMetadataGroup(MetadataGroupTypeInterface metadataGroupTypeInterface) throws MetadataTypeNotAllowedException;

    MetadataGroupTypeInterface createMetadataGroupType();

    MetadataTypeInterface createMetadataType();

    MetsModsInterface createMetsMods(PrefsInterface prefsInterface);

    MetsModsImportExportInterface createMetsModsImportExport(PrefsInterface prefsInterface);

    PersonInterface createPerson(MetadataTypeInterface metadataTypeInterface) throws MetadataTypeNotAllowedException;

    PrefsInterface createPrefs();

    FileformatInterface createRDFFile(PrefsInterface prefsInterface);

    RomanNumeralInterface createRomanNumeral();

    VirtualFileGroupInterface createVirtualFileGroup();

    FileformatInterface createXStream(PrefsInterface prefsInterface) throws PreferencesException;

    PicaPlusInterface createPicaPlus(PrefsInterface prefsInterface);
}
