package scala.collection.parallel.benchmarks.hashtries




import scala.collection.parallel.benchmarks.generic.StandardParIterableBench
import scala.collection.parallel.benchmarks.generic.NotBenchmark
import scala.collection.parallel.benchmarks.generic.Dummy
import scala.collection.parallel.benchmarks.generic.Operators
import scala.collection.parallel.immutable.ParHashTrie





trait ParHashTrieBenches[K, V] extends StandardParIterableBench[(K, V), ParHashTrie[K, V]] {
  
  def nameOfCollection = "ParHashTrie"
  def comparisonMap = collection.Map()
  val forkJoinPool = new scala.concurrent.forkjoin.ForkJoinPool
  
  object Map2 extends IterableBenchCompanion {
    override def defaultSize = 5000
    def benchName = "map2";
    def apply(sz: Int, p: Int, w: String) = new Map2(sz, p, w)
  }
  
  class Map2(val size: Int, val parallelism: Int, val runWhat: String)
  extends IterableBench with StandardParIterableBench[(K, V), ParHashTrie[K, V]] {
    var result: Int = 0
    def comparisonMap = collection.Map()
    def runseq = {
      val r = this.seqcoll.asInstanceOf[collection.immutable.HashMap[K, V]].map(operators.mapper2)
      result = r.size
    }
    def runpar = {
      result = this.parcoll.map(operators.mapper2).size
      //println(collection.parallel.immutable.ParHashTrie.totalcombines)
      //System.exit(1)
    }
    def companion = Map2
    override def repetitionsPerRun = 50
    override def printResults {
      println("Total combines: " + collection.parallel.immutable.ParHashTrie.totalcombines)
      println("Size of last result: " + result)
    }
  }
  
}





object RefParHashTrieBenches extends ParHashTrieBenches[Dummy, Dummy] with NotBenchmark {
  
  type DPair = (Dummy, Dummy)
  
  object operators extends Operators[DPair] {
    def gcd(a: Int, b: Int): Int = {
      val result = if (b == 0) a else {
        gcd(b, a - b * (a / b))
      }
      result + 1000
    }
    def heavy(a: Int): Int = {
      var i = 0
      var sum = a
      while (i < 3000) {
        i += 1
        sum += a + i
      }
      sum
    }
    val reducer = (x: DPair, y: DPair) => {
      //y._2.num = x._2.in + y._2.in
      y
    }
    val mediumreducer = (x: DPair, y: DPair) => {
      y._2.num = gcd(x._2.in, y._2.in)
      y
    }
    val filterer = (p: DPair) => {
      p._1.num % 2 == 0
    }
    val mapper = (p: DPair) => {
      val a = p._1
      a.num = a.in % 2
      (a, p._2)
    }
    override val mapper2 = (p: DPair) => {
      val a = 1 //heavy(p._1.in)
      (new Dummy(p._1.in * -2 + a), p._2)
    }
    val heavymapper = (p: DPair) => {
      val a = p._1
      var i = -100
      while (i < 0) {
        if (a.in < i) a.num += 1
        i += 1
      }
      (a, p._2)
    }
    val taker = (p: DPair) => true
  }
  
  def createSequential(sz: Int, p: Int) = {
    var ht = new collection.immutable.HashMap[Dummy, Dummy]
    for (i <- 0 until sz) ht += ((new Dummy(i), new Dummy(i)))
    ht
  }
  
  def createParallel(sz: Int, p: Int) = {
    var pht = new ParHashTrie[Dummy, Dummy]
    for (i <- 0 until sz) pht += ((new Dummy(i), new Dummy(i)))
    forkJoinPool.setParallelism(p)
    pht.environment = forkJoinPool
    pht
  }
  
}
