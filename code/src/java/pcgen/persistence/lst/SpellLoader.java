/*
 * SpellLoader.java
 * Copyright 2001 (C) Bryan McRoberts <merton_monk@yahoo.com>
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
 * Created on February 22, 2002, 10:29 PM
 *
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 *
 */
package pcgen.persistence.lst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import pcgen.core.Globals;
import pcgen.core.PObject;
import pcgen.core.prereq.Prerequisite;
import pcgen.core.spell.Spell;
import pcgen.persistence.LoadContext;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.SystemLoader;
import pcgen.persistence.lst.prereq.PreParserFactory;
import pcgen.util.Logging;

/**
 *
 * @author  David Rice <david-pcgen@jcuz.com>
 * @version $Revision$
 */
public final class SpellLoader extends LstObjectFileLoader<Spell>
{
	/** Creates a new instance of SpellLoader */
	public SpellLoader()
	{
		super();
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#parseLine(pcgen.core.PObject, java.lang.String, pcgen.persistence.lst.CampaignSourceEntry)
	 */
	@Override
	public void parseLine(Spell spell, String lstLine,
		CampaignSourceEntry source) throws PersistenceLayerException
	{
		final StringTokenizer colToken =
				new StringTokenizer(lstLine, SystemLoader.TAB_DELIM);

		Map<String, LstToken> tokenMap =
				TokenStore.inst().getTokenMap(SpellLstToken.class);

		while (colToken.hasMoreElements())
		{
			final String colString = colToken.nextToken().trim();
			final int idxColon = colString.indexOf(':');
			String key = "";
			try
			{
				key = colString.substring(0, idxColon);
			}
			catch (Exception e)
			{
				// TODO Handle Exception
			}
			SpellLstToken token = (SpellLstToken) tokenMap.get(key);

			if (token != null)
			{
				final String value = colString.substring(idxColon + 1).trim();
				LstUtils.deprecationCheck(token, spell, value);
				if (!token.parse(spell, value))
				{
					Logging.errorPrint("Error parsing spell "
						+ spell.getDisplayName() + ':' + source.getURI() + ':'
						+ colString + "\"");
				}
			}
			else if (PObjectLoader.parseTag(spell, colString))
			{
				continue;
			}
			else
			{
				Logging.errorPrint("Illegal spell info '" + colString + "' in "
					+ source.getURI());
			}
		}

		completeObject(source, spell);
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#getObjectKeyed(java.lang.String)
	 */
	@Override
	protected Spell getObjectKeyed(final String aKey)
	{
		return Globals.getSpellKeyed(aKey);
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#finishObject(pcgen.core.PObject)
	 */
	@Override
	protected void finishObject(PObject target)
	{
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#performForget(pcgen.core.PObject)
	 */
	@Override
	protected void performForget(final Spell objToForget)
	{
		Globals.removeFromSpellMap(objToForget.getKeyName());
	}

	/**
	 * @param spell
	 * @param typeString should be CLASS or DOMAIN
	 * @param listString should be name,name,name=level|name,name=level|etc
	 * where name is the name of a class or domain and
	 * level is an integer for this spell's level for the named class/domain
	 * @throws PersistenceLayerException
	 */
	public static void setLevelList(Spell spell, final String typeString,
		String listString) throws PersistenceLayerException
	{
		String preReqTag = null;
		final int i = listString.lastIndexOf('[');
		int j = listString.lastIndexOf(']');

		if (j < i)
		{
			Logging.errorPrint("Warning: Close Bracket before Open Bracket in Level List: " + listString);
			j = listString.length();
		}

		if (i >= 0)
		{
			preReqTag = listString.substring(i + 1, j);
			if (preReqTag.length() == 0) {
				Logging.errorPrint("Warning: Empty Prerequisite in Level List: " + listString);
			}
			listString = listString.substring(0, i);
		}

		final StringTokenizer aTok =
				new StringTokenizer(listString, "|", false);

		while (aTok.hasMoreTokens())
		{
			final String aList = aTok.nextToken(); // could be name=x or name,name=x

			final StringTokenizer bTok = new StringTokenizer(aList, "=", false);

			while (bTok.hasMoreTokens())
			{
				final String nameList = bTok.nextToken();

				if (!bTok.hasMoreTokens())
				{
					throw new PersistenceLayerException("Badly formed spell "
						+ typeString + " data: " + listString);
				}

				final String aLevel = bTok.nextToken();
				final StringTokenizer cTok =
						new StringTokenizer(nameList, ",", false);

				while (cTok.hasMoreTokens())
				{
					final String aClass = cTok.nextToken();

					if (preReqTag != null)
					{
						PreParserFactory preFactory =
								PreParserFactory.getInstance();
						Prerequisite prerequisite = preFactory.parse(preReqTag);
						spell.addPreReqMapEntry(typeString + "|" + aClass,
							prerequisite);
					}

					spell.setLevelInfo(typeString + "|" + aClass, aLevel);
				}
			}
		}
	}

	/**
	 * @see pcgen.persistence.lst.LstObjectFileLoader#addGlobalObject(pcgen.core.PObject)
	 */
	@Override
	protected void addGlobalObject(final PObject pObj)
	{
		final Object obj = Globals.getSpellMap().get(pObj.getKeyName());
		if (obj == null)
		{
			Globals.addToSpellMap(pObj.getKeyName(), pObj);
		}
		else
		{
			final List<Spell> spellList;
			if (obj instanceof Spell)
			{
				spellList = new ArrayList<Spell>();
				Globals.removeFromSpellMap(((Spell) obj).getKeyName());
				Globals.addToSpellMap(pObj.getKeyName(), spellList);
				spellList.add((Spell) obj);
			}
			else
			{
				spellList = (List<Spell>) obj;
			}
			spellList.add((Spell) pObj);
		}
	}

	@Override
	public void parseToken(LoadContext context, Spell spell, String key, String value, CampaignSourceEntry source) throws PersistenceLayerException {
		SpellLstToken token = TokenStore.inst().getToken(SpellLstToken.class,
				key);

		if (token == null) {
			if (!PObjectLoader.parseTag(context, spell, key, value)) {
				Logging.errorPrint("Illegal spell Token '" + key + "' for "
						+ spell.getDisplayName() + " in " + source.getURI()
						+ " of " + source.getCampaign() + ".");
			}
		} else {
			LstUtils.deprecationCheck(token, spell, value);
			if (!token.parse(context, spell, value))
			{
				Logging.errorPrint("Error parsing token " + key + " in spell "
					+ spell.getDisplayName() + ':' + source.getURI() + ':'
					+ value + "\"");
			}
		}
	}

	@Override
	public Class<Spell> getLoadClass() {
		return Spell.class;
	}
}
