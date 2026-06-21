package de.dennisthegamer.miningstats.hud;

public class HudEffects {

    private static int flashTicksRemaining = 0;
    private static int resetMessageTicksRemaining = 0;

    private static final int FLASH_DURATION = 10;  // 0.5 seconds
    private static final int RESET_MESSAGE_DURATION = 40;  // 2 seconds

    public static void triggerFlash() {
        flashTicksRemaining = FLASH_DURATION;
    }

    public static void triggerResetMessage() {
        resetMessageTicksRemaining = RESET_MESSAGE_DURATION;
    }

    public static void tick() {
        if (flashTicksRemaining > 0) flashTicksRemaining--;
        if (resetMessageTicksRemaining > 0) resetMessageTicksRemaining--;
    }

    public static boolean isFlashing() {
        return flashTicksRemaining > 0;
    }

    public static float getFlashAlpha() {
        return (float) flashTicksRemaining / FLASH_DURATION;
    }

    public static boolean isShowingResetMessage() {
        return resetMessageTicksRemaining > 0;
    }
}
