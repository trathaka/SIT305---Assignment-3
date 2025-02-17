package com.example.assignment2.KoreanNavigation;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.assignment2.LoginPage;
import com.example.assignment2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class KoreanProfileFragment extends Fragment {

    GoogleSignInClient mGoogleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.userImage);
        TextView name = (TextView) view.findViewById(R.id.userName);
        TextView email = (TextView) view.findViewById(R.id.userEmail);
        TextView id = (TextView) view.findViewById(R.id.userID);
        Button signOut = (Button) view.findViewById(R.id.sign_out_button);
        final MediaPlayer mp = MediaPlayer.create(getActivity(),R.raw.sign);
        // onClickListener for sign-out button
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.sign_out_button:
                        signOut();
                        mp.start();
                        break;
                }
            }
        });

        // Configure sign-in to request the user's name, ID, email address and profile picture to include within DEFAULT_SIGN_IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // Use the GoogleSignIn.getLastSignedInAccount to retrieve profile information for a signed in user
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();

            // Bind textView with elements retrieved
            name.setText(personName);
            email.setText(personEmail);
            id.setText(personId);

            // Use Glide library to display user's picture in the imageView
            Glide.with(this).load(String.valueOf(personPhoto)).into(imageView);
        }
        return view;
    }
    // Google sign-out method
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getActivity(), LoginPage.class);
                        startActivity(intent);
                        Toast.makeText(getContext(),"Signed out successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
