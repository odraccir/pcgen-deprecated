package pcgen.core;

import pcgen.core.spell.Spell;

public class ClassSpellList extends CDOMListObject<Spell>
{

	public Class<Spell> getListClass()
	{
		return Spell.class;
	}

	// No additional Functionality :)

}
