package org.juicer.juicerjava.dependency;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaScriptDependencyResolver extends DependencyResolver {

	private static String extension = ".js";
	public static String dependencyPattern = "@depends?\\s+([^\\s\\'\\\"\\;]+)";
	
	public JavaScriptDependencyResolver(String documentRoot) {
		super(documentRoot);
	}

	@Override
	public String extension() {
		// TODO Auto-generated method stub
		return extension;
	}

	@Override
	public String parse(String line) {
		Pattern pattern = Pattern.compile(dependencyPattern);
		Matcher matcher = pattern.matcher(line);
		if(matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

}
