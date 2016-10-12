package com.siemens.parkingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class Settings extends Fragment {
   @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        View settings = inflater.inflate(R.layout.settings_frag, container, false);
        return settings;

   	}
}
