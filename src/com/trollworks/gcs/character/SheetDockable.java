/*
 * Copyright (c) 1998-2014 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.character;

import com.trollworks.gcs.app.CommonDockable;
import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.menu.file.ExportToCommand;
import com.trollworks.toolkit.ui.menu.file.PrintProxy;
import com.trollworks.toolkit.ui.widget.WindowUtils;
import com.trollworks.toolkit.utility.Localization;
import com.trollworks.toolkit.utility.PathUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JScrollPane;

/** A list of advantages and disadvantages from a library. */
public class SheetDockable extends CommonDockable {
	@Localize("An error occurred while trying to save the sheet as a PNG.")
	private static String		SAVE_AS_PNG_ERROR;
	@Localize("An error occurred while trying to save the sheet as a PDF.")
	private static String		SAVE_AS_PDF_ERROR;
	@Localize("An error occurred while trying to save the sheet as HTML.")
	private static String		SAVE_AS_HTML_ERROR;

	static {
		Localization.initialize();
	}

	private CharacterSheet		mSheet;
	private PrerequisitesThread	mPrereqThread;

	/** Creates a new {@link SheetDockable}. */
	public SheetDockable(GURPSCharacter character) {
		super(character);
		GURPSCharacter dataFile = getDataFile();
		mSheet = new CharacterSheet(dataFile);
		JScrollPane scroller = new JScrollPane(mSheet);
		scroller.setBorder(null);
		scroller.getViewport().setBackground(Color.LIGHT_GRAY);
		mSheet.rebuild();
		scroller.getViewport().addChangeListener(mSheet);
		add(scroller, BorderLayout.CENTER);
		mPrereqThread = new PrerequisitesThread(mSheet);
		mPrereqThread.start();
		PrerequisitesThread.waitForProcessingToFinish(dataFile);
		getUndoManager().discardAllEdits();
		dataFile.setModified(false);
	}

	@Override
	public GURPSCharacter getDataFile() {
		return (GURPSCharacter) super.getDataFile();
	}

	/** @return The {@link CharacterSheet}. */
	public CharacterSheet getSheet() {
		return mSheet;
	}

	@Override
	public PrintProxy getPrintProxy() {
		return mSheet;
	}

	@Override
	public String getDescriptor() {
		// RAW: Implement
		return null;
	}

	@Override
	public String[] getAllowedExtensions() {
		return new String[] { GURPSCharacter.EXTENSION, ExportToCommand.PDF_EXTENSION, ExportToCommand.HTML_EXTENSION, ExportToCommand.PNG_EXTENSION };
	}

	@Override
	public String getPreferredSavePath() {
		String name = getDataFile().getDescription().getName();
		if (name.length() == 0) {
			name = getTitle();
		}
		return PathUtils.getFullPath(PathUtils.getParent(PathUtils.getFullPath(getCurrentBackingFile())), name);
	}

	@Override
	public File[] saveTo(File file) {
		ArrayList<File> result = new ArrayList<>();
		String extension = PathUtils.getExtension(file.getName());
		if (ExportToCommand.HTML_EXTENSION.equals(extension)) {
			if (mSheet.saveAsHTML(file, null, null)) {
				result.add(file);
			} else {
				WindowUtils.showError(this, SAVE_AS_HTML_ERROR);
			}
		} else if (ExportToCommand.PNG_EXTENSION.equals(extension)) {
			if (!mSheet.saveAsPNG(file, result)) {
				WindowUtils.showError(this, SAVE_AS_PNG_ERROR);
			}
		} else if (ExportToCommand.PDF_EXTENSION.equals(extension)) {
			if (mSheet.saveAsPDF(file)) {
				result.add(file);
			} else {
				WindowUtils.showError(this, SAVE_AS_PDF_ERROR);
			}
		} else {
			return super.saveTo(file);
		}
		return result.toArray(new File[result.size()]);
	}
}
