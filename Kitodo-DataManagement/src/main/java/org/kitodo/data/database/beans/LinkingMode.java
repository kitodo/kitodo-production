package org.kitodo.data.database.beans;

/**
 * How to link the contents of this folder in a METS fileGrp.
 */
public enum LinkingMode {
    /**
     * A common fileGrp is created, all images will be linked.
     */
    ALL,

    /**
     * The folder is validated, only existing images will be linked.
     */
    EXISTING,

    /**
     * The folder will not be mapped to a file group.
     */
    NO,

    /**
     * Only the selected preview image will be linked in the file group.
     */
    PREVIEW_IMAGE;
}
