package com.example.veganapp;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.MODE_PRIVATE;

public class SupportClasses {

    static class StringFormer
    {
        static final int ONE_THOUSAND = 1000;
        static final int ONE_MILLION = 1000000;

        static String formStringValueFromInt(int value)
        {
            if(value < ONE_THOUSAND)
                return Integer.toString(value);
            if(value < ONE_MILLION)
                return Integer.toString(value / ONE_THOUSAND) + "K";
            return Integer.toString(value / ONE_MILLION) + "M";
        }

        static String formIngredientsList(JsonClasses.Recipe recipe)
        {
            StringBuilder sb = new StringBuilder();
            for (JsonClasses.Ingredient ingredient : recipe.getIngredients())
            {
                sb.append(ingredient.getName());
                if (ingredient.getAmount() != null)
                    sb.append(" - " + ingredient.getAmount());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    class FirebaseInfoLoader
    {
        static final int ONE_MEGABYTE = 1024 * 1024;
        MainActivity parentRef;

        public FirebaseInfoLoader(MainActivity parentRef) {
            this.parentRef = parentRef;
        }

        JsonClasses.MainJson getInfoFromFirebase(final StorageReference targetRef) {

            JsonClasses.MainJson RnR = null;
            targetRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        FileOutputStream fout = parentRef.openFileOutput(targetRef.getName(), MODE_PRIVATE);
                        fout.write(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });

            try {
                FileInputStream fin = parentRef.openFileInput(targetRef.getName());
                Gson gson = new Gson();
                BufferedReader br = new BufferedReader(new InputStreamReader(fin));

                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = br.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                RnR = gson.fromJson(sb.toString(), JsonClasses.MainJson.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return RnR;
        }

        void putInfoIntoFirebase(final StorageReference targetRef, JsonClasses.MainJson RnR) {


            Gson gson = new Gson();
            String r_n_r = gson.toJson(RnR);
            byte[] bytes = r_n_r.getBytes();

            targetRef.putBytes(bytes).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot snapshot) {

                }
            });


            try {
                FileOutputStream fout = parentRef.openFileOutput(targetRef.getName(), MODE_PRIVATE);
                fout.write(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
