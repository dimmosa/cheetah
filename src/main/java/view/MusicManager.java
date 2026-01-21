package view;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;

public final class MusicManager {

    private static Clip current;
    private static String currentPath = null;

    private static boolean muted = false;
    private static float volume = 0.35f;

    private MusicManager() {}

    public static void playLoop(String resourcePath) {
        if (muted) return;
        if (resourcePath == null) return;

        // ✅ אם כבר מנגן את אותו קובץ — לא מתחילים מחדש
        if (resourcePath.equals(currentPath) && current != null && current.isRunning()) {
            return;
        }

        currentPath = resourcePath;
        stop(); // עוצר רק אם זה שיר אחר

        try {
            var url = MusicManager.class.getResource(resourcePath);
            if (url == null) {
                System.out.println("⚠️ Missing music: " + resourcePath);
                return;
            }

            try (var rawIn = new BufferedInputStream(url.openStream());
                 var rawAis = AudioSystem.getAudioInputStream(rawIn);
                 var pcmAis = toPcm16(rawAis)) {

                current = AudioSystem.getClip();
                current.open(pcmAis);
                applyVolume(current);
                current.loop(Clip.LOOP_CONTINUOUSLY);
                current.start();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Failed playing music: " + resourcePath);
            e.printStackTrace();
        }
    }

    private static AudioInputStream toPcm16(AudioInputStream ais) {
        AudioFormat base = ais.getFormat();

        float sampleRate = (base.getSampleRate() <= 0 || base.getSampleRate() == AudioSystem.NOT_SPECIFIED)
                ? 44100f
                : base.getSampleRate();

        int channels = (base.getChannels() <= 0 || base.getChannels() == AudioSystem.NOT_SPECIFIED)
                ? 2
                : base.getChannels();

        AudioFormat decoded = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channels,
                channels * 2,
                sampleRate,
                false
        );

        return AudioSystem.getAudioInputStream(decoded, ais);
    }

    public static void stop() {
        if (current != null) {
            try {
                current.stop();
                current.close();
            } catch (Exception ignored) {}
            current = null;
        }
    }

    public static void setMuted(boolean value) {
        muted = value;
        if (muted) stop();
    }

    public static void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        if (current != null) applyVolume(current);
    }

    private static void applyVolume(Clip clip) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float vv = Math.max(0.0001f, volume);
                float dB = (float) (20.0 * Math.log10(vv));
                dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                gain.setValue(dB);
            }
        } catch (Exception ignored) {}
    }
}
