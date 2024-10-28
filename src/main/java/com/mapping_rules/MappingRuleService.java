package com.mapping_rules;

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

    public Document createMappingRule(MappingRule mappingRule) {
        logger.infof("Mapping rule: %s", mappingRule);
        // Create a new mapping rule
        return mappingRuleRepository.addMappingRule(mappingRule);

    }

    public List<Document> getAllMappingRules() {
        // Get all mapping rules
        return mappingRuleRepository.getMappingRule();
    }
}
