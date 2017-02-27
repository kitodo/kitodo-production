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

package org.goobi.production.importer;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
/**
 *
 * @author Igor Toker
 *
 */
@XStreamAlias("property")
public class FireburnProperty {
    @XStreamAsAttribute
    @XStreamAlias("cdName")
    public String cdName;
    @XStreamAsAttribute
    @XStreamAlias("titel")
    public String titel;
    @XStreamAsAttribute
    @XStreamAlias("date")
    public String date;

    //Anzahl der Cd's
    @XStreamAlias("cdnumber")
    @XStreamAsAttribute
    public int cdnumber = 1;
    @XStreamAlias("size")
    @XStreamAsAttribute
    public long size;
    @XStreamAlias("type")
    @XStreamAsAttribute
    public String type;

    public FireburnProperty(String cdName, String titel, String date, int cdnumber, String type, long size) {
        super();
        this.cdName = cdName;
        this.titel = titel;
        this.date = date;

        this.cdnumber = cdnumber;
        this.type = type;
        this.size = size;
    }


}
