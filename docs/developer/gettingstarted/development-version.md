# Build development version of Kitodo 3.x

## 1. System environment

Download and install [Debian 9.4](https://cdimage.debian.org/debian-cd/9.4.0/amd64/iso-cd/).

### Install sudo and reboot

```
su -c "apt install -y sudo && adduser $USER sudo && echo \"Defaults timestamp_timeout=300\" >> /etc/sudoers.d/timeout && reboot"
```

## 2. Build files for deployment

### Add mysql.com 5.7 repository

```
sudo apt install -y dirmngr
```

```
sudo apt-key adv --keyserver pgp.mit.edu --recv-keys 5072E1F5 && echo "deb http://repo.mysql.com/apt/debian/ stretch mysql-5.7" | sudo tee -a /etc/apt/sources.list.d/mysql-5.7.list
```

### Install openjdk-11

```
echo 'deb http://ftp.debian.org/debian stretch-backports main' | sudo tee /etc/apt/sources.list.d/stretch-backports.list
sudo apt update && sudo apt install -y openjdk-11-jdk
```

### Install packages maven, mysql-community-server and zip

```
sudo debconf-set-selections <<< "mysql-community-server mysql-community-server/root-pass password "
sudo debconf-set-selections <<< "mysql-community-server mysql-community-server/re-root-pass password "
sudo apt update && sudo apt install -y maven mysql-community-server zip
```
### Change java security config (for cloud environments)

```
sudo sed -i 's/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/' /etc/java-11-openjdk/security/java.security
```

### Build development version and modules

```
wget https://github.com/kitodo/kitodo-production/archive/master.zip
unzip master.zip && rm master.zip
(cd kitodo-production-master/ && mvn clean package '-P!development')
zip -j kitodo-3-modules.zip kitodo-production-master/Kitodo/modules/*.jar
mv kitodo-production-master/Kitodo/target/kitodo-3*.war kitodo-3.war
```

Note: If you want to build a release version, you may want to set the version in pom.xml files before packaging

### Create MySQL database and user

```
sudo mysql -e "create database kitodo;grant all privileges on kitodo.* to kitodo@localhost identified by 'kitodo';flush privileges;"
```

### Generate SQL dump (flyway migration)

```
cat kitodo-production-master/Kitodo/setup/schema.sql | mysql -u kitodo -D kitodo --password=kitodo
cat kitodo-production-master/Kitodo/setup/default.sql | mysql -u kitodo -D kitodo --password=kitodo
(cd kitodo-production-master/Kitodo-DataManagement && mvn flyway:baseline -Pflyway && mvn flyway:migrate -Pflyway)
mysqldump -u kitodo --password=kitodo kitodo > kitodo-3.sql
```

### Create zip archive with directories and config files

```
mkdir zip zip/config zip/debug zip/import zip/logs zip/messages zip/metadata zip/plugins zip/plugins/command zip/plugins/import zip/plugins/opac zip/plugins/step zip/plugins/validation zip/rulesets zip/scripts zip/swap zip/temp zip/users zip/xslt zip/diagrams
install -m 444 kitodo-production-master/Kitodo/src/main/resources/kitodo_*.xml zip/config/
install -m 444 kitodo-production-master/Kitodo/src/main/resources/docket*.xsl zip/xslt/
install -m 444 kitodo-production-master/Kitodo/rulesets/*.xml zip/rulesets/
install -m 444 kitodo-production-master/Kitodo/diagrams/*.xml zip/diagrams/
install -m 554 kitodo-production-master/Kitodo/scripts/*.sh zip/scripts/
chmod -w zip/config zip/import zip/messages zip/plugins zip/plugins/command zip/plugins/import zip/plugins/opac zip/plugins/step zip/plugins/validation zip/rulesets zip/scripts zip/xslt
(cd zip && zip -r ../kitodo-3-config.zip *)
```

Note: Create `messages` directory only in the case when you want to add / edit your own translations. Only those translation files will be used, not the one from class path.

### Results

* war file: `kitodo-3.war`
* modules: `kitodo-3-modules.zip`
* sql dump: `kitodo-3.sql`
* zip file: `kitodo-3-config.zip`

## 3. Deployment

### Add Elasticsearch 5.x repository

```
sudo apt install -y apt-transport-https
```

```
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add - && echo "deb https://artifacts.elastic.co/packages/5.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-5.x.list
```

### Install packages tomcat8, elasticsearch and curl

```
sudo apt update && sudo apt install -y tomcat8 elasticsearch curl
```

### Configure Tomcat

```
sudo sed -i 's/JAVA_OPTS="-Djava.awt.headless=true/JAVA_OPTS="-Djava.awt.headless=true -Xmx1920m/' /etc/default/tomcat8
```

### Configure MySQL

```
sudo sh -c "echo '[mysqld] innodb_file_per_table' >> /etc/mysql/my.cnf"
sudo service mysql restart
```

### Configure ElasticSearch

```
sudo sed -i 's/#path.data: \/path\/to\/data/path.data: \/var\/lib\/elasticsearch/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#path.logs: \/path\/to\/logs/path.logs: \/var\/log\/elasticsearch/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#cluster.name: my-application/cluster.name: kitodo/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#node.name: node-1/node.name: kitodo-1/' /etc/elasticsearch/elasticsearch.yml
sudo /bin/systemctl daemon-reload
sudo /bin/systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
```

Other ElasticSearch settings can be adjusted in _kitodo_config.properties_ file:

```
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.protocol=http
elasticsearch.index=kitodo
elasticsearch.batch=1000
elasticsearch.useAuthentication=true
elasticsearch.user=kitodo
elasticsearch.password=kitodo
```

### Create directories and set permissions

```
sudo mkdir /usr/local/kitodo
sudo unzip kitodo-3-config.zip -d /usr/local/kitodo
sudo chown -R tomcat8:tomcat8 /usr/local/kitodo
```

### Install modules

```
sudo mkdir /usr/local/kitodo/modules
sudo unzip kitodo-3-modules.zip -d /usr/local/kitodo/modules
sudo chown -R tomcat8:tomcat8 /usr/local/kitodo/modules
```

### Deploy war file into Tomcat

```
sudo chown tomcat8:tomcat8 kitodo-3.war
sudo mv kitodo-3.war /var/lib/tomcat8/webapps/kitodo.war
until curl -s GET "localhost:8080/kitodo/pages/login.jsf" | grep -q -o "KITODO.PRODUCTION" ; do sleep 1; done
```

### Login

<http://localhost:8080/kitodo/>

* user: testAdmin
* pass: test

### Index example data

Menu System: <http://localhost:8080/kitodo/pages/system.jsf>

* Delete ElasticSearch index
* Create ElasticSearch mapping
* Whole Index / Start indexing

## 4. Updates

### Download sources

```
rm -rf kitodo-production-master
wget https://github.com/kitodo/kitodo-production/archive/master.zip
unzip master.zip && rm master.zip
```

### Reset database

```
sudo mysql -e "drop database kitodo;"
sudo mysql -e "create database kitodo;grant all privileges on kitodo.* to kitodo@localhost identified by 'kitodo';flush privileges;"
cat kitodo-production-master/Kitodo/setup/schema.sql | mysql -u kitodo -D kitodo --password=kitodo
cat kitodo-production-master/Kitodo/setup/default.sql | mysql -u kitodo -D kitodo --password=kitodo
(cd kitodo-production-master/Kitodo-DataManagement && mvn flyway:baseline -Pflyway && mvn flyway:migrate -Pflyway)
```

### Rebuild and deploy war file

```
(cd kitodo-production-master/ && mvn clean package '-P!development')
sudo rm -f /usr/local/kitodo/modules/*
sudo cp kitodo-production-master/Kitodo/modules/*.jar /usr/local/kitodo/modules
sudo chown -R tomcat8:tomcat8 /usr/local/kitodo/modules
mv kitodo-production-master/Kitodo/target/kitodo-3*.war kitodo-3.war
sudo chown tomcat8:tomcat8 kitodo-3.war
sudo mv kitodo-3.war /var/lib/tomcat8/webapps/kitodo.war
sleep 5
until curl -s GET "localhost:8080/kitodo/pages/login.jsf" | grep -q -o "KITODO.PRODUCTION" ; do sleep 1; done
```

Note: If the update provides new example data, it has to be copied from kitodo-production-master/Kitodo/... to /usr/local/kitodo/... manually.

### Reset index

http://localhost:8080/kitodo/pages/system.jsf

* Delete ElasticSearch index
* Create ElasticSearch mapping
* Whole Index / Start indexing
