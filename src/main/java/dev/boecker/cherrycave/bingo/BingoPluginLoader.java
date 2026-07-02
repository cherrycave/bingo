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

        String ktorVersion = "3.5.1";
        resolver.addDependency(new Dependency(new DefaultArtifact("io.ktor:ktor-client-core:" + ktorVersion), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.ktor:ktor-client-cio-jvm:" + ktorVersion), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.ktor:ktor-client-content-negotiation-jvm:" + ktorVersion), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.ktor:ktor-serialization-kotlinx-json-jvm:" + ktorVersion), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui:2.1.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("xyz.xenondevs.invui:invui-kotlin:2.1.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("net.megavex:scoreboard-library-api:2.8.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("net.megavex:scoreboard-library-implementation:2.8.0"), null));

        resolver.addRepository(new RemoteRepository.Builder("invui", "default", "https://repo.xenondevs.xyz/releases").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        classpathBuilder.addLibrary(resolver);
    }
}