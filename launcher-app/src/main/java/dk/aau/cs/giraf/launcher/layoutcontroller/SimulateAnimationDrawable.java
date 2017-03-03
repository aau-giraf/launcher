package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.widget.ImageView;

/**
 * This class simulates the animation in AuthenticationActivity, scanning the QR code.
 * The animation itself is simply 96 pictures shown in sequence, which this class handles.
 */
public class SimulateAnimationDrawable {

    /**
     * the variables needed by the class.
     * The ImageView then animation plays in, the index of the frames,
     * the duration between each frame and the index of the last frame.
     */
    private final ImageView imageView;
    private final int[] frames;
    private final int duration;
    private final int lastFrameNo;

    /**
     * The constructor for the class
     *
     * @param imageView The ImageView we are simulating the class in.
     * @param frames    The index of the frames to be shown
     * @param duration  The duration that each frame should be shown.
     */
    public SimulateAnimationDrawable(ImageView imageView, int[] frames, int duration) {
        this.imageView = imageView;
        this.frames = frames;
        this.duration = duration;
        lastFrameNo = frames.length - 1;

        this.imageView.setImageResource(this.frames[0]);
        play(1);
    }

    /**
     * The play function simulates the animation by itself.
     *
     * @param frameNo The frame number to start with. This is usually called with 1 as argument.
     */
    private void play(final int frameNo) {
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(frames[frameNo]);

                if (frameNo == lastFrameNo) {
                    play(0);
                } else {
                    play(frameNo + 1);
                }
            }
        }, duration);
    }
}
