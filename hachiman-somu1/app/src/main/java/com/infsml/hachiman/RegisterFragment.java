package com.infsml.hachiman;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.infsml.hachiman.databinding.FragmentRegisterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterFragment extends Fragment {
    NavController navController;
    View button_list;
    String available;
    View spinner;
    String username = null;
    String auth_token = null;
    String event = null;

    int semester;
    String section;
    String name;
    int admin;
    String url_link_default = Utility.api_base + "/register";
    String[] prev_names;
    String[] prev_codes;
    String p_course;
    int p_index=0;
    List<String> first_options;
    String first_option;
    List<String> second_options;
    String second_option;
    List<String> third_options;
    String third_option;

    String[] available_codes;
    Bundle bundle;
    Bundle new_bundle;

    FragmentRegisterBinding binding;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        ResultTransmission.result=0;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("Hachiman","Creation");
        binding = FragmentRegisterBinding.inflate(inflater);
        //View fragment_view = inflater.inflate(R.layout.fragment_register, container, false);
        View fragment_view = binding.getRoot();
        navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
        bundle = getArguments();
        if (bundle == null) {
            navController.navigate(R.id.action_registerFragment_to_loginFragment);
            return fragment_view;
        }
        username = bundle.getString("username");
        auth_token = bundle.getString("auth_token");
        event = bundle.getString("event");
        name = bundle.getString("name",name);
        semester = bundle.getInt("semester",semester);
        section = bundle.getString("section",section);
        admin = bundle.getInt("admin",admin);
        Log.i("Hachiman", bundle.toString());
        if (username == null || auth_token == null) {
            navController.navigate(R.id.action_registerFragment_to_loginFragment);
            return fragment_view;
        }
        if (event == null) {
            navController.navigate(R.id.action_registerFragment_to_homeFragment, bundle);
            return fragment_view;
        }
        binding.heading.setText(event);
        binding.usnVal.setText(username);
        binding.semesterVal.setText(""+semester);
        binding.sectionVal.setText(section);
        binding.nameVal.setText(name);
        binding.btnLyt.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);

        fetchOptionData(()->{
            binding.prevCourseLyt.setVisibility(View.VISIBLE);
            JSONArray jsonArray=optionData.optJSONArray("prev");
            assert jsonArray != null;
            prev_codes=new String[jsonArray.length()];
            prev_names=new String[jsonArray.length()];
            for(int i=0;i<jsonArray.length();i++){
                prev_codes[i]=jsonArray.optJSONObject(i).optString("code");
                prev_names[i]= prev_codes[i]+"-"+jsonArray.optJSONObject(i).optString("name");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item,prev_names);
            binding.prevCourseVal.setAdapter(adapter);
            binding.prevCourseVal.setSelection(p_index);
            binding.prevCourseVal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(ResultTransmission.result==0) {
                        JSONArray currentData = optionData.optJSONArray("data");
                        Log.i("Hachiman", "Spinner Item Select");
                        p_course = prev_codes[position];
                        p_index = position;
                        resetProgress(1);
                        first_options = new ArrayList<>();
                        for (int i = 0; i < currentData.length(); i++) {
                            JSONObject cObject = currentData.optJSONObject(i);
                            if (p_course.equals(cObject.optString("code"))) continue;
                            if ("None".equals(cObject.optString("requirement")) || p_course.equals(cObject.optString("requirement")))
                                first_options.add(cObject.toString());
                        }
                        binding.firstChoiceLyt.setVisibility(View.VISIBLE);
                        binding.firstChoiceVal.setVisibility(View.GONE);
                    }
                    ResultTransmission.result=0;
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        binding.firstChoiceChoose.setOnClickListener(v->{
            ResultTransmission.result=0;
            Bundle dat = new Bundle();
            dat.putStringArrayList("options",(ArrayList<String>) first_options);
            dat.putInt("require",1);
            navController.navigate(R.id.action_registerFragment_to_optionFragment,dat);
        });
        binding.secondChoiceChoose.setOnClickListener(v->{
            ResultTransmission.result=0;
            Bundle dat = new Bundle();
            dat.putStringArrayList("options",(ArrayList<String>) second_options);
            dat.putInt("require",2);
            navController.navigate(R.id.action_registerFragment_to_optionFragment,dat);
        });
        binding.thirdChoiceChoose.setOnClickListener(v->{
            ResultTransmission.result=0;
            Bundle dat = new Bundle();
            dat.putStringArrayList("options",(ArrayList<String>) third_options);
            dat.putInt("require",3);
            navController.navigate(R.id.action_registerFragment_to_optionFragment,dat);
        });

        binding.registerBtn.setOnClickListener(v->{
            binding.btnLyt.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            (new Thread(){
                @Override
                public void run(){
                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("username", username);
                        payload.put("auth_token",auth_token);
                        payload.put("event",event);
                        payload.put("first_option",first_option);
                        payload.put("second_option",second_option);
                        payload.put("third_option",third_option);
                        JSONObject res = Utility.postJSON(Utility.api_base + "/register", payload.toString());
                        requireActivity().runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                navController.popBackStack();
                            }
                        });
                    }catch (Exception e){
                        Log.e("Hachiman","Signup Error",e);
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.btnLyt.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }).start();
        });
        binding.cancelBtn.setOnClickListener(v -> {
            navController.popBackStack();
        });
        try {
            analyzeResultTransmission();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fragment_view;
    }

    public void analyzeResultTransmission() throws JSONException {
        Log.i("Hachiman","ResumedSuccessfully"+ResultTransmission.result);
        super.onResume();
        if(ResultTransmission.result==1){
            resetProgress(3);
            first_option=ResultTransmission.value;
            second_options = new ArrayList<>();
            for(int i=0;i<first_options.size();i++){
                JSONObject jsonObject = new JSONObject(first_options.get(i));
                if(first_option.equals(jsonObject.optString("code")))continue;
                second_options.add(first_options.get(i));
            }
            binding.firstChoiceVal.setVisibility(View.VISIBLE);
            binding.firstChoiceVal.setText(first_option);
            binding.secondChoiceLyt.setVisibility(View.VISIBLE);
            binding.secondChoiceVal.setVisibility(View.GONE);
        }else if(ResultTransmission.result==2){
            resetProgress(4);
            second_option=ResultTransmission.value;
            third_options = new ArrayList<>();
            for(int i=0;i<second_options.size();i++){
                JSONObject jsonObject = new JSONObject(second_options.get(i));
                if(second_option.equals(jsonObject.optString("code")))continue;
                third_options.add(second_options.get(i));
            }
            binding.thirdChoiceLyt.setVisibility(View.VISIBLE);
            binding.firstChoiceVal.setVisibility(View.VISIBLE);
            binding.firstChoiceVal.setText(first_option);
            binding.secondChoiceVal.setVisibility(View.VISIBLE);
            binding.secondChoiceVal.setText(second_option);
            binding.thirdChoiceVal.setVisibility(View.GONE);
        }else if(ResultTransmission.result==3){
            third_option = ResultTransmission.value;
            binding.firstChoiceVal.setVisibility(View.VISIBLE);
            binding.firstChoiceVal.setText(first_option);
            binding.secondChoiceVal.setVisibility(View.VISIBLE);
            binding.secondChoiceVal.setText(second_option);
            binding.thirdChoiceVal.setVisibility(View.VISIBLE);
            binding.thirdChoiceVal.setText(third_option);
            binding.registerBtn.setVisibility(View.VISIBLE);
        }
        //ResultTransmission.result=0;
    }

    public void resetProgress(int level){
        switch (level){
            case 0:
                binding.prevCourseLyt.setVisibility(View.GONE);
            case 1:
                binding.firstChoiceLyt.setVisibility(View.GONE);
            case 2:
                binding.secondChoiceLyt.setVisibility(View.GONE);
            case 3:
                binding.thirdChoiceLyt.setVisibility(View.GONE);
        }
        binding.registerBtn.setVisibility(View.GONE);
    }

    JSONObject optionData = null;

    public void fetchOptionData(Runnable runnable) {
        (new Thread() {
            @Override
            public void run() {
            try {
                if(optionData==null) {
                    JSONObject payload = new JSONObject();
                    payload.put("username", username);
                    payload.put("auth_token", auth_token);
                    payload.put("event", event);
                    optionData = Utility.postJSON(
                            Utility.api_base + "/get-option",
                            payload.toString()
                    );
                }
                requireActivity().runOnUiThread(runnable);
            } catch (Exception e) {
                Log.e("Hachiman", "Error", e);
            }
            }
        }).start();
    }
}