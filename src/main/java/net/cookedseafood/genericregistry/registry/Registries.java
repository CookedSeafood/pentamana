package net.cookedseafood.genericregistry.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class Registries {
    private static final Map<Class<?>, Registry<?>> registries;

    /**
     * Register an object to registry.
     * 
     * @param <T> the type of the object to register
     * @param id the key to map with
     * @param o the object to register
     * @return the object
     */
    @SuppressWarnings("unchecked")
    public static <T> T register(Identifier id, T o) {
        ((Registry<T>)getOrPut(o.getClass())).put(id, o);
        return o;
    }

    /**
     * Get an object from registry.
     * 
     * @param <T> the type of the object to get
     * @param type the type of the object to get
     * @param id the key to map with
     * @return the object
     */
    @Nullable
    public static <T> T get(Class<T> type, Identifier id) {
        Registry<T> registry = get(type);
        return registry == null ? null : registry.get(id);
    }

    public static Map<Class<?>, Registry<?>> getRegistries() {
        return registries;
    }

    public static int size() {
        return registries.size();
    }

    public static boolean isEmpty() {
        return registries.isEmpty();
    }

    public static boolean containsKey(Class<?> type) {
        return registries.containsKey(type);
    }

    public static boolean containsValue(Registry<?> registry) {
        return registries.containsValue(registry);
    }

    public static Set<Map.Entry<Class<?>, Registry<?>>> entrySet() {
        return registries.entrySet();
    }

    public static Set<Class<?>> keySet() {
        return registries.keySet();
    }

    public static Collection<Registry<?>> values() {
        return registries.values();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> get(Class<T> type) {
        return (Registry<T>)registries.get(type);
    }

    public static <T> Registry<T> getOrPut(Class<T> type, Registry<T> registry) {
        Registry<T> registry2 = get(type);

        if (registry2 != null) {
            return registry2;
        }

        put(type, registry);
        return registry;
    }

    public static <T> Registry<T> getOrPut(Class<T> type) {
        return getOrPut(type, new Registry<>());
    }

    public static <T> Registry<?> put(Class<T> type, Registry<T> registry) {
        return registries.put(type, registry);
    }

    public static void putAll(Map<Class<?>, Registry<?>> registries) {
        registries.forEach(Registries.registries::put);
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> remove(Class<T> type) {
        return (Registry<T>)registries.remove(type);
    }

    public static <T> boolean remove(Class<T> type, Registry<T> registry) {
        return registries.remove(type, registry);
    }

    public static void clear() {
        registries.clear();
    }

    public static <T> Registry<?> replace(Class<T> type, Registry<T> registry) {
        return registries.replace(type, registry);
    }

    public static <T> boolean replace(Class<T> type, Registry<T> oldRegistries, Registry<T> newRegistries) {
        return registries.replace(type, oldRegistries, newRegistries);
    }

    public static void replaceAll(BiFunction<Class<?>, Registry<?>, Registry<?>> function) {
        registries.replaceAll(function);
    }

    static {
        registries = new HashMap<>();
    }
}
