package me.myogoo.ae2fct.util;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.ae2fct.init.AE2FCTItems;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;

public final class ExtendedTerminalCompatHelper {

    public static final ThreadLocal<Boolean> FLUID_CRAFTING_ENABLED = ThreadLocal.withInitial(() -> false);

    private ExtendedTerminalCompatHelper() {
    }

    public static boolean isSupportedMenu(MEStorageMenu menu) {
        String className = menu.getClass().getName();
        return className.startsWith("me.myogoo.extendedterminal.menu.extendedterminal.")
                || className.startsWith("me.myogoo.extendedterminal.menu.extendedcrafting.")
                || className.startsWith("me.myogoo.extendedterminal.menu.avaritiaRe.")
                || className.equals("me.myogoo.extendedterminal.menu.avaritiaNeo.NeoExtremeTerminalMenu");
    }

    public static boolean hasFluidInteractUpgrade(MEStorageMenu menu) {
        return isSupportedMenu(menu) && TerminalUpgradeHelper.hasUpgrade(menu, AE2FCTItems.TERMINAL_FLUID_INTERACT_CARD.get());
    }
}
