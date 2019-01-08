package cn.edu.fudan.tagservice.service;

import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface TagService {

    void addTag(JSONObject requestBody);

    void addMultiTaggedItem(List<TaggedItem> list);

    void modifyMultiTaggedItem(List<TaggedItem> list);

    void deleteTag(String tagId, String itemId);

    void deleteTaggeds(List<String> itemIds);

    void modifyTag(JSONObject requestBody);

    List<Tag> getTagsByItemId(String item_id);

    List<String> getItemIdsByTagIds(List<String> tagIds);

    List<Tag> getAllDefaultTags();

    void deleteTagByProjectId(String projectId);

    boolean isSolved(String itemId);

    /**
     * 以下是ignoreRecord 相关的service
     * */

    void ignoreOneType(JSONObject requestBody, String token);

    void cancelOneIgnoreRecord(JSONObject requestBody, String token);

    Object getIgnoreRecordList(String token);

    List<String> getIgnoreTypeListByRepoId(String repoId);
}
