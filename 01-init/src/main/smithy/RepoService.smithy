$version: "2"

namespace learn.smithy

use alloy#simpleRestJson

@simpleRestJson
service RepoService {
  version: "1.0.0",
  operations: [RepoSearch]
}

@http(method: "GET", uri: "/repos", code: 200)
operation RepoSearch {
  input: Query,
  output: SearchResult
}

structure Query {

  /// The name of the repository to search for
  @httpQuery("name")
  name: String

}

structure SearchResult {

  @required
  hits: RepoList

}

list RepoList {
    member: Repo
}

structure Repo {
  @required
  name: String
}
