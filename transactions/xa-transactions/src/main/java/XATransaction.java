/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

import com.atomikos.icatch.jta.UserTransactionManager;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.HazelcastXAResource;
import com.hazelcast.transaction.TransactionContext;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.io.File;
import java.io.FilenameFilter;

public class XATransaction {

    public static void main(String[] args) throws Exception {
        cleanAtomikosLogs();

        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        HazelcastXAResource xaResource = instance.getXAResource();

        UserTransactionManager tm = new UserTransactionManager();
        tm.begin();

        Transaction transaction = tm.getTransaction();
        transaction.enlistResource(xaResource);
        TransactionContext context = xaResource.getTransactionContext();
        TransactionalMap<Object, Object> map = context.getMap("map");
        map.put("key", "val");
        transaction.delistResource(xaResource, XAResource.TMSUCCESS);

        tm.commit();

        IMap<Object, Object> m = instance.getMap("map");
        Object val = m.get("key");
        System.out.println("value: " + val);

        cleanAtomikosLogs();
        Hazelcast.shutdownAll();
    }

    private static void cleanAtomikosLogs() {
        try {
            File currentDir = new File(".");
            final File[] tmLogs = currentDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".epoch") || name.startsWith("tmlog");
                }
            });
            for (File tmLog : tmLogs) {
                tmLog.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
