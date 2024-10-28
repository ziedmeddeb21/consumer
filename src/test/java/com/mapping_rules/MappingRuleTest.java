package com.mapping_rules;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest

public class MappingRuleTest  {

    @InjectMocks
    MappingRuleService mappingRuleService;

    @Mock
    MappingRuleRepository mappingRuleRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testGetAllMappingRules() throws Exception {
        List<Document> mappingRules = mappingRuleService.getAllMappingRules();

        assertNotNull(mappingRules, "The list of mapping rules should not be null");
        assertTrue(!mappingRules.isEmpty(), "The list of mapping rules should not be empty");
    }

    @Test
    public void testCreateMappingRule() throws Exception{
        MappingRule testMappingRule = new MappingRule(true, "source_field", "target_field", true, true);
        mappingRuleService.createMappingRule(testMappingRule);

        verify(mappingRuleRepository, times(1)).addMappingRule(testMappingRule);

    }


}
