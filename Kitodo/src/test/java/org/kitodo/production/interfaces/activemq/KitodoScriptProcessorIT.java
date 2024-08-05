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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.command.KitodoScriptService;

public class KitodoScriptProcessorIT {
    @BeforeEach
    public void prepare() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesForWorkflowFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterEach
    public void clean() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        SecurityTestUtils.cleanSecurityContext();
    }

    @Test
    public void shouldExecuteKitodoScript() throws Exception {

        // define test data
        MapMessageObjectReader testData = new MapMessageObjectReader(new MapMessage() {
            @Override
            public String getJMSMessageID() throws JMSException {
                return null;
            }

            @Override
            public void setJMSMessageID(String id) throws JMSException {

            }

            @Override
            public long getJMSTimestamp() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long timestamp) throws JMSException {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                return null;
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {

            }

            @Override
            public void setJMSCorrelationID(String correlationID) throws JMSException {

            }

            @Override
            public String getJMSCorrelationID() throws JMSException {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() throws JMSException {
                return null;
            }

            @Override
            public void setJMSReplyTo(Destination replyTo) throws JMSException {

            }

            @Override
            public Destination getJMSDestination() throws JMSException {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) throws JMSException {

            }

            @Override
            public int getJMSDeliveryMode() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int deliveryMode) throws JMSException {

            }

            @Override
            public boolean getJMSRedelivered() throws JMSException {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean redelivered) throws JMSException {

            }

            @Override
            public String getJMSType() throws JMSException {
                return null;
            }

            @Override
            public void setJMSType(String type) throws JMSException {

            }

            @Override
            public long getJMSExpiration() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSExpiration(long expiration) throws JMSException {

            }

            @Override
            public int getJMSPriority() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSPriority(int priority) throws JMSException {

            }

            @Override
            public void clearProperties() throws JMSException {

            }

            @Override
            public boolean propertyExists(String name) throws JMSException {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String name) throws JMSException {
                return false;
            }

            @Override
            public byte getByteProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public short getShortProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public int getIntProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public long getLongProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public float getFloatProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public double getDoubleProperty(String name) throws JMSException {
                return 0;
            }

            @Override
            public String getStringProperty(String name) throws JMSException {
                return null;
            }

            @Override
            public Object getObjectProperty(String name) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getPropertyNames() throws JMSException {
                return null;
            }

            @Override
            public void setBooleanProperty(String name, boolean value) throws JMSException {

            }

            @Override
            public void setByteProperty(String name, byte value) throws JMSException {

            }

            @Override
            public void setShortProperty(String name, short value) throws JMSException {

            }

            @Override
            public void setIntProperty(String name, int value) throws JMSException {

            }

            @Override
            public void setLongProperty(String name, long value) throws JMSException {

            }

            @Override
            public void setFloatProperty(String name, float value) throws JMSException {

            }

            @Override
            public void setDoubleProperty(String name, double value) throws JMSException {

            }

            @Override
            public void setStringProperty(String name, String value) throws JMSException {

            }

            @Override
            public void setObjectProperty(String name, Object value) throws JMSException {

            }

            @Override
            public void acknowledge() throws JMSException {

            }

            @Override
            public void clearBody() throws JMSException {

            }

            @Override
            public boolean getBoolean(String name) throws JMSException {
                return false;
            }

            @Override
            public byte getByte(String name) throws JMSException {
                return 0;
            }

            @Override
            public short getShort(String name) throws JMSException {
                return 0;
            }

            @Override
            public char getChar(String name) throws JMSException {
                return 0;
            }

            @Override
            public int getInt(String name) throws JMSException {
                return 0;
            }

            @Override
            public long getLong(String name) throws JMSException {
                return 0;
            }

            @Override
            public float getFloat(String name) throws JMSException {
                return 0;
            }

            @Override
            public double getDouble(String name) throws JMSException {
                return 0;
            }

            @Override
            public String getString(String name) throws JMSException {
                return null;
            }

            @Override
            public byte[] getBytes(String name) throws JMSException {
                return null;
            }

            @Override
            public Object getObject(String name) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getMapNames() throws JMSException {
                return null;
            }

            @Override
            public void setBoolean(String name, boolean value) throws JMSException {

            }

            @Override
            public void setByte(String name, byte value) throws JMSException {

            }

            @Override
            public void setShort(String name, short value) throws JMSException {

            }

            @Override
            public void setChar(String name, char value) throws JMSException {

            }

            @Override
            public void setInt(String name, int value) throws JMSException {

            }

            @Override
            public void setLong(String name, long value) throws JMSException {

            }

            @Override
            public void setFloat(String name, float value) throws JMSException {

            }

            @Override
            public void setDouble(String name, double value) throws JMSException {

            }

            @Override
            public void setString(String name, String value) throws JMSException {

            }

            @Override
            public void setBytes(String name, byte[] value) throws JMSException {

            }

            @Override
            public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {

            }

            @Override
            public void setObject(String name, Object value) throws JMSException {

            }

            @Override
            public boolean itemExists(String name) throws JMSException {
                return false;
            }
        }) {
            @Override
            public String getMandatoryString(String s) {
                return "action:test";
            }

            @Override
            public Collection<Integer> getCollectionOfInteger(String s) {
                return Collections.singletonList(1);
            }
        };

        // the object to be tested
        KitodoScriptProcessor underTest = new KitodoScriptProcessor();

        // organize return of results
        List<String> scriptResult = new ArrayList<>();
        List<Process> processesResult = new ArrayList<>();
        Field serviceField = KitodoScriptProcessor.class.getDeclaredField("kitodoScriptService");
        serviceField.setAccessible(true);
        serviceField.set(underTest, new KitodoScriptService() {
            @Override
            public void execute(List<Process> processes, String script) {
                scriptResult.add(script);
                processesResult.addAll(processes);
            }
        });

        // carry out test
        underTest.process(testData);

        // check results
        assertEquals(scriptResult.get(0), "action:test", "should have passed the script to be executed");
        assertEquals(processesResult.size(), 1, "should have passed one process");
        assertEquals(processesResult.get(0).getId(), 1, "should have passed process 1");
    }
}
