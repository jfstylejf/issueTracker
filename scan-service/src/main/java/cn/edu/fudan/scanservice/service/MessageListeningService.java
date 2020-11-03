package cn.edu.fudan.scanservice.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * description: 消息监听接口
 *
 * @author fancying
 * create: 2020-03-02 23:43
 **/
public interface MessageListeningService<T> {


    void listing(T mess);
//    /**
//     * 项目第一次扫描
//     */
//    void firstScan(ConsumerRecord<String, String> message);
//
//    /**
//     * 项目更新后扫描
//     */
//    void scanForUpdate(ConsumerRecord<String, String> message);
//
//    /**
//     * 监听scan完成的消息
//     */
//    void listeningScanStatus(ConsumerRecord<String, String> message);
}
