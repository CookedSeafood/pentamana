package net.cookedseafood.pentamana.api.component;

import org.ladysnake.cca.api.v3.component.Component;

public interface ManaDisplayComponent extends Component {
    byte getManabarLife();

    byte setManabarLife(byte value);

    default byte incrementManabarLife() {
        return incrementManabarLife((byte)1);
    };

    default byte incrementManabarLife(byte value) {
        return setManabarLife((byte)(this.getManabarLife() + value));
    };

    int getManaSupplyPoint();

    int setManaSupplyPoint(int value);

    default int incrementManaSupplyPoint() {
        return incrementManaSupplyPoint(1);
    };

    default int incrementManaSupplyPoint(int value) {
        return setManaSupplyPoint(this.getManaSupplyPoint() + value);
    };

    int getManaCapacityPoint();

    int setManaCapacityPoint(int value);

    default int incrementManaCapacityPoint() {
        return incrementManaCapacityPoint(1);
    };

    default int incrementManaCapacityPoint(int value) {
        return setManaCapacityPoint(this.getManaCapacityPoint() + value);
    };

    byte getManaSupplyPercent();

    byte setManaSupplyPercent(byte value);

    default byte incrementManaSupplyPercent() {
        return incrementManaSupplyPercent((byte)1);
    }

    default byte incrementManaSupplyPercent(byte value) {
        return setManaSupplyPercent((byte)(this.getManaSupplyPercent() + value));
    }
}
