package link.primak.calculatoreditdialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import link.primak.calculatoreditdialog.databinding.CalculatorLayoutBinding;

public class MainActivity extends AppCompatActivity {
    private boolean mIsNewInputValueRequired;
    private String mInputString;
    private List<String> mOperationList;
    //private String mOperationsString;

    private Double mCalcResult;
    private CalcOperation mLastOperation;
    private Double mLastOperand;
    private boolean mIsCalculationPressed;

    private CalculatorLayoutBinding mBinding;
    private static final String REG_EXP  = "-?\\d*\\.?\\d*";
    private static final double DEFAULT_DOUBLE = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOperationList = new ArrayList<>();
        mBinding = DataBindingUtil.setContentView(this, R.layout.calculator_layout);
        mBinding.setProcessor(new CalcProcessor());
        onCEClick(mBinding.btnOpCE); // Set initial values
    }

    public void onDigitClick(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;

            // begin new calculation sequence since "=" has been pressed
            if (mIsCalculationPressed) {
                mIsCalculationPressed = false;
                beginCalcSequence();
            }

            String testString;
            // Start new value or change existing one
            if (mIsNewInputValueRequired) {
                testString = textView.getText().toString();
            } else {
                testString = mInputString + textView.getText();
            }

            // Validate input through RegExp
            if (testString.matches(REG_EXP)) {
                mInputString = testString;
                if (mIsNewInputValueRequired) {
                    mIsNewInputValueRequired = false;
                }
            }

            showValue();
        }
    }

    private void showValue() {
        if (mInputString.endsWith(".0")) {
            mInputString = mInputString.substring(0, mInputString.length() - 2);
        }

        if (TextUtils.isEmpty(mInputString)) {
            mInputString = "0";
        }

        mBinding.textInput.setText(mInputString);

        StringBuilder sb = new StringBuilder();
        for (String s : mOperationList) {
            if (s.endsWith(".0")) {
                sb.append(s.substring(0, s.length() - 2));
            } else {
                sb.append(s);
            }
            sb.append(" ");
        }

        mBinding.textOperations.setText(sb.toString());
    }

    private void beginCalcSequence() {
        mOperationList.clear();
        mLastOperation = null;
        mLastOperand = DEFAULT_DOUBLE;
    }

    public void onBackspaceClick(View view) {
        // If input string represents non number value - clear it completely
        if (!mInputString.matches(REG_EXP)) {
            mInputString = "";
        }

        if (mInputString.length() > 0) {
            mInputString = mInputString.substring(0, mInputString.length()-1);
        }

        showValue();
    }

    public void onCEClick(View view) {
        beginCalcSequence();
        mCalcResult = DEFAULT_DOUBLE;

        onCClick(mBinding.btnOpC);
    }

    public void onCClick(View view) {
        mInputString = "";
        mIsNewInputValueRequired = true;
        showValue();
    }

    public class CalcProcessor {

        private Double calc(@NonNull Double op1, @NonNull Double op2,
                            @NonNull CalcOperation operation){
            switch (operation) {
                case ADD: return op1 + op2;
                case SUBTRACTION: return op1 - op2;
                case MULTIPLICATION: return op1 * op2;
                case DIVISION: return op1 / op2;
                default: return DEFAULT_DOUBLE;
            }
        }

        public void onOperation(View view, CalcOperation operation){
            /* If previously Calculation has been pressed we need reset mLastOperation before
                NEW ARITHMETIC operation
             */
            if ((mIsCalculationPressed) && (operation != CalcOperation.CALCULATION)) {
                beginCalcSequence();
                mLastOperand = mCalcResult;
                mOperationList.add(String.valueOf(mLastOperand)); // Result should be kept
                mOperationList.add(operation.getSymbol()); // Result should be kept
            }

            if (!mIsNewInputValueRequired) {
                // there is an user input need to be passed into mLastOperand
                try {
                    mLastOperand = Double.valueOf(mInputString);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    mLastOperand = DEFAULT_DOUBLE;
                }

                mOperationList.add(String.valueOf(mLastOperand));
            }

            /* If operation is invoked when mIsNewInputValueRequired == false (with user input)
              calculation is needed otherwise only update operation
              Also calculation is invoked when user executes CALCULATION
               */
            if (mLastOperation != null) {
                if ((!mIsNewInputValueRequired) || (operation == CalcOperation.CALCULATION)) {
                    mCalcResult = calc(mCalcResult, mLastOperand, mLastOperation);
                }
            } else {
                mCalcResult = mLastOperand;
            }

            // Update operation
            mIsCalculationPressed = (operation == CalcOperation.CALCULATION);
            if (!mIsCalculationPressed) {
                mLastOperation = operation;

                // Update or change sign of operation
                if (mIsNewInputValueRequired) {
                    if (mOperationList.size() > 0) {
                        mOperationList.set(mOperationList.size()-1, mLastOperation.getSymbol());
                    }
                } else {
                    mOperationList.add(mLastOperation.getSymbol());
                }
            }

            // After calculation - expect new input from user
            mIsNewInputValueRequired = true;

            mInputString = String.valueOf(mCalcResult);

            showValue();
        }
    }
}
