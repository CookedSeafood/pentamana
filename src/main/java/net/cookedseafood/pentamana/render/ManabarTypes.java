package net.cookedseafood.pentamana.render;

import java.util.Arrays;

public enum ManabarTypes {
    CHARACTER((byte)1, "character"),
    NUMERIC((byte)2, "numeric"),
    PERCENTAGE((byte)3, "percentage"),
    NONE((byte)0, "none");

    private byte index;
    private String name;

    ManabarTypes(byte index, String name) {
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
        return Arrays.stream(ManabarTypes.values())
            .filter(manaManabarType -> manaManabarType.name.equals(name))
            .map(manaManabarType -> manaManabarType.index)
            .findAny()
            .orElse((byte)0);
    }

    public static String getName(byte index) {
        return Arrays.stream(ManabarTypes.values())
            .filter(manaManabarType -> manaManabarType.index == index)
            .map(manaManabarType -> manaManabarType.name)
            .findAny()
            .orElse("");
    }

    public static ManabarTypes byIndex(byte index) {
        return Arrays.stream(ManabarTypes.values())
            .filter(manaManabarType -> manaManabarType.index == index)
            .findAny()
            .get();
    }

    public static ManabarTypes byName(String name) {
        return Arrays.stream(ManabarTypes.values())
            .filter(manaManabarType -> manaManabarType.name.equals(name))
            .findAny()
            .get();
    }
}
