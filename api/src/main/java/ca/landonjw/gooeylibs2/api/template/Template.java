package ca.landonjw.gooeylibs2.api.template;

import ca.landonjw.gooeylibs2.api.data.UpdateEmitter;
import ca.landonjw.gooeylibs2.api.template.slot.TemplateSlotDelegate;
import com.google.common.collect.ImmutableList;

import jakarta.annotation.Nonnull;
import java.util.List;

public abstract class Template extends UpdateEmitter<Template> {

    protected final TemplateType templateType;
    private final TemplateSlotDelegate[] slots;

    protected Template(@Nonnull TemplateType templateType, @Nonnull TemplateSlotDelegate[] slots) {
        this.templateType = templateType;
        this.slots = slots;
    }

    public final TemplateType getTemplateType() {
        return this.templateType;
    }

    public final int getSize() {
        return this.slots.length;
    }

    public final TemplateSlotDelegate getSlot(int index) {
        return this.slots[index];
    }

    public final List<TemplateSlotDelegate> getSlots() {
        return ImmutableList.copyOf(this.slots);
    }

    public final void setSlot(int index, TemplateSlotDelegate slot) {
        this.slots[index] = slot;
    }

    public abstract Template clone();

}