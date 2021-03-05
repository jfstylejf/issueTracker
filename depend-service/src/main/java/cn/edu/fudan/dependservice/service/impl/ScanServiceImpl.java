package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.common.component.RepoRestManager;
import cn.edu.fudan.common.domain.po.scan.RepoScan;
import cn.edu.fudan.common.scan.CommonScanProcess;
import cn.edu.fudan.common.scan.ToolScan;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * description: 依赖分析流程
 *
 * @author fancying
 * create: 2021-03-02 21:04
 **/
public class ScanServiceImpl extends CommonScanProcess {

    @Override
    protected ToolScan getToolScan(String tool) {
        return new ToolScanImpl();
//        return null;
    }

    @Override
    protected List<String> getScannedCommitList(String repoUuid, String tool) {
        //need find in data base.


        return null;
    }

    @Override
    protected String getLastedScannedCommit(String repoUuid, String tool) {
        return null;
    }

    @Override
    protected String[] getToolsByRepo(String repoUuid) {
        return new String[]{"dependency"};
//        return new String[0];
    }

    @Override
    protected void insertRepoScan(RepoScan repoScan) {

    }

    @Override
    public <T extends RepoRestManager> void setRepoRestManager(T restInterfaceManager) {

    }

    @Override
    public void updateRepoScan(RepoScan scanInfo) {
        //update if the scan success


    }

    @Override
    public void deleteRepo(String repoUuid) {

    }

    @Override
    public void deleteRepo(String repoUuid, String toolName) {

    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid, String toolName) {
        //get status by repo and toolname
        return null;
    }

    @Override
    public RepoScan getRepoScanStatus(String repoUuid) {
        return null;
    }
//    public  List getExcel() throws FileNotFoundException, IOException {
//        File excelFile = new File("D:\\allIdea\\IssueTracker-test\\depend-service\\src\\main\\resources\\c.xlsx");
//        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excelFile));
//        XSSFSheet sheet = wb.getSheetAt(0);
//        List<List<String>> listAll = new ArrayList<List<String>>();
//        for (Row row : sheet) {
//            List<String> list = new ArrayList<String>();
//            for (Cell cell : row) {
//                switch (cell.getCellType()) {
//                    case Cell.CELL_TYPE_STRING://字符串
//                        String  categoryName = cell.getRichStringCellValue().getString();
//                        list.add(categoryName);
//                        System.out.print(" ");
//                        break;
//                    case Cell.CELL_TYPE_NUMERIC://数值与日期
//                        String axis = Double.toString(cell.getNumericCellValue());
//                        list.add(axis);
//                        System.out.print(" ");
//                        break;
//                    default:
//                }
//            }
//            listAll.add(list);
//            System.out.println();
//        }
//        System.out.println(listAll);
//        System.out.println(listAll.size());
//        return listAll;
//    }

    public static void main(String[] args) {
        ScanServiceImpl s=new ScanServiceImpl();
        System.out.println("hhhh");
        try {
            s.getExcel();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
