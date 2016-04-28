package ngocthuyen.com.myproject;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterFile extends BaseAdapter {

	private Context mContext;
	private ArrayList<EntityFile> mFile;

	public AdapterFile(Context context, ArrayList<EntityFile> arrayList) {
		this.mContext = context;
		this.mFile = arrayList;
	}

	@Override
	public int getCount() {
		return mFile.size();
	}

	@Override
	public Object getItem(int position) {
		return mFile.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.layout_file_entity, null);
			holder.folderName = (TextView) convertView
					.findViewById(R.id.txt_folerName);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		EntityFile file = (EntityFile) getItem(position);
		holder.folderName.setText(file.getName());
		return convertView;
	}

	private static class ViewHolder {
		private TextView folderName;
	}

}
