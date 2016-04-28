package ngocthuyen.com.myproject;

import java.io.Serializable;

import org.json.JSONObject;

public class EntityFile implements Serializable {

	private String name;
	private String file;
	private String time;

	private JSONObject jsonObject;

	private final String NAME = "name";
	private final String SIZE = "size";
	private final String TIME = "ctime";

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public EntityFile() {
	}

	public EntityFile(String name) {
		this.name = name;
	}

	public String getName() {
		name = getData(NAME);
		return name;
	}

	public String getFile() {
		file = getData(SIZE);
		return file;
	}

	public String getTime() {
		time = getData(TIME);
		return time;
	}

	public String getData(String key) {
		try {
			if (jsonObject.has(key)) {
				return jsonObject.getString(key);
			}
		} catch (Exception e) {
		}
		return "";
	}

}
