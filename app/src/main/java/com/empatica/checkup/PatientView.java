package com.empatica.checkup;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class PatientView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public File dirFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit((mSectionsPagerAdapter.getCount() > 1 ? mSectionsPagerAdapter.getCount() - 1 : 1));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //endregion

    }

    public void saveSession(int n, float val){
        String folder_main = "CheckUp";

        File dir = new File(Environment.getExternalStorageDirectory(), folder_main);

        if (!dir.exists()) {
            dir.mkdir();
        }

        File myFile = new File(dir, "Session1.xls");
        if (dir.list().length>0 && n!=0) {
            myFile = dirFile;
        }

        if (n==0) {
//            FileInputStream fin = null;
            FileOutputStream fos = null;
            if (dir.list().length==0) {
                myFile = new File(dir, "Session1.xls");
            }else if (dir.list().length!=0){
                myFile = new File(dir, "Session" + String.valueOf(dir.list().length+1) + ".xls");
            }

//            try {
////                fin = new FileInputStream(myFile);
//            }catch(IOException e){
//                System.out.println("fileinput"+e);
//            }
            dirFile = myFile;

            try{
                Workbook wb = new HSSFWorkbook();

                Sheet bvp_sheet = wb.createSheet("BVP");
                Row bvp_row = bvp_sheet.createRow(0);
                Cell bvp_cell = bvp_row.createCell(0);

                Sheet eda_sheet = wb.createSheet("EDA");
                Row eda_row = eda_sheet.createRow(0);
                Cell eda_cell = eda_row.createCell(0);

                Sheet hr_sheet = wb.createSheet("Heart Rate");
                Row hr_row = hr_sheet.createRow(0);
                Cell hr_cell = hr_row.createCell(0);

                Sheet ibi_sheet = wb.createSheet("IBI");
                Row ibi_row = ibi_sheet.createRow(0);
                Cell ibi_cell = ibi_row.createCell(0);
//                fin.close();
                fos = new FileOutputStream(myFile);
                wb.write(fos);
                fos.close();
            }catch (IOException e){
                System.out.println("wb io" + e);
//            }catch (InvalidFormatException i){
//                System.out.println("wb invformat"+i);
            }finally{
//                org.apache.commons.io.IOUtils.closeQuietly(fin);
                org.apache.commons.io.IOUtils.closeQuietly(fos);
            }

        }
        if (n!=0) {
            FileInputStream fin = null;
            FileOutputStream fos = null;
            try {
                fin=new FileInputStream(myFile);
                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(fin);

                // Create a workbook using the File System
                HSSFWorkbook wb = new HSSFWorkbook(myFileSystem);
                if(n==1){
                    int v=0;
                    Sheet sheet = wb.getSheetAt(0);
                    if((sheet.getPhysicalNumberOfRows()%65536==0)&&(sheet.getPhysicalNumberOfRows()!=0)){
                        v=sheet.getPhysicalNumberOfRows()/65536;
                    }
                    if (sheet.getPhysicalNumberOfRows()==0){
                        Row row = sheet.createRow(0);
                        Cell cell = row.createCell(0)
                                ;
                        cell.setCellValue(val);
                    }else{
                        Row row = sheet.createRow(sheet.getLastRowNum()+1);
                        Cell cell = row.createCell(v);
                        cell.setCellValue(val);
                    }

                }
                if(n==2){
                    int v=0;
                    Sheet sheet = wb.getSheetAt(1);
                    if((sheet.getPhysicalNumberOfRows()%65536==0)&&(sheet.getPhysicalNumberOfRows()!=0)){
                        v=sheet.getPhysicalNumberOfRows()/65536;
                    }
                    if (sheet.getPhysicalNumberOfRows()==0){
                        Row row = sheet.createRow(0);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(val);
                    }else{
                        Row row = sheet.createRow(sheet.getLastRowNum()+1);
                        Cell cell = row.createCell(v);
                        cell.setCellValue(val);
                    }
                }
                if(n==3){
                    int v=0;
                    Sheet sheet = wb.getSheetAt(2);
                    if((sheet.getPhysicalNumberOfRows()%65536==0)&&(sheet.getPhysicalNumberOfRows()!=0)){
                        v=sheet.getPhysicalNumberOfRows()/65536;
                    }
                    if (sheet.getPhysicalNumberOfRows()==0){
                        Row row = sheet.createRow(0);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(val);
                    }else{
                        Row row = sheet.createRow(sheet.getLastRowNum()+1);
                        Cell cell = row.createCell(v);
                        cell.setCellValue(val);
                    }
                }
                if(n==4){
                    int v=0;
                    Sheet sheet = wb.getSheetAt(3);
                    if((sheet.getPhysicalNumberOfRows()%65536==0)&&(sheet.getPhysicalNumberOfRows()!=0)){
                        v=sheet.getPhysicalNumberOfRows()/65536;
                    }
                    if (sheet.getPhysicalNumberOfRows()==0){
                        Row row = sheet.createRow(0);
                        Cell cell = row.createCell(0);
                        cell.setCellValue(val);
                    }else{
                        Row row = sheet.createRow(sheet.getLastRowNum()+1);
                        Cell cell = row.createCell(v);
                        cell.setCellValue(val);
                    }
                }
                fin.close();
                fos = new FileOutputStream(myFile);
                wb.write(fos);
                fos.close();

            }catch (IOException e){
                System.out.println(e);
//            }catch (InvalidFormatException i){
//                System.out.println(i);
            }finally {
                org.apache.commons.io.IOUtils.closeQuietly(fin);
                org.apache.commons.io.IOUtils.closeQuietly(fos);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.past_sessions) {
            startActivity (new Intent (this, PastSessions.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
            implements EmpaDataDelegate, EmpaStatusDelegate {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static final int REQUEST_ENABLE_BT = 1;
        private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

        private static final long STREAMING_TIME = 100000; // Stops streaming 10 seconds after connection

        private static final String EMPATICA_API_KEY = "1482f602113740c0aac7310e724e3a92"; // TODO insert your API Key here

        private EmpaDeviceManager deviceManager = null;

        private TextView accel_xLabel;
        private TextView accel_yLabel;
        private TextView accel_zLabel;
        private TextView bvpLabel;
        private TextView edaLabel;
        private TextView ibiLabel;
        private TextView temperatureLabel;
        private TextView batteryLabel;
        private TextView statusLabel;
        private TextView deviceNameLabel;
        private TextView bvpfilter;
        private TextView ibifiltered;
        private TextView HRData;
        private RelativeLayout dataCnt;
        private float hrData =0;
        private float ibiData=0;
        private int count = 0;
        private int ibi_counter = 0;
        private float bvp_total = 0;
        private float bvp_filtered = 0;
        private float[] filtered_array = new float[20];
        private double[] filteredtimestamp_array = new double[20];
        private float calculated_ibi = 0;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            return fragment;
        }
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission was granted, yay!
                        initEmpaticaDeviceManager();
                    } else {
                        // Permission denied, boo!
                        final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission required")
                                .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // try again
                                        if (needRationale) {
                                            // the "never ask again" flash is not set, try again with permission request
                                            initEmpaticaDeviceManager();
                                        } else {
                                            // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    }
                                })
                                .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // without permission exit is the only way
                                        getActivity().finish();
                                    }
                                })
                                .show();
                    }
                    break;
            }
        }

        private void initEmpaticaDeviceManager() {
            // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
            } else {
                // Create a new EmpaDeviceManager. PatientView is both its data and status delegate.
                deviceManager = new EmpaDeviceManager(getActivity().getApplicationContext(), this, this);

                if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Warning")
                            .setMessage("Please insert your API KEY")
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    getActivity().finish();
                                }
                            })
                            .show();
                    return;
                }
                // Initialize the Device Manager using your API key. You need to have Internet access at this point.
                deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (deviceManager != null) {
                deviceManager.stopScanning();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (deviceManager != null) {
                deviceManager.cleanUp();
            }
        }

        @Override
        public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
            // Check if the discovered device can be used with your API key. If allowed is always false,
            // the device is not linked with your API key. Please check your developer area at
            // https://www.empatica.com/connect/developer.php
            if (allowed) {
                // Stop scanning. The first allowed device will do.
                deviceManager.stopScanning();
                try {
                    // Connect to the device
                    deviceManager.connectDevice(bluetoothDevice);
                    updateLabel(deviceNameLabel, "To: " + deviceName);
                } catch (ConnectionNotAllowedException e) {
                    // This should happen only if you try to connect when allowed == false.
                    Toast.makeText(getActivity(), "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void didRequestEnableBluetooth() {
            // Request the user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // The user chose not to enable Bluetooth
            if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
                // You should deal with this
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
            // No need to implement this right now
        }

        @Override
        public void didUpdateStatus(EmpaStatus status) {
            // Update the UI
            updateLabel(statusLabel, status.name());

            // The device manager is ready for use
            if (status == EmpaStatus.READY) {
                updateLabel(statusLabel, status.name() + " - Turn on your device");
                // Start scanning
                deviceManager.startScanning();
                // The device manager has established a connection
            } else if (status == EmpaStatus.CONNECTED) {
                // Stop streaming after STREAMING_TIME
                ((PatientView)getActivity()).saveSession(0,0);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dataCnt.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Disconnect device
                                deviceManager.disconnect();
                            }
                        }, STREAMING_TIME);
                    }
                });
                // The device manager disconnected from a device
            } else if (status == EmpaStatus.DISCONNECTED) {
                updateLabel(deviceNameLabel, "");
            }
        }

        @Override
        public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
            updateLabel(accel_xLabel, "" + x);
            updateLabel(accel_yLabel, "" + y);
            updateLabel(accel_zLabel, "" + z);
        }

        @Override
        public void didReceiveBVP(float bvp, double timestamp) {
            updateLabel(bvpLabel, "" + bvp);
            filteredBVP(bvp,timestamp);

            ((PatientView)getActivity()).saveSession(1,bvp);
        }

        public void filteredBVP(float bvp, double timestamp) {
            if (count < 10) {
                bvp_total = bvp + bvp_total;
                count+=1;
            } else {
                bvp_filtered = (bvp_total/10);
                double timestamp2 = System.currentTimeMillis() / 1000;
                filteredIBI(bvp_filtered, timestamp2);
                updateLabel(bvpfilter, "" + bvp_filtered);

                count = 0;
            }
        }

        public void filteredIBI(float bvp, double timestamp) {
            filtered_array[ibi_counter] = bvp;
            filteredtimestamp_array[ibi_counter] = timestamp;
            double temp_ibi = 0;
            int indexMax1 = 0;   float max1=0;
            int indexMax2 = 0;   float max2=0;
            if (ibi_counter >= 19) {
                //for case 1 and 2, where the array end maxes
                for(int i = 0; i <=filtered_array.length-1;i++) {
                    if(filtered_array[i] > max1) {
                        max1 = filtered_array[i];
                        indexMax1 = i;
                    }
                }
                //case 1
                if(indexMax1 == 0)
                {
                    int visited_1 =0;
                    for(int j=1; j <= filtered_array.length-1; j++) {
                        if(j!=filtered_array.length-1) {
                            for(int k=1; k < filtered_array.length-2; k++){
                                if((filtered_array[k] > filtered_array[k-1]) && (filtered_array[k] > filtered_array[k+1])) {
                                    max2 = filtered_array[k];
                                    indexMax2 = k;
                                    visited_1 ++;
                                    break;
                                }
                            }
                        }
                        if(visited_1 > 0)
                            break;
                        if (j == filtered_array.length-1) {
                            visited_1 = 0;
                            max2 = filtered_array[j];
                            indexMax2 = j;
                            for(int k=1; k <= filtered_array.length-2; k++)
                            {
                                if(filtered_array[k] > filtered_array[j]) {
                                    max2 = filtered_array[k];
                                    indexMax2 = k;
                                    visited_1 ++;
                                    break;
                                }
                            }
                        }
                        if(visited_1 == 0){
                            break;
                        }
                    }

                }
                //case 2
                if(indexMax1 == (filtered_array.length-1))
                {
                    int visited_2 = 0;
                    for(int j=0; j <= filtered_array.length-2; j++)
                    {
                        if(j == 0) {
                            indexMax2 = j;
                            max2 = filtered_array[j];
                            for(int k=1; k <= filtered_array.length-2; k++)
                            {
                                if(filtered_array[k] > filtered_array[j]) {
                                    max2 = filtered_array[j];
                                    indexMax2 = j;
                                    visited_2 ++;
                                    break;
                                }
                            }
                        }
                        if(visited_2 == 0)
                            break;
                        if(j>0)
                        {
                            visited_2 = 0;
                            for(int k=1; k <= filtered_array.length-2; k++){
                                if((filtered_array[k] > filtered_array[k-1]) && (filtered_array[k] > filtered_array[k+1])) {
                                    max2 = filtered_array[k];
                                    indexMax2 = k;
                                    visited_2 ++;
                                    break;
                                }
                            }
                        }
                        if(visited_2 > 0) {
                            break;
                        }
                    }
                }
                //case 3
                if(indexMax1 != 0 && indexMax1 != filtered_array.length-1) {
                    int visited = 0;
                    for (int k=0; k <= filtered_array.length-1;k++) {
                        if (k==0) {
                            max2 = filtered_array[k];
                            indexMax2 = k;
                            for(int l=1; l <= filtered_array.length-1; l++)
                            {
                                if(filtered_array[l] > filtered_array[k]) {
                                    max2 = filtered_array[l];
                                    indexMax2 = l;
                                    visited++;
                                    break;
                                }
                            }
                        }
                        if(visited == 0)
                            break;
                        if(k>0)
                        {
                            visited = 0;
                            for(int l=1; l < filtered_array.length-2; l++){
                                if(l != indexMax1){
                                    if((filtered_array[l] > filtered_array[l-1]) && (filtered_array[l] > filtered_array[l+1])) {
                                        max2 = filtered_array[l];
                                        indexMax2 = l;
                                        visited ++;
                                        break;
                                    }
                                }
                            }
                        }
                        if(visited > 0)
                            break;
                        if (k==filtered_array.length-1) {
                            visited = 0;
                            for(int l=0; l < filtered_array.length-2; l++)
                            {
                                max2 = filtered_array[k];
                                indexMax2 = k;
                                if(filtered_array[l] > filtered_array[k]) {
                                    max2 = filtered_array[l];
                                    indexMax2 = l;
                                    visited ++;
                                    break;
                                }
                            }
                        }
                        if(visited == 0) {
                            break;
                        }

                    }
                }
                if (indexMax1 > indexMax2) {
                    temp_ibi = filteredtimestamp_array[indexMax1] - filteredtimestamp_array[indexMax2];
                    calculated_ibi = (float)temp_ibi;
                    updateLabel(ibifiltered, "" + String.format("%.2f", calculated_ibi));
                } else {
                    temp_ibi = filteredtimestamp_array[indexMax2] - filteredtimestamp_array[indexMax1];
                    calculated_ibi = (float)temp_ibi;
                /*if(calculated_ibi > 1.2 || calculated_ibi < 0.6) {
                    double random = Math.random() * 1.1 + 0.7;
                    calculated_ibi = (float)random;
                }*/
                    updateLabel(ibifiltered, "" + String.format("%.2f", calculated_ibi));
                }
                ibi_counter = 0;
            }
            ibi_counter ++;
        }

        @Override
        public void didReceiveBatteryLevel(float battery, double timestamp) {
            updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
        }

        @Override
        public void didReceiveGSR(float gsr, double timestamp) {
            updateLabel(edaLabel, "" + gsr);
            ((PatientView)getActivity()).saveSession(2,gsr);
        }

        @Override
        public void didReceiveIBI(float ibi, double timestamp) {
            updateLabel(ibiLabel, "" + ibi);
            ((PatientView)getActivity()).saveSession(4,ibi);
            hrData = ((1/ibi)*60);
            ((PatientView)getActivity()).saveSession(3,hrData);

        }

        @Override
        public void didReceiveTemperature(float temp, double timestamp) {
            updateLabel(temperatureLabel, "" + temp);
        }

        // Update a label with some text, making sure this is run in the UI thread
        private void updateLabel(final TextView label, final String text) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    label.setText(text);
                }
            });
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.content_main, container, false);
            // Initialize vars that reference UI components
            statusLabel = (TextView) rootView.findViewById(R.id.status);
            dataCnt = (RelativeLayout) rootView.findViewById(R.id.dataArea);
            accel_xLabel = (TextView) rootView.findViewById(R.id.accel_x);
            accel_yLabel = (TextView) rootView.findViewById(R.id.accel_y);
            accel_zLabel = (TextView) rootView.findViewById(R.id.accel_z);
            bvpLabel = (TextView) rootView.findViewById(R.id.bvp);
            bvpfilter = (TextView) rootView.findViewById(R.id.bvp_filtered);
            ibifiltered = (TextView) rootView.findViewById(R.id.ibi_filtered);
            HRData = (TextView) rootView.findViewById(R.id.HRV);
            edaLabel = (TextView) rootView.findViewById(R.id.eda);
            ibiLabel = (TextView) rootView.findViewById(R.id.ibi);
            temperatureLabel = (TextView) rootView.findViewById(R.id.temperature);
            batteryLabel = (TextView) rootView.findViewById(R.id.battery);
            deviceNameLabel = (TextView) rootView.findViewById(R.id.deviceName);

            initEmpaticaDeviceManager();
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class HeartRate extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        ;

        public HeartRate() {


        }
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static HeartRate newInstance(int sectionNumber) {
            HeartRate fragment = new HeartRate();
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.hr, container, false);

            // Locate the button in activity_main.xml
            Button hr_button = (Button) rootView.findViewById(R.id.hr_button);
            // Capture button clicks
            hr_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    // Start NewActivity.class
                    Intent intent = new Intent(getActivity(), HeartRateMonitor.class);
                    startActivity(intent);
                }
            });

            return rootView;

        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);


        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = HeartRate.newInstance(position + 1);
                    break;
                case 1:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;
                default:
                    fragment = PlaceholderFragment.newInstance(position + 1);
                    break;

            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HR";
                case 1:
                    return "DATA";
            }
            return null;
        }
    }
}
