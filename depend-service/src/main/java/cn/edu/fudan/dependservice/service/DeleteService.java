package cn.edu.fudan.dependservice.service;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-11 10:51
 **/
public interface DeleteService {
    void deleteOneRepo(String repoUuid, String token) throws InterruptedException;


}
