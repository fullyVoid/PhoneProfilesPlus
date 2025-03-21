package com.codetroopers.betterpickers.numberpicker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.codetroopers.betterpickers.OnDialogDismissListener;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment.NumberPickerDialogHandlerV2;

import java.math.BigDecimal;
import java.util.Vector;

/**
 * User: derek Date: 5/2/13 Time: 7:55 PM
 */
@SuppressWarnings("unused")
public class NumberPickerBuilder {

    private FragmentManager manager; // Required
    private Integer styleResId; // Required
    private Fragment targetFragment;
    private BigDecimal minNumber;
    private BigDecimal maxNumber;
    private Integer plusMinusVisibility;
    private Integer decimalVisibility;
    private String labelText;
    private int mReference;
    private final Vector<NumberPickerDialogHandlerV2> mNumberPickerDialogHandlersV2 = new Vector<>();
    private Integer currentNumberValue;
    private Double currentDecimalValue;
    private Integer currentSignValue;
    private OnDialogDismissListener mOnDismissListener;

    /**
     * Attach a FragmentManager. This is required for creation of the Fragment.
     *
     * @param manager the FragmentManager that handles the transaction
     * @return the current Builder object
     */
    public NumberPickerBuilder setFragmentManager(FragmentManager manager) {
        this.manager = manager;
        return this;
    }

    /**
     * Attach a style resource ID for theming. This is required for creation of the Fragment. Two stock styles are
     * provided using R.style.BetterPickersDialogFragment and R.style.BetterPickersDialogFragment.Light
     *
     * @param styleResId the style resource ID to use for theming
     * @return the current Builder object
     */
    public NumberPickerBuilder setStyleResId(int styleResId) {
        this.styleResId = styleResId;
        return this;
    }

    /**
     * Attach a target Fragment. This is optional and useful if creating a Picker within a Fragment.
     *
     * @param targetFragment the Fragment to attach to
     * @return the current Builder object
     */
    public NumberPickerBuilder setTargetFragment(Fragment targetFragment) {
        this.targetFragment = targetFragment;
        return this;
    }

    /**
     * Attach a reference to this Picker instance. This is used to track multiple pickers, if the user wishes.
     *
     * @param reference a user-defined int intended for Picker tracking
     * @return the current Builder object
     */
    public NumberPickerBuilder setReference(int reference) {
        this.mReference = reference;
        return this;
    }

    /**
     * Set initial value to display
     */
    public NumberPickerBuilder setCurrentNumber(Integer number) {
        if (number != null) {
            if (number >= 0) {
                this.currentSignValue = NumberPicker.SIGN_POSITIVE;
            } else {
                this.currentSignValue = NumberPicker.SIGN_NEGATIVE;
                number = number * -1;
            }

            this.currentNumberValue = number;
            this.currentDecimalValue = null;
        }
        return this;
    }

    /**
     * Set initial value to display
     */
    public NumberPickerBuilder setCurrentNumber(BigDecimal number) {
        if (number != null) {
            if (number.signum() >= 0) {
                this.currentSignValue = NumberPicker.SIGN_POSITIVE;
            } else {
                this.currentSignValue = NumberPicker.SIGN_NEGATIVE;
                number = number.abs();
            }
            BigDecimal[] numberInput = number.divideAndRemainder(BigDecimal.ONE);
            this.currentNumberValue = numberInput[0].intValue();
            this.currentDecimalValue = numberInput[1].doubleValue();
        }
        return this;
    }

    /**
     * Set a minimum number required
     *
     * @param minNumber the minimum required number
     * @return the current Builder object
     */
    public NumberPickerBuilder setMinNumber(BigDecimal minNumber) {
        this.minNumber = minNumber;
        return this;
    }

    /**
     * Set a maximum number required
     *
     * @param maxNumber the maximum required number
     * @return the current Builder object
     */
    public NumberPickerBuilder setMaxNumber(BigDecimal maxNumber) {
        this.maxNumber = maxNumber;
        return this;
    }

    /**
     * Set the visibility of the +/- button. This takes an int corresponding to Android's View.VISIBLE, View.INVISIBLE,
     * or View.GONE.  When using View.INVISIBLE, the +/- button will still be present in the layout but be
     * non-clickable. When set to View.GONE, the +/- button will disappear entirely, and the "0" button will occupy its
     * space.
     *
     * @param plusMinusVisibility an int corresponding to View.VISIBLE, View.INVISIBLE, or View.GONE
     * @return the current Builder object
     */
    public NumberPickerBuilder setPlusMinusVisibility(int plusMinusVisibility) {
        this.plusMinusVisibility = plusMinusVisibility;
        return this;
    }

    /**
     * Set the visibility of the decimal button. This takes an int corresponding to Android's View.VISIBLE,
     * View.INVISIBLE, or View.GONE.  When using View.INVISIBLE, the decimal button will still be present in the layout
     * but be non-clickable. When set to View.GONE, the decimal button will disappear entirely, and the "0" button will
     * occupy its space.
     *
     * @param decimalVisibility an int corresponding to View.VISIBLE, View.INVISIBLE, or View.GONE
     * @return the current Builder object
     */
    public NumberPickerBuilder setDecimalVisibility(int decimalVisibility) {
        this.decimalVisibility = decimalVisibility;
        return this;
    }

    /**
     * Set the (optional) text shown as a label. This is useful if wanting to identify data with the number being
     * selected.
     *
     * @param labelText the String text to be shown
     * @return the current Builder object
     */
    public NumberPickerBuilder setLabelText(String labelText) {
        this.labelText = labelText;
        return this;
    }


    /**
     * Attach universal objects as additional handlers for notification when the Picker is set. For most use cases, this
     * method is not necessary as attachment to an Activity or Fragment is done automatically.  If, however, you would
     * like additional objects to subscribe to this Picker being set, attach Handlers here.
     *
     * @param handler an Object implementing the appropriate Picker Handler
     * @return the current Builder object
     */
    public NumberPickerBuilder addNumberPickerDialogHandler(NumberPickerDialogHandlerV2 handler) {
        this.mNumberPickerDialogHandlersV2.add(handler);
        return this;
    }

    /**
     * Remove objects previously added as handlers.
     *
     * @param handler the Object to remove
     * @return the current Builder object
     */
    public NumberPickerBuilder removeNumberPickerDialogHandler(NumberPickerDialogHandlerV2 handler) {
        this.mNumberPickerDialogHandlersV2.remove(handler);
        return this;
    }

    /**
     * Instantiate and show the Picker
     */
    public void show() {
        if (manager == null || styleResId == null) {
            return;
        }
        FragmentTransaction ft = manager.beginTransaction();
        final Fragment prev = manager.findFragmentByTag("number_dialog");
        if (prev != null) {
            ft.remove(prev).commit();
            ft = manager.beginTransaction();
        }
        ft.addToBackStack(null);

        final NumberPickerDialogFragment fragment = NumberPickerDialogFragment
                .newInstance(mReference, styleResId, minNumber, maxNumber, plusMinusVisibility, decimalVisibility,
                        labelText, currentNumberValue, currentDecimalValue, currentSignValue);
        if (targetFragment != null) {
            //noinspection deprecation
            fragment.setTargetFragment(targetFragment, 0);
        }
        fragment.setNumberPickerDialogHandlersV2(mNumberPickerDialogHandlersV2);
        fragment.setOnDismissListener(mOnDismissListener);
        fragment.show(ft, "number_dialog");
    }

    public NumberPickerBuilder setOnDismissListener(OnDialogDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
        return this;
    }
}
