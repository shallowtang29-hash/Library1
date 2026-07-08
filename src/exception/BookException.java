package exception;

public class BookException extends RuntimeException {
    private String message;

    public BookException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
