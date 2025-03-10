package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

class RunApplicationsDialogPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    final DragHandle dragHandle;
    private final ImageView imageViewIcon;
    private final TextView textViewAppName;
    //private final TextView textViewAppType;
    private final AppCompatImageButton imageViewMenu;
    private final TextView textViewStartApplicationDelay;

    private Application application;

    private final Context context;
    private final RunApplicationsDialogPreference preference;

    RunApplicationsDialogPreferenceViewHolder(View itemView, Context context, RunApplicationsDialogPreference preference)
    {
        super(itemView);

        this.context = context;
        this.preference = preference;

        dragHandle = itemView.findViewById(R.id.run_applications_pref_dlg_item_drag_handle);
        imageViewIcon = itemView.findViewById(R.id.run_applications_pref_dlg_item_icon);
        textViewAppName = itemView.findViewById(R.id.run_applications_pref_dlg_item_app_name);
        //textViewAppType = itemView.findViewById(R.id.run_applications_pref_dlg_item_app_type);
        imageViewMenu = itemView.findViewById(R.id.run_applications_pref_dlg_item_edit_menu);
        textViewStartApplicationDelay = itemView.findViewById(R.id.run_applications_pref_dlg_item_startApplicationDelay);

        itemView.setOnClickListener(this);
    }

    void bindApplication(Application application) {

        // 4. Bind the data to the ViewHolder
        this.application = application;

        if (this.application.type != Application.TYPE_INTENT) {
            if (PPApplication.getApplicationsCache() != null)
                imageViewIcon.setImageBitmap(PPApplication.getApplicationsCache().getApplicationIcon(application, false));
        }
        else
            imageViewIcon.setImageResource(R.drawable.ic_profile_pref_run_application);
        String text = "(A) " + application.appLabel;
        if (application.shortcutId > 0) {
            Shortcut shortcut = DatabaseHandler.getInstance(context.getApplicationContext()).getShortcut(application.shortcutId);
            if (shortcut != null)
                text = "(S) " + shortcut._name;
        }
        else
        if (application.intentId > 0) {
            PPIntent intent = DatabaseHandler.getInstance(context.getApplicationContext()).getIntent(application.intentId);
            if (intent != null)
                text = "(I) " + intent._name;
        }
        textViewAppName.setText(text);
        boolean errorColor = false;
        if ((application.type == Application.TYPE_SHORTCUT) && (application.shortcutId == 0))
            errorColor = true;
        if ((application.type == Application.TYPE_INTENT) && (application.intentId == 0))
            errorColor = true;
        setTextStyle(textViewAppName, errorColor);

        text = context.getString(R.string.applications_editor_dialog_startApplicationDelay);
        text = text + " " + StringFormatUtils.getDurationString(application.startApplicationDelay);
        textViewStartApplicationDelay.setText(text);
        setTextStyle(textViewStartApplicationDelay, errorColor);

        TooltipCompat.setTooltipText(imageViewMenu, context.getString(R.string.tooltip_options_menu));
        imageViewMenu.setTag(application);
        imageViewMenu.setOnClickListener(v -> preference.showEditMenu(imageViewMenu));

    }

    @Override
    public void onClick(View v) {

        // 5. Handle the onClick event for the ViewHolder
        if (this.application != null) {
            preference.startEditor(this.application);
        }
    }

    private void setTextStyle(TextView textView, boolean errorColor)
    {
        if (textView != null) {
            CharSequence title = textView.getText();
            Spannable sbt = new SpannableString(title);
            Object[] spansToRemove = sbt.getSpans(0, title.length(), Object.class);
            for (Object span : spansToRemove) {
                if (span instanceof CharacterStyle)
                    sbt.removeSpan(span);
            }
            if (errorColor) {
                sbt.setSpan(new ForegroundColorSpan(Color.RED), 0, sbt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(sbt);
        }
    }

}
