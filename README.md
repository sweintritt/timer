# ⏱️ timer

A small console timer for keeping track of several things at once. Start a timer for your tea, a server check, or anything else, then carry on working—the app will let you know when each one is up. ☕

## See it in action

```text
$ java -jar target/timer.jar
Timer. Type 'help' for available commands.
> add tea 3m
Added timer 'tea' (3m)
> add check-server 1h
Added timer 'check-server' (1h)
> list
 - tea - 00:03 remaining
 - check-server - 01:00 remaining
> stop check-server
Stopped timer 'check-server'
Timer 'tea' is over!
```

Timers count down independently, and the app prints the timer name and plays an alarm when one ends. Use `help` at any time to see the available commands.

## 🚀 Get started

### Requirements
Java 25 and Maven.

### Build
```
mvn package
```
This creates the single runnable jar `target/timer.jar` (no runtime dependencies; JUnit is used for tests only).

### Run
```
java -jar target/timer.jar
```
Enter commands at the `>` prompt.

## 🧰 Commands
| Command | Description |
|---|---|
| `add <name> <time><unit>` | Add a timer, e.g. `add tee 3m`, `add check-server 1h`. Units: `h`, `m`. Names must be unique. |
| `list` | List running timers with their remaining time in hours and minutes. |
| `stop <name>` | Stop the timer with the given name. |
| `stop all` | Stop all timers. |
| `help` | List available commands with their parameters and descriptions. |
| `quit` | Quit. Refused (running timers are printed) while timers are running; use `stop` first. |

Invalid commands, missing/unsupported units, duplicate names and unknown timer names print an error message.

## 🔔 Alarm
When a timer ends, its name is printed, then an alarm sound (nine 980 Hz beeps, synthesized in code, so no audio file or license is needed) is played once and the timer is removed. If no audio device is available the terminal bell is used. The prompt is redrawn after every command and every finished timer.
