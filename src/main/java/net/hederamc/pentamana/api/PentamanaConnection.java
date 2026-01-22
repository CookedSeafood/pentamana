package net.hederamc.pentamana.api;

public interface PentamanaConnection {
    default boolean canConnectPentamana() {
        throw new UnsupportedOperationException();
    }

    default void setCanConnectPentamana(boolean bool) {
        throw new UnsupportedOperationException();
    }
}
