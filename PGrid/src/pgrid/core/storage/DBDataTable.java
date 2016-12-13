package pgrid.core.storage;

import p2p.basic.Peer;
import pgrid.*;
import pgrid.core.DBManager;
import pgrid.interfaces.basic.PGridP2P;

import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>Title: DBDataTable</p>
 * <p/>
 * <p>Description: DataTable subclass representing the DataBase DataTable</p>
 *
 * @author Mark Kornfilt
 * @version 1.0
 */
public class DBDataTable extends DataTable {

	/**
	 * The data item manager.
	 */
	private StorageManager mStorageManager = StorageManager.getInstance();

	/**
	 * The Data Table id in the DB.
	 */
	private int mDataTableID = -1;

	/**
	 * The DataBase manager
	 */
	private DBManager mDBManager = DBManager.sharedInstance();

	/**
	 * The Host id in the DB.
	 */
	private int mHostID = -1;

	/**
	 * The Signature of the data stored in this data table.
	 */
	private Signature mSignature = null;

	/**
	 * The PGrid facility.
	 */
	private PGridP2P mPGrid = PGridP2P.sharedInstance();

	/**
	 * Construct a data table for the given host.
	 *
	 * @param host the host.
	 */
	public DBDataTable(PGridHost host) {
	   init(host);

	}

	/**
	 * Initialize a datatable
	 *
	 *  @param host the host.
	 */
	public void init(PGridHost host) {
		// add the corresponding host
		mHostID = addHost(host);

		// check if a data table already exists and create it
		mDataTableID = addDataTable(mHostID);

		// read the signature from the DB
		ResultSet rs = mDBManager.execResultSetSQL("select SIGNATURE from " + DBManager.DATA_TABLES_TABLE + " where DATA_TABLE_ID = " + mDataTableID);
		try {
			if (rs.next()) {
				mSignature = new Signature(rs.getString("SIGNATURE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds all delivered Data Items.
	 *
	 * @param collection the Collection.
	 */
	public void addAll(Collection collection) {
		if (collection == null)
			new NullPointerException();
		if (collection.size() == 0)
			return;
		String sql = "";
		for (Iterator it = collection.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			sql += "insert into " + DBManager.DATA_ITEMS_TABLE + " values " +
					"(null, '" + item.getGUID().toString() + "', " +
					addType(item.getType().toString()) + ", '" +
					item.getKey().toString() + "', " +
					addHost((PGridHost)item.getPeer()) + ", '" +
					item.getData().toString() + "');\n";

			sql += "insert into " + DBManager.DATA_TABLE_ITEMS_TABLE + " values (" + mDataTableID + ", identity());\n";
		}
		mDBManager.execSQL(sql);
		mSignature = null;
		if (Constants.TESTS) {
			int count = count();
			int countPath = getDataItems(mPGrid.getLocalPath()).size();
			mPGrid.getStatistics().DataItemsManaged = count;
			mPGrid.getStatistics().DataItemsPath = countPath;
		}
	}

	/**
	 * Inserts a DataItem in the DB.
	 *
	 * @param item the data item.
	 */
	public void addDataItem(DataItem item) {
		try {
			PreparedStatement ps = mDBManager.prepareStatement("insert into " + DBManager.DATA_ITEMS_TABLE + " values (null, ?, ?, ?, ?, ?)");
			ps.setString(1, item.getGUID().toString());
			ps.setInt(2, addType(item.getType().toString()));
			ps.setString(3, item.getKey().toString());
			ps.setInt(4, addHost((PGridHost)item.getPeer()));
			ps.setString(5, item.getData().toString());


			ps.execute();
			ResultSet rs = mDBManager.execResultSetSQL("CALL IDENTITY()");
			rs.next();
			int id = rs.getInt(1);

			PreparedStatement ps2 = mDBManager.prepareStatement("insert into " + DBManager.DATA_TABLE_ITEMS_TABLE + " values (?, ?)");
			ps2.setString(1, String.valueOf(mDataTableID));
			ps2.setString(2, String.valueOf(id));
			ps2.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getErrorCode() == -104) return;
			System.err.println("problem " + e + "\nexecuting query: error code: " + e.getErrorCode());
			e.printStackTrace();
			return;
		}
		mSignature = null;
	}

	/**
	 * Update a DataItem in the DB.
	 *
	 * @param item the data item.
	 */
	public void updateDataItem(DataItem item) {
		try {
			PreparedStatement ps = mDBManager.prepareStatement("update " + DBManager.DATA_ITEMS_TABLE + " SET DATA = ?, KEY = ? WHERE GUID = ?");
			ps.setObject(1, item.getData().toString());
			ps.setString(2, item.getKey().toString());
			ps.setString(3, item.getGUID().toString());
			ps.execute();
		} catch (SQLException e) {
			if (e.getErrorCode() == -104) return;
			System.err.println("problem " + e + "\nexecuting query: error code: " + e.getErrorCode());
			e.printStackTrace();
			return;
		}
		mSignature = null;
	}

	/**
	 * Add a data table entry.
	 *
	 * @param hostID the Host ID in the DB.
	 * @return the data table id if the host exists or has been inserted, -1 if insertion failed.
	 */
	private int addDataTable(int hostID) {
		// check if host already exists
		int id = getDataTableID(hostID);
		if (id >= 0)
			return id;

		// create data table
		return mDBManager.insertSQL(DBManager.DATA_TABLES_TABLE, "null, " + hostID + ", ''");
	}

	/**
	 * Adds a Host to the DB.
	 *
	 * @param host the host to add.
	 * @return The host ID if the host has been inserted, -1 otherwise.
	 */
	private int addHost(PGridHost host) {
		// check if host already exists
		int id = getHostID(host);
		if (id >= 0)
			return id;

		// add new host
		String hostGUID = host.getGUID().toString();
		String hostAddress = host.getAddressString();
		int hostPort = host.getPort();
		int hostQOS = host.getSpeed();
		String hostPath = host.getPath();
		long hostTimeStamp = host.getPathTimestamp();
		return mDBManager.insertSQL(DBManager.HOSTS_TABLE, "null,'" + hostGUID + "','" + hostAddress + "'," + hostPort + ",'" + hostQOS + "','" + hostPath + "'," + hostTimeStamp);
	}

	/**
	 * Returns the SQL statement used to select all data items of this data table.
	 *
	 * @return the SQL statement.
	 */
	String asView() {
		return asView("");
	}

	/**
	 * Returns the SQL statement used to select all data items of this data table.
	 *
	 * @return the SQL statement.
	 */
	String asView(String prefix) {
		return "(select DATA_ITEM_ID, KEY from " + getDataItemsAsSQL(prefix) + ")";
	}

	/**
	 * Removes all data items from the DB belonging to this data table.
	 */
	synchronized public void clear() {
		mDBManager.execDeleteSQL("delete from " + DBManager.HOSTS_TABLE + " where HOST_ID in (select HOST_ID from " +
				DBManager.DATA_TABLES_TABLE + " where DATA_TABLE_ID = " + mDataTableID + ")");
	}

	/**
	 * Returns the number of locally managed DataItems.
	 *
	 * @return the number of DataItems.
	 */
	public int count() {
		return mDBManager.count(getDataItemsAsSQL());
	}

	/**
	 * Removes the data table from the DB.
	 */
	synchronized public void delete() {
		mDBManager.execDeleteSQL("delete from " + DBManager.DATA_TABLES_TABLE + " where DATA_TABLE_ID = " + mDataTableID);
	}

	/**
	 * Duplicates all data items of this data table for to the given one.
	 * @param dataTable the data table to extend.
	 */
	public void duplicate(DBDataTable dataTable) {
		// duplicate data items from the table
		String sql = "insert into " + DBManager.DATA_TABLE_ITEMS_TABLE +
								 " select " + dataTable.getDataTableID() + ", DATA_ITEM_ID from " + getDataItemsAsSQL();
		mDBManager.execSQL(sql);
	}

	/**
	 * Returns the list of all Data Items.
	 *
	 * @return the list of all data items.
	 */
	public Collection getDataItems() {
		return getDataItems("");
	}

	/**
	 * Returns the list of data items with the given prefix.
	 *
	 * @param prefix the common prefix of the selected data items.
	 * @return the list of data items.
	 */
	public Collection getDataItems(String prefix) {
		ResultSet rs = mDBManager.execResultSetSQL(getDataItemsAsSQL(prefix));
		return getDataItems(rs);
	}

		/**
	 * Returns the list of data items with the given prefix.
	 *
	 * @param prefix the common prefix of the selected data items.
	 * @return the list of data items.
	 */
	public Collection getDataItemsDataPrefixed(String prefix) {
			ResultSet rs = null;
			try {
				rs = getDataItemsByDataPrefix(prefix).executeQuery();
			} catch (SQLException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			return getDataItems(rs);
	}

	/**
	 * Returns the list of data items with the given prefix.
	 *
	 * @param lprefix the common prefix of the selected data items.
	 * @param hprefix the common prefix of the selected data items.
	 * @return the list of data items.
	 */
	public Collection getDataItemsDataPrefixed(String lprefix, String hprefix) {
		ResultSet rs = null;
		try {
			rs = getDataItemsByDataPrefix(lprefix, hprefix).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return getDataItems(rs);
	}

	/**
	 * Returns the list of data items for the given result set.
	 *
	 * @param rs the result set.
	 * @return the list of data items.
	 */
	private Collection getDataItems(ResultSet rs) {
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
	 * Returns the SQL statement used to select all data items of this data table.
	 *
	 * @return the SQL statement.
	 */
	private String getDataItemsAsSQL() {
		return getDataItemsAsSQL("");
	}

	/**
	 * Returns the SQL statement used to select all data items with the given prefix of this data table.
	 *
	 * @return the SQL statement.
	 */
	private String getDataItemsAsSQL(String prefix) {
		return "(select di.DATA_ITEM_ID as DATA_ITEM_ID, di.GUID as dGUID, t.NAME as TYPE, di.KEY as KEY, h.GUID as hGUID, h.ADDRESS as ADDR, h.PORT as PORT, di.DATA as DATA " +
				"from " + DBManager.DATA_TABLES_TABLE + " dt, " + DBManager.DATA_TABLE_ITEMS_TABLE + " dti, " +
				DBManager.DATA_ITEMS_TABLE + " di, " + DBManager.HOSTS_TABLE + " h, " + DBManager.TYPES_TABLE + " t " +
				"where dt.DATA_TABLE_ID = " + mDataTableID + " and dti.DATA_TABLE_ID = dt.DATA_TABLE_ID and dti.DATA_ITEM_ID = di.DATA_ITEM_ID " +
				"and h.host_id = di.host_id and t.type_id = di.type_id and di.KEY like '" + prefix + "%')";
	}

	/**
	 * Returns the SQL statement used to select all data items with the given prefix of this data table.
	 *
	 * @return the SQL statement.
	 */
	private PreparedStatement getDataItemsByDataPrefix(String prefix) {
		PreparedStatement ps=null;
		try {
			ps = mDBManager.prepareStatement("(select di.DATA_ITEM_ID as DATA_ITEM_ID, di.GUID as dGUID, t.NAME as TYPE, di.KEY as KEY, h.GUID as hGUID, h.ADDRESS as ADDR, h.PORT as PORT, di.DATA as DATA " +
					"from " + DBManager.DATA_TABLES_TABLE + " dt, " + DBManager.DATA_TABLE_ITEMS_TABLE + " dti, " +
					DBManager.DATA_ITEMS_TABLE + " di, " + DBManager.HOSTS_TABLE + " h, " + DBManager.TYPES_TABLE + " t " +
					"where dt.DATA_TABLE_ID = " + mDataTableID + " and dti.DATA_TABLE_ID = dt.DATA_TABLE_ID and dti.DATA_ITEM_ID = di.DATA_ITEM_ID " +
					"and h.host_id = di.host_id and t.type_id = di.type_id and di.DATA like ?)");
			ps.setString(1, "%"+prefix+"%");
		} catch (SQLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return ps;
	}

		/**
	 * Returns the SQL statement used to select all data items with the given prefix of this data table.
	 *
	 * @return the SQL statement.
	 */
	private PreparedStatement getDataItemsByDataPrefix(String lowerPrefix, String higherPrefix) {
		PreparedStatement ps=null;
		try {
			ps = mDBManager.prepareStatement("(select di.DATA_ITEM_ID as DATA_ITEM_ID, di.GUID as dGUID, t.NAME as TYPE, di.KEY as KEY, h.GUID as hGUID, h.ADDRESS as ADDR, h.PORT as PORT, di.DATA as DATA " +
					"from " + DBManager.DATA_TABLES_TABLE + " dt, " + DBManager.DATA_TABLE_ITEMS_TABLE + " dti, " +
					DBManager.DATA_ITEMS_TABLE + " di, " + DBManager.HOSTS_TABLE + " h, " + DBManager.TYPES_TABLE + " t " +
					"where dt.DATA_TABLE_ID = " + mDataTableID + " and dti.DATA_TABLE_ID = dt.DATA_TABLE_ID and dti.DATA_ITEM_ID = di.DATA_ITEM_ID " +
					"and h.host_id = di.host_id and t.type_id = di.type_id and ( di.DATA > ? OR di.DATA = ? ) AND ( di.DATA < ? OR di.DATA = ?))");
			ps.setString(1, lowerPrefix);
			ps.setString(2, lowerPrefix);
			ps.setString(3, higherPrefix);
			ps.setString(4, higherPrefix);
		} catch (SQLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return ps;
	}

	/**
	 * Returns the data table ID if a data table exists.
	 *
	 * @return the data table ID if it exists, -1 otherwise.
	 */
	protected int getDataTableID() {
		return mDataTableID;
	}

	/**
	 * Returns the data table ID if a data table exists for the given host ID.
	 *
	 * @param hostID the host ID.
	 * @return the data table ID if it exists, -1 otherwise.
	 */
	private int getDataTableID(int hostID) {
		ResultSet rs = mDBManager.execResultSetSQL("select DATA_TABLE_ID from " + DBManager.DATA_TABLES_TABLE + " where HOST_ID=" + hostID);
		try {
			if (rs.next()) {
				return rs.getInt("DATA_TABLE_ID");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Returns the host id in the DB.
	 *
	 * @param host the host.
	 * @return the host ID if the host exists, -1 otherwise.
	 */
	private int getHostID(PGridHost host) {
		String hostGUID = host.getGUID().toString();
		ResultSet rs = mDBManager.execResultSetSQL("select HOST_ID from " + DBManager.HOSTS_TABLE + " where GUID='" + hostGUID + "'");
		try {
			if (rs.next()) {
				return rs.getInt("HOST_ID");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Returns the list of data items from the host.
	 *
	 * @return the list of data items.
	 */
	public Collection getOwnedDataItems() {
		String sql = "(select di.DATA_ITEM_ID as DATA_ITEM_ID, di.GUID as dGUID, t.NAME as TYPE, di.KEY as KEY, h.GUID as hGUID, h.ADDRESS as ADDR, h.PORT as PORT, di.DATA as DATA " +
				"from " + DBManager.DATA_TABLES_TABLE + " dt, " + DBManager.DATA_TABLE_ITEMS_TABLE + " dti, " +
				DBManager.DATA_ITEMS_TABLE + " di, " + DBManager.HOSTS_TABLE + " h, " + DBManager.TYPES_TABLE + " t " +
				"where dt.DATA_TABLE_ID = " + mDataTableID + " and dti.DATA_TABLE_ID = dt.DATA_TABLE_ID and dti.DATA_ITEM_ID = di.DATA_ITEM_ID " +
				"and h.host_id = di.host_id and t.type_id = di.type_id and di.host_id = " + mHostID + ")";
		ResultSet rs = mDBManager.execResultSetSQL(sql);
		return getDataItems(rs);
	}

	/**
	 * Sets the data table to contain only the elements in the view.
	 *
	 * @param table the view representing the new elements of the table.
	 */
	public void setDataTable(DBView table) {
		// keep only the items in the table
		String sql = "delete from " + DBManager.DATA_TABLE_ITEMS_TABLE + " where DATA_ITEM_ID not in (select DATA_ITEM_ID from (" + table.getDataItemsAsSQL() + "))";
		mDBManager.execDeleteSQL(sql);

		// insert new items from the table
		sql = "insert into " + DBManager.DATA_TABLE_ITEMS_TABLE +
								 " select " + mDataTableID + ", DATA_ITEM_ID from " + table.getDataItemsAsSQL() +
								 " except (select * from DATA_TABLE_ITEMS where DATA_TABLE_ID = " + mDataTableID + ")";
		mDBManager.execSQL(sql);
		mSignature = null;
	}

	/**
	 * Returns the signature for the data items.
	 *
	 * @return the signature.
	 */
	public Signature getSignature() {
		// return signature if known
		if (mSignature != null)
			return mSignature;

		// calculate current signature
		StringBuffer signStr = new StringBuffer(count() * 100);
		ResultSet rs = mDBManager.execResultSetSQL("select dGUID from " + getDataItemsAsSQL() + " order by dGUID");
		try {
			while (rs.next()) {
				signStr.append(rs.getString("dGUID") + "\n");
			}
			setSignature(mUtils.signature(signStr.toString(), Signature.DEFAULT_PAGE_SIZE, Signature.DEFAULT_SIGN_LENGTH));
			return mSignature;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}


	/**
	 * Sets the signature of the data table.
	 * @param signature the signature.
	 */
	public void setSignature(Signature signature) {
		mSignature = signature;

		// safe signature in the DB
		String sql = "update " + DBManager.DATA_TABLES_TABLE + " set SIGNATURE = '" + mSignature.toString() + "'";
		mDBManager.execSQL(sql);
	}

	/**
	 * Removes all given data items.
	 *
	 * @param items the items to remove.
	 */
	public void removeAll(Collection items) {
		if (items == null)
			throw new NullPointerException();

		for (Iterator it = items.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			removeDataItem(item);
		}
	}

	/**
	 * Update all given data items.
	 *
	 * @param items the items to Update.
	 */
	public void updateAll(Collection items) {
		if (items == null)
			throw new NullPointerException();

		for (Iterator it = items.iterator(); it.hasNext();) {
			DataItem item = (DataItem)it.next();
			updateDataItem(item);
		}
	}

	/**
	 * Removes all given data items owned by peer.
	 *
	 * @param items the items to remove.
	 */
	// TODO: parse items to remove only items owned by peer
	public void removeAll(Collection items, Peer peer) {
		removeAll(items);
	}

	/**
	 * Update all given data items owned by peer.
	 *
	 * @param items the items to remove.
	 */
	// TODO: parse items to update only items owned by peer
	public void updateAll(Collection items, Peer peer) {
		updateAll(items);
	}

	/**
	 * add a Type in the Types table
	 *
	 * @param typeString the String description of the type
	 * @return the type_id if the type exists or has been inserted or -1 if
	 *         insertion failed
	 */
	public int addType(String typeString) {
		ResultSet rs = mDBManager.execResultSetSQL("SELECT TYPE_ID FROM " + DBManager.TYPES_TABLE + " WHERE name='" + typeString + "'");
		try {
			if (rs.next()) {
				return rs.getInt("TYPE_ID");
			} else {
				return mDBManager.insertSQL(DBManager.TYPES_TABLE, "null,'" + typeString + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * Removes the given Data Item.
	 *
	 * @param dataItem the item to remove.
	 */
	public void removeDataItem(DataItem dataItem) {
		if (dataItem == null)
			throw new NullPointerException();

		mDBManager.execDeleteSQL("delete from " + DBManager.DATA_TABLE_ITEMS_TABLE + " where DATA_ITEM_ID in " +
									 "(select DATA_ITEM_ID from " + DBManager.DATA_ITEMS_TABLE + " where GUID = '" + dataItem.getGUID().toString() + "')");
		mDBManager.execDeleteSQL("delete from " + DBManager.DATA_ITEMS_TABLE + " where GUID = '" + dataItem.getGUID().toString() + "' and DATA_ITEM_ID not in " +
														 "(select DATA_ITEM_ID from " + DBManager.DATA_TABLE_ITEMS_TABLE + ")");
		mSignature = null;
	}

	/**
	 * This method saves the signature and deletes duplicate GUID type data items
	 */
	public void save() {
		mDBManager.execDeleteSQL("DELETE FROM " + DBManager.DATA_ITEMS_TABLE + " WHERE dataitem_id IN (SELECT d1.dataitem_id FROM dataitems d1,dataitems d2 WHERE d1.dataitem_id <> d2.dataitem_id AND d1.dataitem_id > d2.dataitem_id AND d1.key=d2.key AND d1.file_id IS NULL AND d1.host_id=d2.host_id AND d1.managinghost_id = d2.managinghost_id)");
		mDBManager.setConfig("signature", getSignature().toString()); //saves the datatable signature
	}

	/**
	 * Shuts down the DataBase and closes the connection
	 */
	public void shutdown() {
		mDBManager.setConfig("signature", getSignature().toString()); // saves the datatable signature
		mDBManager.dbDataTableShutdown();
	}


}
