package cn.edu.fudan.common.jgit;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;

/**
 * description: jgit
 *
 * @author fancying
 * create: 2020-11-13 16:22
 **/
@Slf4j
public class JGitHelper implements AutoCloseable{

    private static final boolean IS_WINDOWS = System.getProperty("os.accountName").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private Repository repository;
    private RevWalk revWalk;
    private Git git;
    @Getter
    private String repoPath;


    /**
     * @param repoPath  代码仓的路径 如：/home/fdse/a
     */
    public JGitHelper(String repoPath) {

        this.repoPath = repoPath + (IS_WINDOWS ? "\\" : "/");
        String gitDir = this.repoPath  + ".git";

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    public boolean checkout(String commit) {
        try {
            if(commit == null){
                commit = repository.getBranch();
            }
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
            return true;
        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
        }
        return false;
    }


    @Override
    public void close() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }
}