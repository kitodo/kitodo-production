package org.kitodo.dataformat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MediaUnit implements FileXmlElementAccessInterface {

    private Map<MediaVariant, MediaFile> mediaFiles = new HashMap<>();
    private int order;
    private String orderlabel;

    public MediaUnit(DivType div, Mets mets, Map<String, MediaVariant> mediaVariants) {
        mediaFiles = div.getFptr().parallelStream().map(Fptr::getFILEID).filter(object -> object instanceof FileType)
                .map(object -> (FileType) object)
                .collect(Collectors.toMap(
                    file -> mediaVariants.get(mets.getFileSec().getFileGrp().parallelStream()
                            .filter(fileGrp -> fileGrp.getFile().contains(file)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Corrupt file: <mets:fptr> not referenced in <mets:fileGrp>"))
                            .getUSE()),
                    file -> new MediaFile(file.getFLocat().get(0))));
        order = div.getORDER().intValue();
        orderlabel = div.getORDERLABEL();
    }

    @Override
    public Set<? extends Entry<? extends UseXmlAttributeAccessInterface, ? extends FLocatXmlElementAccessInterface>> getAllUsesWithFLocats() {
        return mediaFiles.entrySet();
    }

    @Override
    public FLocatXmlElementAccessInterface getFLocatForUse(UseXmlAttributeAccessInterface use) {
        return mediaFiles.get(use);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getOrderlabel() {
        return orderlabel;
    }

    @Override
    public void putFLocatForUse(UseXmlAttributeAccessInterface use, FLocatXmlElementAccessInterface fLocat) {
        mediaFiles.put((MediaVariant) use, (MediaFile) fLocat);

    }

    @Override
    public void removeFLocatForUse(UseXmlAttributeAccessInterface use) {
        mediaFiles.remove(use);

    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }
}
