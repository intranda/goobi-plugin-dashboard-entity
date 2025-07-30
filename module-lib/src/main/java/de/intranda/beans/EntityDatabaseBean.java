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
        sql.append("SELECT ");
        sql.append("e.object_id, ");
        sql.append("e.property_value AS currentStatus, ");
        sql.append("e.creation_date AS date, ");
        sql.append("m1.value AS docstruct, ");
        sql.append("p2.property_value AS title ");
        sql.append("FROM metadata m1 ");
        sql.append("JOIN properties e ON e.object_id = m1.processid ");
        sql.append("JOIN properties p2 ON p2.object_id = m1.processid ");
        if (StringUtils.isNotBlank(type.getSearchValue())) {
            sql.append("left join metadata m2 on e.object_id = m2.processid ");
        }
        sql.append("WHERE m1.name = 'docstruct' ");
        sql.append("AND m1.value = '").append(type.getName()).append("' ");
        sql.append("AND e.property_name = 'ProcessStatus' ");
        sql.append("AND e.creation_date IS NOT NULL ");
        sql.append("AND p2.property_name = 'DisplayName' ");
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

        sql.append("ORDER BY e.creation_date DESC; ");

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

        //        CREATE INDEX idx_metadata_name_value_proc ON metadata(name, value(190), processid);
        //        CREATE INDEX idx_properties_objectid_propertyname ON properties(object_id, property_name);
        //        CREATE INDEX idx_properties_objectid_propertyname_date ON properties(object_id, property_name, creation_date);

        return answer;
    }

}
