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

public interface MetadataInterface {

    public DocStructInterface getDocStruct();

    public MetadataTypeInterface getType();

    public String getValue();

    public void setDocStruct(DocStructInterface docStructInterface);

    /**
     * @return always {@code true}. The result is never used.
     */
    public boolean setType(MetadataTypeInterface metadataTypeInterface);

    /**
     * @return always {@code true}. The result is never used.
     */
    public boolean setValue(String value);
}
