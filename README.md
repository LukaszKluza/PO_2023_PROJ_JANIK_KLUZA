# Darwin World

Simulation of evolution in a world inhabited by animals.

Every day of simulation consists of:
- Removing dead animals 🦴
- Animals movements 🐻
- Grass consumption by animals 🍏
- Reproduction ⚭
- New grass growth 🌱

Animals movement is defined by its genotype. Newborn animals inherit genotype after parents proportionally to their energy with mutations that can occur during reproduction. 🧬

Position for grass to grow on is random, maintaining the Pareto principle. There is an 80% chance that grass will grow on the 20% of the map near the equator.

Users can choose between two types of maps and two types of genotypes:
- Earth: right and left borders of the map are connected. If animal goes beyond the right border, it appears on the left side etc. Upper and lower borders are like walls. Animal bounces from them.
- Hell Portal: if animal goes beyond the border it is transferred to random generated position, but it loses energy (same amount as reproduction)

Types of genotype:
- Normal: genes are active in order from 1.
- Back and forward: genes are active from 1 to n (where n is a number of genes) and then in reverse order from n-1 back to 1.

Users can define various attributes of the simulation such as:
- Map size
- Initial number of animals
- Number of genes
- Initial animal energy
- Energy needed for reproduction
- Cost of reproduction
- Initial number of grass
- Number of grass generated every day
- Energy gained by consuming grass
- Minimum and maximum number of mutations
- Types of map and genotype
- Simulation speed

Users can save simulation statistics to a CSV file and load configuration from a JSON file. 
Additionally they can run multi simulation thanks to multithreading.

If many animals stand on the same field, conflicts over consumption and reproduction are solved by following aspects:
- Energy 🔋
- Age 🎂
- Number of children. If it is draw animal is chosen randomly. 👪

During simulation various stats are tracked. Users can also choose one of animals present on the map and track it with its stats or track every animal with most popular genotype.

*Darwin World* is created by **Szymon Janik** and **Łukasz Kluza** as a student project for an *Object-Oriented Programming* course at *AGH University of Kraków*.
