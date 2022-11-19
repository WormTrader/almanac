package com.wormtrader.almanac;
/********************************************************************
* @(#)Holiday.java 1.00 20130308
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Holiday: Represents a holiday as a date and a name. We track the
* date as an int in the form yyyymmdd to facilitate binary searches
* on the Holidays list.
*
* @author Rick Salamone
* @version 2.00
* 20130308 rts created
*******************************************************/

public final class Holiday
	{
	private final int fyyyymmdd;
	private final String fName;

	public Holiday(int ayyyymmdd, String aName)
		{
		fyyyymmdd = ayyyymmdd;
		fName = aName;
		}

	public Holiday(String csv)
		{
		String[] pieces = csv.split(",", 2);
		fyyyymmdd = Integer.parseInt(pieces[0].trim());
		fName = pieces[1].trim();
		}

	@Override public String toString() { return "" + fyyyymmdd +"," + fName; }
	@Override public int hashCode() { return fyyyymmdd; }
	public int yyyymmdd() { return fyyyymmdd; }
	public String name() { return fName; }
	@Override public boolean equals(Object that)
		{
		return this == that;
		}

	public boolean equals(int ayyyymmdd) { return fyyyymmdd == ayyyymmdd; }
	}
