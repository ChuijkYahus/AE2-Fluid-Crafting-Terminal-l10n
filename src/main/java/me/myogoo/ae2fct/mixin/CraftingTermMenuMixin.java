package me.myogoo.ae2fct.mixin;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = CraftingTermMenu.class, remap = false)
public abstract class CraftingTermMenuMixin extends MEStorageMenu {

    // MEStorageMenu의 생성자를 만족시키기 위한 더미 - 실제로 호출되지 않음
    private CraftingTermMenuMixin() {
        super(null, 0, null, null, false);
    }

    /**
     * findMissingIngredients의 결과에서 fluid 네트워크를 추가로 검사하여
     * fluid로 충족 가능한 재료를 missing/craftable에서 제거합니다.
     */
    @Inject(method = "findMissingIngredients", at = @At("RETURN"), cancellable = true)
    private void checkFluidForMissingIngredients(Map<Integer, Ingredient> ingredients,
            CallbackInfoReturnable<CraftingTermMenu.MissingIngredientSlots> cir) {
        CraftingTermMenu.MissingIngredientSlots result = cir.getReturnValue();
        if (!result.anyMissingOrCraftable()) {
            return;
        }

        IClientRepo clientRepo = this.getClientRepo();
        if (clientRepo == null) {
            return;
        }

        Set<Integer> newMissing = new HashSet<>(result.missingSlots());
        Set<Integer> newCraftable = new HashSet<>(result.craftableSlots());
        boolean changed = false;

        // missing과 craftable 슬롯 모두 검사
        Set<Integer> slotsToCheck = new HashSet<>();
        slotsToCheck.addAll(result.missingSlots());
        slotsToCheck.addAll(result.craftableSlots());

        for (int slot : slotsToCheck) {
            Ingredient ingredient = ingredients.get(slot);
            if (ingredient == null)
                continue;

            // ingredient의 아이템들에서 fluid를 추출하여 확인
            boolean fluidFound = false;
            boolean fluidCraftable = false;

            for (ItemStack stack : ingredient.getItems()) {
                FluidStack fluidInItem = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
                if (!fluidInItem.isEmpty()) {
                    AEFluidKey fluidKey = AEFluidKey.of(fluidInItem.getFluid());
                    if (fluidKey == null)
                        continue;

                    // client repo에서 해당 fluid가 존재하는지 확인
                    for (var entry : clientRepo.getAllEntries()) {
                        AEKey what = entry.getWhat();
                        if (what instanceof AEFluidKey entryFluidKey && entryFluidKey.equals(fluidKey)) {
                            if (entry.getStoredAmount() >= 1000) {
                                fluidFound = true;
                                break;
                            }
                            if (entry.isCraftable()) {
                                fluidCraftable = true;
                            }
                        }
                    }
                    if (fluidFound)
                        break;
                }
            }

            if (fluidFound) {
                // fluid가 네트워크에 존재 → missing/craftable에서 제거
                newMissing.remove(slot);
                newCraftable.remove(slot);
                changed = true;
            } else if (fluidCraftable && newMissing.contains(slot)) {
                // fluid가 craftable → missing에서 craftable로 이동
                newMissing.remove(slot);
                newCraftable.add(slot);
                changed = true;
            }
        }

        if (changed) {
            cir.setReturnValue(new CraftingTermMenu.MissingIngredientSlots(newMissing, newCraftable));
        }
    }

    /**
     * isCraftable에서 아이템이 craftable하지 않은 경우,
     * 해당 아이템에서 fluid를 추출하여 fluid가 craftable인지도 확인합니다.
     */
    @Inject(method = "isCraftable", at = @At("RETURN"), cancellable = true)
    private void checkFluidCraftable(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return; // 이미 craftable이면 추가 검사 불필요
        }

        FluidStack fluidInItem = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
        if (fluidInItem.isEmpty()) {
            return;
        }

        AEFluidKey fluidKey = AEFluidKey.of(fluidInItem.getFluid());
        if (fluidKey == null) {
            return;
        }

        IClientRepo clientRepo = this.getClientRepo();
        if (clientRepo == null) {
            return;
        }

        for (var entry : clientRepo.getAllEntries()) {
            if (entry.isCraftable() && fluidKey.equals(entry.getWhat())) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
