package com.eldev.td;

import com.badlogic.gdx.math.Vector2;
import java.util.List;

/** Sits on a tile, targets the nearest enemy in range, fires on a cooldown. */
public class Tower {
    public Vector2 pos;

    private final float range = 2.5f * Constants.TILE;
    private final float fireRate = 1.2f;   // shots per second
    private final float damage = 1f;
    private float cooldown = 0f;

    public Tower(float x, float y) {
        pos = new Vector2(x, y);
    }

    public void update(float delta, List<Enemy> enemies, List<Projectile> projectiles) {
        cooldown -= delta;
        if (cooldown > 0) return;

        Enemy target = findTarget(enemies);
        if (target != null) {
            projectiles.add(new Projectile(pos.cpy(), target, damage));
            cooldown = 1f / fireRate;
        }
    }

    private Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        float bestDist = Float.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.alive) continue;
            float d = pos.dst(e.pos);
            if (d <= range && d < bestDist) {
                best = e;
                bestDist = d;
            }
        }
        return best;
    }
}
