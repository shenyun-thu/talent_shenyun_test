package com.beisen.bigdata.test;

import org.apache.hadoop.hbase.util.Bytes;

public class PartitionRowKeyManager {
    public static final int AMOUNT = 20;
    private long currentId = 1;
    private int partition = AMOUNT;
    public void setPartition(int partition){
        this.partition = partition;
    }
    
    public byte[] nextId(){
        try{
            long partitionId = currentId % partition;
            return Bytes.add(Bytes.toBytes(partitionId),Bytes.toBytes(currentId));
        }finally {
            currentId ++;
        }
    }
    
    public byte[][] calsSplitKeys(){
        byte[][] splitKeys = new byte[partition - 1][];
        for(int i = 1;i < partition;i++){
            splitKeys[i-1] = Bytes.toBytes((long)i*1000000000);
        }
        return splitKeys;
    }
}
