package ecgberht.Weka;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Weka {
	List<String> strats = new ArrayList<String>(Arrays.asList("FullBio","BioMech")); 
	List<String> results = new ArrayList<String>(Arrays.asList("1","0"));
	
	Attribute result = new Attribute("result", results);
	Attribute strat = new Attribute("strat", strats);
	Attribute mapSize = new Attribute("mapSize");
	ArrayList<Attribute> attributes = new ArrayList<Attribute>(Arrays.asList(strat,mapSize,result));
	Instance instance = new DenseInstance(3);
	Instances datos =  new Instances("datos", attributes, 1000);
	
	public Instances readArff(String name) throws Exception {
		DataSource source = null;
		Instances data = null;
		String path = "bwapi-data/read/" + name + ".arff";
		if(Files.exists(Paths.get(path))) { 
			source = new DataSource(path);
			data = source.getDataSet();
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
		}
		return data;
	}
	
	public void createAndWriteInstance(String opponent, String stratName, int mapSize, boolean win) throws IOException  {
		String path = "bwapi-data/read/" + opponent + ".arff";
		this.instance.setDataset(datos);
		this.instance.setValue(0, stratName); 
		this.instance.setValue(1, mapSize); 
		if(win) {
			instance.setValue(2, "1");
		}
		else {
			instance.setValue(2, "0");
		}
		System.out.println(this.instance.toString());
		if(this.datos.equals(null)) {
			this.datos = new Instances("datos", attributes, 1000);
		}
		if (this.datos.classIndex() == -1)
			this.datos.setClassIndex(this.datos.numAttributes() - 1);
		this.datos.add(instance);
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		writer.write(this.datos.toString());
		writer.flush();
		writer.close();
		
	}
}
