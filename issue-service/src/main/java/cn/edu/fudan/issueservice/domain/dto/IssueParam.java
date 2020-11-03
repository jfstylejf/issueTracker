package cn.edu.fudan.issueservice.domain.dto;

import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
public class IssueParam {

    private String projectId;
    private String category;
    private String duration;
    private int page;
    private int size;
    private boolean onlyNew;
    private boolean onlyEliminated;
    private List<String> types;
    private List<String> tags;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isOnlyNew() {
        return onlyNew;
    }

    public void setOnlyNew(boolean onlyNew) {
        this.onlyNew = onlyNew;
    }

    public boolean isOnlyEliminated() {
        return onlyEliminated;
    }

    public void setOnlyEliminated(boolean onlyEliminated) {
        this.onlyEliminated = onlyEliminated;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
