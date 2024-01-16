package org.example.simulation;

import org.example.data.SimulationConfiguration;
import org.example.model.*;


import java.util.List;

public class Simulation extends AbstractSimulation implements Runnable{
    private final DayCycleSimulation dayCycleSimulation;
    private volatile boolean shouldStop = false;

    public Simulation(SimulationConfiguration configuration, WorldMap worldMap, int ID) {
        super(configuration,worldMap, ID);
        for(int i=0; i<configuration.getAnimalsNumber(); ++i){
            Animal animal = new Animal(configuration, i);
            listAnimals.add(animal);
            this.map.place(animal);
        }
        GrassPositionGenerator grassPositionGenerator = new GrassPositionGenerator(grassNum, map.getCurrentBounds(), map);
        for (Vector2d position : grassPositionGenerator) {
            Grass grass = new Grass(position);
            setGrass.add(grass);
            this.map.place(grass);
        }
        dayCycleSimulation = new DayCycleSimulation(listAnimals, setGrass, configuration, worldMap, ID);
    }

    @Override
    public void run() {
        StartSimulation();
    }

    private void StartSimulation() {
        while (!shouldStop){
            dayCycleSimulation.removeDeadAnimals();
            dayCycleSimulation.move();
            dayCycleSimulation.consumption();
            dayCycleSimulation.reproduction();
            dayCycleSimulation.grassGrowth();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopSimulation() {
        shouldStop = true;
    }
    public void continueSimulation(){
        shouldStop = false;
        StartSimulation();
    }
    public List<Animal> getAnimalsList() {
        return listAnimals;
    }
}