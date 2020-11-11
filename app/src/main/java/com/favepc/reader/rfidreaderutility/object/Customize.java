package com.favepc.reader.rfidreaderutility.object;

import java.io.Serializable;

public class Customize implements Serializable {

    private String command;
    private Boolean check;
    private String name;

    public Customize(Boolean b, String n, String cmd) {
        command = cmd;
        check = b;
        name = n;
    }

    public void Check(Boolean b) { this.check = b; }
    public void Command(String str) { this.command = str; }
    public void Name(String str) { this.name = str; }

    public Boolean Check() { return this.check; }
    public String Command() { return this.command; }
    public String Name() { return this.name; }
}
