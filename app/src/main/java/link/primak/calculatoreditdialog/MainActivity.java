package link.primak.calculatoreditdialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import link.primak.calculatoreditdialog.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivityTag";
    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    public void onButtonClick(View view) {
        //Log.d(TAG, "onButtonClick: ");
        CalcEditDialog dialog = new CalcEditDialog();

        Double initValue;
        try {
            initValue = Double.valueOf(mBinding.textView.getText().toString());
        } catch (NumberFormatException nfe) {
            initValue = 0.0;
        }

        //Log.d(TAG, "new Bundle(): ");
        Bundle args = new Bundle();
        args.putDouble(CalcEditDialog.ARG_INIT_VALUE, initValue);
        dialog.setArguments(args);
        dialog.setOnCalcValueEditListener(new CalcEditDialog.OnCalcValueEditListener() {
            @Override
            public void onCalcValueEdited(Double value) {
                mBinding.textView.setText(String.valueOf(value));
            }
        });
        //Log.d(TAG, "dialog.show");
        dialog.show(getSupportFragmentManager(), "calculator");
    }

}
