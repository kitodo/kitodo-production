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

package org.kitodo.production.helper.metadata.legacytypeimplementations;

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

/**
 * Connects the UGH factory interface to the legacy helper classes. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyReplacementFactoryHelper implements FactoryInterface {
    private static final Logger logger = LogManager.getLogger(LegacyReplacementFactoryHelper.class);

    @Override
    public ContentFileInterface createContentFile() {
        return new LegacyContentFileHelper();
    }

    @Override
    public DigitalDocumentInterface createDigitalDocument() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public MetadataInterface createMetadata(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException {
        return new LegacyMetadataHelper(metadataType);
    }

    @Override
    public MetadataGroupInterface createMetadataGroup(MetadataGroupTypeInterface metadataGroupType)
            throws MetadataTypeNotAllowedException {

        UnsupportedOperationException e = new UnsupportedOperationException("MetadataGroupInterface is not supported");
        throw andLog(new UnsupportedOperationException("MetadataGroupInterface is not supported"));
    }

    @Override
    public MetadataGroupTypeInterface createMetadataGroupType() {
        throw andLog(new UnsupportedOperationException("Metadata group type is not supported"));
    }

    @Override
    public MetadataTypeInterface createMetadataType() {
        throw andLog(new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public MetsModsInterface createMetsMods(PrefsInterface prefs) throws PreferencesException {
        return new LegacyMetsModsDigitalDocumentHelper(((LegacyPrefsHelper) prefs).getRuleset());
    }

    @Override
    public MetsModsImportExportInterface createMetsModsImportExport(PrefsInterface prefs) throws PreferencesException {
        throw andLog(new UnsupportedOperationException("METS/MODS import/export is not supported"));
    }

    @Override
    public PersonInterface createPerson(MetadataTypeInterface metadataType) throws MetadataTypeNotAllowedException {
        throw andLog(new UnsupportedOperationException("Person is not supported"));
    }

    @Override
    public PicaPlusInterface createPicaPlus(PrefsInterface prefs) {
        throw andLog(new UnsupportedOperationException("PICA+ is not supported"));
    }

    @Override
    public PrefsInterface createPrefs() {
        return new LegacyPrefsHelper();
    }

    @Override
    public FileformatInterface createRDFFile(PrefsInterface prefs) throws PreferencesException {
        throw andLog(new UnsupportedOperationException("RDF file is not supported"));
    }

    @Override
    public RomanNumeralInterface createRomanNumeral() {
        return new LegacyRomanNumeralHelper();
    }

    @Override
    public VirtualFileGroupInterface createVirtualFileGroup() {
        throw andLog(new UnsupportedOperationException("Virtual file group is not supported"));
    }

    @Override
    public FileformatInterface createXStream(PrefsInterface prefs) throws PreferencesException {
        throw andLog(new UnsupportedOperationException("XStream is not supported"));
    }

    /**
     * This method generates a comprehensible log message in case something was
     * overlooked and one of the unimplemented methods should ever be called in
     * operation. The name was chosen deliberately short in order to keep the
     * calling code clear. This method must be implemented in every class
     * because it uses the logger tailored to the class.
     * 
     * @param exception
     *            created {@code UnsupportedOperationException}
     * @return the exception
     */
    private static RuntimeException andLog(UnsupportedOperationException exception) {
        StackTraceElement[] stackTrace = exception.getStackTrace();
        StringBuilder buffer = new StringBuilder(255);
        buffer.append(stackTrace[1].getClassName());
        buffer.append('.');
        buffer.append(stackTrace[1].getMethodName());
        buffer.append("()");
        if (stackTrace[1].getLineNumber() > -1) {
            buffer.append(" line ");
            buffer.append(stackTrace[1].getLineNumber());
        }
        buffer.append(" unexpectedly called unimplemented ");
        buffer.append(stackTrace[0].getMethodName());
        buffer.append("()");
        if (exception.getMessage() != null) {
            buffer.append(": ");
            buffer.append(exception.getMessage());
        }
        logger.error(buffer.toString());
        return exception;
    }
}
