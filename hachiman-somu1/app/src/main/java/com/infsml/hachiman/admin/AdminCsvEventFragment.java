package com.infsml.hachiman.admin;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.infsml.hachiman.R;
import com.infsml.hachiman.databinding.FragmentAdminCsvEventBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

public class AdminCsvEventFragment extends Fragment {
    FragmentAdminCsvEventBinding binding;
    NavController navController;
    String username;
    String auth_token;
    Bundle bundle;
    public AdminCsvEventFragment() {
    }

    public static AdminCsvEventFragment newInstance(String param1, String param2) {
        AdminCsvEventFragment fragment = new AdminCsvEventFragment();
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
        binding = FragmentAdminCsvEventBinding.inflate(inflater,container,false);
        navController = Navigation.findNavController(getActivity(),R.id.fragmentContainerView);
        bundle = getArguments();
        username=bundle.getString("username");
        auth_token=bundle.getString("auth_token");
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                result -> {
                    loadCSV(result);
                });
        binding.selectBtn.setOnClickListener(v->{
            mGetContent.launch("*/*");
        });
        return binding.getRoot();
    }
    public void loadCSV(Uri uri){
        (new Thread(){
            @Override
            public void run(){
                try {
                    InputStream fis = requireContext().getContentResolver().openInputStream(uri);
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i;(i=fis.read())!=-1;){
                        stringBuilder.append((char)i);
                    }
                    String file_content = stringBuilder.toString();
                    requireActivity().runOnUiThread(()->{
                        TableRow tableRow = new TableRow(requireContext());
                        //for(int i=0;i<10;i++){
                            TextView textView = new TextView(requireContext());
                            textView.setText(file_content);
                            tableRow.addView(textView);
                        //}
                        binding.mainTable.addView(tableRow);
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}