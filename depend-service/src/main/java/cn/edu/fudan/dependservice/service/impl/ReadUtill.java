package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.domain.Group;
import cn.edu.fudan.dependservice.domain.RelationShip;
import cn.edu.fudan.dependservice.mapper.GroupMapper;
import cn.edu.fudan.dependservice.mapper.RelationshipMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j

@Builder
@Value
@Data
public class ReadUtill {
    private String commitId;
    private String repo_uuid;
//    private GroupMapper groupMapper;
//    private RelationshipMapper relationshipMapper;


    public Map getFileResult(String filePath) throws IOException, InvalidFormatException {
        Map<String, List> res = new HashMap<>();
        String excelPath = filePath;
        //String encoding = "GBK";
        File excel = new File(excelPath);

        String[] split = excel.getName().split("\\.");  //.是特殊字符，需要转义！！！！！
        Workbook wb;
        //根据文件后缀（xls/xlsx）进行判断
        if ("xls".equals(split[1])) {
            FileInputStream fis = new FileInputStream(excel);   //文件流对象
            wb = new HSSFWorkbook(fis);
        } else {
            wb = new XSSFWorkbook(excel);
        }

        //开始解析
        Sheet sheet = wb.getSheetAt(0);     //读取sheet 0
        Sheet sheet2 = wb.getSheetAt(1);     //读取sheet 0
        List group = getGroups(sheet);
        List relation = getRelationShips(sheet2);
        res.put("group", group);
        res.put("relation", relation);
        return res;
    }

    private List getRelationShips(Sheet sheet) {
        List<RelationShip> res = new ArrayList<>();
        int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();

        // there is no group index==-1.0
        for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            //  not one line one record
            RelationShip relationship = new RelationShip();
            relationship.setRepo_uuid(repo_uuid);
            relationship.setCommit_id(commitId);
            relationship.setGroup_id((int) sheet.getRow(rIndex).getCell(1).getNumericCellValue());
            relationship.setFile(sheet.getRow(rIndex).getCell(2).toString());
            relationship.setDepend_on(sheet.getRow(rIndex).getCell(3).toString());
            relationship.setDepend_details(sheet.getRow(rIndex).getCell(4).toString());
            res.add(relationship);
        }
        return res;
    }

    private  List getGroups(Sheet sheet) {
        List<Group> res = new ArrayList<>();
        int firstRowIndex = sheet.getFirstRowNum() + 1;   //第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();
        // there is no group index==-1.0
        Double lastGroupIndex = -1.0;
//        Group oneGroup = new Group();
        for (int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            //  not one line one record
            Double groupIndex = Double.valueOf(sheet.getRow(rIndex).getCell(0).toString());
            if (!groupIndex.equals(lastGroupIndex)) {
                //one group ok and put data to database and new a group
                Group oneGroup = new Group();
                oneGroup.setCommit_id(commitId);
                oneGroup.setCycle_num(0);
                oneGroup.setRepo_uuid(repo_uuid);
                oneGroup.setGroup_id(groupIndex.intValue());
                res.add(oneGroup);
                lastGroupIndex = groupIndex;
            }

        }

        return res;

    }
}

