/*
 * Copyright 2011 University of Wisconsin, Milwaukee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uwm.cs.scalabison;

import scala.collection.mutable.Map;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.HashMap;

import edu.uwm.cs.util.CharUtil;

class BisonGrammar() extends Grammar {
  val special : Nonterminal = add(ArtificialNonterminal("$accept",""));
  val error : Nonterminal = add(ErrorNonterminal());

  var prologue : String = "/* Generated by Scala-Bison version "+Version.version+" */\n";
  var extra : String = "";

  def addExtra(x : String) = { extra = x; }

  def addPrologue(x : List[Code]) = {
    // Can't get this to work:
    // prologue = (prologue /: x)(+)
    for (c <- x) {
      prologue += (c.toString);
    }
  }

  def setStart(sym : Nonterminal) = { 
    _start = sym;
    rules += new Rule(0,special,start :: end :: Nil,null,NoCode());
  }

  def addRule(new_rule : Rule) = {
    if (_start == null) setStart(new_rule.lhs);
    rules += new_rule;
  }
  
  override def add[T <: Symbol](name : T) : T = super.add(name)

  def getNT(name : String) : Nonterminal = {
    get(name) match {
      case x:Nonterminal => x
      case _ => throw new GrammarSpecificationError("Not a nonterminal: " + name);
    }
  }

  def getCLT(ch : Char) : Terminal = {
    val name : String = CharUtil.lit(ch);
    find(name) match {
      case Some(t:Terminal) => t
      case None => add(new CharLitTerminal(ch));
      case Some(s:Symbol) => throw new GrammarSpecificationError("Not a terminal: " + name);
    }
  }

  override def find(name : String) : Option[Symbol] = {
    val key = if (name.startsWith("$@")) name.substring(1) else name;
    super.find(key);
  }

}

