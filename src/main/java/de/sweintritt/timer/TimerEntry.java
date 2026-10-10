package de.sweintritt.timer;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

record TimerEntry(String name, String label, Duration duration, ScheduledFuture<?> future) {
}
