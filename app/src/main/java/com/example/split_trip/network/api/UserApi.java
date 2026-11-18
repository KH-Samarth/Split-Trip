
package com.example.split_trip.network.api;

import com.example.split_trip.network.models.LoginRequest;
import com.example.split_trip.network.models.UserRequest;
import com.example.split_trip.network.models.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {

    @POST("api/users")
    Call<UserResponse> registerUser(@Body UserRequest request);

    @POST("api/users/login")
    Call<UserResponse> loginUser(@Body LoginRequest request);

}
