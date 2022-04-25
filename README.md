# Collatz HTTP Stream
Computes [Collatz sequences](https://en.wikipedia.org/wiki/Collatz_conjecture) in two ways: one using Akka Actors and
another using Akka Graphs, and [JSON-stream](https://en.wikipedia.org/wiki/JSON_streaming) them using Akka HTTP.

## Actor
### Endpoint
/collatz-stream/actor/{initial-number}
### Topology
![Actor Topology](img/collatz-actor.svg?raw=true)

## Graph
### Endpoint
/collatz-stream/graph/{initial-number}
### Topology
![Graph Topology](img/collatz-graph.svg?raw=true)