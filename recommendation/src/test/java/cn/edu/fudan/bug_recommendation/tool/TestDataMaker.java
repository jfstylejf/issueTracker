package cn.edu.fudan.bug_recommendation.tool;

import cn.edu.fudan.bug_recommendation.domain.Recommendation;

public class TestDataMaker {

    public static Recommendation recommendationMaker(){
        Recommendation recommendation = new Recommendation();
        recommendation.setUuid("test1");
        recommendation.setType("test");
        recommendation.setLocation("test");
        recommendation.setDescription("test");
        recommendation.setBug_lines("1,2");
        recommendation.setStart_line("1");
        recommendation.setEnd_line("5");
        recommendation.setCurr_code("public static void main()");
        recommendation.setCurr_commitid("afsfWRWEF1321");
        recommendation.setPrev_code("private static int main()");
        recommendation.setNext_commitid("SFA3SFGA6FBX21");
        return recommendation;
    }
}

