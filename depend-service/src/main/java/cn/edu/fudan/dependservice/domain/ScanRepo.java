package cn.edu.fudan.dependservice.domain;

import lombok.Data;

@Data
public class ScanRepo {

    private String repoUuid;
    private String repoPath;
    private String branch;
    private boolean getResult;
    private boolean recopy;
    private boolean rescan;
    private String copyRepoPath;
    private String scanCommit;
    private String resultFile;
    private String resultAbsolutePath;
    private boolean copyStatus;
    private String msg;
    private ScanStatus scanStatus;
    private String toScanDate;
    public void test()
    {

        this.hashCode();
        this.equals(new OutOfMemoryError());
        scanStatus.getStatus();
    }
    // todo write equal and hashcode
    @Override
    public int hashCode(){
        int res=0;
        res=repoUuid.hashCode();
        res+=branch.hashCode();
        res+=scanCommit.hashCode();
        return res;
    }
    @Override
    public boolean equals(Object o){
        if(o==this) return true;
        if(!(o instanceof ScanRepo)) return false;
        if(o==null) return false;
        ScanRepo s=(ScanRepo) o;
        if(s.hashCode()!=this.hashCode()) return false;
        return s.repoUuid.equals(this.repoUuid)
                && s.branch.equals(this.branch)
                && s.scanCommit.equals(this.scanCommit);
    }


}
