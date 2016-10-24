package com.example.jakebo.honeywellircamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Interpolator;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "HoneywellIRCamera";
    private final int VIDEODISPLAY = -9999;
    private final int HOTTESTDISPLAY = -9998;

    private ImageView irVideo;
    private ImageView imageView;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView colorBarView;
    private TextView tempView;
    private TextView minTemperatureView;
    private TextView midTemperatureView;
    private TextView maxTemperatureView;
    private Bitmap bm;
    private Bitmap colorBarBM;
    private Object frameSyncToken = new Object();

    private Lepton lepton = new Lepton();
    private ColorMap colorMap = new ColorMap();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private short frameData[] = new short[4800];
    private int rgbData[] = new int[4800];
    private short colorBarRaw[] = new short[4800];
    private short maxMinValue[] = new short[2];
    private int colorType = ColorMap.COLOR_RAINBOW;

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case VIDEODISPLAY:
                    //synchronized (frameSyncToken) {
                        IRVideoDisplay();
                    //    frameSyncToken.notify();
                    //}
                    break;

                case HOTTESTDISPLAY:
                    HottestDisplay();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        irVideo = (ImageView) findViewById(R.id.irVideo);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        tempView = (TextView) findViewById(R.id.tempView);
        minTemperatureView = (TextView) findViewById(R.id.minTemperature);
        midTemperatureView = (TextView) findViewById(R.id.midTemperature);
        maxTemperatureView = (TextView) findViewById(R.id.maxTemperature);
        colorBarView = (ImageView) findViewById(R.id.colorBarView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "IRSensor is working.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorType = ColorMap.COLOR_GRAY;
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorType = ColorMap.COLOR_IRONBLACK;
            }
        });

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorType = ColorMap.COLOR_RAINBOW;
            }
        });

        lepton.LeptonInit();

        // Start frame thread
        Thread frameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    //synchronized (frameSyncToken) {
                        lepton.LeptonGetFrame(frameData, maxMinValue);
                        colorMap.SetMinMaxValue(maxMinValue[1], maxMinValue[0]);

                        Message message = new Message();
                        message.what = VIDEODISPLAY;
                        mHandler.sendMessage(message);
/*
                        try {
                            frameSyncToken.wait(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
*/                    //}
                }
            }
        });
        frameThread.start();

        // Start temperature thread
        Thread temperatureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = new Message();
                    message.what = HOTTESTDISPLAY;
                    mHandler.sendMessage(message);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        temperatureThread.start();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    private void IRVideoDisplay() {
        // View1
        colorMap.RawValueArrayToColorArray(frameData, rgbData, ColorMap.COLOR_GRAY);
        bm = Bitmap.createBitmap(rgbData, 0, 80, 80, 60, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(bm);

        if (colorType == ColorMap.COLOR_GRAY)
            irVideo.setBackground(new BitmapDrawable(getResources(), bm));

        // view2
        colorMap.RawValueArrayToColorArray(frameData, rgbData, ColorMap.COLOR_IRONBLACK);
        bm = Bitmap.createBitmap(rgbData, 0, 80, 80, 60, Bitmap.Config.ARGB_8888);
        imageView2.setImageBitmap(bm);

        if (colorType == ColorMap.COLOR_IRONBLACK)
            irVideo.setBackground(new BitmapDrawable(getResources(), bm));

        // view3
        colorMap.RawValueArrayToColorArray(frameData, rgbData, ColorMap.COLOR_RAINBOW);
        bm = Bitmap.createBitmap(rgbData, 0, 80, 80, 60, Bitmap.Config.ARGB_8888);
        imageView3.setImageBitmap(bm);

        if (colorType == ColorMap.COLOR_RAINBOW)
            irVideo.setBackground(new BitmapDrawable(getResources(), bm));

        // irVideo.setImageBitmap(bm);
    }

    private void HottestDisplay() {
        double temperature = (0.0217 * maxMinValue[0]) + lepton.LeptonGetCelcius() - 177.77;
        tempView.setText(String.format("Hottest: %.2f°C", temperature));

        DrawColorBar();
    }

    private void DrawColorBar() {
        ArrayList<Short> colorBar = new ArrayList<>();

        colorBarRaw = frameData.clone();
        Arrays.sort(colorBarRaw);

        colorBar.add(colorBarRaw[0]);
        for (int i = 0; i < colorBarRaw.length - 1; ++i) {
            if (colorBarRaw[i] != colorBarRaw[i + 1])
                colorBar.add(colorBarRaw[i + 1]);
        }

        SetColorBar(colorBar);
    }

    private void SetColorBar(ArrayList<Short> colorBar) {
        int size = colorBar.size();
        int high = 1;
        int tmpArray[] = new int[size * high];
        int maxValue = maxMinValue[0];
        int minValue = maxMinValue[1];

        /*
         * Display temperatures
         */
        double temperature;

        // min temperature
        temperature = lepton.LeptonGetTemperatureByRawValue(minValue);
        minTemperatureView.setText(String.format("%.2f°C", temperature));
        // mid temperature
        int midRawValue = colorBar.get(colorBar.size() / 2);
        temperature = lepton.LeptonGetTemperatureByRawValue(midRawValue);
        midTemperatureView.setText(String.format("%.2f°C", temperature));
        // max temperature
        temperature = lepton.LeptonGetTemperatureByRawValue(maxValue);
        maxTemperatureView.setText(String.format("%.2f°C", temperature));

        // Draw color bar
        colorMap.RawValueArrayToColorArray(colorBar, tmpArray, colorType);

        for (int j = 1; j < high; ++j)
            for (int i = 0; i < colorBar.size(); ++i)
                tmpArray[j * size + i] = tmpArray[i];

        colorBarBM = Bitmap.createBitmap(tmpArray, 0, colorBar.size(), colorBar.size(), high, Bitmap.Config.ARGB_8888);
        //colorBarView.setImageBitmap(colorBarBM);
        colorBarView.setBackground(new BitmapDrawable(getResources(), colorBarBM));
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jakebo.honeywellircamera/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.jakebo.honeywellircamera/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
