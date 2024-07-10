package de.intranda.beans;

import lombok.Data;

@Data
public class RowEntry {

    private String entityType;
    private String displayName;
    private String lastModified;
    private String status;
    private String processId;
}
