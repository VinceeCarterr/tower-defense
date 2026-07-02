package com.eldev.td;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Textures are generated at runtime with Pixmap so the project has ZERO image
 * files to ship or lose. To use real art instead, drop PNGs in a folder and
 * load them with: new Texture(Gdx.files.internal("enemy.png"));
 */
public class Assets {
    public static Texture enemy;
    public static Texture tower;
    public static Texture projectile;

    public static void load() {
        enemy      = circle(32, new Color(0.90f, 0.20f, 0.20f, 1f)); // red
        tower      = square(48, new Color(0.20f, 0.45f, 0.90f, 1f)); // blue
        projectile = circle(12, new Color(1.00f, 0.90f, 0.20f, 1f)); // yellow
    }

    private static Texture circle(int size, Color color) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fillCircle(size / 2, size / 2, size / 2 - 1);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture square(int size, Color color) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    public static void dispose() {
        enemy.dispose();
        tower.dispose();
        projectile.dispose();
    }
}
