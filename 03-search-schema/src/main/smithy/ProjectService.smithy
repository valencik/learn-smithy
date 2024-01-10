$version: "2"

namespace learn.smithy.v03

use alloy#simpleRestJson
use learn.smithy.v03#Project

@simpleRestJson
service ProjectService {
  version: "1.0.0",
  operations: [ProjectSearch]
}

@http(method: "GET", uri: "/projects", code: 200)
@readonly
operation ProjectSearch {
  input: Query,
  output: SearchResult
}

structure Query {
  /// The query to run on the title field for projects
  @httpQuery("title")
  title: String
}

structure SearchResult {
  @required
  hits: ProjectList
}

list ProjectList {
    member: Project
}
