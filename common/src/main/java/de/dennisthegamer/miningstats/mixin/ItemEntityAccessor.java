package de.dennisthegamer.miningstats.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes {@code ItemEntity.age} (private) so the fortune tracker can ignore old floor drops. */
@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {

    @Accessor("age")
    int miningStats$getAge();
}
