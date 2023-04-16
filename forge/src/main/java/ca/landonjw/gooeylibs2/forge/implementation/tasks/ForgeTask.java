package ca.landonjw.gooeylibs2.forge.implementation.tasks;


import ca.landonjw.gooeylibs2.api.tasks.AbstractTask;
import ca.landonjw.gooeylibs2.api.tasks.ScheduledTask;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.function.Consumer;

public final class ForgeTask extends AbstractTask {
    private Consumer<TickEvent.ServerTickEvent> event;

    ForgeTask(ForgeTaskBuilder builder) {
        super(builder);
    }

    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tick();
            if (isExpired()) {
                MinecraftForge.EVENT_BUS.unregister(this.event);
            }
        }
    }

    @Override
    protected ScheduledTask sync() {
        this.event = this::onServerTick;
        MinecraftForge.EVENT_BUS.addListener(this.event);
        return () -> MinecraftForge.EVENT_BUS.unregister(this.event);
    }

    public static final class ForgeTaskBuilder
            extends AbstractTask.AbstractTaskBuilder<ForgeTask> {
        @Override
        public ForgeTask build() {
            if (consumer == null) {
                throw new IllegalStateException("consumer must be set");
            }
            return new ForgeTask(this);
        }
    }
}
