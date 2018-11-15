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
import java.util.Collections;

/**
 * A programmatically initializable {@code JHoveBase} class.
 */
class KitodoJhoveBase extends JhoveBase {
    /**
     * Initializes a JhoveBase. This needs access to the {@code protected}
     * fields, that’s why it’s a subclass.
     *
     * @param modules
     *            modules to initialize
     * @throws JhoveException
     *             if thrown from {@code super()} constructor
     */
    KitodoJhoveBase(Iterable<String> modules) throws JhoveException {
        super();
        super._encoding = "utf-8";
        modules.forEach(clas -> {
            try {
                Class<?> cl = Class.forName(clas);
                Module module = (Module) cl.newInstance();
                module.setDefaultParams(Collections.emptyList());
                super._moduleMap.put(module.getName().toLowerCase(), module);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    /**
     * Validates a file.
     *
     * @param file
     *            file to validate
     * @param moduleName
     *            name of validation module
     * @param handler
     *            handler to write the output to
     * @throws Exception
     *             if something goes wrong
     */
    void validate(String file, String moduleName, OutputHandler handler) throws Exception {
        super.dispatch(App.newAppWithName("Jhove"), super.getModule(moduleName), null, handler, null,
            new String[] {file });
    }
}
