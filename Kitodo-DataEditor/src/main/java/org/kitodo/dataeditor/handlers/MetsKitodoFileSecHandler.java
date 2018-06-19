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

package org.kitodo.dataeditor.handlers;

import java.util.List;
import java.util.NoSuchElementException;

import org.kitodo.dataeditor.MediaFile;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;

public class MetsKitodoFileSecHandler {

    private MetsKitodoFileSecHandler() {
    }

    private static final MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    /**
     * Inserts MediaFile objects into fileSec of mets object.
     *
     * @param mets
     *            The Mets object.
     * @param mediaFiles
     *            The list of media files.
     */
    public static void insertMediaFilesToLocalFileGroupOfMets(Mets mets, List<MediaFile> mediaFiles) {
        for (MediaFile mediaFile : mediaFiles) {
            insertFileToFileGroupOfMets(mets, mediaFile);
        }
        writeFileIdsToMets(mets);
    }

    private static void insertFileToFileGroupOfMets(Mets mets, MediaFile mediaFile) {
        FileType.FLocat fLocat = objectFactory.createFileTypeFLocat();
        fLocat.setLOCTYPE(mediaFile.getLocationType().toString());
        fLocat.setHref(mediaFile.getFilePath().getPath());

        FileType fileType = objectFactory.createFileType();
        fileType.setMIMETYPE(mediaFile.getMimeType());
        fileType.getFLocat().add(fLocat);

        getLocalFileGroupOfMets(mets).getFile().add(fileType);
    }

    /**
     * Returns the local file group of given mets object as FileGrp object.
     *
     * @param mets
     *            The Mets object.
     * @return The FileGrp object.
     */
    public static MetsType.FileSec.FileGrp getLocalFileGroupOfMets(Mets mets) {
        for (MetsType.FileSec.FileGrp fileGrp : mets.getFileSec().getFileGrp()) {
            if (fileGrp.getUSE().equals("LOCAL")) {
                return fileGrp;
            }
        }
        throw new NoSuchElementException("No local file group in mets object");
    }

    private static void writeFileIdsToMets(Mets mets) {
        int counter = 1;
        for (FileType file : mets.getFileSec().getFileGrp().get(0).getFile()) {
            file.setID("FILE_" + String.format("%04d", counter));
            counter++;
        }
    }
}
