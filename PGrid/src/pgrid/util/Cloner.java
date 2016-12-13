/**
 * Copyright (c) 2003 Roman Schmidt,
 *                    All Rights Reserved.
 *
 * This file is part of the pgrid.utils package.
 * pgrid.utils homepage: http://lsirpeople.epfl.ch/pgrid.helper/pgrid.utils
 *
 * The pgrid.utils package is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this package; see the file gpl.txt.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class makes deep hard copy of any {@link java.io.Serializable} object and all of its
 * {@link java.io.Serializable} member objects.<p>
 * <p/>
 * <b>Example:</b>
 * <pre>
 * Object obj = new Object();
 * Object clone = (Object)Cloner.clone(obj);
 * </pre>
 *
 * @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0 2003/04/15
 * @see java.io.Serializable
 */
public class Cloner {

	/**
	 * Creates a new <code>Cloner</code>.
	 */
	protected Cloner() {
		// do nothing
	}

	/**
	 * Clones the given object.
	 *
	 * @param o the object to clone.
	 * @return the clone.
	 * @throws Exception if any Exception occures
	 */
	public static Object clone(Object o) throws Exception {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bo);
		out.writeObject(o);
		out.close();
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bi);
		Object clone = in.readObject();
		return clone;
	}

}