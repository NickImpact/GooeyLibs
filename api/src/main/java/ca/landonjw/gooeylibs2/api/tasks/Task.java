package ca.landonjw.gooeylibs2.api.tasks;

import ca.landonjw.gooeylibs2.bootstrap.GooeyBootstrapper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface Task {
    boolean isAsync();

    long delay();

    long iterations();

    Consumer<Task> executor();

    boolean infinite();

    boolean isExpired();

    void setExpired();

    static TaskBuilder builder() {
        return GooeyBootstrapper.instance().builders().provide(TaskBuilder.class);
    }

    interface TaskBuilder {
        TaskBuilder async();

        TaskBuilder execute(@Nonnull Runnable runnable);

        TaskBuilder execute(@Nonnull Consumer<Task> consumer);

        TaskBuilder delay(long delay);

        TaskBuilder interval(long interval);

        TaskBuilder iterations(long iterations);

        TaskBuilder infinite();

        Task build();
    }

}
