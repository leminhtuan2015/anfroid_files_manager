package ngocthuyen.com.myproject;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class CustomRequest extends StringRequest {

	private Map<String, String> mParam = new HashMap<String, String>();
	private Map<String, String> mHeaders = new HashMap<>();
	private String url = "";

	public CustomRequest(int method, String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		this.url = url;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		mHeaders.put("Content-Type", "multipart/form-data");
		return mHeaders;
	}

	public void setParams(String key,String param){
		mParam.put(key, param);
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mParam;
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		return super.parseNetworkResponse(response);
	}
}
