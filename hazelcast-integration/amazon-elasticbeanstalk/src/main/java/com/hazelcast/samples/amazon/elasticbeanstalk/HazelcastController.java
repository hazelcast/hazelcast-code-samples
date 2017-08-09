/*
 *
 *  Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.hazelcast.samples.amazon.elasticbeanstalk;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author László Csontos
 */
@RestController
public class HazelcastController implements InitializingBean {

    private static final ResponseEntity<Entry> EMPTY_RESPONSE = new ResponseEntity<Entry>(Entry.NULL_ENTRY, OK);

    @Autowired
    private HazelcastInstance instance;

    private IMap<String, String> hzMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        hzMap = instance.getMap("hzMap");
    }

    @RequestMapping(method = POST, value = "/entries")
    public ResponseEntity<Entry> putIfAbsent(@RequestBody Entry entry) {
        if (entry == null) {
            return new ResponseEntity<Entry>(BAD_REQUEST);
        }
        String oldValue = hzMap.putIfAbsent(entry.getKey(), entry.getValue());
        if (oldValue == null) {
            return EMPTY_RESPONSE;
        }
        return createResponseEntity(entry.getKey(), oldValue);
    }

    @RequestMapping(method = DELETE, value = "/entries/{key}")
    public ResponseEntity<Entry> remove(@PathVariable String key) {
        String value = hzMap.remove(key);
        if (value == null) {
            return new ResponseEntity<Entry>(NOT_FOUND);
        }
        return createResponseEntity(key, value);
    }

    @RequestMapping(method = GET, value = "/entries/{key}")
    public ResponseEntity<Entry> get(@PathVariable String key) {
        String value = hzMap.get(key);
        if (value == null) {
            return new ResponseEntity<Entry>(NOT_FOUND);
        }
        return createResponseEntity(key, value);
    }

    @RequestMapping(method = GET, value = "/entries")
    public ResponseEntity<Set<Entry>> entrySet() {
        Set<Map.Entry<String, String>> hzMapEntries = hzMap.entrySet();
        Set<Entry> entrySet = new HashSet<Entry>(hzMapEntries.size());
        for (Map.Entry<String, String> entry : hzMapEntries) {
            entrySet.add(new Entry(entry.getKey(), entry.getValue()));
        }
        return new ResponseEntity<Set<Entry>>(entrySet, OK);
    }

    private ResponseEntity<Entry> createResponseEntity(String key, String value) {
        return new ResponseEntity<Entry>(new Entry(key, value), OK);
    }
}
