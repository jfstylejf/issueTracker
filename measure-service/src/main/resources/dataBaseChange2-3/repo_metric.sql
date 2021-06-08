
-- Host: 10.135.132.106    Database: issueTracker

START TRANSACTION;

CREATE TABLE repo_metric (
                             id INT(10) NOT NULL AUTO_INCREMENT comment 'id',
                             tag VARCHAR(40) NOT NULL COMMENT '衡量指标类型',
                             repo_uuid VARCHAR(50) COMMENT '衡量库',
                             update_time DATE NOT NULL COMMENT '更新时间',
                             updater VARCHAR(40) NOT NULL COMMENT '更改账号',
                             best_max INT(10) COMMENT '最好标准的上界',
                             best_min INT(10) COMMENT '最好标准的下届',
                             better_max INT(10) COMMENT '较好标准的上界',
                             better_min INT(10) COMMENT '较好标准的下届',
                             normal_max INT(10) COMMENT '中等标准的上界',
                             noraml_min INT(10) COMMENT '中等标准的下界',
                             worse_max INT(10) COMMENT '较差标准的上界',
                             worse_min INT(10) COMMENT '较差标准的下界',
                             worst_max INT(10) COMMENT '最差标准的上界',
                             worst_min INT(10) COMMENT '最差标准的下界',
                             PRIMARY KEY (id),
                             INDEX idx_tag_repo_uuid(tag,repo_uuid)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

COMMIT;