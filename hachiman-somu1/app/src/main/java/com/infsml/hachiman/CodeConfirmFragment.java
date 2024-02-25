package com.infsml.hachiman;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class CodeConfirmFragment extends Fragment {
    NavController navController;
    String username;
    TextView code;
    public CodeConfirmFragment() {
    }
    public static CodeConfirmFragment newInstance(String param1, String param2) {
        CodeConfirmFragment fragment = new CodeConfirmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment_layout = inflater.inflate(R.layout.fragment_code_confirm, container, false);

        navController = Navigation.findNavController(getActivity(),R.id.fragmentContainerView);

        Button submit = fragment_layout.findViewById(R.id.button4);
        code = fragment_layout.findViewById(R.id.editTextNumber);
        username="lamo2";
        Bundle params = getArguments();
        //username=params.getString("username");

        submit.setOnClickListener(v->{
            /*Amplify.Auth.confirmSignUp(
                username,
                code.getText().toString(),
                result -> Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
                error -> Log.e("AuthQuickstart", error.toString())
            );*/
        });
        return fragment_layout;
    }
}