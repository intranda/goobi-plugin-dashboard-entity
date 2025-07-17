package de.intranda.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;

import de.intranda.goobi.plugins.model.EntityConfig;
import de.intranda.goobi.plugins.model.EntityType;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;

@Named
@SessionScoped
public class EntityDatabaseBean implements Serializable {

    private static final long serialVersionUID = 8069177096859541900L;
    @Getter
    private transient EntityConfig configuration;

    private void loadConfiguration() {
        XMLConfiguration config = ConfigPlugins.getPluginConfig("intranda_workflow_entity_editor");
        config.setExpressionEngine(new XPathExpressionEngine());
        configuration = new EntityConfig(config, false);
    }

    public List<EntityType> getAllEntityTypes() {
        try {
            if (configuration == null) {
                loadConfiguration();
            }
            return configuration.getAllTypes();
        } catch (RuntimeException e) {
            Helper.setFehlerMeldung(e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<RowEntry> getEntityData(EntityType type) {

        List<RowEntry> answer = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(
                "select e.object_id, e.property_value as currentStatus, e.creation_date as date, m1.value as docstruct, p2.property_value as title ");
        sql.append("from properties e left join metadata m1 on e.object_id = m1.processid AND e.creation_date IS NOT NULL AND ");
        sql.append("e.property_name = 'ProcessStatus' LEFT JOIN properties p2 ON e.object_id = p2.object_id AND p2.property_name = 'DisplayName' ");

        if (StringUtils.isNotBlank(type.getSearchValue())) {
            sql.append("left join metadata m2 on e.object_id = m2.processid ");
        }
        sql.append("where m1.name='docstruct' ");
        sql.append("and m1.value ='");
        sql.append(type.getName());
        sql.append("' ");
        if (StringUtils.isNotBlank(type.getSearchValue())) {
            sql.append("and m2.name = \"index.EntitySearch\" ");
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
        sql.append("limit 500 ");

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

}
