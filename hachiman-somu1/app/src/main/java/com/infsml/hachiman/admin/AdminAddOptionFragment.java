package com.infsml.hachiman.admin;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.infsml.hachiman.R;
import com.infsml.hachiman.Utility;
import com.infsml.hachiman.databinding.FragmentAdminAddEventBinding;
import com.infsml.hachiman.databinding.FragmentAdminAddOptionBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdminAddOptionFragment extends Fragment {
    FragmentAdminAddOptionBinding binding;
    NavController navController;
    String username;
    String auth_token;
    String event;
    Bundle bundle;
    JSONArray prev_courses;
    public AdminAddOptionFragment() {
    }

    public static AdminAddOptionFragment newInstance(String param1, String param2) {
        AdminAddOptionFragment fragment = new AdminAddOptionFragment();
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

        binding = FragmentAdminAddOptionBinding.inflate(inflater,container,false);
        navController = Navigation.findNavController(getActivity(),R.id.fragmentContainerView);
        bundle = getArguments();
        username=bundle.getString("username");
        auth_token=bundle.getString("auth_token");
        event = bundle.getString("event");
        try {
            prev_courses = new JSONArray(bundle.getString("prev"));
        } catch (JSONException e) {
            e.printStackTrace();
            prev_courses = new JSONArray();
        }
        String[] prev_array = new String[prev_courses.length()+1];
        prev_array[0]="None";
        for(int i = 0 ;i<prev_courses.length();i++){
            JSONObject prev_course = prev_courses.optJSONObject(i);
            prev_array[i+1]=prev_course.optString("code")+"-"+prev_course.optString("name");
        }
        binding.spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    binding.semInp.setVisibility(View.GONE);
                    binding.requiredPrev.setVisibility(View.GONE);
                    binding.textView3.setVisibility(View.GONE);
                }else{
                    binding.semInp.setVisibility(View.VISIBLE);
                    binding.requiredPrev.setVisibility(View.VISIBLE);
                    binding.textView3.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item,prev_array);
        binding.requiredPrev.setAdapter(adapter);
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
                        if(binding.spinner3.getSelectedItemPosition()==0) {
                            payload.put("course_type", "previous");
                            element.put(binding.codeInp.getText().toString());
                            element.put(event);
                            element.put(binding.nameInp.getText().toString());
                        }else{
                            payload.put("course_type", "current");
                            element.put(binding.codeInp.getText().toString());
                            element.put(event);
                            element.put(binding.nameInp.getText().toString());
                            element.put(Integer.parseInt(binding.semInp.getText().toString()));
                            int requirement_pos = binding.requiredPrev.getSelectedItemPosition();
                            if(requirement_pos==0)element.put("None");
                            else element.put(prev_courses.getJSONObject(requirement_pos-1).optString("code"));
                        }
                        details.put(element);
                        payload.put("details",details);
                        JSONObject res = Utility.postJSON(
                                Utility.api_base+"/add-option-admin",
                                payload.toString()
                        );
                        requireActivity().runOnUiThread(()->{
                            navController.navigate(R.id.action_adminAddOptionFragment_to_adminOptionFragment,bundle);
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