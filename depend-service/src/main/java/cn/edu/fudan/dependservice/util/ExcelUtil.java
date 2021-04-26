package cn.edu.fudan.dependservice.util;

import cn.edu.fudan.dependservice.domain.RelationData;
import cn.edu.fudan.dependservice.domain.RelationView;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.List;

/**
 * @author Beethoven
 */
public class ExcelUtil {

    private ExcelUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static HSSFWorkbook exportExcel(RelationData data) {
        //创建一个excel
        HSSFWorkbook workbook = new HSSFWorkbook();
        //新建一个sheet页
        HSSFSheet sheet = workbook.createSheet("dependency-overview");
        //单元格居中
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //标题字体加粗
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        //设置列宽
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 18000);
        sheet.setColumnWidth(5, 18000);
        sheet.setColumnWidth(6, 5000);
        //列表数据的标头
        HSSFRow titleRow = sheet.createRow(0);
        HSSFCell cell0 = titleRow.createCell(0);
        cell0.setCellValue("编号");
        cell0.setCellStyle(style);
        HSSFCell cell1 = titleRow.createCell(1);
        cell1.setCellValue("项目名");
        cell1.setCellStyle(style);
        HSSFCell cell2 = titleRow.createCell(2);
        cell2.setCellValue("所在库");
        cell2.setCellStyle(style);
        HSSFCell cell3 = titleRow.createCell(3);
        cell3.setCellValue("组名");
        cell3.setCellStyle(style);
        HSSFCell cell4 = titleRow.createCell(4);
        cell4.setCellValue("依赖文件");
        cell4.setCellStyle(style);
        HSSFCell cell5 = titleRow.createCell(5);
        cell5.setCellValue("被依赖文件");
        cell5.setCellStyle(style);
        HSSFCell cell6 = titleRow.createCell(6);
        cell6.setCellValue("依赖关系");
        cell6.setCellStyle(style);
        //处理表格数据
        handleExcelData(workbook, sheet,data.getRows());
        return workbook;
    }
    private static void handleExcelData(HSSFWorkbook workbook, HSSFSheet sheet, List<RelationView> relationViews) {
        //单元格样式
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //填入数据,修改样式
        int i=1;
        for(RelationView r:relationViews){
            HSSFRow row = sheet.createRow(i);
            HSSFCell cell0 = row.createCell(0);
            cell0.setCellValue(i++);
            cell0.setCellStyle(style);
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellValue(r.getProjectName());
            cell1.setCellStyle(style);
            HSSFCell cell2 = row.createCell(2);
            cell2.setCellValue(r.getRepoName());
            cell2.setCellStyle(style);
            HSSFCell cell3 = row.createCell(3);
            cell3.setCellValue(r.getGroupId());
            cell3.setCellStyle(style);
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellValue(r.getSourceFile());
            cell4.setCellStyle(style);
            HSSFCell cell5 = row.createCell(5);
            cell5.setCellValue(r.getTargetFile());
            cell5.setCellStyle(style);
            HSSFCell cell6 = row.createCell(6);
            cell6.setCellValue(r.getRelationType());
            cell6.setCellStyle(style);
        }
    }

}
