package cn.edu.fudan.projectmanager.service;

/**
 * description: 通用消息队列接口
 *
 * @author fancying
 * create: 2020-07-27 19:11
 **/
public interface MessageQueue {

    /**
     * description 代码库更新
     *
     * @param message 消息
     */
    <T> void update(T message);

    /**
     * description 代码库下载完成
     *
     * @param message 消息
     */
    <T> void repoIn(T message);

    /**
     *
     */
    <T> void updateInfoDown(T consumerRecord);

}
