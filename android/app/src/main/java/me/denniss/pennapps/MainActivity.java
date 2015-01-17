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

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;


public class MainActivity extends Activity implements CvCameraViewListener2 {

    private Random rand = new Random();
    private MediaPlayer[] deflectSounds;


    private CameraBridgeViewBase cameraView;

    private Mat frameBuffer;
    private ColorDetector detector;
    private Socket io;

    private int screen_width;
    private int screen_height;

    private static final Scalar RECT_COLOR = new Scalar(0, 255, 0, 255);
    private static final String TAG = "penn-apps";



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

        cameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        cameraView.setCvCameraViewListener(this);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        deflectSounds = new MediaPlayer[]{
                MediaPlayer.create(this, R.raw.ping_pong_8bit_beeep),
                MediaPlayer.create(this, R.raw.ping_pong_8bit_peeeeeep),
                MediaPlayer.create(this, R.raw.ping_pong_8bit_plop)
        };

    }


    private int n = 0;

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



            double distance = Math.hypot(lCenter.x - rCenter.x, lCenter.y - rCenter.y);

            Log.i("DISTANCE", ""+distance);

            int THRESHOLD = (int)(screen_width * 0.4);
            if(screen_width - distance < THRESHOLD){

                int center_w = screen_width / 2;
                int center_h = screen_height / 2;
                int bsize = 100;

                // Draw center box
                Point p = new Point(center_w - bsize, center_h - bsize);
                Point q = new Point(center_w + bsize, center_h + bsize);
                Core.rectangle(frameBuffer, p, q, new Scalar(0, 0, 255, 255), 5);

                deflectSounds[rand.nextInt(3)].start();

                //io.deflect(0);

            }

/*
            float dx = (float) (center_w - r.tl().x);
            float dy = (float) (r.tl().y - center_h);

            n++;
            if(n % 10 == 0){
                io.send(sensors.getAngle(), myo.getAngle(), dx, dy);
            }
*/
        }

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