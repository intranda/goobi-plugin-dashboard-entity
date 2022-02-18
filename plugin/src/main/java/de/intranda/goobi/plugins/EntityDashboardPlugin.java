package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.intranda.goobi.plugins.model.EntityConfig;
import de.intranda.goobi.plugins.model.EntityType;
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

    @Getter
    private EntityConfig configuration;

    /**
     * Constructor
     */
    public EntityDashboardPlugin() {
        log.info("Entity dashboard plugin started");
    }

    private void loadConfiguration() {
        XMLConfiguration config = ConfigPlugins.getPluginConfig("intranda_workflow_entity_editor");
        config.setExpressionEngine(new XPathExpressionEngine());
        configuration = new EntityConfig(config);
    }

    public List<EntityType> getAllEntityTypes() {

        if (configuration == null) {
            loadConfiguration();
        }

        return configuration.getAllTypes();
    }

    public List<RowEntry> getTestData() {

        List<RowEntry> answer = new ArrayList<>();
        {
            RowEntry entry = new RowEntry();
            entry.setDisplayName("Pablo Picasso");
            entry.setLastModified("2022-02-18 10:00:00");
            entry.setEntityType("Person");
            entry.setStatus("New");
            entry.setProcessId(7);
            answer.add(entry);
        }

        {
            RowEntry entry = new RowEntry();
            entry.setDisplayName("Louvre");
            entry.setLastModified("1970-01-01 00:00:00");
            entry.setEntityType("Agency");
            entry.setStatus("Published");
            entry.setProcessId(8);
            answer.add(entry);
        }


        return answer;
    }

}
