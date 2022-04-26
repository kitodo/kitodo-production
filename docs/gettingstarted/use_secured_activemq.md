# Securing access to ActiveMQ

## Using SSL connections

Inspired by https://activemq.apache.org/how-do-i-use-ssl

### Create key and trust store

You should create on the server and client side each a key and trust store. The key store should contain the public and private certificates of the client / server side and the trust store should contain the public certificate from the opposite side: on client side the public certificate of the server and on the server side the public certificate of the client(s).

#### Server side

Create the server key store:
```
keytool -genkeypair -alias broker -keyalg RSA -keysize 2048 -validity 365 -keystore <path-to-activemq-installation>/conf/broker.ks
```

Export the server certificate (this is needed for the client side trust store)
```
keytool -export -rfc -alias broker -keystore <path-to-activemq-installation>/conf/broker.ks -file server_certificate.pem
```

If you have already an OpenSSL based certificate then you can use this as base to create the keystore:
```
openssl pkcs12 -export -in <fullchain.pem> -inkey <privkey.pem> -out <pkcs.p12> -name <alias>
keytool -importkeystore -destkeystore <path-to-activemq-installation>/conf/broker.ks -srckeystore <pkcs.p12> -srcstoretype PKCS12 -alias broker
```

Import the client certificate into the server truststore
```
keytool -import -alias kitodo-production-client -keystore <path-to-activemq-installation>/conf/broker.ts -file <path-to>/kitodo-production-client.pem
```

#### Client side

Create the client key store:
```
keytool -genkeypair -alias <alias> -keyalg RSA -keysize 2048 -validity 365 -keystore /usr/local/kitodo/certs/activemq-client.ks
```

Export the client certificate (this is needed for the server side trust store)
```
keytool -export -rfc -alias kitodo-production-client -keystore /usr/local/kitodo/certs/activemq-client.ks -file /usr/local/kitodo/certs/kitodo-production-client.pem
```

If you have already an OpenSSL based certificate then you can use this as base to create the keystore:
```
openssl pkcs12 -export -in <fullchain.pem> -inkey <privkey.pem> -out <pkcs.p12> -name kitodo-production-client
keytool -importkeystore -destkeystore /usr/local/kitodo/certs/activemq-client.ks -srckeystore <pkcs.p12> -srcstoretype PKCS12 -alias kitodo-production-client
```

Import the server certificate into the client truststore
```
keytool -import -alias activemq-server -keystore /usr/local/kitodo/certs/activemq-client.ts -file <path-to>/server_certificate.pem
```

### Configure ActiveMQ

Adjust the `conf/activemq.xml` file

```xml
 <beans
  xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
  http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">
    <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">

        <!-- other options -->

        <transportConnectors>
            <!-- maybe other used transportConnectors -->
            <transportConnector name="ssl" uri="ssl://0.0.0.0:61617?needClientAuth=true&amp;transport.verifyHostName=true&amp;maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>
        </transportConnectors>

        <!-- SSL Configuration Context -->
        <sslContext>
            <sslContext keyStore="file:${activemq.base}/conf/broker.ks"
                keyStorePassword="UsedPasswordOnCreatingKeyStore"
                trustStore="file:${activemq.base}/conf/broker.ts"
                trustStorePassword="UsedPasswordOnCreatingTrustStore" />
        </sslContext>
    </broker>
</beans>
```
## Using authentification and authorization

See https://activemq.apache.org/security

### Define authentification

Adjust the `conf/activemq.xml` file

```xml
<beans
  xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
  http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">
    <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">
        <!-- other options -->
        <plugins>
            <!-- other plugins -->
            <simpleAuthenticationPlugin>
                <users>
                    <!-- login details for admin are take from conf/credentials.properties file or should be used from this file -->
                    <authenticationUser username="${activemq.username}" password="${activemq.password}" groups="senders,receivers,admins" />
                    <authenticationUser username="KitodoProductionUser" password="PasswordForUserKitodoProduction" groups="KitodoProductionGroup" />
                    <!-- some example users -->
                    <authenticationUser username="SomeUserForReadAccess" password="ReadAccessPasswort" groups="receivers" />
                    <authenticationUser username="SomeUserForWriteAccess" password="WriteAccessPasswort" groups="senders" />
                </users>
            </simpleAuthenticationPlugin>
        </plugins>
    </broker>
</beans>
```

### Define authorization

Adjust the `conf/activemq.xml` file

```xml
<beans
  xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
  http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">
    <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">
        <!-- other options -->
        <plugins>
            <!-- other plugins like simpleAuthentificationPlugin -->
            <authorizationPlugin>
                <map>
                    <authorizationMap>
                        <authorizationEntries>
                            <!-- global Advisory topic, should be given to any user -->
                            <authorizationEntry topic="ActiveMQ.Advisory.>" write="senders,KitodoProductionGroup" read="receivers,KitodoProductionGroup" admin="admins,senders,receivers,KitodoProductionGroup" />
                            <!-- Kitodo.Production used queues and topics -->
                            <authorizationEntry queue="KitodoProduction.FinalizeStep.Queue" write="senders" read="KitodoProductionGroup" admin="admins,KitodoProductionGroup" />
                            <authorizationEntry topic="KitodoProduction.ResultMessages.Topic" write="KitodoProductionGroup" read="receivers" admin="admins,KitodoProductionGroup" />
                            <!-- other used queues and topics -->
                        </authorizationEntries>
                    </authorizationMap>
                </map>
            </authorizationPlugin>
        </plugins>
    </broker>
</beans>
```
