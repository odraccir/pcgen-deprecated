/*
 * Created on Sep 2, 2005
 *
 */
package plugin.lsttokens;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.base.formula.Formula;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.ChoiceActor;
import pcgen.cdom.base.ChoiceSet;
import pcgen.cdom.base.Constants;
import pcgen.cdom.base.FormulaFactory;
import pcgen.cdom.base.TransitionChoice;
import pcgen.cdom.choiceset.SimpleChoiceSet;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.Region;
import pcgen.core.PlayerCharacter;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractToken;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.util.Logging;

/**
 * @author djones4
 * 
 */
public class RegionLst extends AbstractToken implements
		CDOMPrimaryToken<CDOMObject>, ChoiceActor<Region>
{
	@Override
	public String getTokenName()
	{
		return "REGION";
	}

	public boolean parse(LoadContext context, CDOMObject pcc, String value)
	{
		if (isEmpty(value) || hasIllegalSeparator('|', value))
		{
			return false;
		}
		StringTokenizer tok = new StringTokenizer(value, Constants.PIPE);
		String item = tok.nextToken();
		Formula count = FormulaFactory.getFormulaFor(item);
		if (count.isStatic())
		{
			item = tok.nextToken();
			if (count.resolve(null, "").intValue() <= 0)
			{
				Logging.addParseMessage(Logging.LST_ERROR, "Count in "
						+ getTokenName() + " must be > 0");
				return false;
			}
			if (!tok.hasMoreTokens())
			{
				Logging.addParseMessage(Logging.LST_ERROR, getTokenName()
						+ " must have a | separating "
						+ "count from the list of possible values: " + value);
				return false;
			}
		}
		List<Region> regions = new ArrayList<Region>();
		while (true)
		{
			regions.add(Region.getConstant(item));
			if (!tok.hasMoreTokens())
			{
				break;
			}
			item = tok.nextToken();
		}

		SimpleChoiceSet<Region> rcs = new SimpleChoiceSet<Region>(regions);
		ChoiceSet<Region> cs = new ChoiceSet<Region>(getTokenName(), rcs);
		TransitionChoice<Region> tc = new TransitionChoice<Region>(cs, count);
		context.obj.put(pcc, ObjectKey.REGION_CHOICE, tc);
		tc.setTitle("Region Selection");
		tc.setRequired(false);
		tc.setChoiceActor(this);
		return true;
	}

	public String[] unparse(LoadContext context, CDOMObject pcc)
	{
		TransitionChoice<Region> tc = context.getObjectContext().getObject(pcc,
				ObjectKey.REGION_CHOICE);
		if (tc == null)
		{
			// indicates no Token
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String count = tc.getCount().toString();
		if (!"1".equals(count))
		{
			sb.append(count);
			sb.append(Constants.PIPE);
		}
		sb.append(tc.getChoices().getLSTformat().replaceAll(Constants.COMMA,
				Constants.PIPE));
		return new String[] { sb.toString() };
	}

	public Class<CDOMObject> getTokenClass()
	{
		return CDOMObject.class;
	}

	public void applyChoice(Region choice, PlayerCharacter pc)
	{
		if (!pc.getRegion().equalsIgnoreCase(choice.toString()))
		{
			pc.setRegion(choice.toString());
		}
	}
}
