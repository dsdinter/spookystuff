package org.tribbloid.spookystuff.entity

import java.util
import java.io.Serializable
import scala.collection.JavaConversions._

/**
 * Created by peng on 12/06/14.
 */
//TODO: verify this! document is really scarce
//The precedence of an inﬁx operator is determined by the operator’s ﬁrst character.
//Characters are listed below in increasing order of precedence, with characters on
//the same line having the same precedence.
//(all letters)
//|
//^
//&
//= !.................................................(new doc)
//< >
//= !.................................................(old doc)
//:
//+ -
//* / %
//(all other special characters)
class ActionPlan(val context: util.HashMap[String, Serializable] = null) extends Serializable {

  def this(context: util.HashMap[String, Serializable], as: Action*) = {
    this(context)
    this.+=(as)
  }

  // everything in this list is formatted
  val actions: util.List[Action] = new util.ArrayList()

  override def equals(a: Any): Boolean = a match {
    case a: ActionPlan => {
      if ((this.context == a.context) && (this.actions == a.actions)) return true
      else return false
    }
    case _ => return false
  }

  override def toString(): String = {
    "ActionPlan("+this.context.toString+","+this.actions.toString+")"
  }

  def +=(a: Action) {
    this.actions.add(a.format(context))
  }

  def +=(as: Seq[Action]) {
    as.foreach{
      a => this.actions.add(a.format(context))
    }
  }

  //will remove context of the parameter! cannot merge two context as they may have conflict keys
  def +=(ac: ActionPlan) {
    this.+=(ac.actions)
  }

  def + (a: Action): ActionPlan = {
    val result = new ActionPlan(this.context, this.actions: _*)
    result.+=(a)
    result
  }

  def + (as: Seq[Action]): ActionPlan = {
    val result = new ActionPlan(this.context, this.actions: _*)
    result.+=(as)
    result
  }

  def + (ac: ActionPlan): ActionPlan = {
    val result = new ActionPlan(this.context, this.actions: _*)
    result.+=(ac.actions)
    result
  }

  //  def +[T <: Serializable](key: String, value: T){
  //    this.context.put(key,value)
  //  }

  def interactions = actions.collect{
    case i: Interactive => i
  }.toSeq

  //only execute interactions and extract the final stage, if has no action will return an empty page
  def !(): Page = {
    val page = PageBuilder.resolveFinal(this.interactions: _*).copy(context = this.context)
    return page
  }

  //execute, left: if nothing extracted will return an empty page, not left: will return nothing
  def !!!(left: Boolean = false): Seq[Page] = {
    var pages = PageBuilder.resolve(this.actions: _*)
    if (pages.size==0 && left ==true) pages = pages.:+(PageBuilder.emptyPage())
    if (this.context !=null) {
      //has to use deep copy, one to many mapping and context may be modified later
      pages = pages.map { _.copy(context = new util.HashMap(this.context)) }
    }
    return pages
  }
}