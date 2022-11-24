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

//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.11.22 um 01:29:20 PM CET 
//


package org.kitodo.dataformat.metskitodo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java-Klasse für kitodoType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="kitodoType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="metadata" type="{http://meta.kitodo.org/v1/}metadataType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="metadataGroup" type="{http://meta.kitodo.org/v1/}metadataGroupType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "kitodoType", namespace = "http://meta.kitodo.org/v1/", propOrder = {
    "metadata",
    "metadataGroup"
})
public class KitodoType implements Equals2
{

    protected List<MetadataType> metadata;
    protected List<MetadataGroupType> metadataGroup;
    @XmlAttribute(name = "version")
    protected String version;

    /**
     * Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataType }
     * 
     * 
     */
    public List<MetadataType> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<MetadataType>();
        }
        return this.metadata;
    }

    /**
     * Gets the value of the metadataGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadataGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadataGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataGroupType }
     * 
     * 
     */
    public List<MetadataGroupType> getMetadataGroup() {
        if (metadataGroup == null) {
            metadataGroup = new ArrayList<MetadataGroupType>();
        }
        return this.metadataGroup;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final KitodoType that = ((KitodoType) object);
        {
            List<MetadataType> lhsMetadata;
            lhsMetadata = (((this.metadata!= null)&&(!this.metadata.isEmpty()))?this.getMetadata():null);
            List<MetadataType> rhsMetadata;
            rhsMetadata = (((that.metadata!= null)&&(!that.metadata.isEmpty()))?that.getMetadata():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "metadata", lhsMetadata), LocatorUtils.property(thatLocator, "metadata", rhsMetadata), lhsMetadata, rhsMetadata, ((this.metadata!= null)&&(!this.metadata.isEmpty())), ((that.metadata!= null)&&(!that.metadata.isEmpty())))) {
                return false;
            }
        }
        {
            List<MetadataGroupType> lhsMetadataGroup;
            lhsMetadataGroup = (((this.metadataGroup!= null)&&(!this.metadataGroup.isEmpty()))?this.getMetadataGroup():null);
            List<MetadataGroupType> rhsMetadataGroup;
            rhsMetadataGroup = (((that.metadataGroup!= null)&&(!that.metadataGroup.isEmpty()))?that.getMetadataGroup():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "metadataGroup", lhsMetadataGroup), LocatorUtils.property(thatLocator, "metadataGroup", rhsMetadataGroup), lhsMetadataGroup, rhsMetadataGroup, ((this.metadataGroup!= null)&&(!this.metadataGroup.isEmpty())), ((that.metadataGroup!= null)&&(!that.metadataGroup.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsVersion;
            lhsVersion = this.getVersion();
            String rhsVersion;
            rhsVersion = that.getVersion();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "version", lhsVersion), LocatorUtils.property(thatLocator, "version", rhsVersion), lhsVersion, rhsVersion, (this.version!= null), (that.version!= null))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy2 strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

}
