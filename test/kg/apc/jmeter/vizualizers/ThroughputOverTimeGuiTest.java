/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.apc.jmeter.vizualizers;

import java.util.logging.Level;
import java.util.logging.Logger;
import kg.apc.jmeter.util.TestJMeterUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author apc
 */
public class ThroughputOverTimeGuiTest
{
   /**
    * 
    */
   public ThroughputOverTimeGuiTest()
   {
   }

   /**
    *
    * @throws Exception
    */
   @BeforeClass
   public static void setUpClass() throws Exception
   {
      TestJMeterUtils.createJmeterEnv();
   }

   /**
    *
    * @throws Exception
    */
   @AfterClass
   public static void tearDownClass() throws Exception
   {
   }

   /**
    *
    */
   @Before
   public void setUp()
   {
   }

   /**
    *
    */
   @After
   public void tearDown()
   {
   }

   /**
    *
    */
   @Test
   public void testGetLabelResource()
   {
      System.out.println("getLabelResource");
      ThroughputOverTimeGui instance = new ThroughputOverTimeGui();
      String expResult = "ThroughputOverTimeGui";
      String result = instance.getLabelResource();
      assertEquals(expResult, result);
   }

   /**
    *
    */
   @Test
   public void testGetStaticLabel()
   {
      System.out.println("getStaticLabel");
      ThroughputOverTimeGui instance = new ThroughputOverTimeGui();
      String expResult = "Transaction Throughput Over Time";
      String result = instance.getStaticLabel();
      assertEquals(expResult, result);
   }

   /**
    *
    */
   @Test
   public void testAdd()
   {
      System.out.println("add");
      SampleResult res = new SampleResult();
      res.setAllThreads(1);
      res.setThreadName("test 1-2");
      ThroughputOverTimeGui instance = new ThroughputOverTimeGui();
      instance.add(res);
      res.sampleStart();
      try
      {
         Thread.sleep(10);
      }
      catch (InterruptedException ex)
      {
      }
      res.sampleEnd();
      instance.add(res);
   }
}