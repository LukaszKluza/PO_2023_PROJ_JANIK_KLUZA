package org.example.model;

import java.util.*;
import java.util.stream.Collectors;

abstract class AbstractWorldMap implements WorldMap{
    private final int ID;
    protected final Boundary bounds;
    private final List<MapChangeListener> observers = new ArrayList<>();
    protected final Map<Vector2d, List<WorldElement>> mapElements = new HashMap<>();
    public abstract Vector2d moveTo(Vector2d position, Vector2d directionVector, Animal animal);

    public Boundary getCurrentBounds() {
        return bounds;
    }
    public AbstractWorldMap(int ID, int width, int height) {
        this.ID = ID;
        bounds = new Boundary(new Vector2d(1,1),new Vector2d(width, height));
    }
    public boolean canMoveTo(Vector2d position) {
        return bounds.lowerLeft().precedes(position) && bounds.upperRight().follows(position);
    }
    @Override
    public void place(WorldElement element) {
        Vector2d position = element.getPosition();
        List<WorldElement> objectsAt = objectAt(position);
        objectsAt.add(element);
        mapElements.put(position, objectsAt);
    }

    @Override
    public void move(Animal animal) {
        Vector2d oldPosition = animal.getPosition();
        animal.move(this);
        Vector2d newPosition = animal.getPosition();
        List<WorldElement> objectsAt = objectAt(newPosition);
        objectsAt.add(animal);
        List<WorldElement> objectsAtOldPosition = mapElements.get(oldPosition);
        objectsAtOldPosition.remove(animal);
        mapElements.put(newPosition, objectsAt);
    }

    @Override
    public void removeDeadAnimals(Animal animal, int day) {
        if(animal.getEnergy() <= 0 && animal.getDayOfDeath().isEmpty()){
            animal.setDayOfDeath(day);
            List<WorldElement> objectsAtAnimalPosition = mapElements.get(animal.getPosition());
            objectsAtAnimalPosition.remove(animal);
        }
    }

    @Override
    public void reproduction(List<Animal> simulationAnimalsList){
        Map<Vector2d, List<Animal>> mapAnimals = new HashMap<>();

        mapElements.forEach((position, elements) -> {
            List<Animal> animalElements = getAnimals(elements);

            if (!animalElements.isEmpty()) {
                mapAnimals.put(position, animalElements);
            }
        });

        AnimalComparator animalComparator = new AnimalComparator();
        mapAnimals.forEach((position, elements) -> {
            if(elements.size()>1){
                elements.sort(animalComparator);
                int readyForReproductionEnergy = elements.get(1).getConfiguration().getReadyEnergy();
                if(elements.get(1).getEnergy() >= readyForReproductionEnergy)bornAnimal(elements.get(0), elements.get(1), simulationAnimalsList);
            }
        });
    }

    private void bornAnimal(Animal a, Animal b, List<Animal> simulationAnimalsList) {
        Animal animal =  new Animal(a,b, simulationAnimalsList.size());
        a.increaseNumberOfChildren();
        b.increaseNumberOfChildren();
        a.subtractReproductionEnergy();
        b.subtractReproductionEnergy();
        place(animal);
        simulationAnimalsList.add(animal);
    }

    @Override
    public void consumption(Set<Grass> grassSet){
        Map<Grass, Animal> consumptionList = new HashMap<>();

        grassSet.forEach(grass -> {
            List<Animal> animalList = getAnimals(mapElements.get(grass.position()));

            AnimalComparator comparator = new AnimalComparator();
            if(!animalList.isEmpty()) {
                animalList.sort(comparator);
                consumptionList.put(grass, animalList.get(0));
            }
        });

        consumptionList.forEach((grass, animal) -> consumeGrass(grass, animal, grassSet));
    }

    private void consumeGrass(Grass grass, Animal animal, Set<Grass> grassSet) {
        grassSet.remove(grass);

        List<WorldElement> objectsAtGrassPosition = mapElements.get(grass.getPosition());
        objectsAtGrassPosition.remove(grass);

        animal.consumeGrass();
    }

    @Override
    public List<WorldElement> objectAt(Vector2d position) {
        if(mapElements.get(position) != null){
            return mapElements.get(position);
        }
        return new ArrayList<>();
    }

    public boolean isOccupiedByAnimal(Vector2d position) {
        List<Animal> animalList = getAnimals(mapElements.get(position));
        return !animalList.isEmpty();
    }

    public List<Animal> getAnimals(List<WorldElement> elements) {
        return elements.stream()
                .filter(element -> element instanceof Animal)
                .map(element -> (Animal) element)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Vector2d, List<WorldElement>> getElements() {
        return Collections.unmodifiableMap(mapElements);
    }

    @Override
    public int getID() {
        return ID;
    }

    public void registerObserver(MapChangeListener observer) {
        observers.add(observer);
    }
    public void unregisterObserver(MapChangeListener observer) {
        observers.remove(observer);
    }

    @Override
    public void mapChanged(int day) {
        for (MapChangeListener observer : observers) {
            observer.mapChanged(this, day);
        }
    }
}
