$version: "2"

namespace learn.smithy.v01

use alloy#simpleRestJson

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


structure Project {
  @required
  title: String
}
