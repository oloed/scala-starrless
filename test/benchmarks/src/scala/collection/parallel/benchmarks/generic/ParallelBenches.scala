package scala.collection.parallel
package benchmarks
package generic







trait ParIterableBench[T, Coll <: ParIterable[T]] extends collection.parallel.benchmarks.Bench {
self =>
  
  protected var seqcoll: Iterable[T] = null
  protected var parcoll: Coll = null.asInstanceOf[Coll]
  
  reset
  
  def reset = runWhat match {
    case "seq" => this.seqcoll = createSequential(size, parallelism)
    case "par" => this.parcoll = createParallel(size, parallelism)
    case _ =>
  }
  
  def nameOfCollection: String
  def operators: Operators[T]
  def createSequential(sz: Int, p: Int): Iterable[T]
  def createParallel(sz: Int, p: Int): Coll
  
  trait IterableBenchCompanion extends BenchCompanion {
    def collectionName = self.nameOfCollection
  }
  
  trait IterableBench extends ParIterableBench[T, Coll] {
    def nameOfCollection = self.nameOfCollection
    def operators = self.operators
    def createSequential(sz: Int, p: Int) = self.createSequential(size, parallelism)
    def createParallel(sz: Int, p: Int) = self.createParallel(size, parallelism)
    def forkJoinPool: scala.concurrent.forkjoin.ForkJoinPool = self.forkJoinPool
  }
  
  def forkJoinPool: scala.concurrent.forkjoin.ForkJoinPool
  
  override def printResults {
    println(" --- Fork join pool state --- ")
    println("Parallelism: " + forkJoinPool.getParallelism)
    println("Active threads: " + forkJoinPool.getActiveThreadCount)
    println("Work stealings: "  + forkJoinPool.getStealCount)
  }
  
}


trait ParSeqBench[T, Coll <: ParSeq[T]] extends ParIterableBench[T, Coll] {
  self =>
  
  def createSequential(sz: Int, p: Int): Seq[T]
  
  trait SeqBenchCompanion extends BenchCompanion {
    def collectionName = self.nameOfCollection
  }
  
  trait SeqBench extends IterableBench with ParSeqBench[T, Coll] {
    override def createSequential(sz: Int, p: Int) = self.createSequential(size, parallelism)
  }
  
}


trait NotBenchmark {
  lazy val runWhat = "";
  val size = -1
  val parallelism = -1
  def runpar {}
  def runseq {}
  def companion = throw new UnsupportedOperationException
}


/**
 * Standard benchmarks for collections.
 */ 
trait StandardParIterableBench[T, Coll <: ParIterable[T]] extends ParIterableBench[T, Coll] {
  
  object Reduce extends IterableBenchCompanion {
    override def defaultSize = 50000
    def benchName = "reduce";
    def apply(sz: Int, p: Int, w: String) = new Reduce(sz, p, w)
  }
  
  class Reduce(val size: Int, val parallelism: Int, val runWhat: String)
  extends IterableBench with StandardParIterableBench[T, Coll] {
    def comparisonMap = collection.Map()
    def runseq = this.seqcoll.reduceLeft(operators.reducer)
    def runpar = this.parcoll.reduce(operators.reducer)
    def companion = Reduce
  }
  
  object ReduceMedium extends IterableBenchCompanion {
    override def defaultSize = 5000
    def benchName = "reduce-medium";
    def apply(sz: Int, p: Int, w: String) = new ReduceMedium(sz, p, w)
  }
  
  class ReduceMedium(val size: Int, val parallelism: Int, val runWhat: String)
  extends IterableBench with StandardParIterableBench[T, Coll] {
    def comparisonMap = collection.Map()
    def runseq = this.seqcoll.reduceLeft(operators.mediumreducer)
    def runpar = this.parcoll.reduce(operators.mediumreducer)
    def companion = ReduceMedium
  }
  
  object Map extends IterableBenchCompanion {
    override def defaultSize = 5000
    def benchName = "map";
    def apply(sz: Int, p: Int, w: String) = new Map(sz, p, w)
  }
  
  class Map(val size: Int, val parallelism: Int, val runWhat: String)
  extends IterableBench with StandardParIterableBench[T, Coll] {
    def comparisonMap = collection.Map()
    def runseq = this.seqcoll.map(operators.mapper)
    def runpar = this.parcoll.map(operators.mapper)
    def companion = Map
  }
  
}



/**
 * Benchmarks for sequence views.
 */
trait ParSeqViewBench[T, Coll <: ParSeqView[T, ParSeq[T], CollSeq], CollSeq] extends ParSeqBench[T, Coll] {
  
  object Reduce extends IterableBenchCompanion {
    override def defaultSize = 50000
    def benchName = "reduce";
    def apply(sz: Int, p: Int, w: String) = new Reduce(sz, p, w)
  }
  
  class Reduce(val size: Int, val parallelism: Int, val runWhat: String)
  extends SeqBench with ParSeqViewBench[T, Coll, CollSeq] {
    def comparisonMap = collection.Map()
    def runseq = this.seqcoll.reduceLeft(operators.reducer)
    def runpar = this.parcoll.reduce(operators.reducer)
    def companion = Reduce
  }
  
  object MediumReduce extends IterableBenchCompanion {
    override def defaultSize = 50000
    def benchName = "reduce-medium";
    def apply(sz: Int, p: Int, w: String) = new MediumReduce(sz, p, w)
  }
  
  class MediumReduce(val size: Int, val parallelism: Int, val runWhat: String)
  extends SeqBench with ParSeqViewBench[T, Coll, CollSeq] {
    def comparisonMap = collection.Map()
    def runseq = this.seqcoll.reduceLeft(operators.mediumreducer)
    def runpar = this.parcoll.reduce(operators.mediumreducer)
    def companion = Reduce
  }
  
  object ModifyThenReduce extends SeqBenchCompanion {
    override def defaultSize = 20000
    def benchName = "modify-then-reduce";
    def apply(sz: Int, p: Int, w: String) = new ModifyThenReduce(sz, p, w)
  }
  
  class ModifyThenReduce(val size: Int, val parallelism: Int, val runWhat: String)
  extends SeqBench with ParSeqViewBench[T, Coll, CollSeq] {
    val toadd = createSequential(size, parallelism)
    def comparisonMap = collection.Map()
    def runseq = {
      val modified = (seqcoll ++ toadd).drop(size).map(operators.mapper).++(toadd).take(size)
      modified.reduceLeft(operators.reducer)
    }
    def runpar = {
      val modified = (parcoll ++ toadd).drop(size).map(operators.mapper).++(toadd).take(size)
      modified.reduce(operators.reducer)
    }
    def companion = ModifyThenReduce
  }
  
  object ModifyThenForce extends SeqBenchCompanion {
    override def defaultSize = 20000
    def benchName = "modify-then-force";
    def apply(sz: Int, p: Int, w: String) = new ModifyThenForce(sz, p, w)
  }
  
  class ModifyThenForce(val size: Int, val parallelism: Int, val runWhat: String)
  extends SeqBench with ParSeqViewBench[T, Coll, CollSeq] {
    val toadd = createSequential(size, parallelism)
    def comparisonMap = collection.Map()
    def runseq = (seqcoll ++ toadd).drop(size).map(operators.mapper).++(toadd).take(size)
    def runpar = {
      val r: ParSeqView[T, ParSeq[T], Seq[T]] = (parcoll ++ toadd).drop(size).map(operators.mapper).++(toadd).take(size)
      r.force
    }
    def companion = ModifyThenForce
  }
  
}
























