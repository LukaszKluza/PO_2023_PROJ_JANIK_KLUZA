package org.example.model;

import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Math.max;


public class Genotype {
    private final int selectedType;
    private int currentGen;
    private int genNumbers;
    private int genotypeDirection;
    private List<Gen> Gens;

    public Genotype(int genNumbers, int genotype){
        selectedType = genotype;
        genotypeDirection = genotype == 1 ? 1 : -1;
        currentGen = genotype == 1 ? -1 : 1;
        newGenotype(genNumbers);
        genotypeGenerate();
    }

    public Genotype (Animal a, Animal b, int genotype){
        selectedType = genotype;
        genotypeDirection = genotype == 1 ? 1 : -1;
        newGenotype(a.getGenNumbers());
        inheritGenotype(a, b);
    }

    private void inheritGenotype(Animal firstAnimal, Animal secondAnimal) {
        Random random = new Random();
        int sumEnergy = firstAnimal.getEnergy() + secondAnimal.getEnergy();
        int numGensA = firstAnimal.getEnergy()*genNumbers/sumEnergy;
        int numGensB = genNumbers-numGensA;
        int maxi = max(numGensA,numGensB);
        int side = random.nextInt(2);
        int point = side == 0 ? maxi : genNumbers-maxi;
        Animal first = numGensA > numGensB && side == 0 || (numGensA < numGensB && side == 1) ? firstAnimal : secondAnimal;
        Animal second = first.equals(firstAnimal) ? secondAnimal : firstAnimal;
        copyGens(0,point, first);
        copyGens(point,genNumbers, second);
    }

    private void newGenotype(int genNumbers) {
        this.genNumbers = genNumbers;
        Gens = new ArrayList<>();
    }
    public void copyGens(int from, int to, Animal parent){
        for (int i = from; i<to; ++i){
            Gens.add(parent.getAnimalGenotype().getGens(i));
        }
    }
    private void genotypeGenerate(){
        Random random = new Random();
        List<Gen> values = Arrays.asList(Gen.values());

        IntStream.range(0, genNumbers).forEach(i -> Gens.add(values.get(random.nextInt(values.size()))));
    }
    public Gen nextGen(){
        if(selectedType == 2 && (currentGen == 0 || currentGen == genNumbers-1)) {
            genotypeDirection *= (-1);
        }
        currentGen += genotypeDirection;
        return Gens.get(currentGen%genNumbers);
    }

    public int nextGenIndex(){
        int genotypeDirectionCopy = genotypeDirection;
        if(selectedType == 2 && (currentGen == 0 || currentGen == genNumbers-1)) {
            genotypeDirectionCopy *= (-1);
        }
        return (currentGen+genotypeDirectionCopy)%genNumbers;
    }

    public int getGenNumbers() {
        return genNumbers;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Gen gen : Gens) {
            result.append(gen.toString());
            result.append(' ');

        }
        return result.toString();
    }

    public Gen getGens(int i) {
        return Gens.get(i);
    }
    public void setGens(int i, Gen val) {
        Gens.set(i, val);
    }

    public List<Gen> getGens() {
        return Collections.unmodifiableList(Gens);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genotype genotype)) return false;
        return selectedType == genotype.selectedType && currentGen == genotype.currentGen && genNumbers == genotype.genNumbers && genotypeDirection == genotype.genotypeDirection && Objects.equals(Gens, genotype.Gens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedType, currentGen, genNumbers, genotypeDirection, Gens);
    }
}
