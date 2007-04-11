/*
 * Copyright (c) 2007 Tom Parker <thpr@users.sourceforge.net>
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
package plugin.lsttokens.pcclass;

import java.net.URISyntaxException;

import org.junit.Test;

import pcgen.core.PCClass;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.CDOMToken;
import pcgen.persistence.lst.LstLoader;
import plugin.lsttokens.testsupport.AbstractTokenTestCase;

public class MaxLevelTokenTest extends AbstractTokenTestCase<PCClass>
{

	static MaxlevelToken token = new MaxlevelToken();
	static PCClassLoaderFacade loader = new PCClassLoaderFacade();

	@Override
	public void setUp() throws PersistenceLayerException, URISyntaxException
	{
		super.setUp();
		prefix = "CLASS:";
	}

	@Override
	public Class<PCClass> getCDOMClass()
	{
		return PCClass.class;
	}

	@Override
	public LstLoader<PCClass> getLoader()
	{
		return loader;
	}

	@Override
	public CDOMToken<PCClass> getToken()
	{
		return token;
	}

	@Test
	public void testInvalidInput() throws PersistenceLayerException
	{
		// Always ensure get is unchanged
		// since no invalid item should set or reset the value
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "TestWP"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "String"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf,
			"TYPE=TestType"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf,
			"TYPE.TestType"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "ALL"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "ANY"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "FIVE"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "4.5"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "1/2"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "1+3"));
		assertEquals(primaryGraph, secondaryGraph);
		// Require Integer greater than zero
		assertFalse(getToken().parse(primaryContext, primaryProf, "-1"));
		assertEquals(primaryGraph, secondaryGraph);
		assertFalse(getToken().parse(primaryContext, primaryProf, "0"));
		assertEquals(primaryGraph, secondaryGraph);
	}

	@Test
	public void testValidInputs() throws PersistenceLayerException
	{
		assertTrue(getToken().parse(primaryContext, primaryProf, "5"));

		assertTrue(getToken().parse(primaryContext, primaryProf, "1"));

	}

	@Test
	public void testRoundRobinFive() throws PersistenceLayerException
	{
		runRoundRobin("5");
	}

	@Test
	public void testRoundRobinNoLimit() throws PersistenceLayerException
	{
		runRoundRobin("NOLIMIT");
	}

}
