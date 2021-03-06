package ksu.tourpedia.identify;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import ksu.tourpedia.identify.CloudSight.ImageIdentifying;
import ksu.tourpedia.identify.imgurmodel.ImageResponse;
import ksu.tourpedia.identify.imgurmodel.Upload;
import ksu.tourpedia.identify.services.UploadService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by DELL on 29/03/16.
 */
public class UploadStart {

    Context mContext;
    File image;
    Upload upload; // Upload object containing image and meta data

    public UploadStart(Context context){

       mContext=context;
       image = ImageHandler.imageFile;//TODO: find a better way to pass the file since this looks unreliable.
    }

    public void uploadImage() {
    /*
      Create the @Upload object
     */
        if (image == null){
            Log.d("debug", "The image is empty");
            return;}
        createUpload(image);

    /*
      Start upload
     */
        new UploadService(mContext).Execute(upload, new UiCallback());
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
        // upload.title = uploadTitle.getText().toString();
        // upload.description = uploadDesc.getText().toString();
    }


    /*public static void showResults(){
        Intent intent=new Intent(context,WikiJsoup.class);
        startActivity(intent);
    }*/
    public static void showResults(String result){


        // message.setText("Status: "+result);

        if(VariablesAndConstants.isFromGlass){

            GlassActivity.sendToGlass(result);

            VariablesAndConstants.isFromGlass=false;
        }
    }
    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            // clearInput();

            //TODO:here start the cloudsight
            // Intent intent=new Intent(context,WikiJsoup.class);
            // startActivity(intent);

            new ImageIdentifying().execute(imageResponse.data.link);

        }



        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                // Snackbar.make(findViewById(R.id.rootView), "No internet connection", Snackbar.LENGTH_SHORT).show();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(null, "No internet connection", duration);
                toast.show();
            }else {
                GlassActivity.sendToGlass("Failed");
            }
        }
    }




}
