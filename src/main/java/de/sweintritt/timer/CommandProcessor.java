package de.sweintritt.timer;

import java.time.Duration;
import java.util.regex.Pattern;

/** Parses and executes user commands. */
public class CommandProcessor {

    private static final Pattern TIME = Pattern.compile("(\\d+)([a-zA-Z]*)");

    private final TimerManager manager;
    private final java.util.function.Consumer<String> output;
    private boolean quit;

    public CommandProcessor(TimerManager manager, java.util.function.Consumer<String> output) {
        this.manager = manager;
        this.output = output;
    }

    public boolean shouldQuit() {
        return quit;
    }

    public void execute(String line) {
        var parts = line.trim().split("\\s+");
        if (parts[0].isEmpty()) {
            return;
        }
        switch (parts[0]) {
            case "add" -> add(parts);
            case "list" -> list();
            case "stop" -> stop(parts);
            case "help" -> help();
            case "quit" -> quit();
            default -> output.accept("Error: unknown command '" + parts[0] + "'. Commands: add, list, stop, help, quit");
        }
    }

    private void help() {
        output.accept("Available commands:");
        output.accept("  add <name> <time><unit> - Add a timer (units: h, m)");
        output.accept("  list - List running timers");
        output.accept("  stop <name> | stop all - Stop one timer or all timers");
        output.accept("  help - Show available commands");
        output.accept("  quit - Quit when no timers are running");
    }

    private void add(String[] parts) {
        if (parts.length != 3) {
            output.accept("Error: usage: add <name> <time><unit>, e.g. add tee 3m");
            return;
        }
        var name = parts[1];
        var m = TIME.matcher(parts[2]);
        if (!m.matches()) {
            output.accept("Error: invalid time '" + parts[2] + "'. Use <number><unit> with unit h or m");
            return;
        }
        if (m.group(2).isEmpty()) {
            output.accept("Error: missing unit in '" + parts[2] + "'. Supported units: h, m");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(m.group(1));
        } catch (NumberFormatException e) {
            output.accept("Error: invalid time '" + parts[2] + "'");
            return;
        }
        Duration duration;
        try {
            duration = switch (m.group(2)) {
                case "h" -> Duration.ofHours(amount);
                case "m" -> Duration.ofMinutes(amount);
                default -> null;
            };
        } catch (ArithmeticException e) {
            output.accept("Error: time '" + parts[2] + "' is too large");
            return;
        }
        if (duration == null) {
            output.accept("Error: unsupported unit '" + m.group(2) + "'. Supported units: h, m");
            return;
        }
        if (duration.isZero()) {
            output.accept("Error: time must be greater than zero");
            return;
        }
        if (!manager.add(name, parts[2], duration)) {
            output.accept("Error: a timer named '" + name + "' already exists");
            return;
        }
        output.accept("Added timer '" + name + "' (" + parts[2] + ")");
    }

    private void list() {
        var timers = manager.list();
        if (timers.isEmpty()) {
            output.accept("No running timers");
            return;
        }
        timers.forEach(t -> output.accept(t));
    }

    private void stop(String[] parts) {
        if (parts.length != 2) {
            output.accept("Error: usage: stop <name> | stop all");
            return;
        }
        if (parts[1].equals("all")) {
            manager.stopAll();
            output.accept("Stopped all timers");
        } else if (manager.stop(parts[1])) {
            output.accept("Stopped timer '" + parts[1] + "'");
        } else {
            output.accept("Error: no timer named '" + parts[1] + "'");
        }
    }

    private void quit() {
        if (manager.isEmpty()) {
            quit = true;
            return;
        }
        output.accept("Cannot quit, timers are still running (use 'stop'):");
        manager.list().forEach(t -> output.accept("  " + t));
    }
}
