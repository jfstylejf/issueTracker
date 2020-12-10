package cn.edu.fudan.issueservice.domain.dbo;

/**
 * @author Beethoven
 */
public class IssueType {


    /**
     * severity sourceId(eg： sonarqube  java:S2204)
     */

    private String uuid;
    private String type;
    private String specificationSource;
    private String category;
    private String description;
    private String language;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSpecificationSource() {
        return specificationSource;
    }

    public void setSpecificationSource(String specificationSource) {
        this.specificationSource = specificationSource;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
