package cn.edu.fudan.tagservice.service.impl;

import cn.edu.fudan.tagservice.component.RestInterfaceManager;
import cn.edu.fudan.tagservice.dao.IgnoreRecordDao;
import cn.edu.fudan.tagservice.dao.TagDao;

import cn.edu.fudan.tagservice.domain.*;
import cn.edu.fudan.tagservice.service.TagService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TagServiceImpl implements TagService {

    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);
    @Value("${ignore.tag_id}")
    private String ignoreTagId ;

    @Value("${to_review.tag_id}")
    private String toReviewTagId ;

    private TagDao tagDao;

    private IgnoreRecordDao ignoreRecordDao;

    private RestInterfaceManager restInterfaceManager;

    @Autowired
    public void setTagDao(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    @Autowired
    public void setIgnoreRecordDao(IgnoreRecordDao ignoreRecordDao) {
        this.ignoreRecordDao = ignoreRecordDao;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Transactional
    @Override
    public void addTag(JSONObject requestBody) {
        String name = requestBody.getString("accountName");
        String scope = requestBody.getString("scope");
        String itemId = requestBody.getString("itemId");
        String tag_id;
        // 默认的tag，不是自定义的
        if (requestBody.getBoolean("isDefault")) {
            tag_id = tagDao.getUuidByNameAndScope(name, scope);
            if (tag_id == null) {
                tag_id = UUID.randomUUID().toString();
                tagDao.addOneTag(new Tag(tag_id, name, scope, PriorityEnum.getByValue(name).getColor()));
            }
            logger.error(tagDao.hasBeenTagged(tag_id, itemId).toString());
            if (tagDao.hasBeenTagged(tag_id, itemId) > 0) {
                throw new RuntimeException("duplicate tag!");
            }
            tagDao.addOneTaggedItem(itemId, tag_id);
        } else {
            if (PriorityEnum.contains(name.toUpperCase())) {
                throw new IllegalArgumentException("enter other tag" + name);
            }
            tag_id = UUID.randomUUID().toString();
            tagDao.addOneTag(new Tag(tag_id, name, scope, "#ffffff"));
            tagDao.addOneTaggedItem(itemId, tag_id);
        }
    }

    @Override
    public void addMultiTaggedItem(JSONArray request) {
        List<TaggedItem> list = new ArrayList<>();
        if(request != null && !request.isEmpty()){
            for(int i=0; i < request.size() ; i++){
                JSONObject jsonObject = request.getJSONObject(i);
                TaggedItem  taggedItem = new TaggedItem();
                taggedItem.setItem_id(jsonObject.getString("item_id"));
                taggedItem.setTag_id(jsonObject.getString("tag_id"));
                list.add(taggedItem);
            }

        }
        tagDao.addMultiTaggedItem(list);
    }

    @Override
    public void modifyMultiTaggedItem(JSONArray request) {
        List<ModifyTaggedItem> list = new ArrayList<>();
        if(request != null && !request.isEmpty()){
            for(int i=0; i < request.size() ; i++){
                JSONObject jsonObject = request.getJSONObject(i);
                ModifyTaggedItem  modifyTaggedItem = new ModifyTaggedItem();
                modifyTaggedItem.setItemId(jsonObject.getString("itemId"));
                modifyTaggedItem.setNewTagId(jsonObject.getString("newTagId"));
                modifyTaggedItem.setPreTagId(jsonObject.getString("preTagId"));
                list.add(modifyTaggedItem);
            }

        }
        tagDao.modifyMultiTaggedItem(list);
    }

    @Override
    public void deleteTag(String tagId, String itemId) {
        tagDao.deleteOneTagged(tagId, itemId);
    }

    @Override
    public void deleteTaggeds(List<String> itemIds) {
        tagDao.deleteTaggeds(itemIds);
    }

    @Override
    public void modifyTag(JSONObject requestBody) {
        String name = requestBody.getString("accountName");
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
            if (PriorityEnum.contains(name.toUpperCase())) {
                throw new IllegalArgumentException("enter other tag" + name);
            }
            tagDao.modifyOneTag(oldTagId, name);
        }

    }

    @Override
    public List<Tag> getTagsByItemId(String item_id) {
        return tagDao.getTagsByItemId(item_id);
    }

    @Override
    public List<String> getItemIdsByTagIds(List<String> tagIds) {
        if (tagIds == null || tagIds.size() == 0) {
            return null;
        }
        return tagDao.getItemIdsByTagIds(tagIds);
    }

    @Override
    public List<String> getItemIdsByTagIds(JSONObject requestBody) {
        List<String> result = new ArrayList<>();

        String repoId = requestBody.getString("repo_id");
        List<String> tagIds = requestBody.getJSONArray("tag_ids").toJavaList(String.class);
        int size = tagIds.size();

        List<WeakHashMap<Object,Object>> items = tagDao.getItemIdsAndCountByTagIdsAndRepoId(tagIds,repoId);
        if(size != 0){
            for(Map<Object,Object> map : items){
                String key = (String)map.get("key");
                long value = (Long)map.get("value");
                if(value == size){
                    result.add(key);
                }
            }
        }else{
            for(Map<Object,Object> map : items){
                String key = (String)map.get("key");
                result.add(key);

            }
        }
        return result;
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
    public boolean isSolved(String itemId) {
        List<Tag> tags=tagDao.getTagsByItemId(itemId);
        if(tags==null||tags.isEmpty()) {
            return false;
        }
        return tags.get(0).getName().equals("Solved");
    }

    @Override
    public List<Tag> getTagsByScope(String scope){
        return tagDao.getTagsByCondition(null,null,scope);
    }

    @Override
    public String getTagIdByItemIdAndScope(String itemId, String scope) {
        return tagDao.getTagIdByItemIdAndScope(itemId,scope);
    }

    @Override
    public List<Tag> getTagsByCondition(String uuid,String name, String scope) {

        return tagDao.getTagsByCondition(uuid,name,scope);
    }


    /**
     * Two scenarios ：USER ,PROJECT
     * */
    @Override
    @Transactional
    //待修改 增加缺陷检测工具
    public void ignoreOneType(JSONObject requestBody,String token) {
        String userId = restInterfaceManager.getUserId(token);
        String userName = requestBody.getString("user_name");
        IgnoreLevelEnum ignoreLevel = IgnoreLevelEnum.valueOf(requestBody.getString("ignore_level").toUpperCase());
        String type = requestBody.getString("type");
        String tool = requestBody.getString("tool");
        String repoId = requestBody.getString("repo_id");

        // before ignore tag query the type is ignored or not
        if (isIgnored(userId, ignoreLevel.value(), type, repoId, tool)) {
            throw new RuntimeException("this type has been ignored");
        }

        JSONArray projects = restInterfaceManager.getProjectsByRepoId(repoId);
        String repoName = projects.getJSONObject(0).getString("accountName");
        String branch = projects.getJSONObject(0).getString("branch");
        List<String> ignoreUuidList = new ArrayList<>();


        // modify issue list status
        if (ignoreLevel.level().equals("repository")) {
            ignoreUuidList = restInterfaceManager.getIssueListByCondition(repoId,type,"", tool);
            restInterfaceManager.batchUpdateIssueListStatus(ignoreUuidList, "Ignore");

            Date date = new Date();
            IgnoreRecord record = new IgnoreRecord();
            record.setUuid(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setUserName(userName);
            record.setType(type);
            record.setTool(tool);
            record.setLevel(ignoreLevel.value());
            record.setRepoId(repoId);
            record.setRepoName(repoName);
            record.setBranch(branch);
            record.setUpdateTime(date);

            // insert ignore relation
            ignoreRecordDao.insertOneRecord(record);
        }else if (ignoreLevel.level().equals("project")) {
            ignoreUuidList = restInterfaceManager.getIssueListByCondition("",type,"", tool);
            restInterfaceManager.batchUpdateIssueListStatus(ignoreUuidList, "Ignore");

            Date date = new Date();
            IgnoreRecord record = new IgnoreRecord();
            record.setUuid(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setUserName(userName);
            record.setType(type);
            record.setTool(tool);
            record.setLevel(ignoreLevel.value());
            record.setUpdateTime(date);

            // insert ignore relation
            ignoreRecordDao.insertOneRecord(record);
        }

        //更改tagged表的tagID
        List<ModifyTaggedItem> ignoreList = new ArrayList<>();
        for (String uuid : ignoreUuidList) {
            String status = restInterfaceManager.getIssueStatusByIssueId(uuid);
            String preTagId = getTagIdByItemIdAndScope(uuid, status);
            ignoreList.add(new ModifyTaggedItem(uuid, preTagId, ignoreTagId));
        }
        tagDao.modifyMultiTaggedItem(ignoreList);
        //tagDao.addMultiTaggedItem(ignoreList);
    }

    @Override
    public void cancelOneIgnoreRecord(String repoId, String level, String type, String tool, String token) {
        String userId = restInterfaceManager.getUserId(token);
        IgnoreLevelEnum ignoreLevel = IgnoreLevelEnum.valueOf(level.toUpperCase());
        List<String> ignoreUuidList;
        List<ModifyTaggedItem> ignoreList = new ArrayList<>();
        if (ignoreLevel == IgnoreLevelEnum.PROJECT) {
            ignoreUuidList = restInterfaceManager.getIssueListByCondition("", type, "Ignore", tool);
            restInterfaceManager.batchUpdateIssueListStatus(ignoreUuidList, "To_Review");
            ignoreRecordDao.cancelInvalidRecord(userId, type, tool);

            //更改tagged表的tagID
            for (String uuid : ignoreUuidList) {
                String status = restInterfaceManager.getIssueStatusByIssueId(uuid);
                String preTagId = getTagIdByItemIdAndScope(uuid, status);
                ignoreList.add(new ModifyTaggedItem(uuid, preTagId, toReviewTagId));
            }
            tagDao.modifyMultiTaggedItem(ignoreList);
            return;
        }
        ignoreUuidList = restInterfaceManager.getIssueListByCondition(repoId, type, "Ignore", tool);
        restInterfaceManager.batchUpdateIssueListStatus(ignoreUuidList, "To_Review");
        ignoreRecordDao.cancelOneIgnoreRecord(userId, ignoreLevel.value(), type, repoId, tool);
        //更改tagged表的tagID
        for (String uuid : ignoreUuidList) {
            String status = restInterfaceManager.getIssueStatusByIssueId(uuid);
            String preTagId = getTagIdByItemIdAndScope(uuid, status);
            ignoreList.add(new ModifyTaggedItem(uuid, preTagId, toReviewTagId));
        }
        tagDao.modifyMultiTaggedItem(ignoreList);
    }

    @Override
    public Object getIgnoreRecordList(String token) {
        String userId = restInterfaceManager.getUserId(token);
        return ignoreRecordDao.getIgnoreRecordList(userId);
    }

    @Override
    public List<String> getIgnoreTypeListByRepoId(String repoId) {
        return ignoreRecordDao.getIgnoreTypeListByRepoId(repoId);
    }

    @Override
    public void deleteIgnoreRecordWhenRepoRemove(String repoId, String accountId) {
        ignoreRecordDao.deleteIgnoreRecordWhenRepoRemove(repoId, accountId);
    }





    /**
     *  根据ignore 的level 级别返回对应的结果
     * */
    private boolean isIgnored(String userId, int level, String type, String repoId, String tool) {
        Integer recordLevel = ignoreRecordDao.queryMinIgnoreLevelByUserId(userId, type);
        if (recordLevel == null) {
            return false;
        }

        // repo 1 project 2
        if (recordLevel == IgnoreLevelEnum.PROJECT.value() ) {
            return true;
        }

        /**
         * 程序运行到这里，说明数据库中有该类型issue的level为repository级别的忽略
         * 如果需要设置全局忽略（project），则需要取消掉低级别的忽略
         */
        if (level == IgnoreLevelEnum.PROJECT.value()) {
            //覆盖低级别的忽略规则
            ignoreRecordDao.cancelInvalidRecord(userId, type, tool);
            return false;
        }

        /**
         * 程序运行到这里，说明数据库中有该类型issue的level为repository级别的忽略
         * 如果要再次设置repository级别的忽略，则要查询数据库中是否有同样repo的忽略存在
         */
        IgnoreRecord ignoreRecord = ignoreRecordDao.queryOneRecord(userId, level, type, repoId, tool);
        return ignoreRecord != null;
    }
}
