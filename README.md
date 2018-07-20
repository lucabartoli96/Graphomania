


<image height=200 src="https://github.com/lucabartoli96/Graphomania/blob/master/screenshots/icon.svg">
  
# Graphomania

Graphomania is an android app for interactive graph creation and algorithms visualization, written in Kotlin.

## Creation

On the main page you can start a new project or continue with an old one, there are two types of graph supported: *graph* (short for "undirected non-weighted graph") and *automaton* (short for finite state machine diagram). Once a project is open (finding a blank screen for a new project), graph can be created/modified:

- Create nodes with double tap, for automata, double tap on a node turns it into final
- Create edges between two nodes clicking both of them with two fingers
- For automata, to create a loop click with one finger anywhere on the screen and on the node with the other
- Delete graph components by throwing them away (dragging them quickly)
- For automata, keep pressed an edge to change label symbols, and a node to turn it into start node

## Algorithms

On the top right menu you can choose algorithms to run or other operations. For graphs DFS and BFS are implemented, for Automata the execution on an input word. Soon new procedures will be added.

## Other Features

As mentioned above projects can be saved and modified, they can of course be deleted, and also exported on the gallery as a .PNG image.
