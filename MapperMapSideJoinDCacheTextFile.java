package MapSideJoin;

/********************************************
*Mapper
*MapperMapSideJoinDCacheTextFile
********************************************/
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MapperMapSideJoinDCacheTextFile extends
  	Mapper<LongWritable, Text, Text, Text> {

	private static HashMap<String, String> DepartmentMap = new HashMap<String, String>();
	private BufferedReader brReader;
	private String strDeptName = "";
	private Text txtMapOutputKey = new Text("");
	private Text txtMapOutputValue = new Text("");

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {

		@SuppressWarnings("deprecation")
		Path[] cacheFilesLocal = DistributedCache.getLocalCacheFiles(context
				.getConfiguration());

		for (Path eachPath : cacheFilesLocal) {
			if (eachPath.getName().toString().trim().contains("departments.txt")) {
				loadDepartmentsHashMap(eachPath, context);
			}
		}

	}

	private void loadDepartmentsHashMap(Path filePath, Context context)
			throws IOException {

		String strLineRead = "";

		try {
			brReader = new BufferedReader(new FileReader(filePath.toString()));

			// Read each line, split and load to HashMap
			while ((strLineRead = brReader.readLine()) != null) {
				String deptFieldArray[] = strLineRead.split(",");
				DepartmentMap.put(deptFieldArray[0].trim(),
						deptFieldArray[1].trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (brReader != null) {
				brReader.close();

			}

		}
	}

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {


		if (value.toString().length() > 0) {
			String arrEmpAttributes[] = value.toString().split(",");
				if(DepartmentMap.containsKey(arrEmpAttributes[2].toString())){
				 strDeptName = DepartmentMap.get(arrEmpAttributes[2].toString());
				}
				else{
					strDeptName="NOT-FOUND";
				}

			txtMapOutputKey.set(arrEmpAttributes[0].toString());

			txtMapOutputValue.set(arrEmpAttributes[1].toString() + "\t"
					+ arrEmpAttributes[2].toString() + "\t" + strDeptName);

		}
		context.write(txtMapOutputKey, txtMapOutputValue);
		strDeptName = "";
	}
}
