package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.LocationMatchResult;
import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.util.CosineUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fancying
 */
@Slf4j
@Component("TMatchStrategy")
@SuppressWarnings("Duplicated")
public class TokenMatchStrategy implements MatchStrategy {

    private final double SIMILARITY_LOWER_LIMIT = 0.85;

    @Override
    public boolean match(RawIssue rawIssue1, RawIssue rawIssue2) {
        boolean result = false;
        List<Location> locations1 = rawIssue1.getLocations ();
        List<Location> locations2 = rawIssue2.getLocations ();


        // 第一种情况，两个location的code都为空
        if(locations1.size () == 1 && locations2.size () == 1){
            String locationCode1 = locations1.get (0).getCode ();
            String locationCode2 = locations2.get (0).getCode ();
            boolean whetherTwoLocationIsEmpty = StringUtils.isEmpty(locationCode1) && StringUtils.isEmpty(locationCode2);

            if(whetherTwoLocationIsEmpty){
                //因为在外面已经做了分类，此时两个location所属文件相同，且缺陷类型相同，故此时直接判定两个raw issue 最佳匹配
                RawIssueMatchResult rawIssueMatchResult1 = RawIssueMatchResult.builder ()
                        .isBestMatch (true)
                        .matchingDegree (1.7)
                        .matchedRawIssueId (rawIssue2.getUuid ())
                        .matchedIssueId (rawIssue2.getIssue_id ())
                        .build ();

                RawIssueMatchResult rawIssueMatchResult2 = RawIssueMatchResult.builder ()
                        .isBestMatch (true)
                        .matchingDegree (1.7)
                        .matchedRawIssueId (rawIssue1.getUuid ())
                        .matchedIssueId (rawIssue1.getIssue_id ())
                        .build ();

                List<RawIssueMatchResult> rawIssueMatchResultDTOs1 = rawIssue1.getRawIssueMatchResults();
                rawIssueMatchResultDTOs1.add (rawIssueMatchResult1);

                List<RawIssueMatchResult> rawIssueMatchResultDTOs2 = rawIssue2.getRawIssueMatchResults();
                rawIssueMatchResultDTOs2.add (rawIssueMatchResult2);

                return true;
            }

        }




        //初始化每个location的信息
        for(Location location1 :locations1){
            location1.setLocationMatchResults(null);
            location1.setMatched (false);
            location1.setMatchedIndex (-1);
        }

        for(Location location2 :locations2){
            location2.setLocationMatchResults(null);
            location2.setMatched (false);
            location2.setMatchedIndex (-1);
        }

        for(Location location1 : locations1){
            for(Location location2 : locations2){
                //对两个location进行匹配
                matchTwoLocation(location1, location2);
            }
        }

        for(Location location1 : locations1){
            if(location1.getLocationMatchResults() != null){
                List<LocationMatchResult> sortedMatchResult = location1.getLocationMatchResults().stream ()
                        .sorted (Comparator.comparing (LocationMatchResult:: getMatchingDegree).reversed ())
                        .collect (Collectors.toList ());
                location1.setLocationMatchResults(sortedMatchResult);
            }
        }

        for(Location location2 : locations2){
            if(location2.getLocationMatchResults() != null){
                List<LocationMatchResult> sortedMatchResult = location2.getLocationMatchResults().stream ()
                        .sorted (Comparator.comparing (LocationMatchResult:: getMatchingDegree).reversed ())
                        .collect (Collectors.toList ());
                location2.setLocationMatchResults(sortedMatchResult);
            }
        }


        Map<String, Location> locationsFirstMap = new HashMap<> ();
        Map<String, Location> locationsSecondMap = new HashMap<> ();


        for(Location location1 : locations1){
            locationsFirstMap.put(location1.getUuid (),location1);
            location1.setMatchedIndex (-1);
        }

        for(Location location2 : locations2){
            locationsSecondMap.put(location2.getUuid (),location2);
            location2.setMatchedIndex (-1);
        }


        for(Location location1 : locations1){
            if(location1.isMatched ()){
                findBestLocationMatching(locationsFirstMap,locationsSecondMap,location1);
            }

        }

        int bestMatchCounts = 0 ;
        double totalScore = 0 ;
        int matchCount = 0;

        for(Location location1 : locations1){
            if(location1.isMatched () == false){
                continue;
            }
            LocationMatchResult locationMatchResult = location1.getLocationMatchResults().get (location1.getMatchedIndex ());

            if(locationMatchResult.getBestMatch() != null && locationMatchResult.getBestMatch()){
                bestMatchCounts ++;
            }

            totalScore += locationMatchResult.getMatchingDegree ();
            matchCount++;
        }

        // 开始判定两个raw issue之间的关系
        boolean isBestMatchRawIssue = false;
        if(totalScore > 0){
            double avgScore = totalScore/matchCount;

            if(bestMatchCounts == locations1.size () && bestMatchCounts == locations2.size ()){
                isBestMatchRawIssue = true;
                result = true;
                //同时将匹配分加1，区别普通的平均值匹配
                avgScore += 1;
            }

            RawIssueMatchResult rawIssueMatchResult1 = RawIssueMatchResult.builder ()
                    .isBestMatch (isBestMatchRawIssue)
                    .matchingDegree (avgScore)
                    .matchedRawIssueId (rawIssue2.getUuid ())
                    .matchedIssueId (rawIssue2.getIssue_id ())
                    .build ();

            RawIssueMatchResult rawIssueMatchResult2 = RawIssueMatchResult.builder ()
                    .isBestMatch (isBestMatchRawIssue)
                    .matchingDegree (avgScore)
                    .matchedRawIssueId (rawIssue1.getUuid ())
                    .matchedIssueId (rawIssue1.getIssue_id ())
                    .build ();


            List<RawIssueMatchResult> rawIssueMatchResultDTOs1 = rawIssue1.getRawIssueMatchResults();
            rawIssueMatchResultDTOs1.add (rawIssueMatchResult1);

            List<RawIssueMatchResult> rawIssueMatchResultDTOs2 = rawIssue2.getRawIssueMatchResults();
            rawIssueMatchResultDTOs2.add (rawIssueMatchResult2);

        }

        return result;
    }

    private void findBestLocationMatching(Map<String, Location> locationsFirstMap,
                                  Map<String, Location> locationsSecondMap,
                                  Location location){

        List<LocationMatchResult> locationMatchResult = location.getLocationMatchResults();
        if(locationMatchResult == null){
            return;
        }
        int size = locationMatchResult.size ();
        for(int i = 0 ; i < size ; i++ ){
            String matchedLocationId = locationMatchResult.get (i).getMatchedLocationId ();

            Location secondLocation = locationsSecondMap.get (matchedLocationId);
            int index = secondLocation.getMatchedIndex ();
            int matchIndex = getIndexOfLocationMatchResultDtoByLocationId(secondLocation,location.getUuid ());
            //如果pre raw issue 列表没有匹配过，则为-1，则将当前raw issue 与之匹配
            if(index == -1){
                secondLocation.setMatchedIndex (matchIndex);
                secondLocation.setMatched (true);
                location.setMatched (true);
                location.setMatchedIndex (i);
                return;
            }
            ///如果pre raw issue 列表已经匹配过，但是此时的raw issue 匹配度更高，则进行替换
            if(matchIndex < index){
                secondLocation.setMatchedIndex (matchIndex);
                String lowMatchDegreeLocationId = secondLocation.getLocationMatchResults().get (index).getMatchedLocationId ();
                Location lowMatchDegreeLocation = locationsFirstMap.get (lowMatchDegreeLocationId);
                findBestLocationMatching(locationsFirstMap,locationsSecondMap,lowMatchDegreeLocation);
                location.setMatched (true);
                location.setMatchedIndex (i);
                return;
            }
        }

        location.setMatchedIndex (-1);
        location.setMatched (false);

    }



    private void matchTwoLocation(Location location1, Location location2){

        double resultScore = 0;
        double tokenSimilarityDegree = 0;
        double isInSameMethod = 0;
        double isSameBugLines = 0;

        //todo token化Location的bug lines的code，然后对比两个location token化后的相似度，这是A级评分

        if(location1.getCode () == null || location1.getCode ().isEmpty ()){
            if(location2.getCode () == null || location2.getCode ().isEmpty ()){
                tokenSimilarityDegree = 1;
            }else{
                tokenSimilarityDegree = 0;
            }
        }else{
            if(location2.getCode () == null || location2.getCode ().isEmpty ()){
                tokenSimilarityDegree = 0;
            }else{
                //如果两个location的code都不为空，则计算余弦相似度
                tokenSimilarityDegree = tokenizeAndSimilarity(location1.getCode (), location2.getCode ());
            }
        }
        if(((Double)Double.NaN).equals(tokenSimilarityDegree)){
            if(location1.getCode ().equals (location2.getCode ())){
                tokenSimilarityDegree = 1;
            }
        }

        if(tokenSimilarityDegree < SIMILARITY_LOWER_LIMIT){
            return ;
        }

        LocationMatchResult locationMatchResult1 = new LocationMatchResult();
        LocationMatchResult locationMatchResult2 = new LocationMatchResult();


        //两者都是类的字段，或者在同一个方法,B级评分
        String method1 = location1.getMethod_name ();
        String method2 = location2.getMethod_name ();
        if(method1 != null && method1.equals (method2)){
            //todo 特殊情况：方法名相同，code也相同的情况下，其实还需要根据code的所在行号与methodName的所在行号的偏移量offset，再进行一次评分
            isInSameMethod = 1;
        }else if(method1 == null && method2 == null ){
            isInSameMethod = 1;
        }

        //两者的bug lines是否相同
        String bugLines1 = location1.getBug_lines ();
        String bugLines2 = location2.getBug_lines ();
        if(bugLines1 == null && bugLines2 == null){
            isSameBugLines = 1;
        }
        if(bugLines1 != null && bugLines1.equals (bugLines2)){
            isSameBugLines = 1;
        }

        if(tokenSimilarityDegree > 0.995 && tokenSimilarityDegree < 1.05
            && isInSameMethod == 1
            && isSameBugLines == 1){
            locationMatchResult1.setBestMatch(true);
            locationMatchResult2.setBestMatch(true);
        }


        //todo 评分机制有待确认
        resultScore = tokenSimilarityDegree + isInSameMethod * 0.5 + isSameBugLines * 0.2 ;


        locationMatchResult1.setMatchingDegree (resultScore);
        locationMatchResult2.setMatchingDegree (resultScore);

        locationMatchResult1.setMatchedLocationId (location2.getUuid ());
        locationMatchResult2.setMatchedLocationId (location1.getUuid ());

        if(location1.getLocationMatchResults() == null ){
            List<LocationMatchResult> locationMatchResultDTOS1 = new ArrayList<> ();
            location1.setLocationMatchResults(locationMatchResultDTOS1);
        }

        if(location2.getLocationMatchResults() == null ){
            List<LocationMatchResult> locationMatchResultDTOS2 = new ArrayList<> ();
            location2.setLocationMatchResults(locationMatchResultDTOS2);
        }

        location1.getLocationMatchResults().add (locationMatchResult1);
        location2.getLocationMatchResults().add (locationMatchResult2);
        location1.setMatched (true);
        location2.setMatched (true);

    }


    private Integer getIndexOfLocationMatchResultDtoByLocationId(Location location , String locationId){
        Integer result = -1;
        if(location == null){
            return result;
        }
        List<LocationMatchResult> locationMatchResults = location.getLocationMatchResults();
        int locationMatchResultDtoSize = locationMatchResults.size ();
        for(int i = 0; i < locationMatchResultDtoSize; i++){
            if(locationId.equals (locationMatchResults.get (i).getMatchedLocationId ())){
                result = i;
                break;
            }
        }

        return result;
    }

    private double tokenizeAndSimilarity(String code1, String code2){
        return CosineUtil.isSimilarCode(code1, code2, true);
    }
}
