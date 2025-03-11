package net.cookedseafood.pentamana.render;

import java.util.Arrays;

public enum ManabarPositions {
    ACTIONBAR((byte)0, "actionbar"),
    BOSSBAR((byte)1, "bossbar");

    private byte index;
    private String name;

    ManabarPositions(byte index, String name) {
        this.index = index;
        this.name = name;
    }

    public byte getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public static byte getIndex(String name) {
        return Arrays.stream(ManabarPositions.values())
            .filter(manabarPosition -> manabarPosition.name.equals(name))
            .map(manabarPosition -> manabarPosition.index)
            .findAny()
            .orElse((byte)0);
    }

    public static String getName(byte index) {
        return Arrays.stream(ManabarPositions.values())
            .filter(manabarPosition -> manabarPosition.index == index)
            .map(manabarPosition -> manabarPosition.name)
            .findAny()
            .orElse("");
    }

    public static ManabarPositions byIndex(byte index) {
        return Arrays.stream(ManabarPositions.values())
            .filter(manabarPosition -> manabarPosition.index == index)
            .findAny()
            .get();
    }

    public static ManabarPositions byName(String name) {
        return Arrays.stream(ManabarPositions.values())
            .filter(manabarPosition -> manabarPosition.name.equals(name))
            .findAny()
            .get();
    }
}
