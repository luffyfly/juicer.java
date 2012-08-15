package org.juicer.juicerjava.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

import org.juicer.juicerjava.dependency.DependencyResolver;
import org.juicer.juicerjava.dependency.JavaScriptDependencyResolver;

public class JavaScriptImporter {
	
	private static final Logger logger = Logger.getLogger(JavaScriptImporter.class);
	
	private static final String SEP = "/";
	//Matches #javascripts("arg1", "arg2") in velocity, the group 1 is "arg1", "arg2"
	private static final String vmJavaScriptsPattern = "#javascripts\\(\\[(((?:'[^']*'|\"[^\"]*\"),\\s*)*(?:'[^']*'|\"[^\"]*\"\\s*){1})\\]\\)";
	private static String JUICER_CMD = "juicer";
	private String documentRoot;
	private String assetsRoot;
	private String staticRoot;
	private static DependencyResolver dependencyResolver = null;
	
	static {
		String osName = System.getProperty("os.name");
		if(osName.indexOf("Windows") >= 0) {
			JUICER_CMD += ".bat";
		}
	}
	/*
	 * 
	 */
	public JavaScriptImporter(String assetsURL, String documentRoot, String staticRoot) {
		super();
		
		if(!documentRoot.endsWith(SEP)) {
			documentRoot += SEP;
		}
		if(!assetsURL.endsWith(SEP)) {
			assetsURL += SEP;
		}
		if(!staticRoot.endsWith(SEP)) {
			staticRoot += SEP;
		}
		this.assetsRoot = assetsURL;
		this.documentRoot = documentRoot;
		this.staticRoot = staticRoot;
		dependencyResolver = new JavaScriptDependencyResolver(this.documentRoot);
	}
	

	public String importStatic(String relativePath) throws IOException {
		String scriptTags = "";
		List<String> deps = dependencyResolver.resolvePath(relativePath);
		for(String dep: deps) {
			scriptTags += "<script charset=\"utf-8\" type=\"text/javascript\" src=\"" + assetsRoot + dep + "\">" + "</sctipt>";
		}
		return scriptTags;
	}
	/**
	 * 璁＄畻paths涓矾寰勬寚鍚戠殑鎵�湁js鐨勪緷璧栵紝杩斿洖鏁翠釜渚濊禆鐨刯s鏁扮粍瀛椾覆銆�
	 * 鍦ㄦā鏉挎枃浠朵腑璋冪敤鏁翠釜鏂规硶锛�
	 * @param paths
	 * @return
	 * @throws IOException 
	 */
	public String calculateDeps(String[] paths) throws IOException {
		String result = null;
		List<String> allDeps = new ArrayList<String>();
		for(String path : paths) {
			List<String> deps = dependencyResolver.resolvePath(path);
			for(String dep : deps) {
				if(!allDeps.contains(dep)) {
					allDeps.add(dep);
				}
			}
			
		}
		
		for(String dep : allDeps) {
			if(result == null) {
				result = "["  + "\"" + assetsRoot + dep + "\"";
			} else {
				result += "," +  "\"" + assetsRoot + dep + "\"";
			}
			
		}
		result += "]";
		return result; 
	}
	
	public boolean compileJSLoaderInVM(String vmPath, String outputPath, String jsOutputPath) throws IOException {
		Pattern pattern = Pattern.compile(vmJavaScriptsPattern);
		InputStream in = null;
		FileWriter out = null;
		try {
			in = new FileInputStream(new File(vmPath));
			out = new FileWriter(new File(outputPath));
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(out);
			
			String line = reader.readLine();
			int lineNum = 0;
			while(line != null) {
				Matcher matcher = pattern.matcher(line);
				List<String> jsPaths = new ArrayList<String>();
				while (matcher.find()) {
					String pathStr = matcher.group(1);
					int start = matcher.start();
					int end = matcher.end();
					for(String path : pathStr.split(",\\s*")) {
						path = path.trim();
						path = path.replace("'", "").replace("\"", "");
						jsPaths.add(path);
					}
					logger.info("Compiling " + vmPath + ", line " + lineNum + ", JS:" + jsPaths.toString());
					line = line.substring(0, start) + compileJS(jsPaths, jsOutputPath) + line.substring(end, line.length());
				}
				line += 1;
				writer.write(line);
				writer.newLine();
				line = reader.readLine();
			}
			writer.flush();
		}  finally {
			if(in != null) {
				in.close();
			}
			
			if(out != null) {
				out.close();
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param jsPaths
	 * @param start
	 * @param end
	 * @return 
	 * @throws IOException 
	 */
	private String compileJS(List<String> jsPaths, String jsOutputPath) throws IOException {
		String tmpPath = getTmpPath(".js");
		BufferedWriter writer = new BufferedWriter(new FileWriter(tmpPath));
		writer.write("/**");
		writer.newLine();
		for(String path : jsPaths) {
			writer.write("@depend " + path);
			writer.newLine();
		}
		writer.write("**/");
		writer.flush();
		writer.close();
		
		File file = new File(tmpPath);
		tmpPath = file.getAbsolutePath();
		String cmd = JUICER_CMD + " merge " + tmpPath + " -o " + jsOutputPath;
		Process process = Runtime.getRuntime().exec(cmd);
		int exitVal = 0;
		try {
			exitVal = process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			exitVal = -1;
			e.printStackTrace();
		} finally {
			if(file.exists()) {
				file.delete();
			}
		}
		if(exitVal != 0) {
			throw new IOException("[缂栬瘧VM鏂囦欢]缂栬瘧鏂囦欢" + jsPaths.toString() + "鍑洪敊");
		}
		
		return dependencyResolver.relatilize(jsOutputPath, staticRoot);
	}
	/**
	 * Get a temporary path for temporary usage.
	 * @return
	 */
	private String getTmpPath(String ext) {
		File file = null;
		String path = null;
		int count = 0;
		do {
			path = documentRoot;
			path += "tmp_" + String.valueOf(new Date().getTime());
			path += "_" + count;
			path += ext;
			file = new File(path);
			count += 1;
		} while(file.exists());
		return path;
	}
	
}
