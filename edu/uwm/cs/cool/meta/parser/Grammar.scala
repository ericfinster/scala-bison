/* LALR(1) Parser generation for Scala
 * John Boyland
 * This file may be used, copied and/or modified for any purpose.
 */
package edu.uwm.cs.cool.meta.parser;

import scala.collection.mutable.Map;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.HashMap;

class Grammar {
  protected val symbolmap : Map[String,Symbol] = new HashMap;
  protected val symbols : ArrayBuffer[Symbol] = new ArrayBuffer;

  protected var _start : Nonterminal = null;
  def start : Nonterminal = _start;

  val end : Terminal = new Terminal("YYEOF","");

  def all_symbols : Iterable[Symbol] = symbols;

  def find(name : String) : Option[Symbol] = symbolmap.get(name);

  protected def add[T <: Symbol](symbol : T) : T = {
    if (symbolmap.contains(symbol.name)) {
      return symbolmap.apply(symbol.name).asInstanceOf[T];
    }
    symbols += symbol;
    symbolmap.put(symbol.name,symbol); symbol
  }

  def get(name : String) : Symbol = {
    find(name) match {
      case Some(s) => s;
      case None => add(new Nonterminal(name,""));
    }
  }

  // add(start);
  // add(epsilon);
  add(end);
  symbolmap.put("$end",end); // another name
  
  val rules : ArrayBuffer[Rule] = new ArrayBuffer[Rule]();

  override def toString : String = toString(false);

  def toString(showRecognition : Boolean) : String = {
    val sb : StringBuilder = new StringBuilder("Grammar\n");
    var lastNT : Nonterminal = null;
    var i : Int = 0;
    for (rule <- rules) {
      val nt : Nonterminal = rule.lhs;
      if (nt != lastNT) sb append '\n';
      if (i < 10000) sb append ' ';
      if (i < 1000) sb append ' ';
      if (i < 100) sb append ' ';
      if (i < 10) sb append ' ';
      sb append i;
      i += 1;
      sb append ' ';
      if (nt == lastNT) {
	for (_ <- (0 until nt.name.length)) {
	  sb append ' ';
	}
	sb append '|';
      } else {
	sb append (nt.name);
	sb append ':';
	lastNT = nt;
      }
      if (rule.rhs == Nil) {
	if (showRecognition) sb append " .";
	sb append " /* empty */";
      } else {
	var rp : Int = rule.recognitionPoint;
	for (sym <- rule.rhs) {
	  if (showRecognition && rp == 0) { sb append " ."; }
	  rp -= 1
	  sb append ' ';
	  sb append (sym.name);
	}
	if (showRecognition && rp == 0) { sb append " ."; }
      }
      sb append '\n';
    }
    sb toString
  }
}