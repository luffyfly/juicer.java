package org.juicer.juicerjava.test.dependency;

import java.io.IOException;
import java.util.List;

import org.juicer.juicerjava.dependency.DependencyResolver;
import org.juicer.juicerjava.dependency.JavaScriptDependencyResolver;
import org.junit.Test;


public class DependencyResolverTest {
	private String documentRoot = "src/test/resources";
	private DependencyResolver resolver = new JavaScriptDependencyResolver(documentRoot);
	
	@Test
	public void testResolve() {
		try {
			List<String> paths = resolver.resolvePath("sub/a.js");
			for(String path : paths) {
				System.out.println(path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
