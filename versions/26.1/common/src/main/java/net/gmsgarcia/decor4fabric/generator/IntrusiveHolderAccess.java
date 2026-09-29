package net.gmsgarcia.decor4fabric.generator;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Re-opens {@code BuiltInRegistries.BLOCK} for new intrusive holders.
 *
 * <p><b>Why this is needed.</b> Every {@code Block} constructor starts with
 * {@code BuiltInRegistries.BLOCK.createIntrusiveHolder(this)} -- the block
 * caches a {@code Holder.Reference} to itself so the holder and the block
 * instance stay the same object. {@code MappedRegistry} supports that only
 * while it holds a non-null {@code unregisteredIntrusiveHolders} map, and
 * {@code MappedRegistry.freeze()} binds those pending holders to their
 * registered entries and then nulls the map. {@code Bootstrap.bootStrap()}
 * freezes as its last step, so after it returns, {@code new Block(...)} throws
 * {@code IllegalStateException: This registry can't create intrusive holders}.
 *
 * <p>The game never hits this because mod registration runs inside the same
 * open window, before the freeze. This generator has to bootstrap first -- it
 * needs the vanilla registries loaded and the data-fixers version set before it
 * can touch any block class -- and that puts it after the window.
 *
 * <p><b>Scope.</b> Only the block registry is reopened, only the two fields
 * involved are touched, and the process exits immediately afterwards. Nothing
 * here is on a runtime path: this class is only reachable from
 * {@link ResourceGenerator}, and the generated tree is the only output. It is
 * not a way to register blocks at runtime, and the mod's own registration still
 * happens the normal way inside the game.
 *
 * <p>Reflection is used rather than an accessor because the fields are private
 * and there is no public unfreeze. A mapping rename therefore fails loudly here
 * with {@link #FIELD_MISSING_HINT} instead of silently producing wrong files.
 */
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

    /**
     * Puts the block registry back into the state mod registration sees, so
     * {@code new Block(...)} works again.
     *
     * @throws IllegalStateException if the expected fields are gone
     */
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
