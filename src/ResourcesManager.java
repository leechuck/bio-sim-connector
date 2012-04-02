




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class ResourcesManager {

	
	public void createResourcesFolder(){
		new File("resources").mkdir();
		
	}
	
	public static Hashtable<String, String[]> createHashtableFromResource(String path) throws FileNotFoundException {
		Hashtable<String, String[]> table = new Hashtable<String, String[]>();
		Scanner unitsfilescanner = new Scanner(BioSimConnector.class.getResourceAsStream(path));
		String nextline = "";
		String key = "";
		Set<String> values = new HashSet<String>();

		if (!unitsfilescanner.hasNext()) {
			System.out.println("Could not create hashtable from file: " + path);
		} else {
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			while (unitsfilescanner.hasNext()) {
				values.clear();
				nextline = unitsfilescanner.nextLine();
				semiseparatorindex = nextline.indexOf(";");
				key = nextline.substring(0, semiseparatorindex);
				nextline = nextline.substring(semiseparatorindex + 2,
						nextline.length());
				Boolean repeat = true;
				while (repeat) {
					if (nextline.contains(",")) {
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					} else {
						values.add(nextline);
						repeat = false;
					}
				}
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
		}
		return table;
	}
	
	
	public static Hashtable<String, String[]> createHashtableFromFile(String path) throws FileNotFoundException {
		Hashtable<String, String[]> table = new Hashtable<String, String[]>();
		Scanner unitsfilescanner = new Scanner(new File(path));
		String nextline = "";
		String key = "";
		Set<String> values = new HashSet<String>();

		if (!unitsfilescanner.hasNext()) {
			System.out.println("Could not create hashtable from file: " + path);
		} else {
			int semiseparatorindex = 0;
			int commaseparatorindex = 0;
			while (unitsfilescanner.hasNext()) {
				values.clear();
				nextline = unitsfilescanner.nextLine();
				semiseparatorindex = nextline.indexOf(";");
				key = nextline.substring(0, semiseparatorindex);
				nextline = nextline.substring(semiseparatorindex + 2,
						nextline.length());
				Boolean repeat = true;
				while (repeat) {
					if (nextline.contains(",")) {
						commaseparatorindex = nextline.indexOf(",");
						values.add(nextline.substring(0, nextline.indexOf(",")));
						commaseparatorindex = nextline.indexOf(",");
						nextline = nextline.substring(commaseparatorindex + 2,
								nextline.length());
					} else {
						values.add(nextline);
						repeat = false;
					}
				}
				table.put(key, (String[]) values.toArray(new String[] {}));
			}
		}
		return table;
	}
}

