package com.eldev.td;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MVP tower defense. One map, one enemy type, one tower type, 5 waves,
 * money economy and win/lose. Everything lives in this one class so the flow
 * (input -> update -> draw) is easy to follow. Split it up as you extend.
 */
public class TowerDefenseGame extends ApplicationAdapter {

    // --- rendering ---
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private final Vector3 touchWorld = new Vector3();

    // --- map ---
    private final List<Vector2> path = new ArrayList<Vector2>();
    private boolean[][] blocked;    // cells the path runs through (cannot build here)
    private boolean[][] occupied;   // cells that already hold a tower

    // --- entities ---
    private final List<Enemy> enemies = new ArrayList<Enemy>();
    private final List<Tower> towers = new ArrayList<Tower>();
    private final List<Projectile> projectiles = new ArrayList<Projectile>();

    // --- game state ---
    private int money, lives, wave;
    private boolean inIntermission;
    private float intermissionTimer;
    private int enemiesLeftToSpawn;
    private float spawnTimer;
    private boolean gameOver, win;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.WORLD_W, Constants.WORLD_H);
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();               // built-in font, no asset files needed
        font.getData().setScale(1.4f);

        Assets.load();
        buildPath();
        reset();
    }

    private void reset() {
        enemies.clear();
        towers.clear();
        projectiles.clear();
        occupied = new boolean[Constants.COLS][Constants.ROWS];
        money = Constants.START_MONEY;
        lives = Constants.START_LIVES;
        wave = 0;
        inIntermission = true;
        intermissionTimer = Constants.INTERMISSION;
        enemiesLeftToSpawn = 0;
        spawnTimer = 0;
        gameOver = false;
        win = false;
    }

    /** Waypoints in tile coords (col,row), row 0 = bottom. -1 / 20 sit off-screen. */
    private void buildPath() {
        int[][] wp = {
            {-1, 9}, {3, 9}, {3, 3}, {9, 3}, {9, 9}, {15, 9}, {15, 3}, {20, 3}
        };
        path.clear();
        for (int[] p : wp) path.add(tileCenter(p[0], p[1]));

        // Rasterize the axis-aligned segments so we know which cells are "road".
        blocked = new boolean[Constants.COLS][Constants.ROWS];
        for (int i = 0; i < wp.length - 1; i++) {
            int c = wp[i][0], r = wp[i][1];
            int c1 = wp[i + 1][0], r1 = wp[i + 1][1];
            int cs = Integer.compare(c1, c), rs = Integer.compare(r1, r);
            while (c != c1 || r != r1) {
                mark(c, r);
                if (c != c1) c += cs; else r += rs;
            }
            mark(c1, r1);
        }
    }

    private void mark(int c, int r) {
        if (c >= 0 && c < Constants.COLS && r >= 0 && r < Constants.ROWS) blocked[c][r] = true;
    }

    private Vector2 tileCenter(int col, int row) {
        return new Vector2(col * Constants.TILE + Constants.TILE / 2f,
                           row * Constants.TILE + Constants.TILE / 2f);
    }

    // ------------------------------------------------------------------ loop

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f); // clamp big hitches
        update(delta);
        draw();
    }

    private void update(float delta) {
        handleInput();
        if (gameOver || win) return;

        updateWaves(delta);

        for (Tower t : towers) t.update(delta, enemies, projectiles);

        for (Iterator<Projectile> it = projectiles.iterator(); it.hasNext();) {
            Projectile p = it.next();
            p.update(delta);
            if (p.done) it.remove();
        }

        for (Iterator<Enemy> it = enemies.iterator(); it.hasNext();) {
            Enemy e = it.next();
            e.update(delta);
            if (!e.alive) {                 // killed by a tower
                money += Constants.KILL_REWARD;
                it.remove();
            } else if (e.reachedEnd) {       // leaked past the end
                lives--;
                it.remove();
                if (lives <= 0) gameOver = true;
            }
        }
    }

    private void updateWaves(float delta) {
        if (inIntermission) {
            intermissionTimer -= delta;
            if (intermissionTimer <= 0 || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                inIntermission = false;
                enemiesLeftToSpawn = Constants.BASE_COUNT + wave * 2;
                spawnTimer = 0;
            }
        } else if (enemiesLeftToSpawn > 0) {
            spawnTimer -= delta;
            if (spawnTimer <= 0) {
                float hp = Constants.BASE_HP + wave * 2;
                float speed = Constants.BASE_SPEED + wave * 6;
                enemies.add(new Enemy(path, speed, hp));
                enemiesLeftToSpawn--;
                spawnTimer = Constants.SPAWN_INTERVAL;
            }
        } else if (enemies.isEmpty()) {       // wave fully cleared
            wave++;
            if (wave >= Constants.TOTAL_WAVES) {
                win = true;
            } else {
                inIntermission = true;
                intermissionTimer = Constants.INTERMISSION;
            }
        }
    }

    private void handleInput() {
        if (gameOver || win) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset();
            return;
        }
        if (Gdx.input.justTouched()) {
            touchWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchWorld);     // screen pixels -> world coords
            int col = (int) (touchWorld.x / Constants.TILE);
            int row = (int) (touchWorld.y / Constants.TILE);
            if (col < 0 || col >= Constants.COLS || row < 0 || row >= Constants.ROWS) return;
            if (blocked[col][row] || occupied[col][row]) return;   // road or taken
            if (money < Constants.TOWER_COST) return;              // too poor

            money -= Constants.TOWER_COST;
            occupied[col][row] = true;
            Vector2 c = tileCenter(col, row);
            towers.add(new Tower(c.x, c.y));
        }
    }

    // ------------------------------------------------------------------ draw

    private void draw() {
        Gdx.gl.glClearColor(0.24f, 0.45f, 0.24f, 1f);   // grass
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);;
        camera.update();

        // road tiles
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeType.Filled);
        shapes.setColor(0.55f, 0.42f, 0.28f, 1f);       // dirt
        for (int c = 0; c < Constants.COLS; c++)
            for (int r = 0; r < Constants.ROWS; r++)
                if (blocked[c][r])
                    shapes.rect(c * Constants.TILE, r * Constants.TILE, Constants.TILE, Constants.TILE);
        shapes.end();

        // sprites + HUD text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Tower t : towers)
            batch.draw(Assets.tower, t.pos.x - 24, t.pos.y - 24, 48, 48);
        for (Enemy e : enemies)
            batch.draw(Assets.enemy, e.pos.x - 16, e.pos.y - 16, 32, 32);
        for (Projectile p : projectiles)
            batch.draw(Assets.projectile, p.pos.x - 6, p.pos.y - 6, 12, 12);
        drawHud();
        batch.end();

        // enemy health bars
        shapes.begin(ShapeType.Filled);
        for (Enemy e : enemies) {
            float w = 32, h = 5, x = e.pos.x - w / 2, y = e.pos.y + 20;
            shapes.setColor(Color.DARK_GRAY);
            shapes.rect(x, y, w, h);
            shapes.setColor(Color.RED);
            shapes.rect(x, y, w * (e.hp / e.maxHp), h);
        }
        shapes.end();
    }

    private void drawHud() {
        font.setColor(Color.WHITE);
        float top = Constants.WORLD_H - 12;
        font.draw(batch, "Money: " + money, 16, top);
        font.draw(batch, "Lives: " + lives, 210, top);
        font.draw(batch, "Wave: " + Math.min(wave + 1, Constants.TOTAL_WAVES) + "/" + Constants.TOTAL_WAVES, 400, top);
        font.draw(batch, "Click a green tile to build (cost " + Constants.TOWER_COST + ")", 600, top);

        float cx = Constants.WORLD_W / 2f, cy = Constants.WORLD_H / 2f;
        if (inIntermission) {
            font.draw(batch, "Next wave in " + (int) Math.ceil(intermissionTimer)
                    + "s   -   press SPACE to start now", cx - 250, cy);
        }
        if (gameOver) font.draw(batch, "GAME OVER   -   press R to restart", cx - 190, cy);
        if (win)      font.draw(batch, "YOU WIN!   -   press R to play again", cx - 190, cy);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        Assets.dispose();
    }
}
