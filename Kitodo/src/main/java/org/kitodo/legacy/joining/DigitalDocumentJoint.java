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

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.DocStructTypeInterface;
import org.kitodo.api.ugh.FileSetInterface;

public class DigitalDocumentJoint implements DigitalDocumentInterface {
    private static final Logger logger = LogManager.getLogger(DigitalDocumentJoint.class);

    @Override
    public void addAllContentFiles() {
        logger.log(Level.TRACE, "addAllContentFiles()");
        // TODO Auto-generated method stub
    }

    @Override
    public DocStructInterface createDocStruct(DocStructTypeInterface docStructType) {
        logger.log(Level.TRACE, "createDocStruct(docStructType: {})", docStructType);
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public FileSetInterface getFileSet() {
        logger.log(Level.TRACE, "getFileSet()");
        // TODO Auto-generated method stub
        return new FileSetJoint();
    }

    @Override
    public DocStructInterface getLogicalDocStruct() {
        logger.log(Level.TRACE, "getLogicalDocStruct()");
        // TODO Auto-generated method stub
        return new LogicalDocStructJoint();
    }

    @Override
    public DocStructInterface getPhysicalDocStruct() {
        logger.log(Level.TRACE, "getPhysicalDocStruct()");
        // TODO Auto-generated method stub
        return new PhysicalDocStructJoint();
    }

    @Override
    public void overrideContentFiles(List<String> images) {
        logger.log(Level.TRACE, "overrideContentFiles(images: {})", images);
        // TODO Auto-generated method stub
    }

    @Override
    public void setLogicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setLogicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }

    @Override
    public void setPhysicalDocStruct(DocStructInterface docStruct) {
        logger.log(Level.TRACE, "setPhysicalDocStruct(docStruct: {})", docStruct);
        // TODO Auto-generated method stub
    }
}
