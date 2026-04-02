package com.example.demo.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OutlookNotification {

    @Valid
    @NotEmpty
    private List<Value> value;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Value {
        @NotBlank
        private String changeType;
        @Valid
        @NotNull
        private ResourceData resourceData;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ResourceData {
        @JsonProperty("@odata.type")
        private String odataType;

        @NotBlank
        @JsonProperty("@odata.id")
        private String odataId;

        @NotBlank
        private String id;
    }
}
