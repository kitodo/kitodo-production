package org.kitodo.dataformat.service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp;

public class MediaVariant implements UseXmlAttributeAccessInterface {

    private String mimeType;
    private String use;

    MediaVariant(FileGrp fileGrp) {
        this.use = fileGrp.getUSE();
        Set<String> mimeTypes = fileGrp.getFile().parallelStream().map(fileType -> fileType.getMIMETYPE())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        switch (mimeTypes.size()) {
            case 0:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + this.use
                        + "\"> does not have any <mets:file> with a MIMETYPE.");
            case 1:
                this.mimeType = mimeTypes.iterator().next();
                break;
            default:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + this.use
                        + "\"> contains differing MIMETYPE values: " + String.join(", ", mimeTypes));
        }
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getUse() {
        return use;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public void setUse(String use) {
        this.use = use;
    }
}
