package cn.edu.fudan.common.domain.po.scan;

import java.util.Date;

/**
 * description: 每一个commit是否成功扫描信息
 *
 * @author fancying
 * create: 2021-02-24 15:35
 **/
public class ScanDetail {

    String repoUuid;
    String commitId;
    String tool;
    ScanStatus status;
    Date startScanTime;
    /**
     *  也是数据库中该条记录的 updateTime，因此入库的时候该记录不初始化
     **/
    Date endScanTime;
    Date commitTime;

}
