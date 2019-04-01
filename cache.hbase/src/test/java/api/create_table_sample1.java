package api;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.regionserver.BloomType;

public class create_table_sample1 {
	public static void main(String[] args) throws Exception {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "172.17.60.20");
		Connection connection = ConnectionFactory.createConnection(conf);
		Admin admin = connection.getAdmin();
		
		HTableDescriptor desc = new HTableDescriptor(TableName.valueOf("TEST1"));
		// MemStore大小。默认128M，不能小于1M
		desc.setMemStoreFlushSize(2097152L);
		// HFile最大size。默认10G。不能小于2M
		desc.setMaxFileSize(10485760L);
		// 日志flush的时候是同步写，还是异步写
		desc.setDurability(Durability.SYNC_WAL);
		
		HColumnDescriptor family1 = new HColumnDescriptor();
		// HColumnDescriptor family1 = new HColumnDescriptor(constants.COLUMN_FAMILY_DF.getBytes());
		family1.setTimeToLive(2 * 60 * 60 * 24); // 过期时间
		family1.setMaxVersions(2); // 版本数
		family1.setBlockCacheEnabled(true);
		desc.addFamily(family1);
		HColumnDescriptor family2 = new HColumnDescriptor();
		// HColumnDescriptor family2 = new HColumnDescriptor(constants.COLUMN_FAMILY_EX.getBytes());
		// 数据生存时间
		family2.setTimeToLive(3 * 60 * 60 * 24);
		// 最小版本数，默认0。
		family2.setMinVersions(2);
		// 最大版本数，默认-1
		family2.setMaxVersions(3);
		// bloom过滤器，有ROW和ROWCOL，ROWCOL除了过滤ROW还要过滤列族。默认ROW。
		family2.setBloomFilterType(BloomType.ROW);
		// 数据块的大小，单位bytes，默认值是65536。
		family2.setBlocksize(65536);
		// 数据块缓存，保存着每个HFile数据块的startKey。默认true。
		family2.setBlockCacheEnabled(true);
		// //写的时候缓存bloom。默认false。
		// family2.setCacheBloomsOnWrite(false);
		// //写的时候缓存索引。默认false。
		// family2.setCacheIndexesOnWrite(false);
		// //存储的时候使用压缩算法。默认NONE。
		// family2.setCompressionType(Compression.Algorithm.NONE);
		// //进行compaction的时候使用压缩算法。默认NONE。
		// family2.setCompactionCompressionType(Compression.Algorithm.NONE);
		// //压缩内存和存储的数据，区别于Snappy。默认NONE。
		// family2.setDataBlockEncoding(DataBlockEncoding.NONE);
		// //关闭的时候，是否剔除缓存的块。默认false。
		// family2.setEvictBlocksOnClose(false);
		// //让数据块缓存在LRU缓存里面有更高的优先级。默认false。
		// family2.setInMemory(false);
		// //集群间复制的时候，如果被设置成REPLICATION_SCOPE_LOCAL就不能被复制了。默认0
		// family2.setScope(HConstants.REPLICATION_SCOPE_GLOBAL);
		desc.addFamily(family2);
		
		admin.createTable(desc);
		admin.close();
		connection.close();
	}
}