$version: "2"

namespace learn.smithy.v04

structure Project {

  /// The title of the project
  @required
  title: String

  /// The description of the repository
  @required
  description: String

  /// The github url of the repository
  @required
  github: String

  /// Whether or not the project is an affiliate project, false by default
  affiliate: Boolean = false

  /// Which platforms the project supports
  @required
  platforms: String //PlatformList

  authors: ListOfAuthor

  fakeauthors: ListOfAuthor
}

// list PlatformList {
//   member: Platform
// }

// enum Platform {
//   JS = "js"
//   JVM = "jvm"
//   NATIVE = "native"
// }

list ListOfAuthor {
  member: Author
}

structure Author {
  name: String

  website: String
}
