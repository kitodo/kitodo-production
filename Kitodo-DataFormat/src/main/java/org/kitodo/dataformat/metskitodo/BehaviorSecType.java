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
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * behaviorSecType: Complex Type for Behavior Sections
 * 			Behaviors are executable code which can be associated with parts of a METS object.  The behaviorSec element is used to group individual behaviors within a hierarchical structure.  Such grouping can be useful to organize families of behaviors together or to indicate other relationships between particular behaviors.
 * 			
 * 
 * <p>Java-Klasse für behaviorSecType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="behaviorSecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="behaviorSec" type="{http://www.loc.gov/METS/}behaviorSecType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="behavior" type="{http://www.loc.gov/METS/}behaviorType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="CREATED" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "behaviorSecType", propOrder = {
    "behaviorSec",
    "behavior"
})
public class BehaviorSecType implements Equals2
{

    protected List<BehaviorSecType> behaviorSec;
    protected List<BehaviorType> behavior;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "LABEL")
    protected String label;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the behaviorSec property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behaviorSec property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehaviorSec().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BehaviorSecType }
     * 
     * 
     */
    public List<BehaviorSecType> getBehaviorSec() {
        if (behaviorSec == null) {
            behaviorSec = new ArrayList<BehaviorSecType>();
        }
        return this.behaviorSec;
    }

    /**
     * Gets the value of the behavior property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the behavior property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBehavior().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BehaviorType }
     * 
     * 
     */
    public List<BehaviorType> getBehavior() {
        if (behavior == null) {
            behavior = new ArrayList<BehaviorType>();
        }
        return this.behavior;
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
     * Ruft den Wert der created-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCREATED() {
        return created;
    }

    /**
     * Legt den Wert der created-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCREATED(XMLGregorianCalendar value) {
        this.created = value;
    }

    /**
     * Ruft den Wert der label-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLABEL() {
        return label;
    }

    /**
     * Legt den Wert der label-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLABEL(String value) {
        this.label = value;
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
        final BehaviorSecType that = ((BehaviorSecType) object);
        {
            List<BehaviorSecType> lhsBehaviorSec;
            lhsBehaviorSec = (((this.behaviorSec!= null)&&(!this.behaviorSec.isEmpty()))?this.getBehaviorSec():null);
            List<BehaviorSecType> rhsBehaviorSec;
            rhsBehaviorSec = (((that.behaviorSec!= null)&&(!that.behaviorSec.isEmpty()))?that.getBehaviorSec():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "behaviorSec", lhsBehaviorSec), LocatorUtils.property(thatLocator, "behaviorSec", rhsBehaviorSec), lhsBehaviorSec, rhsBehaviorSec, ((this.behaviorSec!= null)&&(!this.behaviorSec.isEmpty())), ((that.behaviorSec!= null)&&(!that.behaviorSec.isEmpty())))) {
                return false;
            }
        }
        {
            List<BehaviorType> lhsBehavior;
            lhsBehavior = (((this.behavior!= null)&&(!this.behavior.isEmpty()))?this.getBehavior():null);
            List<BehaviorType> rhsBehavior;
            rhsBehavior = (((that.behavior!= null)&&(!that.behavior.isEmpty()))?that.getBehavior():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "behavior", lhsBehavior), LocatorUtils.property(thatLocator, "behavior", rhsBehavior), lhsBehavior, rhsBehavior, ((this.behavior!= null)&&(!this.behavior.isEmpty())), ((that.behavior!= null)&&(!that.behavior.isEmpty())))) {
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
        {
            XMLGregorianCalendar lhsCREATED;
            lhsCREATED = this.getCREATED();
            XMLGregorianCalendar rhsCREATED;
            rhsCREATED = that.getCREATED();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "created", lhsCREATED), LocatorUtils.property(thatLocator, "created", rhsCREATED), lhsCREATED, rhsCREATED, (this.created!= null), (that.created!= null))) {
                return false;
            }
        }
        {
            String lhsLABEL;
            lhsLABEL = this.getLABEL();
            String rhsLABEL;
            rhsLABEL = that.getLABEL();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "label", lhsLABEL), LocatorUtils.property(thatLocator, "label", rhsLABEL), lhsLABEL, rhsLABEL, (this.label!= null), (that.label!= null))) {
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
