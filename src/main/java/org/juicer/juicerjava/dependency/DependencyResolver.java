package org.juicer.juicerjava.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	
	public Stack<String> resolvePath(String relativePath) throws IOException {
		Stack<String> importedPaths = new Stack<String>();
		Stack<String> resolveStack = new Stack<String>();
		resolvePath(relativePath, importedPaths, resolveStack);
		return importedPaths;
	}
	/**
	 * @param relativePath Relative to the document root.
	 * @throws IOException 
	 * 
	 */
	public void resolvePath(String relativePath, Stack<String> importedPaths, Stack<String> resolveStack) throws IOException {
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
				importedPath = relatilize(absolutePath);
				if(!checkCirculation( relativePath, importedPath)) {
					if(!importedPaths.contains(importedPath)) {
						if(resolveStack.contains(importedPath)) {
							resolveStack.push(importedPath);
							throw new IOException("[æ≤Ã¨“¿¿µº∆À„]¥Ê‘⁄—≠ª∑“¿¿µ£¨«ÎºÏ≤ÈŒƒº˛“¿¿µπÿœµ£¨“¿¿µÀ≥–ÚŒ™" + resolveStack.toString());
						}
						resolveStack.push(importedPath);
						resolvePath(importedPath, importedPaths, resolveStack);
						resolveStack.pop();
						importedPaths.push(importedPath);
					}
				}
				
			}
			line = reader.readLine();
		} 
	}
	/**
	 * Relatilize a absolute path against the document root.
	 * @param absolutePath
	 * @return
	 */
	private String relatilize(String absolutePath) {
		return new File(documentRoot).toURI().relativize(new File(absolutePath).toURI()).getPath();
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
