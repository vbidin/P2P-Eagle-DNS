package pgrid.core;

import pgrid.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Title: DBManager</p>
 * <p/>
 * <p>Description: This class manages the DataBase connection and
 * transactions</p>
 *
 * @author Mark Kornfilt
 */
public class DBManager {

	/**
	 * The name of the DataBase
	 */
	private static final String DBName = Constants.DATA_DIR + "PGridDB";

	/**
	 * The URL of the dataBase when using the "in-process" mode of HSQLDB
	 */
	private static final String hsqldbStandaloneURL = "jdbc:hsqldb:file:" + DBName;

	/**
	 * The URL of the dataBase when using the "server" mode of HSQLDB
	 */
	private static final String hsqldbServerURL = "jdbc:hsqldb:hsql://localhost/" + DBName;

	/**
	 * the HSQLDB JDBC driver
	 */
	private static final String hsqldbDriver = "org.hsqldb.jdbcDriver";

	/**
	 * the shutdown flag for the DBDataTable
	 */
	private boolean dbDataTableShutdownFlag = false;

	/**
	 * the shutdown flaf for the DBRoutingTable
	 */
	private boolean dbRoutingTableShutdownFlag = false;

	/**
	 * The DataBase tables
	 *
	 * @todo remove? See DBDataTable
	 */
	public static final String DATA_ITEMS_TABLE = "DATA_ITEMS";

	public static final String DATA_TABLES_TABLE = "DATA_TABLES";

	public static final String DATA_TABLE_ITEMS_TABLE = "DATA_TABLE_ITEMS";

	public static final String HOSTS_TABLE = "HOSTS";

	public static final String TYPES_TABLE = "TYPES";

	public static final String FIDGETS_TABLE = "FIDGET_LIST";

	public static final String CONFIG_TABLE = "CONFIG";

	/**
	 * The reference to the only instance of this class (Singleton
	 * pattern). This differs from the C++ standard implementation by Gamma
	 * et.al. since Java ensures the order of static initialization at runtime.
	 *
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */

	private static final DBManager SHARED_INSTANCE = new DBManager(hsqldbDriver, hsqldbStandaloneURL, "sa", "");

	private String driverClassName = null;
	private String userName = "sa";
	private String password = "";
	private String jdbcUrl = null;
	private Connection con;

	protected DBManager(String driver, String Url, String uName, String pass) {
		driverClassName = driver;
		userName = uName;
		password = pass;
		jdbcUrl = Url;
	}

	/**
	 * This creates the only instance of this class. This differs from the C++ standard implementation by Gamma et.al.
	 * since Java ensures the order of static initialization at runtime.
	 *
	 * @return the shared instance of this class.
	 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip67.html">
	 *      Lazy instantiation - Balancing performance and resource usage</a>
	 */
	public static DBManager sharedInstance() {
		return SHARED_INSTANCE;
	}

	/**
	 * Compacts the DB and commits.
	 */
	public void compactDB() {
		execDeleteSQL("delete from " + DATA_ITEMS_TABLE + " where DATA_ITEM_ID not in " +
						  "(select DATA_ITEM_ID from " + DATA_TABLE_ITEMS_TABLE + ")");
		// TODO delete unused hosts and types
		execSQL("CHECKPOINT DEFRAG");
		try {
			con.commit();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Perform the initialization of the data base manager.
	 */
	public void init() {
		initConnect();

		if (!checkTables()) {
			createTables("P-Grid.ddl");
		}
	}

	/**
	 * Initializes the connection with the DataBase
	 */
	private void initConnect() {
		try {
			Class.forName(driverClassName).newInstance();
			con = DriverManager.getConnection(jdbcUrl, userName, password);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("problem initilizing connection: " + ex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the auto-commit parameter of the driver
	 *
	 * @param autoCommit the auto-commit flag
	 */
	public void setAutoCommit(boolean autoCommit) {
		try {
			con.setAutoCommit(autoCommit);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the configuration value corresponding to the given key
	 *
	 * @param key The configuration key
	 * @return the configuration value
	 */
	public String getConfig(String key) {
		try {
			ResultSet rs = execResultSetSQL("SELECT VALUE FROM " + CONFIG_TABLE +
					" WHERE KEY = '" + key + "'");
			if (rs.next())
				return rs.getString("VALUE");
			else
				return null;
		} catch (SQLException ex) {
			return null;
		}
	}

	/**
	 * Save the configuration
	 * @param key
	 * @param value
	 */
	public void setConfig(String key, String value) {
		try {
			Statement st = con.createStatement();
			ResultSet rs = execResultSetSQL("SELECT KEY FROM " + CONFIG_TABLE + " WHERE KEY='" + key + "'");
			if (rs.next()) {
				st.executeUpdate("UPDATE " + CONFIG_TABLE + " SET VALUE='" + value +
						"' WHERE KEY='" + key + "'");
			} else {
				st.executeUpdate("INSERT INTO " + CONFIG_TABLE + " VALUES('" + key + "','" + value + "')");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * Counts the number of items returned by this SQL query
	 *
	 * @param tableQuery The SQL query
	 * @return The number of items
	 */
	public int count(String tableQuery) {
		String query = "select count(*) as NUM_ITEMS from " + tableQuery;
		ResultSet rs = null;
		try {
			rs = execResultSetSQL(query);
			rs.next();
			return (rs.getInt("NUM_ITEMS"));
		} catch (SQLException ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	/**
	 * Drops all the tables in the DB
	 */
	public void dropAll() {
		try {
			String[] type = {"TABLE"};
			ResultSet rs = con.getMetaData().getTables(null, null, null, type);
			while (rs.next()) {
				execSQL("DROP TABLE " + rs.getString("TABLE_NAME"));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Executes this SQL query
	 *
	 * @param query The SQL query
	 */
	public void execSQL(String query) {
		Statement st;
		try {
			st = con.createStatement();
			st.execute(query);
			st.close();
		} catch (SQLException ex) {
			if (ex.getErrorCode() == -104) {  //if UNIQUE constraint violation, do nothing
				ex.printStackTrace();
				return;
			}
			System.err.println("problem " + ex + "\nexecuting query: " + query + " \n error code: " + ex.getErrorCode());
			ex.printStackTrace();
		}
	}

	/**
	 * Executes this DELETE SQL statement and throws the exception if deletion failed
	 *
	 * @param query The DELETE SQL statement
	 */
	public int execDeleteSQL(String query) {
		Statement st;
		try {
			st = con.createStatement();
			return st.executeUpdate(query);
		} catch (SQLException ex) {
			System.err.println("problem " + ex + "\nexecuting query: " + query + " \n error code: " + ex.getErrorCode());
			ex.printStackTrace();
			return -1;
		}
	}

	/**
	 * Executes this INSERT SQL statement and returns the auto-generated identity
	 * value for the inserted row
	 *
	 * @param table  The table in which we want to INSERT
	 * @param values The values to INSERT
	 * @return The ID of the inserted row
	 */
	public int insertSQL(String table, String values) {
		execSQL("INSERT INTO " + table + " VALUES(" + values + ")");
		ResultSet rs = execResultSetSQL("CALL IDENTITY()");
		try {
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Creates a PreparedStatement with the given Query
	 *
	 * @param preparedQuery The PreparedStatement SQL query
	 * @return The created PreparedStatement object
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String preparedQuery) throws SQLException {
		return con.prepareStatement(preparedQuery);
	}

	/**
	 * Executes the SQL query and returns a ResultSet
	 *
	 * @param query The SQL query
	 * @return The ResultSet returned by the query
	 */
	public ResultSet execResultSetSQL(String query) {
		Statement st;
		try {
			st = con.createStatement();
			return st.executeQuery(query);
		} catch (SQLException ex) {
			System.err.println("problem " + ex + "\nexecuting query: " + query);
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Checks for the existence of all the required tables
	 *
	 * @return True if all the tables exist
	 */
	public boolean checkTables() {
		Vector tables = new Vector();
		tables.add(DATA_ITEMS_TABLE);
		tables.add(DATA_TABLES_TABLE);
		tables.add(DATA_TABLE_ITEMS_TABLE);
		tables.add(HOSTS_TABLE);
		tables.add(TYPES_TABLE);
		tables.add(FIDGETS_TABLE);
		tables.add(CONFIG_TABLE);

		for (Iterator it = tables.iterator(); it.hasNext();) {
			if (!tableExists((String)it.next())) {
				dropAll();
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks for the existence of a specific table
	 *
	 * @param tableName The name of the table
	 * @return True if the table exists
	 */
	public boolean tableExists(String tableName) {
		try {
			if (!con.getMetaData().getTables(null, null, tableName, null).next()) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException ex) {
			System.err.println("Error: Table existence check: " + ex);
			return false;
		}
	}

	/**
	 * Creates the tables based on the schema defined in this DDL file
	 *
	 * @param ddlFileName The "Data Definition Language" file
	 */
	public void createTables(String ddlFileName) {
		InputStream inStream = getClass().getResourceAsStream("/" + ddlFileName);
		try {
	  BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
	  String line = null;
	  String content = "";
	  while ((line = in.readLine()) != null)
		content = content.concat(line + "\n");
			in.close();
			execSQL(String.valueOf(content));
	  //@todo Maybe dynamic type conversion in the ddl
		}	catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Called by the DBDataTable to shutdown the DB Server
	 */
	public void dbDataTableShutdown() {
		dbDataTableShutdownFlag = true;
		shutdown();
	}

	/**
	 * Called by the DBRoutingTable to shutdown the DB Server
	 */
	public void dbRoutingTableShutdown() {
		dbRoutingTableShutdownFlag = true;
		shutdown();
	}

	/**
	 * Reset the db
	 */
	public void reset() {
		Vector tables = new Vector();
		tables.add(DATA_ITEMS_TABLE);
		tables.add(DATA_TABLES_TABLE);
		tables.add(DATA_TABLE_ITEMS_TABLE);
		tables.add(HOSTS_TABLE);
		tables.add(FIDGETS_TABLE);
		tables.add(TYPES_TABLE);

		for (Iterator it = tables.iterator(); it.hasNext();) {
			execSQL("DELETE FROM " + (String)it.next());
		}

		compactDB();
	}

	/**
	 * Shuts down the Server and closes the connection
	 */
	public void shutdown() {
		execSQL("SHUTDOWN");
		closeConnect();
	}

	/**
	 * Closes the connection to the DB
	 */
	private void closeConnect() {
		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
