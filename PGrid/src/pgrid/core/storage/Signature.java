/**
 * $Id: Signature.java,v 1.2 2005/11/07 16:56:36 rschmidt Exp $
 *
 * Copyright (c) 2002 The P-Grid Team,
 *                    All Rights Reserved.
 *
 * This file is part of the P-Grid package.
 * P-Grid homepage: http://www.p-grid.org/
 *
 * The P-Grid package is free software; you can redistribute it and/or
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
 * along with this program; see the file LICENSE.
 * If not you can find the GPL at http://www.gnu.org/copyleft/gpl.html
 */

package pgrid.core.storage;

/**
 * This class represents a Data item signature.
 *
 * @author @author <a href="mailto:Roman Schmidt <Roman.Schmidt@epfl.ch>">Roman Schmidt</a>
 * @version 1.0.0
 */
public class Signature {

	/**
	 * The default page size.
	 */
	public static final int DEFAULT_PAGE_SIZE = 16 * 1024;

	/**
	 * The default signature length.
	 */
	public static final int DEFAULT_SIGN_LENGTH = 2;

	/**
	 * The separator between to page signatures.
	 */
	private static final String PAGE_SEPARATOR = ":";

	/**
	 * The separator between two page signatures.
	 */
	private static final String SIGN_SEPARATOR = "-";

	/**
	 * The signature.
	 */
	private long[][] mSignatures = null;

	/**
	 * Creates a new signature for the given amount of pages and the given length.
	 *
	 * @param pages  the amount of pages.
	 * @param length the signature length.
	 */
	public Signature(int pages, int length) {
		mSignatures = new long[pages][length];
	}

	/**
	 * Creates a new signature for the given string.
	 *
	 * @param signature the signature string.
	 */
	public Signature(String signature) {
		String[] pages = pgrid.util.Tokenizer.tokenize(signature, PAGE_SEPARATOR);
		mSignatures = new long[pages.length][];
		for (int page = 0; page < pages.length; page++) {
			String[] signs = pgrid.util.Tokenizer.tokenize(pages[page], SIGN_SEPARATOR);
			mSignatures[page] = new long[signs.length];
			for (int sign = 0; sign < signs.length; sign++)
				mSignatures[page][sign] = Long.parseLong(signs[sign], 16);
		}
	}

	/**
	 * Tests if the given object equals this one.
	 *
	 * @param o the object to compare.
	 * @return <TT>true</TT> if equal, <TT>false</TT> otherwise.
	 */
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass())
			return false;
		Signature sig = (Signature)o;
		if (sig.toString().equals(toString()))
			return true;
		else
			return false;
	}

	/**
	 * Returns a hash code value for the object. This method is
	 * supported for the benefit of hashtables such as those provided by
	 * <code>java.util.Hashtable</code>.
	 * <p/>
	 * The general contract of <code>hashCode</code> is:
	 * <ul>
	 * <li>Whenever it is invoked on the same object more than once during
	 * an execution of a Java application, the <tt>hashCode</tt> method
	 * must consistently return the same integer, provided no information
	 * used in <tt>equals</tt> comparisons on the object is modified.
	 * This integer need not remain consistent from one execution of an
	 * application to another execution of the same application.
	 * <li>If two objects are equal according to the <tt>equals(Object)</tt>
	 * method, then calling the <code>hashCode</code> method on each of
	 * the two objects must produce the same integer result.
	 * <li>It is <em>not</em> required that if two objects are unequal
	 * according to the {@link Object#equals(Object)}
	 * method, then calling the <tt>hashCode</tt> method on each of the
	 * two objects must produce distinct integer results.  However, the
	 * programmer should be aware that producing distinct integer results
	 * for unequal objects may improve the performance of hashtables.
	 * </ul>
	 * <p/>
	 * As much as is reasonably practical, the hashCode method defined by
	 * class <tt>Object</tt> does return distinct integers for distinct
	 * objects. (This is typically implemented by converting the internal
	 * address of the object into an integer, but this implementation
	 * technique is not required by the
	 * Java<font size="-2"><sup>TM</sup></font> programming language.)
	 *
	 * @return a hash code value for this object.
	 * @see Object#equals(Object)
	 * @see java.util.Hashtable
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * Sets the signature for the given page and index.
	 *
	 * @param page  the page.
	 * @param index the index.
	 * @param value the signature value.
	 */
	public void setSignature(int page, int index, long value) {
		mSignatures[page][index] = value;
	}

	/**
	 * Returns a string represantation of the signature.
	 *
	 * @return a string represantation of the signature.
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer(mSignatures.length * (mSignatures.length > 0 ? mSignatures[0].length : 1) * 3);
		for (int i = 0; i < mSignatures.length; i++) {
			for (int j = 0; j < mSignatures[i].length; j++) {
				buff.append(Long.toHexString(mSignatures[i][j]).toUpperCase());
				if ((j + 1) < mSignatures[i].length)
					buff.append(SIGN_SEPARATOR);
			}
			if ((i + 1) < mSignatures.length)
				buff.append(PAGE_SEPARATOR);
		}
		return buff.toString();
	}
}
