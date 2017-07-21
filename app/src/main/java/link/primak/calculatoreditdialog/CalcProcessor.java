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
 * Last modified 7/15/17 11:41 PM
 */

package link.primak.calculatoreditdialog;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class CalcProcessor extends BaseObservable {
    private static final String REG_EXP  = "-?\\d*\\.?\\d*";
    public static final double DEFAULT_DOUBLE = 0.0;

    private String mInputValue; // String buffer for digits have been input
    private boolean mIsNewInputValueRequired; // After selecting any operation or resetting calculation
                                              // the new input value is required
    private String mOperationSequence; // The presentation of the operations history
    private boolean mIsComputed; // Shows that COMPUTE (=) has been selected as previous operation
    private Double mLastOperand; // User value has been input previously
    private CalcOperation mLastOperation; // Last operation selected (arithmetic only - except COMPUTE)
    private List<String> mOperationList; // History of the operations and operands
    private Double mCalcResult; // Result of computation

    /**
     * Default constructor
     */
    public CalcProcessor() {
        mOperationList = new ArrayList<>();
        resetCalculator();
    }

    /**
     * Constructor with specified initial value
     * @param initValue
     */
    public CalcProcessor(Double initValue) {
        this();
        setInputValue(initValue.toString());
        mIsNewInputValueRequired = false;
    }

    @Bindable
    public String getInputValue() {
        return mInputValue;
    }

    /**
     * Update input value string (buffer) and notify observers on changes
     * @param inputValue - new value
     */
    public void setInputValue(String inputValue) {
        if (inputValue.endsWith(".0") && mIsNewInputValueRequired) {
            inputValue = inputValue.substring(0, inputValue.length() - 2);
        }

        if (TextUtils.isEmpty(inputValue)) {
            inputValue = "0";
        }

        this.mInputValue = inputValue;
        notifyPropertyChanged(link.primak.calculatoreditdialog.BR.inputValue);
    }

    @Bindable
    public String getOperationSequence() {
        return mOperationSequence;
    }

    /**
     * Update operation sequence string presentation and notify observers on changes
     * @param operationSequence - new value
     */
    public void setOperationSequence(String operationSequence) {
        this.mOperationSequence = operationSequence;
        notifyPropertyChanged(link.primak.calculatoreditdialog.BR.operationSequence);
    }

    /**
     * Perform calculations
     * @param op1 - first operand
     * @param op2 - second operand
     * @param operation - operation to be applied to the operands
     * @return - result of the calculation
     */
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

    /**
     * Build string representation of the history list
     * @return string
     */
    private String buildOperationHistory() {
        StringBuilder sb = new StringBuilder();
        for (String s : mOperationList) {
            if (s.endsWith(".0")) {
                sb.append(s.substring(0, s.length() - 2));
            } else {
                sb.append(s);
            }
            sb.append(" ");
        }

        return sb.toString();
    }

    /**
     * Reset values before new calculation sequence
     */
    private void beginCalcSequence() {
        mOperationList.clear();
        setOperationSequence(buildOperationHistory());
        mLastOperation = null;
        mLastOperand = DEFAULT_DOUBLE;
    }

    /**
     * Process user operation selection
     * @param operation - selected operation
     */
    public void onOperationSelected(CalcOperation operation) {
            /* If previously totals calculation has been performed (COMPUTE),
                it is needed to reset mLastOperation before new one
                (arithmetic only - except COMPUTE)
             */
        if ((mIsComputed) && (operation != CalcOperation.COMPUTE)) {
            beginCalcSequence();
            mLastOperand = mCalcResult;
            mOperationList.add(String.valueOf(mLastOperand)); // Result should be kept
            mOperationList.add(operation.getSymbol()); // Result should be kept
        }

        if (!mIsNewInputValueRequired) {
            // there is an user input need to be passed into mLastOperand
            try {
                mLastOperand = Double.valueOf(mInputValue);
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
            if ((!mIsNewInputValueRequired) || (operation == CalcOperation.COMPUTE)) {
                mCalcResult = calc(mCalcResult, mLastOperand, mLastOperation);
            }
        } else {
            mCalcResult = mLastOperand;
        }

        // Update operation
        mIsComputed = (operation == CalcOperation.COMPUTE);
        if (!mIsComputed) {
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

        setInputValue(String.valueOf(mCalcResult));
        setOperationSequence(buildOperationHistory());
    }

    /**
     * Adds one or more symbols to the input string
     * @param appendix - to be appended
     */
    public void appendInput(String appendix) {
        // begin new calculation sequence since "=" has been pressed
        if (mIsComputed) {
            mIsComputed = false;
            beginCalcSequence();
        }

        String testString;
        // Start new value or change existing one
        if (mIsNewInputValueRequired) {
            testString = appendix;
        } else {
            testString = getInputValue() + appendix;
        }

        // Validate input through RegExp
        if (testString.matches(REG_EXP)) {
            setInputValue(testString);
            mIsNewInputValueRequired = false;
        }
    }

    /**
     * Perform Backspace operation on input string
     */
    public void backspaceInput() {
        // If input string does not represent number value - clear it completely
        if (!mInputValue.matches(REG_EXP)) {
            clearInputValue();
        } else {
            if (mInputValue.length() > 0) {
                setInputValue(mInputValue.substring(0, mInputValue.length()-1));
            }
        }
    }

    /**
     * Clear inputted value
     */
    public void clearInputValue() {
        setInputValue("0");
        mIsNewInputValueRequired = true;
    }

    /**
     * Reset calculator states
     */
    public void resetCalculator() {
        clearInputValue();
        beginCalcSequence();
        mCalcResult = DEFAULT_DOUBLE;
    }

    /**
     * Return result of calculation and finishes calculation if needed
     * @param finishCalculation - define if there is need to finish any pending calculations
     * @return last evaluated result
     */
    public Double getCalcResult(boolean finishCalculation) {
        if (finishCalculation && !mIsNewInputValueRequired) {
            onOperationSelected(CalcOperation.COMPUTE);
        }
        return mCalcResult;
    }

    public enum CalcOperation {
        ADD, SUBTRACTION, MULTIPLICATION, DIVISION, COMPUTE;

        public String getSymbol() {
            switch (this) {
                case ADD: return "+";
                case SUBTRACTION: return "-";
                case MULTIPLICATION: return "×";
                case DIVISION: return "÷";
                case COMPUTE: return "=";
                default: return "";
            }
        }
    }
}
