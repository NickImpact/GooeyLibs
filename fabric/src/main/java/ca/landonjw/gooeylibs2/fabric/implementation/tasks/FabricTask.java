/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
 *  net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents$EndTick
 *  net.minecraft.server.MinecraftServer
 */
package ca.landonjw.gooeylibs2.fabric.implementation.tasks;

import ca.landonjw.gooeylibs2.api.tasks.AbstractTask;
import ca.landonjw.gooeylibs2.api.tasks.ScheduledTask;

import java.util.*;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class FabricTask
extends AbstractTask {
    private static final FabricEndTickParent parent = new FabricEndTickParent();
    private FabricEndTickChild event;

    private FabricTask(FabricTaskBuilder builder) {
        super(builder);
    }

    public void onServerTick(MinecraftServer server) {
        this.tick();
        if (this.isExpired()) {
            this.delegate.stop();
        }
    }

    @Override
    protected ScheduledTask sync() {
        this.event = new FabricEndTickChild(this::onServerTick);
        parent.register(this.event);
        return () -> parent.unregister(this.event);
    }

    static {
        parent.initialize();
    }

    public static class FabricEndTickChild implements ServerTickEvents.EndTick {
        private final UUID identifier = UUID.randomUUID();
        private final ServerTickEvents.EndTick delegate;

        public FabricEndTickChild(ServerTickEvents.EndTick delegate) {
            this.delegate = delegate;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            FabricEndTickChild that = (FabricEndTickChild)o;
            return Objects.equals(this.identifier, that.identifier);
        }

        public int hashCode() {
            return Objects.hash(this.identifier);
        }

        public void onEndTick(MinecraftServer server) {
            this.delegate.onEndTick(server);
        }
    }

    public static class FabricEndTickParent
    implements ServerTickEvents.EndTick {
        private final Collection<ServerTickEvents.EndTick> children = new HashSet<>();
        private boolean initialized;

        public void initialize() {
            if (!this.initialized) {
                ServerTickEvents.END_SERVER_TICK.register(this);
                initialized = true;
            }
        }

        public void register(FabricEndTickChild child) {
            this.children.add(child);
        }

        private void unregister(FabricEndTickChild child) {
            this.children.remove(child);
        }

        public void onEndTick(MinecraftServer server) {
            new ArrayList<>(this.children).forEach(child -> child.onEndTick(server));
        }
    }

    public static class FabricTaskBuilder extends AbstractTask.AbstractTaskBuilder<FabricTask> {
        @Override
        public FabricTask build() {
            if (this.consumer == null) {
                throw new IllegalStateException("consumer must be set");
            }
            return new FabricTask(this);
        }
    }
}

