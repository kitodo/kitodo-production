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

package org.kitodo.helper.metadata;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.ReferenceInterface;

public class LegacyReferenceHelper implements ReferenceInterface {
    private static final Logger logger = LogManager.getLogger(LegacyReferenceHelper.class);

    private LegacyInnerPhysicalDocStructHelper target;

    LegacyReferenceHelper() {

    }

    public LegacyReferenceHelper(LegacyInnerPhysicalDocStructHelper target) {
        this.target = target;
    }

    @Override
    public DocStructInterface getSource() {
        logger.log(Level.TRACE, "getSource()");
        // TODO Auto-generated method stub
        return new LegacyInnerPhysicalDocStructHelper();
    }

    @Override
    public DocStructInterface getTarget() {
        return target;
    }

    @Override
    public String getType() {
        logger.log(Level.TRACE, "getType()");
        // TODO Auto-generated method stub
        return "";
    }
}
