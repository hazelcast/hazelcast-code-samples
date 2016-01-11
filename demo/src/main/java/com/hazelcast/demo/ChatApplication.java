/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.demo;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.io.IOException;
import java.io.Serializable;

/**
 * A Chat Application.
 */
public class ChatApplication {

    private String username;
    private final IMap<String, ChatMessage> map = Hazelcast.newHazelcastInstance(null).getMap("chat-application");

    /**
     * Starts a simple chat application
     *
     * @param args your username
     */
    public static void main(String[] args) {
        ChatApplication application = new ChatApplication();
        String username = (args != null && args.length > 0) ? args[0] : null;
        if (username == null) {
            System.out.println("Enter username: ");
            int input;
            StringBuilder u = new StringBuilder();
            try {
                while ((input = System.in.read()) != '\n') {
                    u.append((char) input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            username = u.toString();
        }
        System.out.println("Hello " + username);
        application.setUsername(username);
        application.run();
    }

    private void setUsername(String name) {
        this.username = name;
        new ChatMessage(username, "has joined").send(map);
    }

    private void run() {
        showConnected(map);
        map.addEntryListener(new ChatCallback(), true);
        while (true) {
            int input;
            StringBuilder message = new StringBuilder();
            ChatMessage chat;
            try {
                while ((input = System.in.read()) != '\n') {
                    message.append((char) input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            chat = new ChatMessage(username, message.toString());
            chat.send(map);
        }
    }

    private void showConnected(IMap<String, ChatMessage> map) {
        for (String user : map.keySet()) {
            System.out.println(user + " is online");
        }
    }

    /**
     * A Chat Message class
     */
    private static class ChatMessage implements Serializable {

        private String username;
        private String message;

        ChatMessage(String username, String message) {
            this.username = username;
            this.message = message;
        }

        void send(IMap<String, ChatMessage> map) {
            map.put(username, this);
        }

        @Override
        public String toString() {
            return username + ": " + message;
        }
    }

    /**
     * Notifies entry changes to Chat
     */
    private class ChatCallback implements EntryAddedListener<String, ChatMessage>, EntryRemovedListener<String, ChatMessage>,
            EntryUpdatedListener<String, ChatMessage> {

        ChatCallback() {
        }

        public void entryAdded(EntryEvent<String, ChatMessage> event) {
            if (!username.equals(event.getKey())) {
                System.out.println(event.getValue());
            }
        }

        public void entryRemoved(EntryEvent<String, ChatMessage> event) {
            if (!username.equals(event.getKey())) {
                System.out.println(event.getKey() + " left");
            }
        }

        public void entryUpdated(EntryEvent<String, ChatMessage> event) {
            if (!username.equals(event.getKey())) {
                System.out.println(event.getValue().toString());
            }
        }
    }
}
