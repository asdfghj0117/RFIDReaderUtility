package com.favepc.reader.rfidreaderutility.object;

import java.io.Serializable;

/**
 * implementing the Serializable interface
 */
public class UCode8 implements Serializable {

    private boolean title;//false = [TX]; true = [RX]
    private String data;
    private String pc;
    private String epc;
    private String rn16;
    private String tid;

    /**
     * Brand identifier(BI) constructor for enable BI, mode 1
     * @param title [TX] or [RX] title on view, false is [TX].
     * @param data send command
     * @param pc pc field of receive command
     * @param epc epc field of receive command
     * @param rn16 rn16 field of receive command
     */
    public UCode8(boolean title, String data, String pc, String epc, String rn16) {
        this.title = title;
        this.data = data;
        this.pc = pc;
        this.epc = epc;
        this.rn16 = rn16;
    }

    /**
     * Brand identifier(BI) constructor for enable BI, mode 2
     * @param title [TX] or [RX] title on view, false is [TX].
     * @param data send command
     * @param pc pc field of receive command
     * @param epc epc field of receive command
     */
    public UCode8(boolean title, String data, String pc, String epc) {
        this.title = title;
        this.data = data;
        this.pc = pc;
        this.epc = epc;
    }

    /**
     * Brand identifier(BI) constructor for disable BI, EPC + TID response
     * @param title [TX] or [RX] title on view, false is [TX] direction.
     * @param data send command
     * @param pc pc field of receive command
     * @param epc epc field of receive command
     * @param tid tid field of receive command
     * @param rn16 rn16 field of receive command
     */
    public UCode8(boolean title, String data, String pc, String epc, String tid, String rn16) {
        this.title = title;
        this.data = data;
        this.pc = pc;
        this.epc = epc;
        this.tid = tid;
        this.rn16 = rn16;

    }

    /**
     * @param title [TX] or [RX] direction, false is [TX].
     */
    public void Title(boolean title) { this.title = title; }

    /**
     * @param s TX direction content
     */
    public void Data(String s) { this.data = s; }

    /**
     * @param pc pc field of receive command
     */
    public void PC(String pc) { this.pc = pc; }

    /**
     * @param epc epc field of receive command
     */
    public void EPC(String epc) { this.epc = epc; }

    /**
     * @param rn rn16 field of receive command
     */
    public void RN16(String rn) { this.rn16 = rn; }

    /**
     * @param tid tid field of receive command
     */
    public void TID(String tid) { this.tid = tid; }

    /**
     * @return title field
     */
    public boolean Title() { return this.title; }

    /**
     * @return send data content
     */
    public String Data() { return this.data; }

    /**
     * @return pc field
     */
    public String PC() { return this.pc; }
    /**
     * @return epc field
     */
    public String EPC() { return this.epc; }
    /**
     * @return rn16 field
     */
    public String RN16() { return this.rn16; }
    /**
     * @return tid field
     */
    public String TID() { return this.tid; }
}
