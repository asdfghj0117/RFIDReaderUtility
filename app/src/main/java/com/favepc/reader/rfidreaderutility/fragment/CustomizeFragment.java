package com.favepc.reader.rfidreaderutility.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.favepc.reader.rfidreaderutility.AppContext;
import com.favepc.reader.rfidreaderutility.MainActivity;
import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.adapter.CommonListAdapter;
import com.favepc.reader.rfidreaderutility.adapter.CustomizeListAdapter;
import com.favepc.reader.rfidreaderutility.implments.SwipeItemTouchHelperCallback;
import com.favepc.reader.rfidreaderutility.interfaces.IFolderItemListener;
import com.favepc.reader.rfidreaderutility.interfaces.SwipeItemTouchHelperActions;
import com.favepc.reader.rfidreaderutility.object.Common;
import com.favepc.reader.rfidreaderutility.object.CustomKeyboardManager;
import com.favepc.reader.rfidreaderutility.object.Customize;
import com.favepc.reader.rfidreaderutility.object.CustomizeObjectSerializer;
import com.favepc.reader.rfidreaderutility.object.FolderLayout;
import com.favepc.reader.rfidreaderutility.object.ProfileXml;
import com.favepc.reader.rfidreaderutility.object.XmlSection;
import com.favepc.reader.service.ReaderService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class CustomizeFragment extends Fragment {

    public enum CommandStates {
        REGULATION,
        DEFAULT, EPC, TID, SELECT, INFO,
        PASSWORD, READ, WRITE, LOCK, KILL,//MULTI,
        CUSTOM, CUSTOM_EM,
        GPIO_CONFIG,
        GPIO_CONFIG_10C, GPIO_CONFIG_10UC,
        GPIO_CONFIG_11C, GPIO_CONFIG_11UC,
        GPIO_CONFIG_14C, GPIO_CONFIG_14UC,
        GPIO_PINS,
        GPIO_GET_PINS,
        GPIO_PIN_10C, GPIO_PIN_10UC,
        GPIO_PIN_11C, GPIO_PIN_11UC,
        GPIO_PIN_14C, GPIO_PIN_14UC,

        B02_T, B02_P,
        B02_UR_SLOTQ, B02_UR, B02_U_SLOTQ, B02_U,
        B02_QR, B02_Q,

        B02ITEM02_R, B02ITEM02_W, B02ITEM02_P, B02ITEM02_T, B02ITEM02_L,
        B02ITEM02_UR_SLOTQ, B02ITEM02_UR, B02ITEM02_U_SLOTQ, B02ITEM02_U,
        B02ITEM02_QR, B02ITEM02_Q, B02ITEM02_CUSTOMIZE,

        B03_SELECT, B03_WRITE, B03_READ,
        B03_GET, B03_TAGRUN,

        B04_GPIO_PINS,
        B04_ANTENNA01, B04_ANTENNA02, B04_ANTENNA03, B04_ANTENNA04,
        B04_ANTENNA05, B04_ANTENNA06, B04_ANTENNA07, B04_ANTENNA08,
    };


    public static final String CUSTOMIZE_COMMAND = "CUSTOMIZE_COMMAND";
    public static final String CUSTOMIZE_DATA = "CUSTOMIZE_DATA";

    public static final String SHARED_PREFS_FILE = "CUSTOMIZE_SHARED_PREFS_FILE";


    private Context mContext;
    private Activity mActivity;
    private AppContext mAppContext;
    private View mCustomizeView = null;
    private CustomKeyboardManager mCustomKeyboardManager;
    private ReaderService mReaderService;

    private ListView mListViewMessage;
    private RecyclerView mListViewCommand;
    private RelativeLayout mRelativeLayoutCommand;
    private CustomizeListAdapter mCustomizeListAdapter;
    private ArrayList<Customize> mCustomizes = new ArrayList<Customize>();
    private CheckBox cbProcess;
    private FloatingActionButton mFab;
    private int mIndex;
    private Button mBtnStart;
    private ImageButton mBtnOpenFolder, mBtnSaveFolder;
    private ArrayList<HashMap<String, String>> mProcessList;
    private CommonListAdapter mCommonListAdapter;
    private ArrayList<Common> mCommons = new ArrayList<Common>();
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    private SwipeItemTouchHelperCallback mRecyclerViewItemTouchCallback = null;

    private final static float CLICK_DRAG_TOLERANCE = 10;   // Often, there will be a slight, unintentional,
    // drag when the user taps the FAB, so we need to account for this.

    private float downRawX, downRawY;
    private float dX, dY;


    private static final int REQUEST_COARSE_READ_PERMISSIONS = 789;
    private static final int REQUEST_COARSE_WRITE_PERMISSIONS = 790;
    FolderLayout mLocalFolders;


    public CustomizeFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public CustomizeFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mAppContext = (AppContext) context.getApplicationContext();
        this.mCustomKeyboardManager = this.mAppContext.getKeyboard();
        this.mActivity = getActivity();
        this.mReaderService = new ReaderService();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mCustomizeView == null) {

            this.mProcessList = new ArrayList<>();

            this.mCustomizeView = inflater.inflate(R.layout.fragment_customize, container, false);

            this.mCommonListAdapter = new CommonListAdapter(this.mContext, R.layout.adapter_common, this.mCommons);
            ListView lv = (ListView)this.mCustomizeView.findViewById(R.id.fragment_customize_lv_msg);
            lv.setAdapter(this.mCommonListAdapter);

            this.mListViewMessage = (ListView)mCustomizeView.findViewById(R.id.fragment_customize_lv_msg);
            //this.mListViewCommand = (ListView)mCustomizeView.findViewById(R.id.fragment_customize_lv_command);
            this.mListViewCommand = (RecyclerView)mCustomizeView.findViewById(R.id.fragment_customize_rv_command);
            this.mRelativeLayoutCommand = (RelativeLayout) mCustomizeView.findViewById(R.id.fragment_customize_rl);

            SharedPreferences prefs = mActivity.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
            try {
                mCustomizes = (ArrayList<Customize>) CustomizeObjectSerializer.deserialize(prefs.getString("TASKS", CustomizeObjectSerializer.serialize(new ArrayList<Customize>())));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ListView change to RecycleView
            //this.mCustomizeListAdapter = new CustomizeListAdapter(this.mContext, R.layout.adapter_customize_listview_item, this.mCustomizes);
            //this.mCustomizeListAdapter.setMode(Attributes.Mode.Single);
            this.mCustomizeListAdapter = new CustomizeListAdapter(R.layout.adapter_customize_listview_item, this.mCustomizes);

            //final LinearLayoutManager layoutManager = new LinearLayoutManager(this.mContext);
            //layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            this.mListViewCommand.setLayoutManager(new LinearLayoutManager(this.mContext, LinearLayoutManager.VERTICAL, false));
            this.mListViewCommand.setAdapter(this.mCustomizeListAdapter);

            this.mRecyclerViewItemTouchCallback = new SwipeItemTouchHelperCallback(new SwipeItemTouchHelperActions() {
                @Override
                public void onLeftClicked(int position) {
                    super.onLeftClicked(position);
                }

                @Override
                public void onRightClicked(int position) {
                    mCustomizes.remove(position);
                    mCustomizeListAdapter.notifyItemRemoved(position);
                    mCustomizeListAdapter.notifyItemRangeChanged(position, mCustomizeListAdapter.getItemCount());
                    super.onRightClicked(position);
                }

                @Override
                public void onMoveClicked(int fromPosition, int toPosition) {
                    Collections.swap(mCustomizes, fromPosition, toPosition);
                    mCustomizeListAdapter.notifyItemMoved(fromPosition, toPosition);
                    //Log.i("RecyclerView", "onMove: from " + fromPosition + " to " + toPosition);
                    super.onMoveClicked(fromPosition, toPosition);
                }
            });

            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(mRecyclerViewItemTouchCallback);
            mItemTouchHelper.attachToRecyclerView(mListViewCommand);

            this.mListViewCommand.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                    mRecyclerViewItemTouchCallback.onDraw(c);
                    //super.onDraw(c, parent, state);
                }
            });

            this.cbProcess = (CheckBox) this.mCustomizeView.findViewById(R.id.fragment_customize_checkbox_process);
            this.cbProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        mListViewCommand.setVisibility(View.GONE);
                        mRelativeLayoutCommand.setVisibility(View.GONE);
                        //mFab.setVisibility(View.GONE);
                        mFab.hide();
                    }
                    else {
                        mListViewCommand.setVisibility(View.VISIBLE);
                        mRelativeLayoutCommand.setVisibility(View.VISIBLE);
                        //mFab.setVisibility(View.VISIBLE);
                        mFab.show();
                    }
                }
            });

            this.mFab = (FloatingActionButton) this.mCustomizeView.findViewById(R.id.fragment_customize_fab);
            this.mFab.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {

                        downRawX = motionEvent.getRawX();
                        downRawY = motionEvent.getRawY();
                        dX = view.getX() - downRawX;
                        dY = view.getY() - downRawY;

                        return true; // Consumed

                    }
                    else if (action == MotionEvent.ACTION_MOVE) {

                        int viewWidth = view.getWidth();
                        int viewHeight = view.getHeight();

                        View viewParent = (View)view.getParent();
                        int parentWidth = viewParent.getWidth();
                        int parentHeight = viewParent.getHeight();

                        float newX = motionEvent.getRawX() + dX;
                        newX = Math.max(0, newX); // Don't allow the FAB past the left hand side of the parent
                        newX = Math.min(parentWidth - viewWidth, newX); // Don't allow the FAB past the right hand side of the parent

                        float newY = motionEvent.getRawY() + dY;
                        newY = Math.max(0, newY); // Don't allow the FAB past the top of the parent
                        newY = Math.min(parentHeight - viewHeight, newY); // Don't allow the FAB past the bottom of the parent

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();

                        return true; // Consumed

                    }
                    else if (action == MotionEvent.ACTION_UP) {

                        float upRawX = motionEvent.getRawX();
                        float upRawY = motionEvent.getRawY();

                        float upDX = upRawX - downRawX;
                        float upDY = upRawY - downRawY;

                        if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                            return view.performClick();
                        }
                        else { // A drag
                            return true; // Consumed
                        }

                    }
                    else {
                        return view.onTouchEvent(motionEvent);
                    }
                }
            });

            this.mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LayoutInflater _inflater = LayoutInflater.from(mContext);
                    View mView = _inflater.inflate(R.layout.fragment_customize_command_dialog, null);
                    AlertDialog.Builder _alertDialogB = new AlertDialog.Builder(mContext);
                    _alertDialogB.setView(mView);

                    final EditText _editTextCommand = (EditText) mView.findViewById(R.id.fragment_customize_dialog_command);
                    final EditText _editTextName = (EditText) mView.findViewById(R.id.fragment_customize_dialog_name);
                    _alertDialogB
                            .setTitle("New Command:")
                            .setCancelable(false)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    mCustomizes.add(new Customize(true, _editTextName.getText().toString(), _editTextCommand.getText().toString()));
                                    //mCustomizeListAdapter.notifyDataSetChanged();
                                    mCustomizeListAdapter.notifyItemInserted(mCustomizes.size());
                                }
                            })

                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                        }
                                    });

                    AlertDialog _alertDialog = _alertDialogB.create();
                    _alertDialog.show();
                }
            });

            this.mBtnStart = (Button) this.mCustomizeView.findViewById(R.id.fragment_customize_btn_send);
            this.mBtnStart.setOnClickListener(new View.OnClickListener() {
                HashMap<String, String> _item;

                @Override
                public void onClick(View v) {
                    if (((MainActivity) mContext).isConnected()) {
                        mProcessList.clear();
                        for (int i = 0; i < mCustomizes.size(); i++) {
                            if (mCustomizes.get(i).Check()) {
                                _item = new HashMap<String, String>();
                                String _str = mCustomizes.get(i).Command();
                                if (_str.indexOf("U") != -1)
                                    _item.put(CUSTOMIZE_COMMAND, "Multi");
                                else
                                    _item.put(CUSTOMIZE_COMMAND, "Single");

                                _item.put(CUSTOMIZE_DATA, ReaderService.Format.bytesToString(mReaderService.raw(mCustomizes.get(i).Command())));
                                mProcessList.add(_item);
                            }
                        }

                        if (mProcessList.size() == 0) {
                            Toast.makeText(mContext, "Check one command to run.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            mCustomizeHandler.post(mRunnableBackground);
                        }
                    }
                    else {
                        Toast.makeText(mContext, "All of the communication interface are unlinked.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            this.mBtnOpenFolder = (ImageButton) this.mCustomizeView.findViewById(R.id.fragment_customize_btn_folder);
            this.mBtnOpenFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //getOwnerActivity
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(mActivity).setMessage("需要開啟READ_EXTERNAL_STORAGE權限")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        ActivityCompat.requestPermissions(mActivity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_COARSE_READ_PERMISSIONS);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                            return;
                        }
                        else {
                            ActivityCompat.requestPermissions(mActivity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_COARSE_READ_PERMISSIONS);
                            return;
                        }
                    }

                    View mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_customize_folder_dialog, null);
                    AlertDialog alert = new FolderAlertDialog(mContext, mView);
                    ((FolderAlertDialog) alert).setIFileListener(new ILoadingFileListener() {
                        @Override
                        public void OnLoadingFile(File file) {

                            ProfileXml xml = parseXml(file.getAbsolutePath());
                            mCustomizes.clear();

                            for (int i = 0; i < xml.getSectionIdxs().size(); i++) {
                                mCustomizes.add(new Customize(
                                        Boolean.valueOf(xml.getSections().get(i).CHECK()),
                                        xml.getSections().get(i).NAME(),
                                        xml.getSections().get(i).COMMAND()));
                            }

                            mCustomizeListAdapter.notifyDataSetChanged();
                            //mCustomizeListAdapter.notifyItemInserted(mCustomizes.size());
                        }
                    });
                    alert.show();
                }
            });

            this.mBtnSaveFolder = (ImageButton) this.mCustomizeView.findViewById(R.id.fragment_customize_btn_save_folder);
            this.mBtnSaveFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            new AlertDialog.Builder(mActivity).setMessage("需要開啟WRITE_EXTERNAL_STORAGE權限")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            ActivityCompat.requestPermissions(mActivity,
                                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_COARSE_WRITE_PERMISSIONS);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create()
                                    .show();
                            return;
                        }
                        else {
                            ActivityCompat.requestPermissions(mActivity,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_COARSE_WRITE_PERMISSIONS);
                            return;
                        }
                    }

                    LayoutInflater _inflater = LayoutInflater.from(mContext);
                    View mView = _inflater.inflate(R.layout.fragment_customize_command_dialog, null);

                    final EditText _editTextCommand = (EditText) mView.findViewById(R.id.fragment_customize_dialog_command);
                    final TextView __editTextCommandTitle = (TextView)  mView.findViewById(R.id.fragment_customize_dialog_command_title);
                    _editTextCommand.setVisibility(View.GONE);
                    __editTextCommandTitle.setVisibility(View.GONE);
                    AlertDialog.Builder _alertDialogB = new AlertDialog.Builder(mContext);
                    _alertDialogB.setView(mView);

                    final EditText _editText = (EditText) mView.findViewById(R.id.fragment_customize_dialog_command);
                    _alertDialogB
                            .setTitle("Save File Name:")
                            .setCancelable(false)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialogBox, int id) {

                                    writeXml(mCustomizes,_editText.getText().toString().trim() + ".xml");
                                }
                            })

                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {
                                            dialogBox.cancel();
                                        }
                                    });

                    AlertDialog _alertDialog = _alertDialogB.create();
                    _alertDialog.show();
                }
            });

        }
        return this.mCustomizeView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_READ_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "READ_EXTERNAL_STORAGE權限已開啟", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(mContext, "READ_EXTERNAL_STORAGE權限未開啟", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_COARSE_WRITE_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "WRITE_EXTERNAL_STORAGE權限已開啟", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(mContext, "WRITE_EXTERNAL_STORAGE權限未開啟", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /*private static ArrayList<String> getDevMountList() {
        String[] toSearch = FileUtils.readFile("/etc/vold.fstab").split(" ");
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }*/

    /*public static String getExternalSdCardPath() {

        if (SDCardUtils.isMounted()) {
            File sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdCardFile.getAbsolutePath();
        }

        String path = null;

        File sdCardFile = null;

        ArrayList<String> devMountList = getDevMountList();

        for (String devMount : devMountList) {
            File file = new File(devMount);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                } else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }

        return null;
    }*/

    private static String ROOT_NAME = "profile";
    private static String SECTION_NAME = "section";
    private static String TITLE_NAME = "name";
    private static String ENTRY_NAME = "entry";
    private static final String CDATA_XML_ELEMENTS = "text definition note profile";
    private ProfileXml parseXml(String path) {


        ProfileXml xml = new ProfileXml();
        File loadFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(loadFile);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName(SECTION_NAME);

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node nNode = nodeList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    XmlSection section = new XmlSection();
                    Element element = (Element) nNode;

                    section.CHECK(element.getElementsByTagName("entry").item(0).getTextContent());
                    section.TYPE(element.getElementsByTagName("entry").item(1).getTextContent());
                    section.NAME(element.getElementsByTagName("entry").item(2).getTextContent());
                    section.COMMANDSTATE(element.getElementsByTagName("entry").item(3).getTextContent());
                    section.COMMAND(element.getElementsByTagName("entry").item(4).getTextContent());
                    section.TABINDEX(element.getElementsByTagName("entry").item(5).getTextContent());
                    xml.addSectionIndex(element.getAttribute("name"));
                    xml.addSection(section);
                }
            }


        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
        }

        return xml;
    }



    private void writeXml(ArrayList<Customize> customize , String path) {


        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath(), path);
        FileOutputStream fileOutputStream = null;
        try {
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }else {
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element profile = document.createElement(ROOT_NAME);
            document.appendChild(profile);

            Customize object;
            for (int i = 0; i < customize.size(); i++) {
                object = customize.get(i);
                Element section = document.createElement(SECTION_NAME);
                section.setAttribute(TITLE_NAME, String.valueOf(i));

                Element check = document.createElement(ENTRY_NAME);
                check.setAttribute(TITLE_NAME, "CHECK");
                check.appendChild(document.createTextNode(String.valueOf(object.Check())));
                section.appendChild(check);

                Element type = document.createElement(ENTRY_NAME);
                type.setAttribute(TITLE_NAME, "TYPE");
                type.appendChild(document.createTextNode("true"));
                section.appendChild(type);

                Element name = document.createElement(ENTRY_NAME);
                name.setAttribute(TITLE_NAME, "NAME");
                name.appendChild(document.createTextNode("\n"));
                section.appendChild(name);

                Element commandState = document.createElement(ENTRY_NAME);
                commandState.setAttribute(TITLE_NAME, "COMMANDSTATE");
                commandState.appendChild(document.createTextNode(String.valueOf(CommandStates.B02ITEM02_CUSTOMIZE.ordinal())));
                section.appendChild(commandState);

                Element command = document.createElement(ENTRY_NAME);
                command.setAttribute(TITLE_NAME, "COMMAND");
                command.appendChild(document.createTextNode(object.Command()));
                section.appendChild(command);

                Element tabIndex = document.createElement(ENTRY_NAME);
                tabIndex.setAttribute(TITLE_NAME, "TABINDEX");
                tabIndex.appendChild(document.createTextNode("0"));
                section.appendChild(tabIndex);

                profile.appendChild(section);

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", new Integer(2));
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource domSource = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            StreamResult streamResult = new StreamResult(printWriter);
            transformer.transform(domSource, streamResult);

            Intent scan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scan.setData(Uri.fromFile(file));
            mActivity.sendBroadcast(scan);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        /*try {
            SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();
            Transformer transformer = transformerHandler.getTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            //transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, CDATA_XML_ELEMENTS);

            Result result = new StreamResult(fileOutputStream);
            transformerHandler.setResult(result);
            transformerHandler.startDocument();
            AttributesImpl attributes = new AttributesImpl();
            transformerHandler.startElement("", ROOT_NAME, ROOT_NAME, attributes);
            Customize object;
            for (int i = 0; i < customize.size(); i++) {
                object = customize.get(i);
                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", String.valueOf(i));
                transformerHandler.startElement("", SECTION_NAME, SECTION_NAME, attributes);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "CHECK");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters(String.valueOf(object.Check()).toCharArray(), 0, String.valueOf(object.Check()).length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "TYPE");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters("true".toCharArray(), 0, "true".length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "NAME");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters("\n".toCharArray(), 0, "\n".length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "COMMANDSTATE");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters(String.valueOf(CommandStates.B02ITEM02_CUSTOMIZE.ordinal()).toCharArray(), 0, String.valueOf(CommandStates.B02ITEM02_CUSTOMIZE.ordinal()).length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "COMMAND");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters(object.Command().toCharArray(), 0, object.Command().length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                attributes.clear();
                attributes.addAttribute("", TITLE_NAME, TITLE_NAME, "", "TABINDEX");
                transformerHandler.startElement("", ENTRY_NAME, ENTRY_NAME, attributes);
                transformerHandler.characters("0".toCharArray(), 0, "0".length());
                transformerHandler.endElement("", ENTRY_NAME, ENTRY_NAME);

                transformerHandler.endElement("", SECTION_NAME, SECTION_NAME);
            }
            transformerHandler.endElement("", ROOT_NAME, ROOT_NAME);
            transformerHandler.endDocument();


        } catch (TransformerConfigurationException | SAXException e) {
            e.printStackTrace();
        }*/

        /*try {

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, ROOT_NAME);

            insertCustomizes(xmlSerializer, customize);

            xmlSerializer.endTag(null, ROOT_NAME);
            xmlSerializer.endDocument();
            xmlSerializer.flush();



            String dataWrite = writer.toString();
            fileOutputStream.write(dataWrite.getBytes());
            fileOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void insertCustomizes(XmlSerializer xmlSerializer, ArrayList<Customize> customize) throws IOException {

        Customize object;
        for (int i = 0; i < customize.size(); i++) {
            object = customize.get(i);
            xmlSerializer.startTag(null, SECTION_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, String.valueOf(i));

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "CHECK");
            xmlSerializer.text(String.valueOf(object.Check()));
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "TYPE");
            xmlSerializer.text("true");
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "NAME");
            xmlSerializer.text("\n");
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "COMMANDSTATE");
            xmlSerializer.text(String.valueOf(CommandStates.B02ITEM02_CUSTOMIZE.ordinal()));
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "COMMAND");
            xmlSerializer.text(object.Command());
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.startTag(null, ENTRY_NAME);
            xmlSerializer.attribute(null, TITLE_NAME, "TABINDEX");
            xmlSerializer.text("0");
            xmlSerializer.endTag(null, ENTRY_NAME);

            xmlSerializer.endTag(null, SECTION_NAME);
        }

    }

    private interface ILoadingFileListener {

        void OnLoadingFile(File file);//implement what to do folder is Unreadable
    }

    private static class FolderAlertDialog extends AlertDialog {

        ILoadingFileListener loadingFileListener;

        public void setIFileListener(ILoadingFileListener loadingFileListener) {
            this.loadingFileListener = loadingFileListener;
        }


        protected FolderAlertDialog(Context context, View view) {
            super(context);

            setView(view);
            FolderLayout _localFolders = (FolderLayout)view.findViewById(R.id.fragment_customize_local_folders);
            _localFolders.setIFolderItemListener(new IFolderItemListener() {
                @Override
                public void OnCannotFileRead(File file) {
                    new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.ic_cancel_black_36dp)
                            .setTitle(
                                    "[" + file.getName() + "] folder can't be read!")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }

                @Override
                public void OnFileClicked(final File file) {
                    new AlertDialog.Builder(getContext())
                            .setIcon(R.drawable.ic_file)
                            .setTitle("Loading [" + file.getName() + "] ?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    loadingFileListener.OnLoadingFile(file);
                                    dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
                }
            });

            _localFolders.setFileDirectory(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath());//"./sys" change directory if u want,default is root
        }

    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.cbProcess.setChecked(true);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }



    @Override
    public void onPause() {

        SharedPreferences prefs = mActivity.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("TASKS", CustomizeObjectSerializer.serialize(mCustomizes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mActivity.getMenuInflater().inflate(R.menu.fragment_clear, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                if (mCommons != null && mCommonListAdapter != null) {
                    mCommons.clear();
                    mCommonListAdapter.notifyDataSetChanged();
                }
                return true;
        }
        return false;
    }

    private void updateView(Common common) {
        if (common.Title())
            mAppContext.setAck(true);
        else
            mAppContext.setAck(false);
        this.mCommons.add(mCommonListAdapter.getCount(), common);
        this.mCommonListAdapter.notifyDataSetChanged();

        if (mCommonListAdapter.getCount() > 300) {
            mCommons.clear();
            mCommonListAdapter.notifyDataSetChanged();
        }
    }

    private Handler mCustomizeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://[TX]
                    updateView(new Common(false,
                            ReaderService.Format.showCRLF(ReaderService.Format.bytesToString((byte[])msg.obj)),
                            mDateFormat.format(new Date())));
                    break;
                case 2:
                    updateView(new Common(true,
                            ReaderService.Format.showCRLF((String)msg.obj),
                            mDateFormat.format(new Date())));
                    final String str = ((String)msg.obj).substring(2, ((String)msg.obj).length());
                    break;
            }
            super.handleMessage(msg);
        }
    };

    StringBuilder _SubRaw = new StringBuilder(256);
    Byte _SubRawOld = 0;
    private Runnable mRunnableBackground = new Runnable() {


        @Override
        public void run() {

            new Thread(new Runnable() {

                int  _processIndex = 0;
                int _timeOut = 500;
                boolean _processEnd = false;
                HashMap<String, String> _item;
                byte[] _bsData;
                String _strData;

                @Override
                public void run() {
                    while(true) {

                        if (!((MainActivity) mContext).isConnected()) {
                            mActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    initCustomize();
                                }
                            });
                            return;
                        }

                        if (_processIndex == mProcessList.size())
                            return;

                        _timeOut = 500;
                        _processEnd = false;
                        //[TX]
                        _item = mProcessList.get(_processIndex);
                        ((MainActivity) mContext).sendData(ReaderService.Format.stringToBytes(_item.get(CUSTOMIZE_DATA)));
                        mCustomizeHandler.sendMessage(mCustomizeHandler.obtainMessage(1, ReaderService.Format.stringToBytes(_item.get(CUSTOMIZE_DATA))));

                        //[RX]
                        while(_timeOut > 1 && !_processEnd) {
                            try {
                                Thread.sleep(4);
                            } catch (InterruptedException e) {e.printStackTrace();}

                            if (((MainActivity) mContext).checkData() > 0) {
                                _bsData = ((MainActivity) mContext).getData();
                                if (_bsData != null) {
                                    _timeOut = 500;
                                    for (int i = 0; i < _bsData.length; i++) {
                                        if (_bsData[i] != 0) {
                                            _SubRaw.append((char) _bsData[i]);
                                            if (_bsData[i] == 0x0A) {
                                                if (_SubRawOld == 0x0D) {
                                                    _strData = _SubRaw.toString();
                                                    switch(mProcessList.get(_processIndex).get(CUSTOMIZE_COMMAND)) {
                                                        case "Multi":
                                                            Message msg = new Message();
                                                            msg.what = 2;
                                                            msg.obj = _SubRaw.toString();
                                                            //Log.d("UUU", Integer.toString(_timeOut) + (String) msg.obj);
                                                            if (msg.obj.equals(ReaderService.COMMANDU_END)) {
                                                                _processEnd = true;
                                                            }
                                                            mCustomizeHandler.sendMessage(msg);

                                                            _SubRaw.setLength(0);
                                                            break;
                                                        case "Single":
                                                            Message msg2 = new Message();
                                                            msg2.what = 2;
                                                            msg2.obj = _SubRaw.toString();
                                                            //Log.d("UUU", (String) msg2.obj);
                                                            _processEnd = true;
                                                            mCustomizeHandler.sendMessage(msg2);
                                                            _SubRaw.setLength(0);
                                                            break;
                                                    }
                                                }
                                            }
                                            _SubRawOld = _bsData[i];
                                        }
                                    }
                                }
                            }
                            _timeOut--;
                        }
                        _processIndex++;
                    }
                }
            }).start();
        }
    };

    private void initCustomize() {
        mCustomizeHandler.removeCallbacks(mRunnableBackground);
    }


}
