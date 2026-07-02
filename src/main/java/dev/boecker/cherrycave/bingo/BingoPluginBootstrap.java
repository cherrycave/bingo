package dev.boecker.cherrycave.bingo;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BingoPluginBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, (datapack) -> {
            try {
                URI uri  = this.getClass().getResource("/bingo-items").toURI();
                datapack.registrar().discoverPack(uri, "provided");
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
            // Discover the pack. The ID is set to "provided", which indicates to
            // a server owner that your plugin includes this data pack.
        });
    }

}