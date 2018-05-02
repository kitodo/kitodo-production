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

package org.kitodo.dataeditor;

import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.ObjectFactory;

public class MetsKitodoObjectFactory extends ObjectFactory {
    private final String moduleName = "Data Editor";
    private final String applicationName = "Kitodo.Production";
    private final String version = "3.0";

    public MetsType.MetsHdr.Agent createKitodoMetsAgent() {
        MetsType.MetsHdr.Agent metsAgent = super.createMetsTypeMetsHdrAgent();
        metsAgent.setOTHERTYPE("SOFTWARE");
        metsAgent.setROLE("CREATOR");
        metsAgent.setTYPE("OTHER");
        metsAgent.setName(applicationName + " - " + moduleName + " - " + version);
        return metsAgent;
    }
}
