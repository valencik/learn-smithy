$version: "2"

namespace learn.smithy.v03

enum FieldType {
  KEYWORD = "keyword"
  TEXT = "text"
}

@trait(selector: "structure > member :test(> string, > list > member > string)")
structure searchOptions {
  @required
  fieldType: FieldType
}