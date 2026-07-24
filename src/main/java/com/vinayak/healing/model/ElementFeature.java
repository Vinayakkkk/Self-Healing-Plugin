package com.vinayak.healing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ElementFeature {

    // HTML tag
    private String tag;

    // Visible text
    private String text;

    // All HTML attributes
    private Map<String, String> attributes =
            new HashMap<>();

    // Generic context
    private String label;

    private String placeholder;

    private String ariaLabel;
private String id;

private String name;

private String className;

private String dataTest;

private String dataTestId;

private String dataQa;

private String dataCy;
    private String role;

    private String title;

    // DOM hierarchy
    private String parentTag;

    private String parentText;

    private String sectionHeading;

    private int depth;

    // Normalized tokens
    private List<String> tokens =
            new ArrayList<>();


            public String getId(){
                return id;
            }

            public void setId(String id){
                this.id=id;
            }


 public String getName(){
                return name;
            }

            public void setName(String name){
                this.name=name;
            }



             public String getDatatest(){
                return dataTest;
            }

            public void setDatatest(String dataTest){
                this.dataTest=dataTest;
            }

             public String getDatatestid(){
                return dataTestId;
            }

            public void setDatatestid(String dataTestId){
                this.dataTestId=dataTestId;
            }


 public String getDataqa(){
                return dataQa;
            }

            public void setDataqa(String dataQa){
                this.dataQa=dataQa;
            }


            public String getDatacy(){
                return dataCy;
            }

            public void setDatacy(String dataCy){
                this.dataCy=dataCy;
            }


 public String getClassname(){
                return className;
            }

            public void setClassname(String className){
                this.className=className;
            }



    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getAriaLabel() {
        return ariaLabel;
    }

    public void setAriaLabel(String ariaLabel) {
        this.ariaLabel = ariaLabel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParentTag() {
        return parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = parentTag;
    }

    public String getParentText() {
        return parentText;
    }

    public void setParentText(String parentText) {
        this.parentText = parentText;
    }

    public String getSectionHeading() {
        return sectionHeading;
    }

    public void setSectionHeading(String sectionHeading) {
        this.sectionHeading = sectionHeading;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    @Override
    public String toString() {
        return "ElementFeature{" +
                "tag='" + tag + '\'' +
                ", text='" + text + '\'' +
                ", attributes=" + attributes +
                ", label='" + label + '\'' +
                ", placeholder='" + placeholder + '\'' +
                ", ariaLabel='" + ariaLabel + '\'' +
                ", role='" + role + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
", name='" + name + '\'' +
", className='" + className + '\'' +
", dataTest='" + dataTest + '\'' +
", dataTestId='" + dataTestId + '\'' +
", dataQa='" + dataQa + '\'' +
", dataCy='" + dataCy + '\'' +
                ", parentTag='" + parentTag + '\'' +
                ", parentText='" + parentText + '\'' +
                ", sectionHeading='" + sectionHeading + '\'' +
                ", depth=" + depth +
                ", tokens=" + tokens +
                '}';
    }
}