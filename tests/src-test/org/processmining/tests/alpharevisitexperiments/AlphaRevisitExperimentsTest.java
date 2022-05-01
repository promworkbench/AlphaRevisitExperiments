package org.processmining.tests.alpharevisitexperiments;
import junit.framework.TestCase;

import org.junit.Test;
import org.processmining.contexts.cli.CLI;

public class AlphaRevisitExperimentsTest extends TestCase {

  @Test
  public void testNewPackage1() throws Throwable {
    String args[] = new String[] {"-l"};
    CLI.main(args);
  }

  @Test
  public void testNewPackage2() throws Throwable {
    String testFileRoot = System.getProperty("test.testFileRoot", ".");
    String args[] = new String[] {"-f", testFileRoot+"/AlphaRevisitExperiments_Example.txt"};
    
    CLI.main(args);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(AlphaRevisitExperimentsTest.class);
  }
  
}
