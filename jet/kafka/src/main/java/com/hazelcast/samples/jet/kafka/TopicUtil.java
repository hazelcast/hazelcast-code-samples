/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.samples.jet.kafka;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TopicUtil implements Closeable {
    private static final ILogger LOGGER = Logger.getLogger(TopicUtil.class);

    private final Admin admin;

    public TopicUtil(String broker) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", broker);
        admin = Admin.create(props);
    }

    public void createTopic(String topicId, int partitionCount) {
        List<NewTopic> newTopics = Collections.singletonList(new NewTopic(topicId, partitionCount, (short) 1));
        CreateTopicsResult createTopicsResult = admin.createTopics(newTopics);
        try {
            createTopicsResult.all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTopic(String topicId) {
        try {
            admin.deleteTopics(Collections.singletonList(topicId)).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        admin.close();
    }

    void forceTopicDeletion(String topicId) {
        try {
            deleteTopic(topicId);
        } catch (Exception ex) {
            LOGGER.fine("Exception while deleting topic " + topicId + ": " + ex.getMessage());
        }
    }
}
