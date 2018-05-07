package hbaseDemo.dao;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

public class BatchImport {

    public static class BatchImportMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        protected void map(LongWritable key, Text value, Context context)
                throws java.io.IOException, InterruptedException {
            // super.setup( context );
            //System.out.println(key + ":" + value);
            context.write(key, value);
        };
    }

    static class BatchImportReducer extends TableReducer<LongWritable, Text, NullWritable> {
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text text : values) {
                final String[] splited = text.toString().split("\t");
                final Put put = new Put(Bytes.toBytes(splited[0]));// 第一列行键
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("data1"), Bytes.toBytes(splited[1]));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("data2"), Bytes.toBytes(splited[2]));
                put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("data3"), Bytes.toBytes(splited[3]));
                context.write(NullWritable.get(), put);
            }
        };
    }

    /**
     * 之前一直报错，failed on connection exception 拒绝连接:nb0:8020
     * 因为namenode节点不在192.168.1.160上，而在192.168.1.161和192.168.1.162
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final Configuration conf = new Configuration();
        conf.set("hbase.rootdir", "hdfs://cetc32/hbase");
        // 设置Zookeeper,直接设置IP地址
        conf.set("hbase.zookeeper.quorum", "192.168.1.160,192.168.1.161,192.168.1.162");
        // 设置hbase表名称(先在shell下创建一个表：create 'mydata','info')
        conf.set(TableOutputFormat.OUTPUT_TABLE, "mydata");
        // 将该值改大，防止hbase超时退出
        conf.set("dfs.socket.timeout", "180000");

        //System.setProperty("HADOOP_USER_NAME", "root");
        // 设置fs.defaultFS
        conf.set("fs.defaultFS", "hdfs://192.168.1.161:8020");
        // 设置yarn.resourcemanager节点
        conf.set("yarn.resourcemanager.hostname", "nb1");

        Job job = Job.getInstance(conf);
        job.setJobName("HBaseBatchImport");
        job.setMapperClass(BatchImportMapper.class);
        job.setReducerClass(BatchImportReducer.class);
        // 设置map的输出，不设置reduce的输出类型
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormatClass(TextInputFormat.class);
        // 不再设置输出路径，而是设置输出格式类型
        job.setOutputFormatClass(TableOutputFormat.class);

        FileInputFormat.setInputPaths(job, "hdfs://192.168.1.161:8020/user/root/input/mydata.txt");

        boolean flag=job.waitForCompletion(true);
        System.out.println(flag);
    }
}