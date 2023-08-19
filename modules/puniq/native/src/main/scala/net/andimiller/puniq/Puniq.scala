package net.andimiller.puniq

import cats.effect.{ExitCode, IO, IOApp}
import net.andimiller.theta.Theta32

import scala.util.hashing.MurmurHash3

object Puniq extends IOApp {

  final val grouping = 32768

  override def run(args: List[String]): IO[ExitCode] =
    fs2.io
      .stdinUtf8[IO](32768)
      .through(fs2.text.lines[IO])
      .map(MurmurHash3.stringHash)
      .chunkN(grouping)
      .fold(
        Theta32.empty[16]
      ) { case (t, c) =>
        t.addHashes(c.iterator)
      }
      .compile
      .lastOrError
      .flatMap { result =>
        IO.println(result.getEstimate.toLong)
      }
      .as(ExitCode.Success)

}
