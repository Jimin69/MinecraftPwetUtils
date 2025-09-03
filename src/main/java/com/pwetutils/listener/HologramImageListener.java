package com.pwetutils.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HologramImageListener {
    private static final List<Hologram> holograms = new ArrayList<>();
    private static VideoHologram currentVideoHologram = null;

    public void loadVideo(double x, double y, double z, int size, boolean transparent) {
        clearVideoHologram();
        currentVideoHologram = new VideoHologram(x, y, z, size, transparent);
    }

    public boolean togglePauseVideo() {
        if (currentVideoHologram != null) {
            if (currentVideoHologram.isPaused()) {
                currentVideoHologram.resume();
                return false;
            } else {
                currentVideoHologram.pause();
                return true;
            }
        }
        return false;
    }

    public boolean hasVideoHologram() {
        return currentVideoHologram != null;
    }

    public void restartVideo() {
        if (currentVideoHologram != null) {
            double x = currentVideoHologram.getX();
            double y = currentVideoHologram.getY();
            double z = currentVideoHologram.getZ();
            int size = currentVideoHologram.getSizeLevel();
            boolean transparent = currentVideoHologram.isTransparent();
            clearVideoHologram();
            currentVideoHologram = new VideoHologram(x, y, z, size, transparent);
        }
    }

    public void clearVideoHologram() {
        if (currentVideoHologram != null) {
            currentVideoHologram.cleanup();
            currentVideoHologram = null;
        }
    }

    public void loadImage(String url, double x, double y, double z) {
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage image = ImageIO.read(new URL(url));
                float aspectRatio = (float) image.getWidth() / image.getHeight();
                float imageHeight = 2.0f;
                float imageWidth = imageHeight * aspectRatio;

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation textureLocation = Minecraft.getMinecraft().getTextureManager()
                            .getDynamicTextureLocation("hologram_" + System.currentTimeMillis(), texture);
                    holograms.add(new Hologram(textureLocation, x, y, z, imageWidth, imageHeight));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void renderHologram(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        for (Hologram hologram : holograms) {
            renderSingleHologram(hologram, playerX, playerY, playerZ);
        }

        if (currentVideoHologram != null) {
            ResourceLocation texture = currentVideoHologram.getCurrentTexture();
            if (texture != null) {
                renderVideoHologram(currentVideoHologram, texture, playerX, playerY, playerZ);
            }
        }
    }

    private static void renderVideoHologram(VideoHologram video, ResourceLocation texture,
                                            double playerX, double playerY, double playerZ) {
        Minecraft mc = Minecraft.getMinecraft();

        double renderX = video.getX() - playerX;
        double renderY = video.getY() - playerY;
        double renderZ = video.getZ() - playerZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderX, renderY, renderZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        float alpha = video.isTransparent() ? 0.9F : 1.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.depthMask(false);

        mc.getTextureManager().bindTexture(texture);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float halfWidth = video.getWidth() / 2.0f;
        float halfHeight = video.getHeight() / 2.0f;

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-halfWidth, halfHeight, 0).tex(1, 0).endVertex();
        worldrenderer.pos(halfWidth, halfHeight, 0).tex(0, 0).endVertex();
        worldrenderer.pos(halfWidth, -halfHeight, 0).tex(0, 1).endVertex();
        worldrenderer.pos(-halfWidth, -halfHeight, 0).tex(1, 1).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void renderSingleHologram(Hologram hologram, double playerX, double playerY, double playerZ) {
        Minecraft mc = Minecraft.getMinecraft();

        double renderX = hologram.x - playerX;
        double renderY = hologram.y - playerY;
        double renderZ = hologram.z - playerZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderX, renderY, renderZ);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.depthMask(false);

        mc.getTextureManager().bindTexture(hologram.textureLocation);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        float halfWidth = hologram.width / 2.0f;
        float halfHeight = hologram.height / 2.0f;

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-halfWidth, halfHeight, 0).tex(1, 0).endVertex();
        worldrenderer.pos(halfWidth, halfHeight, 0).tex(0, 0).endVertex();
        worldrenderer.pos(halfWidth, -halfHeight, 0).tex(0, 1).endVertex();
        worldrenderer.pos(-halfWidth, -halfHeight, 0).tex(1, 1).endVertex();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public void clearHolograms() {
        holograms.clear();
        clearVideoHologram();
    }

    private static class Hologram {
        final ResourceLocation textureLocation;
        final double x, y, z;
        final float width, height;

        Hologram(ResourceLocation textureLocation, double x, double y, double z, float width, float height) {
            this.textureLocation = textureLocation;
            this.x = x;
            this.y = y;
            this.z = z;
            this.width = width;
            this.height = height;
        }
    }
}