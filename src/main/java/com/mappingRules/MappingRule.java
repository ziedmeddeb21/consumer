package com.mappingRules;


public class MappingRule {

    private String id;
    public String source_field;
    public String target_field;
    public boolean isArray;
    public boolean belongsToArray;
    public boolean isKeyVal;
    public String script;
    public Object defaultValue;



    public MappingRule() {
    }

    public MappingRule(boolean belongsToArray, String source_field, String target_field, boolean isArray, boolean isKeyVal, String script, Object defaultValue) {
        this.source_field = source_field;
        this.target_field = target_field;
        this.belongsToArray = belongsToArray;
        this.isArray = isArray;
        this.isKeyVal = isKeyVal;
        this.script = script;
        this.defaultValue = defaultValue;
    }




}
