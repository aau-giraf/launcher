package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.widget.ImageView;

public class SimulateAnimationDrawable{
    private ImageView mImageView;
    private int[] mFrames;
    private int mDuration;
    private int mLastFrameNo;

    public SimulateAnimationDrawable(ImageView pImageView, int[] pFrames, int pDuration){
        mImageView = pImageView;
        mFrames = pFrames;
        mDuration = pDuration;
        mLastFrameNo = pFrames.length - 1;

        mImageView.setImageResource(mFrames[0]);
        play(1);
    }

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
