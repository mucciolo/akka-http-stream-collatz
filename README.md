# Collatz HTTP Stream
Computes [Collatz sequences](https://en.wikipedia.org/wiki/Collatz_conjecture) in two ways: one using
Akka Actors and another using Akka Stream Graphs, and
[JSON-stream](https://en.wikipedia.org/wiki/JSON_streaming) them using Akka HTTP.

## Actor
### Endpoint
/collatz-stream/actor/{initial-number}
### Flow
![Actor Topology](img/collatz-actor-flow.svg?raw=true)

## Graph
### Endpoint
/collatz-stream/graph/{initial-number}
### Flow
![Graph Topology](img/collatz-graph-flow.svg?raw=true)