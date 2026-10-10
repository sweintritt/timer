package de.sweintritt.timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    private static final String PROMPT = "> ";

    public static void main(String[] args) throws IOException {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            var t = new Thread(r, "timer-scheduler");
            t.setDaemon(true);
            return t;
        });
        Object lock = new Object();
        var manager = new TimerManager(scheduler, Alarm.beep(), msg -> {
            synchronized (lock) {
                System.out.println("\r" + msg);
            }
        }, () -> {
            synchronized (lock) {
                System.out.print(PROMPT);
                System.out.flush();
            }
        });
        var processor = new CommandProcessor(manager, msg -> {
            synchronized (lock) {
                System.out.println(msg);
            }
        });
        var in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Timer. Commands: add <name> <time>h|m, list, stop <name|all>, quit");
        while (!processor.shouldQuit()) {
            synchronized (lock) {
                System.out.print(PROMPT);
                System.out.flush();
            }
            String line = in.readLine();
            if (line == null) {
                break;
            }
            processor.execute(line);
        }
        scheduler.shutdownNow();
    }
}
