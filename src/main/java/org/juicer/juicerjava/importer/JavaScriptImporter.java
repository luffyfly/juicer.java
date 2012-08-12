package org.juicer.juicerjava.importer;

import java.io.IOException;
import java.util.Stack;

import org.juicer.juicerjava.dependency.DependencyResolver;
import org.juicer.juicerjava.dependency.JavaScriptDependencyResolver;

public class JavaScriptImporter {
	private static final String SEP = "/";
	
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
	

	String importStatic(String relativePath) throws IOException {
		String scriptTags = "";
		Stack<String> deps = dependencyResolver.resolvePath(relativePath);
		while(!deps.isEmpty()) {
			String dep = deps.pop();
			scriptTags += "<script charset=\"utf-8\" type=\"text/javascript\" src=\"" + assetsRoot + dep + "\">" + "</sctipt>";
			
		}
		return relativePath;
	}
}
