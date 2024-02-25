package com.infsml.hachiman;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class LoginFragment extends Fragment {
    NavController navController;
    View button_list;
    View spinner;
    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment_view =inflater.inflate(R.layout.fragment_login, container, false);
        Button signup_button = fragment_view.findViewById(R.id.cancel_button);
        navController = Navigation.findNavController(requireActivity(),R.id.fragmentContainerView);
        button_list = fragment_view.findViewById(R.id.constraintLayout2);
        spinner = fragment_view.findViewById(R.id.progressBar);
        final TextView usn_textView = fragment_view.findViewById(R.id.usn);
        final TextView pass_textView = fragment_view.findViewById(R.id.password);
        signup_button.setOnClickListener((v)->{
            navController.navigate(R.id.action_loginFragment_to_signupFragment);
        });
        Button login_button = fragment_view.findViewById(R.id.register_button);
        Bundle args = getArguments();
        try {
            if (args != null) {
                if (args.getBoolean("logout", false)) {
                    FileOutputStream prev_login = getContext().openFileOutput("prev_login.json", Context.MODE_PRIVATE);
                    prev_login.write("".getBytes(StandardCharsets.UTF_8));
                    prev_login.flush();
                    prev_login.close();
                }
            }
            checkSignIn();
        }catch (Exception e){
            Log.e("Hachiman","Log out command exception",e);
        }
        login_button.setOnClickListener((v)->{
            button_list.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            (new Thread(){
                @Override
                public void run(){
                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("username", usn_textView.getText().toString());
                        payload.put("password",pass_textView.getText().toString());
                        JSONObject res = Utility.postJSON(Utility.api_base + "/login", payload.toString());
                        JSONObject saveData = new JSONObject();
                        saveData.put("username",payload.optString("username"));
                        saveData.put("token",res.optString("token"));
                        saveData.put("admin",res.optInt("admin"));
                        FileOutputStream prev_login = getContext().openFileOutput("prev_login.json", Context.MODE_PRIVATE);
                        prev_login.write(saveData.toString().getBytes(StandardCharsets.UTF_8));
                        prev_login.flush();
                        prev_login.close();
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putString("username",payload.optString("username"));
                                bundle.putString("auth_token",res.optString("token"));
                                int admin = res.optInt("admin");
                                Log.i("admin","admin = "+admin);
                                if(admin==1){
                                    navController.navigate(R.id.action_loginFragment_to_adminHomeFragment,bundle);
                                }else {
                                    navController.navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                                }
                            }
                        });
                    }catch (Exception e){
                        Log.e("Hachiman","LoginError",e);
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button_list.setVisibility(View.VISIBLE);
                                spinner.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }).start();
        });
        return fragment_view;
    }
    public void checkSignIn(){
        Log.i("Hachiman","Checking previous SignIn");
        try{
            //File prev_login = new File(getContext().getFilesDir(),"prev_login.json");
            FileInputStream prev_login = getContext().openFileInput("prev_login.json");
            int i;StringBuffer stringBuffer=new StringBuffer();
            while ((i=prev_login.read())!=-1){
                stringBuffer.append((char)i);
            }
            String readData = stringBuffer.toString();
            JSONObject readJSON = new JSONObject(readData);
            String uname = readJSON.getString("username");
            String token = readJSON.getString("token");
            int admin = readJSON.getInt("admin");
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bundle bundle = new Bundle();
                    bundle.putString("username",uname);
                    bundle.putString("auth_token",token);
                    if(admin==1){
                        navController.navigate(R.id.action_loginFragment_to_adminHomeFragment,bundle);
                    }else {
                        navController.navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                    }
                }
            });
        }catch (Exception e) {
            Log.e("Hachiman","FileRead",e);
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button_list.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.GONE);
                }
            });
        }
    }
    public void runOutside(int res){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navController.navigate(res);
            }
        });
    }
}