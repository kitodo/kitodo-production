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

import java.util.List;
import ugh.exceptions.TypeNotAllowedForParentException;

public interface DigitalDocument {

    void addAllContentFiles();

    DocStruct createDocStruct(DocStructType docStructType) throws TypeNotAllowedForParentException;

    FileSet getFileSet();

    DocStruct getLogicalDocStruct();

    DocStruct getPhysicalDocStruct();

    void overrideContentFiles(List<String> images);

    void setLogicalDocStruct(DocStruct docStruct);

    void setPhysicalDocStruct(DocStruct docStruct);

}
