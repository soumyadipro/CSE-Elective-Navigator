package com.infsml.hachiman.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infsml.hachiman.R;
import com.infsml.hachiman.Utility;
import com.infsml.hachiman.databinding.AdminHomeElementBinding;
import com.infsml.hachiman.databinding.FragmentAdminOptionBinding;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminOptionFragment extends Fragment {
    NavController navController;
    Bundle bundle;
    String username = null;
    String auth_token = null;
    String event = null;

    public AdminOptionFragment() {
    }
    public static AdminOptionFragment newInstance(String param1, String param2) {
        AdminOptionFragment fragment = new AdminOptionFragment();
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
    FragmentAdminOptionBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminOptionBinding.inflate(inflater,container,false);
        navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
        bundle = getArguments();
        assert bundle!=null;
        username = bundle.getString("username");
        auth_token = bundle.getString("auth_token");
        event = bundle.getString("event");
        binding.addBtn.setOnClickListener(v->{
            if(optionData!=null) {
                Bundle bundle1 = new Bundle(bundle);
                bundle1.putString("prev",optionData.optJSONArray("prev").toString());
                navController.navigate(R.id.action_adminOptionFragment_to_adminAddOptionFragment, bundle1);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        final OptionAdapter optionAdapter = new OptionAdapter();
        binding.recyclerView.setAdapter(optionAdapter);
        fetchOptionData(()->{
            optionAdapter.updateData();
        });
        return binding.getRoot();
    }
    JSONObject optionData;
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
                                Utility.api_base + "/get-option-admin",
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
    public class OptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        public class VHtitle extends RecyclerView.ViewHolder{
            TextView textView;
            public VHtitle(TextView textView){
                super(textView);
                this.textView=textView;
            }
        }
        public class VHoption extends RecyclerView.ViewHolder{
            AdminHomeElementBinding binding1;
            boolean btn_vis = false;
            public VHoption(AdminHomeElementBinding binding1){
                super(binding1.getRoot());
                this.binding1=binding1;
                binding1.coursesBtn.setVisibility(View.GONE);
                binding1.btnLyt.setVisibility(View.GONE);
                binding1.parentLayout.setOnClickListener(v->{
                    btn_vis = !btn_vis;
                    if(btn_vis)binding1.btnLyt.setVisibility(View.VISIBLE);
                    else binding1.btnLyt.setVisibility(View.GONE);
                });
            }
        }
        JSONArray prev;
        JSONArray data;
        public OptionAdapter(){
            prev = new JSONArray();
            data = new JSONArray();
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            if(viewType==0||viewType==2){
                holder = new VHtitle(new TextView(requireContext()));
            }else{
                AdminHomeElementBinding binding1 = AdminHomeElementBinding.inflate(
                        LayoutInflater.from(parent.getContext()),parent,false
                );
                holder = new VHoption(binding1);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            if(viewType==0){
                VHtitle h = (VHtitle) holder;
                h.textView.setText("Previous Courses");
            }else if(viewType==1){
                VHoption p = (VHoption) holder;
                JSONObject object= prev.optJSONObject(position-1);
                p.binding1.title.setText(object.optString("code")+" - "+object.optString("name"));
            }else if(viewType==2){
                VHtitle h = (VHtitle) holder;
                h.textView.setText("Current Choices");
            }
            else{
                VHoption p = (VHoption) holder;
                JSONObject object = data.optJSONObject(position-prev.length()-2);
                p.binding1.title.setText(object.optString("code")+" - "+object.optString("name") +" ("+object.optString("availability")+")");
            }
        }

        @Override
        public int getItemCount() {
            return prev.length()+data.length()+2;
        }
        @Override
        public int getItemViewType(int position){
            if(position==0)return 0;
            if(position<prev.length()+1)return 1;
            if(position==(prev.length()+1))return 2;
            return 3;
        }
        public void updateData(){
            prev = optionData.optJSONArray("prev");
            data = optionData.optJSONArray("data");
            notifyDataSetChanged();
        }
    }
}