package org.opentradingsolutions.log4fix.importer;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Brian M. Coyner
 */
public class Importer {

    private final Thread producer;
    private final Thread consumer;

    public Importer(ImporterModel model, InputStream is) {
        BlockingQueue<String> fixMessages = new LinkedBlockingQueue<String>();
        producer = new Thread(new LogFileParser(is, fixMessages));
        consumer = new Thread(new LogMessageBuilder(model, fixMessages));
    }

    public void start() {

        producer.start();
        consumer.start();

        try {            
            consumer.join();
        } catch (InterruptedException e) {
            stop();
        }
    }

    public void stop() {
        producer.interrupt();
    }
}
