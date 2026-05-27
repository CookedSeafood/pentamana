package net.hederamc.pentamana.client.gui;

import org.jspecify.annotations.Nullable;

public enum ManaBarAlignment {
    BEGINNING("beginning", 0.0f),
    MIDDLE("middle", -0.5f),
    END("end", -1.0f);

    private final String name;
    private final float percent;

    private ManaBarAlignment(String name, float percent) {
        this.name = name;
        this.percent = percent;
    }

    public String getName() {
        return this.name;
    }

    public float getPercent() {
        return this.percent;
    }

    @Nullable
    public static ManaBarAlignment byName(String name) {
        for (ManaBarAlignment alignment : values()) {
            if (alignment.name.equals(name)) {
                return alignment;
            }
        }

        return null;
    }
}
