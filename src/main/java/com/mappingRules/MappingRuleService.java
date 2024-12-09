package com.mappingRules;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class MappingRuleService {

    @Inject
    MappingRuleRepository mappingRuleRepository;

    public final Logger logger = Logger.getLogger(MappingRuleService.class);

    public Document createMappingRule(MappingRule mappingRule, String collectionName) {
        logger.infof("Mapping rule: %s", mappingRule);
        // Create a new mapping rule
         return mappingRuleRepository.addMappingRule(mappingRule, collectionName);

    }

    public List<Document> getAllMappingRules(String collectionName) {
        // Get all mapping rules
        return mappingRuleRepository.getMappingRule(collectionName);
    }

    public Document getMappingRuleById(String collectionName,String id) {
        // Get mapping rule by id
        return mappingRuleRepository.getMappingRuleById(collectionName,id);
    }

    public List<String> getCollectionNames() {
        return mappingRuleRepository.getCollectionNames();
    }

    public Document updateMappingRule(MappingRule mappingRule, String collectionName, String id) {
        logger.infof("Mapping rule: %s", mappingRule);
         return mappingRuleRepository.updateMappingRule(id,mappingRule, collectionName);

    }

    public void deleteMappingRule(String id, String collectionName) {
        mappingRuleRepository.deleteMappingRule( id,collectionName);
    }
}
