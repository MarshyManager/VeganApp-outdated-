package com.example.veganapp.support_classes;

import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class LikeValueChanged implements ValueEventListener {
    String onlineLike;
    String offlineLike;
    SharedPreferences shp;

    public LikeValueChanged(String offlineLike, String onlineLike, SharedPreferences shp) {
        this.onlineLike = onlineLike;
        this.offlineLike = offlineLike;
        this.shp = shp;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        SharedPreferences.Editor editor = shp.edit();
        Integer value = dataSnapshot.getValue(Integer.class);
        if (shp.getBoolean(offlineLike, false) && !shp.getBoolean(onlineLike, false)) {
            dataSnapshot.getRef().setValue(++value);
            editor.putBoolean(onlineLike, true);
        } else if (!shp.getBoolean(offlineLike, false) && shp.getBoolean(onlineLike, false)) {
            dataSnapshot.getRef().setValue(--value);
            editor.putBoolean(onlineLike, false);
        }
        editor.apply();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
