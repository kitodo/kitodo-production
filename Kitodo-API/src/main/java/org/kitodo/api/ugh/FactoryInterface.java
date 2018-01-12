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

/**
 * Factory interface comprising the API constructors.
 *
 * @see "https://en.wikipedia.org/wiki/Abstract_factory_pattern"
 */
public interface FactoryInterface {
    /**
     * Creates a new content file.
     *
     * @return the new content file
     */
    ContentFileInterface createContentFile();

    /**
     * Creates a new digital document.
     *
     * @return the new digital document
     */
    DigitalDocumentInterface createDigitalDocument();

    /**
     * Creates a new metadata entry.
     *
     * @param metadataType
     *            the type of the entry
     * @return the new metadata entry
     * @throws MetadataTypeNotAllowedException
     *             if the type is {@code null}
     */
    MetadataInterface createMetadata(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException;

    /**
     * Creates a new metadata group.
     *
     * @param metadataGroupType
     *            the type of the metadata group
     * @return the new metadata group
     * @throws MetadataTypeNotAllowedException
     *             if the type is {@code null}
     */
    MetadataGroupInterface createMetadataGroup(MetadataGroupTypeInterface metadataGroupTypeInterface)
            throws MetadataTypeNotAllowedException;

    /**
     * Creates a new, empty metadata group type.
     *
     * @return the new metadata group type
     */
    MetadataGroupTypeInterface createMetadataGroupType();

    /**
     * Creates a new, empty metadata type.
     *
     * @return the new metadata type
     */
    MetadataTypeInterface createMetadataType();

    /**
     * Creates a new METS-intern read-writer.
     *
     * @param prefs
     *            ruleset to base the read-writer on
     * @return the new METS read-writer
     * @throws PreferencesException
     *             if there is no {@code <METS>} section in the ruleset
     */
    MetsModsInterface createMetsMods(PrefsInterface prefs) throws PreferencesException;

    /**
     * Creates a new METS/MODS export writer.
     *
     * @param prefs
     *            ruleset to base the writer on
     * @return the new METS read-writer
     * @throws PreferencesException
     *             if there is no {@code <METS>} section in the ruleset
     */
    MetsModsImportExportInterface createMetsModsImportExport(PrefsInterface prefs) throws PreferencesException;

    /**
     * Creates a new person metadata type.
     *
     * @return the new metadata type
     */
    PersonInterface createPerson(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException;

    /**
     * Creates a new PICA plus import reader.
     *
     * @param prefs
     *            ruleset to base the reader on
     * @return the new PICA plus reader
     */
    PicaPlusInterface createPicaPlus(PrefsInterface prefs);

    /**
     * Creates a new, empty rule set.
     *
     * @return the new rule set.
     */
    PrefsInterface createPrefs();

    /**
     * Creates a new Agora-RDF read-writer.
     *
     * @param prefs
     *            ruleset to base the read-writer on
     * @return the new RDF read-writer
     * @throws PreferencesException
     *             if there is no {@code <RDF>} section in the ruleset
     */
    FileformatInterface createRDFFile(PrefsInterface prefs) throws PreferencesException;

    /**
     * Creates a new roman numeral with a value of I.
     *
     * @return the new roman numeral
     */
    RomanNumeralInterface createRomanNumeral();

    /**
     * Creates a new virtual file group.
     *
     * @return the new virtual file group
     */
    VirtualFileGroupInterface createVirtualFileGroup();

    /**
     * Creates a new XStream-intern read-writer.
     *
     * @param prefs
     *            ruleset to base the read-writer on
     * @return the new XStream read-writer
     * @throws PreferencesException
     *             is never thrown
     */
    FileformatInterface createXStream(PrefsInterface prefs) throws PreferencesException;
}
