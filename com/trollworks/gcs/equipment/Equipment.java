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

package com.trollworks.gcs.equipment;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.common.DataFile;
import com.trollworks.gcs.skill.SkillDefault;
import com.trollworks.gcs.utility.collections.EnumExtractor;
import com.trollworks.gcs.utility.io.Images;
import com.trollworks.gcs.utility.io.LocalizedMessages;
import com.trollworks.gcs.utility.io.xml.XMLReader;
import com.trollworks.gcs.utility.io.xml.XMLWriter;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.weapon.MeleeWeaponStats;
import com.trollworks.gcs.weapon.OldWeapon;
import com.trollworks.gcs.weapon.RangedWeaponStats;
import com.trollworks.gcs.weapon.WeaponStats;
import com.trollworks.gcs.widgets.outline.Column;
import com.trollworks.gcs.widgets.outline.ListRow;
import com.trollworks.gcs.widgets.outline.Row;
import com.trollworks.gcs.widgets.outline.RowEditor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/** A piece of equipment. */
public class Equipment extends ListRow {
	private static String				MSG_DEFAULT_NAME;
	private static final String			NEWLINE						= "\n";										//$NON-NLS-1$
	private static final String			SPACE						= " ";											//$NON-NLS-1$
	private static final String			DEFAULT_LEGALITY_CLASS		= "4";											//$NON-NLS-1$
	private static final String			EMPTY						= "";											//$NON-NLS-1$
	/** The XML tag used for items. */
	public static final String			TAG_EQUIPMENT				= "equipment";									//$NON-NLS-1$
	/** The XML tag used for containers. */
	public static final String			TAG_EQUIPMENT_CONTAINER		= "equipment_container";						//$NON-NLS-1$
	private static final String			ATTRIBUTE_EQUIPPED			= "equipped";									//$NON-NLS-1$
	private static final String			TAG_QUANTITY				= "quantity";									//$NON-NLS-1$
	private static final String			TAG_DESCRIPTION				= "description";								//$NON-NLS-1$
	private static final String			TAG_TECH_LEVEL				= "tech_level";								//$NON-NLS-1$
	private static final String			TAG_LEGALITY_CLASS			= "legality_class";							//$NON-NLS-1$
	private static final String			TAG_VALUE					= "value";										//$NON-NLS-1$
	private static final String			TAG_WEIGHT					= "weight";									//$NON-NLS-1$
	private static final String			ATTRIBUTE_UNITS				= "units";										//$NON-NLS-1$
	private static final String			TAG_REFERENCE				= "reference";									//$NON-NLS-1$
	/** The prefix used in front of all IDs for the equipment. */
	public static final String			PREFIX						= GURPSCharacter.CHARACTER_PREFIX + "equipment.";	//$NON-NLS-1$
	/** The field ID for equipped changes. */
	public static final String			ID_EQUIPPED					= PREFIX + "Equipped";							//$NON-NLS-1$
	/** The field ID for quantity changes. */
	public static final String			ID_QUANTITY					= PREFIX + "Quantity";							//$NON-NLS-1$
	/** The field ID for description changes. */
	public static final String			ID_DESCRIPTION				= PREFIX + "Description";						//$NON-NLS-1$
	/** The field ID for tech level changes. */
	public static final String			ID_TECH_LEVEL				= PREFIX + "TechLevel";						//$NON-NLS-1$
	/** The field ID for legality changes. */
	public static final String			ID_LEGALITY_CLASS			= PREFIX + "LegalityClass";					//$NON-NLS-1$
	/** The field ID for value changes. */
	public static final String			ID_VALUE					= PREFIX + "Value";							//$NON-NLS-1$
	/** The field ID for weight changes. */
	public static final String			ID_WEIGHT					= PREFIX + "Weight";							//$NON-NLS-1$
	/** The field ID for extended value changes */
	public static final String			ID_EXTENDED_VALUE			= PREFIX + "ExtendedValue";					//$NON-NLS-1$
	/** The field ID for extended weight changes */
	public static final String			ID_EXTENDED_WEIGHT			= PREFIX + "ExtendedWeight";					//$NON-NLS-1$
	/** The field ID for page reference changes. */
	public static final String			ID_REFERENCE				= PREFIX + "Reference";						//$NON-NLS-1$
	/** The field ID for when the row hierarchy changes. */
	public static final String			ID_LIST_CHANGED				= PREFIX + "ListChanged";						//$NON-NLS-1$
	/** The field ID for when the equipment becomes or stops being a weapon. */
	public static final String			ID_WEAPON_STATUS_CHANGED	= PREFIX + "WeaponStatus";						//$NON-NLS-1$
	private boolean						mEquipped;
	private int							mQuantity;
	private String						mDescription;
	private String						mTechLevel;
	private String						mLegalityClass;
	private double						mValue;
	private double						mWeight;
	private double						mExtendedValue;
	private double						mExtendedWeight;
	private String						mReference;
	private ArrayList<WeaponStats>	mWeapons;
	// For load-time conversion only
	private OldWeapon					mOldWeapon;

	static {
		LocalizedMessages.initialize(Equipment.class);
	}

	/**
	 * Creates a new equipment.
	 * 
	 * @param dataFile The data file to associate it with.
	 * @param isContainer Whether or not this row allows children.
	 */
	public Equipment(DataFile dataFile, boolean isContainer) {
		super(dataFile, isContainer);
		mEquipped = true;
		mQuantity = 1;
		mDescription = MSG_DEFAULT_NAME;
		mTechLevel = EMPTY;
		mLegalityClass = DEFAULT_LEGALITY_CLASS;
		mReference = EMPTY;
		mWeapons = new ArrayList<WeaponStats>();
	}

	/**
	 * Creates a clone of an existing equipment and associates it with the specified data file.
	 * 
	 * @param dataFile The data file to associate it with.
	 * @param equipment The equipment to clone.
	 * @param deep Whether or not to clone the children, grandchildren, etc.
	 */
	public Equipment(DataFile dataFile, Equipment equipment, boolean deep) {
		super(dataFile, equipment);
		boolean forSheet = dataFile instanceof GURPSCharacter;
		mEquipped = forSheet ? equipment.mEquipped : true;
		mQuantity = forSheet ? equipment.mQuantity : 1;
		mDescription = equipment.mDescription;
		mTechLevel = equipment.mTechLevel;
		mLegalityClass = equipment.mLegalityClass;
		mValue = equipment.mValue;
		mWeight = equipment.mWeight;
		mExtendedValue = mQuantity * mValue;
		mExtendedWeight = mQuantity * mWeight;
		mReference = equipment.mReference;
		mWeapons = new ArrayList<WeaponStats>(equipment.mWeapons.size());
		for (WeaponStats weapon : equipment.mWeapons) {
			if (weapon instanceof MeleeWeaponStats) {
				mWeapons.add(new MeleeWeaponStats(this, (MeleeWeaponStats) weapon));
			} else if (weapon instanceof RangedWeaponStats) {
				mWeapons.add(new RangedWeaponStats(this, (RangedWeaponStats) weapon));
			}
		}
		if (deep) {
			int count = equipment.getChildCount();

			for (int i = 0; i < count; i++) {
				addChild(new Equipment(dataFile, (Equipment) equipment.getChild(i), true));
			}
		}
	}

	/**
	 * Loads an equipment and associates it with the specified data file.
	 * 
	 * @param dataFile The data file to associate it with.
	 * @param reader The XML reader to load from.
	 * @throws IOException
	 */
	public Equipment(DataFile dataFile, XMLReader reader) throws IOException {
		this(dataFile, TAG_EQUIPMENT_CONTAINER.equals(reader.getName()));
		load(reader, false);
	}

	@Override public String getLocalizedName() {
		return MSG_DEFAULT_NAME;
	}

	@Override public String getListChangedID() {
		return ID_LIST_CHANGED;
	}

	@Override public String getXMLTagName() {
		return canHaveChildren() ? TAG_EQUIPMENT_CONTAINER : TAG_EQUIPMENT;
	}

	@Override public String getRowType() {
		return "Equipment"; //$NON-NLS-1$
	}

	@Override protected void prepareForLoad(boolean forUndo) {
		super.prepareForLoad(forUndo);
		mEquipped = true;
		mQuantity = 1;
		mDescription = MSG_DEFAULT_NAME;
		mTechLevel = EMPTY;
		mLegalityClass = DEFAULT_LEGALITY_CLASS;
		mReference = EMPTY;
		mValue = 0.0;
		mWeight = 0.0;
		mWeapons = new ArrayList<WeaponStats>();
	}

	@Override protected void loadAttributes(XMLReader reader, boolean forUndo) {
		super.loadAttributes(reader, forUndo);
		if (mDataFile instanceof GURPSCharacter) {
			setEquipped(reader.isAttributeSet(ATTRIBUTE_EQUIPPED));
		}
	}

	@Override protected void loadSubElement(XMLReader reader, boolean forUndo) throws IOException {
		String name = reader.getName();

		if (TAG_DESCRIPTION.equals(name)) {
			mDescription = reader.readText().replace(NEWLINE, SPACE);
		} else if (TAG_TECH_LEVEL.equals(name)) {
			mTechLevel = reader.readText().replace(NEWLINE, SPACE);
		} else if (TAG_LEGALITY_CLASS.equals(name)) {
			mLegalityClass = reader.readText().replace(NEWLINE, SPACE);
		} else if (TAG_VALUE.equals(name)) {
			mValue = reader.readDouble(0.0);
		} else if (TAG_WEIGHT.equals(name)) {
			mWeight = WeightUnits.POUNDS.convert((WeightUnits) EnumExtractor.extract(reader.getAttribute(ATTRIBUTE_UNITS), WeightUnits.values(), WeightUnits.POUNDS), reader.readDouble(0));
		} else if (TAG_REFERENCE.equals(name)) {
			mReference = reader.readText().replace(NEWLINE, SPACE);
		} else if (!forUndo && (TAG_EQUIPMENT.equals(name) || TAG_EQUIPMENT_CONTAINER.equals(name))) {
			addChild(new Equipment(mDataFile, reader));
		} else if (MeleeWeaponStats.TAG_ROOT.equals(name)) {
			mWeapons.add(new MeleeWeaponStats(this, reader));
		} else if (RangedWeaponStats.TAG_ROOT.equals(name)) {
			mWeapons.add(new RangedWeaponStats(this, reader));
		} else if (OldWeapon.TAG_ROOT.equals(name)) {
			mOldWeapon = new OldWeapon(reader);
		} else if (!canHaveChildren()) {
			if (TAG_QUANTITY.equals(name)) {
				mQuantity = reader.readInteger(1);
			} else {
				super.loadSubElement(reader, forUndo);
			}
		} else {
			super.loadSubElement(reader, forUndo);
		}
	}

	@Override protected void finishedLoading() {
		if (mOldWeapon != null) {
			mWeapons.addAll(mOldWeapon.getWeapons(this));
			mOldWeapon = null;
		}
		// We no longer have defaults... that was solely for the weapons
		setDefaults(new ArrayList<SkillDefault>());
		updateExtendedValue(false);
		updateExtendedWeight(false);
	}

	@Override protected void saveAttributes(XMLWriter out, boolean forUndo) {
		if (mDataFile instanceof GURPSCharacter) {
			out.writeAttribute(ATTRIBUTE_EQUIPPED, mEquipped);
		}
	}

	@Override protected void saveSelf(XMLWriter out, boolean forUndo) {
		if (!canHaveChildren()) {
			out.simpleTag(TAG_QUANTITY, mQuantity);
		}
		out.simpleTagNotEmpty(TAG_DESCRIPTION, mDescription);
		out.simpleTagNotEmpty(TAG_TECH_LEVEL, mTechLevel);
		out.simpleTagNotEmpty(TAG_LEGALITY_CLASS, mLegalityClass);
		out.simpleTag(TAG_VALUE, mValue);
		out.simpleTagWithAttribute(TAG_WEIGHT, Double.toString(mWeight), ATTRIBUTE_UNITS, WeightUnits.POUNDS.toString());
		out.simpleTagNotEmpty(TAG_REFERENCE, mReference);
		for (WeaponStats weapon : mWeapons) {
			weapon.save(out);
		}
	}

	@Override public void update() {
		updateExtendedValue(true);
		updateExtendedWeight(true);
	}

	/** @return The quantity. */
	public int getQuantity() {
		return mQuantity;
	}

	/**
	 * @param quantity The quantity to set.
	 * @return Whether it was modified.
	 */
	public boolean setQuantity(int quantity) {
		if (quantity != mQuantity) {
			mQuantity = quantity;
			startNotify();
			notify(ID_QUANTITY, this);
			updateContainingWeights(true);
			updateContainingValues(true);
			endNotify();
			return true;
		}
		return false;
	}

	/** @return The description. */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * @param description The description to set.
	 * @return Whether it was modified.
	 */
	public boolean setDescription(String description) {
		if (!mDescription.equals(description)) {
			mDescription = description;
			notifySingle(ID_DESCRIPTION);
			return true;
		}
		return false;
	}

	/** @return The tech level. */
	public String getTechLevel() {
		return mTechLevel;
	}

	/**
	 * @param techLevel The tech level to set.
	 * @return Whether it was modified.
	 */
	public boolean setTechLevel(String techLevel) {
		if (!mTechLevel.equals(techLevel)) {
			mTechLevel = techLevel;
			notifySingle(ID_TECH_LEVEL);
			return true;
		}
		return false;
	}

	/** @return The legality class. */
	public String getLegalityClass() {
		return mLegalityClass;
	}

	/**
	 * @param legalityClass The legality class to set.
	 * @return Whether it was modified.
	 */
	public boolean setLegalityClass(String legalityClass) {
		if (!mLegalityClass.equals(legalityClass)) {
			mLegalityClass = legalityClass;
			notifySingle(ID_LEGALITY_CLASS);
			return true;
		}
		return false;
	}

	/** @return The value. */
	public double getValue() {
		return mValue;
	}

	/**
	 * @param value The value to set.
	 * @return Whether it was modified.
	 */
	public boolean setValue(double value) {
		if (value != mValue) {
			mValue = value;
			startNotify();
			notify(ID_VALUE, this);
			updateContainingValues(true);
			endNotify();
			return true;
		}
		return false;
	}

	/** @return The extended value. */
	public double getExtendedValue() {
		return mExtendedValue;
	}

	/** @return The weight. */
	public double getWeight() {
		return mWeight;
	}

	/**
	 * @param weight The weight to set.
	 * @return Whether it was modified.
	 */
	public boolean setWeight(double weight) {
		if (weight != mWeight) {
			mWeight = weight;
			startNotify();
			notify(ID_WEIGHT, this);
			updateContainingWeights(true);
			endNotify();
			return true;
		}
		return false;
	}

	private boolean updateExtendedWeight(boolean okToNotify) {
		double savedWeight = mExtendedWeight;
		int count = getChildCount();

		mExtendedWeight = mQuantity * mWeight;
		for (int i = 0; i < count; i++) {
			Equipment child = (Equipment) getChild(i);

			mExtendedWeight += child.mExtendedWeight;
		}
		if (savedWeight != mExtendedWeight) {
			if (okToNotify) {
				notify(ID_EXTENDED_WEIGHT, this);
			}
			return true;
		}
		return false;
	}

	private void updateContainingWeights(boolean okToNotify) {
		Row parent = this;

		while (parent != null && parent instanceof Equipment) {
			Equipment parentRow = (Equipment) parent;

			if (parentRow.updateExtendedWeight(okToNotify)) {
				parent = parentRow.getParent();
			} else {
				break;
			}
		}
	}

	private boolean updateExtendedValue(boolean okToNotify) {
		double savedValue = mExtendedValue;
		int count = getChildCount();

		mExtendedValue = mQuantity * mValue;
		for (int i = 0; i < count; i++) {
			Equipment child = (Equipment) getChild(i);

			mExtendedValue += child.mExtendedValue;
		}
		if (savedValue != mExtendedValue) {
			if (okToNotify) {
				notify(ID_EXTENDED_VALUE, this);
			}
			return true;
		}
		return false;
	}

	private void updateContainingValues(boolean okToNotify) {
		Row parent = this;

		while (parent != null && parent instanceof Equipment) {
			Equipment parentRow = (Equipment) parent;

			if (parentRow.updateExtendedValue(okToNotify)) {
				parent = parentRow.getParent();
			} else {
				break;
			}
		}
	}

	/** @return The extended weight. */
	public double getExtendedWeight() {
		return mExtendedWeight;
	}

	/** @return Whether this item is equipped. */
	public boolean isEquipped() {
		return mEquipped;
	}

	/** @return Whether this item and all of its parents are equipped. */
	public boolean isFullyEquipped() {
		Equipment equipment = this;

		while (equipment != null) {
			if (!equipment.isEquipped()) {
				return false;
			}
			equipment = (Equipment) equipment.getParent();
		}
		return true;
	}

	/**
	 * @param equipped Whether this item is equipped.
	 * @return Whether it was changed.
	 */
	public boolean setEquipped(boolean equipped) {
		if (mEquipped != equipped) {
			mEquipped = equipped;
			notifySingle(ID_EQUIPPED);
			return true;
		}
		return false;
	}

	/** @return The page reference. */
	public String getReference() {
		return mReference;
	}

	/**
	 * @param reference The page reference to set.
	 * @return Whether it was modified.
	 */
	public boolean setReference(String reference) {
		if (!mReference.equals(reference)) {
			mReference = reference;
			notifySingle(ID_REFERENCE);
			return true;
		}
		return false;
	}

	@Override public boolean contains(String text, boolean lowerCaseOnly) {
		return getDescription().toLowerCase().indexOf(text) != -1;
	}

	@Override public Object getData(Column column) {
		return EquipmentColumn.values()[column.getID()].getData(this);
	}

	@Override public String getDataAsText(Column column) {
		return EquipmentColumn.values()[column.getID()].getDataAsText(this);
	}

	@Override public String toString() {
		return getDescription();
	}

	/** @return The weapon list. */
	public List<WeaponStats> getWeapons() {
		return Collections.unmodifiableList(mWeapons);
	}

	/**
	 * @param weapons The weapons to set.
	 * @return Whether it was modified.
	 */
	public boolean setWeapons(List<WeaponStats> weapons) {
		if (!mWeapons.equals(weapons)) {
			mWeapons = new ArrayList<WeaponStats>(weapons);
			for (WeaponStats weapon : mWeapons) {
				weapon.setOwner(this);
			}
			notifySingle(ID_WEAPON_STATUS_CHANGED);
			return true;
		}
		return false;
	}

	@Override public BufferedImage getImage(boolean large) {
		return Images.getEquipmentIcon(large, true);
	}

	@Override public RowEditor<? extends ListRow> createEditor() {
		return new EquipmentEditor(this);
	}

	@Override public void fillWithNameableKeys(HashSet<String> set) {
		super.fillWithNameableKeys(set);
		extractNameables(set, mDescription);
		for (WeaponStats weapon : mWeapons) {
			for (SkillDefault one : weapon.getDefaults()) {
				one.fillWithNameableKeys(set);
			}
		}
	}

	@Override public void applyNameableKeys(HashMap<String, String> map) {
		super.applyNameableKeys(map);
		mDescription = nameNameables(map, mDescription);
		for (WeaponStats weapon : mWeapons) {
			for (SkillDefault one : weapon.getDefaults()) {
				one.applyNameableKeys(map);
			}
		}
	}
}