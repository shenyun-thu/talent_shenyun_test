package com.beisen.bigdata.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class part_test {
    private static byte[][] getSplitKeys(){
        String[] keys = new String[] { "000|","001|","002|","003|","004|","005|","006|","007|","008|","009|","010|","011|","012|","013|","014|","015|","016|","017|","018|","019|","020|",
                "021|","022|","023|","024|","025|","026|","027|","028|","029|","030|","031|","032|","033|","034|","035|","036|","037|","038|","039|","040|","041|","042|","043|","044|","045|",
                "046|","047|","048|","049|","050|","051|","052|","053|","054|","055|","056|","057|","058|","059|","060|","061|","062|","063|","064|","065|","066|","067|","068|","069|","070|",
                "071|","072|","073|","074|","075|","076|","077|","078|","079|","080|","081|","082|","083|","084|","085|","086|","087|","088|","089|","090|","091|","092|","093|","094|","095|",
                "096|","097|","098|","099|","100|","101|","102|","103|","104|","105|","106|","107|","108|","109|","110|","111|","112|","113|","114|","115|","116|","117|","118|","119|","120|",
                "121|","122|","123|","124|","125|","126|","127|","128|","129|","130|","131|","132|","133|","134|","135|","136|","137|","138|","139|","140|","141|","142|","143|","144|","145|",
                "146|","147|","148|","149|","150|","151|","152|","153|","154|","155|","156|","157|","158|","159|","160|","161|","162|","163|","164|","165|","166|","167|","168|","169|","170|",
                "171|","172|","173|","174|","175|","176|","177|","178|","179|","180|","181|","182|","183|","184|","185|","186|","187|","188|","189|","190|","191|","192|","193|","194|","195|",
                "196|","197|","198|","199|","200|","201|","202|","203|","204|","205|","206|","207|","208|","209|","210|","211|","212|","213|","214|","215|","216|","217|","218|","219|","220|",
                "221|","222|","223|","224|","225|","226|","227|","228|","229|","230|","231|","232|","233|","234|","235|","236|","237|","238|","239|","240|","241|","242|","243|","244|","245|",
                "246|","247|","248|","249|","250|","251|","252|","253|","254|"};
        byte[][] splitKeys = new byte[keys.length][];
        TreeSet<byte[]> rows = new TreeSet<byte[]>(Bytes.BYTES_COMPARATOR);
        for(int i = 0;i < keys.length;i++){
            rows.add(Bytes.toBytes(keys[i]));
        }
        Iterator<byte[]> rowKeyIter = rows.iterator();
        int i = 0;
        while(rowKeyIter.hasNext()){
            byte[] tempRow = rowKeyIter.next();
            rowKeyIter.remove();
            splitKeys[i] = tempRow;
            i++;
        }
        return splitKeys;
    }

    private static String getRandomNumber(){
        String ranStr = Math.random()+"";
        int pointIndex = ranStr.indexOf(".");
        int temp = Integer.parseInt(ranStr.substring(pointIndex+1, pointIndex+4));
        temp = temp % 256;
        return String.valueOf(temp);
    }
    
    private static List<Put> batchPut(){
        List<Put> list = new ArrayList<Put>();
        for(int i=1;i<=10000;i++){
            byte[] rowkey = Bytes.toBytes(getRandomNumber()+"-"+System.currentTimeMillis()+"-"+i);
            Put put = new Put(rowkey);
            put.add(Bytes.toBytes("fmy"), Bytes.toBytes("name"), Bytes.toBytes("zs"+i));
            list.add(put);
        }
        return list;
    }
    public static void main(String[] atgs)throws Exception {
        Configuration conf = HBaseConfiguration.create();
        String tableName = "beisendw:part_test";
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("hbase.zookeeper.quorum", "hdfs00,hdfs01,hdfs02");
        conf.set(TableInputFormat.INPUT_TABLE,tableName);
        
        byte[][] splitKeys = getSplitKeys();
        HBaseAdmin hadmin = new HBaseAdmin(conf);
        if(!hadmin.isTableAvailable(tableName)){
            HTableDescriptor tableDesc = new HTableDescriptor(tableName);
            tableDesc.addFamily(new HColumnDescriptor("fmy".getBytes()));
            hadmin.createTable(tableDesc,splitKeys);
        }
        HTable table = new HTable(conf,tableName);
        table.put(batchPut());
    }
}
