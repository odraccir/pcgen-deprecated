/*
 * Copyright 2006-2007 (C) Tom Parker <thpr@users.sourceforge.net>
 * Copyright 2005-2006 (C) Devon Jones
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
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 */
package plugin.lsttokens;

import java.util.Set;
import java.util.TreeSet;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.FormulaFactory;
import pcgen.cdom.content.SpellResistance;
import pcgen.cdom.graph.PCGraphEdge;
import pcgen.core.PObject;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.GlobalLstToken;

/**
 * @author djones4
 * 
 */
public class SrLst implements GlobalLstToken
{

	public String getTokenName()
	{
		return "SR";
	}

	public boolean parse(PObject obj, String value, int anInt)
	{
		if (".CLEAR".equals(value))
		{
			obj.clearSRList();
		}
		else
		{
			obj.setSR(anInt, value);
		}
		return true;
	}

	public boolean parse(LoadContext context, CDOMObject obj, String value)
	{
		if (".CLEAR".equals(value))
		{
			context.graph.unlinkChildNodesOfClass(getTokenName(), obj,
				SpellResistance.class);
		}
		else
		{
			context.graph.linkObjectIntoGraph(getTokenName(), obj,
				getSpellResistance(value));
		}
		return true;
	}

	private SpellResistance getSpellResistance(String value)
	{
		return new SpellResistance(FormulaFactory.getFormulaFor(value));
	}

	public String[] unparse(LoadContext context, CDOMObject obj)
	{
		Set<PCGraphEdge> edgeList =
				context.graph.getChildLinksFromToken(getTokenName(), obj,
					SpellResistance.class);
		if (edgeList == null || edgeList.isEmpty())
		{
			return null;
		}
		Set<String> set = new TreeSet<String>();
		for (PCGraphEdge edge : edgeList)
		{
			SpellResistance sr = (SpellResistance) edge.getSinkNodes().get(0);
			set.add(sr.toString());
		}
		return set.toArray(new String[set.size()]);
	}
}
