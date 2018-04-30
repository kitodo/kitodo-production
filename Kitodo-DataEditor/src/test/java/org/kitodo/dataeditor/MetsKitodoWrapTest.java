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

import java.io.IOException;
import java.net.URI;
import java.util.NoSuchElementException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataGroupType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.ObjectFactory;
import org.kitodo.dataformat.metskitodo.StructLinkType;

public class MetsKitodoWrapTest {

    private URI xmlfile = URI.create("./src/test/resources/testmeta.xml");
    private ObjectFactory objectFactory = new ObjectFactory();

    @Test
    public void addSmLinkTest() {
        String from = "from test";
        String to = "to test";

        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap();
        metsKitodoWrap.addSmLink(from, to);

        StructLinkType.SmLink smLink = (StructLinkType.SmLink) metsKitodoWrap.getMets().getStructLink()
                .getSmLinkOrSmLinkGrp().get(0);

        Assert.assertEquals("'from' value of smLink was wrong", from, smLink.getFrom());
        Assert.assertEquals("'to' value of smLink was wrong", to, smLink.getTo());
    }

    @Test
    public void shouldCreateMetsByFile() throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        Assert.assertEquals("Number of dmdSec elements was wrong!", 3, metsKitodoWrap.getDmdSecs().size());
    }

    @Test
    public void shouldReadValues() throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        String id = metsKitodoWrap.getMets().getDmdSec().get(0).getID();
        Assert.assertEquals("Reading id of dmdSec data out of mets was not correct", "DMDLOG_0000", id);
    }

    @Test
    public void shouldReadKitodoMetadata() throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecIndex(0);

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getValue());
    }

    @Test
    public void shouldReadKitodoMetadataGroup() throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecIndex(0);

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().get(0).getMetadataGroup().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "subTypIdentifierPPN",
            metadataGroupType.getMetadata().get(1).getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "sub10457187X",
            metadataGroupType.getMetadata().get(1).getValue());
    }

    @Test
    public void shouldReadGoobiMetadata() throws JAXBException, TransformerException, IOException {
        URI oldXmlfile = URI.create("./src/test/resources/testmetaOldFormat.xml");
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(oldXmlfile);
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecIndex(0);

        MetadataType metadataType = kitodoType.getMetadata().get(1);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "PublisherName",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "Test Publisher",
            metadataType.getValue());
    }

    @Test
    public void shouldReadGoobiMetadataGroup() throws JAXBException, TransformerException, IOException {
        URI oldXmlfile = URI.create("./src/test/resources/testmetaOldFormat.xml");
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(oldXmlfile);
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecIndex(0);

        MetadataGroupType metadataGroupType = kitodoType.getMetadataGroup().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "typIdentifierPPN",
            metadataGroupType.getMetadata().get(1).getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "10457187X",
            metadataGroupType.getMetadata().get(1).getValue());
    }

    @Test
    public void shouldReadKitodoMetadataById()
            throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        KitodoType kitodoType = metsKitodoWrap.getKitodoTypeByMdSecId("DMDLOG_0002");

        MetadataType metadataType = kitodoType.getMetadata().get(0);
        Assert.assertEquals("Reading data of type 'name' out of kitodo format was not correct", "TitleDocMain",
            metadataType.getName());
        Assert.assertEquals("Reading content metadata out of kitodo format was not correct", "[Seite 157r-181v]",
            metadataType.getValue());
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldNotReadKitodoMetadataByNotExistingId()
            throws JAXBException, TransformerException, IOException {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap(xmlfile);
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("MdSec element with id: not existing was not found");
        metsKitodoWrap.getKitodoTypeByMdSecId("not existing");
    }

    @Test
    public void shouldNotReadNotExistingMdSecByIndex() {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap();
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("MdSec element with index: 0 does not exist");
        metsKitodoWrap.getKitodoTypeByMdSecIndex(0);
    }

    @Test
    public void shouldNotReadNotExistingKitodoMetadataByIndex() {
        MetsKitodoWrap metsKitodoWrap = new MetsKitodoWrap();
        MdSecType mdSecType = objectFactory.createMdSecType();
        MdSecType.MdWrap mdSecTypeMdWrap = objectFactory.createMdSecTypeMdWrap();
        mdSecTypeMdWrap.setXmlData(objectFactory.createMdSecTypeMdWrapXmlData());
        mdSecType.setMdWrap(mdSecTypeMdWrap);
        metsKitodoWrap.getMets().getDmdSec().add(mdSecType);
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("MdSec element with index: 0 does not have kitodo metadata");
        metsKitodoWrap.getKitodoTypeByMdSecIndex(0);
    }
}
