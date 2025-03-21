package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NFCTagPreferenceFragment extends PreferenceDialogFragmentCompat {

    private Context prefContext;
    private NFCTagPreference preference;

    private SingleSelectListDialog mSelectorDialog;
    //private LinearLayout progressLinearLayout;
    //private RelativeLayout dataRelativeLayout;
    private ListView nfcTagListView;
    private EditText nfcTagName;
    private AppCompatImageButton addIcon;
    private NFCTagPreferenceAdapter listAdapter;

    private RefreshListViewAsyncTask rescanAsyncTask;

    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView(@NonNull Context context)
    {
        prefContext = context;
        preference = (NFCTagPreference) getPreference();
        preference.fragment = this;

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.dialog_nfc_tag_preference, null, false);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        //progressLinearLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_linla_progress);
        //dataRelativeLayout = layout.findViewById(R.id.nfc_tag_pref_dlg_rella_data);

        addIcon = view.findViewById(R.id.nfc_tag_pref_dlg_addIcon);
        TooltipCompat.setTooltipText(addIcon, getString(R.string.nfc_tag_pref_dlg_add_button_tooltip));
        addIcon.setOnClickListener(v -> {
            String tagName = nfcTagName.getText().toString();

            /*addNfcTag(tagName);
            NFCTag tag = new NFCTag(0, tagName, "");
            DatabaseHandler.getInstance(context).addNFCTag(tag);
            refreshListView(tagName);*/

            /*Intent nfcTagIntent = new Intent(context.getApplicationContext(), NFCTagReadActivity.class);
            nfcTagIntent.putExtra(NFCTagReadActivity.EXTRA_TAG_NAME, tag);
            ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_READ_EDITOR);*/

            preference.writeToNFCTag(0, tagName);
        });

        nfcTagName = view.findViewById(R.id.nfc_tag_pref_dlg_bt_name);
        nfcTagName.setBackgroundTintList(ContextCompat.getColorStateList(prefContext, R.color.highlighted_spinner_all));
        nfcTagName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GlobalGUIRoutines.setImageButtonEnabled(!nfcTagName.getText().toString().isEmpty(),
                        addIcon, prefContext.getApplicationContext());
            }
        });

        GlobalGUIRoutines.setImageButtonEnabled(!nfcTagName.getText().toString().isEmpty(),
                addIcon, prefContext.getApplicationContext());

        nfcTagListView = view.findViewById(R.id.nfc_tag_pref_dlg_listview);
        listAdapter = new NFCTagPreferenceAdapter(prefContext, preference);
        nfcTagListView.setAdapter(listAdapter);

        nfcTagListView.setOnItemClickListener((parent, item, position, id) -> {
            String ssid = preference.nfcTagList.get(position)._name;
            NFCTagPreferenceAdapter.ViewHolder viewHolder =
                    (NFCTagPreferenceAdapter.ViewHolder) item.getTag();
            viewHolder.checkBox.setChecked(!preference.isNfcTagSelected(ssid));
            if (viewHolder.checkBox.isChecked())
                preference.addNfcTag(ssid);
            else
                preference.removeNfcTag(ssid);
        });

        /*
        nfcTagListView.setOnItemLongClickListener((parent, view12, position, id) -> {
            //NFCTagPreferenceAdapter.ViewHolder viewHolder =
            //        (NFCTagPreferenceAdapter.ViewHolder) v.getTag();
            String nfcTag = preference.nfcTagList.get(position)._name;
            nfcTagName.setText(nfcTag);
            return true;
        });
        */

        /*
        final TextView helpText = layout.findViewById(R.id.nfc_tag_pref_dlg_helpText);
        final ImageView helpIcon = layout.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        ApplicationPreferences.getSharedPreferences(context);
        if (ApplicationPreferences.preferences.getBoolean(PREF_SHOW_HELP, true)) {
            helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
            helpText.setVisibility(View.VISIBLE);
        }
        else {
            helpIcon.setImageResource(R.drawable.ic_button_help);
            helpText.setVisibility(View.GONE);
        }
        helpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                int visibility = helpText.getVisibility();
                if (visibility == View.VISIBLE) {
                    helpIcon.setImageResource(R.drawable.ic_button_help);
                    visibility = View.GONE;
                    editor.putBoolean(PREF_SHOW_HELP, false);
                }
                else {
                    helpIcon.setImageResource(R.drawable.ic_action_profileicon_help_closed);
                    visibility = View.VISIBLE;
                    editor.putBoolean(PREF_SHOW_HELP, true);
                }
                helpText.setVisibility(visibility);
                editor.apply();
            }
        });
        */
        final ImageView helpIcon = view.findViewById(R.id.nfc_tag_pref_dlg_helpIcon);
        TooltipCompat.setTooltipText(helpIcon, getString(R.string.help_button_tooltip));
        helpIcon.setOnClickListener(v -> DialogHelpPopupWindow.showPopup(helpIcon, R.string.menu_help, (Activity)prefContext, /*getDialog(),*/ R.string.nfc_tag_pref_dlg_help, false));


        ImageView changeSelectionIcon = view.findViewById(R.id.nfc_tag_pref_dlg_changeSelection);
        TooltipCompat.setTooltipText(changeSelectionIcon, getString(R.string.nfc_tag_pref_dlg_select_button_tooltip));
        changeSelectionIcon.setOnClickListener(view1 -> {
            if (getActivity() != null)
                if (!getActivity().isFinishing()) {
                    mSelectorDialog = new SingleSelectListDialog(
                            R.string.pref_dlg_change_selection_title,
                            R.array.nfcTagsChangeSelectionArray,
                            SingleSelectListDialog.NOT_USE_RADIO_BUTTONS,
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        preference.value = "";
                                        break;
                                    case 1:
                                        for (NFCTag nfcTag : preference.nfcTagList) {
                                            if (nfcTag._name.equals(nfcTagName.getText().toString()))
                                                preference.addNfcTag(nfcTag._name);
                                        }
                                        break;
                                    default:
                                }
                                refreshListView("");
                                //dialog.dismiss();
                            },
                            false,
                            (Activity) prefContext);

                    mSelectorDialog.show();
                }
        });

        refreshListView("");
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.persistValue();
        } else {
            preference.resetSummary();
        }

        if ((mSelectorDialog != null) && mSelectorDialog.mDialog.isShowing())
            mSelectorDialog.mDialog.dismiss();

        if ((rescanAsyncTask != null) && rescanAsyncTask.getStatus().equals(AsyncTask.Status.RUNNING))
            rescanAsyncTask.cancel(true);

        preference.fragment = null;
    }

    void refreshListView(/*boolean forRescan, */final String scrollToTag)
    {
        //final boolean _forRescan = forRescan;

        rescanAsyncTask = new RefreshListViewAsyncTask(scrollToTag, preference, this, prefContext);
        rescanAsyncTask.execute();
    }

    private static class SortList implements Comparator<NFCTag> {

        public int compare(NFCTag lhs, NFCTag rhs) {
            if (PPApplication.collator != null)
                return PPApplication.collator.compare(lhs._name, rhs._name);
            else
                return 0;
        }

    }

    void showEditMenu(View view)
    {
        //Context context = ((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext();
        final Context viewContext = view.getContext();
        PopupMenu popup;
        //if (android.os.Build.VERSION.SDK_INT >= 19)
        popup = new PopupMenu(viewContext, view, Gravity.END);
        //else
        //    popup = new PopupMenu(context, view);
        new MenuInflater(viewContext).inflate(R.menu.nfc_tag_pref_dlg_item_edit, popup.getMenu());

        int tagPos = (int)view.getTag();
        final NFCTag tagInItem = preference.nfcTagList.get(tagPos);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nfc_tag_pref_dlg_item_menu_writeToNfcTag) {
                preference.writeToNFCTag(tagInItem._id, tagInItem._name);
                return true;
            }
                /*case R.id.nfc_tag_pref_item_menu_readNfcUid:
                    Intent nfcTagIntent = new Intent(context, NFCTagReadActivity.class);
                    nfcTagIntent.putExtra(NFCTagReadActivity.EXTRA_TAG_NAME, tagInItem._name);
                    nfcTagIntent.putExtra(NFCTagReadActivity.EXTRA_TAG_DB_ID, tagInItem._id);
                    ((Activity)context).startActivityForResult(nfcTagIntent, RESULT_NFC_TAG_READ_EDITOR);
                    return true;*/
            else
            if (itemId == R.id.nfc_tag_pref_dlg_item_menu_change) {
                if (!nfcTagName.getText().toString().isEmpty()) {
                    String[] splits = preference.value.split("\\|");
                    preference.value = "";
                    boolean found = false;
                    // add all tags without item tag
                    for (String tag : splits) {
                        if (!tag.isEmpty()) {
                            if (!tag.equals(tagInItem._name)) {
                                if (!preference.value.isEmpty())
                                    //noinspection StringConcatenationInLoop
                                    preference.value = preference.value + "|";
                                //noinspection StringConcatenationInLoop
                                preference.value = preference.value + tag;
                            } else
                                found = true;
                        }
                    }
                    if (found) {
                        // add item tag with new name
                        if (!preference.value.isEmpty())
                            preference.value = preference.value + "|";
                        preference.value = preference.value + nfcTagName.getText().toString();
                    }
                    tagInItem._name = nfcTagName.getText().toString();
                    DatabaseHandler.getInstance(prefContext.getApplicationContext()).updateNFCTag(tagInItem);
                    refreshListView("");
                }
                return true;
            }
            else
            if (itemId == R.id.nfc_tag_pref_dlg_item_menu_delete) {
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.profile_context_item_delete),
                            getString(R.string.delete_nfc_tag_alert_message),
                            getString(R.string.alert_button_yes),
                            getString(R.string.alert_button_no),
                            null, null,
                            (dialog1, which) -> {
                                preference.removeNfcTag(tagInItem._name);
                                DatabaseHandler.getInstance(prefContext.getApplicationContext()).deleteNFCTag(tagInItem);
                                refreshListView("");
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                }
                return true;
            }
            else
            if (itemId == R.id.nfc_tag_pref_dlg_item_menu_copy_name) {
                String nfcTag = tagInItem._name;
                nfcTagName.setText(nfcTag);
                return true;
            }
            else {
                return false;
            }
        });


        if (getActivity() != null)
            if (!getActivity().isFinishing())
                popup.show();
    }

    private static class RefreshListViewAsyncTask extends AsyncTask<Void, Integer, Void> {
        List<NFCTag> _nfcTagList = null;

        final String scrollToTag;
        private final WeakReference<NFCTagPreference> preferenceWeakRef;
        private final WeakReference<NFCTagPreferenceFragment> fragmentWeakRef;
        private final WeakReference<Context> prefContextWeakRef;

        public RefreshListViewAsyncTask(final String scrollToTag,
                                        NFCTagPreference preference,
                                        NFCTagPreferenceFragment fragment,
                                        Context prefContext) {
            this.scrollToTag = scrollToTag;
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.fragmentWeakRef = new WeakReference<>(fragment);
            this.prefContextWeakRef = new WeakReference<>(prefContext);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            _nfcTagList = new ArrayList<>();

            /*
            if (_forRescan) {
                dataRelativeLayout.setVisibility(View.GONE);
                progressLinearLayout.setVisibility(View.VISIBLE);
            }
            */
        }

        @Override
        protected Void doInBackground(Void... params) {
            NFCTagPreferenceFragment fragment = fragmentWeakRef.get();
            NFCTagPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {

                //if (_forRescan)
                //{
                //}

                // add all from db
                List<NFCTag> tagsFromDb = DatabaseHandler.getInstance(prefContext).getAllNFCTags();
                for (NFCTag tag : tagsFromDb)
                    _nfcTagList.add(new NFCTag(tag._id, tag._name, tag._uid));

                // add all from value
                boolean found;
                String[] splits = preference.value.split("\\|");
                for (String tag : splits) {
                    if (!tag.isEmpty()) {
                        found = false;
                        for (NFCTag tagData : _nfcTagList) {
                            if (tag.equals(tagData._name)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            for (NFCTag tagFromDb : tagsFromDb) {
                                if (tagFromDb._name.equals(tag))
                                    _nfcTagList.add(new NFCTag(tagFromDb._id, tag, tagFromDb._uid));
                            }
                        }
                    }
                }

                _nfcTagList.sort(new SortList());

                // move checked on top
                int i = 0;
                int ich = 0;
                while (i < _nfcTagList.size()) {
                    NFCTag nfcTag = _nfcTagList.get(i);
                    if (preference.isNfcTagSelected(nfcTag._name)) {
                        _nfcTagList.remove(i);
                        _nfcTagList.add(ich, nfcTag);
                        ich++;
                    }
                    i++;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            NFCTagPreferenceFragment fragment = fragmentWeakRef.get();
            NFCTagPreference preference = preferenceWeakRef.get();
            Context prefContext = prefContextWeakRef.get();
            if ((fragment != null) && (preference != null) && (prefContext != null)) {
                preference.nfcTagList = new ArrayList<>(_nfcTagList);
                fragment.listAdapter.notifyDataSetChanged();

                /*
                if (_forRescan) {
                    progressLinearLayout.setVisibility(View.GONE);
                    dataRelativeLayout.setVisibility(View.VISIBLE);
                }
                */

                if (!scrollToTag.isEmpty()) {
                    for (int position = 0; position < preference.nfcTagList.size() - 1; position++) {
                        if (preference.nfcTagList.get(position)._name.equals(scrollToTag)) {
                            fragment.nfcTagListView.setSelection(position);
                            break;
                        }
                    }
                }
            }
        }

    }

}
