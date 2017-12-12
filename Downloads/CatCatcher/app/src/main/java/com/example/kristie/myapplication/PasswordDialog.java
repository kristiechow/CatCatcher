package com.example.kristie.myapplication;

/**
 * Created by Kristie on 10/1/17.
 */
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;

public class PasswordDialog extends DialogFragment {

    private String pass;
    private Button confirm;
    private EditText passwordField;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialog_view = inflater.inflate(R.layout.password_box, null);

        builder.setView(dialog_view);

        confirm = dialog_view.findViewById(R.id.confirm);
        passwordField = dialog_view.findViewById(R.id.password2);

        confirm.setEnabled(false);
        Log.d("pwd", MainActivity.pwd.toString());

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (passwordField.getText().toString().equals(MainActivity.pwd.toString())) {
                    Log.d("redo", confirm.toString());
                    confirm.setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                dismiss();
            }
        });

        return builder.create();
    }

}
