package me.myogoo.ae2fct.mixin;

import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.util.prioritylist.IPartitionList;
import me.myogoo.ae2fct.item.VirtualFluidItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = FillCraftingGridFromRecipePacket.class, remap = false)
public class FillCraftingGridFromRecipePacketMixin {

    @Inject(method = "findBestMatchingItemStack", at = @At("RETURN"), cancellable = true)
    private void optimizedFindBestMatchingItemStack(Ingredient ingredient, IPartitionList filter, KeyCounter storage,
            CallbackInfoReturnable<List<AEItemKey>> cir) {
        Set<AEItemKey> enhancedResults = null;
        Set<Fluid> checkedFluids = null;

        for (ItemStack stack : ingredient.getItems()) {
            FluidStack fluidInItem = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (!fluidInItem.isEmpty()) {
                Fluid fluid = fluidInItem.getFluid();
                if (checkedFluids == null)
                    checkedFluids = new HashSet<>();
                if (checkedFluids.add(fluid)) {
                    AEFluidKey fluidKey = AEFluidKey.of(fluid);
                    if (fluidKey != null && storage.get(fluidKey) > 0) {
                        AEItemKey vKey = AEItemKey.of(VirtualFluidItem.createItemStack(fluid));
                        if (vKey != null && (filter == null || filter.isListed(vKey))) {
                            if (enhancedResults == null)
                                enhancedResults = new LinkedHashSet<>();
                            enhancedResults.add(vKey);
                        }
                    }
                }
            }
        }

        if (enhancedResults != null && !enhancedResults.isEmpty()) {
            List<AEItemKey> originalResults = cir.getReturnValue();
            if (originalResults != null) {
                enhancedResults.addAll(originalResults);
            }
            cir.setReturnValue(new ArrayList<>(enhancedResults));
        }
    }

    /**
     * findCraftableKey가 아이템으로 craftable한 것을 찾지 못한 경우,
     * ingredient에 포함된 fluid가 craftable한지 확인하여 VirtualFluidItem의 AEItemKey를 반환합니다.
     */
    @Inject(method = "findCraftableKey", at = @At("RETURN"), cancellable = true)
    private void checkFluidCraftableKey(Ingredient ingredient, ICraftingService craftingService,
            CallbackInfoReturnable<Optional<AEItemKey>> cir) {
        if (cir.getReturnValue().isPresent()) {
            return; // 이미 아이템으로 craftable한 것을 찾았으면 추가 검사 불필요
        }

        for (ItemStack stack : ingredient.getItems()) {
            FluidStack fluidInItem = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (!fluidInItem.isEmpty()) {
                AEFluidKey fluidKey = AEFluidKey.of(fluidInItem.getFluid());
                if (fluidKey != null) {
                    var craftable = craftingService.getFuzzyCraftable(fluidKey, key -> key.equals(fluidKey));
                    if (craftable != null) {
                        // VirtualFluidItem의 AEItemKey를 반환 (AutoCraftEntry가 AEItemKey를 요구하므로)
                        AEItemKey vKey = AEItemKey.of(VirtualFluidItem.createItemStack(fluidInItem.getFluid()));
                        if (vKey != null) {
                            cir.setReturnValue(Optional.of(vKey));
                            return;
                        }
                    }
                }
            }
        }
    }
}
