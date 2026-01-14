package com.ouimet.f1.fantasy_service.dto;

import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class LoginSessionDTO {
    private String subscriptionToken;
    private JsonNode data;
}
