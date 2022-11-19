package com.wormtrader.almanac;
/********************************************************************
* @(#)TabCalendar.java 1.00 20090901
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TabCalendar: Main frame and controlling logic for the Calendar.
*
* The W3C HTML and CSS standards list only 16 valid color names:
* aqua, black, blue, fuchsia, gray, green,
* lime, maroon, navy, olive, purple, red,
* silver, teal, white, and yellow
*
* @author Rick Salamone
* 20090901 - created
* 20091019 rts now a split with a calendar in the top and text in bottom
* 20120518 rts bug fixed using wrong calendar property name
* 20121012 rts use SplitPane instead of JSplitPane
* 20130308 rts shows holiday name
* 20130308 rts added setDate
* 20130319 rts remembers split and bug fix display first date events
*******************************************************/
import com.wormtrader.almanac.DailyData;
import com.wormtrader.almanac.RecurringEvent;
import com.shanebow.ui.calendar.MonthCalendar;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SBSelectable;
import com.shanebow.ui.SplitPane;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;
import java.awt.*;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import java.util.Vector;

public class TabCalendar
	extends JPanel
	implements PropertyChangeListener
	{
	public  static final Font FONT = new Font("SansSerif", Font.PLAIN, 12);
	private final static Color COLOR = new Color( 204, 204, 255 );

	protected MonthCalendar m_monthPanel;
	private JLabel lblEventsPanel = new JLabel();
	private String m_maxDate = null;
	private boolean m_displayCash = true;

	public TabCalendar()
		{
		this(SBDate.yyyymmdd());
		}

	public TabCalendar( String yyyymmdd )
		{
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5,5,0,5));

		m_monthPanel = new MonthCalendar();
		m_monthPanel.addPropertyChangeListener(MonthCalendar.TIMECHANGED_PROPERTY_NAME, this);
		m_monthPanel.setBackground(COLOR);

		lblEventsPanel.setOpaque(true);
		lblEventsPanel.setFont(FONT);
		lblEventsPanel.setVerticalAlignment(JLabel.TOP);
		lblEventsPanel.setBackground(Color.BLACK);
		lblEventsPanel.setForeground(COLOR);
		lblEventsPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

		SplitPane splitPane = new SplitPane(SplitPane.VSPLIT, m_monthPanel, 
		                                     new JScrollPane(lblEventsPanel),
		                                     "usr.almanac.split", 260);
		Dimension minimumSize = new Dimension(100, 50);
		m_monthPanel.setMinimumSize(minimumSize);
		lblEventsPanel.setMinimumSize(minimumSize);

		add(splitPane, BorderLayout.CENTER);
		setDate(yyyymmdd);
		eventsFor(yyyymmdd); // setDate above might not fire a change event
		}

	public void propertyChange(java.beans.PropertyChangeEvent e)
		{
		if ( !e.getPropertyName().equals(MonthCalendar.TIMECHANGED_PROPERTY_NAME))
			return;
		Object source = e.getSource();
		long newTime = ((Long)e.getNewValue()).longValue();
		String yyyymmdd = SBDate.yyyymmdd(newTime);
		setDate(yyyymmdd);
		if ( m_maxDate != null && yyyymmdd.compareTo(m_maxDate) > 0 )
			SBDialog.error("Not Permitted", "You cannot view news post " + m_maxDate );
		else
			eventsFor(yyyymmdd);
		}

	public void setDate(String yyyymmdd)
		{
		m_monthPanel.setDate(yyyymmdd);
		}

	public void setMaxDate(String yyyymmdd)
		{
		m_maxDate = yyyymmdd;
		if (m_maxDate != null)
			setDate(yyyymmdd);
		}

	public void setShowCash(boolean on) { m_displayCash = on; }

	private long m_time;
	private void eventsFor( String yyyymmdd )
		{
		int iyyyymmdd = Integer.parseInt(yyyymmdd);
		String html = "<html><h2>" + htmlSpaces(5) + yyyymmdd;
		if (Holidays.isHoliday(iyyyymmdd))
			html += " - " + Holidays.fetch(iyyyymmdd).name();
		html += "</h2>";
html += "Next holiday " + Holidays.nextHoliday(iyyyymmdd) + "<br>";

		m_time = SBDate.toTime(yyyymmdd);
		if ( m_time < SBDate.timeNow())
			{
			DailyData dd = DailyData.fetch(yyyymmdd);
			if ( dd != null )
				{
				html += htmlHeader(3, "Markets:");
				if ( m_displayCash )
					html += htmlListItem( ddDollarField(dd, DailyData.CASH, "$")
															+ ", " + htmlProfit(dd.get(DailyData.GAIN)));
				html += htmlListItem( ddDollarField(dd, DailyData.INDU, "DOW")
															+ ", " + htmlProfit(dd.get(DailyData.INDP)));
				html += htmlListItem( ddDollarField(dd, DailyData.SPX, " S&P")
												 + ddDollarField(dd, DailyData.NCMP, " NASDAQ")
												 + ddDollarField(dd, DailyData.R2000, " R2000"));
				html += htmlListItem( ddInternals(  dd, DailyData.ANYSE, "NYSE: " ));
				html += htmlListItem( ddInternals(  dd, DailyData.ANASD, "NASD: " ));
				html += htmlListItem( ddDollarField(dd, DailyData.TY, "TY")
												 + ddDollarField(dd, DailyData.EURO, " €/$")
												 + ddDollarField(dd, DailyData.YEN, " $/¥"));

				String news = dd.getNews();
				if ( !news.isEmpty())
					{
					html += htmlHeader(3, "Headlines:");
					for ( String headline : news.split(";"))
						html += htmlListItem( "fuchsia", headline );
					}
				}
			}

/*****
		int year = Integer.parseInt(yyyymmdd.substring(0,4));
		int month = Integer.parseInt(yyyymmdd.substring(4,6)) - 1;
		int dom = Integer.parseInt(yyyymmdd.substring(6,8));
*****/

		Vector<RecurringEvent> rEvents = RecurringEvent.eventsFor( yyyymmdd );
		if ( rEvents.size() > 0 )
			{
			html += htmlHeader(3, "Recuring Events:");
			for ( RecurringEvent recurring : rEvents )
				html += htmlListItem( "YELLOW", recurring.toString());
			}
		lblEventsPanel.setText( html );
		}

	private String ddInternals( DailyData dd, byte field, String label )
		{
		return label + "ADU "	+ htmlColored("AQUA", dd.get(field++))
									 + "." + htmlColored("fuchsia",  dd.get(field++))
									 + "." + htmlColored("YELLOW", dd.get(field++))
				         + " HL " + htmlColored("LIME", dd.get(field++))
					           + "." + htmlColored("RED",  dd.get(field));
		}

	private String ddDollarField( DailyData dd, byte field, String label )
		{
		return label + " " + htmlColored("AQUA", dd.getDollars(field));
		}

	private String htmlSpaces( int level )
		{
		String it = "";
		for ( int i = 0; i < level; i++ ) it += "&nbsp;";
		return it;
		}
	private String htmlHeader( int level, String text )
//		{ return "<h" + level + ">" + text + "</h" + level + ">"; }
		{ return "<b><i>" + text + "</i></b><br>"; }

	private String htmlListItem( String text )
//		{ return "<li>" + text + "</li>"; }
		{ return "&nbsp; * " + text + "<br>"; }

	private String htmlListItem( String color, String text )
		{ return htmlListItem( htmlColored( color, text )); }

	private String htmlColored( String color, String text )
		{ return "<font color=" + color + ">" + text + "</font>"; }

	private String htmlColored( String color, int value )
		{ return "<font color=" + color + ">" + value + "</font>"; }

	private String htmlProfit( int cents )
		{
		return htmlColored((cents < 0)? "RED" : "LIME", SBFormat.toDollarString(cents));
		}
	}
