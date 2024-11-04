package de.intranda.goobi.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

import de.intranda.beans.RowEntry;
import de.intranda.goobi.plugins.model.BreadcrumbItem;
import de.intranda.goobi.plugins.model.EntityType;
import de.sub.goobi.forms.NavigationForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.RulesetManager;
import io.goobi.workflow.locking.LockingBean;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.Prefs;

@PluginImplementation
@Log4j2
public class EntityDashboardPlugin implements IDashboardPlugin {

    private static final long serialVersionUID = -1947078064080952118L;

    @Getter
    private String title = "intranda_dashboard_entity";

    @Getter
    private PluginType type = PluginType.Dashboard;

    @Getter
    private String guiPath = "/uii/plugin_dashboard_entity.xhtml";

    @Getter
    private PluginGuiType pluginGuiType = PluginGuiType.FULL;

    public String loadEntityEdition(RowEntry entry) {

        if (!LockingBean.lockObject(entry.getProcessId(), Helper.getCurrentUser().getNachVorname())) {
            Helper.setFehlerMeldung("plugin_workflow_entity_locked");
            return guiPath;
        }
        BreadcrumbItem bci = new BreadcrumbItem(entry.getEntityType(), entry.getDisplayName(), Integer.parseInt(entry.getProcessId()), "", "");

        NavigationForm form = Helper.getBeanByClass(NavigationForm.class);

        IWorkflowPlugin plugin = form.getWorkflowPlugin();
        if (plugin == null) {
            form.setPlugin("intranda_workflow_entity_editor");
            plugin = form.getWorkflowPlugin();

        }

        try {
            Method selection = plugin.getClass().getMethod("setSelectedBreadcrumb", BreadcrumbItem.class);
            selection.invoke(plugin, bci);
            Method load = plugin.getClass().getMethod("loadSelectedBreadcrumb");
            load.invoke(plugin);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error(e);
        }

        return plugin.getGui();
    }

    public String createNewEntity(EntityType type) {

        NavigationForm form = Helper.getBeanByClass(NavigationForm.class);
        form.setPlugin("intranda_workflow_entity_editor");
        IWorkflowPlugin plugin = form.getWorkflowPlugin();

        try {
            // TODO this should come from the configuration file
            Prefs prefs = RulesetManager.getRulesetByName("Standard").getPreferences();
            Method setEntityType = plugin.getClass().getMethod("setEntityType", EntityType.class);
            setEntityType.invoke(plugin, type);
            Method setPrefs = plugin.getClass().getMethod("setPrefs", Prefs.class);
            setPrefs.invoke(plugin, prefs);

            Method createEntity = plugin.getClass().getMethod("createEntity");
            createEntity.invoke(plugin);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | DAOException e) {
            log.error(e);
        }

        return plugin.getGui();
    }

}
