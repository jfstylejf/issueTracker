package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.enums.IssueTypeInChineseEnum;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.*;

/**
 * @author Beethoven
 */
public class ExcelUtil {

    public static HSSFWorkbook exportExcel(Map<String, Object> issueFilterList, Map<String, String> allRepoToRepoName) {
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
        sheet.setColumnWidth(3, 9000);
        sheet.setColumnWidth(4, 24000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 6000);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);
        sheet.setColumnWidth(10, 6000);
        sheet.setColumnWidth(11, 11000);
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
        cell4.setCellValue("缺陷所在文件位置");
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
        HSSFCell cell9 = titleRow.createCell(9);
        cell9.setCellValue("解决者");
        cell9.setCellStyle(style);
        HSSFCell cell10 = titleRow.createCell(10);
        cell10.setCellValue("解决时间");
        cell10.setCellStyle(style);
        HSSFCell cell11 = titleRow.createCell(11);
        cell11.setCellValue("解决缺陷commit");
        cell11.setCellStyle(style);
        //处理表格数据
        handleExcelData(workbook, sheet, issueFilterList, allRepoToRepoName);

        return workbook;
    }

    @SuppressWarnings("unchecked")
    private static void handleExcelData(HSSFWorkbook workbook, HSSFSheet sheet, Map<String, Object> issueFilterList, Map<String, String> allRepoToRepoName) {
        //获取数据
        List<Map<String, Object>> issueList = (List<Map<String, Object>>) issueFilterList.get("issueList");
        issueList.sort(Comparator.comparingInt(o -> (int) o.get("displayId")));
        //单元格样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //填入数据,修改样式
        int i = 1;
        for (Map<String, Object> issue : issueList) {
            HSSFRow row = sheet.createRow(i);
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(i);
            cell0.setCellStyle(style);
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(IssueTypeInChineseEnum.getIssueTypeInChinese((String) issue.get("type")));
            cell1.setCellStyle(style);
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue((String) issue.get("issueCategory"));
            cell2.setCellStyle(style);
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(allRepoToRepoName.get((String) issue.get("repoId")));
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
            HSSFCell cell9 = row.createCell(9);
            cell9.setCellValue((String) issue.get("solver"));
            cell9.setCellStyle(style);
            HSSFCell cell10 = row.createCell(10);
            cell10.setCellValue((String) issue.get("solveTime"));
            cell10.setCellStyle(style);
            HSSFCell cell11 = row.createCell(11);
            cell11.setCellValue((String) issue.get("solveCommit"));
            cell11.setCellStyle(style);
            i++;
        }
    }

}
