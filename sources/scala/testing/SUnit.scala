/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2004, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
** $Id$
\*                                                                      */

package scala.testing;

/** unit testing methods in the spirit of JUnit 
 */
object SUnit {

  /** a Test can be run with its result being collected */
  trait Test {
    def run(r: TestResult):Unit;
  }

  /** a TestCase defines the fixture to run multiple tests */
  class TestCase(val name: String) with Test with Assert {
    protected def createResult() = 
      new TestResult();

    protected def runTest(): Unit = 
      {}

    def run(r: TestResult): Unit = 
      try {
        runTest();
      } catch {
        case t:Throwable => r.addFailure(this, t);
      }

    def run(): Unit = 
      run(createResult());

    def setUp() = 
      {}

    def tearDown() = 
      {}

    override def toString() =
      name;
  }

  /** a TestFailure collects a failed test together with the thrown exception */
  class TestFailure(val failedTest:Test, val thrownException:Throwable) {

    def this(p:Pair[Test,Throwable]) = this(p._1, p._2);

    override def toString() = 
      failedTest.toString()+" failed due to "+thrownException.toString();

    def trace(): String = {
      val s = new StringBuffer();
      for(val trElem <- thrownException.getStackTrace()) {
        s.append(trElem.toString());
        s.append('\n');
      }
      s.toString()
    }
  }

  /** a TestResult collects the result of executing a test case */
  class TestResult {
    val buf = 
      new scala.collection.mutable.ArrayBuffer[Pair[Test,Throwable]]();

    def addFailure(test:Test, t:Throwable) = 
      buf += Pair(test,t);

    def failureCount() = 
      buf.length;

    def failures() = 
      buf.elements map { x => new TestFailure(x) };
  }

  /** a TestSuite runs a composite of test cases */
  class TestSuite(tests:Test*) with Test {
    val buf =
      new scala.collection.mutable.ArrayBuffer[Test]();

    buf ++= tests;

    def addTest(t: Test) = 
      buf += t;

    def run(r: TestResult):Unit = {
      for(val t <- buf) {
        t.run(r);
      }
    }
  }

  /** an AssertFailed is thrown for a failed assertion */
  case class AssertFailed(msg:String) extends java.lang.RuntimeException {
    override def toString() = 
      "failed assertion:"+msg;
  }

  /** this trait defined useful assert methods */
  trait Assert {
    /** equality */
    def assertEquals[A](msg:String, expected:A, def actual:A): Unit = 
      if( expected != actual ) fail(msg);

    /** equality */
    def assertEquals[A](expected:A, def actual:A): Unit  = 
      assertEquals("(no message)", expected, actual);
    
    /** falseness */
    def assertFalse(msg:String, def actual: Boolean): Unit = 
      assertEquals(msg, false, actual);
    /** falseness */
    def assertFalse(def actual: Boolean): Unit = 
      assertFalse("(no message)", actual);

    /** not null */
    def assertNotNull(msg:String, def actual: AnyRef): Unit = 
      if( null == actual ) fail(msg);

    /** not null */
    def assertNotNull(def actual: AnyRef): Unit  = 
      assertNotNull("(no message)", actual);

    /** reference inequality */
    def assertNotSame(msg:String, def expected: AnyRef, def actual: AnyRef): Unit = 
      if(expected.eq(actual)) fail(msg);
    /** reference inequality */
    def assertNotSame(def expected: AnyRef, def actual: AnyRef): Unit  = 
      assertNotSame("(no message)", expected, actual);

    /** null */
    def assertNull(msg:String, def actual: AnyRef): Unit = 
        if( null != actual ) fail(msg);
    /** null */
    def assertNull(def actual: AnyRef): Unit = 
      assertNull("(no message)", actual);

      
    /** reference equality */
    def assertSame(msg:String, def expected: AnyRef, def actual: AnyRef): Unit = 
        if(!expected.eq(actual)) fail(msg);
    /** reference equality */
    def assertSame(def expected: AnyRef, def actual: AnyRef): Unit  = 
      assertNull("(no message)", actual);

    /** trueness */
    def assertTrue(msg:String, def actual: Boolean): Unit = 
      assertEquals(msg, true, actual);
    /** trueness */
    def assertTrue(def actual: Boolean): Unit  = 
      assertTrue("(no message)", actual);

    /** throws AssertFailed with given message */
    def fail(msg:String): Unit = 
      throw new AssertFailed(msg);
  }
}
