package de.intranda.goobi.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

import de.intranda.goobi.plugins.model.BreadcrumbItem;
import de.intranda.goobi.plugins.model.EntityConfig;
import de.intranda.goobi.plugins.model.EntityType;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.forms.NavigationForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
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
    private String value;

    @Getter
    private PluginGuiType pluginGuiType = PluginGuiType.FULL;

    @Getter
    private transient EntityConfig configuration;

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

    public List<RowEntry> getEntityData(EntityType type) {
        List<RowEntry> answer = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(
                "select e.prozesseID, e.Wert as currentStatus, e.creationDate as date, m1.value as docstruct, p2.Wert as title ");
        sql.append("from prozesseeigenschaften e left join metadata m1 on e.prozesseID = m1.processid ");
        sql.append("left join metadata m2 on e.prozesseID = m2.processid ");

        sql.append("left join prozesseeigenschaften p2 on e.prozesseID = p2.prozesseID ");

        sql.append("where e.titel = \"ProcessStatus\" and m1.name=\"docstruct\" ");
        sql.append("and p2.titel =\"DisplayName\" ");
        sql.append("and m1.value =\"");
        sql.append(type.getName());
        sql.append("\" ");
        sql.append("and m2.name = \"index.EntitySearch\" ");
        if (StringUtils.isNotBlank(type.getSearchValue())) {
            sql.append("and m2.value like \"%");
            String searchTerm = type.getSearchValue();
            searchTerm = searchTerm.replace(" ", "%");
            searchTerm = searchTerm.replace("`", "_");
            searchTerm = searchTerm.replace("’", "_");
            searchTerm = searchTerm.replace("\'", "_");
            sql.append(searchTerm);
            sql.append("%\" ");
        }

        sql.append("order by date desc ");
        //        sql.append("limit 50 ");

        List<?> rows = ProcessManager.runSQL(sql.toString());
        for (Object obj : rows) {
            Object[] objArr = (Object[]) obj;
            String processid = (String) objArr[0];
            String processStatus = (String) objArr[1];
            String processDate = (String) objArr[2];
            String docstruct = (String) objArr[3];
            String entityTitle = (String) objArr[4];
            RowEntry entry = new RowEntry();
            entry.setDisplayName(entityTitle);
            entry.setLastModified(processDate);
            entry.setEntityType(docstruct);
            entry.setStatus(processStatus);
            entry.setProcessId(processid);
            answer.add(entry);
        }

        return answer;
    }

    public String loadEntityEdition(RowEntry entry) {

        if (!LockingBean.lockObject(entry.getProcessId(), Helper.getCurrentUser().getNachVorname())) {
            Helper.setFehlerMeldung("plugin_workflow_entity_locked");
            return guiPath;
        }
        BreadcrumbItem bci = new BreadcrumbItem(entry.getEntityType(), entry.getDisplayName(), Integer.parseInt(entry.getProcessId()), "", "");

        NavigationForm form = Helper.getBeanByClass(NavigationForm.class);

        form.setPlugin("intranda_workflow_entity_editor");
        IWorkflowPlugin plugin = form.getWorkflowPlugin();

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
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | DAOException e) {
            log.error(e);
        }

        return plugin.getGui();
    }



}
