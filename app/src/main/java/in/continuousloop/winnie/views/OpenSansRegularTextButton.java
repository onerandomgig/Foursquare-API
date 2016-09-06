package in.continuousloop.winnie.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * A simple wrapper view around {@link Button} to support OpenSansRegular font
 */
public class OpenSansRegularTextButton extends Button {

    private Typeface mTypeFace;

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextButton(Context context) {
        super(context);
        setFont(context);
    }

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context);
    }

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont(context);
    }

    /**
     * Set the OpenSansRegular font
     * @param context - Application context
     */
    private void setFont(Context context) {
        mTypeFace = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/opensans-regular.ttf");
        setTypeface(mTypeFace);
    }
}
