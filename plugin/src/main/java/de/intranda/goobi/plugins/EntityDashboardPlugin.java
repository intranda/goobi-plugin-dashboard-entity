package de.intranda.goobi.plugins;

import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.config.ConfigPlugins;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class EntityDashboardPlugin implements IDashboardPlugin {

    @Getter
    private String title = "intranda_dashboard_entity";

    @Getter
    private PluginType type = PluginType.Dashboard;

    @Getter
    private String guiPath = "/uii/plugin_dashboard_entity.xhtml";

    @Getter
    private String value;

    @Getter
    private PluginGuiType pluginGuiType = PluginGuiType.FULL;

    /**
     * Constructor
     */
    public EntityDashboardPlugin() {
        log.info("Entity dashboard plugin started");
        value = ConfigPlugins.getPluginConfig(title).getString("value", "default value");
    }

}
