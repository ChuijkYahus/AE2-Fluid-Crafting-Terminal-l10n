package me.myogoo.ae2fct.mixin.et;

import appeng.menu.me.common.MEStorageMenu;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.myogoo.ae2fct.util.ExtendedTerminalCompatHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "me.myogoo.extendedterminal.network.serverbound.FillTableCraftingGridFromRecipePacket", remap = false)
public abstract class ETFillTableCraftingGridFromRecipePacketMixin {

    @WrapMethod(method = "handleOnServer")
    private void ae2fct$withFluidCraftingState(ServerPlayer player, Operation<Void> original) {
        boolean enabled = false;
        if (player.containerMenu instanceof MEStorageMenu menu) {
            enabled = ExtendedTerminalCompatHelper.hasFluidInteractUpgrade(menu);
        }

        ExtendedTerminalCompatHelper.FLUID_CRAFTING_ENABLED.set(enabled);
        try {
            original.call(player);
        } finally {
            ExtendedTerminalCompatHelper.FLUID_CRAFTING_ENABLED.remove();
        }
    }
}
