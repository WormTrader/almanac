package com.wormtrader.almanac;
/********************************************************************
* @(#)Holiday.java 1.00 20130308
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Holidays: Represents a list of holidays. Has methods for searching
* for a Holiday and persisting the list.
*
* @author Rick Salamone
* @version 2.00
* 20130308 rts created
* 20130603 rts data file stored in history root (either web or local)
*******************************************************/
import com.shanebow.web.host.HostFile;
import com.shanebow.util.SBArray;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import com.shanebow.util.TextFile;
import javax.swing.JTable;
import javax.swing.table.*;

public final class Holidays
	extends AbstractTableModel
	{
	public static final int COL_DATE = 0;
	public static final int COL_NAME = 1;
	private static final String MASTER="holidays.csv";
	private static final SBArray<Holiday> _master = new SBArray<Holiday>(20);
	private static String filespec(String fname)
		{
		String dir = SBProperties.get("tw.bar.file.root");
		return new java.io.File(dir, fname).getPath();
		}

	static // public int load( String filespec )
		{
		String filespec = filespec(MASTER);
		try
			{
//filespec = "http://wormtrader.local/mo/data/history/holidays.csv";
			log ("Loading holidays from " + filespec);
			TextFile.thaw( Holiday.class, HostFile.bufferedReader(filespec), _master, false );
			int size = _master.size();
			log ( "loaded: %d holidays", size );
			}
		catch (Exception e)
			{
			System.err.println(filespec + " Error: " + e.toString());
			}
		}

	public static int nextHoliday(int yyyymmdd)
		{
		int index = _master.binarySearch(yyyymmdd);
//		_master.dump("search " + yyyymmdd + " = " + index);
		if (index < 0) index = -index - 1;
		else ++index; // today is a holiday
		int size = _master.size();
		return (index < size)? _master.get(index).yyyymmdd()
		     : _master.get(size-1).yyyymmdd() + 10000; // create a dummy future holiday
		}

	public static boolean isHoliday(int yyyymmdd)
		{ return _master.binarySearch(yyyymmdd) >= 0; }

	public static Holiday fetch(int yyyymmdd)
		{ return _master.bsearch(yyyymmdd); }

	static final String[] columnNames = { "Date", "Name" };

	private static final int[] colWidths = { 20, 80 };
	private void initColumns( JTable table )
		{
		for ( int c = getColumnCount(); c-- > 0; )
			{
			TableColumn column = table.getColumnModel().getColumn(c);
			column.setPreferredWidth(colWidths[c]);
			}
		}

	public int getColumnCount() { return columnNames.length; }
	public String  getColumnName(int c) { return columnNames[c]; }
	public int getRowCount() { return _master.size(); }
	public boolean isCellEditable(int r, int c) { return false; }

	public Object getValueAt( int r, int c )
		{
		Holiday holiday = _master.get(r);
		switch (c)
			{
			case COL_DATE:   return holiday.yyyymmdd();
			case COL_NAME:   return holiday.name();
			}
		return null;
		}
	protected static void log(String fmt, Object... args) {
System.out.format(fmt+"\n", args); }
//com.shanebow.util.SBLog.format(fmt, args); }
	}
