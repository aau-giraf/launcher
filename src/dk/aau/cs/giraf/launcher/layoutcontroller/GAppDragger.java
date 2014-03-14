package dk.aau.cs.giraf.launcher.layoutcontroller;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

public class GAppDragger implements OnDragListener {

    private int newColor;
    private long currentViewId;


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
