/*
 * Copyright (c) Thomas Parker, 2012.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.cdom.facet.input;

import java.util.ArrayList;
import java.util.List;

import pcgen.cdom.enumeration.CharID;
import pcgen.cdom.facet.FacetLibrary;
import pcgen.cdom.facet.PlayerCharacterTrackingFacet;
import pcgen.cdom.facet.RaceSelectionFacet;
import pcgen.cdom.facet.model.RaceFacet;
import pcgen.core.PlayerCharacter;
import pcgen.core.Race;
import pcgen.core.analysis.ChooseActivation;
import pcgen.core.chooser.ChoiceManagerList;
import pcgen.core.chooser.ChooserUtilities;

/**
 * RaceInputFacet is a Facet that tracks the Race of a Player Character.
 */
public class RaceInputFacet
{

	private final PlayerCharacterTrackingFacet trackingFacet = FacetLibrary
		.getFacet(PlayerCharacterTrackingFacet.class);

	private RaceSelectionFacet raceSelectionFacet;

	private RaceFacet raceFacet;

	public boolean set(CharID id, Race race)
	{
		if (ChooseActivation.hasNewChooseToken(race))
		{
			PlayerCharacter pc = trackingFacet.getPC(id);
			ChoiceManagerList<?> aMan =
					ChooserUtilities.getChoiceManager(race, pc);
			return processChoice(id, pc, race, aMan);
		}
		else
		{
			return directSet(id, race, null);
		}
	}

	private <T> boolean processChoice(CharID id, PlayerCharacter pc, Race race,
		ChoiceManagerList<T> aMan)
	{
		List<T> selectedList = new ArrayList<T>();
		List<T> availableList = new ArrayList<T>();
		aMan.getChoices(pc, availableList, selectedList);

		if (availableList.isEmpty())
		{
			return false;
		}
		if (!selectedList.isEmpty())
		{
			//Error?
		}
		final List<T> newSelections =
				aMan.doChooser(pc, availableList, selectedList,
					new ArrayList<String>());
		if (newSelections.size() != 1)
		{
			//Error?
			return false;
		}
		for (T sel : newSelections)
		{
			directSet(id, race, sel);
		}
		return true;
	}

	public void importSelection(CharID id, Race race, String choice)
	{
		PlayerCharacter pc = trackingFacet.getPC(id);
		if (ChooseActivation.hasNewChooseToken(race))
		{
			ChoiceManagerList<?> aMan =
					ChooserUtilities.getChoiceManager(race, pc);
			processImport(id, race, aMan, choice);
		}
		else
		{
			directSet(id, race, null);
		}
	}

	private <T> void processImport(CharID id, Race race,
		ChoiceManagerList<T> aMan, String choice)
	{
		directSet(id, race, aMan.decodeChoice(choice));
	}

	public <T> boolean directSet(CharID id, Race race, T sel)
	{
		raceFacet.set(id, race);
		if (sel != null)
		{
			raceSelectionFacet.set(id, race, sel);
		}
		return true;
	}

	public void remove(CharID id)
	{
		raceFacet.remove(id);
		/*
		 * No need to deal with raceSelectionFacet here. It must listen to
		 * RaceFacet as a set also can implicitly unset the previous race.
		 * Therefore, listening is easier than trying to write
		 * raceSelectionFacet here and in directSet above
		 */
	}

	public void setRaceSelectionFacet(RaceSelectionFacet raceSelectionFacet)
	{
		this.raceSelectionFacet = raceSelectionFacet;
	}

	public void setRaceFacet(RaceFacet raceFacet)
	{
		this.raceFacet = raceFacet;
	}
}
