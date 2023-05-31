package brainight.hrx.core.exceptions;

/**
 * Github: https://github.com/Brainight
 * @author Brainight
 */
public class HorrocruxException extends Exception{

    public HorrocruxException() {
    }

    public HorrocruxException(String message) {
        super(message);
    }

    public HorrocruxException(String message, Throwable cause) {
        super(message, cause);
    }

    public HorrocruxException(Throwable cause) {
        super(cause);
    }

    public HorrocruxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    
}
