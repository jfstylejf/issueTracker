-- Host: 10.135.132.106    Database: issueTracker

ALTER TABLE issueTracker.repo_measure ADD COLUMN absolute_lines INT COMMENT '当前repo下所包含文件的总行数'
