package com.example.demo.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OutlookNotification {

    private List<Value> value;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Value {
        private String changeType;
        private ResourceData resourceData;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ResourceData {
        @JsonProperty("@odata.type")
        private String odataType;
        @JsonProperty("@odata.id")
        private String odataId;
        private String id;
    }
}
