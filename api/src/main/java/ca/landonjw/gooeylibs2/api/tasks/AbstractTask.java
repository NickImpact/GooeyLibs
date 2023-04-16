package ca.landonjw.gooeylibs2.api.tasks;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class AbstractTask
implements Task {
    private final Consumer<Task> consumer;
    private final boolean async;
    private final long interval;
    private long currentIteration;
    private final long iterations;
    private long ticksRemaining;
    private boolean expired;
    protected ScheduledTask delegate;

    protected AbstractTask(AbstractTaskBuilder<?> builder) {
        this.consumer = builder.consumer;
        this.async = builder.async;
        this.interval = builder.interval;
        this.iterations = builder.iterations;
        if (builder.delay > 0L) {
            this.ticksRemaining = builder.delay;
        }
        this.delegate = builder.async ? AsyncTaskScheduler.schedule(this) : this.sync();
    }

    protected abstract ScheduledTask sync();

    @Override
    public boolean isAsync() {
        return this.async;
    }

    @Override
    public long delay() {
        return this.interval;
    }

    @Override
    public long iterations() {
        return this.iterations;
    }

    @Override
    public Consumer<Task> executor() {
        return this.consumer;
    }

    @Override
    public boolean infinite() {
        return this.iterations == -1L;
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    @Override
    public void setExpired() {
        this.expired = true;
    }

    protected void tick() {
        if (!this.expired) {
            this.ticksRemaining = Math.max(0L, --this.ticksRemaining);
            if (this.ticksRemaining == 0L) {
                this.consumer.accept(this);
                ++this.currentIteration;
                if (this.interval > 0L && (this.currentIteration < this.iterations || this.iterations == -1L)) {
                    this.ticksRemaining = this.interval;
                } else {
                    this.expired = true;
                }
            }
        }
    }

    public static abstract class AbstractTaskBuilder<T extends Task>
    implements Task.TaskBuilder {
        protected boolean async;
        protected Consumer<Task> consumer;
        protected long delay;
        protected long interval;
        protected long iterations = 1L;

        @Override
        public Task.TaskBuilder async() {
            this.async = true;
            return this;
        }

        @Override
        public Task.TaskBuilder execute(@Nonnull Runnable runnable) {
            this.consumer = task -> runnable.run();
            return this;
        }

        @Override
        public Task.TaskBuilder execute(@Nonnull Consumer<Task> consumer) {
            this.consumer = consumer;
            return this;
        }

        @Override
        public Task.TaskBuilder delay(long delay) {
            if (delay < 0L) {
                throw new IllegalArgumentException("delay must not be below 0");
            }
            this.delay = delay;
            return this;
        }

        @Override
        public Task.TaskBuilder interval(long interval) {
            if (interval < 0L) {
                throw new IllegalArgumentException("interval must not be below 0");
            }
            this.interval = interval;
            return this;
        }

        @Override
        public Task.TaskBuilder iterations(long iterations) {
            if (iterations < -1L) {
                throw new IllegalArgumentException("iterations must not be below -1");
            }
            this.iterations = iterations;
            return this;
        }

        @Override
        public Task.TaskBuilder infinite() {
            return this.iterations(-1L);
        }

        public abstract T build();
    }
}