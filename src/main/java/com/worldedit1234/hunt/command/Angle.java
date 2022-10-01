package com.worldedit1234.hunt.command;

public class Angle {
    private final float degree;

    public Angle(float degree) {
        this.degree = toPositiveAngle(degree) % 360F;
    }

    public float getDegree() {
        return this.degree;
    }

    public float getRadian() {
        return (float) Math.toRadians(this.degree);
    }

    public Angle rotate(float degree) {
        return new Angle(this.degree + degree);
    }

    public Angle rotate(Angle angle) {
        return this.rotate(angle.getDegree());
    }

    private static float toPositiveAngle(float degree) {
        float result = degree;
        while (result < 0) {
            result += 360F;
        }
        return result;
    }
}
