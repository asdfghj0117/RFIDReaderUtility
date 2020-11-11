package com.favepc.reader.rfidreaderutility.object;

import java.util.ArrayList;
import java.util.List;

public class ProfileXml {

    private List<String> sectionIdxs = null;
    private List<XmlSection> sections = null;

    public ProfileXml() {
        sectionIdxs = new ArrayList<String>();
        sections = new ArrayList<XmlSection>();
    }


    public void addSectionIndex (String s) {
        sectionIdxs.add(s);
    }

    public void addSection(XmlSection s) {
        sections.add(s);
    }

    public List<XmlSection> getSections() {
        return sections;
    }

    public List<String> getSectionIdxs() {
        return  sectionIdxs;
    }
}
