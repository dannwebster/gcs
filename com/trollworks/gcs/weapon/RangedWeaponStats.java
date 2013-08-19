/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is GURPS Character Sheet.
 *
 * The Initial Developer of the Original Code is Richard A. Wilkes.
 * Portions created by the Initial Developer are Copyright (C) 1998-2002,
 * 2005-2007 the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package com.trollworks.gcs.weapon;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.utility.io.xml.XMLReader;
import com.trollworks.gcs.utility.io.xml.XMLWriter;
import com.trollworks.gcs.utility.text.NumberUtils;
import com.trollworks.gcs.widgets.outline.ListRow;

import java.io.IOException;

/** The stats for a ranged weapon. */
public class RangedWeaponStats extends WeaponStats {
	/** The root XML tag. */
	public static final String	TAG_ROOT			= "ranged_weapon";				//$NON-NLS-1$
	private static final String	TAG_ACCURACY		= "accuracy";					//$NON-NLS-1$
	private static final String	TAG_RANGE			= "range";						//$NON-NLS-1$
	private static final String	TAG_RATE_OF_FIRE	= "rate_of_fire";				//$NON-NLS-1$
	private static final String	TAG_SHOTS			= "shots";						//$NON-NLS-1$
	private static final String	TAG_BULK			= "bulk";						//$NON-NLS-1$
	private static final String	TAG_RECOIL			= "recoil";					//$NON-NLS-1$
	/** The field ID for accuracy changes. */
	public static final String	ID_ACCURACY			= PREFIX + TAG_ACCURACY;
	/** The field ID for range changes. */
	public static final String	ID_RANGE			= PREFIX + TAG_RANGE;
	/** The field ID for rate of fire changes. */
	public static final String	ID_RATE_OF_FIRE		= PREFIX + TAG_RATE_OF_FIRE;
	/** The field ID for shots changes. */
	public static final String	ID_SHOTS			= PREFIX + TAG_SHOTS;
	/** The field ID for bulk changes. */
	public static final String	ID_BULK				= PREFIX + TAG_BULK;
	/** The field ID for recoil changes. */
	public static final String	ID_RECOIL			= PREFIX + TAG_RECOIL;
	private String				mAccuracy;
	private String				mRange;
	private String				mRateOfFire;
	private String				mShots;
	private String				mBulk;
	private String				mRecoil;

	/**
	 * Creates a new {@link RangedWeaponStats}.
	 * 
	 * @param owner The owning piece of equipment or advantage.
	 */
	public RangedWeaponStats(ListRow owner) {
		super(owner);
	}

	/**
	 * Creates a clone of the specified {@link RangedWeaponStats}.
	 * 
	 * @param owner The owning piece of equipment or advantage.
	 * @param other The {@link RangedWeaponStats} to clone.
	 */
	public RangedWeaponStats(ListRow owner, RangedWeaponStats other) {
		super(owner, other);
		mAccuracy = other.mAccuracy;
		mRange = other.mRange;
		mRateOfFire = other.mRateOfFire;
		mShots = other.mShots;
		mBulk = other.mBulk;
		mRecoil = other.mRecoil;
	}

	/**
	 * Creates a {@link RangedWeaponStats}.
	 * 
	 * @param owner The owning piece of equipment or advantage.
	 * @param reader The reader to load from.
	 * @throws IOException
	 */
	public RangedWeaponStats(ListRow owner, XMLReader reader) throws IOException {
		super(owner, reader);
	}

	@Override public WeaponStats clone(ListRow owner) {
		return new RangedWeaponStats(owner, this);
	}

	@Override protected void initialize() {
		mAccuracy = EMPTY;
		mRange = EMPTY;
		mRateOfFire = EMPTY;
		mShots = EMPTY;
		mBulk = EMPTY;
		mRecoil = EMPTY;
	}

	@Override protected void loadSelf(XMLReader reader) throws IOException {
		String name = reader.getName();

		if (TAG_ACCURACY.equals(name)) {
			mAccuracy = reader.readText();
		} else if (TAG_RANGE.equals(name)) {
			mRange = reader.readText();
		} else if (TAG_RATE_OF_FIRE.equals(name)) {
			mRateOfFire = reader.readText();
		} else if (TAG_SHOTS.equals(name)) {
			mShots = reader.readText();
		} else if (TAG_BULK.equals(name)) {
			mBulk = reader.readText();
		} else if (TAG_RECOIL.equals(name)) {
			mRecoil = reader.readText();
		} else {
			super.loadSelf(reader);
		}
	}

	@Override protected String getRootTag() {
		return TAG_ROOT;
	}

	@Override protected void saveSelf(XMLWriter out) {
		out.simpleTagNotEmpty(TAG_ACCURACY, mAccuracy);
		out.simpleTagNotEmpty(TAG_RANGE, mRange);
		out.simpleTagNotEmpty(TAG_RATE_OF_FIRE, mRateOfFire);
		out.simpleTagNotEmpty(TAG_SHOTS, mShots);
		out.simpleTagNotEmpty(TAG_BULK, mBulk);
		out.simpleTagNotEmpty(TAG_RECOIL, mRecoil);
	}

	/** @return The accuracy. */
	public String getAccuracy() {
		return mAccuracy;
	}

	/**
	 * Sets the value of accuracy.
	 * 
	 * @param accuracy The value to set.
	 */
	public void setAccuracy(String accuracy) {
		accuracy = sanitize(accuracy);
		if (!mAccuracy.equals(accuracy)) {
			mAccuracy = accuracy;
			notifySingle(ID_ACCURACY);
		}
	}

	/** @return The bulk. */
	public String getBulk() {
		return mBulk;
	}

	/**
	 * Sets the value of bulk.
	 * 
	 * @param bulk The value to set.
	 */
	public void setBulk(String bulk) {
		bulk = sanitize(bulk);
		if (!mBulk.equals(bulk)) {
			mBulk = bulk;
			notifySingle(ID_BULK);
		}
	}

	/** @return The range. */
	public String getRange() {
		return mRange;
	}

	/** @return The range, fully resolved for the user's ST, if possible. */
	public String getResolvedRange() {
		DataFile df = getOwner().getDataFile();
		String range = mRange;

		if (df instanceof GURPSCharacter) {
			int strength = ((GURPSCharacter) df).getStrength();
			String savedRange;

			do {
				savedRange = range;
				range = resolveRange(range, strength);
			} while (!savedRange.equals(range));
		}
		return range;
	}

	private String resolveRange(String range, int strength) {
		int where = range.indexOf("x"); //$NON-NLS-1$

		if (where != -1) {
			int last = where + 1;
			int max = range.length();

			last = skipSpaces(range, last);
			if (last < max) {
				double value = 0.0;
				char ch = range.charAt(last);
				boolean found = false;
				double multiplier = 1.0;

				while (multiplier == 1.0 && ch == '.' || ch >= '0' && ch <= '9') {
					found = true;
					if (ch == '.') {
						multiplier = 0.1;
					} else if (multiplier == 1.0) {
						value *= 10.0;
						value += ch - '0';
					} else {
						value += (ch - '0') * multiplier;
						multiplier *= 0.1;
					}
					if (++last >= max) {
						break;
					}
					ch = range.charAt(last);
				}
				if (found) {
					StringBuffer buffer = new StringBuffer();

					if (where > 0) {
						buffer.append(range.substring(0, where));
					}
					strength *= value;
					buffer.append(NumberUtils.format(strength));
					if (last < max) {
						buffer.append(range.substring(last));
					}
					return buffer.toString();
				}
			}
		}
		return range;
	}

	/**
	 * Sets the value of range.
	 * 
	 * @param range The value to set.
	 */
	public void setRange(String range) {
		range = sanitize(range);
		if (!mRange.equals(range)) {
			mRange = range;
			notifySingle(ID_RANGE);
		}
	}

	/** @return The rate of fire. */
	public String getRateOfFire() {
		return mRateOfFire;
	}

	/**
	 * Sets the value of rate of fire.
	 * 
	 * @param rateOfFire The value to set.
	 */
	public void setRateOfFire(String rateOfFire) {
		rateOfFire = sanitize(rateOfFire);
		if (!mRateOfFire.equals(rateOfFire)) {
			mRateOfFire = rateOfFire;
			notifySingle(ID_RATE_OF_FIRE);
		}
	}

	/** @return The recoil. */
	public String getRecoil() {
		return mRecoil;
	}

	/**
	 * Sets the value of recoil.
	 * 
	 * @param recoil The value to set.
	 */
	public void setRecoil(String recoil) {
		recoil = sanitize(recoil);
		if (!mRecoil.equals(recoil)) {
			mRecoil = recoil;
			notifySingle(ID_RECOIL);
		}
	}

	/** @return The shots. */
	public String getShots() {
		return mShots;
	}

	/**
	 * Sets the value of shots.
	 * 
	 * @param shots The value to set.
	 */
	public void setShots(String shots) {
		shots = sanitize(shots);
		if (!mShots.equals(shots)) {
			mShots = shots;
			notifySingle(ID_SHOTS);
		}
	}

	@Override public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RangedWeaponStats) {
			RangedWeaponStats other = (RangedWeaponStats) obj;

			return mAccuracy.equals(other.mAccuracy) && mRange.equals(other.mRange) && mRateOfFire.equals(other.mRateOfFire) && mShots.equals(other.mShots) && mBulk.equals(other.mBulk) && mRecoil.equals(other.mRecoil) && super.equals(obj);
		}
		return false;
	}
}