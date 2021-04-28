package cn.edu.fudan.cloneservice.controller;

import cn.edu.fudan.cloneservice.domain.*;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import cn.edu.fudan.cloneservice.service.CloneMeasureService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author znj yp wgc
 * @date 2020/10/23
 */
@CrossOrigin
@RestController
public class CloneMeasureController {

    @Autowired
    CloneMeasureService cloneMeasureService;
    @Autowired
    RepoCommitMapper repoCommitMapper;

    @GetMapping(value = {"/cloneMeasure/latestCloneLines"})
    public ResponseBean<CloneMeasure> getLatestCloneLines(@RequestParam("repo_uuid") String repoId) {

        try {
            return new ResponseBean<>(200, "success", cloneMeasureService.getLatestCloneMeasure(repoId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }

    /**
     * fixme 暂时只有一个用户没有做鉴权操作
     */

    @GetMapping(value = {"/cloneMeasure"})
    public ResponseBean<Object> getMeasureCloneData(@RequestParam(value = "repo_uuids", defaultValue = "") String repoId,
                                                    @RequestParam(value = "developers", required = false) String developers,
                                                    @RequestParam(value = "since", required = false, defaultValue = "2000-01-01") String start,
                                                    @RequestParam(value = "until", required = false) String end,
                                                    @RequestParam(value = "order", required = false, defaultValue = "") String order,
                                                    @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                    @RequestParam(value = "ps", required = false, defaultValue = "5") String size,
                                                    @RequestParam(value = "asc", required = false) Boolean isAsc) {
        try {
            if (StringUtils.isEmpty(start)) {
                start = "2000-01-01";
            }
            if (StringUtils.isEmpty(end)) {
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                end = simpleDateFormat.format(today);
            }
            List<CloneMessage> result = cloneMeasureService.getCloneMeasure(repoId, developers, start, end, page, size, isAsc, order);
            if (!StringUtils.isEmpty(developers) && !result.isEmpty()) {
                return new ResponseBean<>(200, "success", result);
            } else {
                List<CloneMessage> cloneMessageSorted = cloneMeasureService.sortByOrder(result, order);
                List<CloneMessage> cloneMessageList = new ArrayList<>();
                if (page != null && size != null) {
                    int pageDigit = Integer.parseInt(page);
                    int sizeDigit = Integer.parseInt(size);
                    if (isAsc != null && !isAsc) {
                        Collections.reverse(cloneMessageSorted);
                    }
                    int index = (pageDigit - 1) * sizeDigit;
                    while ((index < cloneMessageSorted.size()) && (index < pageDigit * sizeDigit)) {
                        cloneMessageList.add(cloneMessageSorted.get(index));
                        index += 1;
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("page", page);
                data.put("total", cloneMessageSorted.size() / Integer.parseInt(size) + 1);
                data.put("records", cloneMessageSorted.size());
                data.put("rows", cloneMessageList);
                return new ResponseBean<>(200, "success", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }

    @GetMapping(value = {"/clone/overall-view"})
    public ResponseBean<Object> getCloneOverallViews(@RequestParam(value = "project_ids", defaultValue = "") String projectIds,
                                                     @RequestParam(value = "project_names", required = false, defaultValue = "") String projectNames,
                                                     @RequestParam(value = "repo_uuids", defaultValue = "") String repoUuids,
                                                     @RequestParam(value = "until") String until,
                                                     @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                     @RequestParam(value = "ps", required = false, defaultValue = "5") String size,
                                                     @RequestParam(value = "asc", required = false) Boolean isAsc,
                                                     HttpServletRequest httpServletRequest) {
        try {
            String token = httpServletRequest.getHeader("token");
            List<String> projectIdList = new ArrayList<>();
            if(!StringUtils.isEmpty(projectNames)){
                Arrays.asList(projectNames.split(",")).forEach(a->projectIdList.add(repoCommitMapper.getProjectIdByProjectName(a)));
                if(!projectIdList.isEmpty()) {
                    projectIds = projectIdList.get(0);
                    for (int i = 1; i < projectIdList.size(); i++) {
                        projectIds = projectIds + "," + projectIdList.get(i);
                    }
                }
            }
            if (StringUtils.isEmpty(until)) {
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                until = simpleDateFormat.format(today);
            }
            List<CloneOverallView> cloneOverallViews = cloneMeasureService.getCloneOverallViews(projectIds, repoUuids, until, token);

            List<CloneOverallView> result = new ArrayList<>();
            if (page != null && size != null) {
                int pageDigit = Integer.parseInt(page);
                int sizeDigit = Integer.parseInt(size);
                if (isAsc != null && !isAsc) {
                    Collections.reverse(cloneOverallViews);
                }
                int index = (pageDigit - 1) * sizeDigit;
                while ((index < cloneOverallViews.size()) && (index < pageDigit * sizeDigit)) {
                    result.add(cloneOverallViews.get(index));
                    index += 1;
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("page", page);
            data.put("total", cloneOverallViews.size() / Integer.parseInt(size) + 1);
            data.put("records", cloneOverallViews.size());
            data.put("rows", result);
            if (!cloneOverallViews.isEmpty()) {
                return new ResponseBean<>(200, "success", data);
            } else {
                return new ResponseBean<>(401, "", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }

    @GetMapping(value = {"/clone/details"})
    public ResponseBean<Object> getCloneDetails(@RequestParam(value = "project_ids", required = false, defaultValue = "") String projectIds,
                                                @RequestParam(value = "project_names", required = false, defaultValue = "") String projectNames,
                                                @RequestParam(value = "repo_uuids", required = false, defaultValue = "") String repoUuid,
                                                @RequestParam(value = "group_id", required = false, defaultValue = "") String groupId,
                                                @RequestParam(value = "commit_id", required = false) String commitIds,
                                                @RequestParam(value = "overall", required = false) Boolean isOverall,
                                                @RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                @RequestParam(value = "ps", required = false, defaultValue = "5") String size,
                                                @RequestParam(value = "until", required = false) String until,
                                                @RequestParam(value = "asc", required = false) Boolean isAsc,
                                                HttpServletRequest httpServletRequest) {
        try {

            String token = httpServletRequest.getHeader("token");
            List<String> projectIdList = new ArrayList<>();
            if(!StringUtils.isEmpty(projectNames)){
                Arrays.asList(projectNames.split(",")).forEach(a->projectIdList.add(repoCommitMapper.getProjectIdByProjectName(a)));
                if(!projectIdList.isEmpty()) {
                    projectIds = projectIdList.get(0);
                    for (int i = 1; i < projectIdList.size(); i++) {
                        projectIds = projectIds + "," + projectIdList.get(i);
                    }
                }
            }
            if (StringUtils.isEmpty(until)) {
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                until = simpleDateFormat.format(today);
            }
            if (Boolean.TRUE.equals(isOverall)) {

                List<CloneDetailOverall> cloneDetailOverall = cloneMeasureService.getCloneDetailOverall(projectIds, commitIds, repoUuid,until, token);
                Collections.sort(cloneDetailOverall);
                List<CloneDetailOverall> result = new ArrayList<>();
                if (page != null && size != null) {
                    int pageDigit = Integer.parseInt(page);
                    int sizeDigit = Integer.parseInt(size);
                    if (isAsc != null && !isAsc) {
                        Collections.reverse(cloneDetailOverall);
                    }
                    int index = (pageDigit - 1) * sizeDigit;
                    while ((index < cloneDetailOverall.size()) && (index < pageDigit * sizeDigit)) {
                        result.add(cloneDetailOverall.get(index));
                        index += 1;
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("page", page);
                data.put("total", cloneDetailOverall.size() / Integer.parseInt(size) + 1);
                data.put("records", cloneDetailOverall.size());
                data.put("rows", result);

                if (!cloneDetailOverall.isEmpty()) {
                    return new ResponseBean<>(200, "success", data);
                } else {
                    return new ResponseBean<>(401, "", null);
                }
            } else {
                List<CloneDetail> cloneDetails = cloneMeasureService.getCloneDetails(projectIds, groupId, commitIds, token);
                Collections.sort(cloneDetails);
                List<CloneDetail> result = new ArrayList<>();
                if (page != null && size != null) {
                    int pageDigit = Integer.parseInt(page);
                    int sizeDigit = Integer.parseInt(size);
                    if (isAsc != null && !isAsc) {
                        Collections.reverse(cloneDetails);
                    }
                    int index = (pageDigit - 1) * sizeDigit;
                    while ((index < cloneDetails.size()) && (index < pageDigit * sizeDigit)) {
                        result.add(cloneDetails.get(index));
                        index += 1;
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("page", page);
                data.put("total", cloneDetails.size() / Integer.parseInt(size) + 1);
                data.put("records", cloneDetails.size());
                data.put("rows", result);

                    return new ResponseBean<>(200, "success", data);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }

    @GetMapping(value = {"/clone/trend-graph"})
    public ResponseBean<List<CloneGroupSum>> getTrendGraph(@RequestParam(value = "project_ids", defaultValue = "") String projectIds,
                                                           @RequestParam(value = "since") String start,
                                                           @RequestParam(value = "until") String end,
                                                           @RequestParam(value = "interval", defaultValue = "week") String interval,
                                                           HttpServletRequest httpServletRequest) {
        try {
            String token = httpServletRequest.getHeader("token");
            if (StringUtils.isEmpty(end)) {
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                end = simpleDateFormat.format(today);
            }
            List<CloneGroupSum> cloneGroupsSum = cloneMeasureService.getCloneGroupsSum(projectIds, start, end, interval, token);

                return new ResponseBean<>(200, "success", cloneGroupsSum);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean<>(401, "failed", null);
        }
    }
}

