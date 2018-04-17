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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import metsModsKitodo.MdSecType;
import metsModsKitodo.ObjectFactory;
import org.junit.Assert;
import org.junit.Test;

import metsModsKitodo.KitodoType;
import metsModsKitodo.MetadataType;
import metsModsKitodo.StructLinkType;
import org.kitodo.dataeditor.exceptions.DataNotFoundException;

public class MetsModsKitodoTest {

    private File xmlfile = new File("./src/test/resources/testmeta.xml");
    private ObjectFactory objectFactory = new ObjectFactory();

    @Test
    public void addSmLinkTest() {
        String from = "from test";
        String to = "to test";

        MetsModsKitodo metsModsKitodo = new MetsModsKitodo();
        metsModsKitodo.addSmLink(from, to);

        StructLinkType.SmLink smLink = (StructLinkType.SmLink) metsModsKitodo.getMets().getStructLink()
                .getSmLinkOrSmLinkGrp().get(0);

        Assert.assertEquals("'from' value of smLink was wrong", from, smLink.getFrom());
        Assert.assertEquals("'to' value of smLink was wrong", to, smLink.getTo());
    }

    @Test
    public void shouldCreateMetsByFile() throws JAXBException, XMLStreamException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo(xmlfile);
        Assert.assertEquals("Number of dmdSec elements was wrong!", 3, metsModsKitodo.getDmdSecs().size());
    }

    @Test
    public void shouldReadValues() throws JAXBException, XMLStreamException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo(xmlfile);
        String id = metsModsKitodo.getMets().getDmdSec().get(0).getID();
        Assert.assertEquals("Reading id of dmdSec data out of mets was not correct", "DMDLOG_0000", id);
    }

    @Test
    public void shouldReadKitodoMetadata() throws JAXBException, XMLStreamException, DataNotFoundException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo(xmlfile);
        KitodoType kitodoType = metsModsKitodo.getKitodoTypeByMdSecIndex(0);

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getContent().get(0).toString());
    }

    @Test
    public void shouldReadKitodoMetadataById() throws JAXBException, XMLStreamException, DataNotFoundException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo(xmlfile);
        KitodoType kitodoType = metsModsKitodo.getKitodoTypeByMdSecId("DMDLOG_0002");

        MetadataType metadataType = kitodoType.getMetadata().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "TitleDocMain",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "[Seite 157r-181v]",
            metadataType.getContent().get(0).toString());
    }

    @Test(expected = DataNotFoundException.class)
    public void shouldNotReadKitodoMetadataByNotExistingId() throws JAXBException, XMLStreamException, DataNotFoundException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo(xmlfile);
        metsModsKitodo.getKitodoTypeByMdSecId("not existing");
    }

    @Test(expected = DataNotFoundException.class)
    public void shouldNotReadEmptyKitodoMetadataByIndex() throws DataNotFoundException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo();
        metsModsKitodo.getKitodoTypeByMdSecIndex(0);
    }

    @Test(expected = DataNotFoundException.class)
    public void shouldNotCreateKitodoMetadata() throws DataNotFoundException {
        MetsModsKitodo metsModsKitodo = new MetsModsKitodo();
        MdSecType mdSecType = objectFactory.createMdSecType();
        MdSecType.MdWrap mdSecTypeMdWrap = objectFactory.createMdSecTypeMdWrap();
        mdSecTypeMdWrap.setXmlData(objectFactory.createMdSecTypeMdWrapXmlData());
        mdSecType.setMdWrap(mdSecTypeMdWrap);
        metsModsKitodo.getMets().getDmdSec().add(mdSecType);
        metsModsKitodo.getKitodoTypeByMdSecIndex(0);
    }
}
