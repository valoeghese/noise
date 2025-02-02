package me.salamander.noisetest.modules.source;

import io.github.antiquitymc.nbt.CompoundTag;
import me.salamander.noisetest.modules.NoiseModule;
import me.salamander.noisetest.modules.types.ArraySourceModule;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;

public class NoiseSourceModule extends ArraySourceModule {
    private long seed;
    private DoubleBinaryOperator[] noiseSamplers;
    private NoiseType noiseType;

    public NoiseSourceModule(NoiseType type) {
        this(6, new Random().nextLong(), type);
    }

    public NoiseSourceModule(int octaves, long seed, NoiseType type) {
    	super(4);
	    this.seed = seed;
	    initParameters();
	    this.parameters[0] = octaves;
	    noiseSamplers = new DoubleBinaryOperator[octaves];
	    this.noiseType = type;

	    createSamplers(true, type);
    }

    private void initParameters(){
        parameters[1] = 1.0;
        parameters[2] = 0.5;
        parameters[3] = 2.0;
    }

    private void createSamplers(boolean regenerate, NoiseType type) {
    	int octaves = (int)parameters[0];

        DoubleBinaryOperator[] newSamplers = new DoubleBinaryOperator[octaves];
        Random random = new Random(seed);

        for(int i = 0; i < octaves; i++){
            boolean usedPrevious = false;
            if(noiseSamplers != null && !regenerate) {
                if (i < noiseSamplers.length) {
                    newSamplers[i] = noiseSamplers[i];
                    usedPrevious = true;
                    random.nextLong();
                }
            }

            if(!usedPrevious) {
                newSamplers[i] = type.apply(random.nextLong());
            }
        }

        noiseSamplers = newSamplers;
    }

    @Override
    public double sample(double x, double y) {
        double total = 0;
	    int octaves = (int) parameters[0];
	    double frequency = parameters[1];
	    double persistence = parameters[2];
	    double lacunarity = parameters[3];

        for(int i = 0; i < octaves; i++)
            total += Math.pow(persistence, i) * noiseSamplers[i].applyAsDouble(x * frequency * Math.pow(lacunarity, i), y * frequency * Math.pow(lacunarity, i));
        return total;
    }

    @Override
    public void setSeed(long s) {
        this.seed = s;
        createSamplers(true, this.noiseType);
    }

    public long getSeed() {
        return seed;
    }

    public void setPersistence(double persistence) {
        this.parameters[2] = persistence;
    }

    public void setLacunarity(double lacunarity) {
        this.parameters[3] = lacunarity;
    }

    public void setFrequency(double frequency) {
        this.parameters[1] = frequency;
    }

    public void setNumOctaves(int octaves){
        if((int)parameters[0] == octaves) return;
        parameters[0] = octaves;
        createSamplers(false, this.noiseType);
    }

    @Override
    public void setParameter(int index, double value) {
    	if(index == 0) setNumOctaves((int) value);
        else super.setParameter(index, value);
    }


    public double getFrequency() {
        return parameters[1];
    }

    @Override
    public void readNBT(CompoundTag tag, List<NoiseModule> sourceLookup) {
        super.readNBT(tag, sourceLookup);

        seed = tag.getLong("seed");

        noiseType = NoiseType.fromString(tag.getString("type"));
        createSamplers(true, noiseType);
    }

    @Override
    public void writeNBT(CompoundTag tag, IdentityHashMap<NoiseModule, Integer> indexLookup) {
        super.writeNBT(tag, indexLookup);
        tag.putString("type", this.noiseType.toString());
        tag.putLong("seed", seed);
    }

    @Override
	public String getNodeRegistryName() {
		return "NoiseSource";
	}

    public NoiseType getNoiseType() {
        return noiseType;
    }
}
