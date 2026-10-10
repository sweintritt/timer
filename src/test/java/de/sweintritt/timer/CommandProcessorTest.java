package de.sweintritt.timer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private ScheduledExecutorService scheduler;
    private final List<String> out = new ArrayList<>();
    private final AtomicInteger alarms = new AtomicInteger();
    private final AtomicInteger redraws = new AtomicInteger();
    private TimerManager manager;
    private CommandProcessor cmd;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        manager = new TimerManager(scheduler, alarms::incrementAndGet, m -> {
            synchronized (out) {
                out.add(m);
            }
        }, redraws::incrementAndGet);
        cmd = new CommandProcessor(manager, m -> {
            synchronized (out) {
                out.add(m);
            }
        });
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    private String last() {
        return out.get(out.size() - 1);
    }

    @Test
    void addAndList() {
        cmd.execute("add tee 3m");
        cmd.execute("add check-server 1h");
        out.clear();
        cmd.execute("list");
        assertEquals(List.of("tee (3m)", "check-server (1h)"), out);
    }

    @Test
    void listEmpty() {
        cmd.execute("list");
        assertEquals("No running timers", last());
    }

    @Test
    void helpListsCommandsWithParametersAndDescriptions() {
        cmd.execute("help");
        assertEquals(List.of(
                "Available commands:",
                "  add <name> <time><unit> - Add a timer (units: h, m)",
                "  list - List running timers",
                "  stop <name> | stop all - Stop one timer or all timers",
                "  help - Show available commands",
                "  quit - Quit when no timers are running"), out);
    }

    @Test
    void invalidUnitsAndTimes() {
        for (String t : new String[] {"3s", "3", "m", "abc", "0m", "-1m", "99999999999999999999m", "9223372036854775807h"}) {
            out.clear();
            cmd.execute("add x " + t);
            assertTrue(last().startsWith("Error"), t);
        }
        assertTrue(manager.isEmpty());
    }

    @Test
    void blankCommandIsIgnored() {
        cmd.execute("  ");
        assertTrue(out.isEmpty());
    }

    @Test
    void addUsageError() {
        cmd.execute("add tee");
        assertTrue(last().startsWith("Error"));
    }

    @Test
    void duplicateName() {
        cmd.execute("add tee 3m");
        cmd.execute("add tee 5m");
        assertTrue(last().contains("already exists"));
        assertEquals(1, manager.list().size());
    }

    @Test
    void unknownCommand() {
        cmd.execute("foo");
        assertTrue(last().startsWith("Error"));
    }

    @Test
    void stopByNameAndAll() {
        cmd.execute("add a 1m");
        cmd.execute("add b 1m");
        cmd.execute("stop a");
        assertEquals(List.of("b (1m)"), manager.list());
        cmd.execute("stop nope");
        assertTrue(last().startsWith("Error"));
        cmd.execute("stop all");
        assertTrue(manager.isEmpty());
    }

    @Test
    void stopRequiresOneArgument() {
        cmd.execute("stop");
        assertEquals("Error: usage: stop <name> | stop all", last());
    }

    @Test
    void quitBlockedWhileRunning() {
        cmd.execute("add a 1m");
        cmd.execute("quit");
        assertFalse(cmd.shouldQuit());
        assertTrue(out.stream().anyMatch(s -> s.contains("a (1m)")));
        cmd.execute("stop all");
        cmd.execute("quit");
        assertTrue(cmd.shouldQuit());
    }

    @Test
    void expiredTimerAlarmsOnceAndIsRemoved() throws Exception {
        assertTrue(manager.add("fast", "1ms", java.time.Duration.ofMillis(10)));
        long end = System.currentTimeMillis() + 5000;
        while (!manager.isEmpty() && System.currentTimeMillis() < end) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
        TimeUnit.MILLISECONDS.sleep(100);
        assertEquals(1, alarms.get());
        assertEquals(1, redraws.get());
        synchronized (out) {
            assertEquals("Timer 'fast' is over!", out.get(0));
        }
    }

    @Test
    void stoppedTimerDoesNotAlarm() throws Exception {
        manager.add("x", "1ms", java.time.Duration.ofMillis(50));
        assertTrue(manager.stop("x"));
        TimeUnit.MILLISECONDS.sleep(200);
        assertEquals(0, alarms.get());
    }
}
