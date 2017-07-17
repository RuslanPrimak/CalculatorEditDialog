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

import static link.primak.calculatoreditdialog.CalcProcessor.Operation.COMPUTE;

public class CalcProcessor {

    private Double mResult;
    private Double mOperand;
    private Operation mOperation;

    /**
     * Default constructor without arguments
     */
    public CalcProcessor() {
    }

    /**
     * Constructor with predefined result value
     * @param result - predefined value
     */
    public CalcProcessor(Double result) {
        this();
        mResult = result;
    }

    public double getResult() {
        return mResult;
    }

    public void setResult(Double result) {
        this.mResult = result;
    }

    public double getOperand() {
        return mOperand;
    }

    public void setOperand(Double operand) {
        // Zero is not acceptable as value for the operand. Addition, subtraction and mult
        if (operand == 0) {
            this.mOperand = null;
        } else {
            this.mOperand = operand;
        }
    }

    public void calc(Operation operation) {
        // If mResult is empty the value of the operand is copied to the mResult
        // and the value of the mOperand has been reset
        if (mResult == null) {
            setResult(mOperand);
            setOperand(null);
        }

        // if mOperand is null - change operation only
        if ((mOperand == null) && (operation != COMPUTE)) {
            mOperation = operation;
        }


    }

    enum Operation
    {
        ADD, SUBTRACTION, MULTIPLICATION, DIVISION, COMPUTE, NONE
    }
}
