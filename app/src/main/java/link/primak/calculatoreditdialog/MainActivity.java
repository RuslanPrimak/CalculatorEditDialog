package link.primak.calculatoreditdialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import link.primak.calculatoreditdialog.databinding.CalculatorLayoutBinding;

public class MainActivity extends AppCompatActivity {
    private boolean mIsNewInputValueRequired;
    private String mInputString;
    private CalcOperation mStackOperation;
    private Double mStackValue;
    private String mOperationsString;

    private CalculatorLayoutBinding mBinding;
    private static final String REG_EXP  = "-?\\d*\\.?\\d*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.calculator_layout);
        mBinding.setProcessor(new CalcProcessor());
        onCEClick(mBinding.btnOpCE); // Set initial values
    }

    public void onDigitClick(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;

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
        mBinding.textOperations.setText(mOperationsString);
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
        mStackValue = null;
        mStackOperation = null;
        mOperationsString = "";
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
                default: return 0.0;
            }
        }


        public void onOperation(View view, CalcOperation operation){
            // TODO: 17-Jul-17 Нужно переделать алгоритм: нужны три переменных:
            // Результат вычисления
            // последняя операция
            // последний операнд
            // если mIsNewInputValueRequired == false, то последний операнд обновляется из Input String
            // В формуле всегда учавствует Результат вычисления, последняя операция, последний операнд
            // Если mIsNewInputValueRequired == true, то ввод не переносится в последний операнд - это
            // позволит повторять вычисления по нажатию на клавишу равно.

            // If new input not specified - update operations only
            if ((mIsNewInputValueRequired) && (operation != CalcOperation.CALCULATION)) {
                mStackOperation = operation;
            } else {
                mIsNewInputValueRequired = true;

                // Retrieve input value
                Double inputValue;
                try {
                    inputValue = Double.valueOf(mInputString);
                    mOperationsString += mInputString + " ";
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    inputValue = 0.0;
                    mOperationsString += "0 ";
                }

                // Write to the operation string
                mOperationsString += operation.getSymbol() + " ";

                // Perform previous calculation
                if ((mStackOperation != null) && (mStackValue != null)) {
                    Double result = calc(mStackValue, inputValue, mStackOperation);
                    // swap values to keep operation for repetitive calculations
                    mStackValue = inputValue;
                    inputValue = result;
                }

                // If current operation is calculation - chain of calculation is completed
                if (operation == CalcOperation.CALCULATION) {
                    mOperationsString = "";
                } else {
                    mStackValue = inputValue;
                    mStackOperation = operation;
                }

                mInputString = String.valueOf(inputValue);
            }

            showValue();
        }
    }
}
