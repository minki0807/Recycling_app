package com.example.recycling_app.util;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.recycling_app.R;
import com.example.recycling_app.dto.ProfileDTO;
import com.example.recycling_app.service.CommunityApiService;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A utility class to handle loading user profile information from the backend and updating UI.
 */
public class ProfileLoader {

    private static final String TAG = "ProfileLoader";

    public interface OnProfileLoadedCallback {
        void onProfileLoaded(ProfileDTO profile);
    }

    /**
     * Loads the user's profile information and updates the provided TextView and ImageView.
     * @param context The context, usually an Activity.
     * @param textView The TextView to display the user's nickname. Can be null.
     * @param imageView The ImageView to display the user's profile picture. Can be null.
     * @param user The FirebaseUser object of the current user.
     */
    public static void loadProfile(Context context, TextView textView, ImageView imageView, FirebaseUser user, OnProfileLoadedCallback callback) {
        if (user == null) {
            Log.e(TAG, "FirebaseUser is null, cannot load profile.");
            if (callback != null) {
                callback.onProfileLoaded(null);
            }
            return;
        }

        CommunityApiService apiService = CommunityApiService.getInstance();
        apiService.getProfile(user.getUid(), new Callback<ProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDTO> call, @NonNull Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileDTO profile = response.body();
                    if (textView != null) {
                        textView.setText(profile.getNickname());
                    }
                    if (imageView != null) {
                        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
                            Glide.with(context).load(profile.getProfileImageUrl()).into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.ic_person);
                        }
                    } if (callback != null) {
                        callback.onProfileLoaded(profile);
                    }
                } else {
                    Log.e(TAG, "Failed to load profile: " + response.code() + " " + response.message());
                    Toast.makeText(context, "프로필 정보 로드 실패", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onProfileLoaded(null);
                    }
            }

            }

            @Override
            public void onFailure(@NonNull Call<ProfileDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Profile load network error: ", t);
                Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onProfileLoaded(null);
                }
            }
        });
    }

    /**
     * Loads the profile for a specific user ID. Useful for displaying other users' profiles.
     * @param context The context, usually an Activity.
     * @param textView The TextView to display the user's nickname. Can be null.
     * @param imageView The ImageView to display the user's profile picture. Can be null.
     * @param uid The UID of the user whose profile to load.
     */
    public static void loadProfileByUid(Context context, TextView textView, ImageView imageView, String uid) {
        CommunityApiService apiService = CommunityApiService.getInstance();
        apiService.getProfile(uid, new Callback<ProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<ProfileDTO> call, @NonNull Response<ProfileDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileDTO profile = response.body();
                    if (textView != null) {
                        textView.setText(profile.getNickname());
                    }
                    if (imageView != null) {
                        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
                            Glide.with(context).load(profile.getProfileImageUrl()).into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.ic_person);
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load profile for UID " + uid + ": " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Profile load network error for UID " + uid, t);
            }
        });
    }
}
