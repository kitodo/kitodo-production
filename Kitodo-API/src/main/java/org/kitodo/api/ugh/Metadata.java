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

package org.kitodo.api.ugh;

public interface Metadata {

    public DocStruct getDocStruct();

    public MetadataType getType();

    public String getValue();

    public void setDocStruct(DocStruct docStruct);

    public void setType(MetadataType metadataType);

    public void setValue(String value);
}
