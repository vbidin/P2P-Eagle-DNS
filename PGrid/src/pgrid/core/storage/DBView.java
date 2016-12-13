package pgrid.core.storage;

import pgrid.GUID;
import pgrid.Type;
import pgrid.PGridKey;
import pgrid.PGridHost;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.core.storage.StorageManager;
import pgrid.core.storage.DBDataTable;
import pgrid.core.DBManager;

import java.util.Collection;
import java.util.Vector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.UnknownHostException;

/**
 * <p>Title: DBView</p>
 * <p/>
 * <p>Description: A utility class to generate a "View" of the tables, i.e.
 * generate SQL queries and subqueries</p>
 *
 * @author Mark Kornfilt
 * @version 1.0
 */
public class DBView {

	/**
	 * The DataBase manager
	 */
	private DBManager mDBManager = DBManager.sharedInstance();

	/**
	 * The Data Item manager.
	 */
	private StorageManager mStorageManager = null;

	/**
	 * The Query defining this View
	 */
	private String mSQLStatement;

	/**
	 * Constructs a DBView based on an SQL statement.
	 *
	 * @param sqlStatement the SQL statement.
	 */
	public DBView(String sqlStatement) {
		mStorageManager = PGridP2P.sharedInstance().getStorageManager();
		mSQLStatement = sqlStatement;
	}

	/**
	 * Returns the list of data items.
	 *
	 * @return the list of data items.
	 */
	public Vector getDataItems() {
		ResultSet rs = mDBManager.execResultSetSQL("select di.DATA_ITEM_ID as DATA_ITEM_ID, di.GUID as dGUID, t.NAME as TYPE, di.KEY as KEY, h.GUID as hGUID, h.ADDRESS as ADDR, h.PORT as PORT, di.DATA as DATA " +
				"from " + DBManager.DATA_TABLES_TABLE + " dt, " + DBManager.DATA_TABLE_ITEMS_TABLE + " dti, " +
				DBManager.DATA_ITEMS_TABLE + " di, " + DBManager.HOSTS_TABLE + " h, " + DBManager.TYPES_TABLE + " t " +
				"where di.DATA_ITEM_ID in (select DATA_ITEM_ID from " + getDataItemsAsSQL() + ") and dti.DATA_TABLE_ID = dt.DATA_TABLE_ID and dti.DATA_ITEM_ID = di.DATA_ITEM_ID " +
				"and h.HOST_ID = di.HOST_ID and t.TYPE_ID = di.TYPE_ID");
		Vector dataitems = new Vector();
		try {
			while (rs.next()) {
				GUID dGuid = GUID.getGUID(rs.getString("dGUID"));
				Type type = mStorageManager.createType(rs.getString("TYPE"));
				PGridKey key = new PGridKey(rs.getString("KEY"));
				PGridHost host = PGridHost.getHost(rs.getString("hGUID"), rs.getString("ADDR"), rs.getString("PORT"));
				//@todo	try to avoid the resolve but otherwise the IP is null
				try {
					host.resolve();
				} catch (UnknownHostException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
				String data = rs.getString("DATA");
				p2p.storage.DataItem item = mStorageManager.createDataItem(dGuid, type, key, host, data);
				dataitems.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataitems;
	}

	/**
	 * Returns the SQL query of this view
	 *
	 * @return The SQL query
	 */
	public String getDataItemsAsSQL() {
		return mSQLStatement;
	}

	/**
	 * Counts the number of data items in this view.
	 *
	 * @return the number of items.
	 */
	public int count() {
		return mDBManager.count(mSQLStatement);
	}

	/**
	 * Returns a selection of a DBView according to the given key criteria.
	 *
	 * @param table the source table.
	 * @param criteria the selection criteria.
	 * @return the new DBView.
	 */
	public static DBView selection(DBDataTable table, String criteria) {
		return new DBView("(select DATA_ITEM_ID, KEY from " + table.asView() + " where KEY like '" + criteria + "%')");
	}

	/**
	 * Returns a selection of a DBView according to the given key criteria.
	 *
	 * @param table the source table.
	 * @param criteria the selection criteria.
	 * @return the new DBView.
	 */
	public static DBView selection(DBView table, String criteria) {
		return new DBView("(select DATA_ITEM_ID, KEY from (" + table.getDataItemsAsSQL() + ") where KEY like '" + criteria + "%')");
	}

	/**
	 * Returns a new DBView representing the set difference from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView setDifference(DBDataTable table1, DBDataTable table2) {
		return new DBView("(" + table1.asView() + " except " + table2.asView() + ")");
	}

	/**
	 * Returns a new DBView representing the set difference from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView setDifference(DBDataTable table1, DBView table2) {
		return new DBView("(" + table1.asView() + " except " + table2.getDataItemsAsSQL() + ")");
	}

	/**
	 * Returns a new DBView representing the set difference from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView setDifference(DBView table1, DBDataTable table2) {
		return new DBView("(" + table1.getDataItemsAsSQL() + " except " + table2.asView() + ")");
	}

	/**
	 * Returns a new DBView representing the set difference from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView setDifference(DBView table1, DBView table2) {
		return new DBView("(" + table1.getDataItemsAsSQL() + " except " + table2.getDataItemsAsSQL() + ")");
	}

	/**
	 * Returns a new DBView representing the union from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView union(DBDataTable table1, DBDataTable table2) {
		return new DBView("(" + table1.asView() + " union " + table2.asView() + ")");
	}

	/**
	 * Returns a new DBView representing the union from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView union(DBDataTable table1, DBView table2) {
		return new DBView("(" + table1.asView() + " union " + table2.getDataItemsAsSQL() + ")");
	}

	/**
	 * Returns a new DBView representing the union from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView union(DBView table1, DBDataTable table2) {
		return new DBView("(" + table1.getDataItemsAsSQL() + " union " + table2.asView() + ")");
	}

	/**
	 * Returns a new DBView representing the union from table1 and table2.
	 *
	 * @param table1 the first table.
	 * @param table2 the second table.
	 * @return the new DBView.
	 */
	public static DBView union(DBView table1, DBView table2) {
		return new DBView("(" + table1.getDataItemsAsSQL() + " union " + table2.getDataItemsAsSQL() + ")");
	}

}
