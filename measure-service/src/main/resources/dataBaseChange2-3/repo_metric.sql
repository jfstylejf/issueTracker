
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'b26b002b33170f3b6511f2a5314e2d31',
    0,
    0.05,
    0.06,
    0.10,
    0.11,
    0.15,
    0.16,
    0.20,
    0.21,
    1
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'b26b002b33170f3b6511f2a5314e2d31',
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
    'DesignContribution',
    null,
    CURDATE(),
    'b26b002b33170f3b6511f2a5314e2d31',
    2147483647,
    200,
    199.99,
    150,
    149.99,
    100,
    99.99,
    50,
    49.99,
    0
);


COMMIT;