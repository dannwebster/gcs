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

package com.trollworks.gcs.prereq;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.criteria.IntegerCriteria;
import com.trollworks.gcs.criteria.StringCompareType;
import com.trollworks.gcs.criteria.StringCriteria;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.utility.io.xml.XMLReader;
import com.trollworks.gcs.utility.io.xml.XMLWriter;
import com.trollworks.gcs.widgets.outline.ListRow;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;

/** A Skill prerequisite. */
public class SkillPrereq extends NameLevelPrereq {
	private static String		MSG_SKILL_NAME_PART;
	private static String		MSG_SPECIALIZATION_PART;
	private static String		MSG_LEVEL_AND_TL_PART;
	/** The XML tag for this class. */
	public static final String	TAG_ROOT			= "skill_prereq";	//$NON-NLS-1$
	private static final String	TAG_SPECIALIZATION	= "specialization"; //$NON-NLS-1$
	private StringCriteria	mSpecializationCriteria;

	/**
	 * Creates a new prerequisite.
	 * 
	 * @param parent The owning prerequisite list, if any.
	 */
	public SkillPrereq(PrereqList parent) {
		super(TAG_ROOT, parent);
		mSpecializationCriteria = new StringCriteria(StringCompareType.IS_ANYTHING, ""); //$NON-NLS-1$
	}

	/**
	 * Loads a prerequisite.
	 * 
	 * @param parent The owning prerequisite list, if any.
	 * @param reader The XML reader to load from.
	 * @throws IOException
	 */
	public SkillPrereq(PrereqList parent, XMLReader reader) throws IOException {
		super(parent, reader);
	}

	private SkillPrereq(PrereqList parent, SkillPrereq prereq) {
		super(parent, prereq);
		mSpecializationCriteria = new StringCriteria(prereq.mSpecializationCriteria);
	}

	@Override protected void initializeForLoad() {
		mSpecializationCriteria = new StringCriteria(StringCompareType.IS_ANYTHING, ""); //$NON-NLS-1$
	}

	@Override public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SkillPrereq) {
			SkillPrereq other = (SkillPrereq) obj;

			return super.equals(other) && mSpecializationCriteria.equals(other.mSpecializationCriteria);
		}
		return false;
	}

	@Override protected void loadSelf(XMLReader reader) throws IOException {
		if (TAG_SPECIALIZATION.equals(reader.getName())) {
			mSpecializationCriteria.load(reader);
		} else {
			super.loadSelf(reader);
		}
	}

	@Override protected void saveSelf(XMLWriter out) {
		mSpecializationCriteria.save(out, TAG_SPECIALIZATION);
	}

	@Override public String getXMLTag() {
		return TAG_ROOT;
	}

	@Override public Prereq clone(PrereqList parent) {
		return new SkillPrereq(parent, this);
	}

	@Override public boolean satisfied(GURPSCharacter character, ListRow exclude, StringBuilder builder, String prefix) {
		boolean satisfied = false;
		String techLevel = null;
		StringCriteria nameCriteria = getNameCriteria();
		IntegerCriteria levelCriteria = getLevelCriteria();

		if (exclude instanceof Skill) {
			techLevel = ((Skill) exclude).getTechLevel();
		}

		for (Skill skill : character.getSkillsIterator()) {
			if (exclude != skill && nameCriteria.matches(skill.getName()) && mSpecializationCriteria.matches(skill.getSpecialization())) {
				satisfied = levelCriteria.matches(skill.getLevel());
				if (satisfied && techLevel != null) {
					String otherTL = skill.getTechLevel();

					satisfied = otherTL == null || techLevel.equals(otherTL);
				}
				break;
			}
		}
		if (!has()) {
			satisfied = !satisfied;
		}
		if (!satisfied && builder != null) {
			builder.append(MessageFormat.format(MSG_SKILL_NAME_PART, prefix, has() ? MSG_HAS : MSG_DOES_NOT_HAVE, nameCriteria.toString()));
			boolean notAnySpecialization = mSpecializationCriteria.getType() != StringCompareType.IS_ANYTHING;

			if (notAnySpecialization) {
				builder.append(MessageFormat.format(MSG_SPECIALIZATION_PART, mSpecializationCriteria.toString()));
			}
			if (techLevel == null) {
				builder.append(MessageFormat.format(MSG_LEVEL_PART, levelCriteria.toString()));
			} else {
				if (notAnySpecialization) {
					builder.append(","); //$NON-NLS-1$
				}
				builder.append(MessageFormat.format(MSG_LEVEL_AND_TL_PART, levelCriteria.toString()));
			}
		}
		return satisfied;
	}

	@Override public void fillWithNameableKeys(HashSet<String> set) {
		super.fillWithNameableKeys(set);
		ListRow.extractNameables(set, mSpecializationCriteria.getQualifier());
	}

	@Override public void applyNameableKeys(HashMap<String, String> map) {
		super.applyNameableKeys(map);
		mSpecializationCriteria.setQualifier(ListRow.nameNameables(map, mSpecializationCriteria.getQualifier()));
	}

	/** @return The specialization comparison object. */
	public StringCriteria getSpecializationCriteria() {
		return mSpecializationCriteria;
	}
}