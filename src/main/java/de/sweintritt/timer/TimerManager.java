package de.sweintritt.timer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Holds the running timers and fires the alarm when one expires. */
public class TimerManager {

    private final ScheduledExecutorService scheduler;
    private final Alarm alarm;
    private final Consumer<String> output;
    private final Runnable redraw;
    private final Map<String, TimerEntry> timers = new LinkedHashMap<>();

    public TimerManager(ScheduledExecutorService scheduler, Alarm alarm, Consumer<String> output, Runnable redraw) {
        this.scheduler = scheduler;
        this.alarm = alarm;
        this.output = output;
        this.redraw = redraw;
    }

    /** Adds a timer; returns false if the name is already used. */
    public synchronized boolean add(String name, String label, Duration duration) {
        if (timers.containsKey(name)) {
            return false;
        }
        var future = scheduler.schedule(() -> expire(name), duration.toMillis(), TimeUnit.MILLISECONDS);
        timers.put(name, new TimerEntry(name, label, duration, future));
        return true;
    }

    private void expire(String name) {
        synchronized (this) {
            if (timers.remove(name) == null) {
                return;
            }
        }
        output.accept("Timer '" + name + "' is over!");
        alarm.play();
        redraw.run();
    }

    public synchronized boolean stop(String name) {
        var entry = timers.remove(name);
        if (entry == null) {
            return false;
        }
        entry.future().cancel(false);
        return true;
    }

    public synchronized void stopAll() {
        timers.values().forEach(t -> t.future().cancel(false));
        timers.clear();
    }

    /** Timer descriptions as "name (label)", in insertion order. */
    public synchronized List<String> list() {
        var result = new ArrayList<String>();
        timers.values().forEach(t -> result.add(t.name() + " (" + t.label() + ")"));
        return result;
    }

    /** Remaining timer durations as " - name - hh:mm remaining", in insertion order. */
    public synchronized List<String> listRemaining() {
        var result = new ArrayList<String>();
        timers.values().forEach(t -> {
            long remainingMillis = t.future().getDelay(TimeUnit.MILLISECONDS);
            long remainingMinutes = Math.max(1,
                    remainingMillis / 60_000 + (remainingMillis % 60_000 == 0 ? 0 : 1));
            result.add(" - " + t.name() + " - "
                    + String.format("%02d:%02d", remainingMinutes / 60, remainingMinutes % 60)
                    + " remaining");
        });
        return result;
    }

    public synchronized boolean isEmpty() {
        return timers.isEmpty();
    }
}
