package de.sweintritt.timer;

final class AlarmVolume {

    private volatile int value = 100;

    int get() {
        return value;
    }

    void set(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
        this.value = value;
    }
}
