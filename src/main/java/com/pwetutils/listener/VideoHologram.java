package com.pwetutils.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class VideoHologram {
    public static final String VIDEO_SERVER_URL = "http://85.215.152.109:3008";
    private static final int FRAMES_BEHIND = 1;
    private static final int FRAMES_AHEAD = 3;

    private static final CopyOnWriteArrayList<VideoHologram> activeInstances = new CopyOnWriteArrayList<>();

    private double x, y, z;
    private final float width, height;
    private final int sizeLevel;
    private final Map<Integer, FrameData> frameCache = new HashMap<>();
    private final Map<Integer, CompletableFuture<Void>> loadingFrames = new HashMap<>();
    private DynamicTexture startScreenTexture = null;
    private DynamicTexture endScreenTexture = null;
    private ResourceLocation startScreenLocation = null;
    private ResourceLocation endScreenLocation = null;
    private int currentFrame = 1;
    private int totalFrames = 0;
    private long firstRenderTime = 0;
    private long lastFrameTime = 0;
    private long videoEndTime = 0;
    private int frameDelay = 100;
    private boolean metadataLoaded = false;
    private long creationTime;
    private boolean paused = false;
    private final boolean transparent;
    private VideoHologramAudio audio;

    private static class FrameData {
        final DynamicTexture texture;
        final ResourceLocation location;

        FrameData(DynamicTexture texture, ResourceLocation location) {
            this.texture = texture;
            this.location = location;
        }
    }

    private enum VideoState {
        WAITING,
        START_SCREEN,
        PLAYING,
        LAST_FRAME,
        END_SCREEN
    }
    private VideoState state = VideoState.WAITING;

    static {
        Thread globalMonitor = new Thread(() -> {
            boolean lastWorldState = true;

            while (true) {
                try {
                    Minecraft mc = Minecraft.getMinecraft();
                    boolean worldExists = mc != null && mc.theWorld != null;

                    if (!worldExists && lastWorldState) {
                        for (VideoHologram instance : activeInstances) {
                            if (!instance.paused) {
                                instance.paused = true;
                                if (instance.audio != null && instance.audio.isPlaying()) {
                                    instance.audio.pause();
                                }
                            }
                            instance.lastFrameTime = System.currentTimeMillis();
                        }

                    } else if (worldExists && !lastWorldState) {
                        for (VideoHologram instance : activeInstances) {
                            instance.lastFrameTime = System.currentTimeMillis();
                        }
                    }

                    lastWorldState = worldExists;
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        globalMonitor.setDaemon(true);
        globalMonitor.start();
    }

    public VideoHologram(double x, double y, double z, int sizeLevel, boolean transparent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeLevel = sizeLevel;
        float baseSize = 1.833f * sizeLevel;
        this.width = baseSize;
        this.height = baseSize * 0.6f;
        this.transparent = transparent;
        this.creationTime = System.currentTimeMillis();
        activeInstances.add(this);
        loadScreens();
        loadMetadata();
        this.audio = new VideoHologramAudio(x, y, z);
    }

    public void moveTo(double newX, double newY, double newZ) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        if (audio != null) {
            audio.updatePosition(newX, newY, newZ);
        }
    }

    private void updateAudioState() {
        if (audio == null) return;

        switch (state) {
            case PLAYING:
                if (!audio.isPlaying() && !paused) {
                    audio.start();
                }
                break;
            case LAST_FRAME:
            case END_SCREEN:
                audio.stop();
                break;
        }
    }

    public void pause() {
        paused = true;
        if (audio != null) audio.pause();
    }

    public void resume() {
        paused = false;
        lastFrameTime = System.currentTimeMillis();
        if (audio != null) audio.resume();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public int getSizeLevel() {
        return sizeLevel;
    }

    private void loadScreens() {
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage startImg = ImageIO.read(new URL(VIDEO_SERVER_URL + "/startscreen"));
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (startScreenTexture != null) {
                        startScreenTexture.deleteGlTexture();
                    }
                    startScreenTexture = new DynamicTexture(startImg);
                    startScreenLocation = Minecraft.getMinecraft().getTextureManager()
                            .getDynamicTextureLocation("video_startscreen", startScreenTexture);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                BufferedImage endImg = ImageIO.read(new URL(VIDEO_SERVER_URL + "/endscreen"));
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (endScreenTexture != null) {
                        endScreenTexture.deleteGlTexture();
                    }
                    endScreenTexture = new DynamicTexture(endImg);
                    endScreenLocation = Minecraft.getMinecraft().getTextureManager()
                            .getDynamicTextureLocation("video_endscreen", endScreenTexture);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadMetadata() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(VIDEO_SERVER_URL + "/info");
                String json = new String(url.openStream().readAllBytes());
                totalFrames = Integer.parseInt(json.split("\"frameCount\":")[1].split(",")[0]);
                frameDelay = 100;
                metadataLoaded = true;
                preloadFrames(1, Math.min(2, totalFrames));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void seekTo(float seconds) {
        if (state != VideoState.PLAYING) return;

        int targetFrame = (int)(seconds * 10) + 1;
        targetFrame = Math.max(1, Math.min(targetFrame, totalFrames));

        currentFrame = targetFrame;
        lastFrameTime = System.currentTimeMillis();

        final int finalTargetFrame = targetFrame;

        frameCache.entrySet().removeIf(entry -> {
            int frame = entry.getKey();
            if (frame < finalTargetFrame - FRAMES_BEHIND || frame > finalTargetFrame + FRAMES_AHEAD) {
                disposeFrame(entry.getValue());
                return true;
            }
            return false;
        });

        for (int i = finalTargetFrame - FRAMES_BEHIND; i <= Math.min(finalTargetFrame + FRAMES_AHEAD, totalFrames); i++) {
            if (i > 0) loadFrame(i);
        }

        if (audio != null) {
            audio.seekTo(seconds);
        }
    }

    public float getDuration() {
        if (!metadataLoaded || totalFrames == 0) return 0;
        return totalFrames / 10.0f;
    }

    private void loadFrame(int frame) {
        if (frame < 1 || frame > totalFrames) return;
        if (frameCache.containsKey(frame) || loadingFrames.containsKey(frame)) return;

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(VIDEO_SERVER_URL + "/frame/" + frame));
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(img);
                    ResourceLocation location = new ResourceLocation("video_frame_" + System.nanoTime());
                    Minecraft.getMinecraft().getTextureManager().loadTexture(location, texture);
                    frameCache.put(frame, new FrameData(texture, location));
                    loadingFrames.remove(frame);
                });
            } catch (Exception e) {
                loadingFrames.remove(frame);
                e.printStackTrace();
            }
        });
        loadingFrames.put(frame, future);
    }

    private void preloadFrames(int start, int count) {
        for (int i = start; i < start + count && i <= totalFrames; i++) {
            loadFrame(i);
        }
    }

    private void disposeFrame(FrameData frame) {
        if (frame != null && frame.texture != null) {
            frame.texture.deleteGlTexture();
            TextureManager tm = Minecraft.getMinecraft().getTextureManager();
            if (frame.location != null) {
                tm.deleteTexture(frame.location);
            }
        }
    }

    private void cleanupFrames(int currentFrame) {
        int minFrame = Math.max(1, currentFrame - FRAMES_BEHIND);
        int maxFrame = Math.min(totalFrames, currentFrame + FRAMES_AHEAD);

        frameCache.entrySet().removeIf(entry -> {
            int frame = entry.getKey();
            if (frame < minFrame || frame > maxFrame) {
                disposeFrame(entry.getValue());
                return true;
            }
            return false;
        });

        loadingFrames.entrySet().removeIf(entry -> {
            int frame = entry.getKey();
            if (frame < minFrame || frame > maxFrame) {
                entry.getValue().cancel(true);
                return true;
            }
            return false;
        });
    }

    public void cleanup() {
        activeInstances.remove(this);

        frameCache.values().forEach(this::disposeFrame);
        frameCache.clear();
        loadingFrames.values().forEach(f -> f.cancel(true));
        loadingFrames.clear();

        if (startScreenTexture != null) {
            startScreenTexture.deleteGlTexture();
            if (startScreenLocation != null) {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(startScreenLocation);
            }
        }

        if (endScreenTexture != null) {
            endScreenTexture.deleteGlTexture();
            if (endScreenLocation != null) {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(endScreenLocation);
            }
        }

        if (audio != null) {
            audio.cleanup();
            audio = null;
        }
    }

    public void skip(float seconds) {
        if (state != VideoState.PLAYING) return;

        int framesToSkip = (int)(seconds * 10);
        int newFrame = currentFrame + framesToSkip;

        newFrame = Math.max(1, Math.min(newFrame, totalFrames));

        if (newFrame == currentFrame) return;

        currentFrame = newFrame;
        lastFrameTime = System.currentTimeMillis();

        final int targetFrame = newFrame;

        frameCache.entrySet().removeIf(entry -> {
            int frame = entry.getKey();
            if (frame < targetFrame - FRAMES_BEHIND || frame > targetFrame + FRAMES_AHEAD) {
                disposeFrame(entry.getValue());
                return true;
            }
            return false;
        });

        for (int i = targetFrame - FRAMES_BEHIND; i <= Math.min(targetFrame + FRAMES_AHEAD, totalFrames); i++) {
            if (i > 0) loadFrame(i);
        }

        if (audio != null) {
            float newTime = (targetFrame - 1) / 10.0f;
            audio.seekTo(newTime);
        }
    }

    public float getProgress() {
        if (!metadataLoaded || totalFrames == 0) return 0;
        if (state == VideoState.END_SCREEN) return 1.0f;
        if (state == VideoState.WAITING || state == VideoState.START_SCREEN) return 0;
        return Math.min(1.0f, (float)currentFrame / totalFrames);
    }

    public ResourceLocation getCurrentTexture() {
        if (!metadataLoaded || totalFrames == 0) {
            return null;
        }

        long now = System.currentTimeMillis();

        switch (state) {
            case WAITING:
                if (startScreenLocation != null) {
                    state = VideoState.START_SCREEN;
                    firstRenderTime = now;
                    return startScreenLocation;
                }
                if (now - creationTime > 3000 && frameCache.containsKey(1)) {
                    state = VideoState.PLAYING;
                    lastFrameTime = now;
                    currentFrame = 1;
                    updateAudioState();
                    return frameCache.get(1).location;
                }
                return null;

            case START_SCREEN:
                if (now - firstRenderTime < 2000) {
                    return startScreenLocation;
                }
                if (frameCache.containsKey(1)) {
                    state = VideoState.PLAYING;
                    lastFrameTime = now;
                    currentFrame = 1;
                    updateAudioState();
                    return frameCache.get(1).location;
                }
                return startScreenLocation;

            case PLAYING:
                if (!paused && now - lastFrameTime >= frameDelay) {
                    int framesToAdvance = (int)((now - lastFrameTime) / frameDelay);
                    currentFrame = Math.min(currentFrame + framesToAdvance, totalFrames);
                    lastFrameTime = now;

                    if (currentFrame >= totalFrames) {
                        state = VideoState.LAST_FRAME;
                        videoEndTime = now;
                        updateAudioState();
                    } else {
                        for (int i = currentFrame + 1; i <= Math.min(currentFrame + FRAMES_AHEAD, totalFrames); i++) {
                            loadFrame(i);
                        }
                        cleanupFrames(currentFrame);
                    }
                } else if (paused) {
                    lastFrameTime = now;
                }

                for (int i = currentFrame; i >= Math.max(1, currentFrame - FRAMES_BEHIND); i--) {
                    FrameData frame = frameCache.get(i);
                    if (frame != null) return frame.location;
                }
                return null;

            case LAST_FRAME:
                if (now - videoEndTime < 2000) {
                    FrameData lastFrame = frameCache.get(totalFrames);
                    if (lastFrame == null) {
                        for (int i = totalFrames; i > Math.max(1, totalFrames - 3); i--) {
                            lastFrame = frameCache.get(i);
                            if (lastFrame != null) break;
                        }
                    }
                    return lastFrame != null ? lastFrame.location : null;
                }
                state = VideoState.END_SCREEN;
                updateAudioState();
                frameCache.values().forEach(this::disposeFrame);
                frameCache.clear();

            case END_SCREEN:
                return endScreenLocation;

            default:
                return null;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}