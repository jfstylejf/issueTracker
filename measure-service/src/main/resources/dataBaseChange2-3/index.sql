-- Host: 10.135.132.106    Database: issueTracker

ALTER TABLE issueTracker.commit DROP INDEX idx_commit_commit_id;

ALTER TABLE issueTracker.commit ADD INDEX idx_commit_id(commit_id);
ALTER TABLE issueTracker.commit ADD INDEX idx_repo_id(repo_id);
ALTER TABLE issueTracker.commit ADD INDEX idx_developer(developer);

ALTER TABLE issueTracker.account_author ADD INDEX idx_account_gitname(account_gitname);
ALTER table issueTracker.account_author ADD index idx_account_name(account_name);

ALTER TABLE issueTracker.repo_measure ADD INDEX idx_developer_name(developer_name);

-- 新增is_compliance,判断是否是包含 jira 的提交
ALTER TABLE issueTracker.repo_measure ADD COLUMN is_compliance INT not NULL AFTER commit_message;
ALTER TABLE issueTracker.repo_measure ADD INDEX idx_is_compliance(is_compliance);
