package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import com.kunzisoft.androidclearchroma.ChromaUtil;

public class CustomColorDialogPreference extends DialogPreference {

    CustomColorDialogPreferenceFragment fragment;

    final int chromaColorMode;
    final int chromaIndicatorMode;

    private AppCompatImageView backgroundPreview;
    private AppCompatImageView colorPreview;

    // Custom xml attributes.
    int value;

    private int defaultValue;
    private boolean savedInstanceState;

    private final Context prefContext;

    public CustomColorDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        prefContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.ChromaPreference);

        chromaColorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaColorMode, 1);
        chromaIndicatorMode = typedArray.getInteger(
                R.styleable.ChromaPreference_chromaIndicatorMode, 1);

        setWidgetLayoutResource(R.layout.preference_widget_custom_color_preference); // resource na layout custom preference - TextView-ImageView

        typedArray.recycle();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
    {
        super.onBindViewHolder(holder);

        backgroundPreview = (AppCompatImageView)holder.findViewById(R.id.dialog_color_chooser_pref_background_preview);
        colorPreview = (AppCompatImageView)holder.findViewById(R.id.dialog_color_chooser_pref_color_preview);

        setColorInWidget();
    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap, int widthLight, int heightLight, float radius) {
        Bitmap output = Bitmap.createBitmap(widthLight, heightLight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, radius, radius, paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        //noinspection IntegerDivisionInFloatingPointContext
        canvas.drawBitmap(bitmap, -(bitmap.getWidth() - widthLight)/2 , -(bitmap.getHeight() - heightLight)/2, paintImage);

        return output;
    }

    private void setColorInWidget() {

        int color;
        if (fragment != null)
            color = fragment.chromaColorView.getCurrentColor();
        else
            color = value;

        try {
            if (colorPreview != null) {
                int shapeWidth = getContext().getResources()
                        .getDimensionPixelSize(R.dimen.acch_shape_preference_width);
                float radius = shapeWidth / 2.0f;

                colorPreview.setImageResource(R.drawable.acch_circle);

                // Update color
                String applicationTheme = ApplicationPreferences.applicationTheme(prefContext, true);
                boolean nightModeOn = !applicationTheme.equals("white");
                //if (GlobalGUIRoutines.isNightModeEnabled(prefContext.getApplicationContext()))
                if (nightModeOn)
                    colorPreview.getDrawable()
                            .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.ADD));
                else
                    colorPreview.getDrawable()
                            .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                /*int nightModeFlags =
                        prefContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        colorPreview.getDrawable()
                                .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.ADD));
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        colorPreview.getDrawable()
                                .setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                        break;
                }*/

                // Bitmap to crop for background
                Bitmap draughtboard = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.acch_draughtboard);
                //noinspection SuspiciousNameCombination
                draughtboard = getRoundedCroppedBitmap(draughtboard, shapeWidth, shapeWidth, radius);
                backgroundPreview.setImageBitmap(draughtboard);

                colorPreview.invalidate();
                backgroundPreview.invalidate();
            }
            //setSummary(summaryPreference);
        } catch (Exception e) {
            //Log.e("CustomColorDialogPreference.setColorInWidget", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray ta, int index)
    {
        super.onGetDefaultValue(ta, index);
        return Color.parseColor(ta.getString(index));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue)
    {
        // Get the persistent value and correct it for the minimum value.
        if (defaultValue != null) {
            value = getPersistedInt((int) defaultValue);
            this.defaultValue = (int)defaultValue;
        }
        else {
            value = getPersistedInt(0xFFFFFFFF);
            this.defaultValue = 0xFFFFFFFF;
        }

        setColorInWidget();
        setSummaryCCDP(value);
    }

    void persistValue() {
        if (shouldPersist()) {
            persistInt(value);
            setColorInWidget();
            setSummaryCCDP(value);
        }
    }

    void resetSummary() {
        if (!savedInstanceState) {
            value = getPersistedInt(defaultValue);
            setSummaryCCDP(value);
        }
        savedInstanceState = false;
    }

    private void setSummaryCCDP(int value)
    {
        setSummary(ChromaUtil.getFormattedColorString(value, false));
    }

    @Override
    protected Parcelable onSaveInstanceState()
    {
        savedInstanceState = true;

        final Parcelable superState = super.onSaveInstanceState();
        /*if (isPersistent()) {
            // save is not needed, is already saved persistent
            return superState;
        }*/

        final CustomColorDialogPreference.SavedState myState = new CustomColorDialogPreference.SavedState(superState);
        if (fragment != null) {
            myState.value = fragment.chromaColorView.getCurrentColor();
        }
        else {
            myState.value = value;
        }
        myState.defaultValue = defaultValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state)
    {
        if ((state == null) || (!state.getClass().equals(SavedState.class))) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            int value = this.value;
            if (fragment != null)
                fragment.chromaColorView.setCurrentColor(value);
            setSummaryCCDP(value);
            return;
        }

        // restore instance state
        CustomColorDialogPreference.SavedState myState = (CustomColorDialogPreference.SavedState)state;
        super.onRestoreInstanceState(myState.getSuperState());
        value = myState.value;
        defaultValue = myState.defaultValue;

        if (fragment != null)
            fragment.chromaColorView.setCurrentColor(value);
        setSummaryCCDP(value);
    }

    // SavedState class
    private static class SavedState extends BaseSavedState
    {
        int value;
        int defaultValue;

        SavedState(Parcel source)
        {
            super(source);

            value = source.readInt();
            defaultValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            super.writeToParcel(dest, flags);

            dest.writeInt(value);
            dest.writeInt(defaultValue);
        }

        SavedState(Parcelable superState)
        {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<CustomColorDialogPreference.SavedState>() {
                    public CustomColorDialogPreference.SavedState createFromParcel(Parcel in)
                    {
                        return new CustomColorDialogPreference.SavedState(in);
                    }
                    public CustomColorDialogPreference.SavedState[] newArray(int size)
                    {
                        return new CustomColorDialogPreference.SavedState[size];
                    }

                };

    }

}
