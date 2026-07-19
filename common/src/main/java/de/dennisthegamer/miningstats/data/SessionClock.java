package de.dennisthegamer.miningstats.data;

/**
 * Stopwatch behind the session timer.
 *
 * <p>Time is banked into {@link #accumulatedMillis} whenever a run segment ends, so a paused clock
 * needs no wall-clock arithmetic at all and {@link #isActive()} is the single source of truth for
 * both the HUD pause glyph and whether the clock advances.
 *
 * <p>Deliberately free of Minecraft types so it can be exercised standalone.
 */
public class SessionClock {

    /** Time banked from run segments that have already ended. */
    private long accumulatedMillis = 0;
    /** Start of the current run segment; only meaningful while {@link #active}. */
    private long runStartTime = 0;
    private boolean active = false;

    /** Clock source; overridden in tests to drive time deterministically. */
    protected long now() {
        return System.currentTimeMillis();
    }

    /** Back to zero and paused. */
    public void reset() {
        accumulatedMillis = 0;
        runStartTime = 0;
        active = false;
    }

    /** Back to zero, but a running clock keeps running and a paused one stays paused. */
    public void resetKeepingRunState() {
        boolean wasActive = active;
        reset();
        if (wasActive) {
            start();
        }
    }

    public void start() {
        if (active) return;
        runStartTime = now();
        active = true;
    }

    public void pause() {
        if (!active) return;
        accumulatedMillis += now() - runStartTime;
        runStartTime = 0;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public long elapsedMillis() {
        long total = accumulatedMillis;
        if (active) {
            total += now() - runStartTime;
        }
        return Math.max(0, total);
    }

    /** Adopts a persisted duration and stays paused until the player resumes. */
    public void restore(long durationMillis) {
        reset();
        // Clamped: older builds could persist a negative duration.
        accumulatedMillis = Math.max(0, durationMillis);
    }
}
