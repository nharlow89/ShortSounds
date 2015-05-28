package com.sloths.speedy.shortsounds.controller;

/**
 * This class represents the EQ & Reverb effect controllers
 * which main usage is for resetting the model to its previous values
 * and storing those previous values
 */
public abstract class EffectController {
    /**
     * Resets the model for the effect to cancel values
     * or default values
     */
    public abstract void resetModel();

}
