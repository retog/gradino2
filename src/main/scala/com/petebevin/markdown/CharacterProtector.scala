package com.petebevin.markdown

import java.util.Collection
import java.util.HashMap
import java.util.Map
import java.util.Random
import CharacterProtector._
//remove if not needed
import _root_.scala.collection.JavaConversions._

object CharacterProtector {

  private val GOOD_CHARS = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"
}

class CharacterProtector {

  private var protectMap: Map[String, String] = new HashMap[String, String]()

  private var unprotectMap: Map[String, String] = new HashMap[String, String]()

  private var rnd: Random = new Random()

  def encode(literal: String): String = {
    if (!protectMap.containsKey(literal)) {
      addToken(literal)
    }
    protectMap.get(literal)
  }

  def decode(coded: String): String = unprotectMap.get(coded)

  def getAllEncodedTokens(): Collection[String] = unprotectMap.keySet

  private def addToken(literal: String) {
    val encoded = longRandomString()
    protectMap.put(literal, encoded)
    unprotectMap.put(encoded, literal)
  }

  private def longRandomString(): String = {
    val sb = new StringBuffer()
    val CHAR_MAX = GOOD_CHARS.length
    for (i <- 0 until 20) {
      sb.append(GOOD_CHARS.charAt(rnd.nextInt(CHAR_MAX)))
    }
    sb.toString
  }

  override def toString(): String = protectMap.toString
}
