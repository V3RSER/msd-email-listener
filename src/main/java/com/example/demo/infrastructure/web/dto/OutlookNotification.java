package com.example.demo.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutlookNotification {

    private List<Value> value;

    public List<Value> getValue() {
        return value;
    }

    public void setValue(List<Value> value) {
        this.value = value;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private String changeType;
        private ResourceData resourceData;

        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }

        public ResourceData getResourceData() {
            return resourceData;
        }

        public void setResourceData(ResourceData resourceData) {
            this.resourceData = resourceData;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceData {
        @JsonProperty("@odata.type")
        private String odataType;
        @JsonProperty("@odata.id")
        private String odataId;
        private String id;

        public String getOdataType() {
            return odataType;
        }

        public void setOdataType(String odataType) {
            this.odataType = odataType;
        }

        public String getOdataId() {
            return odataId;
        }

        public void setOdataId(String odataId) {
            this.odataId = odataId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
