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

package org.kitodo.legacy.joining;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.ContentFileInterface;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.FactoryInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataGroupInterface;
import org.kitodo.api.ugh.MetadataGroupTypeInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.MetadataTypeInterface;
import org.kitodo.api.ugh.MetsModsImportExportInterface;
import org.kitodo.api.ugh.MetsModsInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.PicaPlusInterface;
import org.kitodo.api.ugh.PrefsInterface;
import org.kitodo.api.ugh.RomanNumeralInterface;
import org.kitodo.api.ugh.VirtualFileGroupInterface;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;

public class ReplacementFactory implements FactoryInterface {
    private static final Logger logger = LogManager.getLogger(ReplacementFactory.class);

    @Override
    public ContentFileInterface createContentFile() {
        logger.log(Level.TRACE, "createContentFile()");
        // TODO Auto-generated method stub
        return new ContentFileJoint();
    }

    @Override
    public DigitalDocumentInterface createDigitalDocument() {
        logger.log(Level.TRACE, "createDigitalDocument()");
        // TODO Auto-generated method stub
        return new DigitalMetsKitodoDocumentJoint();
    }

    @Override
    public MetadataInterface createMetadata(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "createMetadata(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
        return new MetadataJoint();
    }

    @Override
    public MetadataGroupInterface createMetadataGroup(MetadataGroupTypeInterface metadataGroupType)
            throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "createMetadataGroup(metadataGroupType: {})", metadataGroupType);
        // TODO Auto-generated method stub
        return new MetadataGroupJoint();
    }

    @Override
    public MetadataGroupTypeInterface createMetadataGroupType() {
        logger.log(Level.TRACE, "createMetadataGroupType()");
        // TODO Auto-generated method stub
        return new MetadataGroupTypeJoint();
    }

    @Override
    public MetadataTypeInterface createMetadataType() {
        logger.log(Level.TRACE, "createMetadataType()");
        // TODO Auto-generated method stub
        return new MetadataTypeJoint();
    }

    @Override
    public MetsModsInterface createMetsMods(PrefsInterface prefs) throws PreferencesException {
        return new DigitalMetsKitodoDocumentJoint(((PrefsJoint) prefs).ruleset);
    }

    @Override
    public MetsModsImportExportInterface createMetsModsImportExport(PrefsInterface prefs) throws PreferencesException {
        logger.log(Level.TRACE, "createMetsModsImportExport(prefs: {})", prefs);
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: METS/MODS import/export is not supported");
        throw new UnsupportedOperationException("MetsModsImportExportInterface is not supported");
    }

    @Override
    public PersonInterface createPerson(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException {
        logger.log(Level.TRACE, "createPerson(metadataType: {})", metadataType);
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: Person is not supported");
        throw new UnsupportedOperationException("Person is not supported");
    }

    @Override
    public PicaPlusInterface createPicaPlus(PrefsInterface prefs) {
        logger.log(Level.TRACE, "createPicaPlus(prefs: {})", prefs);
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: PICA+ is not supported");
        throw new UnsupportedOperationException("PicaPlus is not supported");
    }

    @Override
    public PrefsInterface createPrefs() {
        logger.log(Level.TRACE, "createPrefs()");
        // TODO Auto-generated method stub
        return new PrefsJoint();
    }

    @Override
    public FileformatInterface createRDFFile(PrefsInterface prefs) throws PreferencesException {
        logger.log(Level.TRACE, "createRDFFile(prefs: {})", prefs);
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: RDF file is not supported");
        throw new UnsupportedOperationException("RDFFile is not supported");
    }

    @Override
    public RomanNumeralInterface createRomanNumeral() {
        logger.log(Level.TRACE, "createRomanNumeral()");
        // TODO Auto-generated method stub
        return new RomanNumeralJoint();
    }

    @Override
    public VirtualFileGroupInterface createVirtualFileGroup() {
        logger.log(Level.TRACE, "createVirtualFileGroup()");
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: Virtual file group is not supported");
        throw new UnsupportedOperationException("VirtualFileGroup is not supported");
    }

    @Override
    public FileformatInterface createXStream(PrefsInterface prefs) throws PreferencesException {
        logger.log(Level.TRACE, "createXStream(prefs: {})", prefs);
        // TODO Auto-generated method stub
        logger.error("UnsupportedOperationException: XStream file group is not supported");
        throw new UnsupportedOperationException("XStream is not supported");
    }

}
