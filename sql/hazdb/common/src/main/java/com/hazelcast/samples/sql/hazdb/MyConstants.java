/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.samples.sql.hazdb;

import java.util.List;

public class MyConstants {

    public static final String IMAP_NAME_BUNDESLIGA = "bundesliga";
    public static final String IMAP_NAME_HEARTBEAT = "heartbeat";
    public static final String IMAP_NAME_LEADER = "leader";
    public static final String IMAP_NAME_STADIUM = "stadium";

    public static final List<String> IMAP_NAMES = List.of(IMAP_NAME_BUNDESLIGA, IMAP_NAME_HEARTBEAT, IMAP_NAME_LEADER,
            IMAP_NAME_STADIUM);

    public static final String VIEW_SUFFIX = "_v";
}
