package com.favepc.reader.rfidreaderutility.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.favepc.reader.rfidreaderutility.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Bruce_Chiang on 2020/10/27.
 */
@SuppressLint("ValidFragment")
public class BarcodeFragment extends Fragment {

    private Context mContext;
    private Activity mActivity;
    private View mBarcodeView = null;

    private SurfaceView surfaceView;
    private TextView txtBarcodeValue;
    private ListView mListViewMessage;

    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private BarcodeDetector mBarcodeDetector;
    private CameraSource mCameraSource;
    private ArrayAdapter<String> mBarcodeListAdapter;
    private ArrayList<String> mBarcodes = new ArrayList<String>();
    private Vibrator mVibrator;


    public BarcodeFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public BarcodeFragment(Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.mActivity = getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (this.mBarcodeView == null) {
            this.mBarcodeView = inflater.inflate(R.layout.fragment_barcode, container, false);

            this.txtBarcodeValue = this.mBarcodeView.findViewById(R.id.barcode_txtValue);
            this.surfaceView = this.mBarcodeView.findViewById(R.id.surfaceView);

            this.mBarcodeListAdapter = new ArrayAdapter<String>(mContext, R.layout.adapter_demobarcode, mBarcodes);
            ListView lv = (ListView) this.mBarcodeView.findViewById(R.id.barcode_lvMsg);
            lv.setAdapter(this.mBarcodeListAdapter);
        }


        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new
                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }


        this.mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);

        return this.mBarcodeView;
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(mContext, "Barcode scanner started", Toast.LENGTH_SHORT).show();

        mBarcodeDetector = new BarcodeDetector.Builder(mContext)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        //Manages the camera in conjunction with an underlying detector.
        //Here SurfaceView is the underlying detector.
        mCameraSource = new CameraSource.Builder(mContext, mBarcodeDetector)
                .setRequestedPreviewSize(720, 960)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            //When, in the first instance, the surface is created, this method is called.
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //Opens the camera and starts sending preview frames to the SurfaceView.
                        mCameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(mActivity, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //This method is called when the size or the format of the surface changes.
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            //This is called when the surface is destroyed
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });


        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {
                Toast.makeText(mContext, "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            //Receives the QR Code from the camera preview and adds them in the SparseArray.
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> _barcodeArray = detections.getDetectedItems();

                if (_barcodeArray.size() != 0) {

                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {

                            if (Build.VERSION.SDK_INT >= 26) {
                                mVibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                mVibrator.vibrate(200);
                            }
                            txtBarcodeValue.setText(_barcodeArray.valueAt(0).displayValue);
                            updateView(_barcodeArray.valueAt(0).displayValue);
                        }
                    });

                }
            }
        });
    }


    private void updateView(String str) {

        this.mBarcodes.add(mBarcodeListAdapter.getCount(), str);
        this.mBarcodeListAdapter.notifyDataSetChanged();

        if (mBarcodeListAdapter.getCount() > 50) {
            mBarcodes.clear();
            mBarcodeListAdapter.notifyDataSetChanged();
        }
    }




    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (mCameraSource != null)
                mCameraSource.stop();
        } else {
            if (mCameraSource != null)
                try {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //Opens the camera and starts sending preview frames to the SurfaceView.
                        mCameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(mActivity, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
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
                if (mBarcodes != null && mBarcodeListAdapter != null) {
                    mBarcodes.clear();
                    mBarcodeListAdapter.notifyDataSetChanged();
                }
                return true;
        }
        return false;
    }
}
