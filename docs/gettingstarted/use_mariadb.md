# Use MariaDB instead of MySQL

* Kitodo.Production application must be built with a connector for MariaDB
* Hibernate and Flyway configuration must be adjusted to use MariaDB

## Hibernate configuration

* modifications must be done in file `hibernate.cfg.xml`

### hibernate.dialect

* for correct dialect look at https://stackoverflow.com/a/51734560 or Hibernate JavaDoc.
* f.e. use `org.hibernate.dialect.MariaDB10Dialect` if your MariaDB server is in version 10.1.x

### hibernate.connection.driver_class

* instead of `com.mysql.jdbc.Driver` use `org.mariadb.jdbc.Driver`
* maybe this is not needed anymore as in normal cases connection driver class is detected correct by Hibernate

### hibernate.connection.url

* instead of `jdbc:mysql://...` use `jdbc:mariadb://...`

## Flyway configuration

* only needed if you want to migrate your database with help of Flyway
* modification must be done in file `flyway.properties`

### flyway.url

* instead of `jdbc:mysql://...` use `jdbc:mariadb://...`
