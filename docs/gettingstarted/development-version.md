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
sudo apt-key adv --keyserver pgp.mit.edu --recv-keys 5072E1F5 && echo "deb http://repo.mysql.com/apt/debian/ stretch mysql-5.7" | sudo tee -a /etc/apt/sources.list.d/mysql-5.7.list
```

### Install packages openjdk-8, maven, mysql-community-server and zip

```
sudo debconf-set-selections <<< "mysql-community-server mysql-community-server/root-pass password "
sudo debconf-set-selections <<< "mysql-community-server mysql-community-server/re-root-pass password "
sudo apt update && sudo apt install -y openjdk-8-jdk maven mysql-community-server zip
```

### Build development version

```
wget https://github.com/kitodo/kitodo-production/archive/master.zip
unzip master.zip && rm master.zip
(cd kitodo-production-master/ && mvn clean package '-P!development')
mv kitodo-production-master/Kitodo/target/kitodo-3*.war kitodo-3.war
```

Note: If you want to build a release version, you may want to set the version in pom.xml files before packaging

### Remove duplicate version of bcprov dependency

```
zip -d kitodo-3.war "WEB-INF/lib/bcprov-jdk14-138.jar"
```

### Create MySQL database and user

```
sudo mysql -e 'create database kitodo;grant all privileges on kitodo.* to kitodo@localhost identified by "kitodo";flush privileges;'
```

### Generate SQL dump (flyway migration)

```
wget -O - https://raw.githubusercontent.com/kitodo/kitodo-production/master/Kitodo/setup/schema.sql https://raw.githubusercontent.com/kitodo/kitodo-production/master/Kitodo/setup/default.sql | mysql -u kitodo -D kitodo --password=kitodo
(cd kitodo-production-master/Kitodo-DataManagement && mvn flyway:baseline -Pflyway && mvn flyway:migrate -Pflyway)
mysqldump -u kitodo --password=kitodo kitodo > kitodo-3.sql
```

### Create zip archive with directories and config files

```
mkdir zip zip/config zip/debug zip/import zip/logs zip/messages zip/metadata zip/plugins zip/plugins/command zip/plugins/import zip/plugins/opac zip/plugins/step zip/plugins/validation zip/rulesets zip/scripts zip/swap zip/temp zip/users zip/xslt zip/diagrams
install -m 444 kitodo-production-master/Kitodo/src/main/resources/kitodo_*.xml zip/config/
install -m 444 kitodo-production-master/Kitodo/src/main/resources/modules.xml zip/config/
install -m 444 kitodo-production-master/Kitodo/src/main/resources/docket*.xsl zip/xslt/
install -m 444 kitodo-production-master/Kitodo/rulesets/*.xml zip/rulesets/
install -m 554 kitodo-production-master/Kitodo/scripts/*.sh zip/scripts/
chmod -w zip/config zip/import zip/messages zip/plugins zip/plugins/command zip/plugins/import zip/plugins/opac zip/plugins/step zip/plugins/validation zip/rulesets zip/scripts zip/xslt
(cd zip && zip -r ../kitodo-3-config.zip *)
```

### Results

* war file: `kitodo-3.war`
* sql dump: `kitodo-3.sql`
* zip file: `kitodo-3-config.zip`

### Cleanup MySQL

```
sudo mysql
DROP DATABASE kitodo;
exit;
```

### Uninstall Maven

```
sudo apt remove -y maven && sudo apt autoremove -y
```

## 3. Deployment

### Add Elasticsearch 5.x repository

```
sudo apt install -y apt-transport-https
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
sudo mysql -e "create database kitodo;grant all privileges on kitodo.* to kitodo@localhost identified by 'kitodo';flush privileges;"
cat kitodo-3.sql | mysql -u kitodo -D kitodo --password=kitodo
```

### Configure Elasticsearch

```
sudo sed -i 's/#path.data: \/path\/to\/data/path.data: \/var\/lib\/elasticsearch/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#path.logs: \/path\/to\/logs/path.logs: \/var\/log\/elasticsearch/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#cluster.name: my-application/cluster.name: kitodo/' /etc/elasticsearch/elasticsearch.yml
sudo sed -i 's/#node.name: node-1/node.name: kitodo-1/' /etc/elasticsearch/elasticsearch.yml
sudo /bin/systemctl daemon-reload
sudo /bin/systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
until curl -s -X GET "localhost:9200/kitodo" | grep -q -o "kitodo" ; do sleep 1; done
curl -X PUT "localhost:9200/kitodo"
```

### Create directories and set permissions

```
sudo mkdir /usr/local/kitodo
sudo unzip kitodo-3-config.zip -d /usr/local/kitodo
sudo chown -R tomcat8:tomcat8 /usr/local/kitodo
```

### Deploy war file into Tomcat

```
sudo chown tomcat8:tomcat8 kitodo-3.war
sudo mv kitodo-3.war /var/lib/tomcat8/webapps/kitodo.war
until curl -s GET "localhost:8080/kitodo/pages/login.jsf" | grep -q -o "KITODO.PRODUCTION" ; do sleep 1; done
```

### Login

http://localhost:8080/kitodo/

* user: testAdmin
* pass: test

## 4. Configuration

see <https://github.com/kitodo/kitodo-production/wiki/Installationsanleitung-f%C3%BCr-Kitodo.Production-3.x>
