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

package org.kitodo.dataeditor.entities;

import java.util.List;
import java.util.NoSuchElementException;

import org.kitodo.dataeditor.MediaFile;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.MetsType;

public class FileSec extends MetsType.FileSec {

    private final MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    /**
     * Constructor to copy the data from parent class.
     *
     * @param fileSec
     *            The MetsType.FileSec object.
     */
    public FileSec(MetsType.FileSec fileSec) {
        super.fileGrp = fileSec.getFileGrp();
        super.id = fileSec.getID();
    }

    /**
     * Inserts MediaFile objects into fileSec of mets object.
     *
     * @param mediaFiles
     *            The list of media files.
     */
    public void insertMediaFiles(List<MediaFile> mediaFiles) {
        for (MediaFile mediaFile : mediaFiles) {
            insertFileToFileGroupOfMets(mediaFile);
        }
        writeFileIds();
    }

    private void insertFileToFileGroupOfMets(MediaFile mediaFile) {
        FileType.FLocat fLocat = objectFactory.createFileTypeFLocat();
        fLocat.setLOCTYPE(mediaFile.getLocationType().toString());
        fLocat.setHref(mediaFile.getFilePath().getPath());

        FileType fileType = objectFactory.createFileType();
        fileType.setMIMETYPE(mediaFile.getMimeType());
        fileType.getFLocat().add(fLocat);

        getLocalFileGroup().getFile().add(fileType);
    }

    /**
     * Returns the local file group of given mets object as FileGrp object.
     *
     * @return The FileGrp object.
     */
    public MetsType.FileSec.FileGrp getLocalFileGroup() {
        for (MetsType.FileSec.FileGrp fileGrp : this.getFileGrp()) {
            if (fileGrp.getUSE().equals("LOCAL")) {
                return fileGrp;
            }
        }
        throw new NoSuchElementException("No local file group in mets object");
    }

    private void writeFileIds() {
        int counter = 1;
        for (FileType file : getLocalFileGroup().getFile()) {
            file.setID("FILE_" + String.format("%04d", counter));
            counter++;
        }
    }

}
