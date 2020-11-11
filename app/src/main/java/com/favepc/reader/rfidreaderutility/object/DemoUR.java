package com.favepc.reader.rfidreaderutility.object;

import java.io.Serializable;

/**
 * Created by Bruce_Chiang on 2017/4/11.
 */

public class DemoUR implements Serializable {
    private String pc;
    private String epc;
    private String crc16;
    private String count;
    private String percentage;

    private String r;

    public DemoUR(String pc, String epc, String crc, String c, String per, String r) {
        this.pc = pc;
        this.epc = epc;
        this.crc16 = crc;
        this.count = c;
        this.percentage = per;
        this.r = r;
    }

    public DemoUR(String pc, String epc, String crc, String r) {
        this.pc = pc;
        this.epc = epc;
        this.crc16 = crc;
        this.r = r;
    }

    public void PC(String pc) { this.pc = pc; }
    public void EPC(String epc) { this.epc = epc; }
    public void CRC16(String crc) { this.crc16 = crc; }
    public void Count(String c) { this.count = c; }
    public void Percentage(String per) { this.percentage = per;}
    public void MemRead(String r) { this.r = r; }

    public String PC() { return this.pc; }
    public String EPC() { return this.epc; }
    public String CRC16() { return this.crc16; }
    public String Count() { return this.count; }
    public String Percentage() { return this.percentage; }
    public String MemRead() { return this.r; }
}
