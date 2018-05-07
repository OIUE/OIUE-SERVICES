package org.oiue.service.driver.api;

public class DriverDataField {
	public final static String driverName = "driverName";
	public final static String type = "type";
	public final static String STATUS = "status";
	public final static String DESCRIPTION = "description";
	
	/**
	 * terminal key
	 */
	public final static String TERMINAL_SN = "terminal";
	
	/**
	 * track time, long, utc seconds
	 */
	public final static String TRACK_GPS_TIME = "gps_time";
	
	/*
	 * receive time, long, utc seconds
	 */
	public final static String TRACK_RECV_TIME = "recv_time";
	
	/**
	 * track longitude, double, degree
	 */
	public final static String TRACK_LONGITUDE = "longitude";
	
	/**
	 * track latitude, double, degree
	 */
	public final static String TRACK_LATITUDE = "latitude";
	
	/**
	 * terminal moving direction(heading), int, degree
	 */
	public final static String TRACK_HEADING = "heading";
	
	/**
	 * terminal moving speed, float, km/h
	 */
	public final static String TRACK_SPEED = "speed";
	
	/**
	 * track position geocodeing
	 */
	public final static String TRACK_GEOCODING = "geocoding";
	
	/**
	 * track status, int, [alarm byte, status byte] 0:normal; status byte 1:invalid; 2:warning; 3:alarm
	 */
	public final static int TRACK_STATUS_NORMAL = 0x0;
	public final static int TRACK_STATUS_INVALID = 0x1;
	public final static int TRACK_STATUS_WARNING = 0x2;
	public final static int TRACK_STATUS_ALARM = 0x3;
	
	public final static int TRACK_STATUS_NO_FIXED = 0x10;
	
	public final static int TRACK_STATUS_ANALYZE_SPEED = 0x100;
	public final static int TRACK_STATUS_ANALYZE_REGION = 0x200;
	public final static int TRACK_STATUS_ANALYZE_ROUTE = 0x400;
	public final static int TRACK_STATUS_ANALYZE_POINT = 0x800;
	
	public final static int TRACK_STATUS_ALARM_SPEED = 0x10000;
	public final static int TRACK_STATUS_ALARM_REGION = 0x20000;
	public final static int TRACK_STATUS_ALARM_ROUTE = 0x40000;
	public final static int TRACK_STATUS_ALARM_POINT = 0x80000;
	
	public final static int TRACK_STATUS_ALARM_HELP = 0x100000;
	public final static int TRACK_STATUS_ALARM_TOUCH = 0x200000;
	public final static int TRACK_STATUS_ALARM_SOS = 0x400000;
	public final static int TRACK_STATUS_ALARM_VOLTAGE = 0x800000;
	public final static int TRACK_STATUS_ALARM_POWER = 0x1000000;
	public final static int TRACK_STATUS_ALARM_BREAKDOWN = 0x2000000;
	
	public final static String TRACK_ANALYZE_REGION = "analyze_region";
	public final static String TRACK_ANALYZE_SPEED = "analyze_speed";
	public final static String TRACK_ANALYZE_ROUTE = "analyze_route";
	public final static String TRACK_ANALYZE_POINT = "analyze_point";
	
	/**
	 * terminal status flags
	 */
	public final static String TERMINAL_STATUS = "terminal_status";
	
	/**
	 * termanal status falgs description
	 */
	public final static String TERMINAL_STATUS_DESC = "terminal_status_desc";
	
	/**
	 * extend info prefix, to avert same data field name
	 */
	public final static String EXTEND_PERFIX = "ext_";
	
	public final static String TARGET_ID = "target_id";
	public final static String TARGET_NAME = "target_name";
	public final static String TARGET_COLOR = "target_color";
	public final static String CORP_ID = "corp_id";
	public final static String DRIVER_CODE = "driver_code";
	public final static String DRIVER_ID = "driver_id";
	
	/*
	 * terminal device identity
	 */
	public final static String TERMINAL_IDENTITY = "terminal_identity";
	public final static String IMSI_ADDRESS = "imsi_address";
	
	/*
	 * send json
	 */
	public final static String SEND_DATA_PARAMS = "send_params";
	
	public static int ERROR_COMMAND = 0xFF000001;// unknown command
	public static int ERROR_ARGUMENTS = 0xFF000002;// arguments count
	public static int ERROR_ARGUMENT = 0xFF000003;// argument format
	public static int ERROR_OFFLINE = 0xFF000004;// target offline
	public static int ERROR_SUPPORT = 0xFF000005;// command not support
	public static int ERROR_DRIVER = 0xFF000006;// driver not support
	public static int ERROR_TARGET = 0xFF000007;// target not registed
	public static int ERROR_DRIVERID = 0xFF000008;// driverid not registed
}
