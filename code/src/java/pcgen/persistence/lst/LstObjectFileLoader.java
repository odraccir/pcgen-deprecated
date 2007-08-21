/*
 * LstLineFileLoader.java
 * Copyright 2007 (C) Tom Parker <thpr@users.sourceforge.net>
 * Copyright 2003 (C) David Hibbs <sage_sam@users.sourceforge.net>
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
 * Created on November 17, 2003, 12:00 PM
 *
 * Current Ver: $Revision$ <br>
 * Last Editor: $Author$ <br>
 * Last Edited: $Date$
 */
package pcgen.persistence.lst;

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.StringTokenizer;

import pcgen.cdom.enumeration.ObjectKey;
import pcgen.core.PObject;
import pcgen.core.SettingsHandler;
import pcgen.persistence.LoadContext;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.SystemLoader;
import pcgen.util.Logging;
import pcgen.util.PropertyFactory;

/**
 * This class is an extension of the LstFileLoader that loads items that are
 * PObjects and have a source campaign associated with them. Objects loaded by
 * implementations of this class inherit the core MOD/COPY/FORGET funcationality
 * needed for core PObjects used to directly create characters.
 * 
 * <p>
 * Current Ver: $Revision$ <br>
 * Last Editor: $Author$ <br>
 * Last Edited: $Date$
 * 
 * @author AD9C15
 */
public abstract class LstObjectFileLoader<T extends PObject> extends Observable
		implements LstLoader<T>
{
	/** The String that separates fields in the file. */
	public static final String FIELD_SEPARATOR = "\t"; //$NON-NLS-1$
	/** The String that separates individual objects */
	public static final String LINE_SEPARATOR = "\r\n"; //$NON-NLS-1$

	/** Tag used to include an object */
	public static final String INCLUDE_TAG = "INCLUDE"; //$NON-NLS-1$

	/** Tag used to exclude an object */
	public static final String EXCLUDE_TAG = "EXCLUDE"; //$NON-NLS-1$

	/** The suffix used to indicate this is a copy operation */
	public static final String COPY_SUFFIX = ".COPY"; //$NON-NLS-1$
	/** The suffix used to indicate this is a mod operation */
	public static final String MOD_SUFFIX = ".MOD"; //$NON-NLS-1$
	/** The suffix used to indicate this is a forget operation */
	public static final String FORGET_SUFFIX = ".FORGET"; //$NON-NLS-1$

	private List<ModEntry> copyLineList = new ArrayList<ModEntry>();
	private List<String> forgetLineList = new ArrayList<String>();
	private List<List<ModEntry>> modEntryList = new ArrayList<List<ModEntry>>();
	private Map<String, String> sourceMap = null;
	/** A list of objects that will not be included. */
	protected List<String> excludedObjects = new ArrayList<String>();

	/**
	 * LstObjectFileLoader constructor.
	 */
	public LstObjectFileLoader()
	{
		super();
	}

	/**
	 * This method loads the given list of LST files.
	 * 
	 * @param fileList
	 *            containing the list of files to read
	 * @throws PersistenceLayerException
	 */
	public void loadLstFiles(LoadContext context,
		List<CampaignSourceEntry> fileList) throws PersistenceLayerException
	{
		// Track which sources have been loaded already
		Set<CampaignSourceEntry> loadedFiles = new HashSet<CampaignSourceEntry>();

		// Load the files themselves as thoroughly as possible
		for (CampaignSourceEntry sourceEntry : fileList)
		{
			if (sourceEntry == null)
			{
				continue;
			}

			// Check if the CSE has already been loaded before loading it
			if (!loadedFiles.contains(sourceEntry))
			{
				context.setSourceURI(sourceEntry.getURI());
				loadLstFile(context, sourceEntry);
				loadedFiles.add(sourceEntry);
			}
		}

		// FIXME Need to ensure proper setSourceURI in the copy/mod/forget
		// methods

		// Next we perform copy operations
		processCopies(context);

		// Now handle .MOD items
		sourceMap = null;
		processMods(context);

		// Finally, forget the .FORGET items
		processForgets();
	}

	/**
	 * This method parses the LST file line, applying it to the provided target
	 * object. If the line indicates the start of a new target object, a new
	 * PObject of the appropriate type will be created prior to applying the
	 * line contents. Because of this behavior, it is necessary for this method
	 * to return the new object. Implementations of this method also MUST call
	 * <code>completeObject</code> with the original target prior to returning
	 * the new value.
	 * 
	 * @param lstLine
	 *            String LST formatted line read from the source URL
	 * @param target
	 *            PObject to apply the line to, barring the start of a new
	 *            object
	 * @param source
	 *            CampaignSourceEntry indicating the file that the line was read
	 *            from as well as the Campaign object that referenced the file
	 * @return PObject that was either created or modified by the provided LST
	 *         line
	 * @throws PersistenceLayerException
	 *             if there is a problem with the LST syntax
	 */
	public abstract void parseLine(T target, String lstLine,
		CampaignSourceEntry source) throws PersistenceLayerException;

	public void parseLine(LoadContext context, T target, String lstLine,
		CampaignSourceEntry source)
	{
		final StringTokenizer colToken =
				new StringTokenizer(lstLine, SystemLoader.TAB_DELIM);
		// Set Name
		String firstToken = colToken.nextToken();
		URI oldSource = target.get(ObjectKey.SOURCE_URI);
		if (oldSource == null)
		{
			target.setName(firstToken);
			target.put(ObjectKey.SOURCE_URI, source.getURI());
		}
		else
		{
			// TODO Assumes it's a .MOD or something :/ - check this??
			if (!source.getURI().equals(oldSource))
			{
				// TODO This should be a Supplemental URI list??
				target.put(ObjectKey.SOURCE_URI, source.getURI());
			}
		}

		while (colToken.hasMoreTokens())
		{
			String colString = colToken.nextToken().trim();
			int idxColon = colString.indexOf(':');
			if (idxColon == -1)
			{
				Logging.errorPrint("Invalid Token - does not contain a colon: "
					+ colString);
				continue;
			}
			else if (idxColon == 0)
			{
				Logging.errorPrint("Invalid Token - starts with a colon: "
					+ colString);
				continue;
			}
			String key = colString.substring(0, idxColon);
			String value =
					(idxColon == colString.length() - 1) ? null : colString
						.substring(idxColon + 1);
			try
			{
				parseToken(context, target, key, value, source);
			}
			catch (Throwable t)
			{
				Logging.addParseMessage(Logging.ERROR, "Parse error in token "
					+ key + ": " + t);
			}
		}
	}

	public abstract void parseToken(LoadContext context, T target, String key,
		String value, CampaignSourceEntry source);

	/**
	 * This method is called by the loading framework to signify that the
	 * loading of this object is complete and the object should be added to the
	 * system.
	 * 
	 * <p>
	 * This method will check that the loaded object should be included via a
	 * call to <code>includeObject</code> and if not add it to the list of
	 * excluded objects.
	 * 
	 * <p>
	 * Once the object has been verified the method will call
	 * <code>finishObject</code> to give each object a chance to complete
	 * processing.
	 * 
	 * <p>
	 * The object is then added to the system if it doesn't already exist. If
	 * the object exists, the object sources are compared by date and if the
	 * System setting allowing over-rides is set it will use the object from the
	 * newer source.
	 * 
	 * @param pObj
	 *            The object that has just completed loading.
	 * 
	 * @see pcgen.persistence.lst.LstObjectFileLoader#includeObject(PObject)
	 * @see pcgen.persistence.lst.LstObjectFileLoader#finishObject(PObject)
	 * @see pcgen.core.SettingsHandler#isAllowOverride()
	 * 
	 * @author boomer70 <boomer70@yahoo.com>
	 * @throws PersistenceLayerException
	 * 
	 * @since 5.11
	 */
	public void completeObject(CampaignSourceEntry source, final PObject pObj)
		throws PersistenceLayerException
	{
		if (pObj == null)
		{
			return;
		}

		// Make sure the source info was set
		if (sourceMap != null)
		{
			try
			{
				pObj.setSourceMap(sourceMap);
			}
			catch (ParseException e)
			{
				throw new PersistenceLayerException(e.toString());
			}
		}

		if (includeObject(source, pObj))
		{
			finishObject(pObj);
			final T currentObj = getObjectKeyed(pObj.getKeyName());

			if (currentObj == null || !pObj.equals(currentObj))
			{
				addGlobalObject(pObj);
			}
			else
			{
				if (!currentObj.getSourceURI().equals(pObj.getSourceURI()))
				{
					if (SettingsHandler.isAllowOverride())
					{
						// If the new object is more recent than the current
						// one, use the new object
						final Date pObjDate =
								pObj.getSourceEntry().getSourceBook().getDate();
						final Date currentObjDate =
								currentObj.getSourceEntry().getSourceBook()
									.getDate();
						if ((pObjDate != null)
							&& ((currentObjDate == null) || ((pObjDate
								.compareTo(currentObjDate) > 0))))
						{
							performForget(currentObj);
							addGlobalObject(pObj);
						}
					}
					else
					{
						// Duplicate loading error
						Logging.errorPrintLocalised(
							"Warnings.LstFileLoader.DuplicateObject", //$NON-NLS-1$
							pObj.getKeyName(), currentObj.getSourceURI(), pObj
								.getSourceURI());
					}
				}
			}
		}
		else
		{
			excludedObjects.add(pObj.getKeyName());
		}
	}

	/**
	 * Adds an object to the global repository.
	 * 
	 * @param pObj
	 *            The object to add.
	 * 
	 * @author boomer70 <boomer70@yahoo.com>
	 * 
	 * @since 5.11
	 */
	protected abstract void addGlobalObject(final PObject pObj);

	/**
	 * This method is called when the end of data for a specific PObject is
	 * found.
	 * 
	 * <p>
	 * This method will only be called for objects that are to be included.
	 * 
	 * @param target
	 *            PObject to perform final operations on
	 */
	protected void finishObject(@SuppressWarnings("unused")
	PObject target)
	{
		// Placeholder implementation
	}

	/**
	 * This method should be called by finishObject implementations in order to
	 * check if the parsed object is affected by an INCLUDE or EXCLUDE request.
	 * 
	 * @param parsedObject
	 *            PObject to determine whether to include in Globals etc.
	 * @return boolean true if the object should be included, else false to
	 *         exclude it
	 */
	protected final boolean includeObject(CampaignSourceEntry source,
		PObject parsedObject)
	{
		// Null check; never add nulls or objects without a name/key name
		if ((parsedObject == null) || (parsedObject.getDisplayName() == null)
			|| (parsedObject.getDisplayName().trim().length() == 0)
			|| (parsedObject.getKeyName() == null)
			|| (parsedObject.getKeyName().trim().length() == 0))
		{
			return false;
		}

		// If includes were present, check includes for given object
		List<String> includeItems = source.getIncludeItems();

		if (!includeItems.isEmpty())
		{
			return includeItems.contains(parsedObject.getKeyName());
		}
		// If excludes were present, check excludes for given object
		List<String> excludeItems = source.getExcludeItems();

		if (!excludeItems.isEmpty())
		{
			return !excludeItems.contains(parsedObject.getKeyName());
		}

		return true;
	}

	/**
	 * This method retrieves a PObject from globals by its key. This is used to
	 * avoid duplicate loads, get objects to forget or modify, etc.
	 * 
	 * @param aKey
	 *            String key of PObject to retrieve
	 * @return PObject from Globals
	 */
	protected abstract T getObjectKeyed(String aKey);

	/**
	 * This method loads a single LST formatted file.
	 * 
	 * @param sourceEntry
	 *            CampaignSourceEntry containing the absolute file path or the
	 *            URL from which to read LST formatted data.
	 */
	protected void loadLstFile(LoadContext context,
		CampaignSourceEntry sourceEntry)
	{
		setChanged();
		notifyObservers(sourceEntry.getURI());

		sourceMap = null;

		StringBuilder dataBuffer;

		try
		{
			dataBuffer = LstFileLoader.readFromURI(sourceEntry.getURI());
		}
		catch (PersistenceLayerException ple)
		{
			String message = PropertyFactory.getFormattedString(
				"Errors.LstFileLoader.LoadError", //$NON-NLS-1$
				sourceEntry.getURI(), ple.getMessage());
			Logging.errorPrint(message);
			setChanged();
			notifyObservers(new Exception(message));
			return;
		}

		final String aString = dataBuffer.toString();

		String[] fileLines = aString.split(LstFileLoader.LINE_SEPARATOR_REGEXP);

		for (int i = 0; i < fileLines.length; i++)
		{
			String line = fileLines[i];
			if ((line.length() == 0)
				|| (line.charAt(0) == LstFileLoader.LINE_COMMENT_CHAR))
			{
				continue;
			}
			parseFullLine(context, i + 1, line, sourceEntry);
		}
	}

	public T parseFullLine(LoadContext context, int currentLineNumber,
		String line, CampaignSourceEntry sourceEntry)
	{
		int sepLoc = line.indexOf(FIELD_SEPARATOR);
		String firstToken;
		if (sepLoc == -1)
		{
			firstToken = line;
		}
		else
		{
			firstToken = line.substring(0, sepLoc);
		}

		context.setLine(currentLineNumber);
		// check for comments, copies, mods, and forgets
		if ((line.length() == 0)
			|| (line.charAt(0) == LstFileLoader.LINE_COMMENT_CHAR))
		{
			return null;
		}
		// TODO - Figure out why we need to check SOURCE in this file
		else if (line.startsWith("SOURCE")) //$NON-NLS-1$
		{
			sourceMap = SourceLoader.parseLine(line, sourceEntry.getURI());
		}
		else if (firstToken.indexOf(COPY_SUFFIX) > 0)
		{
			copyLineList.add(new ModEntry(sourceEntry, line, currentLineNumber,
				sourceMap));
		}
		else if (firstToken.indexOf(MOD_SUFFIX) > 0)
		{
			List<ModEntry> modLines = new ArrayList<ModEntry>();
			modLines.add(new ModEntry(sourceEntry, line, currentLineNumber,
				sourceMap));
			modEntryList.add(modLines);
		}
		else if (firstToken.indexOf(FORGET_SUFFIX) > 0)
		{
			forgetLineList.add(line);
		}
		else
		{
			T obj = context.ref.constructCDOMObject(getLoadClass(), firstToken);
			obj.setName(firstToken);
			obj.setSourceCampaign(sourceEntry.getCampaign());
			obj.setSourceURI(sourceEntry.getURI());
			// first column is the name; after that are LST tags
			String restOfLine = line.substring(sepLoc).trim();
			try
			{
				parseLine(obj, restOfLine, sourceEntry);
			}
			catch (PersistenceLayerException ple)
			{
				String message =
						PropertyFactory.getFormattedString(
							"Errors.LstFileLoader.ParseError", //$NON-NLS-1$
							sourceEntry.getURI(), currentLineNumber, ple
								.getMessage());
				Logging.errorPrint(message);
				setChanged();
				notifyObservers(new Exception(message));
				Logging.debugPrint("Parse error:", ple); //$NON-NLS-1$
			}
			catch (Throwable t)
			{
				String message =
						PropertyFactory.getFormattedString(
							"Errors.LstFileLoader.ParseError", //$NON-NLS-1$
							sourceEntry.getURI(), currentLineNumber, t
								.getMessage());
				Logging.errorPrint(message);
				setChanged();
				notifyObservers(new Exception(message));
				Logging.errorPrint(PropertyFactory
					.getString("Errors.LstFileLoader.Ignoring"), //$NON-NLS-1$
					t);
			}
			parseLine(context, obj, line, sourceEntry);
			return obj;
		}
		return null;
	}

	/**
	 * This method, when implemented, will perform a single .FORGET operation.
	 * 
	 * @param objToForget
	 *            containing the object to forget
	 */
	protected abstract void performForget(T objToForget);

	/**
	 * This method will perform a single .COPY operation.
	 * @param entry 
	 * @param context 
	 * 
	 * @param baseName
	 *            String name of the object to copy
	 * @param copyName
	 *            String name of the target object
	 * @throws PersistenceLayerException
	 */
	private T performCopy(LoadContext context, CampaignSourceEntry source,
		String baseKey, String copyName) throws PersistenceLayerException
	{
		T object = getObjectKeyed(baseKey);

		if (object == null)
		{
			String message = PropertyFactory.getFormattedString(
				"Errors.LstFileLoader.CopyObjectNotFound", //$NON-NLS-1$
				baseKey);
			Logging.errorPrint(message);
			setChanged();
			notifyObservers(new Exception(message));
			return null;
		}
		Class<T> cl = (Class<T>) object.getClass();
		T clone = context.ref.cloneConstructedCDOMObject(cl, object, copyName);
		completeObject(source, clone);
		return clone;
	}

	/**
	 * This method will perform a single .COPY operation based on the LST file
	 * content.
	 * 
	 * @param lstLine
	 *            String containing the LST source for the .COPY operation
	 * @throws PersistenceLayerException
	 */
	private void performCopy(LoadContext context, ModEntry me)
		throws PersistenceLayerException
	{
		String lstLine = me.getLstLine();
		int sepLoc = lstLine.indexOf(FIELD_SEPARATOR);
		String name;
		if (sepLoc != -1)
		{
			name = lstLine.substring(0, sepLoc);
		}
		else
		{
			name = lstLine;
		}
		final int nameEnd = name.indexOf(COPY_SUFFIX);
		final String baseName = name.substring(0, nameEnd);
		final String copyName = name.substring(nameEnd + 6);
		T copy = performCopy(context, me.getSource(), baseName, copyName);
		if (copy != null)
		{
			if (sepLoc != -1)
			{
				String restOfLine = me.getLstLine().substring(nameEnd + 6);
				parseLine(copy, restOfLine, me.getSource());
			}
			completeObject(me.getSource(), copy);
		}
	}

	/**
	 * This method will perform a multi-line .MOD operation. This is used for
	 * example in MODs of CLASSES which can have multiple lines. Loaders can
	 * [typically] use the name without checking for (or stripping off) .MOD due
	 * to the implementation of PObject.setName()
	 * 
	 * @param entryList
	 */
	private void performMod(LoadContext context, List<ModEntry> entryList)
	{
		ModEntry entry = entryList.get(0);
		// get the name of the object to modify, trimming off the .MOD
		int nameEnd = entry.getLstLine().indexOf(MOD_SUFFIX);
		String key = entry.getLstLine().substring(0, nameEnd);

		if (excludedObjects.contains(key))
		{
			return;
		}
		// get the actual object to modify
		T object = getCDOMObjectKeyed(context, key);

		if (object == null)
		{
			String message = PropertyFactory.getFormattedString(
				"Errors.LstFileLoader.ModObjectNotFound", //$NON-NLS-1$
				entry.getSource().getURI(), entry.getLineNumber(), key);
			Logging.errorPrint(message);
			setChanged();
			notifyObservers(new Exception(message));
			return;
		}

		// modify the object
		try
		{
			for (ModEntry element : entryList)
			{
				boolean noSource = object.getSourceEntry() == null;
				int hashCode = 0;
				if (!noSource)
				{
					hashCode = object.getSourceEntry().hashCode();
				}

				String line = element.getLstLine();
				int sepLoc = line.indexOf(FIELD_SEPARATOR);
				String restOfLine = line.substring(sepLoc).trim();
				parseLine(object, restOfLine, element.getSource());

				if ((noSource && object.getSourceEntry() != null)
					|| (!noSource && hashCode != object.getSourceEntry()
						.hashCode()))
				{
					// We never had a source and now we do so set the source
					// map or we did have a source and now the hashCode is
					// different so the MOD line must have updated it.
					try
					{
						object.setSourceMap(element.getSourceMap());
					}
					catch (ParseException notUsed)
					{
						Logging.errorPrintLocalised(
							"Errors.LstFileLoader.ParseDate", sourceMap); //$NON-NLS-1$
					}
				}
			}
			completeObject(entry.getSource(), object);
		}
		catch (PersistenceLayerException ple)
		{
			String message = PropertyFactory.getFormattedString(
				"Errors.LstFileLoader.ModParseError", //$NON-NLS-1$
				entry.getSource().getURI(), entry.getLineNumber(), ple
					.getMessage());
			Logging.errorPrint(message);
			setChanged();
			notifyObservers(new Exception(message));
		}
	}

	protected T getCDOMObjectKeyed(LoadContext context, String key)
	{
		return context.ref.getConstructedCDOMObject(getLoadClass(), key);
	}

	/**
	 * This method will process the lines containing a .COPY directive
	 * 
	 * @throws PersistenceLayerException
	 */
	private void processCopies(LoadContext context)
		throws PersistenceLayerException
	{
		for (ModEntry me : copyLineList)
		{
			performCopy(context, me);
		}
		copyLineList.clear();
	}

	/**
	 * This method will process the lines containing a .FORGET directive
	 */
	private void processForgets()
	{

		for (String forgetKey : forgetLineList)
		{
			forgetKey =
					forgetKey.substring(0, forgetKey.indexOf(FORGET_SUFFIX));

			if (excludedObjects.contains(forgetKey))
			{
				continue;
			}
			// Commented out so that deprcated method no longer used
			// performForget(forgetName);

			T objToForget = getObjectKeyed(forgetKey);
			if (objToForget != null)
			{
				performForget(objToForget);
			}
		}
		forgetLineList.clear();
	}

	/**
	 * This method will process the lines containing a .MOD directive
	 */
	private void processMods(LoadContext context)
	{
		for (List<ModEntry> modEntry : modEntryList)
		{
			performMod(context, modEntry);
		}
		modEntryList.clear();
	}

	/**
	 * This class is an entry mapping a mod to its source. Once created,
	 * instances of this class are immutable.
	 */
	public static class ModEntry
	{
		private CampaignSourceEntry source = null;
		private String lstLine = null;
		private int lineNumber = 0;
		private Map<String, String> sourceMap = null;

		/**
		 * ModEntry constructor.
		 * 
		 * @param aSource
		 *            CampaignSourceEntry containing the MOD line [must not be
		 *            null]
		 * @param aLstLine
		 *            LST syntax modification [must not be null]
		 * @param aLineNumber
		 * @param aSourceMap
		 * 
		 * @throws IllegalArgumentException
		 *             if aSource or aLstLine is null.
		 */
		public ModEntry(final CampaignSourceEntry aSource,
			final String aLstLine, final int aLineNumber,
			final Map<String, String> aSourceMap)
		{
			super();

			// These are programming errors so the msgs don't need to be
			// internationalized.
			if (aSource == null)
			{
				throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
			}

			if (aLstLine == null)
			{
				throw new IllegalArgumentException("lstLine must not be null"); //$NON-NLS-1$
			}

			this.source = aSource;
			this.lstLine = aLstLine;
			this.lineNumber = aLineNumber;
			this.sourceMap = aSourceMap;
		}

		/**
		 * This method gets the LST formatted source line for the .MOD
		 * 
		 * @return String in LST format, unmodified from the source file
		 */
		public String getLstLine()
		{
			return lstLine;
		}

		/**
		 * This method gets the source of the .MOD operation
		 * 
		 * @return CampaignSourceEntry indicating where the .MOD came from
		 */
		public CampaignSourceEntry getSource()
		{
			return source;
		}

		/**
		 * 
		 * @return The source map for this MOD entry
		 */
		public Map<String, String> getSourceMap()
		{
			return sourceMap;
		}

		/**
		 * 
		 * @return The line number of the original file for this MOD entry
		 */
		public int getLineNumber()
		{
			return lineNumber;
		}
	}

	public abstract Class<T> getLoadClass();
}
