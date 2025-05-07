package net.cookedseafood.genericregistry.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Id-Object Map.
 * 
 * <p>This should <i>NOT</i> be instanced manualy.</p>
 * 
 * @see Registries#register(Identifier, Object)
 * @see Registries#get(Class, Identifier)
 */
public class Registry<T> {
    private final Map<Identifier, T> registry;

    public Registry(Map<Identifier, T> registry) {
        this.registry = registry;
    }

    public Registry() {
        this.registry = new HashMap<>();
    }

    public static <T> Registry<T> of(Map<Identifier, T> registry) {
        return new Registry<>(registry);
    }

    public Map<Identifier, T> getRegistry() {
        return this.registry;
    }

    public int size() {
        return this.registry.size();
    }

    public boolean isEmpty() {
        return this.registry.isEmpty();
    }

    public boolean containsKey(Identifier id) {
        return this.registry.containsKey(id);
    }

    public boolean containsValue(T object) {
        return this.registry.containsValue(object);
    }

    public Set<Map.Entry<Identifier, T>> entrySet() {
        return this.registry.entrySet();
    }

    public Set<Identifier> keySet() {
        return this.registry.keySet();
    }

    public Collection<T> values() {
        return this.registry.values();
    }

    @Nullable
    public T get(Identifier id) {
        return this.registry.get(id);
    }

    public T getOrPut(Identifier id, T object) {
        T object2 = this.get(id);

        if (object2 != null) {
            return object2;
        }

        this.put(id, object);
        return object;
    }

    public T put(Identifier id, T object) {
        return this.registry.put(id, object);
    }

    public void putAll(Map<Identifier, T> registry) {
        this.registry.putAll(registry);
    }

    public T remove(Identifier id) {
        return this.registry.remove(id);
    }

    public boolean remove(Identifier id, T object) {
        return this.registry.remove(id, object);
    }

    public void clear() {
        this.registry.clear();
    }

    public T replace(Identifier id, T object) {
        return this.registry.replace(id, object);
    }

    public boolean replace(Identifier id, T oldRegistry, T newRegistry) {
        return this.registry.replace(id, oldRegistry, newRegistry);
    }

    public void replaceAll(BiFunction<Identifier, T, T> function) {
        this.registry.replaceAll(function);
    }
}
