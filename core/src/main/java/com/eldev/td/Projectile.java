package com.eldev.td;

import com.badlogic.gdx.math.Vector2;

/** A homing bullet. Flies at its target and applies damage on contact. */
public class Projectile {
    public Vector2 pos;
    public boolean done = false;

    private final Enemy target;
    private final float speed = 420f;
    private final float damage;
    private final Vector2 dir = new Vector2();

    public Projectile(Vector2 pos, Enemy target, float damage) {
        this.pos = pos;
        this.target = target;
        this.damage = damage;
    }

    public void update(float delta) {
        if (target == null || !target.alive) {   // target already gone
            done = true;
            return;
        }
        float step = speed * delta;
        if (pos.dst(target.pos) <= step + 10) {   // close enough = hit
            target.hp -= damage;
            if (target.hp <= 0) target.alive = false;
            done = true;
        } else {
            dir.set(target.pos).sub(pos).nor().scl(step);
            pos.add(dir);
        }
    }
}
