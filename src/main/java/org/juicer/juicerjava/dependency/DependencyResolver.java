package org.juicer.juicerjava.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public abstract class DependencyResolver {
	private String documentRoot;
	private String SEP = "/";
	
	public DependencyResolver(String documentRoot) {
		if(!documentRoot.endsWith(SEP)) {
			documentRoot += SEP;
		}
		this.documentRoot = documentRoot;
	}
	
	public List<String> resolvePath(String relativePath) throws IOException {
		List<String> importedPaths = new ArrayList<String>();
		Stack<String> resolveStack = new Stack<String>();
		
		System.out.println("Computing dependency tree: " + relativePath);
		
		resolvePath(relativePath, importedPaths, resolveStack);
		//Add relativePath to the stack
		String absolutePath = null;
		if(relativePath.startsWith("/")) {
			absolutePath = documentRoot + relativePath.substring(1);
		} else {
			absolutePath = documentRoot + relativePath;
		}
		importedPaths.add(relatilize(absolutePath, null));
		return importedPaths;
	}
	/**
	 * @param relativePath Relative to the document root.
	 * @throws IOException 
	 * 
	 */
	public void resolvePath(String relativePath, List<String> importedPaths, Stack<String> resolveStack) throws IOException {
		File file = new File(documentRoot + relativePath);
		String absoluteDirPath = file.getAbsoluteFile().getParent();
		InputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line = reader.readLine();
		while(line != null){
			String importedPath = parse(line);
			if(importedPath != null) {
				String absolutePath = absoluteDirPath + SEP + importedPath;
				if(importedPath.startsWith("/")) {
					absolutePath = documentRoot + importedPath.substring(1);
				}
				importedPath = relatilize(absolutePath, null);
				if(!checkCirculation( relativePath, importedPath)) {
					if(!importedPaths.contains(importedPath)) {
						if(resolveStack.contains(importedPath)) {
							resolveStack.push(importedPath);
							throw new IOException("[静态依赖计算]存在循环依赖，请检查文件依赖关系，依赖顺序为" + resolveStack.toString());
						}
						resolveStack.push(importedPath);
						resolvePath(importedPath, importedPaths, resolveStack);
						resolveStack.pop();
						importedPaths.add(importedPath);
					}
				}
				
			}
			line = reader.readLine();
		} 
	}
	/**
	 * Relatilize a absolute path against staticRoot.
	 * @param absolutePath
	 * @return
	 */
	public String relatilize(String absolutePath, String staticRoot) {
		if(staticRoot == null) {
			staticRoot = documentRoot;
		}
		return new File(staticRoot).toURI().relativize(new File(absolutePath).toURI()).getPath();
	}
	/**
	 * Check circular dependency
	 * @return if true, the circular dependency exists; if false, not exists
	 */
	private boolean checkCirculation(String relativePath, String importedPath) {
		File file1 = new File(documentRoot + relativePath);
		File file2 = new File(documentRoot + importedPath);
		if(file1.getAbsolutePath().equals(file2.getAbsolutePath())) {
			return true;
		}
		return false;
	}
	public abstract String parse(String line);
	
	public abstract String extension();
}
