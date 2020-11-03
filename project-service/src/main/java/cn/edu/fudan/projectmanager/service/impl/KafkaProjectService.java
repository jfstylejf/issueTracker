package cn.edu.fudan.projectmanager.service.impl;

import cn.edu.fudan.projectmanager.service.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * description:
 *
 * @author fancying
 * create: 2020-07-29 10:44
 **/
@Slf4j
@Service
public class KafkaProjectService implements MessageQueue {


    @Override
    public <T> void update(T message) {

    }

    @Override
    public <T> void repoIn(T message) {

    }

    @Override
    public <T> void updateInfoDown(T consumerRecord) {

    }
}