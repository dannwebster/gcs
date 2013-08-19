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

package com.trollworks.gcs.utility;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/** The standard {@link UndoManager} for use with our app's windows. */
public class StdUndoManager extends UndoManager {
	private boolean	mInTransaction;

	@Override public synchronized void undo() throws CannotUndoException {
		mInTransaction = true;
		super.undo();
		mInTransaction = false;
	}

	@Override public synchronized void redo() throws CannotRedoException {
		mInTransaction = true;
		super.redo();
		mInTransaction = false;
	}

	/** @return Whether this {@link UndoManager} is currently processing an undo or redo. */
	public boolean isInTransaction() {
		return mInTransaction;
	}
}