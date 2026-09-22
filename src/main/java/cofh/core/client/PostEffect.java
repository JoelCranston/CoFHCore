package cofh.core.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PostEffect implements ResourceManagerReloadListener {

    private static final Collection<PostEffect> EFFECTS = new ArrayList<>();

    protected final Identifier shader;
    protected PostChain chain;
    protected boolean loaded;

    public PostEffect(Identifier shader) {

        this.shader = shader;
        EFFECTS.add(this);
    }

    public PostChain getPostChain() {

        return chain;
    }

    public boolean isEnabled() {

        return loaded;
    }

    public void begin(float partialTick) {

    }

    public void end(float partialTick) {

    }

    public void apply(Window window) {

    }

    public void resize(int width, int height) {

    }

    protected void onChainLoad() {

    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {

        // TODO Post chains now run inside the frame graph; effects stay disabled until ported.
        chain = null;
        loaded = false;
    }

    public static Collection<PostEffect> getAllEffects() {

        return Collections.unmodifiableCollection(EFFECTS);
    }

}
