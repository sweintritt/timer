package de.sweintritt.timer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void exitsAfterQuitCommand() throws IOException {
        runMain("quit\n");
    }

    @Test
    void exitsWhenInputEnds() throws IOException {
        runMain("");
    }

    @Test
    void handlesTimerExpiryWhileWaitingForInput() throws IOException {
        var scheduler = new ScheduledThreadPoolExecutor(1) {
            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                return super.schedule(command, 10, TimeUnit.MILLISECONDS);
            }
        };
        assertTrue(runMain("add fast 1m\n", true, scheduler).contains("Timer 'fast' is over!"));
    }

    private String runMain(String input) throws IOException {
        return runMain(input, false, null);
    }

    private String runMain(String input, boolean waitAtEnd, ScheduledThreadPoolExecutor scheduler) throws IOException {
        var originalInput = System.in;
        var originalOutput = System.out;
        var output = new ByteArrayOutputStream();
        var inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)) {
            private boolean waitedAtEnd;

            @Override
            public synchronized int read(byte[] buffer, int offset, int length) {
                if (waitAtEnd && available() == 0 && !waitedAtEnd) {
                    waitedAtEnd = true;
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return super.read(buffer, offset, length);
            }
        };
        try {
            System.setIn(inputStream);
            System.setOut(new PrintStream(output));
            if (scheduler == null) {
                Main.main(new String[0]);
            } else {
                Main.run(inputStream, scheduler);
            }
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
