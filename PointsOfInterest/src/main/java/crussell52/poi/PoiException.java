package crussell52.poi;

@SuppressWarnings("serial")
public class PoiException extends Exception {
	public static final int SYSTEM_ERROR = 0;
	public static final int TOO_CLOSE_TO_ANOTHER_POI = 1;
	public static final int NO_POI_AT_ID = 2;
	public static final int POI_OUT_OF_WORLD = 4;
	public static final int POI_BELONGS_TO_SOMEONE_ELSE = 5;
	public static final int POI_NAME_MISMATCH = 6;
	public static final int MAX_PLAYER_POI_EXCEEDED = 7;
	
	private int _errorCode;
	
	public PoiException(int errorCode, String message)
	{
		super(message);
		_errorCode = errorCode;
	}
	
	public PoiException(int errorCode)
	{
		super();
		_errorCode = errorCode;
	}
	
	public PoiException(int errorCode, Throwable cause)
	{
		super(cause);
		_errorCode = errorCode;
	}
	
	public int getErrorCode()
	{
		return _errorCode;
	}
}
