package net.gmsgarcia.decor4all.generator;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import net.minecraft.core.registries.BuiltInRegistries;

// Re-opens BuiltInRegistries.BLOCK for new intrusive holders.
final class IntrusiveHolderAccess {

    private IntrusiveHolderAccess() {
    }

    private static final String PENDING_HOLDERS = "unregisteredIntrusiveHolders";
    private static final String FROZEN = "frozen";

    static final String FIELD_MISSING_HINT =
            "Could not find the private fields this generator patches. The mappings "
                    + "for the block registry changed; look for '" + PENDING_HOLDERS + "' "
                    + "and '" + FROZEN + "' on MappedRegistry and update this class. "
                    + "Nothing was written.";

    // Puts the block registry back into the state mod registration sees, so new Block(...) works again.
    static void reopen() {
        Object registry = BuiltInRegistries.BLOCK;
        try {
            Field frozen = find(registry.getClass(), FROZEN);
            Field pending = find(registry.getClass(), PENDING_HOLDERS);
            frozen.setAccessible(true);
            pending.setAccessible(true);
            frozen.setBoolean(registry, false);
            // Identity semantics: the map is keyed by the block instance itself
            // and holds it strongly, so equality-based hashing would be both
            // wrong and a needless second source of identity semantics.
            pending.set(registry, new IdentityHashMap<>());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException(FIELD_MISSING_HINT, e);
        }
    }

    private static Field find(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Keep walking up; DefaultedRegistry does not declare these itself.
            }
        }
        throw new NoSuchFieldException(name + " on " + type.getName() + " or any supertype");
    }
}
