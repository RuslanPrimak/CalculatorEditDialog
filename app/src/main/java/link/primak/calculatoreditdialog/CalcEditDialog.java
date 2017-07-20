/*
 * Copyright (c) 2017. Ruslan Primak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 7/19/17 1:40 PM
 */

package link.primak.calculatoreditdialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;

import link.primak.calculatoreditdialog.databinding.CalculatorLayoutBinding;

public class CalcEditDialog extends AppCompatDialogFragment {
    private static final String TAG = "CalcEditDialog";
    public static final String ARG_INIT_VALUE = "arg_init_value";
    private CalculatorLayoutBinding mLayoutBinding;
    private OnCalcValueEditListener mOnCalcValueEditListener;
    private CalcProcessor mCalcProcessor;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateDialog: ");
//        Log.d(TAG, "savedInstanceState: " + savedInstanceState);
//        Log.d(TAG, "getArguments: " + this.getArguments());

        Bundle args = this.getArguments();
        if (args != null) {
            mCalcProcessor = new CalcProcessor(
                    args.getDouble(ARG_INIT_VALUE, CalcProcessor.DEFAULT_DOUBLE));

        } else {
            mCalcProcessor = new CalcProcessor();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mLayoutBinding = CalculatorLayoutBinding.inflate(getActivity().getLayoutInflater());
        mLayoutBinding.setProcessor(mCalcProcessor);
        builder.setView(mLayoutBinding.getRoot());
        final AlertDialog dialog = builder.create();
        mLayoutBinding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if there is a valid result
                Double result = mCalcProcessor.getCalcResult(true);
                if (!(result.isInfinite() || result.isNaN())) {
                    if (mOnCalcValueEditListener != null) {
                        mOnCalcValueEditListener.onCalcValueEdited(result);
                    }
                    dialog.dismiss();
                }
            }
        });

        return dialog;
    }

    public void setOnCalcValueEditListener(OnCalcValueEditListener listener) {
        this.mOnCalcValueEditListener = listener;
    }

    interface OnCalcValueEditListener {
        void onCalcValueEdited(Double value);
    }


}
