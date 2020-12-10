package cn.edu.fudan.tagservice.mapper;

import cn.edu.fudan.tagservice.domain.ModifyTaggedItem;
import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.WeakHashMap;

@Repository
public interface TagMapper {

    void addOneTag(Tag tag);

    void addOneTaggedItem(@Param("item_id") String item_id, @Param("tag_id") String tag_id);

    void addMultiTaggedItem(List<TaggedItem> list);

    void modifyMultiTaggedItem(List<ModifyTaggedItem> list);

    String getUuidByNameAndScope(@Param("accountName") String name, @Param("scope") String scope);

    void deleteOneTag(@Param("tag_id") String tagId);

    void deleteOneTagged(@Param("tag_id") String tagId, @Param("item_id") String itemId);

    void deleteTaggeds(List<String> list);

    void modifyOneTagged(@Param("old_tag_id") String oldTagId, @Param("new_tag_id") String newTagId, @Param("item_id") String itemId);

    void modifyOneTag(@Param("tag_id") String tagId, @Param("accountName") String name);

    Integer hasBeenTagged(@Param("tag_id") String tag_id, @Param("item_id") String item_id);

    List<Tag> getTagsByItemId(@Param("item_id") String item_id);

    List<Tag> getAllDefaultTags();

    List<String> getItemIdsByTagIds(List<String> list);

    void deleteTagByProjectId(@Param("project_id") String projectId);

    /**
     * 根据repoId与tag id集合，查询视图 issue_tag ，获取符合tag id集合中任意tag的 item及其数量
     * @param list
     * @param repoId
     */
    List<WeakHashMap<Object,Object>> getItemIdsAndCountByTagIdsAndRepoId(@Param("list") List<String> list, @Param("repo_id") String repoId);


    List<Tag> getTagsByCondition(@Param("uuid") String uuid,@Param("accountName") String name,@Param("scope") String scope);

    /**
     * 根据item id 与 scope 获取相应的tag id
     * @param itemId
     * @param scope
     * @return
     */
    String getTagIdByItemIdAndScope(@Param("uuid") String itemId,@Param("scope") String scope);
}
