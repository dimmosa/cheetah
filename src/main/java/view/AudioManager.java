package view;

import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class AudioManager {
	// ---------- Background music ----------
	private static Clip bgClip = null;
	private static float bgBaseVolume = 0.55f;      // ווליום רגיל לרקע
	private static float bgDuckedVolume = 0.08f;    // ווליום בזמן SFX חשוב (נמוך מאוד)
	private static int duckMs = 650;                // כמה זמן נשאר נמוך
	private static volatile int duckToken = 0;      // כדי לא להחזיר מוקדם אם יש כמה SFX ברצף


    public enum Sfx {
        BOOM,
        FLAG_RIGHT,
        FLAG_WRONG,
        GOOD_SURPRISE,
        BAD_SURPRISE,
        QUESTION_OPEN,
        ANSWER_RIGHT,
        ANSWER_WRONG,
        WIN,
        GAME_OVER
    }

    private static final Map<Sfx, String> paths = new EnumMap<>(Sfx.class);
    private static final Map<Sfx, Clip> wavClips = new EnumMap<>(Sfx.class);

    private static boolean muted = false;
    private static float wavVolume = 0.85f;

    private AudioManager() {}

    // ✅ call once at startup
    public static void init() {
        // שימי כאן את מה שיש לך באמת בתיקייה (wav/mp3)
        register(Sfx.BOOM,          "/sfx/boom.wav");
        register(Sfx.FLAG_RIGHT,    "/sfx/flag_right.mp3");
        register(Sfx.FLAG_WRONG,    "/sfx/flag_wrong.wav");
        register(Sfx.GOOD_SURPRISE, "/sfx/good_surprise.mp3");
        register(Sfx.BAD_SURPRISE,  "/sfx/bad_surprise.wav");
        register(Sfx.QUESTION_OPEN, "/sfx/question_open.wav");
        register(Sfx.ANSWER_RIGHT,  "/sfx/answer_right.mp3");
        register(Sfx.ANSWER_WRONG,  "/sfx/answer_wrong.wav");

        // אם עדיין אין לך קבצים — או תוסיפי אותם או תשני ל-mp3 שיש לך
        register(Sfx.WIN,       "/sfx/win.mp3");       // או win.wav
        register(Sfx.GAME_OVER, "/sfx/game_over.wav"); // או game_over.wav

        // preload only WAV files into Clip (fast)
        for (var e : paths.entrySet()) {
            String p = e.getValue();
            if (p != null && p.toLowerCase().endsWith(".wav")) {
                wavClips.put(e.getKey(), loadWavClip(p));
            }
        }
    }

    public static void play(Sfx sfx) {
        if (muted) return;
        if (isImportant(sfx)) duckBackground();

        String path = paths.get(sfx);
        if (path == null) {
            System.out.println("⚠️ Missing SFX mapping for: " + sfx);
            return;
        }

        if (path.toLowerCase().endsWith(".wav")) {
            playWav(sfx);
        } else if (path.toLowerCase().endsWith(".mp3")) {
            playMp3(path);
        } else {
            System.out.println("⚠️ Unsupported audio format: " + path);
        }
    }

    public static void setMuted(boolean value) {
        muted = value;
        if (muted) {
            // stop wav clips
            for (Clip c : wavClips.values()) {
                try { if (c != null && c.isRunning()) c.stop(); } catch (Exception ignored) {}
            }
        }
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setWavVolume(float v) {
        wavVolume = Math.max(0f, Math.min(1f, v));
    }

    // ---------------- WAV ----------------
    private static void register(Sfx sfx, String path) {
        paths.put(sfx, path);
        // warn if not found
        if (AudioManager.class.getResource(path) == null) {
            System.out.println("⚠️ Missing file in resources: " + path);
        }
    }

    private static void playWav(Sfx sfx) {
        Clip clip = wavClips.get(sfx);
        if (clip == null) {
            // load on demand
            String path = paths.get(sfx);
            clip = loadWavClip(path);
            if (clip != null) wavClips.put(sfx, clip);
        }
        if (clip == null) return;

        try {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            applyVolume(clip, wavVolume);
            clip.start();
        } catch (Exception e) {
            System.out.println("⚠️ Failed playing WAV: " + sfx);
            e.printStackTrace();
        }
    }

    private static Clip loadWavClip(String path) {
        try {
            var url = AudioManager.class.getResource(path);
            if (url == null) {
                System.out.println("⚠️ Missing WAV: " + path);
                return null;
            }

            // verify WAV header: "RIFF....WAVE"
            try (InputStream checkIn = new BufferedInputStream(url.openStream())) {
                byte[] hdr = new byte[12];
                int n = checkIn.read(hdr);
                if (n < 12 ||
                    hdr[0] != 'R' || hdr[1] != 'I' || hdr[2] != 'F' || hdr[3] != 'F' ||
                    hdr[8] != 'W' || hdr[9] != 'A' || hdr[10] != 'V' || hdr[11] != 'E') {

                    System.out.println("⚠️ File is not a real WAV (RIFF/WAVE): " + path +
                            "  -> convert it to PCM WAV 16-bit.");
                    return null;
                }
            }

            // reopen fresh stream for actual decoding
            try (InputStream rawIn = new BufferedInputStream(url.openStream());
                 AudioInputStream rawAis = AudioSystem.getAudioInputStream(rawIn);
                 AudioInputStream pcmAis = toPcm16(rawAis)) {

                Clip clip = AudioSystem.getClip();
                clip.open(pcmAis);
                return clip;
            }

        } catch (Exception e) {
            System.out.println("⚠️ Failed loading WAV: " + path);
            e.printStackTrace();
            return null;
        }
    }
    private static AudioInputStream toPcm16(AudioInputStream ais) {
        AudioFormat base = ais.getFormat();

        float sr = (base.getSampleRate() <= 0 || base.getSampleRate() == AudioSystem.NOT_SPECIFIED)
                ? 44100f : base.getSampleRate();

        int ch = (base.getChannels() <= 0 || base.getChannels() == AudioSystem.NOT_SPECIFIED)
                ? 2 : base.getChannels();

        AudioFormat decoded = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sr,
                16,
                ch,
                ch * 2,
                sr,
                false
        );

        return AudioSystem.getAudioInputStream(decoded, ais);
    }

    private static void applyVolume(Clip clip, float volume01) {
        try {
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float v = Math.max(0.0001f, volume01);
            float dB = (float) (20.0 * Math.log10(v));
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));

            gain.setValue(dB);
        } catch (Exception ignored) {}
    }

    // ---------------- MP3 (JLayer) ----------------
    private static void playMp3(String path) {
        // JLayer הוא blocking -> נריץ בת'רד
        new Thread(() -> {
            try (InputStream in = AudioManager.class.getResourceAsStream(path)) {
                if (in == null) {
                    System.out.println("⚠️ Missing MP3: " + path);
                    return;
                }
                Player p = new Player(new BufferedInputStream(in));
                p.play();
            } catch (Exception e) {
                System.out.println("⚠️ Failed playing MP3: " + path);
                e.printStackTrace();
            }
        }, "mp3-sfx").start();
    }
    public static void playBackgroundLoop(String wavPath) {
        if (muted) return;

        stopBackground();

        Clip c = loadWavClip(wavPath);
        if (c == null) {
            System.out.println("⚠️ Failed loading BG: " + wavPath);
            return;
        }

        bgClip = c;
        applyVolume(bgClip, bgBaseVolume);
        bgClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgClip.start();
    }

    public static void stopBackground() {
        try {
            if (bgClip != null) {
                bgClip.stop();
                bgClip.close();
                bgClip = null;
            }
        } catch (Exception ignored) {}
    }

    public static void setBackgroundVolume(float v) {
        bgBaseVolume = Math.max(0f, Math.min(1f, v));
        if (bgClip != null) applyVolume(bgClip, bgBaseVolume);
    }
    private static void duckBackground() {
        if (bgClip == null) return;

        int myToken = ++duckToken;

        try {
            applyVolume(bgClip, bgDuckedVolume);
        } catch (Exception ignored) {}

        // להחזיר אחרי קצת זמן, רק אם לא היה עוד SFX חשוב באמצע
        new Thread(() -> {
            try { Thread.sleep(duckMs); } catch (InterruptedException ignored) {}
            if (myToken == duckToken && bgClip != null && !muted) {
                applyVolume(bgClip, bgBaseVolume);
            }
        }, "bg-duck").start();
    }
    private static boolean isImportant(Sfx sfx) {
        return switch (sfx) {
            case BOOM, FLAG_WRONG, BAD_SURPRISE, ANSWER_WRONG, GAME_OVER -> true;
            default -> false;
        };
    }
    private static final Map<Sfx, Clip> activeClips = new HashMap<>();

    public static void stop(Sfx sfx) {
        Clip clip = activeClips.get(sfx);
        if (clip != null) {
            clip.stop();
            clip.close();
            activeClips.remove(sfx);
        }
    }



}
