package com.eldev.td;

import com.badlogic.gdx.math.Vector2;
import java.util.List;

/** Walks along the shared list of waypoints. Dies at 0 hp or leaks at the end. */
public class Enemy {
    public Vector2 pos = new Vector2();
    public float hp, maxHp;
    public boolean alive = true;
    public boolean reachedEnd = false;

    private final List<Vector2> path;
    private int targetIndex = 1;          // heading toward the 2nd waypoint first
    private final float speed;
    private final Vector2 dir = new Vector2();

    public Enemy(List<Vector2> path, float speed, float hp) {
        this.path = path;
        this.speed = speed;
        this.hp = hp;
        this.maxHp = hp;
        pos.set(path.get(0));
    }

    public void update(float delta) {
        if (targetIndex >= path.size()) {
            reachedEnd = true;
            return;
        }
        Vector2 target = path.get(targetIndex);
        float step = speed * delta;
        if (pos.dst(target) <= step) {
            pos.set(target);              // snap to waypoint, aim at the next one
            targetIndex++;
        } else {
            dir.set(target).sub(pos).nor().scl(step);
            pos.add(dir);
        }
    }
}
