package cofh.lib.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static cofh.lib.util.helpers.StringHelper.decompose;

public class DeferredRegisterCoFH<T> {

    private final String modid;
    private final DeferredRegister<T> wrappedRegister;
    private final Map<Identifier, DeferredHolder<T, ? extends T>> registryObjects = new HashMap<>();

    private DeferredRegisterCoFH(DeferredRegister<T> wrappedRegister, String modid) {

        this.modid = modid;
        this.wrappedRegister = wrappedRegister;
    }

    public static <B> DeferredRegisterCoFH<B> create(Identifier registryName, String modid) {

        return new DeferredRegisterCoFH<>(DeferredRegister.create(registryName, modid), modid);
    }

    public static <T> DeferredRegisterCoFH<T> create(Registry<T> reg, String modid) {

        return new DeferredRegisterCoFH<>(DeferredRegister.create(reg, modid), modid);
    }

    public static <T> DeferredRegisterCoFH<T> create(ResourceKey<? extends Registry<T>> key, String modid) {

        return new DeferredRegisterCoFH<>(DeferredRegister.create(key, modid), modid);
    }

    @SuppressWarnings ({"rawtypes", "unchecked"})
    public synchronized <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {

        DeferredHolder<T, I> ret = wrappedRegister.register(name, sup);
        registryObjects.put(ret.getId(), ret);

        return ret;
    }

    /**
     * 26.1.2: BlockBehaviour.Properties and Item.Properties both require an explicit setId before the
     * constructor runs, so a registration has to see its own id. NeoForge's DeferredRegister offers a
     * Function<Identifier, I> overload for exactly this; mirror it here rather than making every caller
     * reach past the wrapper. See docs/api-notes-26.1.2.md, B.2.
     */
    public synchronized <I extends T> DeferredHolder<T, I> register(final String name, final Function<Identifier, ? extends I> func) {

        DeferredHolder<T, I> ret = wrappedRegister.register(name, func);
        registryObjects.put(ret.getId(), ret);

        return ret;
    }

    public Registry<T> makeRegistry(final Consumer<RegistryBuilder<T>> consumer) {

        return wrappedRegister.makeRegistry(consumer);
    }

    public void register(IEventBus bus) {

        wrappedRegister.register(bus);
    }

    public Map<Identifier, DeferredHolder<T, ? extends T>> getRegistryObjects() {

        return registryObjects;
    }

    // region OBJECT RETRIEVAL
    public T get(final String resourceLoc) {

        return get(decompose(modid, resourceLoc, ':'));
    }

    private T get(final String[] resourceLoc) {

        return get(resourceLoc[0], resourceLoc[1]);
    }

    public T get(final String modid, final String name) {

        return get(Identifier.fromNamespaceAndPath(modid, name));
    }

    public T get(final Identifier resourceLoc) {

        DeferredHolder<T, ? extends T> reg = registryObjects.get(resourceLoc);
        return reg == null ? null : reg.get();
    }
    // endregion

    // region SUPPLIER RETRIEVAL
    public Supplier<T> getSup(final String resourceLoc) {

        return getSup(decompose(modid, resourceLoc, ':'));
    }

    private Supplier<T> getSup(final String[] resourceLoc) {

        return getSup(resourceLoc[0], resourceLoc[1]);
    }

    public Supplier<T> getSup(final String modid, final String name) {

        return getSup(Identifier.fromNamespaceAndPath(modid, name));
    }

    @Nullable
    public Supplier<T> getSup(final Identifier resourceLoc) {

        return (Supplier<T>) registryObjects.get(resourceLoc);
    }
    // endregion
}
