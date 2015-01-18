package me.denniss.pennapps;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.zerokol.views.JoystickView;

import java.util.Random;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private Random rand = new Random();
    private MediaPlayer[] deflectSounds;
    private MainActivity selfActivity;

    private CameraBridgeViewBase cameraView;

    private Mat frameBuffer;
    private ColorDetector detector;
    private Socket io;
    private JoystickView joystick;
    private int screen_width;
    private int screen_height;
    private int joystickAngle = 0;

    private static final Scalar RECT_COLOR = new Scalar(255, 0, 0, 255);
    private static final String TAG = "penn-apps";
    private Button startButton,stopButton,reconnect,recalibrate;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        io = new Socket(this);

        selfActivity = this;
        cameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        cameraView.setCvCameraViewListener(this);

        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        reconnect = (Button)findViewById(R.id.reconnectButton);
        recalibrate = (Button)findViewById(R.id.recalibrate);
        joystick = (JoystickView)findViewById(R.id.joystickView);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                io.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                io.stop();
            }
        });

        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                io = new Socket(selfActivity);
            }
        });

        recalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recalalibrate();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        deflectSounds = new MediaPlayer[]{
                MediaPlayer.create(this, R.raw.ping_pong_8bit_beeep),
                MediaPlayer.create(this, R.raw.ping_pong_8bit_peeeeeep),
                MediaPlayer.create(this, R.raw.ping_pong_8bit_plop)
        };


        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int i, int i2, int i3) {
                joystickAngle = i;
               // Log.e("angle", i + "");

            }
        },JoystickView.DEFAULT_LOOP_INTERVAL);

    }


    private int n = 0;

    public void recalalibrate()
    {
        Rect rec = new Rect();
        rec.height = 20;
        rec.width = 20;
        rec.x = frameBuffer.width()/2 - rec.height/2;
        rec.y = frameBuffer.height()/2 - rec.width/2;

        Mat touchedRegionRgba = frameBuffer.submat(rec);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        Scalar mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = rec.width*rec.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        Toast.makeText(this,"Color: " + mBlobColorHsv.val[0]+ "  "+ mBlobColorHsv.val[1] + "  " + mBlobColorHsv.val[2],
                Toast.LENGTH_SHORT).show();
        detector.setHsvColor(mBlobColorHsv);
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        frameBuffer = inputFrame.rgba();

        frameBuffer.cols();

        detector.process(frameBuffer);
//        Rect r = detector.getBoundingBox();

/*
        int center_w = screen_width / 2;
        int center_h = screen_height / 2;
        int bsize = 100;

        // Draw center box
        Point p = new Point(center_w - bsize, center_h - bsize);
        Point q = new Point(center_w + bsize, center_h + bsize);
        Core.rectangle(frameBuffer, p, q, new Scalar(0, 0, 255, 255), 5);
*/

        int center_w = screen_width / 2;
        int center_h = screen_height / 2;


        if(detector.getContours().size() >= 2){

            Rect rectL = detector.getBoundingBox(0);
            Rect rectR = detector.getBoundingBox(1);

            if(rectL.tl().x > rectR.tl().x){
                Rect temp = rectL;
                rectL = rectR;
                rectR = temp;
            }


            Core.rectangle(frameBuffer, rectL.tl(), rectL.br(), RECT_COLOR, 3);
            Core.rectangle(frameBuffer, rectR.tl(), rectR.br(), RECT_COLOR, 3);

            Point lCenter = new Point(rectL.tl().x + (rectL.width / 2), rectL.tl().y + (rectL.height / 2));
            Point rCenter = new Point(rectR.tl().x + (rectR.width / 2), rectR.tl().y + (rectR.height / 2));



            double  distance = Math.hypot(lCenter.x - rCenter.x, lCenter.y - rCenter.y);

            Log.i("DISTANCE", ""+distance);

            double THRESHOLD = 0.2;
            double progress = (distance / screen_width) / THRESHOLD;
    

            if(progress >= 1.0){

                int bsize = 100;

                // Draw center box
                Point p = new Point(center_w - bsize, center_h - bsize);
                Point q = new Point(center_w + bsize, center_h + bsize);
                Core.rectangle(frameBuffer, p, q, new Scalar(0, 0, 255, 255), 5);

               if(joystickAngle < -10)
               {
                   joystickAngle = -10;
               }
               else if(joystickAngle > 10)
               {
                   joystickAngle = 10;
               }

                io.deflect(joystickAngle);
                deflectSounds[rand.nextInt(3)].start();

            }


        }

        Core.circle(frameBuffer, new Point(center_w, center_h), 10, new Scalar(255, 0, 255, 255), 5);

        return frameBuffer;
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();

        //sensors.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);

        //sensors.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frameBuffer = new Mat(height, width, CvType.CV_8UC4);

        screen_width = width;
        screen_height = height;

        detector = new ColorDetector();

    }

    @Override
    public void onCameraViewStopped() {
        frameBuffer.release();
    }
}