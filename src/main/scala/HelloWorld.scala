import akka.dispatch.{Dispatchers, Future}
import java.lang.IllegalStateException
import java.util.concurrent.CountDownLatch
import java.util.UUID

object HelloWorld extends App {
  var n = 1000

  def run() {
    //    var vector = new AtomicReference(Vector[String]())
    //    @volatile var vector = Vector[String]()
    val vector = collection.mutable.ArrayBuffer[String]()
    //    def update(s:String) {
    //      while(true) {
    //        val v = vector.get
    //        val nv = v :+ s
    //        if (vector.compareAndSet(v, nv))
    //          return
    //      }
    //    }
    val lock = AnyRef
    val cdl = new CountDownLatch(1)
    val cdl2 = new CountDownLatch(16)
    val cdl3 = new CountDownLatch(n)
    val messageDispatcher = Dispatchers.newExecutorBasedEventDrivenDispatcher("agents").build
    for (i <- 0 until n) {
      Future {
        val uuid = UUID.randomUUID()
        cdl2.countDown()
        cdl.await()
        lock.synchronized {
          //          vector = vector :+ uuid.toString
          vector += uuid.toString
        }
        //        update(uuid.toString)
        cdl3.countDown()
      }(messageDispatcher)
    }
    cdl2.await()
    cdl.countDown()
    cdl3.await()
    //    if (vector.get.size != n)
    //      throw new IllegalStateException()
    if (vector.size != n)
      throw new IllegalStateException()
  }

  for (i <- 1 to 100) {
    run()
  }

  import scala.compat.Platform.currentTime

  n = 1000000

  def timedRun() {
    val start = currentTime
    run()
    val dur = currentTime - start
    println("Time: " + dur)
  }

  for (i <- 1 to 100)
    timedRun()
}
