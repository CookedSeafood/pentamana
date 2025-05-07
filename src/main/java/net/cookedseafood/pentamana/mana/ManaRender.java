package net.cookedseafood.pentamana.mana;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.cookedseafood.pentamana.Pentamana;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ManaRender {
    private ManaRender.Type type;
    private ManaCharset charset;
    private int pointsPerCharacter;
    private boolean isCompression;
    private byte compressionSize;

    public ManaRender(ManaRender.Type type, ManaCharset charset, int pointsPerCharacter, boolean isCompression, byte compressionSize) {
        this.type = type;
        this.charset = charset;
        this.pointsPerCharacter = pointsPerCharacter;
        this.isCompression = isCompression;
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
        int manaCapacityPoint = (int)(manaCapacity / Pentamana.manaPerPoint);
        int manaSupplyPoint = (int)(manaSupply / Pentamana.manaPerPoint);
        int manaCapacityPointTrimmed = manaCapacityPoint - manaCapacityPoint % pointsPerCharacter;
        int manaPointTrimmed = manaSupplyPoint - manaSupplyPoint % pointsPerCharacter;

        manaCapacityPointTrimmed = Math.min(manaCapacityPointTrimmed, Pentamana.manaPointLimit);

        MutableText text = Text.empty();
        for (int manaPointIndex = 0; manaPointIndex < manaCapacityPointTrimmed; manaPointIndex += pointsPerCharacter) {
            int manaCharacterTypeIndex =
                manaPointIndex < manaPointTrimmed ?
                0 : manaPointIndex < manaSupplyPoint ?
                manaSupplyPoint - manaPointIndex : pointsPerCharacter;
            int manaCharacterIndex = manaPointIndex / pointsPerCharacter;

            Text SelectedManaCharacter = this.charset.getCharset().get(manaCharacterTypeIndex).get(manaCharacterIndex);
            text.append(SelectedManaCharacter);
        }

        return text;
    }

    public Text toTextInNumeric(float manaCapacity, float manaSupply) {
        return Text.literal((int)(manaSupply / Pentamana.manaPerPoint) + "/" + (int)(manaCapacity / Pentamana.manaPerPoint)).setStyle(Style.EMPTY.withColor(Formatting.AQUA));
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

    public ManaCharset getCharset() {
        return charset;
    }

    public void setCharset(ManaCharset charset) {
        this.charset = charset;
    }

    public int getPointsPerCharacter() {
        return pointsPerCharacter;
    }

    public void setPointsPerCharacter(int pointsPerCharacter) {
        this.pointsPerCharacter = pointsPerCharacter;
    }

    public boolean isCompression() {
        return this.isCompression;
    }

    public void setIsCompression(boolean isCompression) {
        this.isCompression = isCompression;
    }

    public byte getCompressionSize() {
        return this.compressionSize;
    }

    public void setCompressionSize(byte compressionSize) {
        this.compressionSize = compressionSize;
    }

    /**
     * A shadow copy.
     * 
     * @return a new ManaRender
     * 
     * @see #deepCopy()
     */
    public ManaRender copy() {
        return new ManaRender(this.type, this.charset, this.pointsPerCharacter, this.isCompression, this.compressionSize);
    }

    /**
     * A deep copy.
     * 
     * @return a new ManaRender
     * 
     * @see #copy()
     */
    public ManaRender deepCopy() {
        return new ManaRender(this.type, this.charset.deepCopy(), this.pointsPerCharacter, this.isCompression, this.compressionSize);
    }

    public static ManaRender fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        return new ManaRender(
            ManaRender.Type.byName(nbtCompound.getString("type")),
            ManaCharset.fromNbt(nbtCompound.getList("charset", NbtElement.LIST_TYPE), wrapperLookup),
            nbtCompound.getInt("pointsPerCharacter"),
            nbtCompound.getBoolean("isCompression"),
            nbtCompound.getByte("compressionSize")
        );
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup wrapperLookup) {
        return new NbtCompound(
            new HashMap<>(
                Map.<String,NbtElement>of(
                    "type",
                    NbtString.of(this.type.name),
                    "charset",
                    this.charset.toNbt(wrapperLookup),
                    "pointsPerCharacter",
                    NbtInt.of(this.pointsPerCharacter),
                    "isCompression",
                    NbtByte.of(this.isCompression),
                    "compressionSize",
                    NbtByte.of(this.compressionSize)
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
