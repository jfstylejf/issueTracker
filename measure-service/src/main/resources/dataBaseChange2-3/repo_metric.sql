
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
                             normal_min INT(10) COMMENT '中等标准的下界',
                             worse_max INT(10) COMMENT '较差标准的上界',
                             worse_min INT(10) COMMENT '较差标准的下界',
                             worst_max INT(10) COMMENT '最差标准的上界',
                             worst_min INT(10) COMMENT '最差标准的下界',
                             PRIMARY KEY (id),
                             INDEX idx_tag_repo_uuid(tag,repo_uuid)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


START TRANSACTION;

-- 创建存储过程前先检查是否存在，存在就删除，这个可以忽略
DROP PROCEDURE IF EXISTS insertOperation;

-- 存储过程
CREATE PROCEDURE insertOperation()
BEGIN

    -- 该变量用于标识是否还有数据需遍历
    DECLARE flag INT DEFAULT FALSE;
    -- 创建一个变量用来存储遍历过程中的值
    DECLARE repo VARCHAR(40);
    -- 查询出需要遍历的数据集合
    DECLARE repoList CURSOR FOR (SELECT DISTINCT(repo_uuid) FROM issueTracker.sub_repository WHERE repo_uuid is not null);
    -- 游标中的内容执行完后将done设置为true
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET flag = TRUE;

    -- 打开游标
    OPEN repoList;

    -- 执行循环
    read_loop : LOOP

        -- 取游标中的值
        FETCH repoList INTO repo;
        -- 判断是否结束循环，一定要放到FETCH之后，因为在fetch不到的时候才会设置done为true
        -- 如果放到fetch之前，先判断done，这个时候done的值还是之前的循环的值，因此就会导致循环一次
        IF flag THEN
            LEAVE read_loop;
        END IF;

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
            repo,
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
            repo,
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
            -2147483648
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
            repo,
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
            repo,
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
            repo,
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
            repo,
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
            repo,
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

    END LOOP read_loop;
    CLOSE repoList;
END;

CALL insertOperation();

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
    -2147483648
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