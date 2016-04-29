package org.apache.spark.mllib.feature

import org.tartarus.snowball.SnowballStemmer

import org.apache.spark.sql.types.{DataType, StringType}
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.ml.param.{Param, ParamMap}
import org.apache.spark.ml.UnaryTransformer

class Stemmer(override val uid: String) extends UnaryTransformer[String, String, Stemmer] {

  def this() = this(Identifiable.randomUID("stemmer"))

  val language: Param[String] = new Param(this, "language", "stemming language (case insensitive).")
  def getLanguage: String = $(language)
  def setLanguage(value: String): this.type = set(language, value)

  setDefault(language -> "English")

  override protected def createTransformFunc: String => String = {
    val stemClass = Class.forName("org.tartarus.snowball.ext." + $(language).toLowerCase + "Stemmer")
    val stemmer = stemClass.newInstance.asInstanceOf[SnowballStemmer]
    originStr => try {
      originStr.split(" ").map((word) =>
        stemmer.setCurrent(word)
        stemmer.stem()
        stemmer.getCurrent).mkString(" ")
    } catch {
      case e: Exception => originStr
    }
  }

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == StringType, s"Input type must be string type but got $inputType.")
  }

  override protected def outputDataType: DataType = StringType

  override def copy(extra: ParamMap): Stemmer = defaultCopy(extra)
}
