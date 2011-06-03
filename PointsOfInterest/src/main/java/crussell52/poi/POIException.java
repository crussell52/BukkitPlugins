package crussell52.PointsOfInterest;

@SuppressWarnings("serial")
public class POIException extends Exception {
	public static final int SYSTEM_ERROR = 0;
	public static final int TOO_CLOSE_TO_ANOTHER_POI = 1;
	public static final int NO_POI_AT_ID = 2;
	public static final int POI_OUT_OF_RANGE = 3;
	public static final int POI_OUT_OF_WORLD = 4;
	
	private int _errorCode;
	
	public POIException(int errorCode, String message)
	{
		super(message);
		_errorCode = errorCode;
	}
	
	public POIException(int errorCode, Throwable cause)
	{
		super(cause);
		_errorCode = errorCode;
	}
	
	public int getErrorCode()
	{
		return _errorCode;
	}
}
