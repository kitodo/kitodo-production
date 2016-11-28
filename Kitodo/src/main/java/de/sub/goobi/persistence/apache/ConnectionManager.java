package de.sub.goobi.persistence.apache;

//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
//CHECKSTYLE:ON

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * @author Robert Sehr
 *
 */
public class ConnectionManager {

	private static final Logger logger = Logger.getLogger(ConnectionManager.class);

	private DataSource ds = null;
	private static GenericObjectPool _pool = null;

	/**
	 * @param config
	 *            configuration from an XML file.
	 */
	public ConnectionManager(SqlConfiguration config) {
		try {
			connectToDB(config);
		} catch (Exception e) {
			logger.error("Failed to construct ConnectionManager", e);
		}
	}

	/**
	 * destructor
	 */
	@Override
	protected void finalize() {
		logger.debug("Finalizing ConnectionManager");
		try {
			super.finalize();
		} catch (Throwable ex) {
			logger.error("ConnectionManager finalize failed to disconnect from mysql: ", ex);
		}
	}

	/**
	 * connectToDB - Connect to the MySql DB!
	 */
	private void connectToDB(SqlConfiguration config) {

		try {
			java.lang.Class.forName(config.getDbDriverName()).newInstance();
		} catch (Exception e) {
			logger.error(
					"Error when attempting to obtain DB Driver: " + config.getDbDriverName() + " on "
							+ new Date().toString(), e);
		}

		logger.debug("Trying to connect to database...");
		try {
			this.ds = setupDataSource(config.getDbURI(), config.getDbUser(), config.getDbPassword(),
					config.getDbPoolMinSize(), config.getDbPoolMaxSize());
			logger.debug("Connection attempt to database succeeded.");
		} catch (Exception e) {
			logger.error("Error when attempting to connect to DB ", e);
		}
	}

	/**
	 * @param connectURI - JDBC Connection URI
	 * @param username - JDBC Connection username
	 * @param password - JDBC Connection password
	 * @param minIdle - Minimum number of idel connection in the connection pool
	 * @param maxActive - Connection Pool Maximum Capacity (Size)
	 * @throws Exception add description
	 */
	public static DataSource setupDataSource(String connectURI, String username, String password, int minIdle,
			int maxActive) {
		//
		// First, we'll need a ObjectPool that serves as the actual pool of connections.
		//
		// We'll use a GenericObjectPool instance, although any ObjectPool implementation will suffice.
		//
		GenericObjectPool connectionPool = new GenericObjectPool(null);

		connectionPool.setMinIdle(minIdle);
		connectionPool.setMaxActive(maxActive);

		ConnectionManager._pool = connectionPool;
		// we keep it for two reasons
		// #1 We need it for statistics/debugging
		// #2 PoolingDataSource does not have getPool()
		// method, for some obscure, weird reason.

		//
		// Next, we'll create a ConnectionFactory that the
		// pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory,
		// using the connect string from configuration
		//
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, username, password);

		//
		// Now we'll create the PoolableConnectionFactory, which wraps
		// the "real" Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		//
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);

		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

		return dataSource;
	}

	/**
	 * @throws Exception add description
	 */
	public static void printDriverStats() throws Exception {
		ObjectPool connectionPool = ConnectionManager._pool;
		if (logger.isDebugEnabled()) {
			logger.debug("NumActive: " + connectionPool.getNumActive());
			logger.debug("NumIdle: " + connectionPool.getNumIdle());
		}
	}

	/**
	 * getNumLockedProcesses - gets the number of currently locked processes on the MySQL db
	 *
	 * @return Number of locked processes
	 */
	public int getNumLockedProcesses() {
		int num_locked_connections = 0;
		try (Connection con = this.ds.getConnection();
				PreparedStatement p_stmt = con.prepareStatement("SHOW PROCESSLIST");
				ResultSet rs = p_stmt.executeQuery()) {
			while (rs.next()) {
				if (rs.getString("State") != null && rs.getString("State").equals("Locked")) {
					num_locked_connections++;
				}
			}
		} catch (java.sql.SQLException ex) {
			logger.error(ex.toString());
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to get Locked Connections - Exception: " + e.toString());
			}
		}
		return num_locked_connections;
	}

	public DataSource getDataSource() {
		return this.ds;
	}

}
