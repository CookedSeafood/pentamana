package net.cookedseafood.pentamana.mana;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;

public class ManaBar {
	protected float capacity;
	protected float supply;
	protected ManaBar.Position position;
	protected ManaTextual textual;
	protected boolean isVisible;
	protected BossBar.Color color;
	protected BossBar.Style style;

	public ManaBar(float capacity, float supply, ManaBar.Position position, ManaTextual textual, boolean isVisible, BossBar.Color color, BossBar.Style style) {
		this.capacity = capacity;
		this.supply = supply;
		this.position = position;
		this.textual = textual;
		this.isVisible = isVisible;
		this.color = color;
		this.style = style;
	}

	public boolean isFull() {
		return this.supply == capacity;
	}

	public boolean isEmpty() {
		return this.supply == 0.0f;
	}

	public float getCapacity() {
		return this.capacity;
	}

	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}

	public float getSupply() {
		return this.supply;
	}

	public void setSupply(float supply) {
		this.supply = supply;
	}

	public ManaBar.Position getPosition() {
		return this.position;
	}

	public void setPosition(ManaBar.Position position) {
		this.position = position;
	}

	public ManaTextual getTextual() {
		return this.textual;
	}

	public void setTextual(ManaTextual textual) {
		this.textual = textual;
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public void setIsVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public BossBar.Color getColor() {
		return this.color;
	}

	public void setColor(BossBar.Color color) {
		this.color = color;
	}

	public BossBar.Style getStyle() {
		return this.style;
	}

	public void setStyle(BossBar.Style style) {
		this.style = style;
	}

	/**
	 * A shadow copy.
	 * 
	 * @return a new ManaBar
	 * 
	 * @see #deepCopy()
	 */
	public ManaBar copy() {
		return new ManaBar(this.capacity, this.supply, this.position, this.textual, this.isVisible, this.color, this.style);
	}

	/**
	 * A deep copy.
	 * 
	 * @return a new ManaBar
	 * 
	 * @see #copy()
	 */
	public ManaBar deepCopy() {
		return new ManaBar(this.capacity, this.supply, this.position, this.textual.deepCopy(), this.isVisible, this.color, this.style);
	}

	public static ManaBar fromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
		return new ManaBar(
			nbtCompound.getFloat("capacity"),
			nbtCompound.getFloat("supply"),
			ManaBar.Position.byName(nbtCompound.getString("position")),
			ManaTextual.fromNbt(nbtCompound.getCompound("textual"), registryLookup),
			nbtCompound.getBoolean("isVisible"),
			BossBar.Color.byName(nbtCompound.getString("color")),
			BossBar.Style.byName(nbtCompound.getString("style"))
		);
	}

	public NbtCompound toNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return new NbtCompound(
			new HashMap<>(
				Map.<String,NbtElement>of(
					"capacity",
					NbtFloat.of(this.capacity),
					"supply",
					NbtFloat.of(this.supply),
					"position",
					NbtString.of(this.position.name),
					"textual",
					this.textual.toNbt(registryLookup),
					"isVisible",
					NbtByte.of(this.isVisible),
					"color",
					NbtString.of(this.color.getName()),
					"style",
					NbtString.of(this.style.getName())
				)
			)
		);
	}

	public static enum Position {
		ACTIONBAR((byte)0, "actionbar"),
		BOSSBAR((byte)1, "bossbar"),
		SIDERBAR((byte)2, "siderbar");

		private byte index;
		private String name;

		Position(byte index, String name) {
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
			return Arrays.stream(Position.values())
				.filter(manabarPosition -> manabarPosition.name.equals(name))
				.map(manabarPosition -> manabarPosition.index)
				.findAny()
				.orElse((byte)0);
		}

		public static String getName(byte index) {
			return Arrays.stream(Position.values())
				.filter(manabarPosition -> manabarPosition.index == index)
				.map(manabarPosition -> manabarPosition.name)
				.findAny()
				.orElse("");
		}

		public static Position byIndex(byte index) {
			return Arrays.stream(Position.values())
				.filter(manabarPosition -> manabarPosition.index == index)
				.findAny()
				.get();
		}

		public static Position byName(String name) {
			return Arrays.stream(Position.values())
				.filter(manabarPosition -> manabarPosition.name.equals(name))
				.findAny()
				.get();
		}
	}
}
