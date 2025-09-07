package com.pwetutils.listener;

import org.lwjgl.openal.*;
import org.lwjgl.BufferUtils;
import net.minecraft.client.Minecraft;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class VideoHologramAudio {
    private static final int SAMPLE_RATE = 44100;
    private static final int NUM_BUFFERS = 4;
    private static final int FADE_SAMPLES = 128;

    private double x, y, z;
    private int source;
    private final Queue<Integer> availableBuffers = new LinkedList<>();
    private final Queue<Integer> queuedBuffers = new LinkedList<>();
    private Thread streamThread;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private float nextChunkTime = 0;
    private boolean hasAudio = false;
    private float audioDuration = 0;
    private boolean allChunksLoaded = false;
    private long lastBufferProcessedTime = 0;

    public VideoHologramAudio(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        initOpenAL();
        checkAudioAvailability();
    }

    private void initOpenAL() {
        try {
            source = AL10.alGenSources();

            AL10.alSource3f(source, AL10.AL_POSITION, (float)x, (float)y, (float)z);
            AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 5.0f);
            AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, 20.0f);
            AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 1.0f);

            for (int i = 0; i < NUM_BUFFERS; i++) {
                availableBuffers.offer(AL10.alGenBuffers());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAudioAvailability() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(VideoHologram.VIDEO_SERVER_URL + "/audio/info");
                String json = new String(url.openStream().readAllBytes());
                hasAudio = json.contains("\"hasAudio\":true");
                if (hasAudio && json.contains("\"duration\":")) {
                    String durStr = json.split("\"duration\":")[1].split(",")[0].split("}")[0];
                    audioDuration = Float.parseFloat(durStr);
                }
            } catch (Exception e) {
                hasAudio = false;
            }
        });
    }

    public void start() {
        if (!hasAudio || playing) return;
        playing = true;
        paused = false;
        nextChunkTime = 0;
        startAt(0);
        allChunksLoaded = false;
        lastBufferProcessedTime = 0;

        streamThread = new Thread(() -> {
            while (playing) {
                if (!paused) {
                    int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                    while (processed-- > 0) {
                        IntBuffer buffer = BufferUtils.createIntBuffer(1);
                        AL10.alSourceUnqueueBuffers(source, buffer);
                        availableBuffers.offer(buffer.get(0));
                        if (allChunksLoaded) {
                            lastBufferProcessedTime = System.currentTimeMillis();
                        }
                    }

                    if (!availableBuffers.isEmpty() && !allChunksLoaded) {
                        Integer buffer = availableBuffers.poll();
                        if (loadAndQueueChunk(buffer, nextChunkTime)) {
                            queuedBuffers.offer(buffer);
                            nextChunkTime += 1.0f;
                            if (nextChunkTime >= audioDuration) {
                                allChunksLoaded = true;
                            }
                        } else {
                            availableBuffers.offer(buffer);
                        }
                    }

                    int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
                    int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);

                    if (state != AL10.AL_PLAYING && queued > 0) {
                        AL10.alSourcePlay(source);
                    }

                    if (allChunksLoaded && queued == 0 && state != AL10.AL_PLAYING) {
                        if (lastBufferProcessedTime > 0 &&
                                System.currentTimeMillis() - lastBufferProcessedTime > 500) {
                            playing = false;
                        }
                    }
                }

                updateListenerPosition();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        streamThread.start();
    }

    private boolean loadAndQueueChunk(int buffer, float time) {
        if (time >= audioDuration) return false;

        try {
            URL url = new URL(VideoHologram.VIDEO_SERVER_URL + "/audio/chunk/" + time);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = url.openStream()) {
                byte[] buf = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buf)) != -1) {
                    baos.write(buf, 0, bytesRead);
                }
            }

            byte[] audioData = baos.toByteArray();
            if (audioData.length > 44) {
                int dataLength = audioData.length - 44;
                ByteBuffer rawBuffer = BufferUtils.createByteBuffer(dataLength);
                rawBuffer.put(audioData, 44, dataLength);
                rawBuffer.flip();

                ShortBuffer shortBuffer = rawBuffer.asShortBuffer();
                short[] samples = new short[shortBuffer.remaining()];
                shortBuffer.get(samples);

                boolean isFirstChunk = (time == 0);
                boolean isLastChunk = (time + 1.0f >= audioDuration);

                if (!isFirstChunk && !isLastChunk) {
                    int fadeInSamples = Math.min(FADE_SAMPLES * 2, samples.length / 8);
                    for (int i = 0; i < fadeInSamples; i += 2) {
                        float factor = (float)i / fadeInSamples;
                        factor = factor * factor;
                        samples[i] = (short)(samples[i] * factor);
                        samples[i + 1] = (short)(samples[i + 1] * factor);
                    }

                    int fadeOutSamples = Math.min(FADE_SAMPLES * 2, samples.length / 8);
                    int startFade = samples.length - fadeOutSamples;
                    for (int i = 0; i < fadeOutSamples; i += 2) {
                        float factor = 1.0f - ((float)i / fadeOutSamples);
                        factor = factor * factor;
                        samples[startFade + i] = (short)(samples[startFade + i] * factor);
                        samples[startFade + i + 1] = (short)(samples[startFade + i + 1] * factor);
                    }
                }

                ByteBuffer processedBuffer = BufferUtils.createByteBuffer(samples.length * 2);
                for (short sample : samples) {
                    processedBuffer.put((byte)(sample & 0xFF));
                    processedBuffer.put((byte)((sample >> 8) & 0xFF));
                }
                processedBuffer.flip();

                AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, processedBuffer, SAMPLE_RATE);
                IntBuffer buf = BufferUtils.createIntBuffer(1);
                buf.put(buffer).flip();
                AL10.alSourceQueueBuffers(source, buf);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private void updateListenerPosition() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        AL10.alListener3f(AL10.AL_POSITION,
                (float)mc.thePlayer.posX,
                (float)mc.thePlayer.posY,
                (float)mc.thePlayer.posZ);

        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        float lookX = (float)(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float lookY = (float)(-Math.sin(Math.toRadians(pitch)));
        float lookZ = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        FloatBuffer orientation = BufferUtils.createFloatBuffer(6);
        orientation.put(new float[] {lookX, lookY, lookZ, 0, 1, 0});
        orientation.flip();
        AL10.alListener(AL10.AL_ORIENTATION, orientation);
    }

    public boolean isPlaying() {
        return playing && !paused;
    }

    public void pause() {
        if (playing && !paused) {
            paused = true;
            AL10.alSourcePause(source);
        }
    }

    public void resume() {
        if (playing && paused) {
            paused = false;

            // check if we have buffers queued, if not, load some
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            if (queued == 0) {
                // need to queue some buffers first
                for (int i = 0; i < 2 && !availableBuffers.isEmpty(); i++) {
                    Integer buffer = availableBuffers.poll();
                    if (loadAndQueueChunk(buffer, nextChunkTime)) {
                        queuedBuffers.offer(buffer);
                        nextChunkTime += 1.0f;
                    } else {
                        availableBuffers.offer(buffer);
                        break;
                    }
                }
            }

            AL10.alSourcePlay(source);
        }
    }

    public void stop() {
        playing = false;
        paused = false;
        if (streamThread != null) {
            streamThread.interrupt();
        }
        AL10.alSourceStop(source);
        AL10.alSourcei(source, AL10.AL_BUFFER, AL10.AL_NONE);

        availableBuffers.addAll(queuedBuffers);
        queuedBuffers.clear();
    }

    public void cleanup() {
        stop();
        AL10.alDeleteSources(source);
        for (Integer buffer : availableBuffers) {
            AL10.alDeleteBuffers(buffer);
        }
        for (Integer buffer : queuedBuffers) {
            AL10.alDeleteBuffers(buffer);
        }
    }

    public void seekTo(float time) {
        if (!hasAudio) return;

        boolean wasPlaying = playing && !paused;
        boolean wasPaused = paused;

        // if we're seeking while paused, we need to handle it differently
        if (playing && paused) {
            // just update the position without stopping
            AL10.alSourceStop(source);
            AL10.alSourcei(source, AL10.AL_BUFFER, AL10.AL_NONE);

            availableBuffers.clear();
            availableBuffers.addAll(queuedBuffers);
            queuedBuffers.clear();
            for (int i = availableBuffers.size(); i < NUM_BUFFERS; i++) {
                availableBuffers.offer(AL10.alGenBuffers());
            }

            nextChunkTime = (float)Math.floor(time);
            allChunksLoaded = false;
            lastBufferProcessedTime = 0;

            // pre-load some buffers but don't play
            for (int i = 0; i < 2 && !availableBuffers.isEmpty(); i++) {
                Integer buffer = availableBuffers.poll();
                if (loadAndQueueChunk(buffer, nextChunkTime)) {
                    queuedBuffers.offer(buffer);
                    nextChunkTime += 1.0f;
                } else {
                    availableBuffers.offer(buffer);
                    break;
                }
            }
            // don't start playing since we're paused
            return;
        }

        // normal seek when playing
        stop();

        nextChunkTime = (float)Math.floor(time);
        allChunksLoaded = false;
        lastBufferProcessedTime = 0;

        availableBuffers.clear();
        queuedBuffers.clear();
        for (int i = 0; i < NUM_BUFFERS; i++) {
            availableBuffers.offer(AL10.alGenBuffers());
        }

        if (wasPlaying) {
            startAt(nextChunkTime);
        }
    }

    private void startAt(float startTime) {
        if (!hasAudio || playing) return;
        playing = true;
        paused = false;
        nextChunkTime = startTime;
        allChunksLoaded = false;
        lastBufferProcessedTime = 0;

        streamThread = new Thread(() -> {
            while (playing) {
                if (!paused) {
                    int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                    while (processed-- > 0) {
                        IntBuffer buffer = BufferUtils.createIntBuffer(1);
                        AL10.alSourceUnqueueBuffers(source, buffer);
                        availableBuffers.offer(buffer.get(0));
                        if (allChunksLoaded) {
                            lastBufferProcessedTime = System.currentTimeMillis();
                        }
                    }

                    if (!availableBuffers.isEmpty() && !allChunksLoaded) {
                        Integer buffer = availableBuffers.poll();
                        if (loadAndQueueChunk(buffer, nextChunkTime)) {
                            queuedBuffers.offer(buffer);
                            nextChunkTime += 1.0f;
                            if (nextChunkTime >= audioDuration) {
                                allChunksLoaded = true;
                            }
                        } else {
                            availableBuffers.offer(buffer);
                        }
                    }

                    int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
                    int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);

                    if (state != AL10.AL_PLAYING && queued > 0) {
                        AL10.alSourcePlay(source);
                    }

                    if (allChunksLoaded && queued == 0 && state != AL10.AL_PLAYING) {
                        if (lastBufferProcessedTime > 0 &&
                                System.currentTimeMillis() - lastBufferProcessedTime > 500) {
                            playing = false;
                        }
                    }
                }

                updateListenerPosition();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        streamThread.start();
    }

    public void updatePosition(double newX, double newY, double newZ) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        AL10.alSource3f(source, AL10.AL_POSITION, (float)newX, (float)newY, (float)newZ);
    }

    public void restart() {
        stop();
        start();
    }

    public void setVolume(float volume) {
        AL10.alSourcef(source, AL10.AL_GAIN, volume);
    }

    public void skipForward(float seconds) {
        seekTo((float)Math.floor(nextChunkTime) + seconds);
    }

    public void skipBackward(float seconds) {
        seekTo(Math.max(0, (float)Math.floor(nextChunkTime) - seconds - 1.0f));
    }
}