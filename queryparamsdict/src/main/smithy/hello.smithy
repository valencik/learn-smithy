$version: "2"

namespace hello

use alloy#simpleRestJson

@simpleRestJson
service HelloWorldService {
  version: "1.0.0",
  operations: [Hello]
}

@http(method: "GET", uri: "/hello/{name}", code: 200)
operation Hello {
  input: Person,
  output: Greeting
}

structure Person {
  @httpLabel
  @required
  name: String,

  /// The various towns you like
  @httpQuery("town")
  town: TownList

  /// Arbitrary tags
  @httpQueryParams
  tags: MapOfStrings
}

list TownList {
  member: String
}

map MapOfStrings {
    key: String
    value: ListOfStrings
}

list ListOfStrings {
    member: String
}

structure Greeting {
  @required
  message: String
}
