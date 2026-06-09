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

package org.kitodo.production.interfaces.activemq;

import java.util.*;

import javax.jms.*;

public class FakeMapMessage implements MapMessage {

    private static final Map<String, Object> data = new HashMap<>();

    @Override
    public String getJMSMessageID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        throw new UnsupportedOperationException();

    }

    @Override
    public String getJMSType() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getJMSPriority() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearProperties() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acknowledge() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearBody() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getBody(Class<T> c) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public char getChar(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String name) throws JMSException {
        return (Integer) data.get(name);
    }

    @Override
    public long getLong(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String name) throws JMSException {
        return (String) data.get(name);
    }

    @Override
    public byte[] getBytes(String name) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(String name) throws JMSException {
        return data.get(name);
    }

    @Override
    public Enumeration getMapNames() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(String name, boolean value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByte(String name, byte value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShort(String name, short value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setChar(String name, char value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(String name, int value) throws JMSException {
        data.put(name, value);
    }

    @Override
    public void setLong(String name, long value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloat(String name, float value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(String name, double value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setString(String name, String value) throws JMSException {
        data.put(name, value);
    }

    @Override
    public void setBytes(String name, byte[] value) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(String name, Object value) throws JMSException {
        data.put(name, value);
    }

    @Override
    public boolean itemExists(String name) throws JMSException {
        return data.containsKey(name);
    }
}
