package com.example;

public class MappingRule {
    public String field_type;
//    public String source_Node;
    public String source_field;
//    public String target_Node;
    public String target_field;

    public MappingRule(String field_type, String source_field, String target_field) {
        this.field_type = field_type;

        this.source_field = source_field;
        this.target_field = target_field;
    }

    public MappingRule(){
        this.field_type = "";
        this.source_field = "";
        this.target_field = "";
    }


}
