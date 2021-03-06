/*
 * Copyright (c) 1998-2016 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * version 2.0. If a copy of the MPL was not distributed with this file, You
 * can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined
 * by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.character;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.ui.GraphicsUtilities;
import com.trollworks.toolkit.ui.WindowSizeEnforcer;
import com.trollworks.toolkit.utility.Localization;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/** Provides simplistic text editing. */
public class TextEditor extends JDialog implements ActionListener, WindowFocusListener {
	@Localize("Cancel")
	@Localize(locale = "de", value = "Abbrechen")
	@Localize(locale = "ru", value = "Отмена")
	@Localize(locale = "es", value = "Cancelar")
	private static String	CANCEL;
	@Localize("Set")
	@Localize(locale = "de", value = "Ok")
	@Localize(locale = "ru", value = "Принять")
	@Localize(locale = "es", value = "Aceptar")
	private static String	SET;

	static {
		Localization.initialize();
	}

	private JTextArea	mEditor;
	private JButton		mSetButton;
	private boolean		mSet;

	/**
	 * Puts up a modal text editor.
	 *
	 * @param centeredOn The component to center the dialog on.
	 * @param title The title for the dialog.
	 * @param text The text to edit.
	 * @return The new text, or <code>null</code> if changes were canceled.
	 */
	public static String edit(Component centeredOn, String title, String text) {
		TextEditor editor = new TextEditor(centeredOn, title, text);
		editor.setVisible(true);
		return editor.mSet ? editor.mEditor.getText() : null;
	}

	private TextEditor(Component centeredOn, String title, String text) {
		super(JOptionPane.getRootFrame(), title, true);
		setResizable(true);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(content, BorderLayout.CENTER);

		mEditor = new JTextArea(text);
		mEditor.setLineWrap(true);
		mEditor.setWrapStyleWord(true);
		JScrollPane scroller = new JScrollPane(mEditor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setMinimumSize(new Dimension(400, 300));
		content.add(scroller, BorderLayout.CENTER);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		createButton(panel, CANCEL);
		mSetButton = createButton(panel, SET);
		content.add(panel, BorderLayout.SOUTH);

		WindowSizeEnforcer.monitor(this);
		addWindowFocusListener(this);
		GraphicsUtilities.packAndCenterWindowOn(this, centeredOn);
	}

	private JButton createButton(JPanel parent, String title) {
		JButton button = new JButton(title);
		button.addActionListener(this);
		parent.add(button);
		return button;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		mSet = event.getSource() == mSetButton;
		dispose();
	}

	@Override
	public void windowGainedFocus(WindowEvent event) {
		mEditor.requestFocus();
		removeWindowFocusListener(this);
	}

	@Override
	public void windowLostFocus(WindowEvent event) {
		// Not used.
	}
}
