package ngocthuyen.com.myproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends Activity {

	private Button btn_upload;
	private ListView mListView;
	private Context mContext;
	private ArrayList<EntityFile> mFile;
	private AdapterFile mAdapter;
	private String mBaseUrl = "http://192.168.2.92:3000";
	Firebase fireBase ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Firebase.setAndroidContext(getApplicationContext());
		fireBase = new Firebase("https://files-manager.firebaseio.com");
		btn_upload = (Button) findViewById(R.id.btn_add);
		mListView = (ListView) findViewById(R.id.listview);
		mContext = this;
		handleEvent();
		getAllData();
		fireBase.child("files").addValueEventListener(
				new ValueEventListener() {

					@Override
					public void onDataChange(DataSnapshot arg0) {
						showToast("change");
						getAllData();
					}

					@Override
					public void onCancelled(FirebaseError arg0) {

					}
				});
	}

	void handleEvent() {
		btn_upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
				mediaIntent.setType("*/*"); // set mime type as per
				startActivityForResult(mediaIntent, 100);
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final EntityFile entityFile = (EntityFile) parent
						.getItemAtPosition(position);
				final Dialog dialog = new Dialog(mContext);
				dialog.setContentView(R.layout.layout_dialog);
				dialog.setTitle("Choose your action");
				Button btn_delete = (Button) dialog
						.findViewById(R.id.btn_delete);
				Button btn_download = (Button) dialog
						.findViewById(R.id.btn_download);
				btn_delete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						removeFile(entityFile.getName());
						dialog.dismiss();
					}
				});
				btn_download.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new DownloadFileFromURL().execute(mBaseUrl + "/"
								+ entityFile.getName());
						dialog.dismiss();
					}
				});
				dialog.show();
				return false;
			}
		});
	}

	private void removeFile(String fileName) {
		RequestQueue queue = Volley.newRequestQueue(this);
		String url = mBaseUrl + "/" + fileName;
		StringRequest request = new StringRequest(Method.DELETE, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String result) {
//						getAllData();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						System.out.println(error);
					}
				});
		queue.add(request);
	}

	private String getNameFileFromPath(String filePath) {
		String name = "";
		if (filePath.contains("/")) {
			String[] array = filePath.split("/");
			if (array.length > 0) {
				name = array[array.length - 1];
			}
		}
		return name;
	}

	private void getAllData() {
		RequestQueue queue = Volley.newRequestQueue(this);
		CustomRequest request = new CustomRequest(Method.GET, mBaseUrl,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String result) {
						System.out.println(result);
						updateAllData(result);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						System.out.println(error);
					}
				});
		request.setRetryPolicy(new DefaultRetryPolicy(30000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		queue.add(request);
	}

	private void updateAllData(String result) {
		try {
			JSONArray array = new JSONArray(result);
			if (array.length() > 0) {
				mFile = new ArrayList<EntityFile>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					EntityFile entityFile = new EntityFile();
					entityFile.setJsonObject(object);
					mFile.add(entityFile);
				}
				mAdapter = new AdapterFile(mContext, mFile);
				mListView.setAdapter(mAdapter);

			}
		} catch (Exception e) {
		}
	}

	void showToast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100 && resultCode == RESULT_OK) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			if (filePath != null && !filePath.equals("")) {
				String name = getNameFileFromPath(filePath);
				// uploadFile(filePath, name);
				uploadFile(Uri.parse(filePath));

			}
		}

	}

	private void uploadFile(String filePath) {
		Bitmap bm = BitmapFactory.decodeFile(filePath);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap
															// object
		byte[] byteArray = baos.toByteArray();

		String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);
		RequestQueue queue = Volley.newRequestQueue(this);
		CustomRequest customRequest = new CustomRequest(Method.POST, mBaseUrl+"/",
				new Response.Listener<String>() {

					@Override
					public void onResponse(String result) {
						System.out.println(result);
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						System.out.println(error);
					}
				});
		customRequest.setParams("myFile", "@" + filePath);
		customRequest.setParams("files", encode);
		queue.add(customRequest);
	}

	private void uploadFile(Uri fileUri) {
		FileUploadService service = ServiceGenerator.createService(FileUploadService.class);
		File file = FileUtils.getFile(fileUri.getPath());
		RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
		MultipartBody.Part body = MultipartBody.Part.createFormData("myFile", file.getName(), requestFile);
		String descriptionString = "hello, this is description speaking";
		RequestBody description = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionString);
		Call<ResponseBody> call = service.upload(description, body);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				Log.v("Upload", "success");
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e("Upload error:", t.getMessage());
			}
		});
	}

	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				String nameFile = getNameFileFromPath(f_url[0]);
				System.out.println(nameFile);
				URLConnection conection = url.openConnection();
				conection.connect();

				// this will be useful so that you can show a tipical 0-100%
				// progress bar
				int lenghtOfFile = conection.getContentLength();

				// download the file
				InputStream input = new BufferedInputStream(url.openStream(),
						8192);

				// Output stream
				OutputStream output = new FileOutputStream(Environment
						.getExternalStorageDirectory().toString()
						+ "/"
						+ nameFile);

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					// After this onProgressUpdate will be called
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded

		}

	}

}
