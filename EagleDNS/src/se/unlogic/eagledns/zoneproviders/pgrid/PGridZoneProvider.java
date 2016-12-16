package se.unlogic.eagledns.zoneproviders.pgrid;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.xbill.DNS.Zone;

import se.unlogic.eagledns.SecondaryZone;
import se.unlogic.eagledns.SystemInterface;
import se.unlogic.eagledns.zoneproviders.ZoneProvider;

public class PGridZoneProvider implements ZoneProvider {

	private final Logger log = Logger.getLogger(this.getClass());

	private String name;

	public void init(String name) throws Exception {
		this.name = name;
	}

	public Collection<Zone> getPrimaryZones() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<SecondaryZone> getSecondaryZones() {
		throw new UnsupportedOperationException();
	}

	public void zoneUpdated(SecondaryZone secondaryZone) {
		throw new UnsupportedOperationException();
	}

	public void zoneChecked(SecondaryZone secondaryZone) {
		throw new UnsupportedOperationException();
	}

	public void shutdown() throws Exception {
	}

	public void setSystemInterface(SystemInterface systemInterface) {
	}

}
