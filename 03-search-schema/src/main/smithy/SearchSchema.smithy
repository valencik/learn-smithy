$version: "2"

namespace learn.smithy.v03

/// This field is used in exact match search.
@trait(selector: "structure > member :test(> string, > list > member > string)")
structure keywordField {
  name: String
}

/// This field is used for full text search.
@trait(selector: "structure > member :test(> string, > list > member > string)")
structure textField {
  name: String
}