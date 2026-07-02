package com.eldev.td;

/** All the tunable numbers live here so balancing is a one-file job. */
public class Constants {
    public static final int TILE = 64;                 // pixel size of one grid tile
    public static final int COLS = 20;                 // grid width  (20 * 64 = 1280)
    public static final int ROWS = 12;                 // grid height (12 * 64 = 768)
    public static final int WORLD_W = COLS * TILE;
    public static final int WORLD_H = ROWS * TILE;

    public static final int TOWER_COST = 40;
    public static final int KILL_REWARD = 8;
    public static final int START_MONEY = 100;
    public static final int START_LIVES = 20;

    public static final int TOTAL_WAVES = 5;
    public static final float INTERMISSION = 5f;       // seconds between waves
    public static final float SPAWN_INTERVAL = 0.8f;   // seconds between enemy spawns

    public static final int   BASE_COUNT = 5;          // enemies in wave 1
    public static final float BASE_HP = 3f;
    public static final float BASE_SPEED = 60f;        // pixels per second
}
