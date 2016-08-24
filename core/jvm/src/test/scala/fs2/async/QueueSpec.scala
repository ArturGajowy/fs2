package fs2
package async

class QueueSpec extends Fs2Spec {
  "Queue" - {
    "unbounded producer/consumer" in {
      forAll { (s: PureStream[Int]) =>
        withClue(s.tag) {
          runLog(Stream.eval(async.unboundedQueue[Task,Int]).flatMap { q =>
            q.dequeue.merge(s.get.evalMap(q.enqueue1).drain).take(s.get.toVector.size)
          }) shouldBe s.get.toVector
        }
      }
    }
    "circularBuffer" in {
      forAll { (s: PureStream[Int], maxSize: SmallPositive) =>
        withClue(s.tag) {
          runLog(Stream.eval(async.circularBuffer[Task,Option[Int]](maxSize.get + 1)).flatMap { q =>
            s.get.noneTerminate.evalMap(q.enqueue1).drain ++ q.dequeue.through(pipe.unNoneTerminate)
          }) shouldBe s.get.toVector.takeRight(maxSize.get)
        }
      }
    }
    "dequeueBatch unbounded" in {
      forAll { (s: PureStream[Int], batchSize: SmallPositive) =>
        withClue(s.tag) {
          runLog(Stream.eval(async.unboundedQueue[Task,Option[Int]]).flatMap { q =>
            s.get.noneTerminate.evalMap(q.enqueue1).drain ++ Stream.constant(batchSize.get).through(q.dequeueBatch).through(pipe.unNoneTerminate)
          }) shouldBe s.get.toVector
        }
      }
    }
    "dequeueBatch circularBuffer" in {
      forAll { (s: PureStream[Int], maxSize: SmallPositive, batchSize: SmallPositive) =>
        withClue(s.tag) {
          runLog(Stream.eval(async.circularBuffer[Task,Option[Int]](maxSize.get + 1)).flatMap { q =>
            s.get.noneTerminate.evalMap(q.enqueue1).drain ++ Stream.constant(batchSize.get).through(q.dequeueBatch).through(pipe.unNoneTerminate)
          }) shouldBe s.get.toVector.takeRight(maxSize.get)
        }
      }
    }
  }
}
