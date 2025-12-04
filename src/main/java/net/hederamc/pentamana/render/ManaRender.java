package net.hederamc.pentamana.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.hederamc.pentamana.data.PentamanaConfig;
import net.hederamc.pentamana.data.PentamanaPreference;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ManaRender {
    private ManaRender.Type type;
    private ManaCharset charset;
    private int pointsPerCharacter;
    private boolean isCompressed;
    private byte compressionSize;

    public ManaRender(ManaRender.Type type, ManaCharset charset, int pointsPerCharacter, boolean isCompressed, byte compressionSize) {
        this.type = type;
        this.charset = charset;
        this.pointsPerCharacter = pointsPerCharacter;
        this.isCompressed = isCompressed;
        this.compressionSize = compressionSize;
    }

    public Text toText(float manaCapacity, float manaSupply) {
        return this.type == ManaRender.Type.CHARACTER ?
            this.toTextInCharacter(manaCapacity, manaSupply) :
            this.type == ManaRender.Type.NUMERIC ?
            this.toTextInNumeric(manaCapacity, manaSupply) :
            this.type == ManaRender.Type.PERCENTAGE ?
            this.toTextInPercentage(manaCapacity, manaSupply) :
            this.type == ManaRender.Type.NONE ?
            this.toTextInNone() :
            Text.empty();
    }

    public Text toTextInCharacter(float manaCapacity, float manaSupply) {
        int manaCapacityPoint;
        int manaSupplyPoint;

        if (this.isCompressed) {
            manaCapacityPoint = this.compressionSize * 2;
            manaSupplyPoint = (int)(manaSupply / manaCapacity * manaCapacityPoint);
        } else {
            manaCapacityPoint = (int)(manaCapacity / PentamanaConfig.manaPerPoint);
            manaSupplyPoint = (int)(manaSupply / PentamanaConfig.manaPerPoint);
        }

        int manaCapacityPointTrimmed = manaCapacityPoint - manaCapacityPoint % this.pointsPerCharacter;
        int manaPointTrimmed = manaSupplyPoint - manaSupplyPoint % this.pointsPerCharacter;

        manaCapacityPointTrimmed = Math.min(manaCapacityPointTrimmed, PentamanaConfig.manaPointLimit);

        MutableText text = Text.empty();
        for (int manaPointIndex = 0; manaPointIndex < manaCapacityPointTrimmed; manaPointIndex += this.pointsPerCharacter) {
            int manaCharacterTypeIndex =
                manaPointIndex < manaPointTrimmed ?
                0 : manaPointIndex < manaSupplyPoint ?
                manaSupplyPoint - manaPointIndex : this.pointsPerCharacter;
            int manaCharacterIndex = manaPointIndex / this.pointsPerCharacter;

            Text SelectedManaCharacter = this.charset.get(manaCharacterTypeIndex).get(manaCharacterIndex);
            text.append(SelectedManaCharacter);
        }

        return text;
    }

    public Text toTextInNumeric(float manaCapacity, float manaSupply) {
        return Text.literal((int)(manaSupply / PentamanaConfig.manaPerPoint) + "/" + (int)(manaCapacity / PentamanaConfig.manaPerPoint)).setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    }

    public Text toTextInPercentage(float manaCapacity, float manaSupply) {
        return Text.literal((int)(manaSupply / manaCapacity * 100) + "%").setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    }

    public Text toTextInNone() {
        return Text.empty();
    }

    public ManaRender.Type getType() {
        return this.type;
    }

    public void setType(ManaRender.Type type) {
        this.type = type;
    }

    public ManaRender withType(ManaRender.Type type) {
        this.setType(type);
        return this;
    }

    public ManaCharset getCharset() {
        return charset;
    }

    public void setCharset(ManaCharset charset) {
        this.charset = charset;
    }

    public ManaRender withCharset(ManaCharset charset) {
        this.setCharset(charset);
        return this;
    }

    public int getPointsPerCharacter() {
        return pointsPerCharacter;
    }

    public void setPointsPerCharacter(int pointsPerCharacter) {
        this.pointsPerCharacter = pointsPerCharacter;
    }

    public ManaRender withPointsPerCharacter(int pointsPerCharacter) {
        this.setPointsPerCharacter(pointsPerCharacter);
        return this;
    }

    public boolean isCompressed() {
        return this.isCompressed;
    }

    public void setCompression(boolean isCompressed) {
        this.isCompressed = isCompressed;
    }

    public ManaRender withCompression(boolean isCompressed) {
        this.setCompression(isCompressed);
        return this;
    }

    public byte getCompressionSize() {
        return this.compressionSize;
    }

    public void setCompressionSize(byte compressionSize) {
        this.compressionSize = compressionSize;
    }

    public ManaRender withCompressionSize(byte compressionSize) {
        this.setCompressionSize(compressionSize);
        return this;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaRender
     * 
     * @see #deepCopy()
     */
    public ManaRender copy() {
        return new ManaRender(this.type, this.charset, this.pointsPerCharacter, this.isCompressed, this.compressionSize);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaRender
     * 
     * @see #copy()
     */
    public ManaRender deepCopy() {
        return new ManaRender(this.type, this.charset.deepCopy(), this.pointsPerCharacter, this.isCompressed, this.compressionSize);
    }

    public static ManaRender fromPreference(PentamanaPreference preference) {
        return new ManaRender(
            preference.type,
            preference.charset,
            preference.pointsPerCharacter,
            preference.isCompressed,
            preference.compressionSize
        );
    }

    public static ManaRender fromNbt(NbtCompound nbtCompound) {
        return new ManaRender(
            nbtCompound.contains("type") ? ManaRender.Type.byName(nbtCompound.getString("type").get()) : PentamanaConfig.DefaultPreference.type,
            ManaCharset.fromNbt(nbtCompound.getListOrEmpty("charset")),
            nbtCompound.getInt("pointsPerCharacter", PentamanaConfig.DefaultPreference.pointsPerCharacter),
            nbtCompound.getBoolean("isCompressed", PentamanaConfig.DefaultPreference.isCompressed),
            nbtCompound.getByte("compressionSize", PentamanaConfig.DefaultPreference.compressionSize)
        );
    }

    public NbtCompound toNbt() {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>ofEntries(
                    Map.entry("type", NbtString.of(this.type.name)),
                    Map.entry("charset", this.charset.toNbt()),
                    Map.entry("pointsPerCharacter", NbtInt.of(this.pointsPerCharacter)),
                    Map.entry("isCompressed", NbtByte.of(this.isCompressed)),
                    Map.entry("compressionSize", NbtByte.of(this.compressionSize))
                )
            )
        );
    }

    public static enum Type {
        CHARACTER((byte)1, "character"),
        NUMERIC((byte)2, "numeric"),
        PERCENTAGE((byte)3, "percentage"),
        NONE((byte)0, "none");
    
        private byte index;
        private String name;
    
        Type(byte index, String name) {
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
            return Arrays.stream(Type.values())
                .filter(manaManabarType -> manaManabarType.name.equals(name))
                .map(manaManabarType -> manaManabarType.index)
                .findAny()
                .orElse((byte)0);
        }
    
        public static String getName(byte index) {
            return Arrays.stream(Type.values())
                .filter(manaManabarType -> manaManabarType.index == index)
                .map(manaManabarType -> manaManabarType.name)
                .findAny()
                .orElse("");
        }
    
        public static Type byIndex(byte index) {
            return Arrays.stream(Type.values())
                .filter(manaManabarType -> manaManabarType.index == index)
                .findAny()
                .get();
        }
    
        public static Type byName(String name) {
            return Arrays.stream(Type.values())
                .filter(manaManabarType -> manaManabarType.name.equals(name))
                .findAny()
                .get();
        }
    }
}
