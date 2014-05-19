package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/**
 * This class was used for the colors in the drawer.
 * The colors needed to be draggable and the apps_container in HomeActivity needed to handle that
 * the apps_container in HomeActivity used to implement this class for that purpose.
 * However, because we have disabled the dragging of colors, it is no longer needed.
 * We have left the code intact, if a future group would wish or need to reimplement the feature.
 */
public class GAppDragger implements OnDragListener {

    /**
     * The variables used by the class.
     */
    private int newColor;
    private long currentViewId;


    /**
     * This is the main function we need to override to handle the dropping of colors on the apps_container.
     * When the entering and exiting the AppImageView, the AppImageView is scaled to provide visual feedback to the user.
     * When dropped the drag on the AppImageView, the AppImageView given the dragged color.
     * @param view The view, in this case the AppImageView, that we are handling
     * @param drawEvent The event that fired the function call
     * @return whether the event succeeded or not.
     */
	@Override
	public boolean onDrag(View view, DragEvent drawEvent) {
		switch(drawEvent.getAction()){
		case DragEvent.ACTION_DRAG_STARTED:
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
            view.setScaleX(1.0f);
            view.setScaleY(1.0f);
            break;
		case DragEvent.ACTION_DRAG_EXITED:

            view.setScaleX(0.9f);
            view.setScaleY(0.9f);
			break;

		case DragEvent.ACTION_DROP:
            currentViewId = Long.parseLong((String)view.getTag());
			newColor = Integer.parseInt(drawEvent.getClipData().getItemAt(0).getText().toString());
			AppAdapter.saveAppBackground(view.getContext(), view, newColor, currentViewId);
			break;

		case DragEvent.ACTION_DRAG_ENDED:
            view.setScaleX(0.9f);
            view.setScaleY(0.9f);
			break;
		
		}
		return true;
	}

}
