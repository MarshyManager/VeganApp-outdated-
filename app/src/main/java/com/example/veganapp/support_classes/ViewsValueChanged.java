package com.example.veganapp.support_classes;

import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class ViewsValueChanged implements ValueEventListener {
    String viewsNum;
    SharedPreferences shp;

    public ViewsValueChanged(String viewsNum, SharedPreferences shp) {
        this.viewsNum = viewsNum;
        this.shp = shp;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        SharedPreferences.Editor editor = shp.edit();
        Integer value = dataSnapshot.getValue(Integer.class);
        dataSnapshot.getRef().setValue(value + shp.getInt(viewsNum, 0));
        editor.putInt(viewsNum, 0);
        editor.apply();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
