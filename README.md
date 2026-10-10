# timer
Create multiple timers on the console to remember things.

## Requirements
Java 25 and Maven.

## Build
```
mvn package
```
This creates the single runnable jar `target/timer.jar` (no runtime dependencies; JUnit is used for tests only).

## Run
```
java -jar target/timer.jar
```
Enter commands at the `>` prompt.

## Commands
| Command | Description |
|---|---|
| `add <name> <time><unit>` | Add a timer, e.g. `add tee 3m`, `add check-server 1h`. Units: `h`, `m`. Names must be unique. |
| `list` | List running timers with their remaining time in hours and minutes. |
| `stop <name>` | Stop the timer with the given name. |
| `stop all` | Stop all timers. |
| `help` | List available commands with their parameters and descriptions. |
| `quit` | Quit. Refused (running timers are printed) while timers are running; use `stop` first. |

Invalid commands, missing/unsupported units, duplicate names and unknown timer names print an error message.

## Alarm
When a timer ends, its name is printed, then an alarm sound (three beeps, synthesized in code, so no audio file or license is needed) is played once and the timer is removed. If no audio device is available the terminal bell is used. The prompt is redrawn after every command and every finished timer.
