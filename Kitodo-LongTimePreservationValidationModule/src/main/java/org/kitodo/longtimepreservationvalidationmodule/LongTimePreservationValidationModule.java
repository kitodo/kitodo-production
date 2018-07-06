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

package org.kitodo.longtimepreservationvalidationmodule;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.longtimepreservation.FileType;
import org.kitodo.api.validation.longtimepreservation.LongTimePreservationValidationInterface;
import org.kitodo.config.Config;

/**
 * An LongTimePreservationValidationInterface implementation using Jhove.
 */
public class LongTimePreservationValidationModule implements LongTimePreservationValidationInterface {
    /**
     * Returns the matching module name for the given file type.
     */
    @SuppressWarnings("serial")
    private static final Map<FileType, String> MODULE_NAMES = new HashMap<FileType, String>(8) {
        {
            put(FileType.GIF, "GIF-hul");
            put(FileType.JPEG, "JPEG-hul");
            put(FileType.JPEG_2000, "JPEG2000-hul");
            put(FileType.PDF, "PDF-hul");
            put(FileType.PNG, "PNG-gdm");
            put(FileType.TIFF, "TIFF-hul");
        }
    };

    /**
     * Path to the {@code jhove.conf} file
     *
     * <p>
     * Example:
     * LongTimePreservationValidationModule.jhoveConf=/var/lib/tomcat8/webapps/kitodo-production/WEB-INF/classes/jhove.conf
     */
    private static final String PARAMETER_JHOVE_CONF = "LongTimePreservationValidationModule.jhoveConf";

    /**
     * Set to true by JUnit to indicate that the jhove.conf file is in the
     * test/resources folder.
     */
    boolean isUnitTesting = false;

    /**
     * {@inheritDoc}<!-- . -->
     *
     * @param fileUri
     *            file URI to validate
     * @param fileType
     *            file type to validate
     */
    @Override
    public ValidationResult validate(URI fileUri, FileType fileType) {
        KitodoOutputHandler result = new KitodoOutputHandler();
        try {
            JhoveBase jhoveBase = new JhoveBase();
            jhoveBase.init(isUnitTesting ? "src/test/resources/jhove.conf" : Config.getParameter(PARAMETER_JHOVE_CONF),
                JhoveBase.getSaxClassFromProperties());
            jhoveBase.dispatch(App.newAppWithName("Jhove"), jhoveBase.getModule(MODULE_NAMES.get(fileType)), null,
                result, null, new String[] {fileUri.getPath() });
        } catch (Exception e) {
            result.treatException(e);
        }
        return result.toValidationResult();
    }
}
