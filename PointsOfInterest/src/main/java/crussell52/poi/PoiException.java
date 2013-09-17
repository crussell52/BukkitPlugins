package crussell52.poi;

/**
 * An Exception relating to an action involving a POI
 */
@SuppressWarnings("serial")
public class PoiException extends Exception {
    /**
     * Indicates a system-level error, the details of which
     * are meaningless to the player.
     */
    public static final int SYSTEM_ERROR = 0;

    /**
     * Indicates that a given POI is too close to another POI.
     *
     * This is typically used while trying to add a new POI.
     */
    public static final int TOO_CLOSE_TO_ANOTHER_POI = 1;

    /**
     * Indicates that a given id does not related to a POI.
     */
    public static final int NO_POI_AT_ID = 2;

    /**
     * Indicates that a POI is in a world other than the expected or allowed one.
     */
    public static final int POI_OUT_OF_WORLD = 4;

    /**
     * Indicates that a POI does not belong to a specific player.
     */
    public static final int POI_BELONGS_TO_SOMEONE_ELSE = 5;

    /**
     * Indicates that a POI has a name other than the one given or expected.
     */
    public static final int POI_NAME_MISMATCH = 6;

    /**
     * Indicates that adding a new POI would exceed the maximum allowed a given
     * player.
     */
    public static final int MAX_PLAYER_POI_EXCEEDED = 7;

    /**
     * Indicates that an invalid POI type was specified.
     */
    public static final int POI_INVALID_TYPE = 8;

    /**
     * Indicates that a player does not have permission to add a POI, but tried to.
     */
    public static final int POI_NO_ADD_PERMISSION = 9;

    /**
     * Indicates that a player does not have permission to use the specified POI type, but tried to.
     */
    public static final int POI_NO_TYPE_PERMISSION = 10;

    /**
     * The specific type of error related to this exception
     */
    private int _errorCode;

    /**
     * Creates a new Exception which includes an error code and message.
     *
     * @param errorCode
     * @param message
     */
    public PoiException(int errorCode, String message)
    {
        super(message);
        _errorCode = errorCode;
    }

    /**
     * Creates a new Exception that has an error code but no message.
     *
     * @param errorCode
     */
    public PoiException(int errorCode)
    {
        super();
        _errorCode = errorCode;
    }

    /**
     * Creates a new exception as the result of something else.
     *
     * @param errorCode
     * @param cause
     */
    public PoiException(int errorCode, Throwable cause)
    {
        super(cause);
        _errorCode = errorCode;
    }

    /**
     * Returns the specific type of error related to this exception.
     *
     * There are static class constants defined to represent available types.
     *
     * @return
     */
    public int getErrorCode()
    {
        return _errorCode;
    }
}
