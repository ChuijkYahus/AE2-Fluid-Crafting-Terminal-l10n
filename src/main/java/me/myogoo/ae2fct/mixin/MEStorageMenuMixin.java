package me.myogoo.ae2fct.mixin;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.ae2fct.init.AE2FCTDataComponent;
import me.myogoo.ae2fct.init.AE2FCTItems;
import me.myogoo.ae2fct.codec.VirtualFluid;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import appeng.api.storage.MEStorage;
import appeng.api.networking.energy.IEnergySource;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin {

    @Shadow
    protected MEStorage storage;

    @Shadow
    protected IEnergySource energySource;

    @Inject(method = "handleNetworkInteraction", at = @At("HEAD"), cancellable = true)
    private void interceptFluidClick(ServerPlayer player, @Nullable AEKey clickedKey, InventoryAction action,
            CallbackInfo ci) {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;

        if (clickedKey instanceof AEFluidKey fluidKey) {
            if (action == InventoryAction.PICKUP_OR_SET_DOWN) {
                if (menu.getCarried().isEmpty()) {
                    long extracted = StorageHelper.poweredExtraction(
                            this.energySource,
                            this.storage,
                            fluidKey,
                            1000,
                            menu.getActionSource());

                    if (extracted > 0) {
                        ItemStack virtualFluidItem = new ItemStack(AE2FCTItems.VIRTUAL_FLUID_ITEM.get());
                        virtualFluidItem.set(AE2FCTDataComponent.VIRTUAL_FLUID,
                                new VirtualFluid(new FluidStack(fluidKey.getFluid(), (int) extracted), 1000));
                        menu.setCarried(virtualFluidItem);
                        ci.cancel();
                    }
                }
            } else if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
                if (menu.getCarried().isEmpty()) {
                    long amount = Math.min(1000, (this.storage.getAvailableStacks().get(fluidKey) + 1) / 2);
                    long extracted = StorageHelper.poweredExtraction(
                            this.energySource,
                            this.storage,
                            fluidKey,
                            amount,
                            menu.getActionSource());
                    if (extracted > 0) {
                        ItemStack virtualFluidItem = new ItemStack(AE2FCTItems.VIRTUAL_FLUID_ITEM.get());
                        virtualFluidItem.set(AE2FCTDataComponent.VIRTUAL_FLUID,
                                new VirtualFluid(new FluidStack(fluidKey.getFluid(), (int) extracted), 1000));
                        menu.setCarried(virtualFluidItem);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
