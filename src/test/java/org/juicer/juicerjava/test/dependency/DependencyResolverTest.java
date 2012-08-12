package org.juicer.juicerjava.test.dependency;

import java.io.IOException;
import java.util.Stack;

import org.juicer.juicerjava.dependency.DependencyResolver;
import org.juicer.juicerjava.dependency.JavaScriptDependencyResolver;
import org.junit.Test;


public class DependencyResolverTest {
	private String documentRoot = "src/test/resources";
	private DependencyResolver resolver = new JavaScriptDependencyResolver(documentRoot);
	
	@Test
	public void testResolve() {
		try {
			Stack<String> paths = resolver.resolvePath("sub/b.js");
			for(String path : paths) {
				System.out.println(path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
