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

package org.kitodo.legacy.joining;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.ReferenceInterface;

public class ReferenceJoint implements ReferenceInterface {
    private static final Logger logger = LogManager.getLogger(PhysicalDocStructJoint.class);

    @Override
    public DocStructInterface getSource() {
        logger.log(Level.TRACE, "getSource()");
        // TODO Auto-generated method stub
        return new PhysicalDocStructJoint();
    }

    @Override
    public DocStructInterface getTarget() {
        logger.log(Level.TRACE, "getTarget()");
        // TODO Auto-generated method stub
        return new PhysicalDocStructJoint();
    }

    @Override
    public String getType() {
        logger.log(Level.TRACE, "getType()");
        // TODO Auto-generated method stub
        return "";
    }
}
