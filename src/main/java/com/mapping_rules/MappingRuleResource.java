package com.mapping_rules;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/mapping-rule")
public class MappingRuleResource {
    @Inject
    MappingRuleService mappingRuleService;

    @POST
    public Response createMappingRule(MappingRule mappingRule) {
        // Create a new mapping rule
        mappingRuleService.createMappingRule(mappingRule);
        return Response.accepted().build();
    }

    @GET
    public Response getAllMappingRules() {
        // Get all mapping rules
        return Response.ok(mappingRuleService.getAllMappingRules()).build();
    }
}
