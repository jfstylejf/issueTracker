package cn.edu.fudan.tagservice.service.impl;

import cn.edu.fudan.tagservice.component.RestInterfaceManager;
import cn.edu.fudan.tagservice.dao.TagDao;

import cn.edu.fudan.tagservice.domain.IgnoreLevelEnum;
import cn.edu.fudan.tagservice.domain.PriorityEnum;
import cn.edu.fudan.tagservice.domain.Tag;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import cn.edu.fudan.tagservice.service.TagService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TagServiceImpl implements TagService {

    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    private TagDao tagDao;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setTagDao(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Transactional
    @Override
    public void addTag(JSONObject requestBody) {
        String name = requestBody.getString("name");
        String scope = requestBody.getString("scope");
        String itemId = requestBody.getString("itemId");
        String tag_id;
        if (requestBody.getBoolean("isDefault")) {   // 默认的tag，不是自定义的
            tag_id = tagDao.getUuidByNameAndScope(name, scope);
            if (tag_id == null) {
                tag_id = UUID.randomUUID().toString();
                tagDao.addOneTag(new Tag(tag_id, name, scope, PriorityEnum.getByValue(name).getColor()));
            }
            logger.error(tagDao.hasBeenTagged(tag_id, itemId).toString());
            if (tagDao.hasBeenTagged(tag_id, itemId) > 0)
                throw new RuntimeException("duplicate tag!");
            tagDao.addOneTaggedItem(itemId, tag_id);
        } else {
            if (PriorityEnum.contains(name.toUpperCase()))
                throw new IllegalArgumentException("enter other tag" + name);
            tag_id = UUID.randomUUID().toString();
            tagDao.addOneTag(new Tag(tag_id, name, scope, "#ffffff"));
            tagDao.addOneTaggedItem(itemId, tag_id);
        }
    }

    @Override
    public void addMultiTaggedItem(List<TaggedItem> list) {
        tagDao.addMultiTaggedItem(list);
    }

    @Override
    public void modifyMultiTaggedItem(List<TaggedItem> list) {
        tagDao.modifyMultiTaggedItem(list);
    }

    @Override
    public void deleteTag(String tagId, String itemId) {
        //tagDao.deleteOneTag(tagId);
        tagDao.deleteOneTagged(tagId, itemId);
    }

    @Override
    public void deleteTaggeds(List<String> itemIds) {
        tagDao.deleteTaggeds(itemIds);
    }

    @Override
    public void modifyTag(JSONObject requestBody) {
        String name = requestBody.getString("name");
        String scope = requestBody.getString("scope");
        String itemId = requestBody.getString("itemId");
        String oldName = requestBody.getString("oldName");
        String oldTagId = tagDao.getUuidByNameAndScope(oldName, scope);
        String newTagId = tagDao.getUuidByNameAndScope(name, scope);
        if (requestBody.getBoolean("isDefault")) {
            if(name.equals("Not Tagged")){
                //取消某个issue的tag
               tagDao.deleteOneTagged(oldTagId,itemId);
            }else{
                tagDao.modifyOneTagged(oldTagId, newTagId, itemId);
            }
        } else {
            if (PriorityEnum.contains(name.toUpperCase()))
                throw new IllegalArgumentException("enter other tag" + name);
            tagDao.modifyOneTag(oldTagId, name);
        }

    }

    @Override
    public List<Tag> getTagsByItemId(String item_id) {
        return tagDao.getTagsByItemId(item_id);
    }

    @Override
    public List<String> getItemIdsByTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.size() == 0)
            return null;
        return tagDao.getItemIdsByTagIds(tagIds);
    }

    @Override
    public List<Tag> getAllDefaultTags() {
        List<Tag> tags= tagDao.getAllDefaultTags();
        tags.sort(Comparator.comparingInt((Tag tag)->PriorityEnum.getByValue(tag.getName()).getLevel()));
        return tags;
    }

    @Override
    public void deleteTagByProjectId(String projectId) {
        tagDao.deleteTagByProjectId(projectId);
    }

    @Override
    public void ignoreOneTag(JSONObject requestBody) throws Exception{
        String userId = restInterfaceManager.getUserId(requestBody.getString("token"));
        IgnoreLevelEnum ignoreLevel = IgnoreLevelEnum.valueOf(requestBody.getString("ignore-level").toUpperCase());
        // before ignore tag query the type is ignored or not
        if (ignored(ignoreLevel.value())) {
            throw new Exception("ignored");
        }

        // insert ignore relation table

        //return;
    }

    @Override
    public void cancelIgnoreRecord(JSONObject requestBody) {

    }

    /**
     *  根据ignore 的level 级别返回对应的结果
     * */
    private boolean ignored(int level) {
        return true;
    }
}
