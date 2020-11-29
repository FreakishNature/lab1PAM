package lab2.saga.classes;

import java.io.IOException;

public interface Processor {
    void process(String message) throws IOException;
}
