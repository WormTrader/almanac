package com.wormtrader.almanac;
/********************************************************************
* @(#)RecurringEvent.java 1.00 20091019
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
*  RecurringEvent: Routines to track recurring events.
*
*  A RecurringEvent is a data structure to hold an event that recurs on a
*  monthly or weekly basis, like the Oil Inventories report that comes out
*  every wednesday, or the Case-Shiller Index which is published the last
*  tuesday of every month.
*
*  A program simply calls the static routine 'eventsFor( String yyyymmdd )'
*  to obtain a Vector of RecurringEvent for the given date, then iterates
*  over the elements. For instance, the following code prints out all the
*  events for Feb 7, 1959:
*
*			for ( RecurringEvent evt : RecurringEvent.eventsFor("19590207"))
*				System.out.println( evt.toString());
*
*  A recurring date currently consists of two parts, the ordinal and the
*  day code. So if m_ordinal = 2 and the m_day = SUNDAY the event occurs
*  on the second sunday of the month. In addition to the weekdays, m_day
*  can be one of the following:
*
*     DAY - day of month: with ordinal = 5 gives 5th day of month
*     BIZ - business day: with ordinal = 3 gives third biz day of month
*     OOA - on or about:  works like DAY, but if the date falls on a weekend,
*                         it will also return the event for the fri - mon
*                         that enclose the day.
*
*  When m_ordinal is zero, it means "every." So, an event that occurs every
*  thursday is indicated by m_ordinal = 0, and m_day = THURSAY.
*
*  When m_ordinal is negative, it counts back from end of month, so m_day = TUE and
*  m_ordinal = -1 indicates that the event recurrs on the last tuesday of the month.
*  If m_day is DAY and m_ordinal = -3, indicates and event that recurs on the 3rd last
*  day of month.
*
*      NEGATIVE values are NOT (yet) supported for m_ordinal if m_day = BIZ or OOA!
*
*  TODO:
*    1) support negative ordinals for m_day = BIZ
*    2) read events in from file
*    3) maybe add larger time fields, like quarter to handle recurring events
*       like last day of quarter, or 2nd friday of the quarter
*    4) add more fields to the recurring event itself to aid in formatting: things
*       like the time of the occurance, who does the event, web link, etc.
*
* @author Rick Salamone
* @version 2.00
* 20091019 rts created
* 20130308 rts documentation
*******************************************************/
import com.shanebow.util.SBDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

public final class RecurringEvent
	{
	private static final RecurringEvent[] recurringEvents =
		{
		new RecurringEvent( " 0, SAT, Update Goals & Vision" ),
		new RecurringEvent( " 0, SUN, Weekly Trading Plan" ),
		new RecurringEvent( " 0, SUN, Do Backups" ),
		new RecurringEvent( " 7, DOM, Lucky Seven" ), 
		new RecurringEvent( "-1, DOM, Last Day of Month" ), 
		new RecurringEvent( "-2, DOM, Second Last Day of Month" ), 
		new RecurringEvent( "-1, TUE, Case-Shiller Home Price Index" ),
		new RecurringEvent( "-1, TUE, CCI: Consumer Confidence Index" ),
		new RecurringEvent( " 0, TUE, Turnaround Tuesday" ),
		new RecurringEvent( " 0, TUE, Oil Inventories" ),
		new RecurringEvent( " 0, TUE, Jobless Claims Report" ),
		new RecurringEvent( " 0, TUE, Money Supply" ),
		new RecurringEvent( " 1, TUE, Same store sales" ),
		new RecurringEvent( " 3, TUE, Business Outlook Survey" ),
		new RecurringEvent( " 1, FRI, Labor Report: Employee Situation Report" ),
		new RecurringEvent( " -3, FRI, 10:00 Reuters Surveys of Consumers Prelim" ),
		new RecurringEvent( " 3, FRI, Options Expiration" ),
		new RecurringEvent( " -1, FRI, 10:00 Reuters Surveys of Consumers Final" ),

		new RecurringEvent( " 9, OOA, Wholesale Trade Report" ), 
		new RecurringEvent( "13, OOA, Retail Sales Report" ),
		new RecurringEvent( "15, OOA, CPI: Consumer Price Index" ),
		new RecurringEvent( "16, OOA, Industrial Production" ),
		new RecurringEvent( "17, OOA, Housing Starts" ),
		new RecurringEvent( "19, OOA, Trade Balance Report" ),
		new RecurringEvent( "20, OOA, Durable Goods Report" ),

		new RecurringEvent( " 1, BIZ, PMI: Purchasing Managers Index" ),
		new RecurringEvent( " 1, BIZ, Auto sales" ),
		new RecurringEvent( " 3, BIZ, ISM Non-Manufacturing Report" ),
		new RecurringEvent( " 5, BIZ, Wholesale Inventories" ),
		};

	private static final int DAY=0; // for m_day, e.g the 20th "day" of the month
	private static final int OOA=8; // for m_day, e.g "on or around" the 19th of the month
	private static final int BIZ=9; // for m_day, e.g the 3rd "business day" of the month
	private static final int EVERY=0; // for m_ordinal, e.g "every" thursday
	private static final int LAST=-1; // for m_ordinal, e.g "last" thursday in month
	static GregorianCalendar cal = new GregorianCalendar();

	public static Vector<RecurringEvent> eventsFor( String yyyymmdd )
		{
		Vector<RecurringEvent> v = new Vector<RecurringEvent>(4);
		int year = Integer.parseInt(yyyymmdd.substring(0,4));
		int month = Integer.parseInt(yyyymmdd.substring(4,6)) - 1;
		int dom = Integer.parseInt(yyyymmdd.substring(6,8));

		for ( RecurringEvent recurring : recurringEvents )
			if ( recurring.occursOn ( year, month, dom ))
				v.add( recurring );
		return v;
		}

	private int    m_ordinal; // 1st, 2nd, ... -1 = last
	private int    m_day; // day of week
	private String m_desc;

	public RecurringEvent( String csv )
		{
		String[] pieces = csv.split( ",", 3 );
		m_ordinal = Integer.parseInt( pieces[0].trim());
		m_day = dayNumber( pieces[1].trim());
// System.out.format( " %2d, %2d: %s\n", m_ordinal, m_day, csv );
		m_desc = pieces[2].trim();
		}

	private int dayNumber( String dayString ) // parse the day string
		{
		if ( dayString.charAt(0) == 'O' )
			return OOA; // on or around
		if ( dayString.charAt(0) == 'B' )
			return BIZ;
		String beginning = dayString.substring(0,2).toUpperCase();
		for ( int i = 1; i <= 7; i++ )
			if ( SBDate.dayName[i].startsWith(beginning))
				return i;
		return DAY;
		}

	public final boolean occursOn ( int year, int month, int dom ) // zero based month
		{
		if ((m_day == DAY) && (m_ordinal > 0 ))
			return m_ordinal == dom;

		cal.set ( Calendar.YEAR, year );
		cal.set ( Calendar.MONTH, month );

		if ((m_day == DAY) && (m_ordinal < 0)) // count back from month end
			return dom == (cal.getActualMaximum ( Calendar.DAY_OF_MONTH ) + 1 + m_ordinal);

		if ( m_day == BIZ ) // iterate thru dom skipping weekends
			{
//TODO: HANDLE NEGATIVE m_ordinal here ala above code
			int eventDom = 0;
			for ( int i = 0; i < m_ordinal; )
				{
				cal.set ( Calendar.DAY_OF_MONTH, ++eventDom );
				int dow = cal.get ( Calendar.DAY_OF_WEEK );
				if ( dow != Calendar.SATURDAY && dow != Calendar.SUNDAY )
					++i; // next ordinal
				}
		// System.out.println("biz day #" + m_ordinal + " is dom " + eventDom );
			return eventDom == dom;
			}
		if ( m_day == OOA ) // see if the on or about date falls on the weekend
			{                 // if so return this event for friday thru monday
			cal.set ( Calendar.DAY_OF_MONTH, m_ordinal );
			int dow = cal.get ( Calendar.DAY_OF_WEEK );
			if ( dow == Calendar.SATURDAY )
				return (dom >= (m_ordinal - 1)) && (dom <= (m_ordinal + 2));
			if ( dow == Calendar.SUNDAY )
				return (dom >= (m_ordinal - 2)) && (dom <= (m_ordinal + 1));
			return m_ordinal == dom;
			}
		else if ( m_ordinal == 0 )
			{
			cal.set ( Calendar.DAY_OF_MONTH, dom );
			return m_day == cal.get ( Calendar.DAY_OF_WEEK );
			}
		else
			{
			cal.set ( Calendar.DAY_OF_WEEK, m_day );
			cal.set ( Calendar.DAY_OF_WEEK_IN_MONTH, m_ordinal );
			return dom == cal.get ( Calendar.DAY_OF_MONTH );
			}
		}

	public String getDesc() { return m_desc; }
	public String toString() { return m_desc + "  ("
	     + ordinal() + " "
			+ ((m_day < 8) ? SBDate.dayName[m_day] : (m_day==OOA)? "OOA" : "biz day")
			+ ")"; }

	public String ordinal()
		{
		switch ( m_ordinal )
			{
			case 0:  return "every";
			case 1:  return "1st";
			case -1: return "last";
			case 2:  return "2nd";
			case -2: return "2nd last";
			case 3:  return "3rd";
			case -3:  return "3rd last";
			}
		if ( m_ordinal > 0 )
			return "" + m_ordinal + "th";
		return "" + (-m_ordinal) + "th last";
		}
	}
