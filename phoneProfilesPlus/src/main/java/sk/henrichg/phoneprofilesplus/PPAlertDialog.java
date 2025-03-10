package sk.henrichg.phoneprofilesplus;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

class PPAlertDialog {
    final AlertDialog mDialog;
    final Activity activity;

    /*
    final DialogInterface.OnClickListener positiveClick;
    final DialogInterface.OnClickListener negativeClick;
    final DialogInterface.OnClickListener neutralClick;
    */

    PPAlertDialog(CharSequence _title, CharSequence _message,
                  CharSequence _positiveText, CharSequence _negativeText,
                  @SuppressWarnings("SameParameterValue") CharSequence _neutralText,
                  CharSequence _checkBoxText,
                  DialogInterface.OnClickListener _positiveClick,
                  DialogInterface.OnClickListener _negativeClick,
                  @SuppressWarnings("SameParameterValue") DialogInterface.OnClickListener _neutralClick,
                  DialogInterface.OnCancelListener _cancelListener,
                  CompoundButton.OnCheckedChangeListener _checkBoxListener,
                  boolean _cancelable,
                  boolean _canceledOnTouchOutside,
                  boolean _checBoxChecked,
                  boolean _checkBoxEnabled,
                  boolean _hideButtonBarDivider,
                  Activity _activity) {
        this.activity = _activity;
        /*
        this.positiveClick = _positiveClick;
        this.negativeClick = _negativeClick;
        this.neutralClick = _neutralClick;
        */

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle(_title);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_info_preference, null);
        dialogBuilder.setView(layout);

        dialogBuilder.setPositiveButton(_positiveText, _positiveClick);
        if (_negativeText != null)
            dialogBuilder.setNegativeButton(_negativeText, _negativeClick);
        if (_neutralText != null)
            dialogBuilder.setNeutralButton(_neutralText, _neutralClick);

        if (_cancelListener != null)
            dialogBuilder.setOnCancelListener(_cancelListener);

        dialogBuilder.setCancelable(_cancelable);

        mDialog = dialogBuilder.create();

        //mDialog.setOnShowListener(dialog -> doShow());

        TextView messageText = layout.findViewById(R.id.info_pref_dialog_info_text);
        messageText.setText(_message);

        View buttonsDivider = layout.findViewById(R.id.info_pref_dialog_buttonBarDivider);
        if (_hideButtonBarDivider)
            buttonsDivider.setVisibility(View.GONE);
        else
            buttonsDivider.setVisibility(View.VISIBLE);

        mDialog.setCanceledOnTouchOutside(_canceledOnTouchOutside);

        if (_checkBoxListener != null) {
            CheckBox checkBox = layout.findViewById(R.id.info_pref_dialog_checkBox);
            checkBox.setText(_checkBoxText);
            checkBox.setEnabled(_checkBoxEnabled);
            checkBox.setChecked(_checBoxChecked);
            checkBox.setOnCheckedChangeListener(_checkBoxListener);
            checkBox.setVisibility(View.VISIBLE);
        }
    }

    /*
    private void doShow() {
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        SingleSelectListDialogAdapter listAdapter = new SingleSelectListDialogAdapter(itemsRes, this);
        listView.setAdapter(listAdapter);
    }
    */

    void show() {
        if (!activity.isFinishing())
            mDialog.show();
    }

}
