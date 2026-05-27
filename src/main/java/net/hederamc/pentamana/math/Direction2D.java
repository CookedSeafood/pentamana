package net.hederamc.pentamana.math;

import org.joml.Vector2d;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

/**
 * Mathematical directions.
 */
public enum Direction2D {
    RIGHT("right", Axis.X, AxisDirection.POSITIVE, new Vector2i(1, 0), 0.0f, 0),
    UP("up", Axis.Y, AxisDirection.POSITIVE, new Vector2i(0, 1), 90.0f, 1),
    LEFT("left", Axis.X, AxisDirection.NEGATIVE, new Vector2i(-1, 0), 180.0f, 2),
    DOWN("down", Axis.Y, AxisDirection.NEGATIVE, new Vector2i(0, -1), 270.0f, 3);

    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vector2i normal;
    private final float rotation;
    private final int index;

    private Direction2D(String name, Axis axis, AxisDirection axisDirection,
            Vector2i normal, float rotation, int index) {
        this.name = name;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = normal;
        this.rotation = rotation;
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public Vector2i getNormal() {
        return this.normal;
    }

    public float getRotation() {
        return this.rotation;
    }

    public int getIndex() {
        return this.index;
    }

    public Direction2D getOpposite() {
        return switch (this) {
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }

    public Direction2D getClockWise() {
        return switch (this) {
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
            case UP -> RIGHT;
        };
    }

    public Direction2D getCounterClockWise() {
        return switch (this) {
            case RIGHT -> UP;
            case UP -> LEFT;
            case LEFT -> DOWN;
            case DOWN -> RIGHT;
        };
    }

    public static Direction2D fromAxisAndDirection(Axis axis, AxisDirection axisDirection) {
        return axis.getDirection(axisDirection);
    }

    public static Direction2D fromVector(Vector2d vector) {
        return fromVector(vector.x, vector.y);
    }

    public static Direction2D fromVector(double x, double y) {
        Direction2D result = RIGHT;
        double highestProjection = 0.0;

        for (Direction2D direction : values()) {
            double projection = x * direction.normal.x + y * direction.normal.y;
            if (projection > highestProjection) {
                highestProjection = projection;
                result = direction;
            }
        }

        return result;
    }

    public static Direction2D fromVector(Vector2i vector) {
        return fromVector(vector.x, vector.y);
    }

    public static Direction2D fromVector(int x, int y) {
        Direction2D result = RIGHT;
        int highestProjection = 0;

        for (Direction2D direction : values()) {
            int projection = x * direction.normal.x + y * direction.normal.y;
            if (projection > highestProjection) {
                highestProjection = projection;
                result = direction;
            }
        }

        return result;
    }

    public static Direction2D fromRotation(float rotation) {
        int index = (int) Math.floor(rotation / 90.0 + 0.5);
        return fromIndex(index);
    }

    public static Direction2D fromIndex(int index) {
        index = index & 3;

        for (Direction2D direction : values()) {
            if (direction.index == index) {
                return direction;
            }
        }

        throw new AssertionError("Unreachable.");
    }

    @Nullable
    public static Direction2D byName(String name) {
        for (Direction2D direction : values()) {
            if (direction.name.equals(name)) {
                return direction;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static enum Axis {
        X("x") {
            @Override
            public Direction2D getPositive() {
                return RIGHT;
            }

            @Override
            public Direction2D getNegative() {
                return LEFT;
            }
        },
        Y("y") {
            @Override
            public Direction2D getPositive() {
                return UP;
            }

            @Override
            public Direction2D getNegative() {
                return DOWN;
            }
        };

        private final String name;

        private Axis(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X;
        }

        public abstract Direction2D getPositive();

        public abstract Direction2D getNegative();

        public Direction2D getDirection(AxisDirection axisDirection) {
            return axisDirection == AxisDirection.POSITIVE ? this.getPositive() : this.getNegative();
        }

        public Direction2D[] getDirections() {
            return new Direction2D[] { this.getPositive(), this.getNegative() };
        }

        @Nullable
        public static Axis byName(String name) {
            for (Axis axis : values()) {
                if (axis.name.equals(name)) {
                    return axis;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum AxisDirection {
        POSITIVE("positive", 1),
        NEGATIVE("negative", -1);

        private final String name;
        private final int normal;

        private AxisDirection(String name, int normal) {
            this.name = name;
            this.normal = normal;
        }

        public String getName() {
            return this.name;
        }

        public int getNormal() {
            return this.normal;
        }

        public AxisDirection getOpposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }

        public static AxisDirection fromVector(double vector) {
            return (int) (Double.doubleToLongBits(vector) >> 63) == 0 ? NEGATIVE : POSITIVE;
        }

        public static AxisDirection fromVector(int vector) {
            return vector < 0 ? NEGATIVE : POSITIVE;
        }

        @Nullable
        public static AxisDirection byName(String name) {
            for (AxisDirection axisDirection : values()) {
                if (axisDirection.name.equals(name)) {
                    return axisDirection;
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
