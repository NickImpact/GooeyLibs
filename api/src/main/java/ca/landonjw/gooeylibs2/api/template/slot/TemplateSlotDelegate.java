package ca.landonjw.gooeylibs2.api.template.slot;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.data.EventEmitter;
import ca.landonjw.gooeylibs2.api.data.Subject;
import org.jetbrains.annotations.Nullable;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Consumer;

public final class TemplateSlotDelegate implements Subject<TemplateSlotDelegate> {

    private final EventEmitter<TemplateSlotDelegate> eventEmitter = new EventEmitter<>();
    private Button button;
    private final int index;

    public TemplateSlotDelegate(@Nullable Button button, int index) {
        this.button = button;
        this.index = index;
    }

    public Optional<Button> getButton() {
        return Optional.ofNullable(this.button);
    }

    public void setButton(@Nullable Button button) {
        if (this.button != null) {
            this.button.unsubscribe(this);
        }

        this.button = button;
        if (this.button != null) {
            this.button.subscribe(this, this::update);
        }

        this.update();
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public void subscribe(@Nonnull Object observer, @Nonnull Consumer<TemplateSlotDelegate> consumer) {
        this.eventEmitter.subscribe(observer, consumer);
    }

    @Override
    public void unsubscribe(@Nonnull Object observer) {
        this.eventEmitter.unsubscribe(observer);
    }

    public void update() {
        this.eventEmitter.emit(this);
    }
}
