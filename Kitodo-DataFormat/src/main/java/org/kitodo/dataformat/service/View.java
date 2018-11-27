package org.kitodo.dataformat.service;

import org.kitodo.api.dataformat.mets.AreaXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;

public class View implements AreaXmlElementAccessInterface {

    MediaUnit mediaUnit;

    public View(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    public FileXmlElementAccessInterface getFile() {
        return mediaUnit;
    }

    @Override
    public void setFile(FileXmlElementAccessInterface file) {
        this.mediaUnit = (MediaUnit) file;
    }
}
