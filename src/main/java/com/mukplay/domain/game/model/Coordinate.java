package com.mukplay.domain.game.model;

/**
 * Server-authoritative logical coordinate system (0.0 ~ 100.0).
 * Decouples backend domain game logic from client-side pixel coordinates.
 */
public record Coordinate(double x, double y) {

    public static final double MIN_BOUND = 0.0;
    public static final double MAX_BOUND = 100.0;
    public static final double INITIAL_CENTER_X = 50.0;
    public static final double INITIAL_CENTER_Y = 50.0;

    public Coordinate {
        x = clamp(x);
        y = clamp(y);
    }

    public static double clamp(double value) {
        if (value < MIN_BOUND) {
            return MIN_BOUND;
        }
        if (value > MAX_BOUND) {
            return MAX_BOUND;
        }
        return value;
    }

    public static boolean isInBounds(double x, double y) {
        return x >= MIN_BOUND && x <= MAX_BOUND && y >= MIN_BOUND && y <= MAX_BOUND;
    }

    public Coordinate translate(double deltaX, double deltaY) {
        return new Coordinate(this.x + deltaX, this.y + deltaY);
    }

    public double distanceTo(Coordinate other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
