package cn.edu.fudan.dependservice.constants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * description: 公共、常用 常量
 *
 * @author fancying
 * create: 2020-06-04 15:23
 **/
public interface PublicConstants {

    boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    String RENAME = "RENAME";
    String ADD = "ADD";
    String DELETE = "DELETE";
    String CHANGE = "CHANGE";
    String MODIFY = "MODIFY";
    String SELF_CHANGE = "SELF_CHANGE";
    String MOVE = "MOVE";
    String CURRENT = "CURRENT";
    String DEVELOPER = "DEVELOPER";
    String REPO = "REPO";
    String DEVELOPER_LOW = "developer";
    String REPO_LOW = "repo";

    String CURRENT_LOW = "current";
    String ADD_LOW = "add";

    String LIVE_LOW = "live";
    String LOSS_LOW = "loss";
    String CHANGE_LOW = "change";
    String DELETE_LOW = "delete";

    String DELETE_SELF = "DELETE_SELF";
    String DELETE_OTHERS = "DELETE_OTHERS";

    String DETAIL_DELETE = "DETAIL_DELETE";

    String CLASS = "class";
    String METHOD = "method";
    String FIELD = "field";
    String STATEMENT = "statement";
    String FILE = "file";

    String MAX = "max";
    String MIN = "min";
    String MEDIAN = "median";
    String AVERAGE = "average";
    String MED = "med";
    String AVG = "avg";

    String UPPER_QUARTILE = "upper_quartile";
    String LOWER_QUARTILE = "lower_quartile";

    String DELIMITER_RENAME = ":";

    String RESPONSE_REPO = "repo";
    String RESPONSE_DEVELOPER = "developer";

    String ROWS = "rows";
    String PAGE = "page";
    String RECORDS = "records";

    String TIME_FORMAT = "yyyy-MM-dd";
    String TRUE = "true";
    String FALSE = "false";

    /**
     * string 转 Date
     *
     * @param date 2020-01-01 00:00:00
     * @return Date
     */
    default Date getDateByString(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, FORMATTER);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
