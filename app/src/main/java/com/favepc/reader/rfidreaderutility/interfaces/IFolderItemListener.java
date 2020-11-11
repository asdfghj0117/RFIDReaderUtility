package com.favepc.reader.rfidreaderutility.interfaces;

import java.io.File;

public interface  IFolderItemListener {

    void OnCannotFileRead(File file);//implement what to do folder is Unreadable
    void OnFileClicked(final File file);//What to do When a file is clicked
}
