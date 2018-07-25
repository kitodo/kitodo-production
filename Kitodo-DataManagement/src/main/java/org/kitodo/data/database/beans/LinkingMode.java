package org.kitodo.data.database.beans;

/**
 * Different ways how to link the contents of a folder in a METS fileGrp.
 */
public enum LinkingMode {
    /**
     * A common fileGrp is created, all images will be linked, even if they have
     * not yet been physically added on the drive.
     */
    ALL,

    /**
     * The folder is validated, only images existing on the drive will be
     * linked.
     */
    EXISTING,

    /**
     * The folder will not be mapped to a fileGrp at all.
     */
    NO,

    /**
     * Only the selected preview image will be linked in the fileGrp.
     */
    PREVIEW_IMAGE;
}
