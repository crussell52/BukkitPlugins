package crussell52.bukkit.escape;

/**
 * A custom Exception which identifies an error condition originating from Escape code.
 */
public class EscapeException extends Exception
{
    // TODO: Re-implement with an actual user-message field.
    // TODO: Consider constants for some or all user messages.

    /**
     * Creates a new instance with a given message.
     *
     * @param message A user-friendly error message representing the condition.
     */
    public EscapeException(String message)
    {
        super(message);
    }
}
