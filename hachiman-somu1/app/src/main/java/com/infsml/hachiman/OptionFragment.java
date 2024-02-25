package com.infsml.hachiman;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.infsml.hachiman.databinding.OptionListElementBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OptionFragment extends Fragment {
    NavController navController;
    RecyclerView recyclerView;
    String username = null;
    String auth_token = null;
    String event = null;
    Bundle bundle;
    MenuProvider _this;
    int require;
    boolean alternate_option;

    public OptionFragment() {
    }

    public static OptionFragment newInstance(String param1, String param2) {
        OptionFragment fragment = new OptionFragment();
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
        View fragment_view = inflater.inflate(R.layout.fragment_option, container, false);
        navController = Navigation.findNavController(requireActivity(), R.id.fragmentContainerView);
        bundle = getArguments();
        List<String> items;
        Log.i("Hachiman",bundle.toString());
        items = bundle.getStringArrayList("options");
        if(items==null)items=new ArrayList<String>();
        require = bundle.getInt("require");
        ResultTransmission.result=ResultTransmission.prev;
        recyclerView = fragment_view.findViewById(R.id.options_recycler_view);
        recyclerView.setAdapter(new OptionListAdapter(items));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return fragment_view;
    }

    class OptionListAdapter extends RecyclerView.Adapter<OptionListAdapter.ViewHolder> {
        List<JSONObject> items;

        class ViewHolder extends RecyclerView.ViewHolder {
            String value;
            String available;
            OptionListElementBinding elementBinding;
            boolean expanded;

            public ViewHolder(@NonNull OptionListElementBinding elementBinding) {
                super(elementBinding.getRoot());
                this.elementBinding = elementBinding;
                expanded = true;
                elementBinding.parentLayout.setOnClickListener(v -> {
                    if (expanded) elementBinding.expandLayout.setVisibility(View.VISIBLE);
                    else elementBinding.expandLayout.setVisibility(View.GONE);
                    expanded = !expanded;
                });
                elementBinding.button7.setOnClickListener(v -> {
                    ResultTransmission.result=require;
                    ResultTransmission.value = value;
                    ResultTransmission.prev=require;
                    navController.popBackStack();
                });
            }
            public void putText(String text){
                elementBinding.title.setText(text);
            }
        }

        public OptionListAdapter(List<String> items) {
            this.items=new ArrayList<>();
            for(int i=0;i<items.size();i++){
                try {
                    this.items.add(new JSONObject(items.get(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_list_element,parent,false);
            OptionListElementBinding listElementBinding = OptionListElementBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
            return new ViewHolder(listElementBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            JSONObject jsonObject = items.get(position);
            String code=jsonObject.optString("code");
            holder.elementBinding.title.setText(code+"-"+jsonObject.optString("name"));
            holder.value=code;
            holder.elementBinding.code.setText(code);
            holder.elementBinding.name.setText(jsonObject.optString("name"));
            holder.elementBinding.available.setText(""+jsonObject.optInt("availability"));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}