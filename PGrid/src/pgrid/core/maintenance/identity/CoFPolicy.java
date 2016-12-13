/*
 * Created on Jan 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package pgrid.core.maintenance.identity;

import pgrid.util.Utils;
import pgrid.interfaces.basic.PGridP2P;
import pgrid.core.maintenance.identity.CoUPolicy;
import pgrid.Properties;
import pgrid.PGridHost;
import pgrid.Constants;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author <a href="mailto:Renault John <renault.john@epfl.ch>">Renault John</a>
 */
public class CoFPolicy extends CoUPolicy {

	/**
	 * The P-Grid facility.
	 */
	private PGridP2P mPGridP2P = null;

	/**
	 * Stale percentage before maintenance
	 */
	protected int mStalePercentage;

	/**
	 * Name of this protocol
	 */
	public final static String PROTOCOL_NAME = "CoF";

	public CoFPolicy() {
		mPGridP2P = PGridP2P.sharedInstance();
		mStalePercentage = mPGridP2P.propertyInteger(Properties.IDENTITY_COF_MAX_STALE);
	}

	/**
	 * @see pgrid.core.maintenance.identity.JoinLeaveProtocol#getProtocolName()
	 */
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	/**
	 * In lazzy strategy (CoF), We should correct the routing table only if in the
	 * current level a certain percent of it is offline or staled.
	 *
	 * @see pgrid.core.maintenance.identity.MaintenancePolicy#stale(pgrid.PGridHost)
	 */
	public boolean stale(PGridHost host) {
		Vector staleHosts = new Vector();
		int percentage = 0;

		if (host.getState() != PGridHost.HOST_UPDATING)
			host.setState(PGridHost.HOST_STALE);

		// if the bootstrap host is unreachable, nothing can be done.
		if (host.getGUID() == null) {
			host.setState(PGridHost.HOST_OFFLINE);
			return false;
		}

		if (mStalePercentage > 0) {
			// check the stale percentage
			PGridHost hosts[];

			// check if the host is a replicas or a route
			if (mPGridP2P.getRoutingTable().getReplicaVector().contains(host)) {
				hosts = mPGridP2P.getRoutingTable().getReplicas();
			} else {
				int commun = Utils.commonPrefix(mPGridP2P.getLocalPath(), host.getPath()).length();
				hosts = mPGridP2P.getRoutingTable().getLevel(commun);
			}

			// perform percentage
			int offline = 0;
			for (int i = 0; i < hosts.length; i++) {
				if (hosts[i].getState() == PGridHost.HOST_OFFLINE ||
						hosts[i].getState() == PGridHost.HOST_STALE) {
					offline++;
					if (!host.equals(hosts[i]))
						staleHosts.add(hosts[i]);
				}
			}
			percentage = (int)(((float)offline / (float)hosts.length) * 100.0);
		}

		if (percentage < mStalePercentage) {
			Constants.LOGGER.finer(percentage + "% of unreachable peers.");
			return false;
		}

		Iterator it = staleHosts.iterator();
		while (it.hasNext()) {
			PGridHost element = (PGridHost)it.next();
			host.setState(PGridHost.HOST_UPDATING);
			queryMapping(element, false);
		}


		// if the percentage is higher then the minimum, correct this host.
		return queryMapping(host, true);
	}
}
