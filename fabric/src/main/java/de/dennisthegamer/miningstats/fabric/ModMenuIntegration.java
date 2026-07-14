package de.dennisthegamer.miningstats.fabric;

import de.dennisthegamer.miningstats.config.ModConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // The config screen needs YACL. If it isn't installed, don't touch ModConfigScreen
        // (which references YACL classes) - returning it would crash ModMenu with a
        // NoClassDefFoundError ("broken implementation of ModMenuApi").
        if (!FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return parent -> null;
        }
        return ModConfigScreen::create;
    }
}
