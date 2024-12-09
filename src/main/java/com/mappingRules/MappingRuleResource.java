package com.mappingRules;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/mapping-rule")
@RolesAllowed("user")
public class MappingRuleResource {
    @Inject
    MappingRuleService mappingRuleService;

    @POST
    @Path("/{collectionName}")
    @Operation(summary = "Create a new mapping rule")
    @RequestBody(
            description = "Mapping rule to create",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example =  """
                            {
                
                                  "source_field": "/commandeALD/numCommandeALD",
                                  "target_field": "/identification/NumCmdLoueur/field_code/field_value",
                                  "isArray": false,
                                  "belongsToArray": false,
                                  "isKeyVal": true,
                                  "script": null,
                                  "defaultValue": null
                                            }
                            }"""
            )
    )
    public Response createMappingRule(MappingRule mappingRule, @PathParam("collectionName") String collectionName) {
        try {

            mappingRuleService.createMappingRule(mappingRule,collectionName);
            return Response.accepted().build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{collectionName}")
    @Operation(summary = "Get all mapping rules of a collection")
    @APIResponse(
            responseCode = "200",
            description = "The mapping rules",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example = """
                            [
                                {
                                    "source_field": "/commandeALD/numCommandeALD",
                                    "target_field": "/identification/NumCmdLoueur/field_code/field_value",
                                    "isArray": false,
                                    "belongsToArray": false,
                                    "isKeyVal": true,
                                    "script": null,
                                    "defaultValue": null
                                }
                            ]"""
            )
    )

    public Response getAllMappingRules(@PathParam("collectionName") String collectionName) {
        try {
            // Get all mapping rules
            return Response.ok(mappingRuleService.getAllMappingRules(collectionName)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/byId/{colName}/{id}")
    @Operation(summary = "Get a mapping rule by id from a collection")
    @APIResponse(
            responseCode = "200",
            description = "The mapping rules",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example = """
                            [
                                {
                                    "source_field": "/commandeALD/numCommandeALD",
                                    "target_field": "/identification/NumCmdLoueur/field_code/field_value",
                                    "isArray": false,
                                    "belongsToArray": false,
                                    "isKeyVal": true,
                                    "script": null,
                                    "defaultValue": null
                                }
                            ]"""
            )
    )
    public Response getMappingRuleById( @PathParam("colName") String collectionName,@PathParam("id") String id) {
        try {
            // Get mapping rule by id
            return Response.ok(mappingRuleService.getMappingRuleById(collectionName,id)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/collection-names")
    @Operation(summary = "Get all collection names")
    @APIResponse(
            responseCode = "200",
            description = "The collection names",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example = """
                            [
                                "collection1",
                                "collection2"
                            ]"""
            )
    )
    public Response getCollectionNames() {
        try {
            return Response.ok(mappingRuleService.getCollectionNames()).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/update/{collectionName}/{id}")
    @Operation(summary = "Update a mapping rule")
    @RequestBody(
            description = "Mapping rule to update",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example =  """
                            {
                
                                  "source_field": "/commandeALD/numCommandeALD",
                                  "target_field": "/identification/NumCmdLoueur/field_code/field_value",
                                  "isArray": false,
                                  "belongsToArray": false,
                                  "isKeyVal": true,
                                  "script": null,
                                  "defaultValue": null
                                            }
                            }"""
            )
    )
    public Response updateMappingRule(MappingRule mappingRule, @PathParam("collectionName") String collectionName, @PathParam("id") String id) {
        try {

            return Response.ok(mappingRuleService.updateMappingRule(mappingRule,collectionName,id)).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/delete/{collectionName}/{id}")
    @Operation(summary = "Delete a mapping rule")
    public Response deleteMappingRule(@PathParam("collectionName") String collectionName, @PathParam("id") String id) {
        try {
            mappingRuleService.deleteMappingRule( id,collectionName);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}
