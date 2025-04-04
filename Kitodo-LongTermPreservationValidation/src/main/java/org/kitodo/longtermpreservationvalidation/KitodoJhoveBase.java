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

package org.kitodo.longtermpreservationvalidation;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.JhoveException;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A programmatically initializable {@code JHoveBase} class.
 */
class KitodoJhoveBase {

    private static final Logger logger = LogManager.getLogger(KitodoJhoveBase.class);

        /**
     * Modules to initialize.
     */
    private static final List<String> MODULES = Arrays.asList("edu.harvard.hul.ois.jhove.module.GifModule",
        "edu.harvard.hul.ois.jhove.module.Jpeg2000Module", "edu.harvard.hul.ois.jhove.module.JpegModule",
        "edu.harvard.hul.ois.jhove.module.PdfModule", "com.mcgath.jhove.module.PngModule",
        "edu.harvard.hul.ois.jhove.module.TiffModule");

    private static final JhoveBase base = initBase();
    private static final App app = App.newAppWithName("JHOVE");

    private static final JhoveBase initBase() {
        logger.error("initialize jhove base class");
        try {
			JhoveBase base = new JhoveBase();
            base.setEncoding("utf-8");
            for(String moduleClass : MODULES) {
                try {
                    logger.error("creat new module instance " + moduleClass);
                    Class<?> cl = Class.forName(moduleClass);
                    Module module = (Module) cl.getDeclaredConstructor().newInstance();
                    logger.error("set module default params");
                    module.setDefaultParams(Collections.emptyList());
                    logger.error("remember module");
                    base.getModuleMap().put(module.getName().toLowerCase(), module);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
			return base;
		} catch (JhoveException e) {
			throw new IllegalStateException("Couldn't initialise JHOVE base", e);
		}
    }


    /**
     * Validates a file.
     *
     * @param filepath
     *            filepath of file that is validated
     * @param moduleName
     *            name of validation module
     * @param handler
     *            handler to write the output to
     * @throws Exception
     *             if something goes wrong
     */
    public static void validate(String filepath, String moduleName, OutputHandler handler) throws Exception {
        logger.error("KitodoJhoveBase.validate");
        try {
            if (!base.process(app, base.getModule(moduleName), handler, filepath)) {
                // processing error
                logger.error("jhove validation returns false");
            };     
        } catch (Exception e) {
            logger.error("exception while validating with jhove", e);
        }
    }
}
