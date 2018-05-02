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

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.DateTime;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.ObjectFactory;

/**
 * This class provides methods for writing Mets objects to xml files.
 */
public class MetsKitodoWriter {

    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    public void save(Mets mets, URI file) throws JAXBException, DatatypeConfigurationException {
        mets = insertMetsHeaderIfNotExist(mets);
        mets = updateLastModDate(mets);
        writeMetsData(mets, file);
    }

    private void writeMetsData(Mets mets, URI file) throws JAXBException {
        JAXBContext jaxbMetsContext = JAXBContext.newInstance(Mets.class);
        Marshaller jaxbMetsMarshaller = jaxbMetsContext.createMarshaller();
        jaxbMetsMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new MetsKitodoPrefixMapper());
        jaxbMetsMarshaller.marshal(mets, new File(file));
    }

    private Mets insertMetsHeaderIfNotExist(Mets mets) throws DatatypeConfigurationException {
        if (Objects.isNull(mets.getMetsHdr())) {

            mets.setMetsHdr(objectFactory.createMetsTypeMetsHdr());
            mets.getMetsHdr().setCREATEDATE(XmlUtils.getXmlTime());

            MetsType.MetsHdr.Agent metsAgent = objectFactory.createKitodoMetsAgent();
            mets.getMetsHdr().getAgent().add(metsAgent);
        }
        return mets;
    }

    private Mets updateLastModDate(Mets mets) throws DatatypeConfigurationException {
        mets.getMetsHdr().setLASTMODDATE(XmlUtils.getXmlTime());
        return mets;
    }
}
