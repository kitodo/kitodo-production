//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2022.11.22 um 01:29:20 PM CET 
//


package org.kitodo.dataformat.metskitodo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * amdSecType: Complex Type for Administrative Metadata Sections
 * 			The administrative metadata section consists of four possible subsidiary sections: techMD (technical metadata for text/image/audio/video files), rightsMD (intellectual property rights metadata), sourceMD (analog/digital source metadata), and digiprovMD (digital provenance metadata, that is, the history of migrations/translations performed on a digital library object from it's original digital capture/encoding).
 * 			
 * 
 * <p>Java-Klasse für amdSecType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="amdSecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="techMD" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="rightsMD" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="sourceMD" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="digiprovMD" type="{http://www.loc.gov/METS/}mdSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "amdSecType", propOrder = {
    "techMD",
    "rightsMD",
    "sourceMD",
    "digiprovMD"
})
public class AmdSecType implements Equals2
{

    protected List<MdSecType> techMD;
    protected List<MdSecType> rightsMD;
    protected List<MdSecType> sourceMD;
    protected List<MdSecType> digiprovMD;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the techMD property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the techMD property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTechMD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getTechMD() {
        if (techMD == null) {
            techMD = new ArrayList<MdSecType>();
        }
        return this.techMD;
    }

    /**
     * Gets the value of the rightsMD property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rightsMD property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRightsMD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getRightsMD() {
        if (rightsMD == null) {
            rightsMD = new ArrayList<MdSecType>();
        }
        return this.rightsMD;
    }

    /**
     * Gets the value of the sourceMD property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sourceMD property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSourceMD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getSourceMD() {
        if (sourceMD == null) {
            sourceMD = new ArrayList<MdSecType>();
        }
        return this.sourceMD;
    }

    /**
     * Gets the value of the digiprovMD property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the digiprovMD property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDigiprovMD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MdSecType }
     * 
     * 
     */
    public List<MdSecType> getDigiprovMD() {
        if (digiprovMD == null) {
            digiprovMD = new ArrayList<MdSecType>();
        }
        return this.digiprovMD;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final AmdSecType that = ((AmdSecType) object);
        {
            List<MdSecType> lhsTechMD;
            lhsTechMD = (((this.techMD!= null)&&(!this.techMD.isEmpty()))?this.getTechMD():null);
            List<MdSecType> rhsTechMD;
            rhsTechMD = (((that.techMD!= null)&&(!that.techMD.isEmpty()))?that.getTechMD():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "techMD", lhsTechMD), LocatorUtils.property(thatLocator, "techMD", rhsTechMD), lhsTechMD, rhsTechMD, ((this.techMD!= null)&&(!this.techMD.isEmpty())), ((that.techMD!= null)&&(!that.techMD.isEmpty())))) {
                return false;
            }
        }
        {
            List<MdSecType> lhsRightsMD;
            lhsRightsMD = (((this.rightsMD!= null)&&(!this.rightsMD.isEmpty()))?this.getRightsMD():null);
            List<MdSecType> rhsRightsMD;
            rhsRightsMD = (((that.rightsMD!= null)&&(!that.rightsMD.isEmpty()))?that.getRightsMD():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "rightsMD", lhsRightsMD), LocatorUtils.property(thatLocator, "rightsMD", rhsRightsMD), lhsRightsMD, rhsRightsMD, ((this.rightsMD!= null)&&(!this.rightsMD.isEmpty())), ((that.rightsMD!= null)&&(!that.rightsMD.isEmpty())))) {
                return false;
            }
        }
        {
            List<MdSecType> lhsSourceMD;
            lhsSourceMD = (((this.sourceMD!= null)&&(!this.sourceMD.isEmpty()))?this.getSourceMD():null);
            List<MdSecType> rhsSourceMD;
            rhsSourceMD = (((that.sourceMD!= null)&&(!that.sourceMD.isEmpty()))?that.getSourceMD():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "sourceMD", lhsSourceMD), LocatorUtils.property(thatLocator, "sourceMD", rhsSourceMD), lhsSourceMD, rhsSourceMD, ((this.sourceMD!= null)&&(!this.sourceMD.isEmpty())), ((that.sourceMD!= null)&&(!that.sourceMD.isEmpty())))) {
                return false;
            }
        }
        {
            List<MdSecType> lhsDigiprovMD;
            lhsDigiprovMD = (((this.digiprovMD!= null)&&(!this.digiprovMD.isEmpty()))?this.getDigiprovMD():null);
            List<MdSecType> rhsDigiprovMD;
            rhsDigiprovMD = (((that.digiprovMD!= null)&&(!that.digiprovMD.isEmpty()))?that.getDigiprovMD():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "digiprovMD", lhsDigiprovMD), LocatorUtils.property(thatLocator, "digiprovMD", rhsDigiprovMD), lhsDigiprovMD, rhsDigiprovMD, ((this.digiprovMD!= null)&&(!this.digiprovMD.isEmpty())), ((that.digiprovMD!= null)&&(!that.digiprovMD.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsID;
            lhsID = this.getID();
            String rhsID;
            rhsID = that.getID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "id", lhsID), LocatorUtils.property(thatLocator, "id", rhsID), lhsID, rhsID, (this.id!= null), (that.id!= null))) {
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
