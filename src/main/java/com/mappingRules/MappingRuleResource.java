package com.mappingRules;

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
        try {
            // Create a new mapping rule
            mappingRuleService.createMappingRule(mappingRule);
            return Response.accepted().build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    public Response getAllMappingRules() {
        try {
            // Get all mapping rules
            return Response.ok(mappingRuleService.getAllMappingRules()).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}
