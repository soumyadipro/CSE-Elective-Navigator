package com.infsml.hachiman;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.infsml.hachiman.databinding.HomeListElement2Binding;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeFragment extends Fragment{
    NavController navController;
    RecyclerView recyclerView;
    String username=null;
    String auth_token=null;
    Bundle bundle=null;
    int semester=-1;
    String section=null;
    String name=null;
    int admin=-1;
    JSONObject event_data;
    public HomeFragment() {
    }
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View fragment_view =inflater.inflate(R.layout.fragment_home, container, false);
        navController = Navigation.findNavController(getActivity(),R.id.fragmentContainerView);
        bundle = getArguments();
        if(bundle==null){
            navController.navigate(R.id.action_homeFragment_to_loginFragment);
            return fragment_view;
        }
        username=bundle.getString("username");
        auth_token=bundle.getString("auth_token");
        if(username==null||auth_token==null){
            navController.navigate(R.id.action_homeFragment_to_loginFragment);
            return fragment_view;
        }
        fetchUserAttributes(()->{
            fetchUserData(()->{
                HomeListAdapter adapter = (HomeListAdapter) recyclerView.getAdapter();
                adapter.loadData(event_data);
                Log.i("Hachiman",username);
            });
            MaterialToolbar toolbar = requireActivity().findViewById(R.id.materialToolbar);
            toolbar.setTitle(username);
        });
        recyclerView = fragment_view.findViewById(R.id.home_recycler_view);
        recyclerView.setAdapter(new HomeListAdapter(navController,bundle));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Button sign_out = fragment_view.findViewById(R.id.signout_btn);
        sign_out.setOnClickListener(v -> {
            Log.i("AuthQuickStart","signing out XD");
            Button b = (Button) v;
            b.setText(R.string.signing_out);
            b.setClickable(false);
            b.setEnabled(false);
            (new Thread() {
                @Override
                public void run() {
                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("username", username);
                        payload.put("auth_token", auth_token);
                        JSONObject jsonObject = Utility.postJSON(
                                Utility.api_base + "/logout",
                                payload.toString()
                        );
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bundle logout = new Bundle();
                                logout.putBoolean("logout",true);
                                navController.navigate(R.id.action_homeFragment_to_loginFragment,logout);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("Hachiman","Logout Error",e);
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                b.setText(R.string.signout);
                                b.setClickable(true);
                                b.setEnabled(true);
                            }
                        });
                    }
                }
            }).start();
        });
        return fragment_view;
    }

    public void fetchUserAttributes(Runnable runnable){
        (new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("username",username);
                    payload.put("auth_token",auth_token);
                    JSONObject jsonObject = Utility.postJSON(
                            Utility.api_base+"/user-attr",
                            payload.toString()
                    );
                    semester = jsonObject.getInt("semester");
                    section = jsonObject.getString("section");
                    admin = jsonObject.getInt("admin");
                    name=jsonObject.getString("name");
                    requireActivity().runOnUiThread(runnable);
                }catch (Exception e){
                    Log.e("Hachiman","Detail Error",e);
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle logout = new Bundle();
                            logout.putBoolean("logout",true);
                            navController.navigate(R.id.action_homeFragment_to_loginFragment,logout);
                        }
                    });
                }
            }
        }).start();
    }
    public void fetchUserData(Runnable runnable){
        (new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("username",username);
                    payload.put("auth_token",auth_token);
                    payload.put("semester",semester);
                    event_data = Utility.postJSON(
                        Utility.api_base+"/get-event",
                        payload.toString()
                    );
                    //chViewOutside(jsonObject);
                    //event_data = jsonObject.getJSONArray("data");
                    requireActivity().runOnUiThread(runnable);
                }catch (Exception e){
                    Log.e("Hachiman","Data Error",e);
                }
            }
        }).start();
    }
    public class HomeListAdapter extends RecyclerView.Adapter{
        JSONArray items;
        JSONArray registered;
        NavController controller;
        Bundle bundle;

        class ItemViewHolder extends RecyclerView.ViewHolder{
            TextView mainText;
            String value;
            public ItemViewHolder(
                    @NonNull @NotNull View itemView,
                    NavController controller,
                    Bundle bundle) {
                super(itemView);
                mainText = itemView.findViewById(R.id.textView);
                View view = itemView.findViewById(R.id.parent_layout);
                view.setOnClickListener(v->{
                    bundle.putString("event",value);
                    bundle.putString("name",name);
                    bundle.putInt("semester",semester);
                    bundle.putString("section",section);
                    bundle.putInt("admin",admin);
                    controller.navigate(R.id.action_homeFragment_to_registerFragment,bundle);
                });
            }
            public void putText(String text){
                mainText.setText(text);
            }
        }
        class RegViewHolder extends RecyclerView.ViewHolder{
            HomeListElement2Binding binding;
            public RegViewHolder(HomeListElement2Binding binding){
                super(binding.getRoot());
                this.binding=binding;
            }
            public void setData(){
                int position = getAdapterPosition()-items.length();
                if(position<0)return;
                JSONObject object = registered.optJSONObject(position);
                binding.event.setText(object.optString("e_code"));
                binding.firstChoiceVal.setText(object.optString("first_option"));
                binding.secondChoiceVal.setText(object.optString("second_option"));
                binding.thirdChoiceVal.setText(object.optString("third_option"));
            }
        }
        public HomeListAdapter(NavController controller, Bundle bundle){
            items = new JSONArray();
            registered = new JSONArray();
            this.controller=controller;
            this.bundle=bundle;
        }
        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if(viewType==0) {
                viewHolder = new ItemViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.home_list_element, parent, false)
                        , controller, new Bundle(bundle));
            }else{
                viewHolder = new RegViewHolder(HomeListElement2Binding.inflate(
                        LayoutInflater.from(parent.getContext()),parent,false
                ));
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
            int type =getItemViewType(position);
            if(type==0) {
                ItemViewHolder viewHolder = (ItemViewHolder) holder;
                JSONObject jsonObject = items.optJSONObject(position);
                String code = jsonObject.optString("code");
                viewHolder.putText(code);
                viewHolder.value = code;
            }else if(type ==1){
                RegViewHolder viewHolder = (RegViewHolder) holder;
                viewHolder.setData();
            }
        }
        public void loadData(JSONObject jsonArray){
            items=jsonArray.optJSONArray("data");
            registered=jsonArray.optJSONArray("registered");
            notifyDataSetChanged();
        }
        @Override
        public int getItemCount() {
            return items.length()+registered.length();
        }
        @Override
        public int getItemViewType(int position){
            if(position>=items.length())return 1;
            return 0;
        }
    }
}