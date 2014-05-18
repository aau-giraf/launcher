package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.widget.ImageView;

/**
 * This class simulates the animation in AuthenticationActivity, scanning the QR code.
 * The animation itself is simply 96 pictures shown in sequence, which this class handles.
 */
public class SimulateAnimationDrawable{
    
    /**
     * the variables needed by the class.
     * The ImageView then animation plays in, the index of the frames, the duration between each frame and the index of the last frame.
     */
    private ImageView mImageView;
    private int[] mFrames;
    private int mDuration;
    private int mLastFrameNo;

    /**
     * The constructor for the class
     * @param pImageView The ImageView we are simulating the class in.
     * @param pFrames The index of the frames to be shown
     * @param pDuration The duration that each frame should be shown.
     */
    public SimulateAnimationDrawable(ImageView pImageView, int[] pFrames, int pDuration){
        mImageView = pImageView;
        mFrames = pFrames;
        mDuration = pDuration;
        mLastFrameNo = pFrames.length - 1;

        mImageView.setImageResource(mFrames[0]);
        play(1);
    }

    /**
     * The play function simulates the animation by itself.
     * @param pFrameNo The frame number to start with. This is usually called with 1 as argument.
     */
    private void play(final int pFrameNo){
        mImageView.postDelayed(new Runnable(){
            @Override
			public void run() {                    
                mImageView.setImageResource(mFrames[pFrameNo]);

                if(pFrameNo == mLastFrameNo)
                    play(0);
                else
                    play(pFrameNo + 1);
            }
        }, mDuration);
    }        
};
