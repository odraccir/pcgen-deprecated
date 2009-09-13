/*
 * CharacterStatsPanel.java
 * Copyright 2008 (C) James Dempsey
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 26/10/2008 14:38:01
 *
 * $Id: $
 */
package pcgen.gui.prefs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import pcgen.cdom.base.Constants;
import pcgen.core.GameMode;
import pcgen.core.PointBuyMethod;
import pcgen.core.SettingsHandler;
import pcgen.core.system.GameModeRollMethod;
import pcgen.gui.CharacterInfo;
import pcgen.gui.PCGen_Frame1;
import pcgen.gui.utils.JComboBoxEx;
import pcgen.gui.utils.Utility;
import pcgen.util.PropertyFactory;


/**
 * The Class <code>CharacterStatsPanel</code> is responsible for managing 
 * the character stats preferences.
 * 
 * Last Editor: $Author: $
 * Last Edited: $Date:  $
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision:  $
 */
@SuppressWarnings("serial")
public class CharacterStatsPanel extends PCGenPrefsPanel
{
	
	private static String in_abilities =
		PropertyFactory.getString("in_Prefs_abilities");
	private String[] pMode;
	private String[] pModeMethodName;
	private JDialog parent;

	private JRadioButton abilitiesAllSameButton;
	private JRadioButton abilitiesPurchasedButton;
	private JRadioButton abilitiesRolledButton;
	private JRadioButton abilitiesUserRolledButton;
	private JComboBoxEx abilityPurchaseModeCombo;
	private JComboBoxEx abilityRolledModeCombo = null;
	private JComboBoxEx abilityScoreCombo;
	private JButton purchaseModeButton;
	private PurchaseModeFrame pmsFrame = null;

	private ActionListener rolledModeListener;
	private ActionListener purchaseModeListener;
	private ActionListener scoreListener;

	/**
	 * Instantiates a new character stats panel.
	 * 
	 * @param parent the parent dialog
	 */
	public CharacterStatsPanel(JDialog parent)
	{
		this.parent = parent;
		
		initComponents();

		addAbilitiesPanelListeners();
	}

	/**
	 * Build and initialise the user interface.
	 */
	private void initComponents()
	{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		JLabel label;
		ButtonGroup exclusiveGroup;
		Border etched = null;
		TitledBorder title1 =
				BorderFactory.createTitledBorder(etched, in_abilities);

		title1.setTitleJustification(TitledBorder.LEFT);
		this.setBorder(title1);
		this.setLayout(gridbag);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(2, 2, 2, 2);

		final GameMode gameMode = SettingsHandler.getGame();

		int row = 0;

		exclusiveGroup = new ButtonGroup();
		Utility.buildConstraints(c, 0, row++, 3, 1, 0, 0);
		label =
				new JLabel(PropertyFactory
					.getString("in_Prefs_abilitiesGenLabel")
					+ ": (" + gameMode.getDisplayName() + ")");
		gridbag.setConstraints(label, c);
		this.add(label);
		Utility.buildConstraints(c, 0, row, 1, 1, 0, 0);
		label = new JLabel("  ");
		gridbag.setConstraints(label, c);
		this.add(label);

		Utility.buildConstraints(c, 1, row++, 2, 1, 0, 0);
		abilitiesUserRolledButton =
				new JRadioButton(PropertyFactory
					.getString("in_Prefs_abilitiesUserRolled"));
		gridbag.setConstraints(abilitiesUserRolledButton, c);
		this.add(abilitiesUserRolledButton);
		exclusiveGroup.add(abilitiesUserRolledButton);

		Utility.buildConstraints(c, 1, row++, 2, 1, 0, 0);
		abilitiesAllSameButton =
				new JRadioButton(PropertyFactory
					.getString("in_Prefs_abilitiesAllSame")
					+ ": ");
		gridbag.setConstraints(abilitiesAllSameButton, c);
		this.add(abilitiesAllSameButton);
		exclusiveGroup.add(abilitiesAllSameButton);
		Utility.buildConstraints(c, 1, row, 1, 1, 0, 0);
		label = new JLabel("  ");
		gridbag.setConstraints(label, c);
		this.add(label);
		Utility.buildConstraints(c, 2, row++, 2, 1, 0, 0);

		abilityScoreCombo = new JComboBoxEx();
		for (int i = gameMode.getStatMin(); i <= gameMode.getStatMax(); ++i)
		{
			abilityScoreCombo.addItem(String.valueOf(i));
		}

		gridbag.setConstraints(abilityScoreCombo, c);
		this.add(abilityScoreCombo);

		GameModeRollMethod rm = gameMode.getRollingMethod(0);
		if (rm != null)
		{
			Utility.buildConstraints(c, 1, row++, 2, 1, 0, 0);
			abilitiesRolledButton = new JRadioButton("Rolled:");
			gridbag.setConstraints(abilitiesRolledButton, c);
			this.add(abilitiesRolledButton);
			exclusiveGroup.add(abilitiesRolledButton);
			Utility.buildConstraints(c, 2, row++, 2, 1, 0, 0);

			abilityRolledModeCombo = new JComboBoxEx();

			int gmi = 0;
			while (rm != null)
			{
				abilityRolledModeCombo.addItem(rm.getMethodName());
				rm = gameMode.getRollingMethod(++gmi);
			}

			gridbag.setConstraints(abilityRolledModeCombo, c);
			this.add(abilityRolledModeCombo);
		}

		final int purchaseMethodCount = gameMode.getPurchaseMethodCount();
		Utility.buildConstraints(c, 1, row++, 2, 1, 0, 0);
		abilitiesPurchasedButton =
				new JRadioButton(PropertyFactory
					.getString("in_Prefs_abilitiesPurchased")
					+ ": ");
		gridbag.setConstraints(abilitiesPurchasedButton, c);
		this.add(abilitiesPurchasedButton);
		exclusiveGroup.add(abilitiesPurchasedButton);
		Utility.buildConstraints(c, 2, row++, 2, 1, 0, 0);

		pMode = new String[purchaseMethodCount];
		pModeMethodName = new String[purchaseMethodCount];

		for (int i = 0; i < purchaseMethodCount; ++i)
		{
			final PointBuyMethod pbm = gameMode.getPurchaseMethod(i);
			pMode[i] = pbm.getDescription();
			pModeMethodName[i] = pbm.getMethodName();
		}

		abilityPurchaseModeCombo = new JComboBoxEx(pMode);

		gridbag.setConstraints(abilityPurchaseModeCombo, c);
		this.add(abilityPurchaseModeCombo);

		//
		// Hide controls if there are no entries to select
		//
		if (purchaseMethodCount == 0)
		{
			abilityPurchaseModeCombo.setVisible(false);
			abilitiesPurchasedButton.setVisible(false);
		}

		Utility.buildConstraints(c, 1, row++, 1, 1, 0, 0);
		label = new JLabel(" ");
		gridbag.setConstraints(label, c);
		this.add(label);
		Utility.buildConstraints(c, 1, row++, 3, 1, 0, 0);
		purchaseModeButton =
				new JButton(PropertyFactory
					.getString("in_Prefs_purchaseModeConfig"));
		gridbag.setConstraints(purchaseModeButton, c);
		this.add(purchaseModeButton);
		purchaseModeButton.addActionListener(new PurcahseModeButtonListener());

		Utility.buildConstraints(c, 5, 20, 1, 1, 1, 1);
		c.fill = GridBagConstraints.BOTH;
		label = new JLabel(" ");
		gridbag.setConstraints(label, c);
		this.add(label);
	}

	/* (non-Javadoc)
	 * @see pcgen.gui.prefs.PCGenPrefsPanel#applyOptionValuesToControls()
	 */
	@Override
	public void applyOptionValuesToControls()
	{
		stopListeners();
		
		final GameMode gameMode = SettingsHandler.getGame();
		boolean bValid = true;
		final int rollMethod = gameMode.getRollMethod();

		switch (rollMethod)
		{
			case Constants.CHARACTERSTATMETHOD_USER:
				abilitiesUserRolledButton.setSelected(true);

				break;

			case Constants.CHARACTERSTATMETHOD_ALLSAME:
				abilitiesAllSameButton.setSelected(true);

				break;

			case Constants.CHARACTERSTATMETHOD_PURCHASE:
				if (!abilitiesPurchasedButton.isVisible()
					|| (pMode.length == 0))
				{
					bValid = false;
				}
				else
				{
					abilitiesPurchasedButton.setSelected(true);
				}

				break;

			case Constants.CHARACTERSTATMETHOD_ROLLED:
				if (abilitiesRolledButton == null)
				{
					bValid = false;
				}
				else
				{
					abilitiesRolledButton.setSelected(true);
					abilityRolledModeCombo.setSelectedItem(gameMode
						.getRollMethodExpressionName());
				}

				break;

			default:
				bValid = false;

				break;
		}

		if (!bValid)
		{
			abilitiesUserRolledButton.setSelected(true);
			gameMode.setRollMethod(Constants.CHARACTERSTATMETHOD_USER);
		}

		final int allStatsValue =
				Math.min(gameMode.getStatMax(), gameMode.getAllStatsValue());
		gameMode.setAllStatsValue(allStatsValue);
		abilityScoreCombo.setSelectedIndex(allStatsValue
			- gameMode.getStatMin());

		if ((pMode != null) && (pModeMethodName != null))
		{
			final String methodName = gameMode.getPurchaseModeMethodName();

			for (int i = 0; i < pMode.length; ++i)
			{
				if (pModeMethodName[i].equals(methodName))
				{
					abilityPurchaseModeCombo.setSelectedIndex(i);
				}
			}
		}
		
		startListeners();
	}

	/**
	 * Create and display purchase mode stats popup frame.
	 */
	private void showPurchaseModeConfiguration()
	{
		if (pmsFrame == null)
		{
			pmsFrame = new PurchaseModeFrame(parent);

			// add a listener to know when the window has closed
			pmsFrame.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					final int purchaseMethodCount =
							SettingsHandler.getGame().getPurchaseMethodCount();
					pMode = new String[purchaseMethodCount];
					pModeMethodName = new String[purchaseMethodCount];

					final String methodName =
							SettingsHandler.getGame()
								.getPurchaseModeMethodName();
					abilityPurchaseModeCombo.removeAllItems();

					for (int i = 0; i < purchaseMethodCount; ++i)
					{
						final PointBuyMethod pbm =
								SettingsHandler.getGame().getPurchaseMethod(i);
						pMode[i] = pbm.getDescription();
						pModeMethodName[i] = pbm.getMethodName();
						abilityPurchaseModeCombo.addItem(pMode[i]);

						if (pModeMethodName[i].equals(methodName))
						{
							abilityPurchaseModeCombo.setSelectedIndex(i);
						}
					}

					// free resources
					pmsFrame = null;

					//
					// If user has added at least one method, then make the controls visible. Otherwise
					// it is not a valid choice and cannot be selected, so hide it.
					//
					abilityPurchaseModeCombo
						.setVisible(purchaseMethodCount != 0);
					abilitiesPurchasedButton
						.setVisible(purchaseMethodCount != 0);

					//
					// If no longer visible, but was selected, then use 'user rolled' instead
					//
					if (!abilitiesPurchasedButton.isVisible()
						&& abilitiesPurchasedButton.isSelected())
					{
						abilitiesUserRolledButton.setSelected(true);
					}

				}
			});
		}

		Utility.centerDialog(pmsFrame);

		// ensure the frame is visible (in case user selects menu item again).
		pmsFrame.setVisible(true);
	}

	/**
	 * Create and add the listeners for the panel.
	 */
	private void addAbilitiesPanelListeners()
	{
		scoreListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				abilitiesAllSameButton.setSelected(true);
			}
		};

		purchaseModeListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				abilitiesPurchasedButton.setSelected(true);
			}
		};

		rolledModeListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				abilitiesRolledButton.setSelected(true);
			}
		};

		startListeners();
	}

	/**
	 * Start the listeners that track changing data. These have to
	 * be stopped when updating data programatically to avoid
	 * spurious setting of dirty flags etc.
	 */
	private void startListeners()
	{
		abilityScoreCombo.addActionListener(scoreListener);
		abilityPurchaseModeCombo.addActionListener(purchaseModeListener);
		abilityRolledModeCombo.addActionListener(rolledModeListener);
	}

	/**
	 * Stop the listeners that track changing data. These have to
	 * be stopped when updating data programatically to avoid
	 * spurious setting of dirty flags etc.
	 */
	private void stopListeners()
	{
		abilityScoreCombo.removeActionListener(scoreListener);
		abilityPurchaseModeCombo.removeActionListener(purchaseModeListener);
		abilityRolledModeCombo.removeActionListener(rolledModeListener);
	}

	/* (non-Javadoc)
	 * @see pcgen.gui.prefs.PCGenPrefsPanel#getTitle()
	 */
	@Override
	public String getTitle()
	{
		return in_abilities;
	}

	/* (non-Javadoc)
	 * @see pcgen.gui.prefs.PCGenPrefsPanel#setOptionsBasedOnControls()
	 */
	@Override
	public void setOptionsBasedOnControls()
	{
		final GameMode gameMode = SettingsHandler.getGame();
		gameMode.setAllStatsValue(abilityScoreCombo.getSelectedIndex()
			+ gameMode.getStatMin());

		if (abilitiesUserRolledButton.isSelected())
		{
			gameMode.setRollMethod(Constants.CHARACTERSTATMETHOD_USER);
		}
		else if (abilitiesAllSameButton.isSelected())
		{
			gameMode.setRollMethod(Constants.CHARACTERSTATMETHOD_ALLSAME);
		}
		else if (abilitiesPurchasedButton.isSelected())
		{
			if (abilityPurchaseModeCombo.isVisible()
				&& (abilityPurchaseModeCombo.getSelectedIndex() >= 0))
			{
				gameMode
					.setPurchaseMethodName(pModeMethodName[abilityPurchaseModeCombo
						.getSelectedIndex()]);
			}
			else
			{
				gameMode.setRollMethod(Constants.CHARACTERSTATMETHOD_USER);
			}
		}
		else if ((abilitiesRolledButton != null)
			&& (abilitiesRolledButton.isSelected()))
		{
			if (abilityRolledModeCombo.getSelectedIndex() >= 0)
			{
				gameMode.setRollMethodExpressionByName(abilityRolledModeCombo
					.getSelectedItem().toString());
			}
			else
			{
				gameMode.setRollMethod(Constants.CHARACTERSTATMETHOD_USER);
			}
		}

		//
		// Update summary tab in case we have changed from/to rolled stats method
		//
		final CharacterInfo characterPane = PCGen_Frame1.getCharacterPane();
		if (characterPane != null)
		{
			characterPane.setPaneForUpdate(characterPane.infoSummary());
			characterPane.setRefresh(true);
			characterPane.refresh();
		}
	}

	/**
	 * Handler for the Purchase Mode Config button.
	 */
	private final class PurcahseModeButtonListener implements ActionListener
	{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent actionEvent)
		{
			showPurchaseModeConfiguration();
		}
	}

}
