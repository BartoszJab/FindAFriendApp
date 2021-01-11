package com.uwb.findafriendapp.fragments;

import com.uwb.findafriendapp.Notifications.MyResponse;
import com.uwb.findafriendapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA4cuWHLw:APA91bHRmz8mhFkDShoRt3zVpKFpThJQPyquj7Q1pC5xdS2bMd1M8h96whusBkCNX9nmMHMp5AwIQuNAgzimu5xYb6c74BkSHHmwbWbcz0NVdlNfYwxmMQHRHZNoHgempVA6mlChC8wi"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
