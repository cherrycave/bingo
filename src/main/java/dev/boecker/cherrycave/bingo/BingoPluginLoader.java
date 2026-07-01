package dev.boecker.cherrycave.bingo;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class BingoPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:2.1.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:2.1.1"), null));

        resolver.addRepository(new RemoteRepository.Builder("invui", "default", "https://repo.xenondevs.xyz/releases").build());

        classpathBuilder.addLibrary(resolver);
    }
}