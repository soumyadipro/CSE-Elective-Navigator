package com.infsml.hachiman.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infsml.hachiman.R;
import com.infsml.hachiman.Utility;
import com.infsml.hachiman.databinding.FragmentAdminAddEventBinding;
import com.infsml.hachiman.databinding.FragmentAdminHomeBinding;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminAddEventFragment extends Fragment {
    FragmentAdminAddEventBinding binding;
    NavController navController;
    String username;
    String auth_token;
    Bundle bundle;
    public AdminAddEventFragment() {
    }
    public static AdminAddEventFragment newInstance(String param1, String param2) {
        AdminAddEventFragment fragment = new AdminAddEventFragment();
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
        binding = FragmentAdminAddEventBinding.inflate(inflater,container,false);
        navController = Navigation.findNavController(getActivity(),R.id.fragmentContainerView);
        bundle = getArguments();
        username=bundle.getString("username");
        auth_token=bundle.getString("auth_token");
        binding.addBtn.setOnClickListener(v -> {
            binding.addBtn.setEnabled(false);
            binding.addBtn.setClickable(false);
            binding.addBtn.setText("Adding..");
            (new Thread(){
                @Override
                public void run(){
                    try{
                        JSONObject payload = new JSONObject();
                        payload.put("username",username);
                        payload.put("auth_token",auth_token);
                        JSONArray details = new JSONArray();
                        JSONArray element = new JSONArray();
                        element.put(binding.codeInp.getText().toString());
                        element.put(binding.nameInp.getText().toString());
                        element.put(Integer.parseInt(binding.semInp.getText().toString()));
                        details.put(element);
                        payload.put("details",details);
                        JSONObject res = Utility.postJSON(
                                Utility.api_base+"/add-event-admin",
                                payload.toString()
                        );
                        requireActivity().runOnUiThread(()->{
                            navController.navigate(R.id.action_adminAddEventFragment_to_adminHomeFragment,bundle);
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                        requireActivity().runOnUiThread(()->{
                            binding.addBtn.setEnabled(true);
                            binding.addBtn.setClickable(true);
                            binding.addBtn.setText("Retry");
                        });
                    }
                }
            }).start();
        });
        return binding.getRoot();
    }
}