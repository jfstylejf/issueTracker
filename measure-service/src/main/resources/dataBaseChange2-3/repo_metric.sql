
-- Host: 10.135.132.106    Database: issueTracker

START TRANSACTION;

CREATE TABLE repo_metric (
                             id INT(10) NOT NULL AUTO_INCREMENT comment 'id',
                             tag VARCHAR(40) NOT NULL COMMENT '衡量指标类型',
                             repo_uuid VARCHAR(50) COMMENT '衡量库',
                             update_time DATE NOT NULL COMMENT '更新时间',
                             updater VARCHAR(40) NOT NULL COMMENT '更改账号',
                             best_max DOUBLE(16,2) COMMENT '最好标准的上界',
                             best_min DOUBLE(16,2) COMMENT '最好标准的下届',
                             better_max DOUBLE(16,2) COMMENT '较好标准的上界',
                             better_min DOUBLE(16,2) COMMENT '较好标准的下届',
                             normal_max DOUBLE(16,2) COMMENT '中等标准的上界',
                             normal_min DOUBLE(16,2) COMMENT '中等标准的下界',
                             worse_max DOUBLE(16,2) COMMENT '较差标准的上界',
                             worse_min DOUBLE(16,2) COMMENT '较差标准的下界',
                             worst_max DOUBLE(16,2) COMMENT '最差标准的上界',
                             worst_min DOUBLE(16,2) COMMENT '最差标准的下界',
                             PRIMARY KEY (id),
                             INDEX idx_tag_repo_uuid(tag,repo_uuid)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- 插入 repo 为 null情况下的库维度信息，也为基准信息
INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'LivingStaticIssue',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    0,
    0,
    1,
    3,
    4,
    5,
    6,
    10,
    11,
    2147483647
);

INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'CodeStability',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    1.00,
    0.80,
    0.79,
    0.60,
    0.59,
    0.40,
    0.39,
    0.20,
    0.19,
    0.00
);

INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'CommitStandard',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    1.00,
    0.80,
    0.79,
    0.60,
    0.59,
    0.40,
    0.39,
    0.20,
    0.19,
    0.00
);

INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'CyclomaticComplexity',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    -2147483648,
    0,
    1,
    10,
    11,
    30,
    31,
    50,
    51,
    2147483647
);

INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'CloneLine',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    0,
    40,
    41,
    100,
    101,
    150,
    151,
    300,
    301,
    2147483647
);

INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'BigMethodNum',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    0,
    0,
    1,
    2,
    3,
    5,
    6,
    9,
    10,
    2147483647
);
INSERT INTO issueTracker.repo_metric
(
    tag,
    repo_uuid,
    update_time,
    updater,
    best_max,
    best_min,
    better_max,
    better_min,
    normal_max,
    normal_min,
    worse_max,
    worse_min,
    worst_max,
    worst_min
)
VALUES
(
    'WorkLoad',
    null,
    CURDATE(),
    'ec15d79e36e14dd258cfff3d48b73d35',
    2147483647,
    15000,
    14999,
    12000,
    11999,
    8000,
    7999,
    5000,
    4999,
    1000
);



COMMIT;