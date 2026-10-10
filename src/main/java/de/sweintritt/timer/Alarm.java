package de.sweintritt.timer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

/** Plays an alarm sound once. The sound is synthesized, so no audio asset is needed. */
public interface Alarm {

    void play();

    /** Three short sine beeps; falls back to the terminal bell if no audio device is available. */
    static Alarm beep() {
        return () -> {
            try {
                float rate = 44100f;
                byte[] data = new byte[(int) (rate * 1.5)];
                for (int i = 0; i < data.length / 2; i++) {
                    double t = i / rate;
                    boolean on = (t % 0.5) < 0.3;
                    short v = (short) (on ? Math.sin(2 * Math.PI * 880 * t) * 12000 : 0);
                    data[2 * i] = (byte) v;
                    data[2 * i + 1] = (byte) (v >> 8);
                }
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(new AudioFormat(rate, 16, 1, true, false), data, 0, data.length);
                clip.start();
            } catch (Exception | Error e) {
                System.out.print("\007");
                System.out.flush();
            }
        };
    }
}
