package com.wormtrader.almanac;

import java.sql.*;
import java.util.*;

import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBLog;

public final class DailyData
	{
	public static final String MODULE="DailyData";
	public static final byte CASH  =  0; // account net liquidation value in cents
	public static final byte GAIN  =  1; // change from previous day's m_cash in cents
	public static final byte CG    =  2; // encoded Vector Vest Color Guard
	public static final byte MTI   =  3; // Vector Vest market timing indicator * 100
	public static final byte VIX   =  4; // Volatility Index * 100
	public static final byte TY    =  5; // ten year bond yield in basis points
	public static final byte EURO  =  6; // USD/EURO in cents
	public static final byte YEN   =  7; // in cents
	public static final byte INDU  =  8; // Dow Jones Industrials Close in cents
	public static final byte INDP  =  9; // change from previous session's indu
	public static final byte NCMP  = 10; // NASDAQ composite in cents
	public static final byte R2000 = 11; // NASDAQ composite in cents
	public static final byte SPX   = 12; // S & P 500 close
	public static final byte ANYSE = 13; // NYSE advancing issues
	public static final byte DNYSE = 14; //  "   declining issues
	public static final byte UNYSE = 15; //  "   unchanged issues
	public static final byte HNYSE = 16; //  "   new highs
	public static final byte LNYSE = 17; //  "   new lows
	public static final byte VNYSE = 18; // NYSE volume in thousands of shares
	public static final byte ANASD = 19; // NASDAQ advancing issues
	public static final byte DNASD = 20; //  "   declining issues
	public static final byte UNASD = 21; //  "   unchanged issues
	public static final byte HNASD = 22; //  "   new highs
	public static final byte LNASD = 23; //  "   new lows
	public static final byte VNASD = 24; // NASDAQ volume in thousands of shares

	public static final String[] LABELS = new String[25];

	private int[] fields = new int[25];
	private static int[] min = new int[25];
	private static int[] max = new int[25];
	private final long   m_time;  // date/time seconds ala SBDate
	private final String m_notes; // financial headlines

	private static final String EMPTY_STRING = "";
	private static final String DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
	private static final String URL = "jdbc:odbc:MarketData";
	private static final String USERNAME = "";
	private static final String PASSWORD = "";

	private static Vector<DailyData> m_data = null;
	static public Vector<DailyData> loadAll()
		{
		SBLog.write ( MODULE + "." + "loadAll()" );
m_data = new Vector<DailyData>(500);
		for ( int i = 0; i < min.length; i++ )
			{
			min[i] = Integer.MAX_VALUE;
			max[i] = Integer.MIN_VALUE;
			}
		try
			{
			Class.forName(DRIVER);
			Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			String sql = "SELECT * FROM [DATA$]";
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns =  metaData.getColumnCount();
	//		for ( int j=1; j <= numberOfColumns; j++ )
	//			SBLog.write ( MODULE + "." + j + ") " + metaData.getColumnLabel(j));
			while(rs.next())
				{
				try { m_data.add( new DailyData( rs )); }
				catch (Exception ex)
					{
					SBLog.write ( MODULE + ".**" + ex.getMessage());
					break;
					}
				}
			statement.close();
			conn.close();
			}
		catch (Exception e)
			{
			SBLog.write ( MODULE + "." + "Exception: " + e.getMessage());
			}
		for ( byte f = MTI; f <= YEN; f++ )
			SBLog.format ( "%s.[%d] %d - %d\n", MODULE, f, getMin(f), getMax(f));
		int n = numDates(); // m_data.size();
		SBLog.format( "%s ready to go: %d dates %s - %s\n",
					MODULE, n, m_data.get(0).yyyymmdd(), m_data.get(n-1).yyyymmdd());
		return m_data;
		}

	public static Vector<DailyData> allDates() // { return m_data; }
	{ return (m_data == null) ? loadAll() : m_data; }
	public static int numDates() { return m_data.size(); }
	public static DailyData day(int i) { return m_data.get(i); }
	public static DailyData day(long time)
		{
		for ( DailyData d : m_data )
			if (d.m_time == time) return d;
		return null;
		}
	public static int getMin(byte field) { return min[field]; }
	public static int getMax(byte field) { return max[field]; }

	private DailyData( ResultSet rs ) throws Exception
		{
		String yyyymmdd = rs.getString(1);
		if ((yyyymmdd == null) || (yyyymmdd.length() < 8))
			throw new Exception( "Invalid date: '" + yyyymmdd + "'" ); 
		m_time = SBDate.toTime(yyyymmdd);
		if ( m_time > SBDate.today )
			throw new Exception( "Future date: '" + yyyymmdd + "'"  + m_time ); 

		int cash =  (int)(rs.getDouble(2) * 100);
		if ( cash == 0 ) // record not filled in yet
			throw new Exception( "Blank cash for: '" + yyyymmdd + "'");
		set( CASH, cash );
		set( GAIN, (int)(rs.getDouble(3) * 100));

		int cg  = (rs.getByte(4) & 0x000f); cg <<= 4; // cgprc
		    cg |= (rs.getByte(5) & 0x000f); cg <<= 4; // cgrt
		    cg |= (rs.getByte(6) & 0x000f);           // cgbr;
		fields[CG] = cg;
		set( MTI,   (int)(rs.getDouble(7) * 100));
		set( VIX,   (int)(rs.getDouble(8) * 100));
		set( TY,    (int)(rs.getDouble(9) * 1000));
		set( EURO,  (int)(rs.getDouble(10) * 100));
		set( YEN,   (int)(rs.getDouble(11) * 100));
		set( INDU,  (int)(rs.getDouble(12) * 100));
		set( INDP,  (int)(rs.getDouble(13) * 100));
		set( NCMP,  (int)(rs.getDouble(14) * 100));
		set( R2000, (int)(rs.getDouble(15) * 100));
		set( SPX,   (int)(rs.getDouble(16) * 100));
		set( ANYSE, rs.getInt(17));
		set( DNYSE, rs.getInt(18));
		set( UNYSE, rs.getInt(19));
		set( HNYSE, rs.getInt(20));
		set( LNYSE, rs.getInt(21));
		set( VNYSE, (int)(rs.getLong(22) / 1000));
		set( ANASD, rs.getInt(23));
		set( DNASD, rs.getInt(24));
		set( UNASD, rs.getInt(25));
		set( HNASD, rs.getInt(26));
		set( LNASD, rs.getInt(27));
		set( VNASD, (int)(rs.getLong(28) / 1000));
		String notes = rs.getString(29);
		m_notes = ( notes == null ) ? EMPTY_STRING : notes;
		}

	private void  set(byte field, int value)
		{
		fields[field] = value;
		if ( value > max[field] ) max[field] = value;
		else if ( value < min[field] ) min[field] = value;
		}
	public int    get(byte field) { return fields[field]; }
	public long   getTime()       { return m_time; }
	public String getNews()       { return m_notes; }
	public String yyyymmdd()      { return SBDate.yyyymmdd(m_time); }
	public String getDollars(byte field) { return SBFormat.toDollarString(fields[field]); }

	public String toString()
		{
		return SBDate.mmddyy(m_time)
				+ " " + SBFormat.toDollarString(get(INDU))
				+ "(" + SBFormat.toDollarString(get(INDP)) + ")"
				+ " " + SBFormat.toDollarString(get(SPX))
				+ " " + SBFormat.toDollarString(get(NCMP))
				+ " " + SBFormat.toDollarString(get(CASH))
				+ " " + SBFormat.toDollarString(get(GAIN))
				+ " " + m_notes;
		}

	static public DailyData fetch( String yyyymmdd )
		{
//		SBLog.format( "%s.fetch(%s)...\n", MODULE, yyyymmdd );
		DailyData it = null;
		try
			{
			Class.forName(DRIVER);
			Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			String sql = "SELECT * FROM [DATA$] where yyyymmdd='" + yyyymmdd + "'";
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if ( rs.next())
				{
				try { it = new DailyData( rs ); }
				catch (Exception ex) { SBLog.format( "%s.fetch(%s): %s", MODULE, yyyymmdd, ex.getMessage()); }
				}
			statement.close();
			conn.close();
			}
		catch (Exception e) { SBLog.format( "%s.fetch(%s): %s", MODULE, yyyymmdd, e.getMessage()); }
		return it;
		}

	static public DailyData last()
		{
//		SBLog.format( "%s.fetch(%s)...\n", MODULE, yyyymmdd );
		DailyData it = null;
		String yyyymmdd = "";
		try
			{
			Class.forName(DRIVER);
			Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			String sql = "SELECT yyyymmdd FROM [DATA$] ORDER BY yyyymmdd DESC";
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if ( rs.next())
yyyymmdd = rs.getString(1);
/*********
				{
				try { yyyymmdd = rs.getString(1); }
				catch (Exception ex) { SBLog.format( "%s.last(%s): %s", MODULE, yyyymmdd, ex.getMessage()); }
				}
*********/
			statement.close();
			sql = "SELECT * FROM [DATA$] where yyyymmdd='" + yyyymmdd + "'";
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
			if ( rs.next())
				{
				try { it = new DailyData( rs ); }
				catch (Exception ex) { SBLog.format( "%s.last(%s): %s", MODULE, yyyymmdd, ex.getMessage()); }
				}
			statement.close();
			conn.close();
			}
		catch (Exception e) { SBLog.format( "%s.last(%s): %s", MODULE, yyyymmdd, e.getMessage()); }
		return it;
		}
	}
