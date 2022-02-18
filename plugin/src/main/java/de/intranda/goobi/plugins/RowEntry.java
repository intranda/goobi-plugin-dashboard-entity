package de.intranda.goobi.plugins;

import lombok.Data;

@Data
public class RowEntry {

    private String entityType;

    private String displayName;
    private String lastModified;
    private String status;
    private Integer processId;

}
