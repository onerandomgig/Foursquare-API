package in.continuousloop.winnie.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A wrapper view around {@link TextView} to support OpenSansRegular font
 */
public class OpenSansRegularTextView extends TextView {

    private Typeface mTypeFace;

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextView(Context context) {
        super(context);
        setFont(context);
    }

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont(context);
    }

    /**
     * {@inheritDoc}
     */
    public OpenSansRegularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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
