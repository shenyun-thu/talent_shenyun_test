package com.beisen.bigdata.util

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter.FilterList.Operator
import org.apache.hadoop.hbase.filter.{Filter, FilterList, SingleColumnValueFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.log4j.Logger

/**
  * Created by hanweiwei on 2017/7/5.
  */
object HbaseUtil {
  private val logger = Logger.getLogger(this.getClass.getName)

  /**
    * 写入HBase
    *
    * @param tableName
    * @param putList
    * @param isOnline
    *  是否线上HBASE
    * @return
    */
  def putHbase(tableName: String, putList: util.List[Put], isOnline : Boolean): Boolean = {
    var conn: Connection = null
    var table: Table = null
    try {
      conn = getHbaseConnection(isOnline)
      table = conn.getTable(TableName.valueOf(tableName))
      table.put(putList)
      true
    } catch {
      case e: Exception => logger.error(e)
        false
    } finally {
      if (table != null) {
        table.close()
      }
      if (conn != null && !conn.isClosed) {
        conn.close()
      }
    }
  }

  /**
    * 设置Hbase数据增量读取过滤条件
    * @param scan
    * @param backwardDayRange
    */
  def setIncrementalFilter(scan : Scan, backwardDayRange : Integer) : Unit={
    if(backwardDayRange <= 0){
      logger.info("全量任务")
      return
    }
    val FAMILY_BYTE_0 = "0".getBytes

    // 增量导入添加时间过滤
    val insertTimeBytes = Bytes.toBytes("ETLINSERTTIME")
    scan.addColumn(FAMILY_BYTE_0, insertTimeBytes)
    val sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00.000")
    val cal = Calendar.getInstance
    cal.add(Calendar.DATE, -backwardDayRange)
    val beginDate = sdf.format(cal.getTime)
    val endDate = sdf.format(new Date)

    logger.info("增量任务" + beginDate + "——" + endDate)
    val startFilter = new SingleColumnValueFilter(FAMILY_BYTE_0, insertTimeBytes, CompareOp.GREATER_OR_EQUAL, Bytes.toBytes(beginDate))
    val endFilter = new SingleColumnValueFilter(FAMILY_BYTE_0, insertTimeBytes, CompareOp.LESS, Bytes.toBytes(endDate))
    startFilter.setFilterIfMissing(true)
    endFilter.setFilterIfMissing(true)
    val filters = new util.ArrayList[Filter]
    filters.add(startFilter)
    filters.add(endFilter)
    val filterList = new FilterList(Operator.MUST_PASS_ALL, filters)
    scan.setFilter(filterList)
  }

  /**
    * 创建Hbase连接
    * @param isOnLine
    * @return
    */
  def getHbaseConnection(isOnLine : Boolean) : Connection ={
    val conf = new Configuration
    if(isOnLine) {
      conf.set("hbase.zookeeper.quorum", "tjhadoop00,tjhadoop01,tjhadoop02")
    }else {
      conf.set("hbase.zookeeper.quorum", "hdfs00,hdfs01,hdfs02")
    }

    System.setProperty("HADOOP_USER_NAME", "hadoop")

    ConnectionFactory.createConnection(HBaseConfiguration.create(conf))
  }
}
