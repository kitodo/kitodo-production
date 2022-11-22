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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.lang.Equals2;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy2;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * behaviorType: Complex Type for Behaviors
 * 			 A behavior can be used to associate executable behaviors with content in the METS object.  A behavior element has an interface definition element that represents an abstract definition  of the set  of behaviors represented by a particular behavior.  A behavior element also has an behavior  mechanism which is a module of executable code that implements and runs the behavior defined abstractly by the interface definition.
 * 			
 * 
 * <p>Java-Klasse für behaviorType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="behaviorType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="interfaceDef" type="{http://www.loc.gov/METS/}objectType" minOccurs="0"/&gt;
 *         &lt;element name="mechanism" type="{http://www.loc.gov/METS/}objectType"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *       &lt;attribute name="STRUCTID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *       &lt;attribute name="BTYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="CREATED" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="LABEL" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GROUPID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ADMID" type="{http://www.w3.org/2001/XMLSchema}IDREFS" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "behaviorType", propOrder = {
    "interfaceDef",
    "mechanism"
})
public class BehaviorType implements Equals2
{

    protected ObjectType interfaceDef;
    @XmlElement(required = true)
    protected ObjectType mechanism;
    @XmlAttribute(name = "ID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    protected String id;
    @XmlAttribute(name = "STRUCTID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> structid;
    @XmlAttribute(name = "BTYPE")
    protected String btype;
    @XmlAttribute(name = "CREATED")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar created;
    @XmlAttribute(name = "LABEL")
    protected String label;
    @XmlAttribute(name = "GROUPID")
    protected String groupid;
    @XmlAttribute(name = "ADMID")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> admid;

    /**
     * Ruft den Wert der interfaceDef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ObjectType }
     *     
     */
    public ObjectType getInterfaceDef() {
        return interfaceDef;
    }

    /**
     * Legt den Wert der interfaceDef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectType }
     *     
     */
    public void setInterfaceDef(ObjectType value) {
        this.interfaceDef = value;
    }

    /**
     * Ruft den Wert der mechanism-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ObjectType }
     *     
     */
    public ObjectType getMechanism() {
        return mechanism;
    }

    /**
     * Legt den Wert der mechanism-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectType }
     *     
     */
    public void setMechanism(ObjectType value) {
        this.mechanism = value;
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
     * Gets the value of the structid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the structid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSTRUCTID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getSTRUCTID() {
        if (structid == null) {
            structid = new ArrayList<Object>();
        }
        return this.structid;
    }

    /**
     * Ruft den Wert der btype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBTYPE() {
        return btype;
    }

    /**
     * Legt den Wert der btype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBTYPE(String value) {
        this.btype = value;
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
     * Ruft den Wert der groupid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGROUPID() {
        return groupid;
    }

    /**
     * Legt den Wert der groupid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGROUPID(String value) {
        this.groupid = value;
    }

    /**
     * Gets the value of the admid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the admid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getADMID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getADMID() {
        if (admid == null) {
            admid = new ArrayList<Object>();
        }
        return this.admid;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy2 strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final BehaviorType that = ((BehaviorType) object);
        {
            ObjectType lhsInterfaceDef;
            lhsInterfaceDef = this.getInterfaceDef();
            ObjectType rhsInterfaceDef;
            rhsInterfaceDef = that.getInterfaceDef();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "interfaceDef", lhsInterfaceDef), LocatorUtils.property(thatLocator, "interfaceDef", rhsInterfaceDef), lhsInterfaceDef, rhsInterfaceDef, (this.interfaceDef!= null), (that.interfaceDef!= null))) {
                return false;
            }
        }
        {
            ObjectType lhsMechanism;
            lhsMechanism = this.getMechanism();
            ObjectType rhsMechanism;
            rhsMechanism = that.getMechanism();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "mechanism", lhsMechanism), LocatorUtils.property(thatLocator, "mechanism", rhsMechanism), lhsMechanism, rhsMechanism, (this.mechanism!= null), (that.mechanism!= null))) {
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
            List<Object> lhsSTRUCTID;
            lhsSTRUCTID = (((this.structid!= null)&&(!this.structid.isEmpty()))?this.getSTRUCTID():null);
            List<Object> rhsSTRUCTID;
            rhsSTRUCTID = (((that.structid!= null)&&(!that.structid.isEmpty()))?that.getSTRUCTID():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "structid", lhsSTRUCTID), LocatorUtils.property(thatLocator, "structid", rhsSTRUCTID), lhsSTRUCTID, rhsSTRUCTID, ((this.structid!= null)&&(!this.structid.isEmpty())), ((that.structid!= null)&&(!that.structid.isEmpty())))) {
                return false;
            }
        }
        {
            String lhsBTYPE;
            lhsBTYPE = this.getBTYPE();
            String rhsBTYPE;
            rhsBTYPE = that.getBTYPE();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "btype", lhsBTYPE), LocatorUtils.property(thatLocator, "btype", rhsBTYPE), lhsBTYPE, rhsBTYPE, (this.btype!= null), (that.btype!= null))) {
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
        {
            String lhsGROUPID;
            lhsGROUPID = this.getGROUPID();
            String rhsGROUPID;
            rhsGROUPID = that.getGROUPID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "groupid", lhsGROUPID), LocatorUtils.property(thatLocator, "groupid", rhsGROUPID), lhsGROUPID, rhsGROUPID, (this.groupid!= null), (that.groupid!= null))) {
                return false;
            }
        }
        {
            List<Object> lhsADMID;
            lhsADMID = (((this.admid!= null)&&(!this.admid.isEmpty()))?this.getADMID():null);
            List<Object> rhsADMID;
            rhsADMID = (((that.admid!= null)&&(!that.admid.isEmpty()))?that.getADMID():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "admid", lhsADMID), LocatorUtils.property(thatLocator, "admid", rhsADMID), lhsADMID, rhsADMID, ((this.admid!= null)&&(!this.admid.isEmpty())), ((that.admid!= null)&&(!that.admid.isEmpty())))) {
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
