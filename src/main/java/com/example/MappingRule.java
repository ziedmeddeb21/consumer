package com.example;


public class MappingRule {
    public String field_type;
    public String source_field;
    public String target_field;
    public boolean isArray;
    public boolean isKeyVal;


    public MappingRule(String field_type, String source_field, String target_field, boolean isArray, boolean isKeyVal) {
        this.field_type = field_type;

        this.source_field = source_field;
        this.target_field = target_field;
        this.isArray = isArray;
        this.isKeyVal = isKeyVal;
    }

    public MappingRule(){
        this.field_type = "";
        this.source_field = "";
        this.target_field = "";
    }


}
