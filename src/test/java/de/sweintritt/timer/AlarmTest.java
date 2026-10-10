package de.sweintritt.timer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AlarmTest {

    @Test
    void beepPlaysOrFallsBackWhenAudioIsUnavailable() {
        assertDoesNotThrow(Alarm.beep()::play);
    }
}
