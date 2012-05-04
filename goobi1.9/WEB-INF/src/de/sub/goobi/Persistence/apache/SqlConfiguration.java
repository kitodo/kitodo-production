package de.sub.goobi.Persistence.apache;


public class SqlConfiguration {

	// TODO aus Konfig holen
	
	private String dbDriverName ="com.mysql.jdbc.Driver";
	private String dbUser = "root";
	private String dbPassword = "goobi";
	private String dbURI = "jdbc:mysql://localhost/goobi?autoReconnect=true&amp;autoReconnectForPools=true";

	private int dbPoolMinSize = 1;
	private int dbPoolMaxSize = 20;



	public String getDbDriverName() {
		return this.dbDriverName;
	}

	public String getDbUser() {
		return this.dbUser;
	}

	public String getDbPassword() {
		return this.dbPassword;
	}

	public String getDbURI() {
		return this.dbURI;
	}

	public int getDbPoolMinSize() {
		return this.dbPoolMinSize;
	}

	public int getDbPoolMaxSize() {
		return this.dbPoolMaxSize;
	}


}
