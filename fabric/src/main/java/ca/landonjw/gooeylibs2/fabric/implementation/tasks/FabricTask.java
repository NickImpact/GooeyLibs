package ca.landonjw.gooeylibs2.fabric.implementation.tasks;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import jakarta.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

public class FabricTask implements Task {

    private static final FabricEndTickParent parent = new FabricEndTickParent();

    private final Consumer<Task> consumer;
    private final FabricEndTickChild event;

    private final long interval;
    private long currentIteration;
    private final long iterations;

    private long ticksRemaining;
    private boolean expired;

    FabricTask(Consumer<Task> consumer, long delay, long interval, long iterations) {
        this.consumer = consumer;
        this.interval = interval;
        this.iterations = iterations;

        if (delay > 0) {
            ticksRemaining = delay;
        }

        parent.initialize();
        this.event = new FabricEndTickChild(this::onServerTick);
        parent.register(this.event);
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired() {
        expired = true;
    }

    void tick() {
        if (!expired) {
            this.ticksRemaining = Math.max(0, --ticksRemaining);

            if (ticksRemaining == 0) {
                consumer.accept(this);
                currentIteration++;

                if (interval > 0 && (currentIteration < iterations || iterations == -1)) {
                    ticksRemaining = interval;
                } else {
                    expired = true;
                }
            }
        }
    }

    public void onServerTick(MinecraftServer server) {
        tick();
        if (isExpired()) {
            parent.unregister(this.event);
        }
    }

    public static class FabricTaskBuilder implements TaskBuilder {

        private Consumer<Task> consumer;
        private long delay;
        private long interval;
        private long iterations = 1;

        public TaskBuilder execute(@Nonnull Runnable runnable) {
            this.consumer = (task) -> runnable.run();
            return this;
        }

        public TaskBuilder execute(@Nonnull Consumer<Task> consumer) {
            this.consumer = consumer;
            return this;
        }

        public TaskBuilder delay(long delay) {
            if (delay < 0) {
                throw new IllegalArgumentException("delay must not be below 0");
            }
            this.delay = delay;
            return this;
        }

        public TaskBuilder interval(long interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("interval must not be below 0");
            }
            this.interval = interval;
            return this;
        }

        public TaskBuilder iterations(long iterations) {
            if (iterations < -1) {
                throw new IllegalArgumentException("iterations must not be below -1");
            }
            this.iterations = iterations;
            return this;
        }

        public TaskBuilder infinite() {
            return iterations(-1);
        }

        public FabricTask build() {
            if (consumer == null) {
                throw new IllegalStateException("consumer must be set");
            }
            return new FabricTask(consumer, delay, interval, iterations);
        }

    }

    public static class FabricEndTickParent implements ServerTickEvents.EndTick {

        private final Collection<ServerTickEvents.EndTick> children = new HashSet<>();
        private boolean initialized;

        public void initialize() {
            if(!this.initialized) {
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

        @Override
        public void onEndTick(MinecraftServer server) {
            new ArrayList<>(this.children).forEach(child -> child.onEndTick(server));
        }
    }

    public static class FabricEndTickChild implements ServerTickEvents.EndTick {

        private final UUID identifier = UUID.randomUUID();
        private final ServerTickEvents.EndTick delegate;

        public FabricEndTickChild(ServerTickEvents.EndTick delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FabricEndTickChild that = (FabricEndTickChild) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

        @Override
        public void onEndTick(MinecraftServer server) {
            this.delegate.onEndTick(server);
        }
    }
}
