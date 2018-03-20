package exception;

import lombok.Getter;
import lombok.Setter;

public class ParserException extends RuntimeException {

    @Getter
    @Setter
    private int lineNumber;
    @Getter
    @Setter
    private int linePosition;
    @Getter
    @Setter
    private String errorMsg;

    public ParserException(int lineNumber, int linePosition, String message) {
        super(String.format(
                "ParserError cause in line:[ %s ] position: [ %s ] \n Error Msg : %s",
                lineNumber,
                linePosition,
                message
        ));
        this.lineNumber = lineNumber;
        this.linePosition = linePosition;
        this.errorMsg = message;
    }
}
