$version: "2"

namespace learn.smithy.v03

use alloy#simpleRestJson

structure Project {

  /// The title of the project
  @required
  @searchOptions(fieldType: "keyword")
  title: String

  /// The description of the repository
  @required
  @searchOptions(fieldType: "text")
  description: String

  /// The github url of the repository
  @required
  github: String

  /// Whether or not the project is an affiliate project, false by default
  affiliate: Boolean = false

  /// Which platforms the project supports
  @required
  platforms: PlatformList

}

list PlatformList {
  member: Platform
}

enum Platform {
  JS = "js"
  JVM = "jvm"
  NATIVE = "native"
}