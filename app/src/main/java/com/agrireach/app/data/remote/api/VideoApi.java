package com.agrireach.app.data.remote.api;

import com.agrireach.app.data.remote.dto.VideoResponse;
import com.agrireach.app.data.remote.dto.VideoUploadRequest;
import com.agrireach.app.data.remote.dto.VideoUploadResponse;
import com.agrireach.app.data.remote.dto.VideoDownloadResponse;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VideoApi {
    @POST("api/videos/upload-request")
    Call<VideoUploadResponse> createUploadRequest(@Body VideoUploadRequest request);

    @POST("api/videos/{videoId}/confirm-upload")
    Call<Void> confirmUpload(@Path("videoId") String videoId);

    @GET("api/videos/admin")
    Call<List<VideoResponse>> getAdminVideos();

    @GET("api/videos/{videoId}/download-url")
    Call<VideoDownloadResponse> getDownloadUrl(
            @Path("videoId") String videoId
    );

}