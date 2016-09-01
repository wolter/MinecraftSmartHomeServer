package biz.wolter.minecraft.bukkit.thing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;

import biz.wolter.minecraft.bukkit.SmartHome;

public class ThingList extends ArrayList<Thing> {
	
	private boolean saveImmediately = true;
	
	private File file;
	
	private ThingList () {
	}	
	
	public boolean removeThing(Thing o) {
		boolean success = super.remove(o);
		if (success && saveImmediately) {
			save();
		}
		return success;
	}
	
	public boolean addThing(Thing o) {
		boolean success = super.add(o);
		SmartHome.getPlugin(SmartHome.class).getLogger().info("add " + o);
		if (success && saveImmediately) {
			save();
		}
		return success;
	}
	
	public static ThingList load(File file) {
		
		ThingList list;

        try {
        	FileReader fr = new FileReader(file);
        	Gson gson = new Gson();
        	list = (ThingList) gson.fromJson(fr, ThingList.class);
        	
    	} catch (FileNotFoundException e) {
    		System.out.println(e.getLocalizedMessage());
        	list = new ThingList();
        }
        
        list.file = file;
        return list;
	}
	
    public synchronized void save() {
    	
		String absolutePath = file.getAbsolutePath();
		File filePath = new File(absolutePath.substring(0,absolutePath.lastIndexOf(File.separator)));
        if (!filePath.exists()) {
        	filePath.mkdirs();
        }
    	
		try {
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file);
			Gson gson = new Gson();
			fw.write(gson.toJson(this));
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    public Thing findThingByLocation(ThingLocation location) {
    	Thing thing = null;
		for (Thing t : this) {
			if (t.location.equals(location)) {
				thing = t;
				break;
			}
		}
    	return thing;
    }
    
    public Thing findThingById(String id) {
    	Thing thing = null;
		for (Thing t : this) {
			if (t.id.equals(id)) {
				thing = t;
				break;
			}
		}
    	return thing;
    }    
    
    public boolean isSavedImmediately() {
    	return saveImmediately;
    }
    
}