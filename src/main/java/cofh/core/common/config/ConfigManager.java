package cofh.core.common.config;

import cofh.core.util.ProxyUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    protected List<IBaseConfig> commonSubConfigs = new ArrayList<>();
    protected List<IBaseConfig> serverSubConfigs = new ArrayList<>();
    protected List<IBaseConfig> clientSubConfigs = new ArrayList<>();

    protected ModContainer container;

    protected boolean commonInit = false;
    protected boolean clientInit = false;
    protected boolean serverInit = false;

    protected final ModConfigSpec.Builder commonConfig = new ModConfigSpec.Builder();
    protected ModConfigSpec commonSpec;

    protected final ModConfigSpec.Builder clientConfig = new ModConfigSpec.Builder();
    protected ModConfigSpec clientSpec;

    protected final ModConfigSpec.Builder serverConfig = new ModConfigSpec.Builder();
    protected ModConfigSpec serverSpec;

    public ConfigManager register(ModContainer container, IEventBus bus) {

        this.container = container;
        bus.register(this);
        return this;
    }

    public synchronized ConfigManager addCommonConfig(IBaseConfig config) {

        commonSubConfigs.add(config);
        return this;
    }

    public synchronized ConfigManager addClientConfig(IBaseConfig config) {

        clientSubConfigs.add(config);
        return this;
    }

    public synchronized ConfigManager addServerConfig(IBaseConfig config) {

        serverSubConfigs.add(config);
        return this;
    }

    public void setupCommon() {

        if (!commonInit) {
            genCommonConfig();
            commonSpec = commonConfig.build();
            container.registerConfig(ModConfig.Type.COMMON, commonSpec);
            commonInit = true;
        }
    }

    /**
     * Must be called in Mod Constructor or NewRegistryEvent at latest.
     */
    public void setupClient() {

        if (ProxyUtils.isClient() && !clientInit) {
            genClientConfig();
            clientSpec = clientConfig.build();
            container.registerConfig(ModConfig.Type.CLIENT, clientSpec);
            clientInit = true;
        }
    }

    /**
     * Must be called in Mod Constructor or Common Setup event at latest.
     */
    public void setupServer() {

        if (!serverInit) {
            genServerConfig();
            serverSpec = serverConfig.build();
            container.registerConfig(ModConfig.Type.SERVER, serverSpec);
            serverInit = true;
        }
    }

    public boolean isClientInit() {

        return clientInit;
    }

    public boolean isServerInit() {

        return serverInit;
    }

    public ModConfigSpec getServerSpec() {

        return serverSpec;
    }

    public ModConfigSpec getClientSpec() {

        return clientSpec;
    }

    protected void genCommonConfig() {

        for (IBaseConfig cfg : commonSubConfigs) {
            cfg.apply(commonConfig);
        }
    }

    protected void genServerConfig() {

        for (IBaseConfig cfg : serverSubConfigs) {
            cfg.apply(serverConfig);
        }
    }

    protected void genClientConfig() {

        for (IBaseConfig cfg : clientSubConfigs) {
            cfg.apply(clientConfig);
        }
    }

    protected void refreshCommonConfig() {

        commonSubConfigs.forEach(IBaseConfig::refresh);
    }

    protected void refreshServerConfig() {

        serverSubConfigs.forEach(IBaseConfig::refresh);
    }

    protected void refreshClientConfig() {

        clientSubConfigs.forEach(IBaseConfig::refresh);
    }

    // region CONFIGURATION
    // Not Unloading: reading a value then throws.
    @SubscribeEvent
    public void configLoad(ModConfigEvent.Loading event) {

        configRefresh(event);
    }

    @SubscribeEvent
    public void configReload(ModConfigEvent.Reloading event) {

        configRefresh(event);
    }

    protected void configRefresh(ModConfigEvent event) {

        switch (event.getConfig().getType()) {
            case COMMON -> refreshCommonConfig();
            case CLIENT -> refreshClientConfig();
            case SERVER -> refreshServerConfig();
        }
    }
    // endregion
}
