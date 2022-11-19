package com.wormtrader.almanac;
/********************************************************************
* @(#)DateWalker.java 1.00 2010
* Copyright © 2010-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* DateWalker: For processing date ranges. Provides the methods to step
* through and generate lists of dates avoiding weekends and holidays.
*
* Usage: Given long[2] dateRange
*
*		String startDate = SBDate.yyyymmdd(dateRange[0]);
*		String endDate = SBDate.yyyymmdd(dateRange[1]);
*
*		DateWalker dl = new DateWalker( startDate, true );
*
*		// the following line gives the effective start
*		// date which is only different from startDate
*		// if start date is a weekend
*		String yyyymmdd = dl.yyyymmdd();
*
*		int runDays = dl.countDaysTil( endDate );
*		for ( int dayNum = 0; dayNum < runDays; dayNum++ )
*			{
*			// do something with this yyyymmdd
*			yyyymmdd = dl.nextDay();
*			}
*
* @author Rick Salamone
* @version 2.00
* 208      rts created
* 20130307 rts major overhaul to version 2
* 20130308 rts added holiday support
* 20130328 rts added nextMarketDay
* 20130410 rts added prevMarketDay
* 20140212 rts list() returns a List - added array() to return []
* 20140303 rts added ymd version of prevMarketDate
*******************************************************/
import com.shanebow.util.SBDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

public final class DateWalker
	{
	private static final int MILLIS_PER_SEC = 1000;
	boolean m_skipWeekends = true;
	private final long fTimeStart;
	private final long fTimeEnd;
	private long fTimeCurrent;

	public static final int prevMarketDay(int ymd) {
		return Integer.parseInt(prevMarketDay("" + ymd));
		}

	public static final int nextMarketDay(int ymd) {
		return Integer.parseInt(nextMarketDay("" + ymd));
		}

	public static final String prevMarketDay(String yyyymmdd)
		{
		long time = SBDate.toTime(yyyymmdd);
		String it;
		do
			{
			time = SBDate.prevWeekDay(time);
			it = SBDate.yyyymmdd(time);
			}
		while (Holidays.isHoliday(Integer.parseInt(it)));
		return it;
		}

	public static final String nextMarketDay(String yyyymmdd)
		{
		long time = SBDate.toTime(yyyymmdd);
		String it;
		do
			{
			time = SBDate.nextWeekDay(time);
			it = SBDate.yyyymmdd(time);
			}
		while (Holidays.isHoliday(Integer.parseInt(it)));
		return it;
		}

	private static final Calendar getCal( long t )
		{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t * MILLIS_PER_SEC);
		return cal;
		}

	public DateWalker(long[] aRange)
		{
		this(aRange[0], aRange[1], true);
		}

	public DateWalker(long[] aRange, boolean aSkipWeekends )
		{
		this(aRange[0], aRange[1], aSkipWeekends);
		}

	public DateWalker(String yyyymmdd0, String yyyymmdd1)
		{
		this(SBDate.toTime(yyyymmdd0 + "  09:30"),
		     SBDate.toTime(yyyymmdd1 + "  16:00"), true);
		}

	public DateWalker(long aTimeStart, long aTimeEnd, boolean aSkipWeekends )
		{
		fTimeStart = aTimeStart;
		fTimeEnd = aTimeEnd;
		fTimeCurrent = fTimeStart;
		m_skipWeekends = aSkipWeekends;
		}

	private long getTime(Calendar aCal) { 	return aCal.getTimeInMillis() / MILLIS_PER_SEC; }

	public final int size()
		{
		return list().size();
		}

	public final String[] array()
		{
		return list().toArray(new String[0]);
		}

	public final List<String> list()
		{
		List<String> list = new Vector<String>();
		Calendar c = Calendar.getInstance();
		long currentMillis = fTimeStart * MILLIS_PER_SEC;
		c.setTimeInMillis(currentMillis);
		int currentDOW = c.get(Calendar.DAY_OF_WEEK);
		long endMillis = (fTimeEnd * MILLIS_PER_SEC);
		while (currentMillis <= endMillis)
			{
			int yyyymmdd = yyyymmdd(c);
			if (!Holidays.isHoliday(yyyymmdd))
				list.add("" + yyyymmdd);
// else System.out.println("Skipped holiday " + yyyymmdd + " " + Holidays.fetch(yyyymmdd).name()
// + " " + SBDate.dayName[c.get(Calendar.DAY_OF_WEEK)]);
			int daysToAdd = (currentDOW == Calendar.FRIDAY)? 3 : 1;
			c.add(Calendar.DATE, daysToAdd);
			if (currentDOW != Calendar.FRIDAY) ++currentDOW;
			else currentDOW = Calendar.MONDAY;
			currentMillis = c.getTimeInMillis();
			}
		return list;
		}

	@Override public String toString()
		{
		return SBDate.mmddyy_hhmm(fTimeStart) + "-" + SBDate.mmddyy_hhmm(fTimeEnd);
		}

	public int yyyymmdd(Calendar aCal)
		{
		return 10000 * aCal.get(Calendar.YEAR)
				+   100 * (1 + aCal.get(Calendar.MONTH))
				+         aCal.get(Calendar.DAY_OF_MONTH);
		}

	public int yyyymmdd() { return yyyymmdd(getCal(fTimeCurrent)); }
	}
