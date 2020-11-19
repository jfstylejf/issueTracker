package cn.edu.fudan.tagservice.controller;

import cn.edu.fudan.tagservice.domain.ModifyTaggedItem;
import cn.edu.fudan.tagservice.domain.ResponseBean;
import cn.edu.fudan.tagservice.domain.TaggedItem;
import cn.edu.fudan.tagservice.service.TagService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author WZY
 * @version 1.0
 **/
@RestController
@EnableAutoConfiguration
public class TagController {

    private TagService tagService;

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }


    @GetMapping("/tags/default")
    public Object getAllDefaultTags() {
        return tagService.getAllDefaultTags();
    }

    @GetMapping("/tags/scope")
    public Object getTagsByScope(@RequestParam("scope") String scope) {
        return tagService.getTagsByScope(scope);
    }

    //JSONObject(accountName、scope、itemId、isDefault)
    @PostMapping("/tags")
    public Object addTag(@RequestBody JSONObject requestBody) {
        try {
            tagService.addTag(requestBody);
            return new ResponseBean(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "add failed :" + e.getMessage(), null);
        }
    }

    @DeleteMapping("/tags/{tag-id}")
    public Object deleteTag(@PathVariable("tag-id") String tagId, @RequestParam("item-id") String itemId) {
        try {
            tagService.deleteTag(tagId, itemId);
            return new ResponseBean(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "delete failed :" + e.getMessage(), null);
        }
    }

    //JSONObject(accountName,isDefault,itemId,scope,oldName)
    @PutMapping("/tags")
    public Object modifyTag(@RequestBody JSONObject requestBody) {
        try {
            tagService.modifyTag(requestBody);
            return new ResponseBean(200, "modify success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "modify failed :" + e.getMessage(), null);
        }
    }

    /**
     *  ignore a kind of tag
     *   repo-id ignore-level type
     */
    @PostMapping("/tags/ignore")
    public Object ignoreOneType(@RequestBody JSONObject requestBody, HttpServletRequest request){
        try {
            tagService.ignoreOneType(requestBody, request.getHeader("token"));
            return new ResponseBean(200, "ignore success !", null);
        } catch (Exception e) {
            return new ResponseBean(401, "ignore failed ! " + e.getMessage(), null);
        }
    }

    /**
     *  cancel one ignored tag
     *  type repo-id level (new)
     */
    @DeleteMapping("/tags/ignore")
    public Object cancelIgnoreRecord(@RequestParam("repo-id") String repoId,@RequestParam("level") String level,
                                     @RequestParam("type") String type, @RequestParam("tool") String tool, HttpServletRequest request) {
        try {
            tagService.cancelOneIgnoreRecord(repoId, level, type, tool, request.getHeader("token"));
            return new ResponseBean(200, "modify success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "modify failed :" + e.getMessage(), null);
        }
    }

    @GetMapping("/tags/ignore")
    public Object getIgnoreRecordList(HttpServletRequest request) {
            return tagService.getIgnoreRecordList(request.getHeader("token"));
    }

    @GetMapping("/tags/isSolved")
    public Object isSolved(@RequestParam("issueId")String issueId){
        try{
            return new ResponseBean(200,"success",tagService.isSolved(issueId));
        }catch (Exception e){
            return new ResponseBean(500,e.getMessage(),null);
        }
    }

    @GetMapping("/tags/condition")
    public Object getTagsByCondition(@RequestParam(value = "accountName" ,required = false)String name,
                                     @RequestParam(value ="scope" ,required = false)String scope,
                                     @RequestParam(value = "uuid", required = false)String uuid){
        try{
            return new ResponseBean(200,"success",tagService.getTagsByCondition(uuid,name,scope));
        }catch (Exception e){
            return new ResponseBean(500,e.getMessage(),null);
        }
    }
    /**
     *  以下为内部服务调用
     * */
    @PostMapping("/inner/tags")
    public Object addMultiTaggedItem(@RequestBody JSONArray request) {
        try {

            tagService.addMultiTaggedItem(request);
            return new ResponseBean(200, "add success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "add failed :" + e.getMessage(), null);
        }
    }

    @PostMapping("/inner/tags/tagged-modify")
    public Object modifyMultiTaggedItem(@RequestBody JSONArray request) {
        try {
            tagService.modifyMultiTaggedItem(request);
            return new ResponseBean(200, "update success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "update failed :" + e.getMessage(), null);
        }
    }

    @PostMapping("/inner/tags/item-ids")
    public Object getItemIds(@RequestBody List<String> tagIds) {
        return tagService.getItemIdsByTagIds(tagIds);
    }

    @PostMapping("/inner/tags/required-item-ids")
    public Object getRequiredItemIds(@RequestBody JSONObject requestBody ) {
        return tagService.getItemIdsByTagIds(requestBody);
    }

    @PostMapping("/inner/tags/tagged-delete")
    public Object deleteTagged(@RequestBody List<String> itemIds) {
        try {
            tagService.deleteTaggeds(itemIds);
            return new ResponseBean(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "delete failed :" + e.getMessage(), null);
        }
    }

    @GetMapping("/inner/tags")
    public Object getTags(@RequestParam("item_id") String item_id) {
        return tagService.getTagsByItemId(item_id);
    }

    @GetMapping("/inner/tags/scope")
    public Object getTagIdByItemIdAndScope(@RequestParam("item_id") String itemId,@RequestParam("scope") String scope) {
        return tagService.getTagIdByItemIdAndScope(itemId,scope);
    }


    @DeleteMapping("/inner/tags")
    public Object deleteTagByProjectId(@RequestParam("project-id") String projectId) {
        try {
            tagService.deleteTagByProjectId(projectId);
            return new ResponseBean(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "delete failed :" + e.getMessage(), null);
        }
    }

    @GetMapping("/inner/tags/ignore/types")
    public Object getIgnoreTypeListByRepoId(@RequestParam("repo-id") String repoId) {
        return tagService.getIgnoreTypeListByRepoId(repoId);
    }

    @DeleteMapping("/inner/tags/ignore")
    public Object deleteIgnoreRecordWhenRepoRemove(@RequestParam("repo-id")String repoId,@RequestParam("account-id") String accountId) {
        try {
            tagService.deleteIgnoreRecordWhenRepoRemove(repoId, accountId);
            return new ResponseBean(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean(401, "delete failed :" + e.getMessage(), null);
        }
    }
}