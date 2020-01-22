package bwem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

class Asserter {
    private boolean failOnError = true;
    private OutputStream outStream = System.err;

    void throwIllegalStateException(String message) {
        final IllegalStateException exception = new IllegalStateException(message);
        if (failOnError) {
            throw exception;
        }
        else {
            try {
                throw exception;
            } catch (IllegalStateException e) {
                if (outStream != null) {
                    try {
                        outStream.write(Arrays
                                .stream(e.getStackTrace())
                                .map(s -> s.toString() + "\n")
                                .collect(Collectors.joining())
                                .getBytes());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    void setFailOutputStream(OutputStream outputStream) {
        this.outStream = outputStream;
    }
}
