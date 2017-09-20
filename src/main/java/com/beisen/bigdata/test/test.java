package com.beisen.bigdata.test;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import com.beisen.bigdata.util.SparkUtil;

import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class test {
    private static final String readTableNamePre = "BEISENTALENTDW";
    private static final String family = "result";
    private static final byte[] familyBytes = family.getBytes();
    private static int count = 0;//用于记录每一个测试的得分项目数量
    private static String[] scores;
    private static final Logger logger = Logger.getLogger(test.class);
    public static void main(String[] args){
        logger.info("程序开始");
        SparkConf conf1 = new SparkConf();
        conf1.setMaster("local[*]");
        conf1.setAppName("shenyun_test");
        JavaSparkContext jsc =  new JavaSparkContext(conf1);
        String[] columnList = new String[]{"tenantid", "testid","score","score1"};
        Configuration conf = SparkUtil.buildHbaseConfigForTable(readTableNamePre + ".SHENYUN", family, columnList);
       
        JavaPairRDD<ImmutableBytesWritable, Result> testRdd = jsc.newAPIHadoopRDD(conf, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
      
        JavaPairRDD<String,String> talentRdd = testRdd.filter(new Function<Tuple2<ImmutableBytesWritable, Result>, Boolean>() {
            @Override
            public Boolean call(Tuple2<ImmutableBytesWritable, Result> f) throws Exception {
                Result result = f._2;
                String tenantId = new String(result.getValue(familyBytes,"tenantid".getBytes()));
                String score = new String(result.getValue(familyBytes,"score".getBytes()));
                if(tenantId != null && tenantId.equals("t1") && !score.isEmpty()){
                    return new Boolean(true);
                
                }
                return new Boolean(false);
            }
        }).mapToPair(new PairFunction<Tuple2<ImmutableBytesWritable, Result>, String, String>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<ImmutableBytesWritable, Result> t) throws Exception {
                        Result res = t._2;
                        Cell[] cells = res.rawCells();
                        String tenantId = Bytes.toString(res.getValue(familyBytes,"tenantid".getBytes()));
                        String testId = Bytes.toString(res.getValue(familyBytes,"testid".getBytes()));
                        String value = "";
                        for(int i = 0;i<cells.length;i++){
                            String temp = Bytes.toString(CellUtil.cloneQualifier(cells[i]));
                           // logger.info(temp);
                            if(temp.startsWith("score")){
                                value = value + "," + Bytes.toString(res.getValue(familyBytes,temp.getBytes()));
                            }
                        }
                        String key =  tenantId + " " + testId;
                        logger.info("key : " + key + " value : " + value);
                        return new Tuple2<>(key,value);
                    }
                });
        talentRdd.collect();
        logger.info("程序结束zzzzzzzzzzzz");
    }
}



















