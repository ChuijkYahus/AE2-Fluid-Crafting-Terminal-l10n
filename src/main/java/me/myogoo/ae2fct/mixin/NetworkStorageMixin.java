package me.myogoo.ae2fct.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.me.storage.NetworkStorage;
import me.myogoo.ae2fct.codec.VirtualFluid;
import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import me.myogoo.ae2fct.init.AE2FCTItems;
import net.neoforged.neoforge.fluids.FluidStack;

@Mixin(value = NetworkStorage.class, remap = false)
public abstract class NetworkStorageMixin {

    @Shadow
    public abstract long insert(AEKey what, long amount, Actionable type, IActionSource src);

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void onInsert(AEKey what, long amount, Actionable type, IActionSource src,
            CallbackInfoReturnable<Long> cir) {
        if (what instanceof AEItemKey itemKey && itemKey.is(AE2FCTItems.VIRTUAL_FLUID_ITEM.get())) {
            VirtualFluid virtualFluid = itemKey.get(AE2FCTDataComponent.VIRTUAL_FLUID);
            if (virtualFluid != null) {
                FluidStack fluidStack = virtualFluid.fluid();
                if (!fluidStack.isEmpty()) {
                    long fluidAmountPerItem = fluidStack.getAmount();
                    long totalFluidToInsert = amount * fluidAmountPerItem;
                    AEFluidKey fluidKey = AEFluidKey.of(fluidStack.getFluid());

                    if (fluidKey != null) {
                        // First simulate insertion to check how much fluid can actually fit
                        long simulatedFluidInserted = this.insert(fluidKey, totalFluidToInsert, Actionable.SIMULATE,
                                src);

                        // Calculate how many full items we can insert
                        long itemsToInsert = simulatedFluidInserted / fluidAmountPerItem;

                        // Only perform the action if we can insert at least 1 full item, and the mode
                        // is MODULATE
                        if (itemsToInsert > 0 && type == Actionable.MODULATE) {
                            this.insert(fluidKey, itemsToInsert * fluidAmountPerItem, Actionable.MODULATE, src);
                        }

                        // Return the number of items successfully (or simula-ably) inserted
                        cir.setReturnValue(itemsToInsert);
                    }
                }
            }
        }
    }
}
