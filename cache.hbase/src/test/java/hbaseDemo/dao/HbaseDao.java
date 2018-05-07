package hbaseDemo.dao;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseDao {
    private static Configuration conf = HBaseConfiguration.create();
    static {
//        conf.set("hbase.rootdir", "hdfs://cc/hbase");
    	conf.set("hbase.zookeeper.property.clientPort", "2181"); 
        // 设置Zookeeper,直接设置IP地址
        conf.set("hbase.zookeeper.quorum", "172.17.60.20");
    }

    // 创建表
    public static void createTable(String tablename, String columnFamily) throws Exception {
        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin = connection.getAdmin();

        TableName tableNameObj = TableName.valueOf(tablename);

        if (admin.tableExists(tableNameObj)) {
            System.out.println("Table exists!");
            System.exit(0);
        } else {
            HTableDescriptor tableDesc = new HTableDescriptor(TableName.valueOf(tablename));
            tableDesc.addFamily(new HColumnDescriptor(columnFamily));
            admin.createTable(tableDesc);
            System.out.println("create table success!");
        }
        admin.close();
        connection.close();
    }

    // 删除表
    public static void deleteTable(String tableName) {
        try {
            Connection connection = ConnectionFactory.createConnection(conf);
            Admin admin = connection.getAdmin();
            TableName table = TableName.valueOf(tableName);
            admin.disableTable(table);
            admin.deleteTable(table);
            System.out.println("delete table " + tableName + " ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 插入一行记录
    public static void addRecord(String tableName, String rowKey, String family, String qualifier, String value){
        try {
            Connection connection = ConnectionFactory.createConnection(conf);
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            table.close();
            connection.close();
            System.out.println("insert recored " + rowKey + " to table " + tableName + " ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        try {
        	HbaseDao.createTable("testTb", "info");
        	HbaseDao.addRecord("testTb", "001", "info", "name", "zhangsan");
        	HbaseDao.addRecord("testTb", "001", "info", "age", "20");
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
        //HbaseDao.deleteTable("testTb");
    }
}