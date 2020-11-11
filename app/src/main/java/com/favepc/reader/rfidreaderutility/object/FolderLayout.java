package com.favepc.reader.rfidreaderutility.object;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.interfaces.IFolderItemListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderLayout extends LinearLayout implements AdapterView.OnItemClickListener {

    Context context;
    IFolderItemListener folderListener;
    private List<Map<String, Object>> mFilesList;
    private List<String> mItems = null;
    private List<String> mPaths = null;
    private Map<String, Object> mFilesMap;
    private int[] mFileImg = {
            R.drawable.ic_directory,
            R.drawable.ic_file};
    private static final String ROOT = "/";
    private static final String PRE_LEVEL = "..";
    public static final int FIRST_ITEM = 0;
    public static final int SECOND_ITEM = 1;
    private String IMG_ITEM = "image";
    private String NAME_ITEM = "name";
    private TextView myPath;
    private ListView lstView;
    private String mDirPath = ROOT;

    public FolderLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.adapter_customize_folder_view, this);
        myPath = (TextView) findViewById(R.id.adapter_customize_folder_path);
        myPath.setEllipsize(TextUtils.TruncateAt.valueOf("MARQUEE"));
        lstView = (ListView) findViewById(R.id.adapter_customize_folder_list);

        Log.i("FolderView", "Constructed");
        //getDir(root, lstView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onListItemClick((ListView) parent, view, position, id);
    }

    public void setIFolderItemListener(IFolderItemListener folderItemListener) {
        this.folderListener = folderItemListener;
    }


    public void setFileDirectory(String dirPath) {
        mFilesList =  new ArrayList<>();
        mItems = new ArrayList<String>();
        mPaths = new ArrayList<String>();
        mDirPath = dirPath;
        getFileDirectory(dirPath);
    }


    private void getFileDirectory(String dirPath) {

        myPath.setText(dirPath);


        mFilesList.clear();
        mItems.clear();
        mPaths.clear();


        if (!dirPath.equals(ROOT)) {

            mFilesMap = new HashMap<>();
            mItems.add(ROOT);
            mPaths.add(FIRST_ITEM, ROOT);
            mFilesMap.put(IMG_ITEM, mFileImg[0]);
            mFilesMap.put(NAME_ITEM, ROOT);
            mFilesList.add(mFilesMap);
            //回上一層
            mFilesMap = new HashMap<>();
            mItems.add(PRE_LEVEL);
            mPaths.add(SECOND_ITEM, new File(dirPath).getParent());
            mFilesMap.put(IMG_ITEM, mFileImg[0]);
            mFilesMap.put(NAME_ITEM, PRE_LEVEL);
            mFilesList.add(mFilesMap);

        }

        File[] files = new File(dirPath).listFiles();

        for (int i = 0; i < files.length; i++) {

            mFilesMap = new HashMap<>();
            mItems.add(files[i].getName());
            mPaths.add(files[i].getPath());
            if(files[i].isDirectory()){
                mFilesMap.put(IMG_ITEM, mFileImg[0]);
            }
            else {
                mFilesMap.put(IMG_ITEM, mFileImg[1]);
            }
            mFilesMap.put(NAME_ITEM, files[i].getName());
            mFilesList.add(mFilesMap);

        }

        Log.i("Folders", files.length + "");

        setItemList();

    }

    //can manually set Item to display, if u want
    public void setItemList() {
        SimpleAdapter simpleAdapter = new SimpleAdapter(context,
                mFilesList, R.layout.adapter_customize_folder_view_row, new String[]{IMG_ITEM, NAME_ITEM},
                new int[]{R.id.image, R.id.text});
        lstView.setAdapter(simpleAdapter);
        lstView.setOnItemClickListener(this);
    }


    private String nowPath;
    public void onListItemClick(ListView l, View v, int position, long id) {
        String target = mPaths.get(position);
        if(target.equals(ROOT)){
            nowPath = mPaths.get(position);
            getFileDirectory(mDirPath);
            //simpleAdapter.notifyDataSetChanged();
        } else if(target.equals(PRE_LEVEL)){
            nowPath = mPaths.get(position);
            getFileDirectory(new File(nowPath).getParent());
            //simpleAdapter.notifyDataSetChanged();
        } else {
            File file = new File(target);

            if (file.canRead()) {
                if (file.isDirectory()) {
                    nowPath = mPaths.get(position);
                    getFileDirectory(mPaths.get(position));
                    //simpleAdapter.notifyDataSetChanged();
                } else{
                    String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                    if (extension.equals(".xml")) {
                        if (folderListener != null) {
                            folderListener.OnFileClicked(file);
                        }
                    }
                    else {
                        if (folderListener != null) {
                            folderListener.OnCannotFileRead(file);
                        }
                    }
                }
            } else{
                if (folderListener != null) {
                    folderListener.OnCannotFileRead(file);
                }
            }
        }
    }

}
