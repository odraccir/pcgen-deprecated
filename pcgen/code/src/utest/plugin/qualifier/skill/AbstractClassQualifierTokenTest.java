/**
 * 
 */
package plugin.qualifier.skill;

import java.net.URISyntaxException;

import pcgen.cdom.base.CDOMObject;
import pcgen.core.Skill;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.persistence.CDOMLoader;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import plugin.lsttokens.ChooseLst;
import plugin.lsttokens.choose.SkillToken;
import plugin.lsttokens.testsupport.AbstractQualifierTokenTestCase;
import plugin.lsttokens.testsupport.CDOMTokenLoader;
import plugin.lsttokens.testsupport.TokenRegistration;

public class AbstractClassQualifierTokenTest extends
		AbstractQualifierTokenTestCase<CDOMObject, Skill>
{

	static ChooseLst token = new ChooseLst();
	static SkillToken subtoken = new SkillToken();
	static CDOMTokenLoader<CDOMObject> loader = new CDOMTokenLoader<CDOMObject>(
			CDOMObject.class);

	private static final ClassToken CLASS_TOKEN = new ClassToken();

	public AbstractClassQualifierTokenTest()
	{
		super("CLASS", true);
	}

	@Override
	public void setUp() throws PersistenceLayerException, URISyntaxException
	{
		super.setUp();
		TokenRegistration.register(CLASS_TOKEN);
	}

	@Override
	public CDOMSecondaryToken<?> getSubToken()
	{
		return subtoken;
	}

	@Override
	public Class<Skill> getTargetClass()
	{
		return Skill.class;
	}

	@Override
	public Class<Skill> getCDOMClass()
	{
		return Skill.class;
	}

	@Override
	public CDOMLoader<CDOMObject> getLoader()
	{
		return loader;
	}

	@Override
	public CDOMPrimaryToken<CDOMObject> getToken()
	{
		return token;
	}

	@Override
	protected boolean allowsNotQualifier()
	{
		return false;
	}

}