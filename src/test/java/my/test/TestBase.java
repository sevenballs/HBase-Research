/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package my.test;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.util.Bytes;

public class TestBase {
    private static final Configuration sharedConf = HBaseConfiguration.create();
    private static final HTablePool tablePool = new HTablePool(sharedConf, sharedConf.getInt("hbase.htable.pool.max", 100));

    protected String tableName;

    public TestBase() {
    }

    public TestBase(String tableName) {
        this.tableName = tableName;
    }

    public HTableInterface getHTable() {
        return (HTableInterface) tablePool.getTable(tableName);
    }

    public void createTable(String... familyNames) throws IOException {
        createTable(Compression.Algorithm.GZ, familyNames);
    }

    public void createTable(Compression.Algorithm compressionType, String... familyNames) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(sharedConf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (String familyName : familyNames) {
            HColumnDescriptor hcd = new HColumnDescriptor(familyName);
            hcd.setCompressionType(compressionType);
            hcd.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            hcd.setMaxVersions(3);
            hcd.setMinVersions(1);
            htd.addFamily(hcd);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd);
        }
    }

    public void createTable(HColumnDescriptor... columnDescriptors) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(sharedConf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (HColumnDescriptor columnDescriptor : columnDescriptors) {
            if (columnDescriptor.getDataBlockEncoding() == DataBlockEncoding.NONE)
                columnDescriptor.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            htd.addFamily(columnDescriptor);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd);
        }
    }

    public void createTable(byte[][] splitKeys, HColumnDescriptor... columnDescriptors) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(sharedConf);
        HTableDescriptor htd = new HTableDescriptor(tableName);

        for (HColumnDescriptor columnDescriptor : columnDescriptors) {
            if (columnDescriptor.getDataBlockEncoding() == DataBlockEncoding.NONE)
                columnDescriptor.setDataBlockEncoding(DataBlockEncoding.FAST_DIFF);
            htd.addFamily(columnDescriptor);
        }

        if (!admin.tableExists(htd.getName())) {
            admin.createTable(htd, splitKeys);
        }
    }

    public void deleteTable() throws IOException {
        HBaseAdmin admin = new HBaseAdmin(sharedConf);
        if (!admin.tableExists(tableName)) {
            return;
        }
        admin.disableTable(tableName);
        admin.deleteTable(tableName);
    }

    public byte[] toB(String str) {
        return Bytes.toBytes(str);
    }

    public byte[] toB(long v) {
        return Bytes.toBytes(v);
    }

    public String toS(byte[] bytes) {
        return Bytes.toString(bytes);
    }

    public void p(Object o) {
        System.out.println(o);
    }

    public void p() {
        System.out.println();
    }
}
