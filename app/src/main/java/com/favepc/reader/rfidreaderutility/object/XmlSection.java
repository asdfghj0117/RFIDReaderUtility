package com.favepc.reader.rfidreaderutility.object;

public class XmlSection {
    private String check, type, name, commandState, command, tabIndex;

    public XmlSection() {}
    public XmlSection(String check, String type, String name, String commandState, String command, String tabIndex) {
        this.check = check;
        this.type = type;
        this.name = name;
        this.commandState = commandState;
        this.command = command;
        this.tabIndex = tabIndex;
    }

    public void CHECK(String c) { this.check = c; }
    public void TYPE(String c) { this.type = c; }
    public void NAME(String c) { this.name = c; }
    public void COMMANDSTATE(String c) { this.commandState = c; }
    public void COMMAND(String c) { this.command = c; }
    public void TABINDEX(String c) { this.tabIndex = c; }

    public String CHECK() { return this.check; }
    public String TYPE() { return this.type; }
    public String NAME() { return this.name; }
    public String COMMANDSTATE() { return this.commandState; }
    public String COMMAND() { return this.command; }
    public String TABINDEX() { return this.tabIndex; }
}
