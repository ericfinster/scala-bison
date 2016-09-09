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

import scala.io.Source
import scala.collection.mutable.Set
import scala.collection.mutable.HashSet;


/**
 * LALR(1) Parse tables in Bison format
 */
class BisonTable(val bison : BisonGrammar) extends Table(bison) {

  def fromFile(filename : String) = {
    if (Options.meta_debug) {
      println("Reading bison output " + filename);
    }
    val s : Source = Source.fromFile(filename);
    getStates(s);
    s.reset();
    getActions(Source.fromFile(filename))
  }
  
  private def skipToStates(it : Iterator[String]) : Int = {
    for (s <- it) {
      if (s.startsWith("state ") || s.startsWith("State ")) {
        val stateNum = Integer.parseInt(s.substring(6,s.length))
        return stateNum;
      }
    }
    -1
  }

  private def getStates(s : Source) = {
    val it : Iterator[String] = s.getLines;
    while (skipToStates(it) >= 0) {
      it.next;
      states += getState(it)
    }
  }

  private def getState(it : Iterator[String]) : State = {
    val is : Set[Item] = new HashSet[Item]();
    /*if (expectTwoEmpty) {
      val first = it.next;
      if (!first.equals("\n")) {
        println("Unexpected bison output file format: expected blank line: " + first);
        System.exit(-1);
      }
    }*/
    for (s <- it) {
      if (s.equals("") || s.equals("\n")) {
        return new State(states.length,is);
      } else {
        is += Item.fromLine(grammar,s);
      }
    }
    throw new GrammarSpecificationError("unexpected EOF")
  }

  private def getActions(s : Source) = {
    val it : Iterator[String] = s.getLines;
    var i : Int = 0;
    while (skipToStates(it) >= 0) {
      // println("Getting actions for state " + i);
      it.next;
      val state : State = states(i);
      val check : State = getState(it);
      if (!state.equals(check)) {
        throw new GrammarSpecificationError("State mismatch: " + state + " != " + check);
      }
      getAction(state,it)
      // println(state);
      i += 1;
    }
  }

  private def getAction(state : State, it : Iterator[String]) : Unit = {
    var empty : Boolean = false;
    for (s <- it) {
      if (s.equals("\n") || s.equals("")) {
        if (empty) return ();
        empty = true;
      } else {
        empty = false;
        val pieces : Array[String] = s.substring(4,s.length).split(" +");
        if (pieces(1).charAt(0) != '[' ) {
          pieces(1) match {
          case "shift," => {
            val next : Int = Integer.parseInt(pieces(6));
          grammar.find(pieces(0)) match {
          case Some(nt:ArtificialNonterminal) =>
          state.putGoto(nt,states(next))
          case Some(t:Terminal) => 
          state.putAction(t,ShiftAction(states(next)));
          case s => throw new GrammarSpecificationError("Unknown terminal "
              +pieces(0));
          }
          }
          case "reduce" => {
            val num : Int = Integer.parseInt(pieces(4));
            val rule : Rule = grammar.rules(num);
            val action : Action = ReduceAction(rule);
            if (pieces(0).equals("$default")) {
              state.default = action
            } else {
              grammar.find(pieces(0)) match {
              case Some(t:Terminal) => state.putAction(t,action)
              case _ => throw new GrammarSpecificationError("Unknown terminal"
                  +pieces(0));
            }
          }
          }
          case "go" => {
            val next : Int = Integer.parseInt(pieces(4));
            grammar.find(pieces(0)) match {
            case Some(nt:Nonterminal) => state.putGoto(nt,states(next))
            case _ => throw new GrammarSpecificationError("Unknown nonterm "
                +pieces(0));
            }
          }
          case "accept" => {
            state.default = AcceptAction();
          }
          case "error" => {
            grammar.find(pieces(0)) match {
            case Some(t:Terminal) => 
            state.putAction(t,ErrorAction(pieces(2)));
            case _ => throw new GrammarSpecificationError("Unknown terminal "
                +pieces(0));
            }
          }
          case _ => {
            for (s <- pieces) {
              println("Piece: " + s);
            }
            throw new GrammarSpecificationError("Unknown action " + pieces(1) + " in " + pieces);
          }
          }
        }
      }
    }
  }
}

object BisonTable {
  def main(args : Array[String]) = {
    for (s <- args) {
      val scanner : BisonScanner = new BisonScanner(Source.fromFile(s+".y"))
    val parser : BisonParser = new BisonParser();
      parser.reset(s+".y",scanner);
      if (parser.yyparse()) {
        println(parser.result);
        val table : BisonTable = new BisonTable(parser.result);
        table.fromFile(s+".output");
        println(table);
        table.setRecognitionPoints();
      }
    }
  }
}
