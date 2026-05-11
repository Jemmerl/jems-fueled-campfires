package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.JemsCampfires;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JemsCampfireMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (compatFarmersDelight(mixinClassName) && (LoadingModList.get().getModFileById("farmersdelight") != null)) {
            return hasClass(targetClassName);
        }

        return true;
    }

    private boolean compatFarmersDelight(String mixinClassName) {
        return ((Objects.equals(mixinClassName, "com.jemmerl.jemscampfires.mixin.compat.farmersdelight.FDStoveBlockMixins"))
                || ((Objects.equals(mixinClassName, "com.jemmerl.jemscampfires.mixin.compat.farmersdelight.FDStoveTEMixins"))));
    }


    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static boolean hasClass(String name) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(name);
            return true;
        } catch (ClassNotFoundException e) {
            mixinFailError(name);
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            mixinFailError(name);
            return false;
        }
    }

    private static void mixinFailError(String name) {
        JemsCampfires.LOGGER.error(JemsCampfires.MOD_ID + ": Mixin into class {} failed; class not present.", name);
    }
}
