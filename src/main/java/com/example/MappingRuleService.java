package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.util.List;

@ApplicationScoped
public class MappingRuleService {

    @Inject
    MappingRuleRepository mappingRuleRepository;

    public void createMappingRule(MappingRule mappingRule) {
        // Create a new mapping rule
        mappingRuleRepository.addMappingRule(mappingRule);
        System.out.println("Mapping Rule created: " + mappingRule.field_type +  " " + mappingRule.source_field + " " + mappingRule.target_field);


    }

    public List<Document> getAllMappingRules() {
        // Get all mapping rules
        return mappingRuleRepository.getMappingRule();
    }
}
