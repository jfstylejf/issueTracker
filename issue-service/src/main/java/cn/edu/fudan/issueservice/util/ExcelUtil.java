package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.enums.IssueTypeInChineseEnum;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.*;

/**
 * @author Beethoven
 */
public class ExcelUtil {

    public static HSSFWorkbook exportExcel(Map<String, Object> issueFilterList, JSONObject allRepo) {
        //创建一个excel
        HSSFWorkbook workbook = new HSSFWorkbook();
        //新建一个sheet页
        HSSFSheet sheet = workbook.createSheet("issues detail");
        //单元格居中
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //标题字体加粗
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        //设置列宽
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 8000);
        sheet.setColumnWidth(4, 22000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 6000);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(8, 4000);
        //列表数据的标头
        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell cell0 = titleRow.createCell(0);
        cell0.setCellValue("编号");
        cell0.setCellStyle(style);
        HSSFCell cell1 = titleRow.createCell(1);
        cell1.setCellValue("缺陷名称");
        cell1.setCellStyle(style);
        HSSFCell cell2 = titleRow.createCell(2);
        cell2.setCellValue("缺陷类型");
        cell2.setCellStyle(style);
        HSSFCell cell3 = titleRow.createCell(3);
        cell3.setCellValue("库");
        cell3.setCellStyle(style);
        HSSFCell cell4 = titleRow.createCell(4);
        cell4.setCellValue("位置");
        cell4.setCellStyle(style);
        HSSFCell cell5 = titleRow.createCell(5);
        cell5.setCellValue("引入者");
        cell5.setCellStyle(style);
        HSSFCell cell6 = titleRow.createCell(6);
        cell6.setCellValue("引入时间");
        cell6.setCellStyle(style);
        HSSFCell cell7 = titleRow.createCell(7);
        cell7.setCellValue("状态");
        cell7.setCellStyle(style);
        HSSFCell cell8 = titleRow.createCell(8);
        cell8.setCellValue("优先级");
        cell8.setCellStyle(style);
        //处理表格数据
        handleExcelData(workbook, sheet, issueFilterList, allRepo);

        return workbook;
    }

    @SuppressWarnings("unchecked")
    private static void handleExcelData(HSSFWorkbook workbook, HSSFSheet sheet, Map<String, Object> issueFilterList, JSONObject allRepo) {

        List<Map<String, Object>> issueList = (List<Map<String, Object>>) issueFilterList.get("issueList");
        issueList.sort(Comparator.comparingInt(o -> (int) o.get("displayId")));

        Map<String, String> repoName = new HashMap<>(64);
        for(String projectName : allRepo.keySet()){
            Iterator<Object> iterator = allRepo.getJSONArray(projectName).stream().iterator();
            while (iterator.hasNext()){
                JSONObject next = (JSONObject) iterator.next();
                repoName.put(next.getString("repo_id"), next.getString("name"));
            }
        }

        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //填入数据,修改样式
        int i = 1;
        for (Map<String, Object> issue : issueList) {
            HSSFRow row = sheet.createRow(i);
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue((int) issue.get("displayId"));
            cell0.setCellStyle(style);
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(IssueTypeInChineseEnum.getIssueTypeInChinese((String) issue.get("type")));
            cell1.setCellStyle(style);
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue((String) issue.get("issueCategory"));
            cell2.setCellStyle(style);
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(repoName.get((String) issue.get("repoId")));
            cell3.setCellStyle(style);
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue((String) issue.get("targetFiles"));
            cell4.setCellStyle(style);
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue((String) issue.get("producer"));
            cell5.setCellStyle(style);
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue((String) issue.get("startCommitDate"));
            cell6.setCellStyle(style);
            HSSFCell cell7 = row.createCell(7);
            cell7.setCellValue((String) issue.get("status"));
            cell7.setCellStyle(style);
            HSSFCell cell8 = row.createCell(8);
            cell8.setCellValue((String) issue.get("priority"));
            cell8.setCellStyle(style);
            i++;
        }
    }

}
