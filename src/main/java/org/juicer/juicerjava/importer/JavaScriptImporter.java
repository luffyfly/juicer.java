package org.juicer.juicerjava.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.juicer.juicerjava.dependency.DependencyResolver;
import org.juicer.juicerjava.dependency.JavaScriptDependencyResolver;

public class JavaScriptImporter {
	private static final String SEP = "/";
	//Matches #javascripts("arg1", "arg2") in velocity, the group 1 is "arg1", "arg2"
	private static final String vmJavaScriptsPattern = "#javascripts\\(\\[(((?:'[^']*'|\"[^\"]*\"),\\s*)*(?:'[^']*'|\"[^\"]*\"\\s*){1})\\]\\)";
	private static final String JUICER_CMD = "juicer";
	private String documentRoot;
	private String assetsRoot;
	private static DependencyResolver dependencyResolver = null;
	
	public JavaScriptImporter(String assetsRoot, String documentRoot) {
		super();
		this.documentRoot = documentRoot;
		if(!assetsRoot.endsWith(SEP)) {
			assetsRoot += SEP;
		}
		this.assetsRoot = assetsRoot;
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
	 * 计算paths中路径指向的所有js的依赖，返回整个依赖的js数组字串。
	 * 在模板文件中调用整个方法，
	 * @param paths
	 * @return
	 * @throws IOException 
	 */
	public String calculateDeps(List<String> paths) throws IOException {
		String result = null;
		Set<String> allDeps = new HashSet<String>();
		for(String path : paths) {
			List<String> deps = dependencyResolver.resolvePath(path);
			allDeps.addAll(deps);
		}
		for(String dep: allDeps) {
			if(result == null) {
				result = "[" + "\"" + dep + "\"";
			} else {
				result += ",\"" + dep + "\"";
			}
			
		}
		result += "]";
		return result;
	}
	
	public boolean compileJSLoaderInVM(String vmPath) {
		Pattern pattern = Pattern.compile(vmJavaScriptsPattern);
		
		Matcher matcher = pattern.matcher("#javascripts(['1', '3', 'test.js', 't.js'])");
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
			
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param jsPaths
	 * @param start
	 * @param end
	 */
	private void compileJS(List<String> jsPaths) {
		
	}
}
