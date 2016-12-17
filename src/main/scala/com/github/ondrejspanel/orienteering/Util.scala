package com.github.ondrejspanel.orienteering

import scala.collection.mutable

object Util {
  private def lcs_dp[T](a: Seq[T], b: Seq[T]): Seq[T] = {
    val lengths = Array.ofDim[Int](a.length + 1, b.length + 1)
    for (i <- a.indices; j <- b.indices) {
      lengths(i + 1)(j + 1) =
        if (a(i) == b(j)) lengths(i)(j) + 1
        else math.max(lengths(i + 1)(j), lengths(i)(j + 1))
    }
    val sb = mutable.ListBuffer[T]()
    var x = a.length
    var y = b.length
    while (x != 0 && y != 0) {
      if (lengths(x)(y) == lengths(x - 1)(y)) x -= 1
      else if (lengths(x)(y) == lengths(x)(y - 1)) y -= 1
      else {
        assert(a(x - 1) == b(y - 1))
        sb.append(a(x - 1))
        x -= 1
        y -= 1
      }
    }
    sb.reverse //.toList // Buffer is also Seq, no need to convert
  }

  def lcs[T](a: Seq[T], b: Seq[T]): Seq[T] = {
    lcs_dp(a, b)
  }

  def timeFormat(sec: Long) = {
    val minutes = sec / 60
    val seconds = sec - minutes * 60
    f"$minutes%d:$seconds%02d"
  }
}
