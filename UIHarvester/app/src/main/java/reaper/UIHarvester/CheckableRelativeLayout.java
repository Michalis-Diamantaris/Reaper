/*
 * UIHarvester
 * https://github.com/Michalis-Diamantaris/Reaper
 * Michalis Diamantaris, diamant@ics.forth.gr
 */
package reaper.UIHarvester;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;


public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private boolean isChecked = false;

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        changeColor(isChecked);
    }

    public void toggle() {
        this.isChecked = !this.isChecked;
        changeColor(this.isChecked);
    }

    private void changeColor(boolean isChecked){
        if (isChecked) {
            setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }
}