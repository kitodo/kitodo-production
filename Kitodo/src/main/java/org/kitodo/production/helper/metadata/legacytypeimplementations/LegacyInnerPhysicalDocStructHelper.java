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

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;

/**
 * Connects a legacy doc struct from the physical map to a media unit. This is a
 * soldering class to keep legacy code operational which is about to be removed.
 * Do not use this class.
 */
public class LegacyInnerPhysicalDocStructHelper implements LegacyDocStructHelperInterface {
    private static final Logger logger = LogManager.getLogger(LegacyInnerPhysicalDocStructHelper.class);

    private static final MetsService metsService = ServiceManager.getMetsService();

    /**
     * A constant for local use. We cannot make this constant constant because
     * the service loader is an instance variable.
     */
    @Deprecated
    public final MediaVariant local = new MediaVariant();

    /**
     * The media unit accessed via this soldering class.
     */
    private MediaUnit mediaUnit;

    @Deprecated
    public LegacyInnerPhysicalDocStructHelper() {
        this.mediaUnit = new MediaUnit();
    }

    @Deprecated
    public LegacyInnerPhysicalDocStructHelper(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    @Deprecated
    public void addContentFile(LegacyContentFileHelper contentFile) {
        mediaUnit.getMediaFiles().put(local, ((LegacyContentFileHelper) contentFile).getMediaFile());
    }

    @Override
    @Deprecated
    public void addMetadata(LegacyMetadataHelper metadata) {
        if (LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER.equals(metadata.getMetadataType())) {
            mediaUnit.setOrder(Integer.parseInt(metadata.getValue()));
        } else if (LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL.equals(metadata.getMetadataType())) {
            mediaUnit.setOrderlabel(metadata.getValue());
        } else {
            logger.log(Level.TRACE, "addMetadata(metadata: {})", metadata);
        }
    }

    @Override
    @Deprecated
    public List<LegacyDocStructHelperInterface> getAllChildren() {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadata() {
        return Arrays.asList(
            new LegacyMetadataHelper(this, LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER,
                    Integer.toString(mediaUnit.getOrder())),
            new LegacyMetadataHelper(this, LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL,
                    mediaUnit.getOrderlabel()));
    }

    @Override
    @Deprecated
    public List<LegacyMetadataHelper> getAllMetadataByType(LegacyMetadataTypeHelper metadataType) {
        if (metadataType == LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDER) {
            return Objects.nonNull(mediaUnit)
                    ? Arrays.asList(
                        new LegacyMetadataHelper(this, metadataType, Integer.toString(mediaUnit.getOrder())))
                    : Collections.emptyList();
        } else if (metadataType == LegacyMetadataTypeHelper.SPECIAL_TYPE_ORDERLABEL) {
            return Objects.nonNull(mediaUnit) && Objects.nonNull(mediaUnit.getOrderlabel())
                    ? Arrays.asList(new LegacyMetadataHelper(this, metadataType, mediaUnit.getOrderlabel()))
                    : Collections.emptyList();
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    @Override
    @Deprecated
    public List<LegacyReferenceHelper> getAllReferences(String direction) {
        /*
         * Although the method is called because the same loop is used for
         * logical and physical structure elements, it must come back empty.
         */
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public List<LegacyMetadataTypeHelper> getDisplayMetadataTypes() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public String getImageName() {
        URI uri = mediaUnit.getMediaFiles().get(local);
        return new File(uri.getPath()).getName();
    }

    MediaUnit getMediaUnit() {
        return mediaUnit;
    }

    @Override
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getDocStructType() {
        return LegacyInnerPhysicalDocStructTypePageHelper.INSTANCE;
    }

    /**
     * This method is not part of the interface, but the JSF code digs in the
     * depths of the UGH and uses it on the guts.
     * 
     * @return Method delegated to {@link #getDocStructType()}
     */
    @Deprecated
    public LegacyLogicalDocStructTypeHelper getType() {
        if (!logger.isTraceEnabled()) {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            logger.log(Level.WARN, "Method {}.{}() invokes {}.{}(), bypassing the interface!",
                stackTrace[1].getClassName(), stackTrace[1].getMethodName(), stackTrace[0].getClassName(),
                stackTrace[0].getMethodName());
        }
        return getDocStructType();
    }
}
