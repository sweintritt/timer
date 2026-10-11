package de.sweintritt.timer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class AlarmTest {

    @Test
    void soundHasNineBeepIntervalsAt980Hz() {
        float rate = 44100f;
        byte[] sound = AlarmSound.create(rate);

        assertEquals((int) (rate * 9), sound.length);
        for (int beep = 0; beep < 9; beep++) {
            int activeSample = (int) ((beep * 0.5 + 0.125) * rate) * 2;
            int silentSample = (int) ((beep * 0.5 + 0.4) * rate) * 2;
            assertNotEquals(0, sample(sound, activeSample));
            assertEquals(0, sample(sound, silentSample));
        }

        int positiveCrossings = 0;
        short previous = sample(sound, 0);
        for (int i = 1; i < (int) (rate * 0.3); i++) {
            short current = sample(sound, i * 2);
            if (previous <= 0 && current > 0) {
                positiveCrossings++;
            }
            previous = current;
        }
        assertEquals(294, positiveCrossings);
    }

    @Test
    void volumeScalesSamplesAndZeroIsSilent() {
        float rate = 44100f;
        byte[] full = AlarmSound.create(rate, 100);
        byte[] half = AlarmSound.create(rate, 50);
        byte[] silent = AlarmSound.create(rate, 0);
        int sampleIndex = (int) (0.125 * rate) * 2;

        assertEquals(sample(full, sampleIndex) / 2, sample(half, sampleIndex));
        for (byte value : silent) {
            assertEquals(0, value);
        }
    }

    @Test
    void beepPlaysOrFallsBackWhenAudioIsUnavailable() {
        assertDoesNotThrow(Alarm.beep()::play);
    }

    @Test
    void silentAlarmDoesNotUseTerminalBellFallback() {
        var originalOutput = System.out;
        var output = new ByteArrayOutputStream();
        var volume = new AlarmVolume();
        volume.set(0);
        try {
            System.setOut(new PrintStream(output));
            Alarm.beep(volume).play();
        } finally {
            System.setOut(originalOutput);
        }
        assertFalse(output.toString().contains("\007"));
    }

    private short sample(byte[] sound, int byteIndex) {
        return (short) ((sound[byteIndex + 1] << 8) | (sound[byteIndex] & 0xff));
    }
}
